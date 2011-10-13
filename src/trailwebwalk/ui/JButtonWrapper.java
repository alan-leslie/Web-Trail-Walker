
package trailwebwalk.ui;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 * @author al
 */
public class JButtonWrapper implements PlayPauseDisplay {
    private final JButton theButton;
    private final ImageIcon thePlayIcon;

    JButtonWrapper(JButton playPauseButton, ImageIcon playIcon) {
        theButton = playPauseButton;
        thePlayIcon = playIcon;
    }

    public void setToPlay() {
        theButton.setIcon(thePlayIcon);
        
        // todo disable the next and prev buttons
    }
    
    public void setToPause() {
//        theButton.setIcon(thePlayIcon);
        
        // todo enable next and prev buttons
}
}
