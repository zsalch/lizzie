package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.IntStream;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.InternationalFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChangeMoveDialog extends JDialog {
  public final ResourceBundle resourceBundle = ResourceBundle.getBundle("l10n.DisplayStrings");
  private String osName;
  private JTextField[] txts;
  private JRadioButton rdoPass;
  private JRadioButton rdoSwap;

  public String enginePath = "";
  public String weightPath = "";
  public String commandHelp = "";

  private Path curPath;
  private BufferedInputStream inputStream;
  private JFormattedTextField txtMoveNumber;
  private JSONObject leelazConfig;
  private JTextField txtChangeCoord;
  private JRadioButton rdoChangeCoord;

  public ChangeMoveDialog() {
    setTitle(resourceBundle.getString("LizzieChangeMove.title.config"));
    setModalityType(ModalityType.APPLICATION_MODAL);
    setType(Type.POPUP);
    setBounds(100, 100, 385, 233);
    getContentPane().setLayout(new BorderLayout());
    JPanel buttonPane = new JPanel();
    getContentPane().add(buttonPane, BorderLayout.CENTER);
    JButton okButton = new JButton(resourceBundle.getString("LizzieChangeMove.button.ok"));
    okButton.setBounds(90, 138, 65, 23);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        applyChange();
      }
    });
    buttonPane.setLayout(null);
    okButton.setActionCommand("OK");
    buttonPane.add(okButton);
    getRootPane().setDefaultButton(okButton);

    JButton cancelButton = new JButton(resourceBundle.getString("LizzieChangeMove.button.cancel"));
    cancelButton.setBounds(207, 138, 65, 23);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    cancelButton.setActionCommand("Cancel");
    buttonPane.add(cancelButton);

    JLabel lblChangeTo = new JLabel(resourceBundle.getString("LizzieChangeMove.title.changeTo"));
    lblChangeTo.setBounds(10, 95, 74, 14);
    buttonPane.add(lblChangeTo);
    lblChangeTo.setHorizontalAlignment(SwingConstants.LEFT);

    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setGroupingUsed(false);

    txtChangeCoord = new JFormattedTextField();
    txtChangeCoord.setBounds(117, 92, 47, 20);
    buttonPane.add(txtChangeCoord);
    txtChangeCoord.setColumns(10);

    ButtonGroup group = new ButtonGroup();
    rdoChangeCoord = new JRadioButton("");
    rdoChangeCoord.setBounds(90, 91, 21, 21);
    buttonPane.add(rdoChangeCoord);
    rdoChangeCoord.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (rdoChangeCoord.isSelected()) {
          txtChangeCoord.setEnabled(true);
        } else {
          txtChangeCoord.setEnabled(false);
        }
      }
    });
    group.add(rdoChangeCoord);

    rdoSwap = new JRadioButton(resourceBundle.getString("ChangeMoveDialog.rdoSwap.text"));
    rdoSwap.setBounds(290, 91, 55, 23);
    buttonPane.add(rdoSwap);
    group.add(rdoSwap);

    rdoPass = new JRadioButton(resourceBundle.getString("ChangeMoveDialog.rdoPass.text"));
    rdoPass.setBounds(189, 91, 55, 23);
    buttonPane.add(rdoPass);
    group.add(rdoPass);

    JLabel lblMoveNumber = new JLabel(resourceBundle.getString("LizzieChangeMove.title.moveNumber"));
    lblMoveNumber.setBounds(10, 67, 74, 14);
    buttonPane.add(lblMoveNumber);

    txtMoveNumber = new JFormattedTextField(new InternationalFormatter(nf) {
      protected DocumentFilter getDocumentFilter() {
        return filter;
      }

      private DocumentFilter filter = new DigitOnlyFilter();
    });
    txtMoveNumber.setBounds(117, 64, 47, 20);
    buttonPane.add(txtMoveNumber);
    txtMoveNumber.setColumns(10);

    JLabel lblPrompt1 = new JLabel(resourceBundle.getString("ChangeMoveDialog.lblNewLabel.text"));
    lblPrompt1.setBounds(10, 11, 349, 14);
    buttonPane.add(lblPrompt1);

    JLabel lblPrompt2 = new JLabel(resourceBundle.getString("ChangeMoveDialog.lblPrompt2.text"));
    lblPrompt2.setBounds(10, 28, 349, 14);
    buttonPane.add(lblPrompt2);

    JLabel lblPrompt3 = new JLabel(resourceBundle.getString("ChangeMoveDialog.lblPrompt3.text"));
    lblPrompt3.setBounds(10, 45, 349, 14);
    buttonPane.add(lblPrompt3);

    setLocationRelativeTo(getOwner());
  }

  private void applyChange() {
    Lizzie.board.reopen(getChangeToType());
  }

  private Integer txtFieldValue(JTextField txt) {
    if (txt.getText().trim().isEmpty()) {
      return 0;
    } else {
      return Integer.parseInt(txt.getText().trim());
    }
  }

  private class DigitOnlyFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
        throws BadLocationException {
      String newStr = string != null ? string.replaceAll("\\D++", "") : "";
      if (!newStr.isEmpty()) {
        fb.insertString(offset, newStr, attr);
      }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
        throws BadLocationException {
      String newStr = text != null ? text.replaceAll("\\D++", "") : "";
      if (!newStr.isEmpty()) {
        fb.replace(offset, length, newStr, attrs);
      }
    }
  }

  public boolean isWindows() {
    return osName != null && !osName.contains("darwin") && osName.contains("win");
  }

  private int getChangeToType() {
    if (rdoPass.isSelected()) {
      return -1;
    } else if (rdoSwap.isSelected()) {
      return -2;
    } else {
      return txtFieldValue(txtChangeCoord);
    }
  }
}
