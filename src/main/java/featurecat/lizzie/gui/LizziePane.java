package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JWindow;

/** The window used to display the game. */
public class LizziePane extends JWindow {

  /** Keys to lookup borders in defaults table. */
  private static final int[] cursorMapping =
      new int[] {
        Cursor.NW_RESIZE_CURSOR,
        Cursor.NW_RESIZE_CURSOR,
        Cursor.N_RESIZE_CURSOR,
        Cursor.NE_RESIZE_CURSOR,
        Cursor.NE_RESIZE_CURSOR,
        Cursor.NW_RESIZE_CURSOR,
        0,
        0,
        0,
        Cursor.NE_RESIZE_CURSOR,
        Cursor.W_RESIZE_CURSOR,
        0,
        0,
        0,
        Cursor.E_RESIZE_CURSOR,
        Cursor.SW_RESIZE_CURSOR,
        0,
        0,
        0,
        Cursor.SE_RESIZE_CURSOR,
        Cursor.SW_RESIZE_CURSOR,
        Cursor.SW_RESIZE_CURSOR,
        Cursor.S_RESIZE_CURSOR,
        Cursor.SE_RESIZE_CURSOR,
        Cursor.SE_RESIZE_CURSOR
      };

  /** The amount of space (in pixels) that the cursor is changed on. */
  private static final int CORNER_DRAG_WIDTH = 16;

  /** Region from edges that dragging is active from. */
  private static final int BORDER_DRAG_THICKNESS = 5;

  /**
   * <code>Cursor</code> used to track the cursor set by the user. This is initially <code>
   * Cursor.DEFAULT_CURSOR</code>.
   */
  private Cursor lastCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  protected PaneDragListener dragListener;
  protected Input input;
  private BufferStrategy bs;

  public LizziePane() {
    super();
  }

  /** Creates a window */
  public LizziePane(LizzieMain owner) {
    super(owner);
    initCompotents();
    input = owner.input;
    installInputListeners();
  }

  private void initCompotents() {

    // setModal(true);

    // setModalityType(ModalityType.APPLICATION_MODAL);

    // setMinimumSize(new Dimension(640, 400));
    // JSONArray windowSize = Lizzie.config.uiConfig.getJSONArray("window-size");
    // setSize(windowSize.getInt(0), windowSize.getInt(1));
    // setLocationRelativeTo(owner);

    if (Lizzie.config.startMaximized) {
      // setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    // setUndecorated(true);
    // setResizable(true);
    getRootPane().setBorder(BorderFactory.createEmptyBorder());
    // getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
    setVisible(true);

    createBufferStrategy(2);
    bs = getBufferStrategy();

    // necessary for Windows users - otherwise Lizzie shows a blank white screen on
    // startup until
    // updates occur.
    // repaint();

    // When the window is closed: save the SGF file, then run shutdown()
    // this.addWindowListener(new WindowAdapter() {
    // public void windowClosing(WindowEvent e) {
    // Lizzie.shutdown();
    // }
    // });
  }

  public void updateComponentSize() {
    try {
      int width = this.getWidth();
      int height = this.getHeight();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int screenWidth = screenSize.width;
      int screenHeight = screenSize.height;
      float proportionW = screenWidth / width;
      float proportionH = screenHeight / height;
      Component[] components = this.getRootPane().getContentPane().getComponents();
      for (Component co : components) {
        float x = co.getX() * proportionW;
        float y = co.getY() * proportionH;
        float w = co.getWidth() * proportionW;
        float h = co.getHeight() * proportionH;
        co.setBounds((int) x, (int) y, (int) w, (int) h);
        // int size = (int) (co.getFont().getSize() * proportionH);
        // Font font = new Font(co.getFont().getFontName(), co.getFont().getStyle(),
        // size);
        // co.setFont(font);
      }
    } catch (Exception e) {
    }
  }

  private class PaneDragListener extends MouseAdapter {

    /** Set to true if the drag operation is moving the window. */
    private boolean isMovingWindow;

    /** Used to determine the corner the resize is occurring from. */
    private int dragCursor;

    /** X location the mouse went down on for a drag operation. */
    private int dragOffsetX;

    /** Y location the mouse went down on for a drag operation. */
    private int dragOffsetY;

    /** Width of the window when the drag started. */
    private int dragWidth;

    /** Height of the window when the drag started. */
    private int dragHeight;

    /** Window the <code>JRootPane</code> is in. */
    private Window window;

    public PaneDragListener(Window window) {
      this.window = window;
    }

    public void mouseMoved(MouseEvent e) {
      Window w = (Window) e.getSource();

      JWindow f = null;
      JDialog d = null;

      if (w instanceof JWindow) {
        f = (JWindow) w;
      } else if (w instanceof JDialog) {
        d = (JDialog) w;
      }

      // Update the cursor
      int cursor = getCursor(calculateCorner(w, e.getX(), e.getY()));

      if (cursor != 0
          && ((f != null) // && (f.isResizable() && (f.getExtendedState() & Frame.MAXIMIZED_BOTH)
              // == 0))
              || (d != null && d.isResizable()))) {
        w.setCursor(Cursor.getPredefinedCursor(cursor));
      } else {
        w.setCursor(lastCursor);
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (dragCursor != 0 && window != null && !window.isValid()) {
        // Some Window systems validate as you resize, others won't,
        // thus the check for validity before repainting.
        window.validate();
        getRootPane().repaint();
      }
      isMovingWindow = false;
      dragCursor = 0;
    }

    public void mousePressed(MouseEvent e) {
      Point dragWindowOffset = e.getPoint();
      Window w = (Window) e.getSource();
      if (w != null) {
        w.toFront();
      }

      JWindow f = null;
      JDialog d = null;

      if (w instanceof JWindow) {
        f = (JWindow) w;
      } else if (w instanceof JDialog) {
        d = (JDialog) w;
      }

      // int frameState = (f != null) ? f.getExtendedState() : 0;

      if (((f != null) // && ((frameState & Frame.MAXIMIZED_BOTH) == 0)
              || (d != null))
          && dragWindowOffset.y >= BORDER_DRAG_THICKNESS
          && dragWindowOffset.x >= BORDER_DRAG_THICKNESS
          && dragWindowOffset.x < w.getWidth() - BORDER_DRAG_THICKNESS) {
        isMovingWindow = true;
        dragOffsetX = dragWindowOffset.x;
        dragOffsetY = dragWindowOffset.y;
      } else if (f != null // && f.isResizable() && ((frameState & Frame.MAXIMIZED_BOTH) == 0)
          || (d != null && d.isResizable())) {
        dragOffsetX = dragWindowOffset.x;
        dragOffsetY = dragWindowOffset.y;
        dragWidth = w.getWidth();
        dragHeight = w.getHeight();
        dragCursor = getCursor(calculateCorner(w, dragWindowOffset.x, dragWindowOffset.y));
      }
    }

    public void mouseDragged(MouseEvent e) {
      Window w = (Window) e.getSource();
      Point pt = e.getPoint();

      if (isMovingWindow) {
        Point eventLocationOnScreen = e.getLocationOnScreen();
        w.setLocation(eventLocationOnScreen.x - dragOffsetX, eventLocationOnScreen.y - dragOffsetY);
      } else if (dragCursor != 0) {
        Rectangle r = w.getBounds();
        Rectangle startBounds = new Rectangle(r);
        Dimension min = w.getMinimumSize();

        switch (dragCursor) {
          case Cursor.E_RESIZE_CURSOR:
            adjust(r, min, 0, 0, pt.x + (dragWidth - dragOffsetX) - r.width, 0);
            break;
          case Cursor.S_RESIZE_CURSOR:
            adjust(r, min, 0, 0, 0, pt.y + (dragHeight - dragOffsetY) - r.height);
            break;
          case Cursor.N_RESIZE_CURSOR:
            adjust(r, min, 0, pt.y - dragOffsetY, 0, -(pt.y - dragOffsetY));
            break;
          case Cursor.W_RESIZE_CURSOR:
            adjust(r, min, pt.x - dragOffsetX, 0, -(pt.x - dragOffsetX), 0);
            break;
          case Cursor.NE_RESIZE_CURSOR:
            adjust(
                r,
                min,
                0,
                pt.y - dragOffsetY,
                pt.x + (dragWidth - dragOffsetX) - r.width,
                -(pt.y - dragOffsetY));
            break;
          case Cursor.SE_RESIZE_CURSOR:
            adjust(
                r,
                min,
                0,
                0,
                pt.x + (dragWidth - dragOffsetX) - r.width,
                pt.y + (dragHeight - dragOffsetY) - r.height);
            break;
          case Cursor.NW_RESIZE_CURSOR:
            adjust(
                r,
                min,
                pt.x - dragOffsetX,
                pt.y - dragOffsetY,
                -(pt.x - dragOffsetX),
                -(pt.y - dragOffsetY));
            break;
          case Cursor.SW_RESIZE_CURSOR:
            adjust(
                r,
                min,
                pt.x - dragOffsetX,
                0,
                -(pt.x - dragOffsetX),
                pt.y + (dragHeight - dragOffsetY) - r.height);
            break;
          default:
            break;
        }
        if (!r.equals(startBounds)) {
          w.setBounds(r);
          // Defer repaint/validate on mouseReleased unless dynamic
          // layout is active.
          if (Toolkit.getDefaultToolkit().isDynamicLayoutActive()) {
            w.validate();
            getRootPane().repaint();
          }
        }
      }
    }

    private int calculateCorner(Window c, int x, int y) {
      Insets insets = c.getInsets();
      int xPosition = calculatePosition(x - insets.left, c.getWidth() - insets.left - insets.right);
      int yPosition = calculatePosition(y - insets.top, c.getHeight() - insets.top - insets.bottom);

      if (xPosition == -1 || yPosition == -1) {
        return -1;
      }
      return yPosition * 5 + xPosition;
    }

    private int getCursor(int corner) {
      if (corner == -1) {
        return 0;
      }
      return cursorMapping[corner];
    }

    private int calculatePosition(int spot, int width) {
      if (spot < BORDER_DRAG_THICKNESS) {
        return 0;
      }
      if (spot < CORNER_DRAG_WIDTH) {
        return 1;
      }
      if (spot >= (width - BORDER_DRAG_THICKNESS)) {
        return 4;
      }
      if (spot >= (width - CORNER_DRAG_WIDTH)) {
        return 3;
      }
      return 2;
    }

    private void adjust(
        Rectangle bounds, Dimension min, int deltaX, int deltaY, int deltaWidth, int deltaHeight) {
      bounds.x += deltaX;
      bounds.y += deltaY;
      bounds.width += deltaWidth;
      bounds.height += deltaHeight;
      if (min != null) {
        if (bounds.width < min.width) {
          int correction = min.width - bounds.width;
          if (deltaX != 0) {
            bounds.x -= correction;
          }
          bounds.width = min.width;
        }
        if (bounds.height < min.height) {
          int correction = min.height - bounds.height;
          if (deltaY != 0) {
            bounds.y -= correction;
          }
          bounds.height = min.height;
        }
      }
    }
  }

  protected void installDesignListeners() {
    if (dragListener == null) {
      dragListener = new PaneDragListener(this);
    }
    addMouseListener(dragListener);
    addMouseMotionListener(dragListener);
  }

  protected void uninstallDesignListeners() {
    removeMouseListener(dragListener);
    removeMouseMotionListener(dragListener);
  }

  protected void installInputListeners() {
    addMouseListener(input);
    addKeyListener(input);
    addMouseWheelListener(input);
    addMouseMotionListener(input);
  }

  protected void uninstallInputListeners() {
    removeMouseListener(input);
    removeKeyListener(input);
    removeMouseWheelListener(input);
    removeMouseMotionListener(input);
  }

  public void setDesignMode(boolean mode) {
    if (mode) {
      uninstallInputListeners();
      installDesignListeners();
    } else {
      uninstallDesignListeners();
      installInputListeners();
    }
  }
}
