package featurecat.lizzie.theme;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * ITheme
 */
public interface ITheme {
    static ITheme loadTheme(String name) {
        ITheme ret = _loadTheme(name);
        if (ret == null) {
            return new DefaultTheme();
        }
        return ret;
    }

    static ITheme _loadTheme(String name) {
        try {
            File themes = new File("theme");
            if (!themes.isDirectory()) {
                return null;
            }
            ArrayList<URL> jarFileList = new ArrayList<URL>();
            for (File file : themes.listFiles()) {
                if (file.canRead() && file.getName().endsWith(".jar")) {
                    jarFileList.add(file.toURI().toURL());
                }
            }
            URLClassLoader loader = new URLClassLoader(jarFileList.toArray(new URL[jarFileList.size()]));
            Class<?> theme = loader.loadClass(name);
            loader.close();
            ITheme ret = (ITheme) theme.newInstance();
            return ret;
        } catch (Exception e) {
            return new DefaultTheme();
        }
    }

    // Considering that the theme may implement different pieces for each coordinate, you need to pass in the coordinates.
    BufferedImage getBlackStone(int[] position);

    BufferedImage getWhiteStone(int[] position);

    BufferedImage getBoard();

    BufferedImage getBackground();

    /**
     * Allow user specify the font name
     */
    default String getFontName() {
        return null;
    }

    /**
     * Use solid current stone indicator
     */
    default boolean solidStoneIndicator() {
        return false;
    }

    /**
     * The background color of the comment panel
     */
    default Color commentBackgroundColor() {
        return new Color(0, 0, 0, 200);
    }

    /**
     * The font color of the comment
     */
    default Color commentFontColor() {
        return Color.WHITE;
    }
}
