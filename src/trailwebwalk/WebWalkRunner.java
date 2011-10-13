package trailwebwalk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.openqa.selenium.WebDriverException;
import trailwebwalk.browser.Browser;
import trailwebwalk.browser.Hyperlink;
import trailwebwalk.browser.Page;

/**
 *
 * @author al
 * Class encompassing basic functionality (go forward, back, error status etc.)
 * for walking the web.
 * @invariant - type of walk is set to one value alone.
 * @invariant - the Logger is a valid logger.
 * Note that the web browser can be set to null (before start and when stopped),
 * to a valid browser and to an invalid browser (the browser window has been
 * shut down without a pause/stop). In certain cases it should recover from
 * incorrect states (see precons below for more details).
 * Normally expect to have only one object of this class in a program.
 */
public class WebWalkRunner {
    // enum indication of the current status of the walk

    public enum WalkStatus {

        successfulStep,
        permissionDenied,
        pageTimedOut,
        pageNotFound,
        failedStep, 
        complete
    };

    // type of walk - crucial to decision of where the walk starts, how a
    // random link is selected and decisions on walkStatus
    public enum WalkType {
        trail
    };
    
    private Browser webBrowser = null;
    private final Logger theLogger;
    private WalkStatus walkStatus = WalkStatus.successfulStep;
    private int failureCount = 0;
    private String defaultLinkText = "";     // the link that should be selected if applicable
    private URL initialURL = null; // starting URL
    private final String theTrailFileName; // name of file that includes trail to be followed
    private List<URL> theTrail = null;  // trail of urls to be visited
    private ListIterator<URL> trailIterator = null;

    /**
     *
     * @param newType - the type of walk required
     * @param trailFile 
     * @param newLogger - valid logger for output info
     * @throws MalformedURLException 
     * @precon - as per invariant/param spec
     * @postcon - as per invariant
     */
    public WebWalkRunner(
            String trailFile,
            Logger newLogger) throws MalformedURLException {
        defaultLinkText = "";
        theTrailFileName = trailFile;
        theLogger = newLogger;
        
        initTrail();
    }

    /**
     * 
     * @param newInitialURL
     */
    public void setInitialURL(URL newInitialURL) {
        initialURL = newInitialURL;
    }

    /**
     * Start up the walk by logging into the web site if required.
     * @param idString - a valid iidentifier or null
     * @param passwordString - the password for the identifier or null
     * @param profileId - a profile (defaults if invalid) identifier or null
     * @throws WebDriverException - when the browser is invalid (e.g. the
     * attached firefox browser has been shut down without a pause/stop)
     * @precon - as per invariant/param spec
     * @postcon - the firefox browser is on the correct page to begin walk if
     * the status is set to successfulStep, otherwise set to loginFailure
     * (in case of login failure) or pageTimeout if there has been a socket
     * timeout.
     * @postcon - as per invariant
     */
    public void startUp(String idString,
            String passwordString,
            String profileId) throws WebDriverException {
        theLogger.log(Level.INFO, "Start up");
        webBrowser = new Browser(profileId, theLogger);
        boolean isStumbleUpon = false;

        try {
            webBrowser.start(initialURL, isStumbleUpon, idString, passwordString);
            setStatus(WalkStatus.successfulStep);
        } catch (LoginException ex) {
            theLogger.log(Level.SEVERE, null, ex);
            setStatus(WalkStatus.failedStep);
        } catch (WebDriverException theEx) {
            if (isExceptionTimeout(theEx)) {
                theLogger.log(Level.WARNING,
                        "Socket Timeout exception", theEx);
                webBrowser.stopPageLoad();
                setStatus(WalkStatus.pageTimedOut);
            } else {
                throw theEx;
            }
        }
    }

    /**
     *
     * @return - the type of walk
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public WalkType getType() {
        return WalkType.trail;
    }

    /**
     *
     * @precon - as per invariant
     * @postcon - the browser is pointing to the last page that was
     * successfully visited
     * @postcon - as per invariant/return value
     */
    public void restore() {
        Page theCurrentPage = webBrowser.getCurrentPage();

        if (theCurrentPage == null) {
            webBrowser.addNewPage();
        } else {
            if (webBrowser.hasPageMoved()) {
                webBrowser.restorePage();
            }
        }
    }

    /**
     * @precon - as per invariant
     * @postcon - as per invariant
     * @postcon - the status is set to successful
     */
    public void pause() {
        setStatus(WebWalkRunner.WalkStatus.successfulStep);
    }

    /**
     *
     * @return - whether the page that the browser is currently pointing to is
     * different from the one last visited by the browser.
     * @precon - as per invariant
     * @postcon - as per invariant (no change to internal state).
     */
    public boolean hasPageMoved() {
        return webBrowser.hasPageMoved();
    }

    /**
     * @precon - as per invariant
     * @postcon - loading of the page in the browser has been interrupted.
     * @postcon - browser is closed.
     */
    public void stop() {
        theLogger.log(Level.INFO, "Stop");

        if (webBrowser != null) {
            try {
                webBrowser.quit();
            } catch (WebDriverException ex) {
                theLogger.log(Level.INFO, "WebDriverException caught on trying to close down - ignored");
            }
            webBrowser = null;
        }

        setStatus(WalkStatus.successfulStep);
    }

    /**
     * steps forward to next random link.
     * @precon - as per invariant
     * @postcon - that the browser has moved on one page and status is set to
     * success.
     * @postcon - or status is set as failed after x number of incorrect attempts.
     * @postcon - or status is set to a value that reflects the reason for failure.
     * @throws WebDriverException - if the step forward fails because of socket
     * timeout.
     */
    public void step() throws WebDriverException {
        theLogger.log(Level.INFO, "Step");
        String currentPageURL = webBrowser.getCurrentPageURL();

        theLogger.log(Level.INFO, "Current page: {0}",
                currentPageURL);
        try {
            Page webPage = webBrowser.getCurrentPage();
            Hyperlink link = null;

            if (trailIterator != null) {
                if (trailIterator.hasNext()) {
                    String theURL = trailIterator.next().toString();
                    webBrowser.gotoURL(theURL);
                } else {
                    setStatus(WalkStatus.complete);
                }
            }

            Page newPage = webBrowser.getCurrentPage();
            String newPageURL = newPage.getURL();
            theLogger.log(Level.INFO, "New page: {0}", newPageURL);

            if (checkStatus() != WalkStatus.complete) {
                setStatus(WalkStatus.successfulStep);
            }

            theLogger.log(Level.INFO, "Status set");
        } catch (WebDriverException theEx) {
            if (isExceptionTimeout(theEx)) {
                theLogger.log(Level.WARNING,
                        "Socket Timeout exception", theEx);
                webBrowser.stopPageLoad();
                setStatus(WalkStatus.pageTimedOut);
            } else {
                throw theEx;
            }
        }

        if (!(checkStatus() == WalkStatus.successfulStep
                || checkStatus() == WalkStatus.complete)
                && failureCount > 3) {
            setStatus(WalkStatus.failedStep);
        }
    }

    /**
     * causes the browser to refresh the current page.
     * @precon - as per invariant
     * @postcon - as per invariant
     * @throws WebDriverException - if there is a socket timeout.
     */
    public void refresh() throws WebDriverException {
        theLogger.log(Level.INFO, "Refresh");
        String currentPageURL = webBrowser.getCurrentPageURL();

        try {
            webBrowser.refresh();

            Page newPage = webBrowser.getCurrentPage();
            setStatus(WalkStatus.successfulStep);
        } catch (WebDriverException theEx) {
            if (isExceptionTimeout(theEx)) {
                theLogger.log(Level.WARNING,
                        "Socket Timeout exception", theEx);
                webBrowser.stopPageLoad();
                setStatus(WalkStatus.pageTimedOut);
            } else {
                throw theEx;
            }
        }
    }

    /**
     * goes back to the previously (successfully visited) page.
     * @precon - as per invariant
     * @postcon - the browser is pointing to the previous page.
     * @postcon - as per invariant
     * @throws WebDriverException - if it was unsuccessful.
     */
    public void goBack() throws WebDriverException {
        theLogger.log(Level.INFO, "GoBack");
        try {
            webBrowser.goBack();
            String theURL = trailIterator.previous().toString();
            setStatus(WalkStatus.successfulStep);
        } catch (WebDriverException theEx) {
            if (isExceptionTimeout(theEx)) {
                theLogger.log(Level.WARNING,
                        "Socket Timeout exception", theEx);
                webBrowser.stopPageLoad();
                setStatus(WalkStatus.pageTimedOut);
            } else {
                throw theEx;
            }
        }
    }

    /**
     * @return - whether the walk has been successfully started.
     * @precon - as per invariant
     * @postcon - as per invariant/return
     */
    public boolean isStarted() {
        if (webBrowser != null) {
            if (webBrowser.containsLink(defaultLinkText)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return - the current status of the walk.
     * @precon - as per invariant
     * @postcon - as per invariant/return
     */
    public WebWalkRunner.WalkStatus checkStatus() {
        return walkStatus;
    }

    /**
     * @return - whether the action required has failed or not.
     * @precon - as per invariant
     * @postcon - as per invariant/return
     */
    public boolean hasFailed() {
        if (walkStatus == WalkStatus.successfulStep) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param newStatus - the new status for the walker
     * @precon - as per invariant
     * @postcon - as per invariant
     * @postcon - status is as per the newStatus param
     */
    public void setStatus(WalkStatus newStatus) {
        theLogger.log(Level.INFO, "SetStatus: " + newStatus.toString(),
                newStatus);
        if (newStatus == WalkStatus.successfulStep) {
            failureCount = 0;
        } else {
            ++failureCount;
        }

        walkStatus = newStatus;
    }

    /**
     * checks whether the exception to be examined is a timeout. So this has
     * nothing to do with the state of this object.
     * @param ex - the exception to be examined
     * @return - whether that exception is actually a timeout (usually a socket
     * timeout).
     * @precon - as per invariant
     * @postcon - as per invariant
     * @postcon - no change to internal state.
     */
    private boolean isExceptionTimeout(Exception ex) {
        boolean theResult = false;

        Throwable theCause = ex; //.getCause();
        if (theCause != null) {
            if (theCause instanceof WebDriverException) {
                Throwable theRealCause = theCause.getCause();

                if (theRealCause instanceof SocketTimeoutException) {
                    theResult = true;
                }
            }
        }

        return theResult;
    }

    /**
     *
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    private void initTrail() {
        theTrail = new ArrayList<URL>();
        FileReader theReader = null;

        try {
            theReader = new FileReader(theTrailFileName);
            BufferedReader in = new BufferedReader(theReader);

            String theURLAsString = null;

            while ((theURLAsString = in.readLine()) != null) {
                try {
                    URL theTrailURL = new URL(theURLAsString);
                    theTrail.add(theTrailURL);
                } catch (MalformedURLException ex) {
                    theLogger.log(Level.WARNING, "Failed making URL from{0}", theURLAsString);
                }
            }
        } catch (IOException e) {
            // ...
        } finally {
            if (null != theReader) {
                try {
                    theReader.close();
                } catch (IOException e) {
                    /* .... */
                }
            }
        }

        trailIterator = theTrail.listIterator();
        if (trailIterator.hasNext()) {
            initialURL = trailIterator.next();
        }
    }
    
    
    int getCurrentTrailPos() {
        if (trailIterator != null) {
            int thePos = trailIterator.nextIndex()-1;
            return thePos;
        }

        return 0;
    }

    boolean isAtEnd() {
        if (trailIterator != null) {
            return !trailIterator.hasNext();
        }

        return true;
    }

    boolean isAtStart() {
        if (trailIterator != null) {
            return !trailIterator.hasPrevious();
        }

        return true;
    }

    List<String> getTrailText() {
        List<String> retVal = new ArrayList<String>();
        if (theTrail != null) {
            for (URL theTrailURL : theTrail) {
                retVal.add(theTrailURL.getPath());
            }
        }

        return retVal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebWalkRunner other = (WebWalkRunner) obj;
        
        if ((this.defaultLinkText == null) ? (other.defaultLinkText != null) : !this.defaultLinkText.equals(other.defaultLinkText)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.defaultLinkText != null ? this.defaultLinkText.hashCode() : 0);
        return hash;
    }
}
