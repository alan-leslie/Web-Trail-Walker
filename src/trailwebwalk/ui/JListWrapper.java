/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trailwebwalk.ui;

import javax.swing.JList;

/**
 *
 * @author al
 */
public class JListWrapper implements ListItemSelector {
    private final JList theList;

    JListWrapper(JList theList) {
        this.theList = theList;
    }
    public void selectItem(int itemNo){
        theList.setSelectedIndex(itemNo);
    }
}

