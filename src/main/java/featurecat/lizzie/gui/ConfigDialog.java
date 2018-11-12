package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigDialog extends JDialog {
  private JTextField txtEngine;
  private JTextField txtEngine1;
  private JTextField txtEngine2;
  private JTextField txtEngine3;
  private JTextField txtEngine4;
  private JTextField txtEngine5;
  private JTextField txtEngine6;
  private JTextField txtEngine7;
  private JTextField txtEngine8;
  private JTextField txtEngine9;

  /** Launch the application. */
  public static void main(String[] args) {
    try {
      ConfigDialog dialog = new ConfigDialog();
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Create the dialog. */
  public ConfigDialog() {
    setModalityType(ModalityType.APPLICATION_MODAL);
    setType(Type.POPUP);
    setBounds(100, 100, 661, 567);
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                setVisible(false);
              }
            });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
    {
      JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
      getContentPane().add(tabbedPane, BorderLayout.CENTER);

      JPanel engineTab = new JPanel();
      tabbedPane.addTab("Engine", null, engineTab, null);
      engineTab.setLayout(null);

      JLabel lblEngine = new JLabel("Default Engine");
      lblEngine.setBounds(6, 44, 92, 16);
      lblEngine.setHorizontalAlignment(SwingConstants.LEFT);
      engineTab.add(lblEngine);
      {
        txtEngine = new JTextField();
        txtEngine.setBounds(110, 39, 479, 26);
        engineTab.add(txtEngine);
        txtEngine.setColumns(10);
      }

      JLabel lblEngine1 = new JLabel("Engine 1");
      lblEngine1.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine1.setBounds(6, 80, 92, 16);
      engineTab.add(lblEngine1);

      txtEngine2 = new JTextField();
      txtEngine2.setColumns(10);
      txtEngine2.setBounds(110, 105, 479, 26);
      engineTab.add(txtEngine2);

      JLabel lblEngine2 = new JLabel("Engine 2");
      lblEngine2.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine2.setBounds(6, 110, 92, 16);
      engineTab.add(lblEngine2);

      txtEngine1 = new JTextField();
      txtEngine1.setColumns(10);
      txtEngine1.setBounds(110, 75, 479, 26);
      engineTab.add(txtEngine1);

      JLabel lblEngine3 = new JLabel("Engine 3");
      lblEngine3.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine3.setBounds(6, 140, 92, 16);
      engineTab.add(lblEngine3);

      txtEngine3 = new JTextField();
      txtEngine3.setColumns(10);
      txtEngine3.setBounds(110, 135, 479, 26);
      engineTab.add(txtEngine3);

      JLabel lblEngine4 = new JLabel("Engine 4");
      lblEngine4.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine4.setBounds(6, 170, 92, 16);
      engineTab.add(lblEngine4);

      txtEngine4 = new JTextField();
      txtEngine4.setColumns(10);
      txtEngine4.setBounds(110, 165, 479, 26);
      engineTab.add(txtEngine4);

      JLabel lblEngine5 = new JLabel("Engine 5");
      lblEngine5.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine5.setBounds(6, 200, 92, 16);
      engineTab.add(lblEngine5);

      txtEngine5 = new JTextField();
      txtEngine5.setColumns(10);
      txtEngine5.setBounds(110, 195, 479, 26);
      engineTab.add(txtEngine5);

      JLabel lblEngine6 = new JLabel("Engine 6");
      lblEngine6.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine6.setBounds(6, 230, 92, 16);
      engineTab.add(lblEngine6);

      txtEngine6 = new JTextField();
      txtEngine6.setColumns(10);
      txtEngine6.setBounds(110, 225, 479, 26);
      engineTab.add(txtEngine6);

      JLabel lblEngine7 = new JLabel("Engine 7");
      lblEngine7.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine7.setBounds(6, 260, 92, 16);
      engineTab.add(lblEngine7);

      txtEngine7 = new JTextField();
      txtEngine7.setColumns(10);
      txtEngine7.setBounds(110, 255, 479, 26);
      engineTab.add(txtEngine7);

      JLabel lblEngine8 = new JLabel("Engine 8");
      lblEngine8.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine8.setBounds(6, 290, 92, 16);
      engineTab.add(lblEngine8);

      txtEngine8 = new JTextField();
      txtEngine8.setColumns(10);
      txtEngine8.setBounds(110, 285, 479, 26);
      engineTab.add(txtEngine8);

      txtEngine9 = new JTextField();
      txtEngine9.setColumns(10);
      txtEngine9.setBounds(110, 318, 479, 26);
      engineTab.add(txtEngine9);

      JLabel lblEngine9 = new JLabel("Engine 9");
      lblEngine9.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine9.setBounds(6, 320, 92, 16);
      engineTab.add(lblEngine9);

      JButton button = new JButton("...");
      button.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              String el = getEngineLine();
            }
          });
      button.setBounds(590, 39, 44, 29);
      engineTab.add(button);

      JButton button_1 = new JButton("...");
      button_1.setBounds(590, 75, 44, 29);
      engineTab.add(button_1);

      JButton button_2 = new JButton("...");
      button_2.setBounds(590, 105, 44, 29);
      engineTab.add(button_2);

      JButton button_3 = new JButton("...");
      button_3.setBounds(590, 135, 44, 29);
      engineTab.add(button_3);

      JButton button_4 = new JButton("...");
      button_4.setBounds(590, 165, 44, 29);
      engineTab.add(button_4);

      JButton button_5 = new JButton("...");
      button_5.setBounds(590, 195, 44, 29);
      engineTab.add(button_5);

      JButton button_6 = new JButton("...");
      button_6.setBounds(590, 225, 44, 29);
      engineTab.add(button_6);

      JButton button_7 = new JButton("...");
      button_7.setBounds(590, 255, 44, 29);
      engineTab.add(button_7);

      JButton button_8 = new JButton("...");
      button_8.setBounds(590, 285, 44, 29);
      engineTab.add(button_8);

      JButton button_9 = new JButton("...");
      button_9.setBounds(590, 315, 44, 29);
      engineTab.add(button_9);
      {
        JPanel uiTab = new JPanel();
        tabbedPane.addTab("UI", null, uiTab, null);
      }
    }
    JTextField[] txts =
        new JTextField[] {
          txtEngine1,
          txtEngine2,
          txtEngine3,
          txtEngine4,
          txtEngine5,
          txtEngine6,
          txtEngine7,
          txtEngine8,
          txtEngine9
        };
    txtEngine.setText(Lizzie.config.leelazConfig.getString("engine-command"));
    Optional<JSONArray> enginesOpt =
        Optional.ofNullable(Lizzie.config.leelazConfig.optJSONArray("engine-command-list"));
    enginesOpt.ifPresent(
        a -> {
          IntStream.range(0, a.length())
              .forEach(
                  i -> {
                    txts[i].setText(a.getString(i));
                  });
        });
    setLocationRelativeTo(getOwner());
  }

  private String getEngineLine() {
    setVisible(false);
    String engineLine = "";
    File enginePath = null;
    File weightPath = null;
//    FileNameExtensionFilter filter = new FileNameExtensionFilter("leela zero", "*");
//    JSONObject filesystem = Lizzie.config.persisted.getJSONObject("filesystem");
    Path curPath = (new File(".")).getAbsoluteFile().toPath();
    JFileChooser chooser = new JFileChooser(".");
    //    chooser.setFileFilter(filter);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle("Please select the leela zero");
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      enginePath = chooser.getSelectedFile();
      if (enginePath != null) {
        chooser.setDialogTitle("Please select the weight file");
        String a = curPath.relativize(enginePath.toPath()).toString();
        a = "./" + a;
        result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
          weightPath = chooser.getSelectedFile();
          if (weightPath != null) {
            String b = curPath.relativize(enginePath.toPath()).toString();
            b = "./" + b;
            EngineParameter ep = new EngineParameter(enginePath.getPath(), weightPath.getPath());
            ep.setVisible(true);
          }
        }
      }
    }
    setVisible(true);
    return engineLine;
  }
}
