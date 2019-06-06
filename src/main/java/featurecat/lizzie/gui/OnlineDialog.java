package featurecat.lizzie.gui;

import featurecat.lizzie.Lizzie;
import featurecat.lizzie.rules.BoardData;
import featurecat.lizzie.rules.BoardHistoryList;
import featurecat.lizzie.rules.BoardHistoryNode;
import featurecat.lizzie.rules.SGFParser;
import featurecat.lizzie.rules.Stone;
import featurecat.lizzie.util.AjaxHttpRequest;
import featurecat.lizzie.util.Utils;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

public class OnlineDialog extends JDialog {
  public final ResourceBundle resourceBundle = ResourceBundle.getBundle("l10n.DisplayStrings");
  private ScheduledExecutorService online = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> schedule = null;
  private WebSocketClient client;
  private JFormattedTextField txtRefreshTime;
  int refreshTime;
  private JTextField txtUrl;
  private String ajaxUrl = "";
  private Map queryMap = null;
  private String whitePlayer = "";
  private String blackPlayer = "";
  private int seqs = 0;
  private BoardHistoryList history = null;
  private int boardSize = 19;

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

    txtUrl.selectAll();

    setLocationRelativeTo(getOwner());
  }

  private void applyChange() {
    //
    int type = checkUrl();

    if (type > 0) {
      setVisible(false);
      try {
        proc(type);
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
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

  private int checkUrl() {
    int type = 0;
    String id = null;
    String url = txtUrl.getText().trim();

    Pattern up =
        Pattern.compile("https*://(?s).*?([^\\./]+\\.[^\\./]+)/(?s).*?(live/room/)([^/]+)/[^\\n]*");
    Matcher um = up.matcher(url);
    if (um.matches() && um.groupCount() >= 3) {
      id = um.group(3);
      if (id != null && !id.isEmpty()) {
        ajaxUrl = "https://api." + um.group(1) + "/golive/dtl?id=" + id;
        return 1;
      }
    }

    up = Pattern.compile("https*://(?s).*?([^\\./]+\\.[^\\./]+)/(?s).*?(live/board/)([^/]+)");
    um = up.matcher(url);
    if (um.matches() && um.groupCount() >= 3) {
      id = um.group(3);
      if (id != null && !id.isEmpty()) {
        ajaxUrl = "https://api." + um.group(1) + "/golive/dtl?id=" + id;
        return 1;
      }
    }

    up = Pattern.compile("https*://(?s).*?([^\\./]+\\.[^\\./]+)/(?s).*?(game/play/)[0-9]+/([^/]+)");
    um = up.matcher(url);
    if (um.matches() && um.groupCount() >= 3) {
      id = um.group(3);
      if (id != null && !id.isEmpty()) {
        ajaxUrl = "https://api." + um.group(1) + "/golive/dtl?id=" + id;
        return 0;
      }
    }

    try {
      queryMap = splitQuery(new URI(url));
      System.out.println("Query:" + queryMap.toString());
      if (queryMap != null
          && queryMap.get("gameid") != null
          && queryMap.get("createtime") != null) {
        return 3;
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    return type;
  }

  private void proc(int type) throws IOException, URISyntaxException {
    refreshTime = Utils.txtFieldValue(txtRefreshTime);
    refreshTime = (refreshTime > 0 ? refreshTime : 10);
    //    if (!online.isShutdown()) {
    //      online.shutdown();
    //    }
    if (schedule != null && !schedule.isCancelled() && !schedule.isDone()) {
      schedule.cancel(false);
    }
    switch (type) {
      case 1:
        refresh("(?s).*?(\\\"Content\\\":\\\")(.+)(\\\",\\\")(?s).*");
        break;
      case 2:
        break;
      case 3:
        req();
        break;
      default:
        break;
    }
  }

  public void refresh(String format) throws IOException {
    Map params = new HashMap();
    final AjaxHttpRequest ajax = new AjaxHttpRequest();

    ajax.setReadyStateChangeListener(
        new AjaxHttpRequest.ReadyStateChangeListener() {
          public void onReadyStateChange() {
            int readyState = ajax.getReadyState();
            if (readyState == AjaxHttpRequest.STATE_COMPLETE) {
              // System.out.println(ajax.getResponseText());
              Pattern sp = Pattern.compile(format);
              Matcher sm = sp.matcher(ajax.getResponseText());
              if (sm.matches() && sm.groupCount() >= 2) {
                String sgf = sm.group(2);
                // System.out.println(sgf);
                BoardHistoryList liveNode = SGFParser.parseSgf(sgf);
                int diffMove = Lizzie.board.getHistory().sync(liveNode);
                // System.out.println(liveNode + "diff:" + diffMove);
                if (diffMove >= 0) {
                  Lizzie.board.goToMoveNumberBeyondBranch(diffMove > 0 ? diffMove - 1 : 0);
                  while (Lizzie.board.nextMove()) ;
                }
              }
            }
          }
        });

    if (schedule == null || schedule.isCancelled() || schedule.isDone()) {
      schedule =
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

  private void req() throws URISyntaxException {
    seqs = 0;
    byte[] b = {
      119, 115, 58, 47, 47, 119, 115, 46, 104, 117, 97, 110, 108, 101, 46, 113, 113, 46, 99, 111,
      109, 47, 119, 113, 98, 114, 111, 97, 100, 99, 97, 115, 116, 108, 111, 116, 117, 115
    };
    URI uri = new URI(new String(b));

    Lizzie.board.clear();
    if (client != null && client.isOpen()) {
      client.close();
    }
    client =
        new WebSocketClient(uri) {

          public void onOpen(ServerHandshake arg0) {
            System.out.println("socket open");
          }

          public void onMessage(String arg0) {
            System.out.println("socket message" + arg0);
          }

          public void onError(Exception arg0) {
            arg0.printStackTrace();
            System.out.println("socket error");
          }

          public void onClose(int arg0, String arg1, boolean arg2) {
            System.out.println("socket close");
          }

          public void onMessage(ByteBuffer bytes) {
            System.out.println("socket message ByteBuffer" + byteArrayToHexString(bytes.array()));
            parseReq(bytes);
          }
        };

    client.connect();

    while (!client.getReadyState().equals(ReadyState.OPEN)) {
      // System.out.println("socket pending");
    }
    System.out.println("socket opened");
    byte[] req1 =
        req1(
            90,
            ++seqs,
            23406,
            Utils.intOfMap(queryMap, "gameid"),
            Utils.intOfMap(queryMap, "showtype"),
            Utils.intOfMap(queryMap, "showid"),
            Utils.intOfMap(queryMap, "createtime"));
    System.out.println("socket send ByteBuffer" + byteArrayToHexString(req1));
    client.send(req1);
  }

  public byte[] req1(int len, int seq, int msgID, int gameId, int showType, int showId, int time) {
    ByteBuffer bytes = ByteBuffer.allocate(len);
    bytes.putShort((short) len);
    bytes.putShort((short) 1);
    bytes.putInt(seq);
    bytes.putShort((short) -4);
    bytes.putInt(50000);
    bytes.put((byte) 0);
    bytes.put((byte) 0);
    bytes.putShort((short) msgID);
    bytes.putShort((short) 0);
    bytes.putInt(1000);
    bytes.put((byte) 0);
    bytes.put((byte) 234);
    bytes.putShort((short) 0);
    bytes.putShort((short) 0);
    bytes.putShort((short) 60);
    bytes.putInt(0);
    bytes.putInt(gameId);
    bytes.putInt(showType);
    bytes.putInt(showId);
    bytes.putInt(0);
    bytes.putInt(-1);
    bytes.putInt(0);
    bytes.putShort((short) 0);
    bytes.putInt(3601);
    bytes.putInt(time);
    bytes.putInt(0);
    bytes.putInt(0);
    bytes.putInt(0);
    bytes.putInt(0);
    bytes.putInt(1);
    return bytes.array();
  }

  public byte[] req2(int len, int seq, int msgID, int gameId, int showType, int showId, int time) {
    ByteBuffer bytes = ByteBuffer.allocate(len);
    bytes.putShort((short) len);
    bytes.putShort((short) 1);
    bytes.putInt(seq);
    bytes.putShort((short) -4);
    bytes.putInt(50000);
    bytes.put((byte) 0);
    bytes.put((byte) 0);
    bytes.putShort((short) msgID);
    bytes.putShort((short) 0);
    bytes.putInt(1000);
    bytes.put((byte) 0);
    bytes.put((byte) 234);
    bytes.putShort((short) 0);
    bytes.putShort((short) 0);
    bytes.putShort((short) 24);
    bytes.putInt(0);
    bytes.putInt(gameId);
    bytes.putInt(showType);
    bytes.putInt(showId);
    bytes.putShort((short) 0);
    bytes.putInt(3601);
    return bytes.array();
  }

  public void parseReq(ByteBuffer res) {
    int totalLength = res.getShort();
    int ver = res.getShort();
    int seq = res.getInt();
    int dialogID = res.getShort();
    int din = res.getInt();
    int bodyFlag = res.get();
    int option = res.get();
    int msgID = res.getShort();
    System.out.println("recv msgID:" + msgID);
    if (msgID == 23406) {
      int msgType = res.getShort();
      int MsgSeq = res.getInt();
      int srcFe = res.get();
      int dstFe = res.get();
      int srcId = res.getShort();
      int dstId = res.getShort();
      int bodyLen = res.getShort();

      int resultId = res.getInt();
      int gameId = res.getInt();
      int showType = res.getInt();
      int showId = res.getInt();
      int startReq = res.getInt();
      int showFragmentNum = res.getInt();
      List<Fragment> fragmentList = new ArrayList<Fragment>();
      for (int i = 0; i < showFragmentNum; i++) {
        int len = res.getShort();
        byte[] frag = new byte[len];
        res.get(frag, res.arrayOffset(), len);
        fragmentList.add(new Fragment(len, frag));
      }

      processFrag(fragmentList);

      int isSplitPkg = res.getInt();
      int lastSeq = res.getInt();
      int curRound = res.getInt();
      int transparentLen = res.getShort();
      // TODO
      if (transparentLen > 0) {
        // Transparent
      } else {
        int transparent = res.get();
      }
      int version = res.getInt();
      int createTime = res.getInt();
      int srcType = res.getInt();

      if (schedule == null || schedule.isCancelled() || schedule.isDone()) {
        schedule =
            online.scheduleAtFixedRate(
                new Runnable() {
                  @Override
                  public void run() {
                    if (client.isOpen()) {
                      byte[] req2 =
                          req2(
                              54,
                              ++seqs,
                              23413,
                              Utils.intOfMap(queryMap, "gameid"),
                              Utils.intOfMap(queryMap, "showtype"),
                              Utils.intOfMap(queryMap, "showid"),
                              Utils.intOfMap(queryMap, "createtime"));
                      System.out.println(
                          "socket send req2 ByteBuffer" + byteArrayToHexString(req2));
                      client.send(req2);
                    }
                  }
                },
                1,
                refreshTime,
                TimeUnit.SECONDS);
      }
    } else if (msgID == 23407) {
      int msgType = res.getShort();
      int MsgSeq = res.getInt();
      int srcFe = res.get();
      int dstFe = res.get();
      int srcId = res.getShort();
      int dstId = res.getShort();
      int bodyLen = res.getShort();

      int gameId = res.getInt();
      int showType = res.getInt();
      int showId = res.getInt();
      int startReq = res.getInt();
      int showFragmentNum = res.getInt();
      List<Fragment> fragmentList = new ArrayList<Fragment>();
      for (int i = 0; i < showFragmentNum; i++) {
        int len = res.getShort();
        byte[] frag = new byte[len];
        res.get(frag, res.arrayOffset(), len);
        fragmentList.add(new Fragment(len, frag));
      }

      processFrag(fragmentList);

    } else if (msgID == 23413) {
      int msgType = res.getShort();
      int MsgSeq = res.getInt();
      int srcFe = res.get();
      int dstFe = res.get();
      int srcId = res.getShort();
      int dstId = res.getShort();
      int bodyLen = res.getShort();

      int resultId = res.getInt();
      int gameId = res.getInt();
      int showType = res.getInt();
      int showId = res.getInt();
      int online = res.getInt();
      int status = res.getInt();
      int tipsLen = res.getInt();
      for (int i = 0; i < tipsLen; i++) {
        int len = res.getShort();
        byte[] tips = new byte[len];
        res.get(tips, res.arrayOffset(), len);
        // TODO
      }
      int curRound = res.getInt();
      int transparentLen = res.getShort();
      // TODO
      if (transparentLen > 0) {
        // Transparent
      } else {
        int transparent = res.get();
      }
      int version = res.getInt();
      int createTime = res.getInt();
      int srcType = res.getInt();
    }
  }

  private void processFrag(List<Fragment> fragmentList) {

    for (Fragment f : fragmentList) {
      if (f != null) {
        System.out.println("Msg:" + f.type + ":" + (f.line != null ? f.line.toString() : ""));
        if (f.type == 20032) {
          int size = ((JSONObject) f.line.opt("AAA307")).optInt("AAA16");
          if (size > 0) {
            boardSize = size;
            history = new BoardHistoryList(BoardData.empty(size));
            JSONObject a309 = ((JSONObject) f.line.opt("AAA309"));
            blackPlayer =
                a309 == null
                    ? ""
                    : ("86".equals(a309.optString("AAA227"))
                        ? a309.optString("AAA225")
                        : a309.optString("AAA224"));
            JSONObject a308 = ((JSONObject) f.line.opt("AAA308"));
            whitePlayer =
                a308 == null
                    ? ""
                    : ("86".equals(a308.optString("AAA227"))
                        ? a308.optString("AAA225")
                        : a308.optString("AAA224"));
            Lizzie.frame.setPlayers(whitePlayer, blackPlayer);
          } else {
            break;
          }
        } else if (f.type == 7005) {
          int num = f.line.optInt("AAA102");
          Stone color = (num % 2 != 0) ? Stone.BLACK : Stone.WHITE;
          int index = f.line.optInt("AAA106");
          int[] coord = asCoord(Stone.BLACK.equals(color) ? index : index - 1024);
          boolean changeMove = false;

          if (num <= history.getMoveNumber()) {
            int cur = history.getMoveNumber();
            for (int i = num; i <= cur; i++) {
              BoardHistoryNode currentNode = history.getCurrentHistoryNode();
              if (currentNode.previous().isPresent()) {
                BoardHistoryNode pre = currentNode.previous().get();
                history.previous();
                if (pre.numberOfChildren() <= 1) {
                  int idx = pre.indexOfNode(currentNode);
                  pre.deleteChild(idx);
                  changeMove = false;
                } else {
                  changeMove = true;
                }
              }
            }
          }

          // if (coord == null) {
          // history.pass(color, newBranch, false);
          // } else {
          history.place(coord[0], coord[1], color, false, changeMove);
          // }
        } else if (f.type == 8005) {
          int num = f.line.optInt("AAA72");
          String comment = f.line.optString("AAA37");
          // TOOD Check Move Number?
          history.getData().comment += comment + "\n";
        } else if (f.type == 8185) {
          JSONObject branch = (JSONObject) f.line.opt("AAA79");
          if (branch != null) {
            int moveNum = branch.optInt("AAA20") - 1;
            if (moveNum > 0) {
              history.goToMoveNumber(moveNum, false);
              String branchCmt = branch.optString("AAA283");
              JSONArray branchMoves = branch.optJSONArray("AAA106");
              if (branchMoves != null && branchMoves.length() > 0) {
                if (history.getCurrentHistoryNode().numberOfChildren() == 0) {
                  //                  BoardData data = BoardData.empty(boardSize);
                  //                  data.moveMNNumber = history.getData().moveMNNumber + 1;
                  //                  data.moveNumber = history.getData().moveNumber + 1;
                  //                  history.getCurrentHistoryNode().addOrGoto(data);
                  Stone color =
                      history.getLastMoveColor() == Stone.WHITE ? Stone.BLACK : Stone.WHITE;
                  history.pass(color, false, true);
                  history.previous();
                }
                for (int i = 0; i < branchMoves.length(); i++) {
                  Stone color =
                      history.getLastMoveColor() == Stone.WHITE ? Stone.BLACK : Stone.WHITE;
                  int index = branchMoves.getInt(i);
                  int[] coord = asCoord(Stone.BLACK.equals(color) ? index : index - 1024);
                  history.place(coord[0], coord[1], color, i == 0);
                  if (i == 0) {
                    history.getData().comment += branchCmt + "\n";
                  }
                }
                history.toBranchTop();
                while (history.next(true).isPresent()) ;
              }
            }
          }
        }
      }
    }

    while (history.previous().isPresent()) ;
    int diffMove = Lizzie.board.getHistory().sync(history);
    System.out.println("Diff Move:" + diffMove);
    if (diffMove >= 0) {
      Lizzie.board.goToMoveNumberBeyondBranch(diffMove > 0 ? diffMove - 1 : 0);
      while (Lizzie.board.nextMove()) {
        System.out.println("Diff Move NextMove");
      }
    }
    while (history.next(true).isPresent()) ;
  }

  private int[] asCoord(int index) {
    int[] coord = new int[2];
    if (index >= 1024) {
      int i = index - 1024;
      coord[0] = i % 32;
      coord[1] = i / 32;
    }
    return coord;
  }

  private class Fragment {
    private int len;
    private byte[] frag;
    public long type;
    public JSONObject line;

    public Fragment(int len, byte[] frag) {
      this.len = len;
      this.frag = frag;
      Proto o = parseProto(frag);
      // System.out.println("type:" + o.type);
      // System.out.println("raw:" + byteArrayToHexString(o.raw));
      this.type = o.type;
      if (o.type == 20032) {
        line = decode52(ByteBuffer.wrap(o.raw));
      } else if (o.type == 7005) {
        line = decode20(ByteBuffer.wrap(o.raw));
      } else if (o.type == 8005) {
        line = decode7(ByteBuffer.wrap(o.raw));
      } else if (o.type == 7025) {
        // TODO AA23
        o.type = 0;
      } else if (o.type == 8185) {
        line = decode17(ByteBuffer.wrap(o.raw));
      }
    }

    private JSONObject decode52(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long t = uint32(buf);
        t = t >>> 3;
        if (t == 1) {
          m.put("AAA311", uint32(buf));
        } else if (t == 2) {
          m.put("AAA303", uint64(buf));
        } else if (t == 3) {
          m.put("AAA312", uint32(buf));
        } else if (t == 4) {
          m.put("AAA305", uint64(buf));
        } else if (t == 5) {
          m.put("AAA306", uint64(buf));
        } else if (t == 6) {
          int len = (int) uint32(buf);
          byte[] newB = new byte[len];
          buf.get(newB, 0, len);
          m.put("AAA307", decode1(ByteBuffer.wrap(newB)));
        } else if (t == 7) {
          int len = (int) uint32(buf);
          byte[] newB = new byte[len];
          buf.get(newB, 0, len);
          m.put("AAA308", decode48(ByteBuffer.wrap(newB)));
        } else if (t == 8) {
          int len = (int) uint32(buf);
          byte[] newB = new byte[len];
          buf.get(newB, 0, len);
          m.put("AAA309", decode48(ByteBuffer.wrap(newB)));
        } else if (t == 9) {
          m.put("AAA310", uint64(buf));
        } else {
          // TODO
          break;
          // skipType(buf, (int) (t & 7));
        }
      }
      return m;
    }

    private JSONObject decode1(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long tl = uint32(buf);
        int t = (int) (tl >>> 3);
        switch (t) {
          case 1:
            m.put("AAA1", uint64(buf));
            break;
          case 2:
            m.put("AAA2", uint64(buf));
            break;
          case 3:
            m.put("AAA3", uint32(buf));
            break;
          case 4:
            m.put("AAA4", uint32(buf));
            break;
          case 5:
            m.put("AAA5", uint32(buf));
            break;
          case 6:
            m.put("AAA6", uint32(buf));
            break;
          case 7:
            m.put("AAA7", uint32(buf));
            break;
          case 8:
            m.put("AAA8", uint32(buf));
            break;
          case 9:
            m.put("AAA9", uint32(buf));
            break;
          case 10:
            m.put("AAA10", uint32(buf));
            break;
          case 11:
            m.put("AAA11", uint32(buf));
            break;
          case 12:
            m.put("AAA12", uint32(buf));
            break;
          case 13:
            m.put("AAA13", uint32(buf));
            break;
          case 14:
            m.put("AAA14", uint32(buf));
            break;
          case 15:
            m.put("AAA15", uint32(buf));
            break;
          case 16:
            m.put("AAA16", uint32(buf));
            break;
          case 17:
            m.put("AAA17", uint32(buf));
            break;
          default:
            // skipType(buf, (int) (t & 7));
            break;
        }
      }
      return m;
    }

    private JSONObject decode48(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long tl = uint32(buf);
        int t = (int) (tl >>> 3);
        switch (t) {
          case 1:
            m.put("AAA224", readString(buf));
            break;
          case 2:
            m.put("AAA225", readString(buf));
            break;
          case 3:
            m.put("AAA226", uint32(buf));
            break;
          case 4:
            m.put("AAA227", uint32(buf));
            break;
          case 5:
            m.put("AAA228", uint32(buf));
            break;
          case 6:
            m.put("AAA234", uint32(buf));
            break;
          case 7:
            m.put("AAA248", uint32(buf));
            break;
          case 8:
            m.put("AAA249", uint32(buf));
            break;
          case 9:
            m.put("AAA250", uint64(buf));
            break;
          case 10:
            m.put("AAA251", readString(buf));
            break;
          default:
            // skipType(buf, (int) (t & 7));
            break;
        }
      }
      return m;
    }

    private JSONObject decode20(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long tl = uint32(buf);
        int t = (int) (tl >>> 3);
        switch (t) {
          case 1:
            m.put("AAA311", uint32(buf));
            break;
          case 2:
            m.put("AAA303", uint64(buf));
            break;
          case 3:
            m.put("AAA312", uint32(buf));
            break;
          case 4:
            m.put("AAA106", uint32(buf));
            break;
          case 5:
            m.put("AAA168", uint32(buf));
            break;
          case 6:
            m.put("AAA158", uint32(buf));
            break;
          case 7:
            m.put("AAA109", uint32(buf));
            break;
          case 8:
            m.put("AAA102", uint32(buf));
            break;
          default:
            // r.skipType(t&7)
            // break;
            return m;
        }
      }
      return m;
    }

    private JSONObject decode7(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long tl = uint32(buf);
        int t = (int) (tl >>> 3);
        switch (t) {
          case 1:
            m.put("AAA311", uint32(buf));
            break;
          case 2:
            m.put("AAA303", uint64(buf));
            break;
          case 3:
            m.put("AAA312", uint32(buf));
            break;
          case 4:
            m.put("AAA72", uint32(buf));
            break;
          case 5:
            m.put("AAA37", readString(buf));
            break;
          case 6:
            m.put("AAA38", uint32(buf));
            break;
          default:
            // r.skipType(t&7)
            // break;
            return m;
        }
      }
      return m;
    }

    private JSONObject decode17(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long tl = uint32(buf);
        int t = (int) (tl >>> 3);
        switch (t) {
          case 1:
            m.put("AAA311", uint32(buf));
            break;
          case 2:
            m.put("AAA303", uint64(buf));
            break;
          case 3:
            m.put("AAA312", uint32(buf));
            break;
          case 4:
            m.put("AAA77", uint32(buf));
            break;
          case 5:
            m.put("AAA78", uint32(buf));
            break;
          case 6:
            int len = (int) uint32(buf);
            byte[] newB = new byte[len];
            buf.get(newB, 0, len);
            m.put("AAA79", decode2(ByteBuffer.wrap(newB)));
            break;
          case 7:
            // TODO AAA80
            break;
          default:
            // r.skipType(t&7)
            // break;
            return m;
        }
      }
      return m;
    }

    private JSONObject decode2(ByteBuffer buf) {
      JSONObject m = new JSONObject();
      while (buf.position() < buf.array().length) {
        long tl = uint32(buf);
        int t = (int) (tl >>> 3);
        switch (t) {
          case 1:
            if (m.optJSONArray("AAA106") == null) {
              m.put("AAA106", new JSONArray("[]"));
            }
            m.getJSONArray("AAA106").put(uint32(buf));
            break;
          case 2:
            m.put("AAA19", uint32(buf));
            break;
          case 3:
            m.put("AAA20", uint32(buf));
            break;
          case 4:
            m.put("AAA283", readString(buf));
            break;
          case 5:
            if (m.optJSONArray("AAA37") == null) {
              m.put("AAA37", new JSONArray("[]"));
            }
            m.getJSONArray("AAA37").put(readString(buf));
            break;
          default:
            // r.skipType(t&7)
            // break;
            return m;
        }
      }
      return m;
    }

    private void skip(ByteBuffer buf, int e) {
      if (e > 0) {
        if ((buf.position() + e) > buf.array().length) return;
        buf.position(buf.position() + e);
      } else
        do {
          if (buf.position() > buf.array().length) return;
        } while ((128 & buf.get()) != 0);
    }

    private ByteBuffer skipType(ByteBuffer buf, int e) {
      switch (e) {
        case 0:
          skip(buf, 0);
        case 1:
          skip(buf, 8);
        case 2:
          skip(buf, (int) uint32(buf));
        case 3:
          for (; ; ) {
            e = (int) (7 & uint32(buf));
            if (4 == e) break;
            skipType(buf, e);
          }
          break;
        case 5:
          skip(buf, 4);
        default:
          // TODO Error
      }
      return buf;
    }

    private long uint32(ByteBuffer buf) {
      long i = 0;
      long b = buf.get() & 0xFF;
      i = (127 & b) >>> 0;
      if (b < 128) return i;
      b = buf.get();
      i = (i | (127 & b) << 7) >>> 0;
      if (b < 128) return i;
      b = buf.get();
      i = (i | (127 & b) << 14) >>> 0;
      if (b < 128) return i;
      b = buf.get();
      i = (i | (127 & b) << 21) >>> 0;
      if (b < 128) return i;
      b = buf.get();
      i = (i | (15 & b) << 28) >>> 0;
      if (b < 128) return i;
      b = buf.get();
      // TODO
      return i;
    }

    private long uint64(ByteBuffer buf) {
      Uint64 e = u(buf);
      if (e != null && ((e.hi >>> 31) != 0)) {
        long t = 1 + ~e.lo >>> 0;
        long o = ~e.hi >>> 0;
        if (t == 0) {
          o = o + 1 >>> 0;
          return -(t + 4294967296L * o);
        }
        return t;
      }
      return e.lo + 4294967296L * e.hi;
    }

    private Uint64 u(ByteBuffer buf) {
      int t = 0;
      Uint64 e = new Uint64();
      long b = 0;
      if (!(buf.array().length - buf.position() > 4)) {
        for (; t < 3; ++t) {
          if (buf.position() >= buf.array().length) return e;
          b = buf.get() & 0xFF;
          e.lo = (e.lo | (127 & b) << 7 * t) >>> 0;
          if (b < 128) return e;
        }
        b = buf.get() & 0xFF;
        e.lo = (e.lo | (127 & b) << 7 * t) >>> 0;
        return e;
      }
      for (; t < 4; ++t) {
        b = buf.get() & 0xFF;
        e.lo = (e.lo | (127 & b) << 7 * t) >>> 0;
        if (b < 128) return e;
      }
      b = buf.get() & 0xFF;
      e.lo = (e.lo | (127 & b) << 28) >>> 0;
      e.hi = (e.hi | (127 & b) >> 4) >>> 0;
      if (b < 128) return e;
      t = 0;
      if (buf.array().length - buf.position() > 4) {
        for (; t < 5; ++t) {
          b = buf.get() & 0xFF;
          e.hi = (e.hi | (127 & b) << 7 * t + 3) >>> 0;
          if (b < 128) return e;
        }
      } else
        for (; t < 5; ++t) {
          if (buf.position() >= buf.array().length) break;
          b = buf.get() & 0xFF;
          e.hi = (e.hi | (127 & b) << 7 * t + 3) >>> 0;
          if (b < 128) return e;
        }
      // TODO Error
      return e;
    }

    private class Uint64 {
      public long lo = 0;
      public long hi = 0;
    }

    private byte[] bytes(ByteBuffer buf) {
      long e = uint32(buf);
      long t = buf.position();
      long o = t + e;
      if (o > buf.array().length) return null;
      byte[] b = new byte[(int) e];
      for (int i = 0; i < e; i++) {
        b[i] = buf.get();
      }
      return b;
    }

    private String readString(ByteBuffer buf) {
      byte[] e = bytes(buf);
      if (e == null || e.length <= 0) return "";
      List<Long> s = new ArrayList();
      StringBuilder i = new StringBuilder();
      int t = 0;
      int o = e.length;
      int n = 0;
      long r;
      for (; t < o; ) {
        r = e[t++] & 0xFF;
        if (r < 128) {
          s.add(r);
        } else if (r > 191 && r < 224) {
          s.add((31 & r) << 6 | 63 & (e[t++] & 0xFF));
        } else if (r > 239 && r < 365) {
          r =
              ((7 & r) << 18
                      | (63 & (e[t++] & 0xFF)) << 12
                      | (63 & (e[t++] & 0xFF)) << 6
                      | 63 & (e[t++] & 0xFF))
                  - 65536;
          s.add(55296 + (r >> 10));
          s.add(56320 + (1023 & r));
        } else {
          s.add((15 & r) << 12 | (63 & (e[t++] & 0xFF)) << 6 | 63 & (e[t++] & 0xFF));
          // n > 8191;
          for (long l : s) {
            String str = fromCharCode((int) l);
            i.append(str);
          }
          n = 0;
          s = new ArrayList();
        }
      }

      if (i.length() == 0 || s.size() > 0) {
        for (long l : s) {
          String str = fromCharCode((int) l);
          i.append(str);
        }
      }

      return i.toString();
    }

    private Proto parseProto(byte[] e) {
      int o = e.length;
      if (o <= 2) return null;
      int r = 0;
      long i = (long) (256 * (e[r] & 0xFF)) + (long) (e[r + 1] & 0xFF);
      r += 2;
      o -= 2;
      long s = 0;
      if (32768 == i) {
        if (o <= 4) return null;
        s =
            (long) (256 * (e[r] & 0xFF) * 256 * 256)
                + (long) (256 * (e[r + 1] & 0xFF) * 256)
                + (long) (256 * (e[r + 2] & 0xFF))
                + (long) ((e[r + 3] & 0xFF));
        r += 4;
        o -= 4;
      } else {
        s = i;
      }
      if (s < 7 || Integer.compareUnsigned((int) s, 0x80000000) >= 0) return null;
      if (o < s) return null;
      long n = (long) (256 * (e[r] & 0xFF)) + (long) (e[r + 1] & 0xFF);
      o -= 2;
      r += 2; // TODO
      o -= 1;
      byte[] a = new byte[e.length - 4 - r - 1];
      for (int p = ++r; p < e.length - 4; p++) a[p - r] = e[p];
      return new Proto(n, a);
    }
  }

  private class Proto {
    public long type;
    public byte[] raw;
    public ByteBuffer bb;

    public Proto(long type, byte[] raw) {
      this.type = type;
      this.raw = raw;
      bb = ByteBuffer.wrap(raw);
    }
  }

  public static String byteArrayToHexString(byte[] a) {
    if (a == null) return "null";
    int iMax = a.length - 1;
    if (iMax == -1) return "[]";

    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; ; i++) {
      b.append(String.format((a[i] & 0xFF) < 16 ? "0x0%X" : "0x%X", a[i]));
      if (i == iMax) return b.append(']').toString();
      b.append(", ");
    }
  }

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static String fromCharCode(int... codePoints) {
    return new String(codePoints, 0, codePoints.length);
  }

  public Map<String, List<String>> splitQuery(URI uri) {
    if (uri.getQuery() == null) {
      return Collections.emptyMap();
    }
    return Arrays.stream(uri.getQuery().split("&"))
        .map(this::splitQueryParameter)
        .collect(
            Collectors.groupingBy(
                SimpleImmutableEntry::getKey,
                LinkedHashMap::new,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }

  public SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
    final int idx = it.indexOf("=");
    final String key = idx > 0 ? it.substring(0, idx) : it;
    final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
    return new SimpleImmutableEntry<>(key, value);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(
        () -> {
          try {
            OnlineDialog window = new OnlineDialog();
            window.setVisible(true);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }
}
