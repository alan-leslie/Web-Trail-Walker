/*
 * 
 *
 */

package trailwebwalk;

import trailwebwalk.ui.WebTrailWalkUI;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author al
 */
public class Main {
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    // TODO -
    // Dead end page for case when it is a form
    public static void main(String[] args) throws IOException {
        String[] ids = {"Pause24.gif", "Play24.gif", "Stop24.gif", "Next24.gif", "Prev24.gif"};
        BufferedImage[] images = new BufferedImage[ids.length];
        for (int j = 0; j < images.length; j++) {
            images[j] = ImageIO.read(new File(ids[j]));
        }
        
        Properties properties = new Properties();
        FileInputStream is = null;
        
        try {
            is = new FileInputStream( "WebTrailWalk.properties" );
            properties.load( is );
        } catch( IOException e ) {
            // ...
        } finally {
            if( null != is ) {
                try {
                    is.close();
                } catch( IOException e ) {
                    /* .... */
                }
            }
        }

        Logger theLogger = Main.makeLogger();
        WebTrailWalkUI theUI = new WebTrailWalkUI(images);
        WebWalkController theController = new WebWalkController(properties, theLogger);
        theUI.setController(theController);
        theController.startUp();
        theUI.start();
    }

    /**
     *
     * @return - valid logger (single file).
     */
    private static Logger makeLogger() {
        Logger lgr = Logger.getLogger("WebTrailWalk");
        lgr.setUseParentHandlers(false);
        lgr.addHandler(simpleFileHandler());
        return lgr;
    }

    /**
     *
     * @return - valid file handler for logger.
     */
    private static FileHandler simpleFileHandler() {
        try {
            FileHandler hdlr = new FileHandler("WebTrailWalker.log");
            hdlr.setFormatter(new SimpleFormatter());
            return hdlr;
        } catch (Exception e) {
            System.out.println("Failed to create log file");
            return null;
        }
    }
}
