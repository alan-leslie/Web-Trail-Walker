/*
 */
package randomwebwalk.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import randomwebwalk.RandomWebWalkController;

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
    private JTextField startPageTextField;
    private JButton playPauseButton;
    private JButton stopButton;
    private JButton nextButton;
    private JButton previousButton;
    private JList trailList;
    private Thread taskThread = null;
    private PlayPauseListener thePlayPauseListener = null;
    private StopListener theStopListener = null;
    private NextListener theNextListener = null;
    private PreviousListener thePreviousListener = null;
    private URL initialURL = null;
    private RandomWebWalkController theController = null;

    // @param images - needs to be three images at least
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
        List<String> trailText = null;

        // todo disable next and prev buttons

        if (theController != null) {
            trailText = theController.getTrailText();
        }

        setLayout(new GridLayout(3, 1));

        getContentPane().add(getListPanel(trailText));
        getContentPane().add(getLabelPanel());
        getContentPane().add(getButtonPanel(), "Last");
        setSize(200, 350);
        setLocation(1050, 550);
        setVisible(true);
    }

    public void play() {
        if (!isPlaying()) {
            initialURL = null;
            boolean isValidURL = false;

            if (theController != null) {
                if (theController.needsStartPage()) {
                    String initialURLStr = startPageTextField.getText();

                    try {
                        if (!initialURLStr.isEmpty()) {
                            if (initialURLStr.contains("http://")) {
                                initialURL = new URL(initialURLStr);
                            } else {
                                String theFullURLStr = theController.getBaseURL() + initialURLStr;
                                initialURL = new URL(theFullURLStr);
                            }
                            isValidURL = true;
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(WebTrailWalkUI.class.getName()).log(Level.SEVERE, null, ex);
                        statusLabel.setText("Invalid URL");
                    }
                } else {
                    isValidURL = true;
                }

                if (isValidURL) {
                    System.out.println("play - setting icon to pause");
                    playPauseButton.setIcon(pauseIcon);
                    walk();
                }
            }
        }
    }

    public void pause() {
        if (isPlaying()) {
            playPauseButton.setIcon(playIcon);

            if (theController != null) {
                theController.pauseTask();
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
            theController.setInitialURL(initialURL);
            taskThread = new Thread(theController);

            taskThread.setPriority(Thread.NORM_PRIORITY);
            taskThread.start();
        }
    }

    private JPanel getLabelPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
//        panelId.add(idLabel);
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setVerticalAlignment(JLabel.BOTTOM);
        statusLabel.setText("Status");
//        statusLabel.setSize(200, 50);
        startPageTextField = new JTextField();
        startPageTextField.setColumns(30);
        panel.add(startPageTextField);
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

        trailList = new JList(listModel);
        trailList.setSelectedIndex(0);
        trailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trailList.setLayoutOrientation(JList.VERTICAL);
        trailList.setVisibleRowCount(-1);

        JScrollPane listScrollPane = new JScrollPane(trailList);
        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(trailList);
//        panel.setSize(200, 200);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.add(listScrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void setController(RandomWebWalkController newController) {
        theController = newController;
    }

    // todo - disable buttons if at end or start
    // or if stopped or running?    
    private void setEnableButtons() {
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
    }

    void prev() {
        theController.stepBack();
        int theTrailPos = theController.getCurrentTrailPos();
        trailList.setSelectedIndex(theTrailPos);
        setEnableButtons();
    }

    void next() {
        theController.stepForward();
        int theTrailPos = theController.getCurrentTrailPos();
        trailList.setSelectedIndex(theTrailPos);
        setEnableButtons();
    }
}
