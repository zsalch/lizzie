package featurecat.lizzie.gui;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.GameInfo;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import org.json.JSONArray;

public class LizzieMain extends JFrame {
  public static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");

  public static Input input;
  public static BasicInfoPane basicInfoPane;
  public static BoardPane boardPane;
  public static SubBoardPane subBoardPane;
  public static WinratePane winratePane;
  public static VariationTreePane variationTreePane;
  public static CommentPane commentPane;
  public static boolean designMode;

  private int originX;
  private int originY;
  private int originW;
  private int originH;

  private LizzieLayout layout;

  private static final String DEFAULT_TITLE = "Lizzie - Leela Zero Interface";

  public static Font uiFont;
  public static Font winrateFont;

  private final BufferStrategy bs;

  private static final BufferedImage emptyImage = new BufferedImage(1, 1, TYPE_INT_ARGB);
  private BufferedImage cachedImage;

  private BufferedImage cachedBackground;

  private BufferedImage cachedWallpaperImage = emptyImage;
  private int cachedBackgroundWidth = 0, cachedBackgroundHeight = 0;
  private boolean cachedBackgroundShowControls = false;
  private boolean cachedShowWinrate = true;
  private boolean cachedShowVariationGraph = true;
  private boolean cachedShowLargeSubBoard = true;
  private boolean cachedLargeWinrate = true;
  private boolean cachedShowComment = true;
  private boolean redrawBackgroundAnyway = false;

  static {
    // load fonts
    try {
      uiFont =
          Font.createFont(
              Font.TRUETYPE_FONT,
              Thread.currentThread()
                  .getContextClassLoader()
                  .getResourceAsStream("fonts/OpenSans-Regular.ttf"));
      winrateFont =
          Font.createFont(
              Font.TRUETYPE_FONT,
              Thread.currentThread()
                  .getContextClassLoader()
                  .getResourceAsStream("fonts/OpenSans-Semibold.ttf"));
    } catch (IOException | FontFormatException e) {
      e.printStackTrace();
    }
  }

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
    //    addWindowStateListener(
    //        new WindowStateListener() {
    //          public void windowStateChanged(WindowEvent e) {
    //            updateComponentSize();
    //          }
    //        });

    input = new Input();

    addMouseListener(input);
    addKeyListener(input);
    addMouseWheelListener(input);
    //    addMouseMotionListener(input);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setMinimumSize(new Dimension(640, 400));
    JSONArray windowSize = Lizzie.config.uiConfig.getJSONArray("window-size");
    setSize(windowSize.getInt(0), windowSize.getInt(1));
    setLocationRelativeTo(null); // Start centered, needs to be called *after* setSize...

    // Allow change font in the config
    if (Lizzie.config.uiFontName != null) {
      uiFont = new Font(Lizzie.config.uiFontName, Font.PLAIN, 12);
    }
    if (Lizzie.config.winrateFontName != null) {
      winrateFont = new Font(Lizzie.config.winrateFontName, Font.BOLD, 12);
    }

    if (Lizzie.config.startMaximized) {
      setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    layout = new LizzieLayout();
    getContentPane().setLayout(layout);
    basicInfoPane = new BasicInfoPane(this);
    boardPane = new BoardPane(this);
    // TODO
    Lizzie.frame = boardPane;
    subBoardPane = new SubBoardPane(this);
    winratePane = new WinratePane(this);
    variationTreePane = new VariationTreePane(this);
    commentPane = new CommentPane(this);
    getContentPane().add(boardPane, LizzieLayout.MAIN_BOARD);
    getContentPane().add(basicInfoPane, LizzieLayout.BASIC_INFO);
    getContentPane().add(winratePane, LizzieLayout.WINRATE);
    getContentPane().add(subBoardPane, LizzieLayout.SUB_BOARD);
    getContentPane().add(variationTreePane, LizzieLayout.VARIATION);
    getContentPane().add(commentPane, LizzieLayout.COMMENT);

    setVisible(true);

    createBufferStrategy(2);
    bs = getBufferStrategy();

    addComponentListener(
        new ComponentAdapter() {
          //          @Override
          //          public void componentResized(ComponentEvent e) {
          //            updateComponentSize();
          //          }
          //
          @Override
          public void componentMoved(ComponentEvent e) {
            layout.invalidateLayout(getContentPane());
          }
        });

    //    repaint();
  }
  /**
   * Draws the game board and interface
   *
   * @param g0 not used
   */
  //  public void paint(Graphics g0) {
  //    super.paintComponents(g0);
  //    int width = getWidth();
  //    int height = getHeight();
  //
  //    originX = getX();
  //    originY = getY();
  //    originW = width;
  //    originH = height;
  //
  //    Optional<Graphics2D> backgroundG;
  //    if (cachedBackgroundWidth != width
  //        || cachedBackgroundHeight != height
  //        || redrawBackgroundAnyway) {
  //      backgroundG = Optional.of(createBackground());
  //    } else {
  //      backgroundG = Optional.empty();
  //    }
  //
  //    cachedImage = new BufferedImage(width, height, TYPE_INT_ARGB);
  //    Graphics2D g = (Graphics2D) cachedImage.getGraphics();
  //    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
  //
  //    // cleanup
  //    g.dispose();
  //
  //    // draw the image
  //    Graphics2D bsGraphics = (Graphics2D) bs.getDrawGraphics();
  //    bsGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
  // RenderingHints.VALUE_RENDER_QUALITY);
  //    bsGraphics.drawImage(cachedBackground, 0, 0, null);
  //    bsGraphics.drawImage(cachedImage, 0, 0, null);
  //
  //    // cleanup
  //    bsGraphics.dispose();
  //    bs.show();
  //  }

  /**
   * temporary measure to refresh background. ideally we shouldn't need this (but we want to release
   * Lizzie 0.5 today, not tomorrow!). Refactor me out please! (you need to get blurring to work
   * properly on startup).
   */
  public void refreshBackground() {
    redrawBackgroundAnyway = true;
  }

  public BufferedImage getWallpaper() {
    if (cachedWallpaperImage == emptyImage) {
      cachedWallpaperImage = Lizzie.config.theme.background();
    }
    return cachedWallpaperImage;
  }

  private Graphics2D createBackground() {
    cachedBackground = new BufferedImage(getWidth(), getHeight(), TYPE_INT_RGB);
    cachedBackgroundWidth = cachedBackground.getWidth();
    cachedBackgroundHeight = cachedBackground.getHeight();
    //    cachedBackgroundShowControls = showControls;
    cachedShowWinrate = Lizzie.config.showWinrate;
    cachedShowVariationGraph = Lizzie.config.showVariationGraph;
    cachedShowLargeSubBoard = Lizzie.config.showLargeSubBoard();
    cachedLargeWinrate = Lizzie.config.showLargeWinrate();
    cachedShowComment = Lizzie.config.showComment;

    redrawBackgroundAnyway = false;

    Graphics2D g = cachedBackground.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    BufferedImage wallpaper = getWallpaper();
    int drawWidth = max(wallpaper.getWidth(), getWidth());
    int drawHeight = max(wallpaper.getHeight(), getHeight());
    // Support seamless texture
    drawTextureImage(g, wallpaper, 0, 0, drawWidth, drawHeight);

    return g;
  }

  /** Draw texture image */
  public void drawTextureImage(
      Graphics2D g, BufferedImage img, int x, int y, int width, int height) {
    TexturePaint paint =
        new TexturePaint(img, new Rectangle(0, 0, img.getWidth(), img.getHeight()));
    g.setPaint(paint);
    g.fill(new Rectangle(x, y, width, height));
  }

  public void toggleDesignMode() {
    this.designMode = !this.designMode;
    //    boardPane.setDesignMode(designMode);
    commentPane.setDesignMode(designMode);
  }

  public void invalidLayout() {
    layout.invalidateLayout(getContentPane());
  }

  public void repaintSub() {
    subBoardPane.repaint();
    winratePane.repaint();
  }

  public void updateStatus() {
    basicInfoPane.repaint();
    variationTreePane.repaint();
    //    commentPane.repaint();
    commentPane.drawComment();
  }

  public static void openConfigDialog() {
    ConfigDialog configDialog = new ConfigDialog();
    configDialog.setVisible(true);
    //    configDialog.dispose();
  }

  public static void openChangeMoveDialog() {
    ChangeMoveDialog changeMoveDialog = new ChangeMoveDialog();
    changeMoveDialog.setVisible(true);
  }

  public static void openAvoidMoveDialog() {
    AvoidMoveDialog avoidMoveDialog = new AvoidMoveDialog();
    avoidMoveDialog.setVisible(true);
  }

  public void toggleGtpConsole() {
    Lizzie.leelaz.toggleGtpConsole();
    if (Lizzie.gtpConsole != null) {
      Lizzie.gtpConsole.setVisible(!Lizzie.gtpConsole.isVisible());
    } else {
      Lizzie.gtpConsole = new GtpConsolePane(this);
      Lizzie.gtpConsole.setVisible(true);
    }
  }

  public static void startNewGame() {
    GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();

    NewGameDialog newGameDialog = new NewGameDialog();
    newGameDialog.setGameInfo(gameInfo);
    newGameDialog.setVisible(true);
    boolean playerIsBlack = newGameDialog.playerIsBlack();
    boolean isNewGame = newGameDialog.isNewGame();
    //    newGameDialog.dispose();
    if (newGameDialog.isCancelled()) return;

    if (isNewGame) {
      Lizzie.board.clear();
    } else {
      //      Lizzie.board.saveMoveNumber();
      //      Lizzie.leelaz.clear();
      //      Lizzie.frame.resetTitle();
    }
    Lizzie.leelaz.sendCommand("komi " + gameInfo.getKomi());

    Lizzie.leelaz.time_settings();
    Lizzie.frame.playerIsBlack = playerIsBlack;
    Lizzie.frame.isNewGame = isNewGame;
    Lizzie.frame.isPlayingAgainstLeelaz = true;

    boolean isHandicapGame = gameInfo.getHandicap() != 0;
    if (isNewGame) {
      Lizzie.board.getHistory().setGameInfo(gameInfo);
      if (isHandicapGame) {
        Lizzie.board.getHistory().getData().blackToPlay = false;
        Lizzie.leelaz.sendCommand("fixed_handicap " + gameInfo.getHandicap());
        if (playerIsBlack) Lizzie.leelaz.genmove("W");
      } else if (!playerIsBlack) {
        Lizzie.leelaz.genmove("B");
      }
    } else {
      //      Lizzie.board.restoreMoveNumber();
      Lizzie.board.getHistory().setGameInfo(gameInfo);
      if (Lizzie.frame.playerIsBlack != Lizzie.board.getData().blackToPlay) {
        if (!Lizzie.leelaz.isThinking) {
          Lizzie.leelaz.genmove((Lizzie.board.getData().blackToPlay ? "B" : "W"));
        }
      }
    }
  }
}
