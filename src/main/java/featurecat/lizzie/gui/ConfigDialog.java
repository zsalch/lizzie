package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.InternationalFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigDialog extends JDialog {
  public final ResourceBundle resourceBundle = ResourceBundle.getBundle("l10n.DisplayStrings");

  public String enginePath = "";
  public String weightPath = "";
  public String commandHelp = "";

  private String osName;
  private Path curPath;
  private BufferedInputStream inputStream;
  private JSONObject leelazConfig;

  // Engine Tab
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
  private JTextField[] txts;
  private JFormattedTextField txtMaxAnalyzeTime;
  private JFormattedTextField txtMaxGameThinkingTime;
  private JFormattedTextField txtAnalyzeUpdateInterval;
  private JCheckBox chkPrintEngineLog;

  // UI Tab
  private JTextField txtBoardSize;
  private JRadioButton rdoBoardSizeOther;
  private JRadioButton rdoBoardSize19;
  private JRadioButton rdoBoardSize13;
  private JRadioButton rdoBoardSize9;
  private JRadioButton rdoBoardSize7;
  private JRadioButton rdoBoardSize5;
  private JRadioButton rdoBoardSize4;
  private JFormattedTextField txtMinPlayoutRatioForStats;
  private JCheckBox chkShowCoordinates;
  private JRadioButton rdoShowMoveNumberNo;
  private JRadioButton rdoShowMoveNumberAll;
  private JRadioButton rdoShowMoveNumberLast;
  private JTextField txtShowMoveNumber;
  private JCheckBox chkShowBlunderBar;
  private JCheckBox chkDynamicWinrateGraphWidth;
  private JCheckBox chkAppendWinrateToComment;
  private JCheckBox chkColorByWinrateInsteadOfVisits;
  private JSlider sldBoardPositionProportion;
  private JSpinner spnWinrateStrokeWidth;
  private JSpinner spnMinimumBlunderBarWidth;
  private JSpinner spnShadowSize;
  private JComboBox cmbFontName;
  private JComboBox cmbUiFontName;
  private JComboBox cmbWinrateFontName;

  // Theme Tab
  private JComboBox cmbThemes;

  public ConfigDialog() {
    setTitle(resourceBundle.getString("LizzieConfig.title.config"));
    setModalityType(ModalityType.APPLICATION_MODAL);
    setType(Type.POPUP);
    setBounds(100, 100, 661, 567);
    getContentPane().setLayout(new BorderLayout());
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    JButton okButton = new JButton(resourceBundle.getString("LizzieConfig.button.ok"));
    okButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
            saveConfig();
            applyChange();
          }
        });
    okButton.setActionCommand("OK");
    buttonPane.add(okButton);
    getRootPane().setDefaultButton(okButton);
    JButton cancelButton = new JButton(resourceBundle.getString("LizzieConfig.button.cancel"));
    cancelButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        });
    cancelButton.setActionCommand("Cancel");
    buttonPane.add(cancelButton);
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    JPanel engineTab = new JPanel();
    tabbedPane.addTab(resourceBundle.getString("LizzieConfig.title.engine"), null, engineTab, null);
    engineTab.setLayout(null);

    JLabel lblEngine = new JLabel(resourceBundle.getString("LizzieConfig.title.engine"));
    lblEngine.setBounds(6, 44, 92, 16);
    lblEngine.setHorizontalAlignment(SwingConstants.LEFT);
    engineTab.add(lblEngine);

    txtEngine = new JTextField();
    txtEngine.setBounds(87, 40, 502, 26);
    engineTab.add(txtEngine);
    txtEngine.setColumns(10);

    JLabel lblEngine1 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 1");
    lblEngine1.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine1.setBounds(6, 80, 92, 16);
    engineTab.add(lblEngine1);

    txtEngine2 = new JTextField();
    txtEngine2.setColumns(10);
    txtEngine2.setBounds(87, 105, 502, 26);
    engineTab.add(txtEngine2);

    JLabel lblEngine2 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 2");
    lblEngine2.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine2.setBounds(6, 110, 92, 16);
    engineTab.add(lblEngine2);

    txtEngine1 = new JTextField();
    txtEngine1.setColumns(10);
    txtEngine1.setBounds(87, 75, 502, 26);
    engineTab.add(txtEngine1);

    JLabel lblEngine3 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 3");
    lblEngine3.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine3.setBounds(6, 140, 92, 16);
    engineTab.add(lblEngine3);

    txtEngine3 = new JTextField();
    txtEngine3.setColumns(10);
    txtEngine3.setBounds(87, 135, 502, 26);
    engineTab.add(txtEngine3);

    JLabel lblEngine4 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 4");
    lblEngine4.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine4.setBounds(6, 170, 92, 16);
    engineTab.add(lblEngine4);

    txtEngine4 = new JTextField();
    txtEngine4.setColumns(10);
    txtEngine4.setBounds(87, 165, 502, 26);
    engineTab.add(txtEngine4);

    JLabel lblEngine5 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 5");
    lblEngine5.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine5.setBounds(6, 200, 92, 16);
    engineTab.add(lblEngine5);

    txtEngine5 = new JTextField();
    txtEngine5.setColumns(10);
    txtEngine5.setBounds(87, 195, 502, 26);
    engineTab.add(txtEngine5);

    JLabel lblEngine6 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 6");
    lblEngine6.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine6.setBounds(6, 230, 92, 16);
    engineTab.add(lblEngine6);

    txtEngine6 = new JTextField();
    txtEngine6.setColumns(10);
    txtEngine6.setBounds(87, 225, 502, 26);
    engineTab.add(txtEngine6);

    JLabel lblEngine7 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 7");
    lblEngine7.setHorizontalAlignment(SwingConstants.LEFT);
    lblEngine7.setBounds(6, 260, 92, 16);
    engineTab.add(lblEngine7);

    txtEngine7 = new JTextField();
    txtEngine7.setColumns(10);
    txtEngine7.setBounds(87, 255, 502, 26);
    engineTab.add(txtEngine7);

    JLabel lblEngine8 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 8");
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

    JLabel lblEngine9 = new JLabel(resourceBundle.getString("LizzieConfig.title.engine") + " 9");
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
    button_1.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine1.setText(el);
            }
            setVisible(true);
          }
        });
    button_1.setBounds(595, 75, 40, 26);
    engineTab.add(button_1);

    JButton button_2 = new JButton("...");
    button_2.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine2.setText(el);
            }
            setVisible(true);
          }
        });
    button_2.setBounds(595, 105, 40, 26);
    engineTab.add(button_2);

    JButton button_3 = new JButton("...");
    button_3.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine3.setText(el);
            }
            setVisible(true);
          }
        });
    button_3.setBounds(595, 135, 40, 26);
    engineTab.add(button_3);

    JButton button_4 = new JButton("...");
    button_4.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine4.setText(el);
            }
            setVisible(true);
          }
        });
    button_4.setBounds(595, 165, 40, 26);
    engineTab.add(button_4);

    JButton button_5 = new JButton("...");
    button_5.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine5.setText(el);
            }
            setVisible(true);
          }
        });
    button_5.setBounds(595, 195, 40, 26);
    engineTab.add(button_5);

    JButton button_6 = new JButton("...");
    button_6.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine6.setText(el);
            }
            setVisible(true);
          }
        });
    button_6.setBounds(595, 225, 40, 26);
    engineTab.add(button_6);

    JButton button_7 = new JButton("...");
    button_7.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine7.setText(el);
            }
            setVisible(true);
          }
        });
    button_7.setBounds(595, 255, 40, 26);
    engineTab.add(button_7);

    JButton button_8 = new JButton("...");
    button_8.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine8.setText(el);
            }
            setVisible(true);
          }
        });
    button_8.setBounds(595, 285, 40, 26);
    engineTab.add(button_8);

    JButton button_9 = new JButton("...");
    button_9.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String el = getEngineLine();
            if (!el.isEmpty()) {
              txtEngine9.setText(el);
            }
            setVisible(true);
          }
        });
    button_9.setBounds(595, 315, 40, 26);
    engineTab.add(button_9);

    JLabel lblMaxAnalyzeTime =
        new JLabel(resourceBundle.getString("LizzieConfig.title.maxAnalyzeTime"));
    lblMaxAnalyzeTime.setBounds(6, 370, 157, 16);
    engineTab.add(lblMaxAnalyzeTime);

    JLabel lblMaxAnalyzeTimeMinutes =
        new JLabel(resourceBundle.getString("LizzieConfig.title.minutes"));
    lblMaxAnalyzeTimeMinutes.setBounds(213, 370, 82, 16);
    engineTab.add(lblMaxAnalyzeTimeMinutes);

    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setGroupingUsed(false);

    txtMaxAnalyzeTime =
        new JFormattedTextField(
            new InternationalFormatter(nf) {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new DigitOnlyFilter();
            });
    txtMaxAnalyzeTime.setBounds(171, 365, 40, 26);
    engineTab.add(txtMaxAnalyzeTime);
    txtMaxAnalyzeTime.setColumns(10);

    JLabel lblMaxGameThinkingTime =
        new JLabel(resourceBundle.getString("LizzieConfig.title.maxGameThinkingTime"));
    lblMaxGameThinkingTime.setBounds(6, 400, 157, 16);
    engineTab.add(lblMaxGameThinkingTime);

    JLabel lblMaxGameThinkingTimeSeconds =
        new JLabel(resourceBundle.getString("LizzieConfig.title.seconds"));
    lblMaxGameThinkingTimeSeconds.setBounds(213, 400, 82, 16);
    engineTab.add(lblMaxGameThinkingTimeSeconds);

    txtMaxGameThinkingTime =
        new JFormattedTextField(
            new InternationalFormatter(nf) {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new DigitOnlyFilter();
            });
    txtMaxGameThinkingTime.setColumns(10);
    txtMaxGameThinkingTime.setBounds(171, 395, 40, 26);
    engineTab.add(txtMaxGameThinkingTime);

    JLabel lblAnalyzeUpdateInterval =
        new JLabel(resourceBundle.getString("LizzieConfig.title.analyzeUpdateInterval"));
    lblAnalyzeUpdateInterval.setBounds(331, 368, 157, 16);
    engineTab.add(lblAnalyzeUpdateInterval);

    JLabel lblAnalyzeUpdateIntervalCentisec =
        new JLabel(resourceBundle.getString("LizzieConfig.title.centisecond"));
    lblAnalyzeUpdateIntervalCentisec.setBounds(538, 368, 82, 16);
    engineTab.add(lblAnalyzeUpdateIntervalCentisec);

    txtAnalyzeUpdateInterval =
        new JFormattedTextField(
            new InternationalFormatter(nf) {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new DigitOnlyFilter();
            });
    txtAnalyzeUpdateInterval.setColumns(10);
    txtAnalyzeUpdateInterval.setBounds(496, 363, 40, 26);
    engineTab.add(txtAnalyzeUpdateInterval);

    JLabel lblPrintEngineLog =
        new JLabel(resourceBundle.getString("LizzieConfig.title.printEngineLog"));
    lblPrintEngineLog.setBounds(6, 430, 157, 16);
    engineTab.add(lblPrintEngineLog);

    chkPrintEngineLog = new JCheckBox("");
    chkPrintEngineLog.setBounds(167, 425, 201, 23);
    engineTab.add(chkPrintEngineLog);

    JPanel uiTab = new JPanel();
    tabbedPane.addTab(resourceBundle.getString("LizzieConfig.title.ui"), null, uiTab, null);
    uiTab.setLayout(null);

    JLabel lblBoardSize = new JLabel(resourceBundle.getString("LizzieConfig.title.boardSize"));
    lblBoardSize.setBounds(6, 6, 67, 16);
    lblBoardSize.setHorizontalAlignment(SwingConstants.LEFT);
    uiTab.add(lblBoardSize);

    rdoBoardSize19 = new JRadioButton("19x19");
    rdoBoardSize19.setBounds(85, 2, 84, 23);
    uiTab.add(rdoBoardSize19);

    rdoBoardSize13 = new JRadioButton("13x13");
    rdoBoardSize13.setBounds(170, 2, 84, 23);
    uiTab.add(rdoBoardSize13);

    rdoBoardSize9 = new JRadioButton("9x9");
    rdoBoardSize9.setBounds(255, 2, 57, 23);
    uiTab.add(rdoBoardSize9);

    rdoBoardSize7 = new JRadioButton("7x7");
    rdoBoardSize7.setBounds(325, 2, 67, 23);
    uiTab.add(rdoBoardSize7);

    rdoBoardSize5 = new JRadioButton("5x5");
    rdoBoardSize5.setBounds(395, 2, 67, 23);
    uiTab.add(rdoBoardSize5);

    rdoBoardSize4 = new JRadioButton("4x4");
    rdoBoardSize4.setBounds(460, 2, 67, 23);
    uiTab.add(rdoBoardSize4);

    rdoBoardSizeOther = new JRadioButton("");
    rdoBoardSizeOther.addChangeListener(
        new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (rdoBoardSizeOther.isSelected()) {
              txtBoardSize.setEnabled(true);
            } else {
              txtBoardSize.setEnabled(false);
            }
          }
        });
    rdoBoardSizeOther.setBounds(530, 2, 29, 23);
    uiTab.add(rdoBoardSizeOther);

    ButtonGroup group = new ButtonGroup();
    group.add(rdoBoardSize19);
    group.add(rdoBoardSize13);
    group.add(rdoBoardSize9);
    group.add(rdoBoardSize7);
    group.add(rdoBoardSize5);
    group.add(rdoBoardSize4);
    group.add(rdoBoardSizeOther);

    txtBoardSize =
        new JFormattedTextField(
            new InternationalFormatter(nf) {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new DigitOnlyFilter();
            });
    txtBoardSize.setBounds(564, 1, 52, 26);
    uiTab.add(txtBoardSize);
    txtBoardSize.setColumns(10);

    JLabel lblMinPlayoutRatioForStats =
        new JLabel(resourceBundle.getString("LizzieConfig.title.minPlayoutRatioForStats"));
    lblMinPlayoutRatioForStats.setBounds(6, 40, 157, 16);
    uiTab.add(lblMinPlayoutRatioForStats);
    txtMinPlayoutRatioForStats =
        new JFormattedTextField(
            new InternationalFormatter() {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new NumericFilter();
            });
    txtMinPlayoutRatioForStats.setColumns(10);
    txtMinPlayoutRatioForStats.setBounds(171, 35, 57, 26);
    uiTab.add(txtMinPlayoutRatioForStats);

    JLabel lblShowCoordinates =
        new JLabel(resourceBundle.getString("LizzieConfig.title.showCoordinates"));
    lblShowCoordinates.setBounds(6, 67, 157, 16);
    uiTab.add(lblShowCoordinates);
    chkShowCoordinates = new JCheckBox("");
    chkShowCoordinates.addChangeListener(
        new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (chkShowCoordinates.isSelected() != Lizzie.config.showCoordinates) {
              Lizzie.config.toggleCoordinates();
              Lizzie.main.invalidLayout();
            }
          }
        });
    chkShowCoordinates.setBounds(170, 64, 57, 23);
    uiTab.add(chkShowCoordinates);

    JLabel lblShowMoveNumber =
        new JLabel(resourceBundle.getString("LizzieConfig.title.showMoveNumber"));
    lblShowMoveNumber.setBounds(6, 94, 157, 16);
    uiTab.add(lblShowMoveNumber);

    rdoShowMoveNumberNo =
        new JRadioButton(resourceBundle.getString("LizzieConfig.title.showMoveNumberNo"));
    rdoShowMoveNumberNo.setBounds(170, 91, 84, 23);
    uiTab.add(rdoShowMoveNumberNo);

    rdoShowMoveNumberAll =
        new JRadioButton(resourceBundle.getString("LizzieConfig.title.showMoveNumberAll"));
    rdoShowMoveNumberAll.setBounds(261, 91, 65, 23);
    uiTab.add(rdoShowMoveNumberAll);

    rdoShowMoveNumberLast =
        new JRadioButton(resourceBundle.getString("LizzieConfig.title.showMoveNumberLast"));
    rdoShowMoveNumberLast.addChangeListener(
        new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (rdoShowMoveNumberLast.isSelected()) {
              txtShowMoveNumber.setEnabled(true);
            } else {
              txtShowMoveNumber.setEnabled(false);
            }
          }
        });
    rdoShowMoveNumberLast.setBounds(325, 91, 67, 23);
    uiTab.add(rdoShowMoveNumberLast);

    ButtonGroup showMoveGroup = new ButtonGroup();
    showMoveGroup.add(rdoShowMoveNumberNo);
    showMoveGroup.add(rdoShowMoveNumberAll);
    showMoveGroup.add(rdoShowMoveNumberLast);

    txtShowMoveNumber =
        new JFormattedTextField(
            new InternationalFormatter(nf) {
              protected DocumentFilter getDocumentFilter() {
                return filter;
              }

              private DocumentFilter filter = new DigitOnlyFilter();
            });
    txtShowMoveNumber.setBounds(395, 89, 52, 26);
    uiTab.add(txtShowMoveNumber);
    txtShowMoveNumber.setColumns(10);

    JLabel lblShowBlunderBar =
        new JLabel(resourceBundle.getString("LizzieConfig.title.showBlunderBar"));
    lblShowBlunderBar.setBounds(6, 121, 157, 16);
    uiTab.add(lblShowBlunderBar);
    chkShowBlunderBar = new JCheckBox("");
    chkShowBlunderBar.setBounds(170, 118, 57, 23);
    uiTab.add(chkShowBlunderBar);

    JLabel lblDynamicWinrateGraphWidth =
        new JLabel(resourceBundle.getString("LizzieConfig.title.dynamicWinrateGraphWidth"));
    lblDynamicWinrateGraphWidth.setBounds(6, 148, 157, 16);
    uiTab.add(lblDynamicWinrateGraphWidth);
    chkDynamicWinrateGraphWidth = new JCheckBox("");
    chkDynamicWinrateGraphWidth.setBounds(170, 145, 57, 23);
    uiTab.add(chkDynamicWinrateGraphWidth);

    JLabel lblAppendWinrateToComment =
        new JLabel(resourceBundle.getString("LizzieConfig.title.appendWinrateToComment"));
    lblAppendWinrateToComment.setBounds(6, 175, 157, 16);
    uiTab.add(lblAppendWinrateToComment);
    chkAppendWinrateToComment = new JCheckBox("");
    chkAppendWinrateToComment.setBounds(170, 172, 57, 23);
    uiTab.add(chkAppendWinrateToComment);

    JLabel lblColorByWinrateInsteadOfVisits =
        new JLabel(resourceBundle.getString("LizzieConfig.title.colorByWinrateInsteadOfVisits"));
    lblColorByWinrateInsteadOfVisits.setBounds(6, 202, 163, 16);
    uiTab.add(lblColorByWinrateInsteadOfVisits);
    chkColorByWinrateInsteadOfVisits = new JCheckBox("");
    chkColorByWinrateInsteadOfVisits.setBounds(170, 199, 57, 23);
    uiTab.add(chkColorByWinrateInsteadOfVisits);

    JLabel lblBoardPositionProportion =
        new JLabel(resourceBundle.getString("LizzieConfig.title.boardPositionProportion"));
    lblBoardPositionProportion.setBounds(6, 229, 163, 16);
    uiTab.add(lblBoardPositionProportion);
    sldBoardPositionProportion = new JSlider();
    sldBoardPositionProportion.setPaintTicks(true);
    sldBoardPositionProportion.setSnapToTicks(true);
    sldBoardPositionProportion.addChangeListener(
        new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (Lizzie.main.BoardPositionProportion != sldBoardPositionProportion.getValue()) {
              Lizzie.main.BoardPositionProportion = sldBoardPositionProportion.getValue();
              Lizzie.main.invalidLayout();
            }
          }
        });
    sldBoardPositionProportion.setValue(4);
    sldBoardPositionProportion.setMaximum(8);
    sldBoardPositionProportion.setBounds(170, 225, 200, 28);
    uiTab.add(sldBoardPositionProportion);

    // Theme Tab
    JPanel themeTab = new JPanel();
    tabbedPane.addTab(resourceBundle.getString("LizzieConfig.title.theme"), null, themeTab, null);
    themeTab.setLayout(null);

    File themeFolder = new File(Theme.pathPrefix);
    File[] themes =
        themeFolder.listFiles(
            new FileFilter() {
              public boolean accept(File f) {
                return f.isDirectory() && !".".equals(f.getName());
              }
            });
    List<String> themeList =
        Arrays.asList(themes).stream().map(t -> t.getName()).collect(Collectors.toList());
    themeList.add(0, resourceBundle.getString("LizzieConfig.title.defaultTheme"));

    JLabel lblThemes = new JLabel(resourceBundle.getString("LizzieConfig.title.theme"));
    lblThemes.setBounds(12, 11, 90, 20);
    themeTab.add(lblThemes);
    cmbThemes = new JComboBox(themeList.toArray());
    cmbThemes.setBounds(175, 11, 173, 20);
    themeTab.add(cmbThemes);

    JLabel lblWinrateStrokeWidth =
        new JLabel(resourceBundle.getString("LizzieConfig.title.winrateStrokeWidth"));
    lblWinrateStrokeWidth.setBounds(10, 44, 163, 16);
    themeTab.add(lblWinrateStrokeWidth);
    spnWinrateStrokeWidth = new JSpinner();
    spnWinrateStrokeWidth.setModel(new SpinnerNumberModel(2, 1, 10, 1));
    spnWinrateStrokeWidth.setBounds(175, 42, 41, 20);
    themeTab.add(spnWinrateStrokeWidth);

    JLabel lblMinimumBlunderBarWidth =
        new JLabel(resourceBundle.getString("LizzieConfig.title.minimumBlunderBarWidth"));
    lblMinimumBlunderBarWidth.setBounds(10, 74, 163, 16);
    themeTab.add(lblMinimumBlunderBarWidth);
    spnMinimumBlunderBarWidth = new JSpinner();
    spnMinimumBlunderBarWidth.setModel(new SpinnerNumberModel(1, 1, 10, 1));
    spnMinimumBlunderBarWidth.setBounds(175, 72, 41, 20);
    themeTab.add(spnMinimumBlunderBarWidth);

    JLabel lblShadowSize = new JLabel(resourceBundle.getString("LizzieConfig.title.shadowSize"));
    lblShadowSize.setBounds(10, 104, 163, 16);
    themeTab.add(lblShadowSize);
    spnShadowSize = new JSpinner();
    spnShadowSize.setModel(new SpinnerNumberModel(50, 1, 100, 1));
    spnShadowSize.setBounds(175, 102, 41, 20);
    themeTab.add(spnShadowSize);

    String fonts[] =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    JLabel lblFontName = new JLabel(resourceBundle.getString("LizzieConfig.title.fontName"));
    lblFontName.setBounds(10, 134, 163, 16);
    themeTab.add(lblFontName);
    cmbFontName = new JComboBox(fonts);
    cmbFontName.setMaximumRowCount(16);
    cmbFontName.setBounds(174, 133, 200, 20);
    cmbFontName.setRenderer(new FontComboBoxRenderer<String>());
    cmbFontName.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            cmbFontName.setFont(
                new Font((String) e.getItem(), Font.PLAIN, cmbFontName.getFont().getSize()));
          }
        });
    themeTab.add(cmbFontName);

    JLabel lblUiFontName = new JLabel(resourceBundle.getString("LizzieConfig.title.uiFontName"));
    lblUiFontName.setBounds(10, 164, 163, 16);
    themeTab.add(lblUiFontName);
    cmbUiFontName = new JComboBox(fonts);
    cmbUiFontName.setMaximumRowCount(16);
    cmbUiFontName.setBounds(174, 163, 200, 20);
    cmbUiFontName.setRenderer(new FontComboBoxRenderer<String>());
    cmbUiFontName.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            cmbUiFontName.setFont(
                new Font((String) e.getItem(), Font.PLAIN, cmbUiFontName.getFont().getSize()));
          }
        });
    themeTab.add(cmbUiFontName);

    JLabel lblWinrateFontName =
        new JLabel(resourceBundle.getString("LizzieConfig.title.winrateFontName"));
    lblWinrateFontName.setBounds(10, 194, 163, 16);
    themeTab.add(lblWinrateFontName);
    cmbWinrateFontName = new JComboBox(fonts);
    cmbWinrateFontName.setMaximumRowCount(16);
    cmbWinrateFontName.setBounds(174, 193, 200, 20);
    cmbWinrateFontName.setRenderer(new FontComboBoxRenderer<String>());
    cmbWinrateFontName.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            cmbWinrateFontName.setFont(
                new Font((String) e.getItem(), Font.PLAIN, cmbWinrateFontName.getFont().getSize()));
          }
        });
    themeTab.add(cmbWinrateFontName);
    
    // Engines
    txts =
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
    leelazConfig = Lizzie.config.leelazConfig;
    txtEngine.setText(leelazConfig.getString("engine-command"));
    Optional<JSONArray> enginesOpt =
        Optional.ofNullable(leelazConfig.optJSONArray("engine-command-list"));
    enginesOpt.ifPresent(
        a -> {
          IntStream.range(0, a.length())
              .forEach(
                  i -> {
                    txts[i].setText(a.getString(i));
                  });
        });
    txtMaxAnalyzeTime.setText(String.valueOf(leelazConfig.getInt("max-analyze-time-minutes")));
    txtAnalyzeUpdateInterval.setText(
        String.valueOf(leelazConfig.getInt("analyze-update-interval-centisec")));
    txtMaxGameThinkingTime.setText(
        String.valueOf(leelazConfig.getInt("max-game-thinking-time-seconds")));
    chkPrintEngineLog.setSelected(leelazConfig.getBoolean("print-comms"));
    curPath = (new File("")).getAbsoluteFile().toPath();
    osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    setBoardSize();
    txtMinPlayoutRatioForStats.setText(String.valueOf(Lizzie.config.minPlayoutRatioForStats));
    chkShowCoordinates.setSelected(Lizzie.config.showCoordinates);
    chkShowBlunderBar.setSelected(Lizzie.config.showBlunderBar);
    chkDynamicWinrateGraphWidth.setSelected(Lizzie.config.dynamicWinrateGraphWidth);
    chkAppendWinrateToComment.setSelected(Lizzie.config.appendWinrateToComment);
    chkColorByWinrateInsteadOfVisits.setSelected(Lizzie.config.colorByWinrateInsteadOfVisits);
    sldBoardPositionProportion.setValue(Lizzie.config.boardPositionProportion);
    spnWinrateStrokeWidth.setValue(Lizzie.config.uiConfig.optInt("winrate-stroke-width", 3));
    spnMinimumBlunderBarWidth.setValue(
        Lizzie.config.uiConfig.optInt("minimum-blunder-bar-width", 3));
    spnShadowSize.setValue(Lizzie.config.uiConfig.optInt("shadow-size", 100));
    cmbFontName.setSelectedItem(Lizzie.config.uiConfig.optString("font-name", null));
    cmbUiFontName.setSelectedItem(Lizzie.config.uiConfig.optString("ui-font-name", null));
    cmbWinrateFontName.setSelectedItem(Lizzie.config.uiConfig.optString("winrate-font-name", null));
    cmbThemes.setSelectedItem(Lizzie.config.uiConfig.optString("theme", ""));

    setShowMoveNumber();
    setLocationRelativeTo(getOwner());
  }

  private String getEngineLine() {
    String engineLine = "";
    File engineFile = null;
    File weightFile = null;
    JFileChooser chooser = new JFileChooser(".");
    if (isWindows()) {
      FileNameExtensionFilter filter =
          new FileNameExtensionFilter(
              resourceBundle.getString("LizzieConfig.title.engine"), "exe", "bat", "sh");
      chooser.setFileFilter(filter);
    } else {
      setVisible(false);
    }
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle(resourceBundle.getString("LizzieConfig.prompt.selectEngine"));
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      engineFile = chooser.getSelectedFile();
      if (engineFile != null) {
        enginePath = engineFile.getAbsolutePath();
        enginePath = relativizePath(engineFile.toPath());
        getCommandHelp();
        JFileChooser chooserw = new JFileChooser(".");
        chooserw.setMultiSelectionEnabled(false);
        chooserw.setDialogTitle(resourceBundle.getString("LizzieConfig.prompt.selectWeight"));
        result = chooserw.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
          weightFile = chooserw.getSelectedFile();
          if (weightFile != null) {
            weightPath = relativizePath(weightFile.toPath());
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

  private String relativizePath(Path path) {
    Path relatPath;
    if (path.startsWith(curPath)) {
      relatPath = curPath.relativize(path);
    } else {
      relatPath = path;
    }
    return relatPath.toString();
  }

  private void getCommandHelp() {

    List<String> commands = new ArrayList<String>();
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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void applyChange() {
    Lizzie.board.reopen(getBoardSize());
  }

  private Integer txtFieldIntValue(JTextField txt) {
    if (txt.getText().trim().isEmpty()) {
      return 0;
    } else {
      return Integer.parseInt(txt.getText().trim());
    }
  }

  private Double txtFieldDoubleValue(JTextField txt) {
    if (txt.getText().trim().isEmpty()) {
      return 0.0;
    } else {
      return new Double(txt.getText().trim());
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

  private class NumericFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
        throws BadLocationException {
      String newStr = string != null ? string.replaceAll("[^0-9\\.]++", "") : "";
      if (!newStr.isEmpty()) {
        fb.insertString(offset, newStr, attr);
      }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
        throws BadLocationException {
      String newStr = text != null ? text.replaceAll("[^0-9\\.]++", "") : "";
      if (!newStr.isEmpty()) {
        fb.replace(offset, length, newStr, attrs);
      }
    }
  }

  private class FontComboBoxRenderer<E> extends JLabel implements ListCellRenderer<E> {
    @Override
    public Component getListCellRendererComponent(
        JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
      final String fontName = (String) value;
      setText(fontName);
      setFont(new Font(fontName, Font.PLAIN, 12));
      return this;
    }
  }

  public boolean isWindows() {
    return osName != null && !osName.contains("darwin") && osName.contains("win");
  }

  private void setBoardSize() {
    int size = Lizzie.config.uiConfig.optInt("board-size", 19);
    txtBoardSize.setEnabled(false);
    switch (size) {
      case 19:
        rdoBoardSize19.setSelected(true);
        break;
      case 13:
        rdoBoardSize13.setSelected(true);
        break;
      case 9:
        rdoBoardSize9.setSelected(true);
        break;
      case 7:
        rdoBoardSize7.setSelected(true);
        break;
      case 5:
        rdoBoardSize5.setSelected(true);
        break;
      case 4:
        rdoBoardSize4.setSelected(true);
        break;
      default:
        txtBoardSize.setText(String.valueOf(size));
        rdoBoardSizeOther.setSelected(true);
        txtBoardSize.setEnabled(true);
        break;
    }
  }

  private int getBoardSize() {
    if (rdoBoardSize19.isSelected()) {
      return 19;
    } else if (rdoBoardSize13.isSelected()) {
      return 13;
    } else if (rdoBoardSize9.isSelected()) {
      return 9;
    } else if (rdoBoardSize7.isSelected()) {
      return 7;
    } else if (rdoBoardSize5.isSelected()) {
      return 5;
    } else if (rdoBoardSize4.isSelected()) {
      return 4;
    } else {
      int size = Integer.parseInt(txtBoardSize.getText().trim());
      if (size < 2) {
        size = 19;
      }
      return size;
    }
  }

  private void setShowMoveNumber() {
    txtShowMoveNumber.setEnabled(false);
    if (Lizzie.config.showMoveNumber) {
      if (Lizzie.config.onlyLastMoveNumber > 0) {
        rdoShowMoveNumberLast.setSelected(true);
        txtShowMoveNumber.setText(String.valueOf(Lizzie.config.onlyLastMoveNumber));
        txtShowMoveNumber.setEnabled(true);
      } else {
        rdoShowMoveNumberAll.setSelected(true);
      }
    } else {
      rdoShowMoveNumberNo.setSelected(true);
    }
  }

  private void saveConfig() {
    try {
      leelazConfig.putOpt("max-analyze-time-minutes", txtFieldIntValue(txtMaxAnalyzeTime));
      leelazConfig.putOpt(
          "analyze-update-interval-centisec", txtFieldIntValue(txtAnalyzeUpdateInterval));
      leelazConfig.putOpt(
          "max-game-thinking-time-seconds", txtFieldIntValue(txtMaxGameThinkingTime));
      leelazConfig.putOpt("print-comms", chkPrintEngineLog.isSelected());
      leelazConfig.put("engine-command", txtEngine.getText().trim());
      JSONArray engines = new JSONArray();
      Arrays.asList(txts).forEach(t -> engines.put(t.getText().trim()));
      leelazConfig.put("engine-command-list", engines);
      Lizzie.config.uiConfig.put("board-size", getBoardSize());
      Lizzie.config.minPlayoutRatioForStats = txtFieldDoubleValue(txtMinPlayoutRatioForStats);
      Lizzie.config.uiConfig.put(
          "min-playout-ratio-for-stats", Lizzie.config.minPlayoutRatioForStats);
      Lizzie.config.uiConfig.putOpt("show-coordinates", chkShowCoordinates.isSelected());
      Lizzie.config.uiConfig.put("board-size", getBoardSize());
      Lizzie.config.showMoveNumber = !rdoShowMoveNumberNo.isSelected();
      Lizzie.config.onlyLastMoveNumber =
          rdoShowMoveNumberLast.isSelected() ? txtFieldIntValue(txtShowMoveNumber) : 0;
      Lizzie.config.allowMoveNumber =
          Lizzie.config.showMoveNumber
              ? (Lizzie.config.onlyLastMoveNumber > 0 ? Lizzie.config.onlyLastMoveNumber : -1)
              : 0;
      Lizzie.config.uiConfig.put("show-move-number", Lizzie.config.showMoveNumber);
      Lizzie.config.uiConfig.put("only-last-move-number", Lizzie.config.onlyLastMoveNumber);

      Lizzie.config.showBlunderBar = chkShowBlunderBar.isSelected();
      Lizzie.config.uiConfig.putOpt("show-blunder-bar", Lizzie.config.showBlunderBar);
      Lizzie.config.dynamicWinrateGraphWidth = chkDynamicWinrateGraphWidth.isSelected();
      Lizzie.config.uiConfig.putOpt(
          "dynamic-winrate-graph-width", Lizzie.config.dynamicWinrateGraphWidth);
      Lizzie.config.appendWinrateToComment = chkAppendWinrateToComment.isSelected();
      Lizzie.config.uiConfig.putOpt(
          "append-winrate-to-comment", Lizzie.config.appendWinrateToComment);
      Lizzie.config.colorByWinrateInsteadOfVisits = chkColorByWinrateInsteadOfVisits.isSelected();
      Lizzie.config.uiConfig.putOpt(
          "color-by-winrate-instead-of-visits", Lizzie.config.colorByWinrateInsteadOfVisits);
      Lizzie.config.boardPositionProportion = sldBoardPositionProportion.getValue();
      Lizzie.config.uiConfig.putOpt(
          "board-postion-proportion", Lizzie.config.boardPositionProportion);
      Lizzie.config.uiConfig.put("winrate-stroke-width", spnWinrateStrokeWidth.getValue());
      Lizzie.config.uiConfig.put("minimum-blunder-bar-width", spnMinimumBlunderBarWidth.getValue());
      Lizzie.config.uiConfig.put("shadow-size", spnShadowSize.getValue());
      Lizzie.config.uiConfig.put("font-name", cmbFontName.getSelectedItem());
      Lizzie.config.uiConfig.put("ui-font-name", cmbUiFontName.getSelectedItem());
      Lizzie.config.uiConfig.put("winrate-font-name", cmbWinrateFontName.getSelectedItem());
      Lizzie.config.uiConfig.put("theme", cmbThemes.getSelectedItem());

      Lizzie.config.save();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
