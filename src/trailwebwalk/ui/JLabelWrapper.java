package trailwebwalk.ui;

import javax.swing.JLabel;

/**
 *
 * @author al
 */
public class JLabelWrapper implements WalkStatusDisplay {
    private final JLabel theLabel;

    JLabelWrapper(JLabel newLabel){
        theLabel = newLabel;
    }

    public String getText() {
        return theLabel.getText();
    }

    public void setText(String theText) {
        theLabel.setText(theText);
    }
}
