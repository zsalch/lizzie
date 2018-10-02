package featurecat.lizzie.theme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Custom Theme
 * Allow to load the external image & theme config
 */
public class CustomTheme implements ITheme {
    BufferedImage blackStoneCached = null;
    BufferedImage whiteStoneCached = null;
    BufferedImage boardCached = null;
    BufferedImage backgroundCached = null;

    private String themeName = null;
    private String configFile = "theme.txt";
    private String pathPrefix = "theme" + File.separator + "custom";
    private String path = null;
    private JSONObject config = new JSONObject();
    private DefaultTheme defaultTheme = null;

    public CustomTheme(String themeName) {
        this.themeName = themeName;
        this.path = this.pathPrefix + File.separator + this.themeName;
        File file = new File(this.path + File.separator + this.configFile);
        if (file.canRead()) {
            FileInputStream fp;
            try {
                fp = new FileInputStream(file);
                config = new JSONObject(new JSONTokener(fp));
                fp.close();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (JSONException e) {
            }
        }
    }

    @Override
    public BufferedImage getBlackStone(int[] position) {
        if (blackStoneCached == null) {
            try {
                blackStoneCached = ImageIO.read(new File(this.path + File.separator + config.optString("black-stone-image", "black.png")));
            } catch (IOException e) {
                blackStoneCached = getDefaltTheme().getBlackStone(position);
            }
        }
        return blackStoneCached;
    }

    @Override
    public BufferedImage getWhiteStone(int[] position) {
        if (whiteStoneCached == null) {
            try {
                whiteStoneCached = ImageIO.read(new File(this.path + File.separator + config.optString("white-stone-image", "white.png")));
            } catch (IOException e) {
                whiteStoneCached = getDefaltTheme().getWhiteStone(position);
            }
        }
        return whiteStoneCached;
    }

    @Override
    public BufferedImage getBoard() {
        if (boardCached == null) {
            try {
                boardCached = ImageIO.read(new File(this.path + File.separator + config.optString("board-image", "board.png")));
            } catch (IOException e) {
                boardCached = getDefaltTheme().getBoard();
            }
        }
        return boardCached;
    }

    @Override
    public BufferedImage getBackground() {
        if (backgroundCached == null) {
            try {
                backgroundCached = ImageIO.read(new File(this.path + File.separator + config.optString("background-image", "background.png")));
            } catch (IOException e) {
                backgroundCached = getDefaltTheme().getBackground();
            }
        }
        return backgroundCached;
    }

    /**
     * Use custom font
     */
    @Override
    public String getFontName() {
        return config.optString("font-name", null);
    }

    /**
     * Get a default theme
     */
    private DefaultTheme getDefaltTheme() {
        if (this.defaultTheme == null) {
            this.defaultTheme = new DefaultTheme();
        }
        return this.defaultTheme;
    }
}