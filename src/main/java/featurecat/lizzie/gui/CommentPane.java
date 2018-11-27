package featurecat.lizzie.gui;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.min;

import com.jhlabs.image.GaussianFilter;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.Board;
import java.awt.*;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javax.swing.*;

/** The window used to display the game. */
public class CommentPane extends LizziePane {
  private static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");

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

  // Save the player title
  private String playerTitle = "";

  // Display Comment
  private JScrollPane scrollPane;
  private JTextPane commentPane;
  private BufferedImage cachedCommentImage = new BufferedImage(1, 1, TYPE_INT_ARGB);
  private String cachedComment;
  private Rectangle commentRect;

  /** Creates a window */
  public CommentPane(LizzieMain owner) {
    super(owner);
    // setModal(true);

    // setModalityType(ModalityType.APPLICATION_MODAL);

    // setMinimumSize(new Dimension(640, 400));
    // JSONArray windowSize = Lizzie.config.uiConfig.getJSONArray("window-size");
    //    setBounds(200, 300, 200, 300);
    //    commentRect = new Rectangle(0, 0, 100, 150);

    // setBounds(owner.getInsets().left, owner.getInsets().top, windowSize.getInt(0)
    // - owner.getInsets().left - owner.getInsets().right, windowSize.getInt(1) -
    // owner.getInsets().top - owner.getInsets().bottom);
    // setLocationRelativeTo(null); // Start centered, needs to be called *after*
    // setSize...

    commentPane = new JTextPane();
    commentPane.setText("Comment Pane");
    commentPane.setEditable(false);
    //    commentPane.setMargin(new Insets(5, 5, 5, 5));
    commentPane.setBackground(Lizzie.config.commentBackgroundColor);
    commentPane.setForeground(Lizzie.config.commentFontColor);
    scrollPane = new JScrollPane();
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(
        javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    getContentPane().add(scrollPane);
    scrollPane.setViewportView(commentPane);
    //    setUndecorated(true);
    // getRootPane().setBorder(BorderFactory.createEmptyBorder());
    //     getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    setVisible(true);

    //    createBufferStrategy(2);
    //    bs = getBufferStrategy();
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

  private GaussianFilter filter20 = new GaussianFilter(20);
  private GaussianFilter filter10 = new GaussianFilter(10);

  private boolean userAlreadyKnowsAboutCommandString = false;

  private final Consumer<String> placeVariation =
      v -> Board.asCoordinates(v).ifPresent(c -> Lizzie.board.place(c[0], c[1]));

  /**
   * Process Comment Mouse Wheel Moved
   *
   * @return true when the scroll event was processed by this method
   */
  public boolean processCommentMouseWheelMoved(MouseWheelEvent e) {
    if (Lizzie.config.showComment && commentRect.contains(e.getX(), e.getY())) {
      scrollPane.dispatchEvent(e);
      createCommentImage(true, commentRect.width, commentRect.height);
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
        scrollPane.addNotify();
        scrollPane.setSize(w, h);
        cachedCommentImage =
            new BufferedImage(scrollPane.getWidth(), scrollPane.getHeight(), TYPE_INT_ARGB);
        Graphics2D g2 = cachedCommentImage.createGraphics();
        scrollPane.validate();
        scrollPane.printAll(g2);
        g2.dispose();
      }
    }
  }

  /**
   * Draw the Comment of the Sgf file
   *
   * @param g
   * @param x
   * @param y
   * @param w
   * @param h
   */
  private void drawComment(Graphics2D g, int x, int y, int w, int h) {
    String comment = Lizzie.board.getHistory().getData().comment;
    int fontSize = (int) (min(getWidth(), getHeight()) * 0.0294);
    if (Lizzie.config.commentFontSize > 0) {
      fontSize = Lizzie.config.commentFontSize;
    } else if (fontSize < 16) {
      fontSize = 16;
    }
    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, fontSize);
    commentPane.setFont(font);
    commentPane.setText(comment);
    commentPane.setSize(w, h);
    createCommentImage(!comment.equals(this.cachedComment), w, h);
    commentRect = new Rectangle(x, y, scrollPane.getWidth(), scrollPane.getHeight());
    g.drawImage(
        cachedCommentImage,
        commentRect.x,
        commentRect.y,
        commentRect.width,
        commentRect.height,
        null);
    cachedComment = comment;
  }

  public void setDesignMode(boolean mode) {
    super.setDesignMode(mode);
    if (mode) {
      this.commentPane.setVisible(false);
      this.scrollPane.setVisible(false);
    } else {
      this.commentPane.setVisible(true);
      this.scrollPane.setVisible(true);
    }
  }
}
