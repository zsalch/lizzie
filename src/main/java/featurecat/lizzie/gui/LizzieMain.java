package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import javax.swing.JFrame;
import org.json.JSONArray;

public class LizzieMain extends JFrame {

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

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setMinimumSize(new Dimension(640, 400));
    JSONArray windowSize = Lizzie.config.uiConfig.getJSONArray("window-size");
    setSize(windowSize.getInt(0), windowSize.getInt(1));
    setLocationRelativeTo(null); // Start centered, needs to be called *after* setSize...

    //    boardPane = new BoardPane(this);
    Lizzie.frame = new BoardPane(this);
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

    Input input = new Input();

    addMouseListener(input);
    addKeyListener(input);
    addMouseWheelListener(input);
    addMouseMotionListener(input);
  }

  public void toggleDesignMode() {
    this.designMode = !this.designMode;
    Lizzie.frame.setDesignMode(designMode);
  }
}
