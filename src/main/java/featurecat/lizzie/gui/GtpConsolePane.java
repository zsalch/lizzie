package featurecat.lizzie.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class GtpConsolePane extends JDialog {
  private static final ResourceBundle resourceBundle =
      ResourceBundle.getBundle("l10n.DisplayStrings");

  // Display Comment
  private HTMLDocument htmlDoc;
  private HTMLEditorKit htmlKit;
  private StyleSheet htmlStyle;
  private JScrollPane scrollPane;
  private JTextPane console;
  private String command;
  private boolean isAnalyzeCommand = false;
  private final JTextField textField = new JTextField();

  /** Creates a Gtp Console Window */
  public GtpConsolePane(Window owner) {
    super(owner);
    setTitle("Gtp Console");

    setBounds(0, owner.getY(), owner.getX(), owner.getHeight());

    htmlKit = new HTMLEditorKit();
    htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
    htmlStyle = htmlKit.getStyleSheet();
    htmlStyle.addRule(
        "body {background:#000000; color:#d0d0d0; font-family:Consolas, Menlo, Monospace; margin: 4px; }");
    htmlStyle.addRule(".coord {color: #ffffff;}");
    //    getContentPane().setLayout(new BorderLayout(0, 0));

    console = new JTextPane();
    console.setBorder(BorderFactory.createEmptyBorder());
    console.setEditable(false);
    console.setEditorKit(htmlKit);
    console.setDocument(htmlDoc);
    // console.setBackground(Lizzie.config.commentBackgroundColor);
    // console.setForeground(Lizzie.config.commentFontColor);
    scrollPane = new JScrollPane();
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    textField.setBackground(Color.DARK_GRAY);
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    getContentPane().add(textField, BorderLayout.SOUTH);
    //    scrollPane.setVerticalScrollBarPolicy(
    //        javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setViewportView(console);
    getRootPane().setBorder(BorderFactory.createEmptyBorder());
    getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    setVisible(true);
  }

  public void addCommand(String command, int commandNumber) {
    if (command == null || command.trim().length() == 0) {
      return;
    }
    this.command = command;
    this.isAnalyzeCommand =
        command.startsWith("lz-analyze") || command.startsWith("lz-genmove_analyze");
    addText(formatCommand(command, commandNumber));
  }

  public void addLine(String line) {
    if (line == null || line.trim().length() == 0 || isAnalyzeCommand) {
      return;
    }
    addText(format(line));
  }

  private void addText(String text) {
    try {
      htmlKit.insertHTML(htmlDoc, htmlDoc.getLength(), text, 0, 0, null);
      console.setCaretPosition(htmlDoc.getLength());
    } catch (BadLocationException | IOException e) {
      e.printStackTrace();
    }
  }

  public String formatCommand(String command, int commandNumber) {
    return String.format("<b>GTP> %d %s </b><br />", commandNumber, command);
  }

  public String format(String text) {
    StringBuilder sb = new StringBuilder();
    // TODO
    text =
        text.replaceAll("\\b([a-hj-zA-HJ-Z][1-9][0-9]?)\\b", "<b class=\"coord\">$1</b>")
            .replaceAll(" (info move)", "<br />$1")
            .replaceAll("(\r\n)|(\n)", "<br />")
            .replaceAll(" ", "&nbsp;");
    sb.append("<b>   </b>").append(text);
    return sb.toString();
  }
}
