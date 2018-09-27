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
            properties.merge(key, value, (old, val) -> old + "," + val);
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
        return properties.getOrDefault(key, defaultValue);
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
            properties.forEach((key, value) -> addProperty(key, value));
        }
    }

    /**
     * Add the properties from string
     * 
     * @return
     */
    public void addProperties(String propsStr) {
        if (propsStr != null) {
            boolean inTag = false, escaping = false;
            String tag = null;
            StringBuilder tagBuilder = new StringBuilder();
            StringBuilder tagContentBuilder = new StringBuilder();

            for (int i = 0; i < propsStr.length(); i++) {
                char c = propsStr.charAt(i);
                if (escaping) {
                    tagContentBuilder.append(c);
                    escaping = false;
                    continue;
                }
                switch (c) {
                    case '(':
                        if (inTag) {
                            if (i > 0) {
                                tagContentBuilder.append(c);
                            }
                        }
                        break;
                    case ')':
                        if (inTag) {
                            tagContentBuilder.append(c);
                        }
                        break;
                    case '[':
                        inTag = true;
                        String tagTemp = tagBuilder.toString();
                        if (!tagTemp.isEmpty()) {
                            tag = tagTemp.replaceAll("[a-z]", "");
                        }
                        tagContentBuilder = new StringBuilder();
                        break;
                    case ']':
                        inTag = false;
                        tagBuilder = new StringBuilder();
                        addProperty(tag, tagContentBuilder.toString());
                        break;
                    case ';':
                        break;
                    default:
                        if (inTag) {
                            if (c == '\\') {
                                escaping = true;
                                continue;
                            }
                            tagContentBuilder.append(c);
                        } else {
                            if (c != '\n' && c != '\r' && c != '\t' && c != ' ') {
                                tagBuilder.append(c);
                            }
                        }
                }
            }
        }
    }

    /**
     * Get properties string
     * 
     * @return
     */
    public String propertiesString() {
        StringBuilder sb = new StringBuilder();
        if (properties != null) {
            properties.forEach((key, value) -> sb.append(nodeString(key, value)));
        }
        return sb.toString();
    }

    /**
     * Get node string
     * 
     * @param key
     * @param value
     * @return
     */
    public String nodeString(String key, String value) {
        StringBuilder sb = new StringBuilder();
        if ("LB".equals(key) || "AB".equals(key) || "AW".equals(key) || "AE".equals(key)) {
            // Label and add/remove stones
            sb.append(key);
            String[] vals = value.split(",");
            for (String val : vals) {
                sb.append("[").append(val).append("]");
            }
        } else {
            sb.append(key).append("[").append(value).append("]");
        }
        return sb.toString();
    }
}
