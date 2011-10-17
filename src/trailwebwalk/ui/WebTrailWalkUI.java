/*
 */
package trailwebwalk.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import trailwebwalk.TrailItem;
import trailwebwalk.WebWalkController;

/*
 * User interface class for random web walker.
 * Simple interface that includes: an input field for the start URL
 * a status display text field
 * two buttons indicating whether the program is running pausec or stopped.
 */
public class WebTrailWalkUI extends JFrame {
    private ImageIcon pauseIcon;
    private ImageIcon playIcon;
    private ImageIcon stopIcon;
    private ImageIcon nextIcon;
    private ImageIcon previousIcon;
    private JLabel statusLabel;
    private JButton playPauseButton;
    private JButton stopButton;
    private JButton nextButton;
    private JButton previousButton;
    private ToggleJList trailList;
    private Thread taskThread = null;
    private PlayPauseListener thePlayPauseListener = null;
    private StopListener theStopListener = null;
    private NextListener theNextListener = null;
    private PreviousListener thePreviousListener = null;
    private WebWalkController theController = null;

    // @param images - needs to be five images at least
    public WebTrailWalkUI(BufferedImage[] images) {
        setTitle("Trail Walk");
        pauseIcon = new ImageIcon(images[0]);
        playIcon = new ImageIcon(images[1]);
        stopIcon = new ImageIcon(images[2]);
        nextIcon = new ImageIcon(images[3]);
        previousIcon = new ImageIcon(images[4]);
    }

    public void start() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        List<String> trailText = new ArrayList<String>();

        if (theController != null) {
            List<TrailItem> trailItems = theController.getTrailItems();
            for(TrailItem theItem: trailItems){
                trailText.add(theItem.getLabel());              
            }           
        }

        setLayout(new BorderLayout());

        getContentPane().add(getListPanel(trailText));
        getContentPane().add(getButtonPanel(), BorderLayout.PAGE_END);
        setSize(200, 350);
        setLocation(1050, 550);
        setEnableNextPrevButtons(false);
        setVisible(true);
    }

    public void play() {
        if (!isPlaying()) {
            setEnableNextPrevButtons(false);

            if (theController != null) {
                    System.out.println("play - setting icon to pause");
                    playPauseButton.setIcon(pauseIcon);
                    walk();
            }
        }
    }

    public void pause() {
        if (isPlaying()) {
            playPauseButton.setIcon(playIcon);

            if (theController != null) {
                theController.pauseTask();
                setEnableNextPrevButtons(true);
            }

            System.out.println("pause - setting icon to play");
        }
    }

    public void stop() {
        if (theController != null) {
            theController.stopTask();
        }
        System.out.println("stop - setting icon to play");
        playPauseButton.setIcon(playIcon);
        setEnableNextPrevButtons(false);
    }

    public boolean isPlaying() {
        if (taskThread != null
                && taskThread.isAlive()) {
            return true;
        } else {
            return false;
        }
    }

    private void walk() {
        if (isPlaying()) {
            System.out.println("Thread is running - not starting new task");
        } else {
            WalkStatusDisplay theStatusDisplay = new JLabelWrapper(statusLabel);
            theController.setNotificationDisplay(theStatusDisplay);
            PlayPauseDisplay thePlayPauseDisplay = new JButtonWrapper(playPauseButton, playIcon);
            theController.setPlayPauseDisplay(thePlayPauseDisplay);           
            ListItemSelector theListItemSelector = new JListWrapper(trailList);
            theController.setListItemSelector(theListItemSelector);           
            taskThread = new Thread(theController);

            taskThread.setPriority(Thread.NORM_PRIORITY);
            taskThread.start();
        }
    }

    private JPanel getLabelPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setVerticalAlignment(JLabel.BOTTOM);
        statusLabel.setText("Status");
        panel.add(statusLabel);
        panel.setSize(25, 250);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        return panel;
    }

    private JPanel getButtonPanel() {
        playPauseButton = new JButton(playIcon);
        thePlayPauseListener = new PlayPauseListener(this);
        playPauseButton.addActionListener(thePlayPauseListener);

        stopButton = new JButton(stopIcon);
        theStopListener = new StopListener(this);
        stopButton.addActionListener(theStopListener);

        nextButton = new JButton(nextIcon);
        theNextListener = new NextListener(this);
        nextButton.addActionListener(theNextListener);

        previousButton = new JButton(previousIcon);
        thePreviousListener = new PreviousListener(this);
        previousButton.addActionListener(thePreviousListener);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(playPauseButton);
        panel.add(stopButton);
        panel.add(previousButton);
        panel.add(nextButton);

        return panel;
    }

    private JPanel getListPanel(List<String> trailText) {
        DefaultListModel listModel = new DefaultListModel();

        for (String trailItem : trailText) {
            listModel.addElement(trailItem);
        }

        trailList = new ToggleJList(listModel);
        trailList.setSelectedIndex(0);
//        trailList.setSelectedValue(trailText.get(0), true);
        trailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trailList.setLayoutOrientation(JList.VERTICAL);
        trailList.setVisibleRowCount(-1);

        JScrollPane listScrollPane = new JScrollPane(trailList);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.add(listScrollPane, BorderLayout.CENTER);
        panel.add(getLabelPanel(), BorderLayout.PAGE_END);

        return panel;
    }

    public void setController(WebWalkController newController) {
        theController = newController;
    }

    private void setEnableNextPrevButtons(boolean isEnabled) {
        if(isEnabled){          
            if (theController.isAtStart()) {
                previousButton.setEnabled(false);
            } else {
                previousButton.setEnabled(true);
            }

            if (theController.isAtEnd()) {
                nextButton.setEnabled(false);
            } else {
                nextButton.setEnabled(true);
            }
        } else {
            previousButton.setEnabled(isEnabled);
            nextButton.setEnabled(isEnabled);
        }       
                    
        trailList.setEnableUserSelection(isEnabled);
    }

    void prev() {
        theController.stepBack();
        int theTrailPos = theController.getCurrentTrailPos();
        trailList.setSelectedIndex(theTrailPos);
        setEnableNextPrevButtons(true);
    }

    void next() {
        theController.stepForward();
        int theTrailPos = theController.getCurrentTrailPos();
        trailList.setSelectedIndex(theTrailPos);
        setEnableNextPrevButtons(true);
    }
}
