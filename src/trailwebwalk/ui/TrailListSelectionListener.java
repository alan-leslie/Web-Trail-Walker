
package trailwebwalk.ui;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author al
 */
public class TrailListSelectionListener implements ListSelectionListener {
    private JList trailList = null;
    private final WebTrailWalkUI theUI;
    
    public TrailListSelectionListener(WebTrailWalkUI theUI,
            JList trailList){
        this.theUI = theUI;
        this.trailList = trailList;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            int theNewIndex = trailList.getSelectedIndex();
            
           if (theNewIndex != -1) {
               theUI.stepTo(theNewIndex);
            //Selection
            // go to the required index
            }
        }
    }   
}
