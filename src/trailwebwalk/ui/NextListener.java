package trailwebwalk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author al
 */
public class NextListener implements ActionListener {
    private final WebTrailWalkUI theUI;

    NextListener(WebTrailWalkUI theNewUI) {
        theUI = theNewUI;
    }

    public void actionPerformed(ActionEvent e) {
        theUI.next();
    }
}

