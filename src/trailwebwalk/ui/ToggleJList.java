package trailwebwalk.ui;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *
 * @author al
 */
public class ToggleJList extends JList {

    boolean userSelectionEnabled = false;

    public ToggleJList(DefaultListModel listModel) {
        super(listModel);
    }

    public void setEnableUserSelection(boolean enableSelection) {
        userSelectionEnabled = enableSelection;
    }

    public void setSelectedIndex(int index) {
        if (userSelectionEnabled) {
            super.setSelectedIndex(index);
        }
    }

    public void setSelectedIndices(int[] indices) {
        if (userSelectionEnabled) {
            super.setSelectedIndices(indices);
        }
    }

    public void setSelectedValue(Object anObject, boolean shouldScroll) {
        if (userSelectionEnabled) {
            super.setSelectedValue(anObject, shouldScroll);
        }
    }

    public void addSelectionInterval(int anchor, int lead) {
        if (userSelectionEnabled) {
            super.addSelectionInterval(anchor, lead);
        }
    }
}
