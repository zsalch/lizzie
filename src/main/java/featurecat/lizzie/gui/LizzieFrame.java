package featurecat.lizzie.gui;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;

import com.jhlabs.image.GaussianFilter;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.GameInfo;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.rules.Board;
import featurecat.lizzie.rules.BoardData;
import featurecat.lizzie.rules.GIBParser;
import featurecat.lizzie.rules.SGFParser;
import java.awt.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONArray;
import org.json.JSONObject;

/** The window used to display the game. */
public class LizzieFrame extends JFrame {
  private static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");

  private static final String[] commands = {
    resourceBundle.getString("LizzieFrame.commands.keyN"),
    resourceBundle.getString("LizzieFrame.commands.keyEnter"),
    resourceBundle.getString("LizzieFrame.commands.keySpace"),
    resourceBundle.getString("LizzieFrame.commands.keyUpArrow"),
    resourceBundle.getString("LizzieFrame.commands.keyDownArrow"),
    resourceBundle.getString("LizzieFrame.commands.rightClick"),
    resourceBundle.getString("LizzieFrame.commands.mouseWheelScroll"),
    resourceBundle.getString("LizzieFrame.commands.keyC"),
    resourceBundle.getString("LizzieFrame.commands.keyP"),
    resourceBundle.getString("LizzieFrame.commands.keyPeriod"),
    resourceBundle.getString("LizzieFrame.commands.keyA"),
    resourceBundle.getString("LizzieFrame.commands.keyM"),
    resourceBundle.getString("LizzieFrame.commands.keyI"),
    resourceBundle.getString("LizzieFrame.commands.keyO"),
    resourceBundle.getString("LizzieFrame.commands.keyS"),
    resourceBundle.getString("LizzieFrame.commands.keyAltC"),
    resourceBundle.getString("LizzieFrame.commands.keyAltV"),
    resourceBundle.getString("LizzieFrame.commands.keyF"),
    resourceBundle.getString("LizzieFrame.commands.keyV"),
    resourceBundle.getString("LizzieFrame.commands.keyW"),
    resourceBundle.getString("LizzieFrame.commands.keyG"),
    resourceBundle.getString("LizzieFrame.commands.keyT"),
    resourceBundle.getString("LizzieFrame.commands.keyHome"),
    resourceBundle.getString("LizzieFrame.commands.keyEnd"),
    resourceBundle.getString("LizzieFrame.commands.keyControl"),
  };
  private static final String DEFAULT_TITLE = "Lizzie - Leela Zero Interface";
  private static BoardRenderer boardRenderer;
  private static BoardRenderer subBoardRenderer;
  private static VariationTree variationTree;
  private static WinrateGraph winrateGraph;

  public static Font uiFont;
  public static Font winrateFont;

  private final BufferStrategy bs;

  private static final int[] outOfBoundCoordinate = new int[] {-1, -1};
  public int[] mouseOverCoordinate = outOfBoundCoordinate;
  public boolean showControls = false;
  public boolean showCoordinates = false;
  public boolean isPlayingAgainstLeelaz = false;
  public boolean playerIsBlack = true;
  public int winRateGridLines = 3;

  private long lastAutosaveTime = System.currentTimeMillis();

  // Save the player title
  private String playerTitle = "";

  // Display Comment
  private JScrollPane scrollPane;
  private JTextPane commentPane;
  private BufferedImage cachedCommentImage = new BufferedImage(1, 1, TYPE_INT_ARGB);
  private String cachedComment;
  private Rectangle commentRect;

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

  /** Creates a window */
  public LizzieFrame() {
    super(DEFAULT_TITLE);

    boardRenderer = new BoardRenderer(true);
    subBoardRenderer = new BoardRenderer(false);
    variationTree = new VariationTree();
    winrateGraph = new WinrateGraph();

    setMinimumSize(new Dimension(640, 480));
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

    commentPane = new JTextPane();
    commentPane.setEditable(false);
    commentPane.setMargin(new Insets(5, 5, 5, 5));
    commentPane.setBackground(Lizzie.config.commentBackgroundColor);
    commentPane.setForeground(Lizzie.config.commentFontColor);
    scrollPane = new JScrollPane();
    scrollPane.setViewportView(commentPane);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(
        javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    commentRect = new Rectangle(0, 0, 0, 0);

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
    repaint();

    // When the window is closed: save the SGF file, then run shutdown()
    this.addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            Lizzie.shutdown();
          }
        });
  }

  public static void startNewGame() {
    GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();

    NewGameDialog newGameDialog = new NewGameDialog();
    newGameDialog.setGameInfo(gameInfo);
    newGameDialog.setVisible(true);
    boolean playerIsBlack = newGameDialog.playerIsBlack();
    newGameDialog.dispose();
    if (newGameDialog.isCancelled()) return;

    Lizzie.board.clear();
    Lizzie.board.getHistory().setGameInfo(gameInfo);
    Lizzie.leelaz.sendCommand("komi " + gameInfo.getKomi());

    Lizzie.leelaz.sendCommand(
        "time_settings 0 "
            + Lizzie.config.config.getJSONObject("leelaz").getInt("max-game-thinking-time-seconds")
            + " 1");
    Lizzie.frame.playerIsBlack = playerIsBlack;
    Lizzie.frame.isPlayingAgainstLeelaz = true;

    boolean isHandicapGame = gameInfo.getHandicap() != 0;
    if (isHandicapGame) {
      Lizzie.board.getHistory().getData().blackToPlay = false;
      Lizzie.leelaz.sendCommand("fixed_handicap " + gameInfo.getHandicap());
      if (playerIsBlack) Lizzie.leelaz.genmove("W");
    } else if (!playerIsBlack) {
      Lizzie.leelaz.genmove("B");
    }
  }

  public static void editGameInfo() {
    GameInfo gameInfo = Lizzie.board.getHistory().getGameInfo();

    GameInfoDialog gameInfoDialog = new GameInfoDialog();
    gameInfoDialog.setGameInfo(gameInfo);
    gameInfoDialog.setVisible(true);

    gameInfoDialog.dispose();
  }

  public static void saveFile() {
    FileNameExtensionFilter filter = new FileNameExtensionFilter("*.sgf", "SGF");
    JSONObject filesystem = Lizzie.config.persisted.getJSONObject("filesystem");
    JFileChooser chooser = new JFileChooser(filesystem.getString("last-folder"));
    chooser.setFileFilter(filter);
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showSaveDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (file.exists()) {
        int ret =
            JOptionPane.showConfirmDialog(
                null,
                resourceBundle.getString("LizzieFrame.prompt.sgfExists"),
                "Warning",
                JOptionPane.OK_CANCEL_OPTION);
        if (ret == JOptionPane.CANCEL_OPTION) {
          return;
        }
      }
      if (!file.getPath().endsWith(".sgf")) {
        file = new File(file.getPath() + ".sgf");
      }
      try {
        SGFParser.save(Lizzie.board, file.getPath());
        filesystem.put("last-folder", file.getParent());
      } catch (IOException err) {
        JOptionPane.showConfirmDialog(
            null,
            resourceBundle.getString("LizzieFrame.prompt.failedTosaveFile"),
            "Error",
            JOptionPane.ERROR);
      }
    }
  }

  public static void openFile() {
    FileNameExtensionFilter filter = new FileNameExtensionFilter("*.sgf or *.gib", "SGF", "GIB");
    JSONObject filesystem = Lizzie.config.persisted.getJSONObject("filesystem");
    JFileChooser chooser = new JFileChooser(filesystem.getString("last-folder"));

    chooser.setFileFilter(filter);
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) loadFile(chooser.getSelectedFile());
  }

  public static void loadFile(File file) {
    JSONObject filesystem = Lizzie.config.persisted.getJSONObject("filesystem");
    if (!(file.getPath().endsWith(".sgf") || file.getPath().endsWith(".gib"))) {
      file = new File(file.getPath() + ".sgf");
    }
    try {
      System.out.println(file.getPath());
      if (file.getPath().endsWith(".sgf")) {
        SGFParser.load(file.getPath());
      } else {
        GIBParser.load(file.getPath());
      }
      filesystem.put("last-folder", file.getParent());
    } catch (IOException err) {
      JOptionPane.showConfirmDialog(
          null,
          resourceBundle.getString("LizzieFrame.prompt.failedToOpenFile"),
          "Error",
          JOptionPane.ERROR);
    }
  }

  private BufferedImage cachedImage;

  private BufferedImage cachedBackground;
  private int cachedBackgroundWidth = 0, cachedBackgroundHeight = 0;
  private boolean cachedBackgroundShowControls = false;
  private boolean cachedShowWinrate = true;
  private boolean cachedShowVariationGraph = true;
  private boolean redrawBackgroundAnyway = false;

  /**
   * Draws the game board and interface
   *
   * @param g0 not used
   */
  public void paint(Graphics g0) {
    autosaveMaybe();

    Optional<Graphics2D> backgroundG;
    if (cachedBackgroundWidth != getWidth()
        || cachedBackgroundHeight != getHeight()
        || cachedBackgroundShowControls != showControls
        || cachedShowWinrate != Lizzie.config.showWinrate
        || cachedShowVariationGraph != Lizzie.config.showVariationGraph
        || redrawBackgroundAnyway) {
      backgroundG = Optional.of(createBackground());
    } else {
      backgroundG = Optional.empty();
    }

    if (!showControls) {
      // layout parameters

      int topInset = this.getInsets().top;

      // board
      int maxSize = (int) (min(getWidth(), getHeight() - topInset));
      maxSize = max(maxSize, Board.boardSize + 5); // don't let maxWidth become too small
      int boardX = (getWidth() - maxSize) / 2;
      int boardY = topInset + (getHeight() - topInset - maxSize) / 2 + 3;

      int panelMargin = (int) (maxSize * 0.05);

      // move statistics (winrate bar)
      // boardX equals width of space on each side
      int statx = 0;
      int staty = boardY + maxSize / 8;
      int statw = boardX - statx - panelMargin;
      int stath = maxSize / 10;

      // winrate graph
      int grx = statx;
      int gry = staty + stath;
      int grw = statw;
      int grh = statw;

      // graph container
      int contx = statx;
      int conty = staty;
      int contw = statw;
      int conth = stath;

      // captured stones
      int capx = 0;
      int capy = this.getInsets().top;
      int capw = boardX - (int) (maxSize * 0.05);
      int caph = boardY + maxSize / 8 - this.getInsets().top;

      // variation tree container
      int vx = boardX + maxSize + panelMargin;
      int vy = 0;
      int vw = getWidth() - vx;
      int vh = getHeight();

      // variation tree
      int treex = vx;
      int treey = vy;
      int treew = vw + 1;
      int treeh = vh;

      // pondering message
      int ponderingX = this.getInsets().left;
      int ponderingY = boardY + (int) (maxSize * 0.93);
      double ponderingSize = .02;

      // dynamic komi
      int dynamicKomiLabelX = this.getInsets().left;
      int dynamicKomiLabelY = boardY + (int) (maxSize * 0.86);

      int dynamicKomiX = this.getInsets().left;
      int dynamicKomiY = boardY + (int) (maxSize * 0.89);
      double dynamicKomiSize = .02;

      // loading message
      int loadingX = ponderingX;
      int loadingY = ponderingY;
      double loadingSize = 0.03;

      // subboard
      int subBoardX = 0;
      int subBoardY = gry + grh;
      int subBoardWidth = grw;
      int subBoardHeight = ponderingY - subBoardY;
      int subBoardLength = min(subBoardWidth, subBoardHeight);

      if (Lizzie.config.showLargeSubBoard()) {
        boardX = getWidth() - maxSize - panelMargin;
        int spaceW = boardX - panelMargin;
        int spaceH = getHeight() - topInset;
        int panelW = spaceW / 2;
        int panelH = spaceH / 4;
        capx = 0;
        capy = topInset;
        capw = panelW;
        caph = (int) (panelH * 0.2);
        statx = 0;
        staty = capy + caph;
        statw = panelW;
        stath = (int) (panelH * 0.4);
        grx = statx;
        gry = staty + stath;
        grw = statw;
        grh = panelH - caph - stath;
        contx = statx;
        conty = staty;
        contw = statw;
        conth = stath + grh;
        vx = panelW;
        vy = 0;
        vw = panelW;
        vh = topInset + panelH;
        treex = vx;
        treey = vy;
        treew = vw + 1;
        treeh = vh;
        subBoardX = 0;
        subBoardY = topInset + panelH;
        subBoardWidth = spaceW;
        subBoardHeight = ponderingY - subBoardY;
        subBoardLength = min(subBoardWidth, subBoardHeight);
      }

      // initialize

      cachedImage = new BufferedImage(getWidth(), getHeight(), TYPE_INT_ARGB);
      Graphics2D g = (Graphics2D) cachedImage.getGraphics();

      if (Lizzie.config.showStatus) drawCommandString(g);

      boardRenderer.setLocation(boardX, boardY);
      boardRenderer.setBoardLength(maxSize);
      boardRenderer.draw(g);

      if (Lizzie.leelaz.isLoaded()) {
        if (Lizzie.config.showStatus) {
          String statusKey = "LizzieFrame.display." + (Lizzie.leelaz.isPondering() ? "on" : "off");
          String statusText = resourceBundle.getString(statusKey);
          String ponderingText = resourceBundle.getString("LizzieFrame.display.pondering");
          String switching = resourceBundle.getString("LizzieFrame.prompt.switching");
          String switchingText = Lizzie.leelaz.switching() ? switching : "";
          String weightText = Lizzie.leelaz.currentWeight();
          String text = ponderingText + " " + statusText + " " + weightText + " " + switchingText;
          drawPonderingState(g, text, ponderingX, ponderingY, ponderingSize);
        }

        Optional<String> dynamicKomi = Lizzie.leelaz.getDynamicKomi();
        if (Lizzie.config.showDynamicKomi && dynamicKomi.isPresent()) {
          String text = resourceBundle.getString("LizzieFrame.display.dynamic-komi");
          drawPonderingState(g, text, dynamicKomiLabelX, dynamicKomiLabelY, dynamicKomiSize);
          drawPonderingState(g, dynamicKomi.get(), dynamicKomiX, dynamicKomiY, dynamicKomiSize);
        }

        // Todo: Make board move over when there is no space beside the board
        if (Lizzie.config.showWinrate) {
          if (backgroundG.isPresent()) {
            drawContainer(backgroundG.get(), contx, conty, contw, conth);
          }
          drawMoveStatistics(g, statx, staty, statw, stath);
          winrateGraph.draw(g, grx, gry, grw, grh);
        }

        if (Lizzie.config.showVariationGraph) {
          if (backgroundG.isPresent()) {
            drawContainer(backgroundG.get(), vx, vy, vw, vh);
          }
          int cHeight = 0;
          if (Lizzie.config.showComment) {
            // Draw the Comment of the Sgf
            cHeight = drawComment(g, vx, vy, vw, vh, false);
          }
          variationTree.draw(g, treex, treey, treew, treeh - cHeight);
        } else {
          if (Lizzie.config.showComment) {
            // Draw the Comment of the Sgf
            drawComment(g, vx, topInset, vw, vh - topInset + vy, true);
          }
        }

        if (Lizzie.config.showSubBoard) {
          try {
            subBoardRenderer.setLocation(subBoardX, subBoardY);
            subBoardRenderer.setBoardLength(subBoardLength);
            subBoardRenderer.draw(g);
          } catch (Exception e) {
            // This can happen when no space is left for subboard.
          }
        }
      } else if (Lizzie.config.showStatus) {
        String loadingText = resourceBundle.getString("LizzieFrame.display.loading");
        drawPonderingState(g, loadingText, loadingX, loadingY, loadingSize);
      }

      if (Lizzie.config.showCaptured) drawCaptured(g, capx, capy, capw, caph);

      // cleanup
      g.dispose();
    }

    // draw the image
    Graphics2D bsGraphics = (Graphics2D) bs.getDrawGraphics();
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

  private Graphics2D createBackground() {
    cachedBackground = new BufferedImage(getWidth(), getHeight(), TYPE_INT_RGB);
    cachedBackgroundWidth = cachedBackground.getWidth();
    cachedBackgroundHeight = cachedBackground.getHeight();
    cachedBackgroundShowControls = showControls;
    cachedShowWinrate = Lizzie.config.showWinrate;
    cachedShowVariationGraph = Lizzie.config.showVariationGraph;

    redrawBackgroundAnyway = false;

    Graphics2D g = cachedBackground.createGraphics();

    BufferedImage wallpaper = boardRenderer.getWallpaper();
    int drawWidth = max(wallpaper.getWidth(), getWidth());
    int drawHeight = max(wallpaper.getHeight(), getHeight());
    // Support seamless texture
    boardRenderer.drawTextureImage(g, wallpaper, 0, 0, drawWidth, drawHeight);

    return g;
  }

  private void drawContainer(Graphics g, int vx, int vy, int vw, int vh) {
    if (vx < cachedBackground.getMinX()
        || vx + vw > cachedBackground.getMinX() + cachedBackground.getWidth()
        || vy < cachedBackground.getMinY()
        || vy + vh > cachedBackground.getMinY() + cachedBackground.getHeight()) {
      return;
    }

    BufferedImage result = new BufferedImage(vw, vh, TYPE_INT_ARGB);
    filter20.filter(cachedBackground.getSubimage(vx, vy, vw, vh), result);
    g.drawImage(result, vx, vy, null);
  }

  private void drawPonderingState(Graphics2D g, String text, int x, int y, double size) {
    int fontSize = (int) (max(getWidth(), getHeight()) * size);
    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, fontSize);
    FontMetrics fm = g.getFontMetrics(font);
    int stringWidth = fm.stringWidth(text);
    // Truncate too long text when display switching prompt
    if (Lizzie.leelaz.isLoaded()) {
      int mainBoardX = boardRenderer.getLocation().x;
      if ((mainBoardX > x) && stringWidth > (mainBoardX - x)) {
        text = truncateStringByWidth(text, fm, mainBoardX - x);
        stringWidth = fm.stringWidth(text);
      }
    }
    int stringHeight = fm.getAscent() - fm.getDescent();
    int width = max(stringWidth, 1);
    int height = max((int) (stringHeight * 1.2), 1);

    BufferedImage result = new BufferedImage(width, height, TYPE_INT_ARGB);
    // commenting this out for now... always causing an exception on startup. will fix in the
    // upcoming refactoring
    //        filter20.filter(cachedBackground.getSubimage(x, y, result.getWidth(),
    // result.getHeight()), result);
    g.drawImage(result, x, y, null);

    g.setColor(new Color(0, 0, 0, 130));
    g.fillRect(x, y, width, height);
    g.drawRect(x, y, width, height);

    g.setColor(Color.white);
    g.setFont(font);
    g.drawString(
        text, x + (width - stringWidth) / 2, y + stringHeight + (height - stringHeight) / 2);
  }

  /**
   * @return a shorter, rounded string version of playouts. e.g. 345 -> 345, 1265 -> 1.3k, 44556 ->
   *     45k, 133523 -> 134k, 1234567 -> 1.2m
   */
  public String getPlayoutsString(int playouts) {
    if (playouts >= 1_000_000) {
      double playoutsDouble = (double) playouts / 100_000; // 1234567 -> 12.34567
      return round(playoutsDouble) / 10.0 + "m";
    } else if (playouts >= 10_000) {
      double playoutsDouble = (double) playouts / 1_000; // 13265 -> 13.265
      return round(playoutsDouble) + "k";
    } else if (playouts >= 1_000) {
      double playoutsDouble = (double) playouts / 100; // 1265 -> 12.65
      return round(playoutsDouble) / 10.0 + "k";
    } else {
      return String.valueOf(playouts);
    }
  }

  /**
   * Truncate text that is too long for the given width
   *
   * @param line
   * @param fm
   * @param fitWidth
   * @return fitted
   */
  private static String truncateStringByWidth(String line, FontMetrics fm, int fitWidth) {
    if (line.isEmpty()) {
      return "";
    }
    int width = fm.stringWidth(line);
    if (width > fitWidth) {
      int guess = line.length() * fitWidth / width;
      String before = line.substring(0, guess).trim();
      width = fm.stringWidth(before);
      if (width > fitWidth) {
        int diff = width - fitWidth;
        int i = 0;
        for (; (diff > 0 && i < 5); i++) {
          diff = diff - fm.stringWidth(line.substring(guess - i - 1, guess - i));
        }
        return line.substring(0, guess - i).trim();
      } else {
        return before;
      }
    } else {
      return line;
    }
  }

  private GaussianFilter filter20 = new GaussianFilter(20);
  private GaussianFilter filter10 = new GaussianFilter(10);

  /** Display the controls */
  void drawControls() {
    userAlreadyKnowsAboutCommandString = true;

    cachedImage = new BufferedImage(getWidth(), getHeight(), TYPE_INT_ARGB);

    // redraw background
    createBackground();

    List<String> commandsToShow = new ArrayList<>(Arrays.asList(commands));
    if (Lizzie.leelaz.getDynamicKomi().isPresent()) {
      commandsToShow.add(resourceBundle.getString("LizzieFrame.commands.keyD"));
    }

    Graphics2D g = cachedImage.createGraphics();

    int maxSize = min(getWidth(), getHeight());
    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, (int) (maxSize * 0.034));
    g.setFont(font);

    FontMetrics metrics = g.getFontMetrics(font);
    int maxCmdWidth = commandsToShow.stream().mapToInt(c -> metrics.stringWidth(c)).max().orElse(0);
    int lineHeight = (int) (font.getSize() * 1.15);

    int boxWidth = min((int) (maxCmdWidth * 1.4), getWidth());
    int boxHeight = min(commandsToShow.size() * lineHeight, getHeight());

    int commandsX = min(getWidth() / 2 - boxWidth / 2, getWidth());
    int commandsY = min(getHeight() / 2 - boxHeight / 2, getHeight());

    BufferedImage result = new BufferedImage(boxWidth, boxHeight, TYPE_INT_ARGB);
    filter10.filter(
        cachedBackground.getSubimage(commandsX, commandsY, boxWidth, boxHeight), result);
    g.drawImage(result, commandsX, commandsY, null);

    g.setColor(new Color(0, 0, 0, 130));
    g.fillRect(commandsX, commandsY, boxWidth, boxHeight);
    int strokeRadius = 2;
    g.setStroke(new BasicStroke(2 * strokeRadius));
    g.setColor(new Color(0, 0, 0, 60));
    g.drawRect(
        commandsX + strokeRadius,
        commandsY + strokeRadius,
        boxWidth - 2 * strokeRadius,
        boxHeight - 2 * strokeRadius);

    int verticalLineX = (int) (commandsX + boxWidth * 0.3);
    g.setColor(new Color(0, 0, 0, 60));
    g.drawLine(
        verticalLineX,
        commandsY + 2 * strokeRadius,
        verticalLineX,
        commandsY + boxHeight - 2 * strokeRadius);

    g.setStroke(new BasicStroke(1));

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(Color.WHITE);
    int lineOffset = commandsY;
    for (String command : commandsToShow) {
      String[] split = command.split("\\|");
      g.drawString(
          split[0],
          verticalLineX - metrics.stringWidth(split[0]) - strokeRadius * 4,
          font.getSize() + lineOffset);
      g.drawString(split[1], verticalLineX + strokeRadius * 4, font.getSize() + lineOffset);
      lineOffset += lineHeight;
    }

    refreshBackground();
  }

  private boolean userAlreadyKnowsAboutCommandString = false;

  private void drawCommandString(Graphics2D g) {
    if (userAlreadyKnowsAboutCommandString) return;

    int maxSize = (int) (min(getWidth(), getHeight()) * 0.98);

    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, (int) (maxSize * 0.03));
    String commandString = resourceBundle.getString("LizzieFrame.prompt.showControlsHint");
    int strokeRadius = 2;

    int showCommandsHeight = (int) (font.getSize() * 1.1);
    int showCommandsWidth = g.getFontMetrics(font).stringWidth(commandString) + 4 * strokeRadius;
    int showCommandsX = this.getInsets().left;
    int showCommandsY = getHeight() - showCommandsHeight - this.getInsets().bottom;
    g.setColor(new Color(0, 0, 0, 130));
    g.fillRect(showCommandsX, showCommandsY, showCommandsWidth, showCommandsHeight);
    g.setStroke(new BasicStroke(2 * strokeRadius));
    g.setColor(new Color(0, 0, 0, 60));
    g.drawRect(
        showCommandsX + strokeRadius,
        showCommandsY + strokeRadius,
        showCommandsWidth - 2 * strokeRadius,
        showCommandsHeight - 2 * strokeRadius);
    g.setStroke(new BasicStroke(1));

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(Color.WHITE);
    g.setFont(font);
    g.drawString(commandString, showCommandsX + 2 * strokeRadius, showCommandsY + font.getSize());
  }

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
    int strokeRadius = 3;
    g.setStroke(new BasicStroke(2 * strokeRadius));
    g.drawLine(
        posX + strokeRadius, posY + strokeRadius,
        posX - strokeRadius + width, posY + strokeRadius);
    g.drawLine(
        posX + strokeRadius, posY + 3 * strokeRadius,
        posX + strokeRadius, posY - strokeRadius + height);
    g.drawLine(
        posX - strokeRadius + width, posY + 3 * strokeRadius,
        posX - strokeRadius + width, posY - strokeRadius + height);

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
          resourceBundle.getString("LizzieFrame.display.lastMove") + text,
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

  private void drawCaptured(Graphics2D g, int posX, int posY, int width, int height) {
    // Draw border
    g.setColor(new Color(0, 0, 0, 130));
    g.fillRect(posX, posY, width, height);

    // border. does not include bottom edge
    int strokeRadius = 3;
    g.setStroke(new BasicStroke(2 * strokeRadius));
    g.drawLine(
        posX + strokeRadius, posY + strokeRadius, posX - strokeRadius + width, posY + strokeRadius);
    g.drawLine(
        posX + strokeRadius,
        posY + 3 * strokeRadius,
        posX + strokeRadius,
        posY - strokeRadius + height);
    g.drawLine(
        posX - strokeRadius + width,
        posY + 3 * strokeRadius,
        posX - strokeRadius + width,
        posY - strokeRadius + height);

    // Draw middle line
    g.drawLine(
        posX - strokeRadius + width / 2,
        posY + 3 * strokeRadius,
        posX - strokeRadius + width / 2,
        posY - strokeRadius + height);
    g.setColor(Color.white);

    // Draw black and white "stone"
    int diam = height / 3;
    int smallDiam = diam / 2;
    int bdiam = diam, wdiam = diam;
    if (Lizzie.board.inScoreMode()) {
      // do nothing
    } else if (Lizzie.board.getHistory().isBlacksTurn()) {
      wdiam = smallDiam;
    } else {
      bdiam = smallDiam;
    }
    g.setColor(Color.black);
    g.fillOval(
        posX + width / 4 - bdiam / 2, posY + height * 3 / 8 + (diam - bdiam) / 2, bdiam, bdiam);

    g.setColor(Color.WHITE);
    g.fillOval(
        posX + width * 3 / 4 - wdiam / 2, posY + height * 3 / 8 + (diam - wdiam) / 2, wdiam, wdiam);

    // Draw captures
    String bval, wval;
    setPanelFont(g, (float) (width * 0.06));
    if (Lizzie.board.inScoreMode()) {
      double score[] = Lizzie.board.getScore(Lizzie.board.scoreStones());
      bval = String.format("%.0f", score[0]);
      wval = String.format("%.1f", score[1]);
    } else {
      bval = String.format("%d", Lizzie.board.getData().blackCaptures);
      wval = String.format("%d", Lizzie.board.getData().whiteCaptures);
    }

    g.setColor(Color.WHITE);
    int bw = g.getFontMetrics().stringWidth(bval);
    int ww = g.getFontMetrics().stringWidth(wval);
    boolean largeSubBoard = Lizzie.config.showLargeSubBoard();
    int bx = (largeSubBoard ? diam : -bw / 2);
    int wx = (largeSubBoard ? bx : -ww / 2);

    g.drawString(bval, posX + width / 4 + bx, posY + height * 7 / 8);
    g.drawString(wval, posX + width * 3 / 4 + wx, posY + height * 7 / 8);
  }

  private void setPanelFont(Graphics2D g, float size) {
    Font font = uiFont.deriveFont(Font.PLAIN, size);
    g.setFont(font);
  }

  /**
   * Checks whether or not something was clicked and performs the appropriate action
   *
   * @param x x coordinate
   * @param y y coordinate
   */
  public void onClicked(int x, int y) {
    // Check for board click
    Optional<int[]> boardCoordinates = boardRenderer.convertScreenToCoordinates(x, y);
    int moveNumber = winrateGraph.moveNumber(x, y);

    if (boardCoordinates.isPresent()) {
      int[] coords = boardCoordinates.get();
      if (Lizzie.board.inAnalysisMode()) Lizzie.board.toggleAnalysis();
      if (!isPlayingAgainstLeelaz || (playerIsBlack == Lizzie.board.getData().blackToPlay))
        Lizzie.board.place(coords[0], coords[1]);
    }
    if (Lizzie.config.showWinrate && moveNumber >= 0) {
      isPlayingAgainstLeelaz = false;
      Lizzie.board.goToMoveNumberBeyondBranch(moveNumber);
    }
    if (Lizzie.config.showSubBoard && subBoardRenderer.isInside(x, y)) {
      Lizzie.config.toggleLargeSubBoard();
    }
    repaint();
  }

  private final Consumer<String> placeVariation =
      v -> Board.asCoordinates(v).ifPresent(c -> Lizzie.board.place(c[0], c[1]));

  public boolean playCurrentVariation() {
    boardRenderer.variationOpt.ifPresent(vs -> vs.forEach(placeVariation));
    return boardRenderer.variationOpt.isPresent();
  }

  public void playBestMove() {
    boardRenderer.bestMoveCoordinateName().ifPresent(placeVariation);
  }

  public void onMouseMoved(int x, int y) {
    mouseOverCoordinate = outOfBoundCoordinate;
    Optional<int[]> coords = boardRenderer.convertScreenToCoordinates(x, y);
    coords.filter(c -> !isMouseOver(c[0], c[1])).ifPresent(c -> repaint());
    coords.ifPresent(c -> mouseOverCoordinate = c);
  }

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

  /**
   * Process Comment Mouse Wheel Moved
   *
   * @return true when the scroll event was processed by this method
   */
  public boolean processCommentMouseWheelMoved(MouseWheelEvent e) {
    if (Lizzie.config.showComment && commentRect.contains(e.getX(), e.getY())) {
      scrollPane.dispatchEvent(e);
      createCommentImage(true, 0, 0);
      getGraphics()
          .drawImage(
              cachedCommentImage,
              commentRect.x,
              commentRect.y,
              commentRect.width,
              commentRect.height,
              null);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Create comment cached image
   *
   * @param forceRefresh
   * @param w
   * @param h
   */
  public void createCommentImage(boolean forceRefresh, int w, int h) {
    if (forceRefresh || scrollPane.getWidth() != w || scrollPane.getHeight() != h) {
      if (w > 0 && h > 0) {
        scrollPane.setSize(w, h);
      }
      cachedCommentImage =
          new BufferedImage(scrollPane.getWidth(), scrollPane.getHeight(), TYPE_INT_ARGB);
      Graphics2D g2 = cachedCommentImage.createGraphics();
      scrollPane.doLayout();
      scrollPane.addNotify();
      scrollPane.validate();
      scrollPane.printAll(g2);
      g2.dispose();
    }
  }

  private void autosaveMaybe() {
    int interval =
        Lizzie.config.config.getJSONObject("ui").getInt("autosave-interval-seconds") * 1000;
    long currentTime = System.currentTimeMillis();
    if (interval > 0 && currentTime - lastAutosaveTime >= interval) {
      Lizzie.board.autosave();
      lastAutosaveTime = currentTime;
    }
  }

  public void toggleCoordinates() {
    showCoordinates = !showCoordinates;
  }

  public void setPlayers(String whitePlayer, String blackPlayer) {
    playerTitle = String.format("(%s [W] vs %s [B])", whitePlayer, blackPlayer);
    updateTitle();
  }

  public void updateTitle() {
    StringBuilder sb = new StringBuilder(DEFAULT_TITLE);
    sb.append(playerTitle);
    sb.append(" [" + Lizzie.leelaz.engineCommand() + "]");
    setTitle(sb.toString());
  }

  private void setDisplayedBranchLength(int n) {
    boardRenderer.setDisplayedBranchLength(n);
  }

  public void startRawBoard() {
    boolean onBranch = boardRenderer.isShowingBranch();
    int n = (onBranch ? 1 : BoardRenderer.SHOW_RAW_BOARD);
    boardRenderer.setDisplayedBranchLength(n);
  }

  public void stopRawBoard() {
    boardRenderer.setDisplayedBranchLength(BoardRenderer.SHOW_NORMAL_BOARD);
  }

  public boolean incrementDisplayedBranchLength(int n) {
    return boardRenderer.incrementDisplayedBranchLength(n);
  }

  public void resetTitle() {
    playerTitle = "";
    updateTitle();
  }

  public void copySgf() {
    try {
      // Get sgf content from game
      String sgfContent = SGFParser.saveToString();

      // Save to clipboard
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable transferableString = new StringSelection(sgfContent);
      clipboard.setContents(transferableString, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void pasteSgf() {
    // Get string from clipboard
    String sgfContent =
        Optional.ofNullable(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null))
            .filter(cc -> cc.isDataFlavorSupported(DataFlavor.stringFlavor))
            .flatMap(
                cc -> {
                  try {
                    return Optional.of((String) cc.getTransferData(DataFlavor.stringFlavor));
                  } catch (UnsupportedFlavorException e) {
                    e.printStackTrace();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                  return Optional.empty();
                })
            .orElse("");

    // Load game contents from sgf string
    if (!sgfContent.isEmpty()) {
      SGFParser.loadFromString(sgfContent);
    }
  }

  public void increaseMaxAlpha(int k) {
    boardRenderer.increaseMaxAlpha(k);
  }

  /**
   * Draw the Comment of the Sgf file
   *
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   * @param full
   * @return
   */
  private int drawComment(Graphics2D g, int x, int y, int w, int h, boolean full) {
    String comment = Lizzie.board.getHistory().getData().comment;
    int cHeight = full ? h : (int) (h * 0.5);
    int fontSize = (int) (min(getWidth(), getHeight()) * 0.0294);
    if (Lizzie.config.commentFontSize > 0) {
      fontSize = Lizzie.config.commentFontSize;
    } else if (fontSize < 16) {
      fontSize = 16;
    }
    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, fontSize);
    commentPane.setFont(font);
    commentPane.setText(comment);
    commentPane.setSize(w, cHeight);
    createCommentImage(!comment.equals(this.cachedComment), w, cHeight);
    commentRect =
        new Rectangle(x, y + (h - cHeight), scrollPane.getWidth(), scrollPane.getHeight());
    g.drawImage(
        cachedCommentImage,
        commentRect.x,
        commentRect.y,
        commentRect.width,
        commentRect.height,
        null);
    cachedComment = comment;
    return cHeight;
  }
}
