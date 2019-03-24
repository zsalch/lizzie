package featurecat.lizzie.gui;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.min;

import com.jhlabs.image.GaussianFilter;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.rules.Board;
import featurecat.lizzie.rules.BoardData;
import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.*;

/** The window used to display the game. */
public class WinratePane extends JDialog {
  private static WinrateGraph winrateGraph;

  private final BufferStrategy bs;

  private static final int[] outOfBoundCoordinate = new int[] {-1, -1};
  public int[] mouseOverCoordinate = outOfBoundCoordinate;
  public boolean showControls = false;
  public boolean isPlayingAgainstLeelaz = false;
  public boolean playerIsBlack = true;
  public int winRateGridLines = 3;
  public int BoardPositionProportion = 4;

  private long lastAutosaveTime = System.currentTimeMillis();
  private boolean isReplayVariation = false;

  /** Creates a window */
  public WinratePane(JFrame owner) {
    super(owner);
    //    setModal(true);

    //    setModalityType(ModalityType.APPLICATION_MODAL);

    winrateGraph = new WinrateGraph();

    setUndecorated(true);
    //    getRootPane().setBorder(BorderFactory.createEmptyBorder());
    //    getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
    setVisible(true);

    createBufferStrategy(2);
    bs = getBufferStrategy();

    Input input = new Input();

    addMouseListener(input);
    addKeyListener(input);
    addMouseWheelListener(input);
    addMouseMotionListener(input);

    // necessary for Windows users - otherwise Lizzie shows a blank white screen on startup until
    // updates occur.
    //    repaint();

  }

  /** Clears related status from empty board. */
  public void clear() {
    if (winrateGraph != null) {
      winrateGraph.clear();
    }
  }

  private BufferedImage cachedImage;

  private BufferedImage cachedBackground;
  private int cachedBackgroundWidth = 0, cachedBackgroundHeight = 0;
  private boolean cachedBackgroundShowControls = false;
  private boolean cachedShowWinrate = true;
  private boolean cachedShowVariationGraph = true;
  private boolean cachedShowLargeSubBoard = true;
  private boolean cachedLargeWinrate = true;
  private boolean cachedShowComment = true;
  private boolean redrawBackgroundAnyway = false;
  private int cachedBoardPositionProportion = BoardPositionProportion;

  /**
   * Draws the game board and interface
   *
   * @param g0 not used
   */
  public void paint(Graphics g0) {

    int x = 0; // getX();
    int y = 0; // getY();
    int width = getWidth();
    int height = getHeight();

    // initialize

    cachedImage = new BufferedImage(width, height, TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D) cachedImage.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    if (Lizzie.leelaz != null && Lizzie.leelaz.isLoaded()) {
      if (Lizzie.config.showWinrate) {
        //                  if (backgroundG.isPresent()) {
        //                    drawContainer(backgroundG.get(), contx, conty, contw, conth);
        //                  }
        int hh = height * 3 / 13;
        drawMoveStatistics(g, x, y, width, hh);
        winrateGraph.draw(g, x, y + hh, width, height - hh);
      }
    }

    // cleanup
    g.dispose();

    // draw the image
    Graphics2D bsGraphics = (Graphics2D) bs.getDrawGraphics();
    bsGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    bsGraphics.drawImage(cachedBackground, 0, 0, null);
    bsGraphics.drawImage(cachedImage, 0, 0, null);

    // cleanup
    bsGraphics.dispose();
    bs.show();
  }

  /**
   * temporary measure to refresh background. ideally we shouldn't need this (but we want to release
   * Lizzie 0.5 today, not tomorrow!). Refactor me out please! (you need to get blurring to work
   * properly on startup).
   */
  public void refreshBackground() {
    redrawBackgroundAnyway = true;
  }

  private void drawContainer(Graphics g, int vx, int vy, int vw, int vh) {
    if (vw <= 0
        || vh <= 0
        || vx < cachedBackground.getMinX()
        || vx + vw > cachedBackground.getMinX() + cachedBackground.getWidth()
        || vy < cachedBackground.getMinY()
        || vy + vh > cachedBackground.getMinY() + cachedBackground.getHeight()) {
      return;
    }

    BufferedImage result = new BufferedImage(vw, vh, TYPE_INT_ARGB);
    filter20.filter(cachedBackground.getSubimage(vx, vy, vw, vh), result);
    g.drawImage(result, vx, vy, null);
  }

  private GaussianFilter filter20 = new GaussianFilter(20);
  private GaussianFilter filter10 = new GaussianFilter(10);

  private void drawMoveStatistics(Graphics2D g, int posX, int posY, int width, int height) {
    if (width < 0 || height < 0) return; // we don't have enough space

    double lastWR = 50; // winrate the previous move
    boolean validLastWinrate = false; // whether it was actually calculated
    Optional<BoardData> previous = Lizzie.board.getHistory().getPrevious();
    if (previous.isPresent() && previous.get().playouts > 0) {
      lastWR = previous.get().winrate;
      validLastWinrate = true;
    }

    Leelaz.WinrateStats stats = Lizzie.leelaz.getWinrateStats();
    double curWR = stats.maxWinrate; // winrate on this move
    boolean validWinrate = (stats.totalPlayouts > 0); // and whether it was actually calculated
    if (isPlayingAgainstLeelaz
        && playerIsBlack == !Lizzie.board.getHistory().getData().blackToPlay) {
      validWinrate = false;
    }

    if (!validWinrate) {
      curWR = 100 - lastWR; // display last move's winrate for now (with color difference)
    }
    double whiteWR, blackWR;
    if (Lizzie.board.getData().blackToPlay) {
      blackWR = curWR;
    } else {
      blackWR = 100 - curWR;
    }

    whiteWR = 100 - blackWR;

    // Background rectangle
    g.setColor(new Color(0, 0, 0, 130));
    g.fillRect(posX, posY, width, height);

    // border. does not include bottom edge
    int strokeRadius = Lizzie.config.showBorder ? 3 : 1;
    g.setStroke(new BasicStroke(strokeRadius == 1 ? strokeRadius : 2 * strokeRadius));
    g.drawLine(
        posX + strokeRadius, posY + strokeRadius,
        posX - strokeRadius + width, posY + strokeRadius);
    if (Lizzie.config.showBorder) {
      g.drawLine(
          posX + strokeRadius, posY + 3 * strokeRadius,
          posX + strokeRadius, posY - strokeRadius + height);
      g.drawLine(
          posX - strokeRadius + width, posY + 3 * strokeRadius,
          posX - strokeRadius + width, posY - strokeRadius + height);
    }

    // resize the box now so it's inside the border
    posX += 2 * strokeRadius;
    posY += 2 * strokeRadius;
    width -= 4 * strokeRadius;
    height -= 4 * strokeRadius;

    // Title
    strokeRadius = 2;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(Color.WHITE);
    setPanelFont(g, (int) (min(width, height) * 0.2));

    // Last move
    if (validLastWinrate && validWinrate) {
      String text;
      if (Lizzie.config.handicapInsteadOfWinrate) {
        double currHandicapedWR = Lizzie.leelaz.winrateToHandicap(100 - curWR);
        double lastHandicapedWR = Lizzie.leelaz.winrateToHandicap(lastWR);
        text = String.format(": %.2f", currHandicapedWR - lastHandicapedWR);
      } else {
        text = String.format(": %.1f%%", 100 - lastWR - curWR);
      }

      g.drawString(
          LizzieMain.resourceBundle.getString("LizzieFrame.display.lastMove") + text,
          posX + 2 * strokeRadius,
          posY + height - 2 * strokeRadius); // - font.getSize());
    } else {
      // I think it's more elegant to just not display anything when we don't have
      // valid data --dfannius
      // g.drawString(resourceBundle.getString("LizzieFrame.display.lastMove") + ": ?%",
      //              posX + 2 * strokeRadius, posY + height - 2 * strokeRadius);
    }

    if (validWinrate || validLastWinrate) {
      int maxBarwidth = (int) (width);
      int barWidthB = (int) (blackWR * maxBarwidth / 100);
      int barWidthW = (int) (whiteWR * maxBarwidth / 100);
      int barPosY = posY + height / 3;
      int barPosxB = (int) (posX);
      int barPosxW = barPosxB + barWidthB;
      int barHeight = height / 3;

      // Draw winrate bars
      g.fillRect(barPosxW, barPosY, barWidthW, barHeight);
      g.setColor(Color.BLACK);
      g.fillRect(barPosxB, barPosY, barWidthB, barHeight);

      // Show percentage above bars
      g.setColor(Color.WHITE);
      g.drawString(
          String.format("%.1f%%", blackWR),
          barPosxB + 2 * strokeRadius,
          posY + barHeight - 2 * strokeRadius);
      String winString = String.format("%.1f%%", whiteWR);
      int sw = g.getFontMetrics().stringWidth(winString);
      g.drawString(
          winString,
          barPosxB + maxBarwidth - sw - 2 * strokeRadius,
          posY + barHeight - 2 * strokeRadius);

      g.setColor(Color.GRAY);
      Stroke oldstroke = g.getStroke();
      Stroke dashed =
          new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {4}, 0);
      g.setStroke(dashed);

      for (int i = 1; i <= winRateGridLines; i++) {
        int x = barPosxB + (int) (i * (maxBarwidth / (winRateGridLines + 1)));
        g.drawLine(x, barPosY, x, barPosY + barHeight);
      }
      g.setStroke(oldstroke);
    }
  }

  private void setPanelFont(Graphics2D g, float size) {
    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, (int) size);
    g.setFont(font);
  }

  /**
   * Checks whether or not something was clicked and performs the appropriate action
   *
   * @param x x coordinate
   * @param y y coordinate
   */
  public void onClicked(int x, int y) {
    int moveNumber = winrateGraph.moveNumber(x, y);

    if (Lizzie.config.showWinrate && moveNumber >= 0) {
      isPlayingAgainstLeelaz = false;
      Lizzie.board.goToMoveNumberBeyondBranch(moveNumber);
      repaint();
    }
  }

  private final Consumer<String> placeVariation =
      v -> Board.asCoordinates(v).ifPresent(c -> Lizzie.board.place(c[0], c[1]));

  public void onMouseMoved(int x, int y) {}

  public boolean isMouseOver(int x, int y) {
    return mouseOverCoordinate[0] == x && mouseOverCoordinate[1] == y;
  }

  public void onMouseDragged(int x, int y) {
    int moveNumber = winrateGraph.moveNumber(x, y);
    if (Lizzie.config.showWinrate && moveNumber >= 0) {
      if (Lizzie.board.goToMoveNumberWithinBranch(moveNumber)) {
        repaint();
      }
    }
  }

  public int moveNumber(int x, int y) {
    return winrateGraph.moveNumber(x, y);
  }
}
