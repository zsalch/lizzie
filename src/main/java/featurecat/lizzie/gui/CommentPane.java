package featurecat.lizzie.gui;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import featurecat.lizzie.Lizzie;

/** The window used to display the game. */
public class CommentPane extends LizziePane {
  private static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");

  //  private final BufferStrategy bs;

  // Display Comment
  private HTMLDocument htmlDoc;
  private HTMLEditorKit htmlKit;
  private StyleSheet htmlStyle;
  private JScrollPane scrollPane;
  private JTextPane commentPane;
  private BufferedImage cachedCommentImage = new BufferedImage(1, 1, TYPE_INT_ARGB);
  private String cachedComment;
  private Rectangle commentRect;

  /** Creates a window */
  public CommentPane(LizzieMain owner) {
    super(owner);
    setLayout(new BorderLayout(0, 0));

    htmlKit = new HTMLEditorKit();
    htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
    htmlStyle = htmlKit.getStyleSheet();
    htmlStyle.addRule("body {background:#"+String.format("%02x%02x%02x",Lizzie.config.commentBackgroundColor.getRed(),Lizzie.config.commentBackgroundColor.getGreen(),Lizzie.config.commentBackgroundColor.getBlue())+"; color:#"+String.format("%02x%02x%02x",Lizzie.config.commentFontColor.getRed(),Lizzie.config.commentFontColor.getGreen(),Lizzie.config.commentFontColor.getBlue())+";}");
    
    commentPane = new JTextPane();
    commentPane.setEditorKit(htmlKit);
    commentPane.setDocument(htmlDoc);
    commentPane.setText("");
    commentPane.setEditable(true);
    //    commentPane.setMargin(new Insets(5, 5, 5, 5));
    commentPane.setBackground(Lizzie.config.commentBackgroundColor);
    commentPane.setForeground(Lizzie.config.commentFontColor);
    scrollPane = new JScrollPane();
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(
        javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    // TODO
    //    getContentPane().add(scrollPane);
    add(scrollPane);
    scrollPane.setViewportView(commentPane);
    //    setUndecorated(true);
    // getRootPane().setBorder(BorderFactory.createEmptyBorder());
    //     getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    setVisible(true);

    //    createBufferStrategy(2);
    //    bs = getBufferStrategy();
  }

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
  public void drawComment() {
    String comment = Lizzie.board.getHistory().getData().comment;
    int fontSize = (int) (min(getWidth(), getHeight()) * 0.0294);
    if (Lizzie.config.commentFontSize > 0) {
      fontSize = Lizzie.config.commentFontSize;
    } else if (fontSize < 16) {
      fontSize = 16;
    }
    Font font = new Font(Lizzie.config.fontName, Font.PLAIN, fontSize);
    commentPane.setFont(font);
    addText(comment);
  }

  private void addText(String text) {
    try {
      htmlDoc.remove(0, htmlDoc.getLength());
      htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), text, 0, 0, null);
      commentPane.setCaretPosition(htmlDoc.getLength());
    } catch (BadLocationException | IOException e) {
      e.printStackTrace();
    }
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
