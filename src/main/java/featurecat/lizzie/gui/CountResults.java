package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.json.JSONArray;

public class CountResults extends JFrame {
  private static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");
  public int allBlackCounts = 0;
  public int allWhiteCounts = 0;
  int blackEat = 0;
  int whiteEat = 0;
  JPanel buttonpanel = new JPanel();
  public JButton button =
      new JButton(resourceBundle.getString("CountDialog.estimateButton.clickone"));
  public JButton button2 =
      new JButton(resourceBundle.getString("CountDialog.autoEstimateButton.clickone"));

  public CountResults() {
    this.setAlwaysOnTop(true);
    this.add(buttonpanel, BorderLayout.SOUTH);
    this.setResizable(false);

    this.addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            invisiable();
            Lizzie.frame.removeEstimateRect();
            Lizzie.frame.noAutoEstimateByZen();
            Lizzie.frame.noEstimateByZen();
            Lizzie.frame.repaint();
          }
        });

    boolean persisted = Lizzie.config.persistedUi != null;
    if (persisted
        && Lizzie.config.persistedUi.optJSONArray("movecount-position") != null
        && Lizzie.config.persistedUi.optJSONArray("movecount-position").length() == 4) {
      JSONArray pos = Lizzie.config.persistedUi.getJSONArray("movecount-position");
      setBounds(pos.getInt(0), pos.getInt(1), pos.getInt(2), pos.getInt(3));
    } else {
      Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
      setBounds(0, (int) screensize.getHeight() / 2 - 125, 340, 260); // 240
    }

    try {
      this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/assets/logo.png")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    button2.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.frame.zen.noRead = false;
            if (!Lizzie.frame.isAutoEstimating) {
              Lizzie.frame.isAutoEstimating = true;
              Lizzie.frame.zen.syncBoradStat();
              Lizzie.frame.zen.countStones();
              button2.setText(resourceBundle.getString("CountDialog.autoEstimateButton.clicktwo"));
            } else {
              Lizzie.frame.removeEstimateRect();
              button2.setText(resourceBundle.getString("CountDialog.autoEstimateButton.clickone"));
              Lizzie.frame.isAutoEstimating = false;
            }
          }
        });
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            Lizzie.frame.zen.noRead = false;
            if (!Lizzie.frame.isEstimating) {
              Lizzie.frame.estimateByZen();
              Lizzie.frame.isEstimating = true;
              button.setText(resourceBundle.getString("CountDialog.estimateButton.clicktwo"));
            } else {
              Lizzie.frame.noEstimateByZen();
            }
          }
        });
    button.setBounds(0, 240, 100, 20);
    button2.setBounds(100, 240, 100, 20);
    buttonpanel.setBounds(0, 240, 100, 20);
    buttonpanel.add(button);
    buttonpanel.add(button2);
  }

  public void Counts(
      int blackEatCount,
      int whiteEatCount,
      int blackPrisonerCount,
      int whitePrisonerCount,
      int blackpont,
      int whitepoint) {
    allBlackCounts = 0;
    allWhiteCounts = 0;
    blackEat = 0;
    whiteEat = 0;

    allBlackCounts = blackpont + blackEatCount + whitePrisonerCount;
    allWhiteCounts = whitepoint + whiteEatCount + blackPrisonerCount;
    blackEat = blackEatCount;
    whiteEat = whiteEatCount;
    if (!Lizzie.frame.isAutoEstimating) {
      button.setText(resourceBundle.getString("CountDialog.estimateButton.clicktwo"));
      Lizzie.frame.isEstimating = true;
    }
    repaint();
  }

  public void paint(Graphics g) {

    Graphics2D g2 = (Graphics2D) g;

    Image image = null;
    try {
      image = ImageIO.read(getClass().getResourceAsStream("/assets/countbackground.jpg"));
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    int withtimes = (340 / image.getWidth(getOwner())) + 1;
    int highttimes = (260 / image.getHeight(getOwner())) + 1;

    for (int i = 0; i < highttimes; i++) {
      for (int j = 0; j < withtimes; j++) {
        g2.drawImage(image, image.getWidth(getOwner()) * j, image.getHeight(getOwner()) * i, null);
      }
    }
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.BLACK);
    g2.setStroke(new BasicStroke(2f));
    g2.fillOval(50, 100, 32, 32);
    // g2.drawOval(260,50, 32, 32);
    g2.setColor(Color.WHITE);
    g2.fillOval(260, 100, 32, 32);
    g2.setColor(Color.BLACK);
    Font allFont;

    try {
      allFont =
          Font.createFont(
              Font.TRUETYPE_FONT,
              Thread.currentThread()
                  .getContextClassLoader()
                  .getResourceAsStream("fonts/OpenSans-Semibold.ttf"));

    } catch (IOException | FontFormatException e) {
      e.printStackTrace();
    }
    allFont = new Font("allFont", Font.BOLD, 40);
    g2.setFont(allFont);
    if (allBlackCounts >= allWhiteCounts) {
      g2.setColor(Color.BLACK);
      g2.drawString(resourceBundle.getString("CountDialog.bigBlack"), 45, 75);
    } else {
      g2.setColor(Color.WHITE);
      g2.drawString(resourceBundle.getString("CountDialog.bigWhite"), 45, 75);
    }
    allFont = new Font("allFont", Font.BOLD, 25);
    g2.setFont(allFont);
    g2.drawString(
        resourceBundle.getString("CountDialog.onBoardLead")
            + Math.abs(allBlackCounts - allWhiteCounts)
            + resourceBundle.getString("CountDialog.points"),
        115,
        70);
    allFont = new Font("allFont", Font.BOLD, 20);
    g2.setColor(Color.BLACK);
    g2.setFont(allFont);
    g2.drawString(resourceBundle.getString("CountDialog.areaCount"), 145, 170);
    g2.drawString(resourceBundle.getString("CountDialog.eat"), 145, 212);
    g2.drawString(allBlackCounts + "", 53, 170);
    g2.drawString(blackEat + "", 53, 212);
    g2.setColor(Color.WHITE);
    g2.drawString(allWhiteCounts + "", 265, 170);
    g2.drawString(whiteEat + "", 265, 212);
    button.repaint();
    button2.repaint();
  }

  private void invisiable() {
    this.setVisible(false);
  }
}
