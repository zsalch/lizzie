package featurecat.lizzie.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

public class LizzieLayout implements LayoutManager2, java.io.Serializable {
  private int hgap;
  private int vgap;

  Component mainBoard;
  Component subBoard;
  Component winratePane;
  Component variationPane;
  Component basicInfoPane;
  Component commentPane;
  Component consolePane;

  public static final String MAIN_BOARD = "mainBoard";
  public static final String SUB_BOARD = "subBoard";
  public static final String WINRATE = "winratePane";
  public static final String VARIATION = "variationPane";
  public static final String BASIC_INFO = "basicInfoPane";
  public static final String COMMENT = "commentPane";
  public static final String CONSOLE = "consolePane";
  public static final String PAGE_START = MAIN_BOARD;
  public static final String PAGE_END = WINRATE;
  //    public static final String LINE_START = BEFORE_LINE_BEGINS;
  //    public static final String LINE_END = AFTER_LINE_ENDS;

  public LizzieLayout() {
    this(3, 0);
  }

  public LizzieLayout(int hgap, int vgap) {
    this.hgap = hgap;
    this.vgap = vgap;
  }

  public int getHgap() {
    return hgap;
  }

  public void setHgap(int hgap) {
    this.hgap = hgap;
  }

  public int getVgap() {
    return vgap;
  }

  public void setVgap(int vgap) {
    this.vgap = vgap;
  }

  public void addLayoutComponent(Component comp, Object constraints) {
    synchronized (comp.getTreeLock()) {
      if ((constraints == null) || (constraints instanceof String)) {
        addLayoutComponent((String) constraints, comp);
      } else {
        throw new IllegalArgumentException(
            "cannot add to layout: constraint must be a string (or null)");
      }
    }
  }

  public void addLayoutComponent(String name, Component comp) {
    synchronized (comp.getTreeLock()) {
      if (name == null) {
        name = MAIN_BOARD;
      }

      if (BASIC_INFO.equals(name)) {
        basicInfoPane = comp;
      } else if (MAIN_BOARD.equals(name)) {
        mainBoard = comp;
      } else if (VARIATION.equals(name)) {
        variationPane = comp;
      } else if (WINRATE.equals(name)) {
        winratePane = comp;
      } else if (SUB_BOARD.equals(name)) {
        subBoard = comp;
      } else if (COMMENT.equals(name)) {
        commentPane = comp;
      } else if (CONSOLE.equals(name)) {
        consolePane = comp;
      }
      //        else if (BEFORE_LINE_BEGINS.equals(name)) {
      //            firstItem = comp;
      //        } else if (AFTER_LINE_ENDS.equals(name)) {
      //            lastItem = comp;
      //        }
      else {
        throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
      }
    }
  }

  public void removeLayoutComponent(Component comp) {
    synchronized (comp.getTreeLock()) {
      if (comp == basicInfoPane) {
        basicInfoPane = null;
      } else if (comp == mainBoard) {
        mainBoard = null;
      } else if (comp == variationPane) {
        variationPane = null;
      } else if (comp == winratePane) {
        winratePane = null;
      } else if (comp == subBoard) {
        subBoard = null;
      }
      if (comp == commentPane) {
        commentPane = null;
      } else if (comp == consolePane) {
        consolePane = null;
      }
      //        else if (comp == firstItem) {
      //            firstItem = null;
      //        } else if (comp == lastItem) {
      //            lastItem = null;
      //        }
    }
  }

  public Component getLayoutComponent(Object constraints) {
    if (BASIC_INFO.equals(constraints)) {
      return basicInfoPane;
    } else if (MAIN_BOARD.equals(constraints)) {
      return mainBoard;
    } else if (SUB_BOARD.equals(constraints)) {
      return subBoard;
    } else if (VARIATION.equals(constraints)) {
      return variationPane;
    } else if (WINRATE.equals(constraints)) {
      return winratePane;
    } else if (COMMENT.equals(constraints)) {
      return commentPane;
    } else if (CONSOLE.equals(constraints)) {
      return consolePane;
      //        }
      //        else if (LINE_START.equals(constraints)) {
      //            return firstItem;
      //        } else if (LINE_END.equals(constraints)) {
      //            return lastItem;
    } else {
      throw new IllegalArgumentException(
          "cannot get component: unknown constraint: " + constraints);
    }
  }

  public Component getLayoutComponent(Container target, Object constraints) {
    boolean ltr = target.getComponentOrientation().isLeftToRight();
    Component result = null;

    if (MAIN_BOARD.equals(constraints)) {
      result = mainBoard; // (commentPane != null) ? commentPane : mainBoard;
    } else if (SUB_BOARD.equals(constraints)) {
      result = subBoard; // (consolePane != null) ? consolePane : variationPane;
    } else if (VARIATION.equals(constraints)) {
      //            result = ltr ? firstItem : lastItem;
      //            if (result == null) {
      result = variationPane;
      //            }
    } else if (WINRATE.equals(constraints)) {
      //            result = ltr ? lastItem : firstItem;
      //            if (result == null) {
      result = winratePane;
      //            }
    } else if (BASIC_INFO.equals(constraints)) {
      result = basicInfoPane;
    } else {
      throw new IllegalArgumentException(
          "cannot get component: invalid constraint: " + constraints);
    }

    return result;
  }

  public Object getConstraints(Component comp) {
    if (comp == null) {
      return null;
    }
    if (comp == basicInfoPane) {
      return BASIC_INFO;
    } else if (comp == mainBoard) {
      return MAIN_BOARD;
    } else if (comp == subBoard) {
      return SUB_BOARD;
    } else if (comp == variationPane) {
      return VARIATION;
    } else if (comp == winratePane) {
      return WINRATE;
    } else if (comp == commentPane) {
      return COMMENT;
    } else if (comp == consolePane) {
      return CONSOLE;
      //        } else if (comp == firstItem) {
      //            return LINE_START;
      //        } else if (comp == lastItem) {
      //            return LINE_END;
    }
    return null;
  }

  public Dimension minimumLayoutSize(Container target) {
    synchronized (target.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);

      boolean ltr = target.getComponentOrientation().isLeftToRight();
      Component c = null;

      if ((c = getChild(WINRATE, ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = getChild(VARIATION, ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = getChild(BASIC_INFO, ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = getChild(MAIN_BOARD, ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
      }
      if ((c = getChild(SUB_BOARD, ltr)) != null) {
        Dimension d = c.getMinimumSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
      }

      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

  public Dimension preferredLayoutSize(Container target) {
    synchronized (target.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);

      boolean ltr = target.getComponentOrientation().isLeftToRight();
      Component c = null;

      if ((c = getChild(WINRATE, ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = getChild(VARIATION, ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width += d.width + hgap;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = getChild(BASIC_INFO, ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width += d.width;
        dim.height = Math.max(d.height, dim.height);
      }
      if ((c = getChild(MAIN_BOARD, ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
      }
      if ((c = getChild(SUB_BOARD, ltr)) != null) {
        Dimension d = c.getPreferredSize();
        dim.width = Math.max(d.width, dim.width);
        dim.height += d.height + vgap;
      }

      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public float getLayoutAlignmentX(Container parent) {
    return 0.5f;
  }

  public float getLayoutAlignmentY(Container parent) {
    return 0.5f;
  }

  public void invalidateLayout(Container target) {}

  public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      //        int top = insets.top;
      //        int bottom = target.height - insets.bottom;
      //        int left = insets.left;
      //        int right = target.width - insets.right;
      //
      //        boolean ltr = target.getComponentOrientation().isLeftToRight();
      //        Component c = null;
      //
      //        if ((c=getChild(MAIN_BOARD,ltr)) != null) {
      //            c.setSize(right - left, c.height);
      //            Dimension d = c.getPreferredSize();
      //            c.setBounds(left, top, right - left, d.height);
      //            top += d.height + vgap;
      //        }
      //        if ((c=getChild(SUB_BOARD,ltr)) != null) {
      //            c.setSize(right - left, c.height);
      //            Dimension d = c.getPreferredSize();
      //            c.setBounds(left, bottom - d.height, right - left, d.height);
      //            bottom -= d.height + vgap;
      //        }
      //        if ((c=getChild(WINRATE,ltr)) != null) {
      //            c.setSize(c.width, bottom - top);
      //            Dimension d = c.getPreferredSize();
      //            c.setBounds(right - d.width, top, d.width, bottom - top);
      //            right -= d.width + hgap;
      //        }
      //        if ((c=getChild(VARIATION,ltr)) != null) {
      //            c.setSize(c.width, bottom - top);
      //            Dimension d = c.getPreferredSize();
      //            c.setBounds(left, top, d.width, bottom - top);
      //            left += d.width + hgap;
      //        }
      //        if ((c=getChild(BASIC_INFO,ltr)) != null) {
      //            c.setBounds(left, top, right - left, bottom - top);
      //        }
    }
  }

  private Component getChild(String key, boolean ltr) {
    Component result = null;

    if (key == MAIN_BOARD) {
      result = (commentPane != null) ? commentPane : mainBoard;
    } else if (key == SUB_BOARD) {
      result = (consolePane != null) ? consolePane : variationPane;
    }
    //        else if (key == VARIATION) {
    //            result = ltr ? firstItem : lastItem;
    //            if (result == null) {
    //                result = subBoard;
    //            }
    //        }
    //        else if (key == WINRATE) {
    //            result = ltr ? lastItem : firstItem;
    //            if (result == null) {
    //                result = winratePane;
    //            }
    //        }
    else if (key == BASIC_INFO) {
      result = basicInfoPane;
    }
    //        if (result != null && !result.visible) {
    //            result = null;
    //        }
    return result;
  }

  public String toString() {
    return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + "]";
  }
}
