package featurecat.lizzie.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

public class BasicLizziePaneUI extends LizziePaneUI implements SwingConstants {
  protected LizziePane lizziePane;
  private boolean floating;
  private int floatingX;
  private int floatingY;
  private JFrame floatingFrame;
  private RootPaneContainer floatingLizziePane;
  protected DragWindow dragWindow;
  private Container dockingSource;
  private int dockingSensitivity = 0;
  protected int focusedCompIndex = -1;
  private Dimension originSize = null;

  protected Color dockingColor = null;
  protected Color floatingColor = null;
  protected Color dockingBorderColor = null;
  protected Color floatingBorderColor = null;

  protected MouseInputListener dockingListener;
  protected PropertyChangeListener propertyListener;

  protected ContainerListener lizziePaneContListener;
  protected FocusListener lizziePaneFocusListener;
  private Handler handler;

  protected String constraintBeforeFloating;

  @Deprecated protected KeyStroke upKey;
  @Deprecated protected KeyStroke downKey;
  @Deprecated protected KeyStroke leftKey;
  @Deprecated protected KeyStroke rightKey;

  private static String FOCUSED_COMP_INDEX = "LizziePane.focusedCompIndex";

  public static ComponentUI createUI(JComponent c) {
    return new BasicLizziePaneUI();
  }

  public void installUI(JComponent c) {
    lizziePane = (LizziePane) c;

    // Set defaults
    installDefaults();
    installComponents();
    // TODO Default disabled drag
    //    installListeners();
    //    installKeyboardActions();

    // Initialize instance vars
    dockingSensitivity = 0;
    floating = false;
    floatingX = floatingY = 0;
    floatingLizziePane = null;

    LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);

    if (c.getClientProperty(FOCUSED_COMP_INDEX) != null) {
      focusedCompIndex = ((Integer) (c.getClientProperty(FOCUSED_COMP_INDEX))).intValue();
    }
  }

  public void uninstallUI(JComponent c) {

    // Clear defaults
    uninstallDefaults();
    uninstallComponents();
    uninstallListeners();
    //    uninstallKeyboardActions();

    // Clear instance vars
    if (isFloating()) setFloating(false, null);

    floatingLizziePane = null;
    dragWindow = null;
    dockingSource = null;

    c.putClientProperty(FOCUSED_COMP_INDEX, Integer.valueOf(focusedCompIndex));
  }

  protected void installDefaults() {
    LookAndFeel.installBorder(lizziePane, "LizziePane.border");
    LookAndFeel.installColorsAndFont(
        lizziePane, "LizziePane.background", "LizziePane.foreground", "LizziePane.font");
    // Toolbar specific defaults
    if (dockingColor == null || dockingColor instanceof UIResource)
      dockingColor = UIManager.getColor("LizziePane.dockingBackground");
    if (floatingColor == null || floatingColor instanceof UIResource)
      floatingColor = UIManager.getColor("LizziePane.floatingBackground");
    if (dockingBorderColor == null || dockingBorderColor instanceof UIResource)
      dockingBorderColor = UIManager.getColor("LizziePane.dockingForeground");
    if (floatingBorderColor == null || floatingBorderColor instanceof UIResource)
      floatingBorderColor = UIManager.getColor("LizziePane.floatingForeground");
  }

  protected void uninstallDefaults() {
    LookAndFeel.uninstallBorder(lizziePane);
    dockingColor = null;
    floatingColor = null;
    dockingBorderColor = null;
    floatingBorderColor = null;

    //    installNormalBorders(lizziePane);
  }

  protected void installComponents() {}

  protected void uninstallComponents() {}

  public void installListeners() {
    dockingListener = createDockingListener();

    if (dockingListener != null) {
      lizziePane.addMouseMotionListener(dockingListener);
      lizziePane.addMouseListener(dockingListener);
    }

    propertyListener = createPropertyListener(); // added in setFloating
    if (propertyListener != null) {
      lizziePane.addPropertyChangeListener(propertyListener);
    }

    lizziePaneContListener = createLizziePaneContListener();
    if (lizziePaneContListener != null) {
      lizziePane.addContainerListener(lizziePaneContListener);
    }

    lizziePaneFocusListener = createLizziePaneFocusListener();

    if (lizziePaneFocusListener != null) {
      // Put focus listener on all components in lizziePane
      Component[] components = lizziePane.getComponents();

      for (Component component : components) {
        component.addFocusListener(lizziePaneFocusListener);
      }
    }
  }

  public void uninstallListeners() {
    if (dockingListener != null) {
      lizziePane.removeMouseMotionListener(dockingListener);
      lizziePane.removeMouseListener(dockingListener);

      dockingListener = null;
    }

    if (propertyListener != null) {
      lizziePane.removePropertyChangeListener(propertyListener);
      propertyListener = null; // removed in setFloating
    }

    if (lizziePaneContListener != null) {
      lizziePane.removeContainerListener(lizziePaneContListener);
      lizziePaneContListener = null;
    }

    if (lizziePaneFocusListener != null) {
      // Remove focus listener from all components in lizziePane
      Component[] components = lizziePane.getComponents();

      for (Component component : components) {
        component.removeFocusListener(lizziePaneFocusListener);
      }

      lizziePaneFocusListener = null;
    }
    handler = null;
  }

  protected void installKeyboardActions() {
    InputMap km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    SwingUtilities.replaceUIInputMap(lizziePane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);

    //    LazyActionMap.installLazyActionMap(lizziePane, LizziePaneUI.class,
    // "LizziePane.actionMap");
  }

  InputMap getInputMap(int condition) {
    if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
      return (InputMap) UIManager.get("LizziePane.ancestorInputMap", lizziePane.getLocale());
    }
    return null;
  }

  protected void uninstallKeyboardActions() {
    SwingUtilities.replaceUIActionMap(lizziePane, null);
    SwingUtilities.replaceUIInputMap(
        lizziePane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
  }

  protected void navigateFocusedComp(int direction) {
    int nComp = lizziePane.getComponentCount();
    int j;

    switch (direction) {
      case EAST:
      case SOUTH:
        if (focusedCompIndex < 0 || focusedCompIndex >= nComp) break;

        j = focusedCompIndex + 1;

        while (j != focusedCompIndex) {
          if (j >= nComp) j = 0;
          Component comp = lizziePane.getComponentAtIndex(j++);

          if (comp != null && comp.isFocusable() && comp.isEnabled()) {
            comp.requestFocus();
            break;
          }
        }

        break;

      case WEST:
      case NORTH:
        if (focusedCompIndex < 0 || focusedCompIndex >= nComp) break;

        j = focusedCompIndex - 1;

        while (j != focusedCompIndex) {
          if (j < 0) j = nComp - 1;
          Component comp = lizziePane.getComponentAtIndex(j--);

          if (comp != null && comp.isFocusable() && comp.isEnabled()) {
            comp.requestFocus();
            break;
          }
        }

        break;

      default:
        break;
    }
  }

  /**
   * No longer used, use LizziePaneUI.createFloatingWindow(LizziePane)
   *
   * @see #createFloatingWindow
   */
  protected JFrame createFloatingFrame(LizziePane lizziePane) {
    Window window = SwingUtilities.getWindowAncestor(lizziePane);
    JFrame frame =
        new JFrame(
            lizziePane.getName(), (window != null) ? window.getGraphicsConfiguration() : null) {
          // Override createRootPane() to automatically resize
          // the frame when contents change
          protected JRootPane createRootPane() {
            JRootPane rootPane =
                new JRootPane() {
                  private boolean packing = false;

                  public void validate() {
                    super.validate();
                    if (!packing) {
                      packing = true;
                      pack();
                      packing = false;
                    }
                  }
                };
            rootPane.setOpaque(true);
            return rootPane;
          }
        };
    frame.getRootPane().setName("LizziePane.FloatingFrame");
    frame.setResizable(false);
    frame.setSize(lizziePane.getSize());
    WindowListener wl = createFrameListener();
    frame.addWindowListener(wl);
    return frame;
  }

  /**
   * Creates a window which contains the lizziePane after it has been dragged out from its container
   *
   * @return a <code>RootPaneContainer</code> object, containing the lizziePane.
   * @since 1.4
   */
  protected RootPaneContainer createFloatingWindow(LizziePane lizziePane) {
    class LizziePaneDialog extends JDialog {
      public LizziePaneDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
      }

      public LizziePaneDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
      }

      // Override createRootPane() to automatically resize
      // the frame when contents change
      protected JRootPane createRootPane() {
        JRootPane rootPane =
            new JRootPane() {
              private boolean packing = false;

              public void validate() {
                super.validate();
                if (!packing) {
                  packing = true;
                  pack();
                  packing = false;
                }
              }
            };
        rootPane.setOpaque(true);
        return rootPane;
      }
    }

    JDialog dialog;
    Window window = SwingUtilities.getWindowAncestor(lizziePane);
    if (window instanceof Frame) {
      dialog = new LizziePaneDialog((Frame) window, lizziePane.getName(), false);
    } else if (window instanceof Dialog) {
      dialog = new LizziePaneDialog((Dialog) window, lizziePane.getName(), false);
    } else {
      dialog = new LizziePaneDialog((Frame) null, lizziePane.getName(), false);
    }

    dialog.getRootPane().setName("LizziePane.FloatingWindow");
    dialog.setTitle(lizziePane.getName());
    dialog.setResizable(true);
    dialog.setSize(lizziePane.getSize());
    WindowListener wl = createFrameListener();
    dialog.addWindowListener(wl);
    return dialog;
  }

  protected DragWindow createDragWindow(LizziePane lizziePane) {
    Window frame = null;
    if (lizziePane != null) {
      Container p;
      for (p = lizziePane.getParent(); p != null && !(p instanceof Window); p = p.getParent()) ;
      if (p != null && p instanceof Window) frame = (Window) p;
    }
    if (floatingLizziePane == null) {
      floatingLizziePane = createFloatingWindow(lizziePane);
    }
    if (floatingLizziePane instanceof Window) frame = (Window) floatingLizziePane;
    DragWindow dragWindow = new DragWindow(frame);
    return dragWindow;
  }

  public void setFloatingLocation(int x, int y) {
    floatingX = x;
    floatingY = y;
  }

  public boolean isFloating() {
    return floating;
  }

  public void setFloating(boolean b, Point p) {
    if (lizziePane.isFloatable()) {
      boolean visible = false;
      Window ancestor = SwingUtilities.getWindowAncestor(lizziePane);
      if (ancestor != null) {
        visible = ancestor.isVisible();
      }
      if (dragWindow != null) dragWindow.setVisible(false);
      this.floating = b;
      if (floatingLizziePane == null) {
        floatingLizziePane = createFloatingWindow(lizziePane);
      }
      if (b == true) {
        if (dockingSource == null) {
          dockingSource = lizziePane.getParent();
          dockingSource.remove(lizziePane);
        }
        constraintBeforeFloating = calculateConstraint();
        if (propertyListener != null) UIManager.addPropertyChangeListener(propertyListener);
        floatingLizziePane.getContentPane().add(lizziePane, BorderLayout.CENTER);
        if (floatingLizziePane instanceof Window) {
          ((Window) floatingLizziePane).pack();
          ((Window) floatingLizziePane).setLocation(floatingX, floatingY);
          Insets insets = ((Window) floatingLizziePane).getInsets();
          Dimension d =
              new Dimension(
                  originSize.width + insets.left + insets.right,
                  originSize.height + insets.top + insets.bottom);
          ((Window) floatingLizziePane).setMinimumSize(d);
          if (visible) {
            ((Window) floatingLizziePane).setVisible(true);
          } else {
            ancestor.addWindowListener(
                new WindowAdapter() {
                  public void windowOpened(WindowEvent e) {
                    ((Window) floatingLizziePane).setVisible(true);
                  }
                });
          }
        }
      } else {
        if (floatingLizziePane == null) floatingLizziePane = createFloatingWindow(lizziePane);
        if (floatingLizziePane instanceof Window) ((Window) floatingLizziePane).setVisible(false);
        floatingLizziePane.getContentPane().remove(lizziePane);
        String constraint = getDockingConstraint(dockingSource, p);
        // TODO
        if (constraint == null) {
          constraint = LizzieLayout.MAIN_BOARD;
        }
        if (dockingSource == null) dockingSource = lizziePane.getParent();
        if (propertyListener != null) UIManager.removePropertyChangeListener(propertyListener);
        dockingSource.add(constraint, lizziePane);
      }
      dockingSource.invalidate();
      Container dockingSourceParent = dockingSource.getParent();
      if (dockingSourceParent != null) dockingSourceParent.validate();
      dockingSource.repaint();
    }
  }

  /** Gets the color displayed when over a docking area */
  public Color getDockingColor() {
    return dockingColor;
  }

  /** Sets the color displayed when over a docking area */
  public void setDockingColor(Color c) {
    this.dockingColor = c;
  }

  /** Gets the color displayed when over a floating area */
  public Color getFloatingColor() {
    return floatingColor;
  }

  /** Sets the color displayed when over a floating area */
  public void setFloatingColor(Color c) {
    this.floatingColor = c;
  }

  private boolean isBlocked(Component comp, Object constraint) {
    if (comp instanceof Container) {
      Container cont = (Container) comp;
      LayoutManager lm = cont.getLayout();
      if (lm instanceof LizzieLayout) {
        LizzieLayout blm = (LizzieLayout) lm;
        Component c = blm.getLayoutComponent(cont, constraint);
        return (c != null && c != lizziePane);
      }
    }
    return false;
  }

  public boolean canDock(Component c, Point p) {
    return (p != null && getDockingConstraint(c, p) != null);
  }

  private String calculateConstraint() {
    String constraint = null;
    LayoutManager lm = dockingSource.getLayout();
    if (lm instanceof LizzieLayout) {
      constraint = (String) ((LizzieLayout) lm).getConstraints(lizziePane);
    }
    return (constraint != null) ? constraint : constraintBeforeFloating;
  }

  private String getDockingConstraint(Component c, Point p) {
    if (p == null) return constraintBeforeFloating;
    if (c.contains(p)) {
      dockingSensitivity = lizziePane.getSize().height;
      //      if (p.y < dockingSensitivity && !isBlocked(c, LizzieLayout.NORTH)) {
      //        return LizzieLayout.NORTH;
      //      }
      //      // East (Base distance on height for now!)
      //      if (p.x >= c.getWidth() - dockingSensitivity && !isBlocked(c, LizzieLayout.EAST)) {
      //        return LizzieLayout.EAST;
      //      }
      //      // West (Base distance on height for now!)
      //      if (p.x < dockingSensitivity && !isBlocked(c, LizzieLayout.WEST)) {
      //        return LizzieLayout.WEST;
      //      }
      //      if (p.y >= c.getHeight() - dockingSensitivity && !isBlocked(c, LizzieLayout.SOUTH)) {
      //        return LizzieLayout.SOUTH;
      //      }
    }
    return null;
  }

  protected void dragTo(Point position, Point origin) {
    originSize = lizziePane.getSize();
    if (lizziePane.isFloatable()) {
      try {
        if (dragWindow == null) dragWindow = createDragWindow(lizziePane);
        Point offset = dragWindow.getOffset();
        if (offset == null) {
          Dimension size = lizziePane.getSize(); // TODO .getPreferredSize();
          offset = new Point(size.width / 2, size.height / 2);
          dragWindow.setOffset(offset);
        }
        Point global = new Point(origin.x + position.x, origin.y + position.y);
        Point dragPoint = new Point(global.x - offset.x, global.y - offset.y);
        if (dockingSource == null) dockingSource = lizziePane.getParent();
        constraintBeforeFloating = calculateConstraint();
        Point dockingPosition = dockingSource.getLocationOnScreen();
        Point comparisonPoint =
            new Point(global.x - dockingPosition.x, global.y - dockingPosition.y);
        if (canDock(dockingSource, comparisonPoint)) {
          dragWindow.setBackground(getDockingColor());
          String constraint = getDockingConstraint(dockingSource, comparisonPoint);
          dragWindow.setBorderColor(dockingBorderColor);
        } else {
          dragWindow.setBackground(getFloatingColor());
          dragWindow.setBorderColor(floatingBorderColor);
        }

        dragWindow.setLocation(dragPoint.x, dragPoint.y);
        if (dragWindow.isVisible() == false) {
          Dimension size = lizziePane.getSize(); // TODO.getPreferredSize();
          dragWindow.setSize(size.width, size.height);
          dragWindow.setVisible(true);
        }
      } catch (IllegalComponentStateException e) {
      }
    }
  }

  protected void floatAt(Point position, Point origin) {
    if (lizziePane.isFloatable()) {
      try {
        Point offset = dragWindow.getOffset();
        if (offset == null) {
          offset = position;
          dragWindow.setOffset(offset);
        }
        Point global = new Point(origin.x + position.x, origin.y + position.y);
        setFloatingLocation(global.x - offset.x, global.y - offset.y);
        if (dockingSource != null) {
          Point dockingPosition = dockingSource.getLocationOnScreen();
          Point comparisonPoint =
              new Point(global.x - dockingPosition.x, global.y - dockingPosition.y);
          if (canDock(dockingSource, comparisonPoint)) {
            setFloating(false, comparisonPoint);
          } else {
            setFloating(true, null);
          }
        } else {
          setFloating(true, null);
        }
        dragWindow.setOffset(null);
      } catch (IllegalComponentStateException e) {
      }
    }
  }

  private Handler getHandler() {
    if (handler == null) {
      handler = new Handler();
    }
    return handler;
  }

  protected ContainerListener createLizziePaneContListener() {
    return getHandler();
  }

  protected FocusListener createLizziePaneFocusListener() {
    return getHandler();
  }

  protected PropertyChangeListener createPropertyListener() {
    return getHandler();
  }

  protected MouseInputListener createDockingListener() {
    getHandler().lp = lizziePane;
    return getHandler();
  }

  protected WindowListener createFrameListener() {
    return new FrameListener();
  }

  /**
   * Paints the contents of the window used for dragging.
   *
   * @param g Graphics to paint to.
   * @throws NullPointerException is <code>g</code> is null
   * @since 1.5
   */
  protected void paintDragWindow(Graphics g) {
    g.setColor(dragWindow.getBackground());
    int w = dragWindow.getWidth();
    int h = dragWindow.getHeight();
    g.fillRect(0, 0, w, h);
    g.setColor(dragWindow.getBorderColor());
    g.drawRect(0, 0, w - 1, h - 1);
  }
  //
  //  private static class Actions extends UIAction {
  //    private static final String NAVIGATE_RIGHT = "navigateRight";
  //    private static final String NAVIGATE_LEFT = "navigateLeft";
  //    private static final String NAVIGATE_UP = "navigateUp";
  //    private static final String NAVIGATE_DOWN = "navigateDown";
  //
  //    public Actions(String name) {
  //      super(name);
  //    }
  //
  //    public void actionPerformed(ActionEvent evt) {
  //      String key = getName();
  //      LizziePane lizziePane = (LizziePane) evt.getSource();
  //      LizziePaneUI ui = (LizziePaneUI) BasicLookAndFeel.getUIOfType(lizziePane.getUI(),
  // LizziePaneUI.class);
  //
  //      if (NAVIGATE_RIGHT == key) {
  //        ui.navigateFocusedComp(EAST);
  //      } else if (NAVIGATE_LEFT == key) {
  //        ui.navigateFocusedComp(WEST);
  //      } else if (NAVIGATE_UP == key) {
  //        ui.navigateFocusedComp(NORTH);
  //      } else if (NAVIGATE_DOWN == key) {
  //        ui.navigateFocusedComp(SOUTH);
  //      //      }
  //    }
  //  }

  private class Handler
      implements ContainerListener, FocusListener, MouseInputListener, PropertyChangeListener {

    //
    // ContainerListener
    //
    public void componentAdded(ContainerEvent evt) {
      Component c = evt.getChild();

      if (lizziePaneFocusListener != null) {
        c.addFocusListener(lizziePaneFocusListener);
      }
    }

    public void componentRemoved(ContainerEvent evt) {
      Component c = evt.getChild();

      if (lizziePaneFocusListener != null) {
        c.removeFocusListener(lizziePaneFocusListener);
      }
    }

    //
    // FocusListener
    //
    public void focusGained(FocusEvent evt) {
      Component c = evt.getComponent();
      focusedCompIndex = lizziePane.getComponentIndex(c);
    }

    public void focusLost(FocusEvent evt) {}

    //
    // MouseInputListener (DockingListener)
    //
    LizziePane lp;
    boolean isDragging = false;
    Point origin = null;

    public void mousePressed(MouseEvent evt) {
      if (!lp.isEnabled()) {
        return;
      }
      isDragging = false;
    }

    public void mouseReleased(MouseEvent evt) {
      if (!lp.isEnabled()) {
        return;
      }
      if (isDragging) {
        Point position = evt.getPoint();
        if (origin == null) origin = evt.getComponent().getLocationOnScreen();
        floatAt(position, origin);
      }
      origin = null;
      isDragging = false;
    }

    public void mouseDragged(MouseEvent evt) {
      if (!lp.isEnabled()) {
        return;
      }
      isDragging = true;
      Point position = evt.getPoint();
      if (origin == null) {
        origin = evt.getComponent().getLocationOnScreen();
      }
      dragTo(position, origin);
    }

    public void mouseClicked(MouseEvent evt) {}

    public void mouseEntered(MouseEvent evt) {}

    public void mouseExited(MouseEvent evt) {}

    public void mouseMoved(MouseEvent evt) {}

    //
    // PropertyChangeListener
    //
    public void propertyChange(PropertyChangeEvent evt) {
      String propertyName = evt.getPropertyName();
      if (propertyName == "lookAndFeel") {
        lizziePane.updateUI();
      }
    }
  }

  protected class FrameListener extends WindowAdapter {
    public void windowClosing(WindowEvent w) {
      if (lizziePane.isFloatable()) {
        if (dragWindow != null) dragWindow.setVisible(false);
        floating = false;
        if (floatingLizziePane == null) floatingLizziePane = createFloatingWindow(lizziePane);
        if (floatingLizziePane instanceof Window) ((Window) floatingLizziePane).setVisible(false);
        floatingLizziePane.getContentPane().remove(lizziePane);
        String constraint = constraintBeforeFloating;
        if (dockingSource == null) dockingSource = lizziePane.getParent();
        if (propertyListener != null) UIManager.removePropertyChangeListener(propertyListener);
        dockingSource.add(lizziePane, constraint);
        dockingSource.invalidate();
        Container dockingSourceParent = dockingSource.getParent();
        if (dockingSourceParent != null) dockingSourceParent.validate();
        dockingSource.repaint();
      }
    }
  }

  protected class LizziePaneContListener implements ContainerListener {
    // NOTE: This class exists only for backward compatibility. All
    // its functionality has been moved into Handler. If you need to add
    // new functionality add it to the Handler, but make sure this
    // class calls into the Handler.
    public void componentAdded(ContainerEvent e) {
      getHandler().componentAdded(e);
    }

    public void componentRemoved(ContainerEvent e) {
      getHandler().componentRemoved(e);
    }
  }

  protected class LizziePaneFocusListener implements FocusListener {
    // NOTE: This class exists only for backward compatibility. All
    // its functionality has been moved into Handler. If you need to add
    // new functionality add it to the Handler, but make sure this
    // class calls into the Handler.
    public void focusGained(FocusEvent e) {
      getHandler().focusGained(e);
    }

    public void focusLost(FocusEvent e) {
      getHandler().focusLost(e);
    }
  }

  protected class PropertyListener implements PropertyChangeListener {
    // NOTE: This class exists only for backward compatibility. All
    // its functionality has been moved into Handler. If you need to add
    // new functionality add it to the Handler, but make sure this
    // class calls into the Handler.
    public void propertyChange(PropertyChangeEvent e) {
      getHandler().propertyChange(e);
    }
  }

  /**
   * This class should be treated as a &quot;protected&quot; inner class. Instantiate it only within
   * subclasses of LizziePaneUI.
   */
  public class DockingListener implements MouseInputListener {
    // NOTE: This class exists only for backward compatibility. All
    // its functionality has been moved into Handler. If you need to add
    // new functionality add it to the Handler, but make sure this
    // class calls into the Handler.
    protected LizziePane lizziePane;
    protected boolean isDragging = false;
    protected Point origin = null;

    public DockingListener(LizziePane t) {
      this.lizziePane = t;
      getHandler().lp = t;
    }

    public void mouseClicked(MouseEvent e) {
      getHandler().mouseClicked(e);
    }

    public void mousePressed(MouseEvent e) {
      getHandler().lp = lizziePane;
      getHandler().mousePressed(e);
      isDragging = getHandler().isDragging;
    }

    public void mouseReleased(MouseEvent e) {
      getHandler().lp = lizziePane;
      getHandler().isDragging = isDragging;
      getHandler().origin = origin;
      getHandler().mouseReleased(e);
      isDragging = getHandler().isDragging;
      origin = getHandler().origin;
    }

    public void mouseEntered(MouseEvent e) {
      getHandler().mouseEntered(e);
    }

    public void mouseExited(MouseEvent e) {
      getHandler().mouseExited(e);
    }

    public void mouseDragged(MouseEvent e) {
      getHandler().lp = lizziePane;
      getHandler().origin = origin;
      getHandler().mouseDragged(e);
      isDragging = getHandler().isDragging;
      origin = getHandler().origin;
    }

    public void mouseMoved(MouseEvent e) {
      getHandler().mouseMoved(e);
    }
  }

  protected class DragWindow extends Window {
    Color borderColor = Color.gray;
    Point offset; // offset of the mouse cursor inside the DragWindow

    DragWindow(Window w) {
      super(w);
    }

    public Point getOffset() {
      return offset;
    }

    public void setOffset(Point p) {
      this.offset = p;
    }

    public void setBorderColor(Color c) {
      if (this.borderColor == c) return;
      this.borderColor = c;
      repaint();
    }

    public Color getBorderColor() {
      return this.borderColor;
    }

    public void paint(Graphics g) {
      paintDragWindow(g);
      // Paint the children
      super.paint(g);
    }

    public Insets getInsets() {
      return new Insets(1, 1, 1, 1);
    }
  }
}
