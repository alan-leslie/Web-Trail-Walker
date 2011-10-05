/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package randomwebwalk.ui;

import randomwebwalk.ui.WebTrailWalkUI;
import java.awt.event.*;

/**
 *
 * @author al
 */
public class StopListener implements ActionListener {
    private final WebTrailWalkUI theUI;

    StopListener(WebTrailWalkUI theNewUI) {
        theUI = theNewUI;
    }

    public void actionPerformed(ActionEvent e) {
        theUI.stop();
    }
}
