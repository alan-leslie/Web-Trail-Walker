/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trailwebwalk.ui;

import trailwebwalk.ui.WebTrailWalkUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author al
 */
public class PlayPauseListener implements ActionListener {
    private final WebTrailWalkUI theUI;

    PlayPauseListener(WebTrailWalkUI theNewUI){
        theUI = theNewUI;
    }

    public void actionPerformed(ActionEvent e) {
        if(theUI.isPlaying()){
            theUI.pause();
        } else {
            theUI.play();
        }
    }
}
