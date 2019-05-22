package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.BoardHistoryList;
import featurecat.lizzie.rules.SGFParser;
import featurecat.lizzie.util.AjaxHttpRequest;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;

public class OnlineDialog extends JDialog {
  public final ResourceBundle resourceBundle = ResourceBundle.getBundle("l10n.DisplayStrings");
  private JFormattedTextField txtRefreshTime;
  private JTextField txtUrl;
  private String ajaxUrl = "";

  public OnlineDialog() {
    setTitle(resourceBundle.getString("OnlineDialog.title.config"));
    setModalityType(ModalityType.APPLICATION_MODAL);
    setType(Type.POPUP);
    setBounds(100, 100, 414, 207);
    getContentPane().setLayout(new BorderLayout());
    JPanel buttonPane = new JPanel();
    getContentPane().add(buttonPane, BorderLayout.CENTER);
    JButton okButton = new JButton(resourceBundle.getString("OnlineDialog.button.ok"));
    okButton.setBounds(103, 138, 74, 29);
    okButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            applyChange();
          }
        });
    buttonPane.setLayout(null);
    okButton.setActionCommand("OK");
    buttonPane.add(okButton);
    getRootPane().setDefaultButton(okButton);

    JButton cancelButton = new JButton(resourceBundle.getString("OnlineDialog.button.cancel"));
    cancelButton.setBounds(231, 138, 74, 29);
    cancelButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        });
    cancelButton.setActionCommand("Cancel");
    buttonPane.add(cancelButton);

    JLabel lblUrl = new JLabel(resourceBundle.getString("OnlineDialog.title.url"));
    lblUrl.setBounds(10, 51, 56, 14);
    buttonPane.add(lblUrl);
    lblUrl.setHorizontalAlignment(SwingConstants.LEFT);

    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setGroupingUsed(false);

    txtUrl = new JTextField();
    txtUrl.setBounds(69, 48, 319, 20);
    buttonPane.add(txtUrl);
    txtUrl.setColumns(10);

    JLabel lblRefresh = new JLabel(resourceBundle.getString("OnlineDialog.title.refresh"));
    lblRefresh.setBounds(10, 82, 56, 14);
    buttonPane.add(lblRefresh);

    JLabel lblRefreshTime = new JLabel(resourceBundle.getString("OnlineDialog.title.refreshTime"));
    lblRefreshTime.setBounds(113, 79, 81, 14);
    buttonPane.add(lblRefreshTime);

    txtRefreshTime =
        new JFormattedTextField(
            new InternationalFormatter(nf) {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new DigitOnlyFilter();
            });
    txtRefreshTime.setBounds(69, 79, 36, 20);
    txtRefreshTime.setText("10");
    buttonPane.add(txtRefreshTime);
    txtRefreshTime.setColumns(10);

    JLabel lblPrompt1 = new JLabel(resourceBundle.getString("OnlineDialog.lblPrompt1.text"));
    lblPrompt1.setBounds(10, 11, 398, 14);
    buttonPane.add(lblPrompt1);

    setLocationRelativeTo(getOwner());
  }

  private void applyChange() {
    //
    String id = checkUrl();
    if (id != null && !id.isEmpty()) {
      try {
        refresh();
        setVisible(false);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private Integer txtFieldValue(JTextField txt) {
    if (txt.getText().trim().isEmpty()
        || txt.getText().trim().length() >= String.valueOf(Integer.MAX_VALUE).length()) {
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

  private String checkUrl() {
    String id = null;
    String url = txtUrl.getText().trim();
    Pattern up =
        Pattern.compile("https*://(?s).*?([^\\./]+\\.[^\\./]+)/(?s).*?(live/room/)([^/]+)/[^\\n]*");

    Matcher um = up.matcher(url);
    if (um.matches() && um.groupCount() >= 3) {
      id = um.group(3);
      if (id != null && !id.isEmpty()) {
        ajaxUrl = "https://share." + um.group(1) + "/golive/detail?id=" + id;
      }
    }
    return id;
  }

  public void refresh() throws IOException {
    Map params = new HashMap();
    int refreshTime = txtFieldValue(txtRefreshTime);
    refreshTime = (refreshTime > 0 ? refreshTime : 10);
    ScheduledExecutorService online = Executors.newScheduledThreadPool(1);
    final AjaxHttpRequest ajax = new AjaxHttpRequest();

    ajax.setReadyStateChangeListener(
        new AjaxHttpRequest.ReadyStateChangeListener() {
          public void onReadyStateChange() {
            int readyState = ajax.getReadyState();
            if (readyState == AjaxHttpRequest.STATE_COMPLETE) {
              //              System.out.println(ajax.getResponseText());
              Pattern sp =
                  Pattern.compile("(?s).*?(\\\"Content\\\":\\\")(.+)(\\\",\\\"BlackFace)(?s).*");
              Matcher sm = sp.matcher(ajax.getResponseText());
              if (sm.matches() && sm.groupCount() >= 2) {
                String sgf = sm.group(2);
                //                System.out.println(sgf);
                BoardHistoryList liveNode = SGFParser.parseSgf(sgf);
                int diffMove = Lizzie.board.getHistory().sync(liveNode);
                //                System.out.println(liveNode + "diff:" + diffMove);
                if (diffMove > 0) {
                  Lizzie.board.goToMoveNumberBeyondBranch(diffMove - 1);
                  while (Lizzie.board.nextMove()) ;
                }
              }
            }
          }
        });

    online.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              ajax.open("GET", ajaxUrl, true);
              ajax.send(params);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        },
        1,
        refreshTime,
        TimeUnit.SECONDS);
  }
}
