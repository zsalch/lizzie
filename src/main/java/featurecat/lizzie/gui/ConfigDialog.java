package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.json.JSONArray;

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
        txtEngine.setBounds(110, 39, 466, 26);
        engineTab.add(txtEngine);
        txtEngine.setColumns(10);
      }

      JLabel lblEngine1 = new JLabel("Engine 1");
      lblEngine1.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine1.setBounds(6, 80, 92, 16);
      engineTab.add(lblEngine1);

      txtEngine2 = new JTextField();
      txtEngine2.setColumns(10);
      txtEngine2.setBounds(110, 105, 466, 26);
      engineTab.add(txtEngine2);

      JLabel lblEngine2 = new JLabel("Engine 2");
      lblEngine2.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine2.setBounds(6, 110, 92, 16);
      engineTab.add(lblEngine2);

      txtEngine1 = new JTextField();
      txtEngine1.setColumns(10);
      txtEngine1.setBounds(110, 75, 466, 26);
      engineTab.add(txtEngine1);

      JLabel lblEngine3 = new JLabel("Engine 3");
      lblEngine3.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine3.setBounds(6, 140, 92, 16);
      engineTab.add(lblEngine3);

      txtEngine3 = new JTextField();
      txtEngine3.setColumns(10);
      txtEngine3.setBounds(110, 135, 466, 26);
      engineTab.add(txtEngine3);

      JLabel lblEngine4 = new JLabel("Engine 4");
      lblEngine4.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine4.setBounds(6, 170, 92, 16);
      engineTab.add(lblEngine4);

      txtEngine4 = new JTextField();
      txtEngine4.setColumns(10);
      txtEngine4.setBounds(110, 165, 466, 26);
      engineTab.add(txtEngine4);

      JLabel lblEngine5 = new JLabel("Engine 5");
      lblEngine5.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine5.setBounds(6, 200, 92, 16);
      engineTab.add(lblEngine5);

      txtEngine5 = new JTextField();
      txtEngine5.setColumns(10);
      txtEngine5.setBounds(110, 195, 466, 26);
      engineTab.add(txtEngine5);

      JLabel lblEngine6 = new JLabel("Engine 6");
      lblEngine6.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine6.setBounds(6, 230, 92, 16);
      engineTab.add(lblEngine6);

      txtEngine6 = new JTextField();
      txtEngine6.setColumns(10);
      txtEngine6.setBounds(110, 225, 466, 26);
      engineTab.add(txtEngine6);

      JLabel lblEngine7 = new JLabel("Engine 7");
      lblEngine7.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine7.setBounds(6, 260, 92, 16);
      engineTab.add(lblEngine7);

      txtEngine7 = new JTextField();
      txtEngine7.setColumns(10);
      txtEngine7.setBounds(110, 255, 466, 26);
      engineTab.add(txtEngine7);

      JLabel lblEngine8 = new JLabel("Engine 8");
      lblEngine8.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine8.setBounds(6, 290, 92, 16);
      engineTab.add(lblEngine8);

      txtEngine8 = new JTextField();
      txtEngine8.setColumns(10);
      txtEngine8.setBounds(110, 285, 466, 26);
      engineTab.add(txtEngine8);

      txtEngine9 = new JTextField();
      txtEngine9.setColumns(10);
      txtEngine9.setBounds(110, 318, 466, 26);
      engineTab.add(txtEngine9);

      JLabel lblEngine9 = new JLabel("Engine 9");
      lblEngine9.setHorizontalAlignment(SwingConstants.LEFT);
      lblEngine9.setBounds(6, 320, 92, 16);
      engineTab.add(lblEngine9);
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
}
