package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
  
  public String enginePath = "";
  public String weightPath = "";
  public String commandHelp = "";

  private Path curPath;
  private BufferedInputStream inputStream;

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
        txtEngine.setBounds(87, 40, 502, 26);
        engineTab.add(txtEngine);
        txtEngine.setColumns(10);
      }

      JLabel lblEngine1 = new JLabel("Engine 1");
      lblEngine1.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine1.setBounds(6, 80, 92, 16);
      engineTab.add(lblEngine1);

      txtEngine2 = new JTextField();
      txtEngine2.setColumns(10);
      txtEngine2.setBounds(87, 105, 502, 26);
      engineTab.add(txtEngine2);

      JLabel lblEngine2 = new JLabel("Engine 2");
      lblEngine2.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine2.setBounds(6, 110, 92, 16);
      engineTab.add(lblEngine2);

      txtEngine1 = new JTextField();
      txtEngine1.setColumns(10);
      txtEngine1.setBounds(87, 75, 502, 26);
      engineTab.add(txtEngine1);

      JLabel lblEngine3 = new JLabel("Engine 3");
      lblEngine3.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine3.setBounds(6, 140, 92, 16);
      engineTab.add(lblEngine3);

      txtEngine3 = new JTextField();
      txtEngine3.setColumns(10);
      txtEngine3.setBounds(87, 135, 502, 26);
      engineTab.add(txtEngine3);

      JLabel lblEngine4 = new JLabel("Engine 4");
      lblEngine4.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine4.setBounds(6, 170, 92, 16);
      engineTab.add(lblEngine4);

      txtEngine4 = new JTextField();
      txtEngine4.setColumns(10);
      txtEngine4.setBounds(87, 165, 502, 26);
      engineTab.add(txtEngine4);

      JLabel lblEngine5 = new JLabel("Engine 5");
      lblEngine5.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine5.setBounds(6, 200, 92, 16);
      engineTab.add(lblEngine5);

      txtEngine5 = new JTextField();
      txtEngine5.setColumns(10);
      txtEngine5.setBounds(87, 195, 502, 26);
      engineTab.add(txtEngine5);

      JLabel lblEngine6 = new JLabel("Engine 6");
      lblEngine6.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine6.setBounds(6, 230, 92, 16);
      engineTab.add(lblEngine6);

      txtEngine6 = new JTextField();
      txtEngine6.setColumns(10);
      txtEngine6.setBounds(87, 225, 502, 26);
      engineTab.add(txtEngine6);

      JLabel lblEngine7 = new JLabel("Engine 7");
      lblEngine7.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine7.setBounds(6, 260, 92, 16);
      engineTab.add(lblEngine7);

      txtEngine7 = new JTextField();
      txtEngine7.setColumns(10);
      txtEngine7.setBounds(87, 255, 502, 26);
      engineTab.add(txtEngine7);

      JLabel lblEngine8 = new JLabel("Engine 8");
      lblEngine8.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine8.setBounds(6, 290, 92, 16);
      engineTab.add(lblEngine8);

      txtEngine8 = new JTextField();
      txtEngine8.setColumns(10);
      txtEngine8.setBounds(87, 285, 502, 26);
      engineTab.add(txtEngine8);

      txtEngine9 = new JTextField();
      txtEngine9.setColumns(10);
      txtEngine9.setBounds(87, 315, 502, 26);
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
              if (!el.isEmpty()) {
                txtEngine.setText(el);
              }
              setVisible(true);
            }
          });
      button.setBounds(595, 40, 40, 26);
      engineTab.add(button);

      JButton button_1 = new JButton("...");
      button_1.setBounds(595, 75, 40, 26);
      engineTab.add(button_1);

      JButton button_2 = new JButton("...");
      button_2.setBounds(595, 105, 40, 26);
      engineTab.add(button_2);

      JButton button_3 = new JButton("...");
      button_3.setBounds(595, 135, 40, 26);
      engineTab.add(button_3);

      JButton button_4 = new JButton("...");
      button_4.setBounds(595, 165, 40, 26);
      engineTab.add(button_4);

      JButton button_5 = new JButton("...");
      button_5.setBounds(595, 195, 40, 26);
      engineTab.add(button_5);

      JButton button_6 = new JButton("...");
      button_6.setBounds(595, 225, 40, 26);
      engineTab.add(button_6);

      JButton button_7 = new JButton("...");
      button_7.setBounds(595, 255, 40, 26);
      engineTab.add(button_7);

      JButton button_8 = new JButton("...");
      button_8.setBounds(595, 285, 40, 26);
      engineTab.add(button_8);

      JButton button_9 = new JButton("...");
      button_9.setBounds(595, 315, 40, 26);
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

    curPath = (new File("")).getAbsoluteFile().toPath();
    setLocationRelativeTo(getOwner());
  }

  private String getEngineLine() {
    setVisible(false);
    String engineLine = "";
    File engineFile = null;
    File weightFile = null;
    JFileChooser chooser = new JFileChooser(".");
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle("Please select the leela zero");
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      engineFile = chooser.getSelectedFile();
      if (engineFile != null) {
        enginePath = engineFile.getAbsolutePath();
        enginePath = curPath.relativize(engineFile.toPath()).toString();
//        enginePath = "./" + enginePath;
        getCommandHelp();
        chooser.setDialogTitle("Please select the weight file");
        result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
          weightFile = chooser.getSelectedFile();
          if (weightFile != null) {
            weightPath = curPath.relativize(weightFile.toPath()).toString();
            weightPath = "./" + weightPath;
            EngineParameter ep = new EngineParameter(enginePath, weightPath, this);
            ep.setVisible(true);
            if (!ep.commandLine.isEmpty()) {
              engineLine = ep.commandLine;
            }
          }
        }
      }
    }
    return engineLine;
  }

  private String RelativizePath(Path path) {
    Path relatPath = curPath.relativize(path);
    return relatPath.toString();
  }
  
  private void getCommandHelp() {

    List<String >commands = new ArrayList<String>();
    commands.add(enginePath);
    commands.add("-h");

    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    processBuilder.directory();
    processBuilder.redirectErrorStream(true);
    try {
      Process process = processBuilder.start();
      inputStream = new BufferedInputStream(process.getInputStream());
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      executor.execute(this::read);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void read() {
    try {
      int c;
      StringBuilder line = new StringBuilder();
      while ((c = inputStream.read()) != -1) {
        line.append((char) c);
      }
      commandHelp = line.toString();
      System.out.println("Command help done."+commandHelp);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
