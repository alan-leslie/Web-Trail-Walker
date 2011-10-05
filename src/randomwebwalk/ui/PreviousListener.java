package randomwebwalk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author al
 */
public class PreviousListener implements ActionListener {
    private final WebTrailWalkUI theUI;

    PreviousListener(WebTrailWalkUI theNewUI) {
        theUI = theNewUI;
    }

    public void actionPerformed(ActionEvent e) {
        theUI.prev();
    }
}
