package featurecat.lizzie.rules;

import java.util.HashMap;
import java.util.Map;

public class BoardData {
    public int moveNumber;
    public int[] lastMove;
    public int[] moveNumberList;
    public boolean blackToPlay;

    public Stone lastMoveColor;
    public Stone[] stones;
    public Zobrist zobrist;

    public boolean verify;

    public double winrate;
    public int playouts;

    public int blackCaptures;
    public int whiteCaptures;

    // Comment in the Sgf move
    public String comment;

    // Node properties
    private final Map<String, String> properties = new HashMap<String, String>();

    public BoardData(Stone[] stones, int[] lastMove, Stone lastMoveColor, boolean blackToPlay, Zobrist zobrist, int moveNumber, int[] moveNumberList, int blackCaptures, int whiteCaptures, double winrate, int playouts) {
        this.moveNumber = moveNumber;
        this.lastMove = lastMove;
        this.moveNumberList = moveNumberList;
        this.blackToPlay = blackToPlay;

        this.lastMoveColor = lastMoveColor;
        this.stones = stones;
        this.zobrist = zobrist;
        this.verify = false;

        this.winrate = winrate;
        this.playouts = playouts;
        this.blackCaptures = blackCaptures;
        this.whiteCaptures = whiteCaptures;
    }

    /**
     * Add a key and value
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, String value) {
        if ("LB".equals(key) || "AB".equals(key) || "AW".equals(key) || "AE".equals(key)) {
            // Label and add/remove stones
            String oriValue = properties.get(key);
            if (oriValue == null) {
                properties.put(key, value);
            } else {
                properties.put(key, oriValue + "," + value);
            }
        } else {
            properties.put(key, value);
        }
    }

    /**
     * Get a value with key
     * 
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Get a value with key, or the default if there is no such key
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public String optProperty(String key, String defaultValue) {
        if (properties.get(key) == null) {
            return defaultValue;
        } else {
            return properties.get(key);
        }
    }

    /**
     * Get the properties
     * 
     * @return
     */
    public Map<String, String> getProperties() {
      return properties;
    }
    

    /**
     * Add the properties
     * 
     * @return
     */
    public void addProperties(Map<String, String> properties) {
        if (properties != null && properties.size() > 0) {
            for (String key : properties.keySet()) {
                addProperty(key, properties.get(key));
            }
        }
    }
}
