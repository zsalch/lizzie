package featurecat.lizzie.analysis;

import featurecat.lizzie.Config;
import featurecat.lizzie.Lizzie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EngineManager {

  private List<Leelaz> engineList;
  private int currentEngineNo;

  public EngineManager(Config config) throws JSONException, IOException {

    JSONObject eCfg = config.config.getJSONObject("leelaz");
    String engineCommand = eCfg.getString("engine-command");
    // substitute in the weights file
    engineCommand = engineCommand.replaceAll("%network-file", eCfg.getString("network-file"));

    // Start default engine
    Leelaz lz = new Leelaz(engineCommand);
    Lizzie.leelaz = lz;
    Lizzie.board = lz.board;
    lz.startEngine();
    engineList = new ArrayList();
    engineList.add(lz);
    currentEngineNo = 0;

    new Thread(
            () -> {
              // Process other engine
              Optional<JSONArray> enginesOpt =
                  Optional.ofNullable(
                      Lizzie.config.leelazConfig.optJSONArray("engine-command-list"));
              enginesOpt.ifPresent(
                  m -> {
                    m.forEach(
                        a -> {
                          if (a != null && !a.toString().isEmpty()) {
                            Leelaz e;
                            try {
                              e = new Leelaz(a.toString());
                              // TODO: how sync the board
                              e.board = Lizzie.board;
                              e.startEngine();
                              // TODO: Need keep analyze?
                              //                  e.togglePonder();
                              engineList.add(e);
                            } catch (JSONException | IOException e1) {
                              e1.printStackTrace();
                            }
                          }
                        });
                  });
            })
        .start();
  }

  /**
   * Switch the Engine by index number
   *
   * @param index engine index
   */
  public void switchEngine(int index) {
    if (index == this.currentEngineNo || index > this.engineList.size()) return;
    Leelaz curEng = engineList.get(this.currentEngineNo);
    if (curEng.isThinking) {
      if (Lizzie.frame.isPlayingAgainstLeelaz) {
        Lizzie.frame.isPlayingAgainstLeelaz = false;
        Lizzie.leelaz.isThinking = false;
      }
      curEng.togglePonder();
    }
    // TODO: Need keep analyze?
    if (curEng.isPondering()) {
      curEng.togglePonder();
    }
    curEng.board.saveMoveNumber();
    try {
      Leelaz newEng = engineList.get(index);
      Lizzie.leelaz = newEng;
      // TODO: how sync the board
      //      newEng.board = curEng.board;
      //      Lizzie.board = newEng.board;
      if (!newEng.isStarted()) {
        newEng.startEngine();
      }
      if (!newEng.isPondering()) {
        newEng.togglePonder();
      }
      Lizzie.board.restoreMoveNumber();
      this.currentEngineNo = index;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
