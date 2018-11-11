package featurecat.lizzie.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class EngineParameter extends JDialog {

  public String enginePath;
  public String weightPath;
  public String parameters;

  private final JPanel contentPanel = new JPanel();
  private JTextField textField;
  private JTextField textField_1;

  /** Create the dialog. */
  public EngineParameter(String enginePath, String weightPath) {
    setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
    setModal(true);
    setType(Type.POPUP);
    setModalityType(ModalityType.APPLICATION_MODAL);
    setBounds(100, 100, 544, 409);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(null);
    {
      JLabel lblNewLabel = new JLabel("Engine Command:");
      lblNewLabel.setBounds(6, 17, 114, 16);
      contentPanel.add(lblNewLabel);
    }
    {
      textField = new JTextField();
      textField.setEditable(false);
      textField.setBounds(123, 12, 415, 26);
      textField.setText(enginePath + " --weights " + weightPath);
      contentPanel.add(textField);
      textField.setColumns(10);
    }
    {
      JLabel lblParameter = new JLabel("Parameter:");
      lblParameter.setBounds(6, 45, 114, 16);
      contentPanel.add(lblParameter);
    }
    {
      textField_1 = new JTextField();
      textField_1.setColumns(10);
      textField_1.setBounds(123, 40, 415, 26);
      contentPanel.add(textField_1);
    }

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(6, 83, 532, 259);
    contentPanel.add(scrollPane);

    JTextPane txtParams = new JTextPane();
    txtParams.setText("wer");
    txtParams.setEditable(true);
    scrollPane.setViewportView(txtParams);
    scrollPane.setVerticalScrollBarPolicy(
        javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
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
  }
}
