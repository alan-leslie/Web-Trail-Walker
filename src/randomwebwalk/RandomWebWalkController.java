package randomwebwalk;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import randomwebwalk.ui.PlayPauseDisplay;
import randomwebwalk.ui.WalkStatusDisplay;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.WebDriverException;

/**
 *
 * @author al
 * Class that controls the high level activity of the walker.
 * Focusses on the policy for failure recovery rather than on the basic actions
 * performed by the walker.
 * Controls the thread for the walker (therefore implements runnable).
 * @invariant - the runner is valid.
 * @invariant - the Logger is a valid logger.
 */
public class RandomWebWalkController implements Runnable {

    private WalkStatusDisplay statusLabel = null; // status label passed from UI
    private PlayPauseDisplay playPauseDisplay = null; // play display passed from UI.
    private final RandomWebWalkRunner theRunner;
    private volatile boolean taskStopped = false;
    private final int BETWEEN_PAGE_SLEEP_TIME; // time to wait between page refresh
    private final RandomWebWalkRunner.WalkType theType;
    private final Logger theLogger;
    private final String profileId; // the firefox profile identifier
    private String theBaseURL;  // the base URL 
    private final ExecutorService exec;

    /**
     *
     * @param properties - got from the Random.properties file.
     * @param newLogger - valid logger.
     * @throws MalformedURLException 
     * @precon - as per param spec.
     * @postcon - as per invariant.
     */
    public RandomWebWalkController(Properties properties,
            Logger newLogger) throws MalformedURLException {
        theLogger = newLogger;
        exec = Executors.newSingleThreadExecutor();

        theType = RandomWebWalkRunner.WalkType.trail;

        String trailFile = properties.getProperty("TrailFileName", "");
        theRunner = new RandomWebWalkRunner(theType, trailFile, theLogger);

        String sleepTimeProperty = properties.getProperty("SleepTime");

        int intSleepTime = Integer.parseInt(sleepTimeProperty);

        if (intSleepTime > 0) {
            BETWEEN_PAGE_SLEEP_TIME = intSleepTime;
        } else {
            BETWEEN_PAGE_SLEEP_TIME = 25;
        }

        profileId = properties.getProperty("ProfileId");
    }

    /**
     * starts the walker thread running (so calls start up and controls stepping).
     * @precon - as per invariant
     * @postcon - that the walker thread has finished.
     * @postcon - as per invariant. 
     */
    public void run() {
        statusLabel.setText("Walking");
        taskStopped = false;

        try {
            start();

            RandomWebWalkRunner.WalkStatus runnerStatus = theRunner.checkStatus();

            if (runnerStatus != RandomWebWalkRunner.WalkStatus.successfulStep) {
                pauseTask();
                statusLabel.setText("Walking failed");
                return;
            } else {
                pauseBetweenPages(runnerStatus);
            }

            while (!isTaskStopped()) {
                step();

                RandomWebWalkRunner.WalkStatus stepRunnerStatus = theRunner.checkStatus();

                if (stepRunnerStatus == RandomWebWalkRunner.WalkStatus.failedStep) {
                    pauseTask();
                    statusLabel.setText("Walking failed");
                } else {
                    if (stepRunnerStatus == RandomWebWalkRunner.WalkStatus.complete) {
                        pauseTask();
                        statusLabel.setText("Walking complete");
                    } else {
                        pauseBetweenPages(stepRunnerStatus);
                    }
                }
            }
        } catch (InterruptedException e) {
            theLogger.log(Level.INFO, null, e);
            pauseTask();
        } catch (Exception ex) {
            theLogger.log(Level.SEVERE, null, ex);
            pauseTask();
        }
    }

    /**
     *
     * @return - whether the walker has been interrupted.
     * @precon - as per invariant
     * @postcon - no change to internal state.
     * @postcon - as per invariant.
     */
    public synchronized boolean isTaskStopped() {
        return taskStopped;
    }

    /**
     * Connects this controller to the input display.
     * @param theStatusDisplay
     */
    public void setNotificationDisplay(WalkStatusDisplay theStatusDisplay) {
        statusLabel = theStatusDisplay;
    }

    /**
     * Sets the URL that the walker should initially connect to to the input.
     * @param newInitialURL - a valid URL to connect to.
     */
    public void setInitialURL(URL newInitialURL) {
        if (theType == RandomWebWalkRunner.WalkType.free) {
            theRunner.setInitialURL(newInitialURL);
        }

        if (theType == RandomWebWalkRunner.WalkType.delicious) {

            theRunner.setInitialURL(newInitialURL);
        }
    }

    /**
     * Sets the pay/pause display to the specified param.
     * @param newPlayPauseDisplay 
     */
    public void setPlayPauseDisplay(PlayPauseDisplay newPlayPauseDisplay) {
        playPauseDisplay = newPlayPauseDisplay;
    }

    /**
     * Starts the walker running (called by new thread).
     */
    private void start() {
        if (!theRunner.isStarted()) {
            theRunner.stop();
            theRunner.startUp("", "", profileId);
        } else {
            theRunner.restore();
        }
    }

    /**
     * Step forward
     * @precon - as per invariant.
     * @postcon - either the browser has stepped forward to the next page or
     * has performed failure correction - e.g refresh, go back or fail.
     */
    private void step() throws Exception {
        if (theRunner.hasPageMoved()) {
            pauseTask();
            return;
        }

        theRunner.step();

        RandomWebWalkRunner.WalkStatus theStatus = theRunner.checkStatus();

        if (theStatus != RandomWebWalkRunner.WalkStatus.successfulStep) {
            switch (theStatus) {
                case pageNotEnglish:
                    recoverPageNotEnglish();
                    break;
                case permissionDenied:
                    recoverPageNotFound();
                    break;
                case pageNotFound:
                    recoverPageNotFound();
                    break;
                case pageTimedOut:
                    recoverPageTimeout();
                    break;
                case pageDeadEnd:
                    recoverPageDeadEnd();
                    break;
            }
        }
    }

    /**
     * Perform pause for the required time between page change.
     */
    private void pauseBetweenPages(RandomWebWalkRunner.WalkStatus runnerStatus) throws InterruptedException {
        if (runnerStatus == RandomWebWalkRunner.WalkStatus.successfulStep) {
            int counter = 0;
            while (counter++ < (BETWEEN_PAGE_SLEEP_TIME * 10)
                    && !isTaskStopped()) {
                Thread.sleep(100);

                if (counter % 10 == 0) {
                    int downCounter = BETWEEN_PAGE_SLEEP_TIME - (counter / 10);
                    statusLabel.setText("counter = " + downCounter);
                }
            }
        }
    }

    /**
     * Stops the current walk (should interrupt any current processing).
     */
    public synchronized void stopTask() {
        theLogger.log(Level.INFO, "Stopping");
        pauseTask();
        statusLabel.setText("Walking stopped");
        theRunner.stop();
    }

    /**
     * Pause the current task (should interrupt any current processing).
     */
    public synchronized void pauseTask() {
        theLogger.log(Level.INFO, "Pausing");
        taskStopped = true;
        statusLabel.setText("Walking interrupted/paused");
        playPauseDisplay.setToPlay();
        theRunner.pause();
    }

    /**
     *
     * @return whether the type of walk requires an explicitly set start URL
     * @precon - as per invariant.
     * @postcon -as per invariant/return spec.
     */
    public boolean needsStartPage() {
        if (theRunner.getType() == RandomWebWalkRunner.WalkType.free
                || theRunner.getType() == RandomWebWalkRunner.WalkType.delicious) {
            return true;
        }

        return false;
    }

    /**
     *
     * @return the base url
     * @precon - as per invariant.
     * @postcon -as per invariant/return spec.
     */
    public String getBaseURL() {
        return theBaseURL;
    }

    /**
     * process case of failure of next page not found.
     */
    private void recoverPageNotFound() throws WebDriverException {
        theLogger.log(Level.INFO, "recoverPageNotFound");
        goBack();
    }

    /**
     * process case of failure of next page not in english.
     */
    private void recoverPageNotEnglish() throws WebDriverException {
        theLogger.log(Level.INFO, "recoverPageNotEnglish");
        goBack();
    }

    /**
     * process case of failure of next page permission denied.
     */
    private void recoverPermissionDenied() throws WebDriverException {
        theLogger.log(Level.INFO, "recoverPermissionDenied");
        goBack();
    }

    /**
     * process case of failure of page timeout.
     */
    private void recoverPageTimeout() throws WebDriverException {
        theLogger.log(Level.INFO, "recoverPageTimeout");
        RandomWebWalkRunner.WalkStatus runnerStatus = theRunner.checkStatus();

        if (runnerStatus == RandomWebWalkRunner.WalkStatus.pageTimedOut) {
            theLogger.log(Level.INFO, "trying refresh");
            theRunner.refresh();
            runnerStatus = theRunner.checkStatus();
        }

        if (runnerStatus == RandomWebWalkRunner.WalkStatus.pageTimedOut) {
            theLogger.log(Level.INFO, "trying go back");
            theRunner.goBack();
            runnerStatus = theRunner.checkStatus();
        }

        if (runnerStatus == RandomWebWalkRunner.WalkStatus.pageTimedOut) {
            theLogger.log(Level.INFO, "giving up");
            theRunner.setStatus(RandomWebWalkRunner.WalkStatus.failedStep);
        }
    }

    /**
     * process case of failure of next page is a dead end.
     */
    private void recoverPageDeadEnd() throws Exception {
        goBack();
    }

    /**
     * Pass through to walker to just go back one page.
     */
    private void goBack() throws WebDriverException {
        theRunner.goBack();
    }

    /**
     * 
     */
    public void stepBack() {
        // need to know whether it is at start so cannot step back
        // if so disable the button
        if (!isTaskStopped()) {
            pauseTask();
        }

        GoBackWorker goBackWorker = new GoBackWorker(theRunner);

        try {
            exec.submit(goBackWorker).get();
        } catch (InterruptedException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     */
    public void stepForward() {
        if (!isTaskStopped()) {
            pauseTask();
        }

        if (!theRunner.isAtEnd()) {
            StepWorker stepWorker = new StepWorker(theRunner);

            try {
                exec.submit(stepWorker).get();
            } catch (InterruptedException ex) {
                theLogger.log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                theLogger.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 
     * @return
     */
    public List<String> getTrailText() {
        List<String> retVal = theRunner.getTrailText();
        return retVal;
    }

    /**
     * 
     * @return
     */
    public boolean isAtEnd() {
        if (theRunner != null) {
            return theRunner.isAtEnd();
        }

        return true;
    }

    /**
     * 
     * @return
     */
    public boolean isAtStart() {
        if (theRunner != null) {
            return theRunner.isAtStart();
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RandomWebWalkController other = (RandomWebWalkController) obj;

        if (this.theType != other.theType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.theType != null ? this.theType.hashCode() : 0);
        return hash;
    }
}