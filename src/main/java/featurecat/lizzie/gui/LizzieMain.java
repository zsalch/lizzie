package featurecat.lizzie.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JFrame;

import org.json.JSONArray;

import featurecat.lizzie.Lizzie;

public class LizzieMain extends JFrame {

  public static BoardPane boardPane;
  private static final String DEFAULT_TITLE = "Lizzie - Leela Zero Interface";

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
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

  /**
   * Create the frame.
   */
  public LizzieMain() {
    super(DEFAULT_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setMinimumSize(new Dimension(640, 400));
    JSONArray windowSize = Lizzie.config.uiConfig.getJSONArray("window-size");
    setSize(windowSize.getInt(0), windowSize.getInt(1));
    setLocationRelativeTo(null); // Start centered, needs to be called *after* setSize...

    boardPane = new BoardPane(this);

    if (Lizzie.config.startMaximized) {
      setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    setVisible(true);
  }

}
