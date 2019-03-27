package featurecat.lizzie.gui;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import com.jhlabs.image.GaussianFilter;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.Board;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javax.swing.*;

/** The window used to display the game. */
public class SubBoardPane extends LizziePane {
  private static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");

  private static BoardRenderer subBoardRenderer;

  //  private final BufferStrategy bs;

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
  public SubBoardPane(LizzieMain owner) {
    super(owner);
    //    setModal(true);

    //    setModalityType(ModalityType.APPLICATION_MODAL);

    subBoardRenderer = new BoardRenderer(false);

    //    setUndecorated(true);
    //    getRootPane().setBorder(BorderFactory.createEmptyBorder());
    //    getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
//    setBackground(new Color(0, 0, 0, 0));
    setVisible(true);

    // TODO BufferStrategy does not support transparent background?
    //    createBufferStrategy(2);
    //    bs = getBufferStrategy();

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            if (Lizzie.config.showSubBoard) {
              Lizzie.config.toggleLargeSubBoard();
              owner.invalidLayout();
            }
          }
        });
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
  @Override
  protected void paintComponent(Graphics g0) {

    int x = 0; // getX();
    int y = 0; // getY();
    int width = getWidth();
    int height = getHeight();
    // layout parameters

    // initialize

    cachedImage = new BufferedImage(width, height, TYPE_INT_ARGB);
    Graphics2D g = (Graphics2D) cachedImage.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    if (Lizzie.leelaz != null && Lizzie.leelaz.isLoaded()) {

      if (Lizzie.config.showSubBoard) {
        try {
          subBoardRenderer.setLocation(x, y);
          subBoardRenderer.setBoardLength(width);
          subBoardRenderer.draw(g);
        } catch (Exception e) {
          // This can happen when no space is left for subboard.
        }
      }
    }

    // cleanup
    g.dispose();

    // draw the image
    // TODO BufferStrategy does not support transparent background?
    Graphics2D bsGraphics = (Graphics2D) g0; // bs.getDrawGraphics();
    bsGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    //    bsGraphics.drawImage(cachedBackground, 0, 0, null);
    bsGraphics.drawImage(cachedImage, 0, 0, null);

    // cleanup
    bsGraphics.dispose();
    // TODO BufferStrategy does not support transparent background?
    //    bs.show();
  }

  private GaussianFilter filter20 = new GaussianFilter(20);
  private GaussianFilter filter10 = new GaussianFilter(10);

  /**
   * Checks whether or not something was clicked and performs the appropriate action
   *
   * @param x x coordinate
   * @param y y coordinate
   */
  public void onClicked(int x, int y) {

    if (Lizzie.config.showSubBoard && subBoardRenderer.isInside(x, y)) {
      Lizzie.config.toggleLargeSubBoard();
    }
    repaint();
  }

  private final Consumer<String> placeVariation =
      v -> Board.asCoordinates(v).ifPresent(c -> Lizzie.board.place(c[0], c[1]));

  public boolean isMouseOver(int x, int y) {
    return mouseOverCoordinate[0] == x && mouseOverCoordinate[1] == y;
  }

  public boolean isInside(int x1, int y1) {
    return subBoardRenderer.isInside(x1, y1);
  }
}
