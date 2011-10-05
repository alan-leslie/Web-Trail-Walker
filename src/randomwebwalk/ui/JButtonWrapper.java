
package randomwebwalk.ui;

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
    }
}
