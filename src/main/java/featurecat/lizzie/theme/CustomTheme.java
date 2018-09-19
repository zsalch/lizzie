package featurecat.lizzie.theme;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


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
    private String path = "theme/custom/";
    private DefaultTheme defaultTheme = null;

    public CustomTheme(String themeName) {
        this.themeName = themeName;
        this.path += this.themeName;
    }

    @Override
    public BufferedImage getBlackStone(int[] position) {
        if (blackStoneCached == null) {
            try {
                blackStoneCached = ImageIO.read(new File(this.path + "/black0.png"));
            } catch (IOException e) {
                e.printStackTrace();
                if (this.defaultTheme == null) {
                    this.defaultTheme = new DefaultTheme();
                }
                blackStoneCached = this.defaultTheme.getBlackStone(position);
            }
        }
        return blackStoneCached;
    }

    @Override
    public BufferedImage getWhiteStone(int[] position) {
        if (whiteStoneCached == null) {
            try {
                whiteStoneCached = ImageIO.read(new File(this.path + "/white0.png"));
            } catch (IOException e) {
                e.printStackTrace();
                if (this.defaultTheme == null) {
                    this.defaultTheme = new DefaultTheme();
                }
                whiteStoneCached = this.defaultTheme.getWhiteStone(position);
            }
        }
        return whiteStoneCached;
    }

    @Override
    public BufferedImage getBoard() {
        if (boardCached == null) {
            try {
                boardCached = ImageIO.read(new File(this.path + "/board.png"));
            } catch (IOException e) {
                e.printStackTrace();
                if (this.defaultTheme == null) {
                    this.defaultTheme = new DefaultTheme();
                }
                boardCached = this.defaultTheme.getBoard();
            }
        }
        return boardCached;
    }

    @Override
    public BufferedImage getBackground() {
        if (backgroundCached == null) {
            try {
                backgroundCached = ImageIO.read(new File(this.path + "/background.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
                if (this.defaultTheme == null) {
                    this.defaultTheme = new DefaultTheme();
                }
                backgroundCached = this.defaultTheme.getBackground();
            }
        }
        return backgroundCached;
    }
}