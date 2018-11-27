package featurecat.lizzie.gui;

import static java.lang.Math.max;
import static java.lang.Math.min;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.Board;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;
import org.json.JSONArray;

public class LizzieMain extends JFrame {

  public static Input input;
  public static BoardPane boardPane;
  public static CommentPane commentPane;
  public static boolean designMode;
  private static final String DEFAULT_TITLE = "Lizzie - Leela Zero Interface";

  /** Launch the application. */
  public static void main(String[] args) {
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            try {
              LizzieMain frame = new LizzieMain();
              frame.setVisible(true);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
  }

  /** Create the frame. */
  public LizzieMain() {
    super(DEFAULT_TITLE);

    input = new Input();

    addMouseListener(input);
    addKeyListener(input);
    addMouseWheelListener(input);
    addMouseMotionListener(input);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setMinimumSize(new Dimension(640, 400));
    JSONArray windowSize = Lizzie.config.uiConfig.getJSONArray("window-size");
    setSize(windowSize.getInt(0), windowSize.getInt(1));
    setLocationRelativeTo(null); // Start centered, needs to be called *after* setSize...

    // boardPane = new BoardPane(this);
    boardPane = new BoardPane(this);
    Lizzie.frame = boardPane;
    commentPane = new CommentPane(this);
    Lizzie.frame.setBounds(
        getX() + getInsets().left,
        getY() + getInsets().top,
        getWidth() - getInsets().left - getInsets().right,
        getHeight() - getInsets().top - getInsets().bottom);

    if (Lizzie.config.startMaximized) {
      setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    setVisible(true);

    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            updateComponentSize();
          }

          @Override
          public void componentMoved(ComponentEvent e) {
            updateComponentSize();
          }
        });
  }

  public void updateComponentSize() {

    int x = this.getX();
    int y = this.getY();
    int width = this.getWidth();
    int height = this.getHeight();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = screenSize.width;
    int screenHeight = screenSize.height;
    float proportionW = screenWidth / width;
    float proportionH = screenHeight / height;

    // layout parameters

    int topInset = this.getInsets().top;
    int leftInset = this.getInsets().left;
    int rightInset = this.getInsets().right;
    int bottomInset = this.getInsets().bottom;
    int maxBound = Math.max(width, height);

    // board
    int maxSize = (int) (min(width - leftInset - rightInset, height - topInset - bottomInset));
    maxSize = max(maxSize, Board.boardSize + 5); // don't let maxWidth become too small
    int boardX = (width - maxSize) / 2;
    int boardY = topInset + (height - topInset - bottomInset - maxSize) / 2;

    int panelMargin = (int) (maxSize * 0.02);

    // captured stones
    int capx = leftInset;
    int capy = topInset;
    int capw = boardX - panelMargin - leftInset;
    int caph = boardY + maxSize / 8 - topInset;

    // move statistics (winrate bar)
    // boardX equals width of space on each side
    int statx = capx;
    int staty = capy + caph;
    int statw = capw;
    int stath = maxSize / 10;

    // winrate graph
    int grx = statx;
    int gry = staty + stath;
    int grw = statw;
    int grh = maxSize / 3;

    // variation tree container
    int vx = boardX + maxSize + panelMargin;
    int vy = capy;
    int vw = width - vx - rightInset;
    int vh = height - vy - bottomInset;

    // pondering message
    double ponderingSize = .02;
    int ponderingX = leftInset;
    int ponderingY =
        height - bottomInset - (int) (maxSize * 0.033) - (int) (maxBound * ponderingSize);

    // dynamic komi
    double dynamicKomiSize = .02;
    int dynamicKomiX = leftInset;
    int dynamicKomiY = ponderingY - (int) (maxBound * dynamicKomiSize);
    int dynamicKomiLabelX = leftInset;
    int dynamicKomiLabelY = dynamicKomiY - (int) (maxBound * dynamicKomiSize);

    // loading message;
    double loadingSize = 0.03;
    int loadingX = ponderingX;
    int loadingY = ponderingY - (int) (maxBound * (loadingSize - ponderingSize));

    // subboard
    int subBoardY = gry + grh;
    int subBoardWidth = grw;
    int subBoardHeight = ponderingY - subBoardY;
    int subBoardLength = min(subBoardWidth, subBoardHeight);
    int subBoardX = statx + (statw - subBoardLength) / 2;

    if (width >= height) {
      // Landscape mode
      if (Lizzie.config.showLargeSubBoard()) {
        boardX = width - maxSize - panelMargin;
        int spaceW = boardX - panelMargin - leftInset;
        int spaceH = height - topInset - bottomInset;
        int panelW = spaceW / 2;
        int panelH = spaceH / 4;

        // captured stones
        capw = panelW;
        caph = (int) (panelH * 0.2);
        // move statistics (winrate bar)
        staty = capy + caph;
        statw = capw;
        stath = (int) (panelH * 0.4);
        // winrate graph
        gry = staty + stath;
        grw = statw;
        grh = panelH - caph - stath;
        // variation tree container
        vx = statx + statw;
        vw = panelW;
        vh = panelH;
        // subboard
        subBoardY = gry + grh;
        subBoardWidth = spaceW;
        subBoardHeight = ponderingY - subBoardY;
        subBoardLength = Math.min(subBoardWidth, subBoardHeight);
        subBoardX = statx + (statw + vw - subBoardLength) / 2;
      } else if (Lizzie.config.showLargeWinrate()) {
        boardX = width - maxSize - panelMargin;
        int spaceW = boardX - panelMargin - leftInset;
        int spaceH = height - topInset - bottomInset;
        int panelW = spaceW / 2;
        int panelH = spaceH / 4;

        // captured stones
        capy = topInset + panelH + 1;
        capw = spaceW;
        caph = (int) ((ponderingY - topInset - panelH) * 0.15);
        // move statistics (winrate bar)
        staty = capy + caph;
        statw = capw;
        stath = caph;
        // winrate graph
        gry = staty + stath;
        grw = statw;
        grh = ponderingY - gry;
        // variation tree container
        vx = leftInset + panelW;
        vw = panelW;
        vh = panelH;
        // subboard
        subBoardY = topInset;
        subBoardWidth = panelW - leftInset;
        subBoardHeight = panelH;
        subBoardLength = Math.min(subBoardWidth, subBoardHeight);
        subBoardX = statx + (vw - subBoardLength) / 2;
      }
    } else {
      // Portrait mode
      if (Lizzie.config.showLargeSubBoard()) {
        // board
        maxSize = (int) (maxSize * 0.8);
        boardY = height - maxSize - bottomInset;
        int spaceW = width - leftInset - rightInset;
        int spaceH = boardY - panelMargin - topInset;
        int panelW = spaceW / 2;
        int panelH = spaceH / 2;
        boardX = (spaceW - maxSize) / 2 + leftInset;

        // captured stones
        capw = panelW / 2;
        caph = panelH / 2;
        // move statistics (winrate bar)
        staty = capy + caph;
        statw = capw;
        stath = caph;
        // winrate graph
        gry = staty + stath;
        grw = statw;
        grh = spaceH - caph - stath;
        // variation tree container
        vx = capx + capw;
        vw = panelW / 2;
        vh = spaceH;
        // subboard
        subBoardX = vx + vw;
        subBoardWidth = panelW;
        subBoardHeight = boardY - topInset;
        subBoardLength = Math.min(subBoardWidth, subBoardHeight);
        subBoardY = capy + (gry + grh - capy - subBoardLength) / 2;
        // pondering message
        ponderingY = height;
      } else if (Lizzie.config.showLargeWinrate()) {
        // board
        maxSize = (int) (maxSize * 0.8);
        boardY = height - maxSize - bottomInset;
        int spaceW = width - leftInset - rightInset;
        int spaceH = boardY - panelMargin - topInset;
        int panelW = spaceW / 2;
        int panelH = spaceH / 2;
        boardX = (spaceW - maxSize) / 2 + leftInset;

        // captured stones
        capw = panelW / 2;
        caph = panelH / 4;
        // move statistics (winrate bar)
        statx = capx + capw;
        staty = capy;
        statw = capw;
        stath = caph;
        // winrate graph
        gry = staty + stath;
        grw = spaceW;
        grh = boardY - gry - 1;
        // variation tree container
        vx = statx + statw;
        vy = capy;
        vw = panelW / 2;
        vh = caph;
        // subboard
        subBoardY = topInset;
        subBoardWidth = panelW / 2;
        subBoardHeight = gry - topInset;
        subBoardLength = Math.min(subBoardWidth, subBoardHeight);
        subBoardX = vx + vw;
        // pondering message
        ponderingY = height;
      } else {
        // Normal
        // board
        boardY = (height - maxSize + topInset - bottomInset) / 2;
        int spaceW = width - leftInset - rightInset;
        int spaceH = boardY - panelMargin - topInset;
        int panelW = spaceW / 2;
        int panelH = spaceH / 2;

        // captured stones
        capw = panelW * 3 / 4;
        caph = panelH / 2;
        // move statistics (winrate bar)
        statx = capx + capw;
        staty = capy;
        statw = capw;
        stath = caph;
        // winrate graph
        grx = capx;
        gry = staty + stath;
        grw = capw + statw;
        grh = boardY - gry;
        // subboard
        subBoardX = grx + grw;
        subBoardWidth = panelW / 2;
        subBoardHeight = boardY - topInset;
        subBoardLength = Math.min(subBoardWidth, subBoardHeight);
        subBoardY = capy + (boardY - topInset - subBoardLength) / 2;
        // variation tree container
        vx = leftInset + panelW;
        vy = boardY + maxSize;
        vw = panelW;
        vh = height - vy - bottomInset;
      }
    }

    // graph container
    int contx = statx;
    int conty = staty;
    int contw = statw;
    int conth = stath + grh;
    if (width < height) {
      contw = grw;
      if (Lizzie.config.showLargeWinrate()) {
        contx = grx;
        conty = gry;
        conth = grh;
      } else {
        contx = capx;
        conty = capy;
        conth = stath + grh;
      }
    }

    // variation tree
    int treex = vx;
    int treey = vy;
    int treew = vw;
    int treeh = vh;

    // comment panel
    int cx = vx, cy = vy, cw = vw, ch = vh;
    if (Lizzie.config.showComment) {
      if (width >= height) {
        if (Lizzie.config.showVariationGraph) {
          treeh = vh / 2;
          cy = vy + treeh;
          ch = treeh;
        }
      } else {
        if (Lizzie.config.showVariationGraph) {
          if (Lizzie.config.showLargeSubBoard()) {
            treeh = vh / 2;
            cy = vy + treeh;
            ch = treeh;
          } else {
            treew = vw / 2;
            cx = vx + treew;
            cw = treew;
          }
        }
      }
    }

    boardPane.setBounds(x + boardX, y + boardY, maxSize, maxSize);
    commentPane.setBounds(x + cx, y + cy, cw, ch);
  }

  public void toggleDesignMode() {
    this.designMode = !this.designMode;
    Lizzie.frame.setDesignMode(designMode);
    commentPane.setDesignMode(designMode);
  }
}
