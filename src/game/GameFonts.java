package game;


import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class GameFonts {

    public static final Font PIXEL_ARMY = loadFont("/font/PixelArmy.ttf", 62f);
    public static final Font MUNRO = loadFont("/font/Munro.ttf", 22f);

    private static Font loadFont(String path, float size) {
        try {
            // Get the file as a stream from the resources root
            InputStream is = GameFonts.class.getResourceAsStream(path);

            if (is == null) {
                System.err.println("Error: Could not find font file at " + path);
                // Return a default system font so the game doesn't crash
                return new Font("Serif", Font.PLAIN, (int) size);
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, is);


            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

            return font.deriveFont(size);

        } catch (Exception e) {
            System.err.println("Exception while loading font: " + path);
            e.printStackTrace();
            return new Font("Serif", Font.PLAIN, (int) size);
        }
    }
}
