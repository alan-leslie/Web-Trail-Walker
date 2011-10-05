package randomwebwalk.browser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author al
 * Wrapper for selenium web driver to represent the (usually firefox) browser
 * that the WebDriver is connected to.
 * Used to cache data that otherwise would have to be obtained through
 * the web driver each time.
 * Contains a list of the pages (URLs) already successfully visited.
 * @invariant - WebDriver is valid (this requires the invariants of the
 * WebDriver class) or (after quit) is null.
 * @invariant - the Logger is a valid logger.
 */
public class Browser {

    private WebDriverWrapper webDriver;
    private List<Page> pageList = new ArrayList<Page>();
    private final Logger theLogger;
    private final int HISTORY_LIMIT = 15;

    /**
     *
     * @param profileId
     * @param newLogger
     */
    public Browser(String profileId,
            Logger newLogger) {
        webDriver = new WebDriverWrapper(profileId);
        theLogger = newLogger;
    }

    /**
     *
     * @return - whether the instance of this browser is valid (whether the
     * attached web driver is valid).
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public boolean isAlive() {
        String theCurrentPage = "";

        if (webDriver == null) {
            return false;
        } else {
            try {
                theCurrentPage = webDriver.getCurrentPage();
            } catch (Exception theEx) {
                // nothing required
            }
        }

        if (theCurrentPage.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     *
     * @param linkText - the clickable text (e.g. 'random article' or 'stumble!').
     * @return - whether the clickable text appears on the current page
     * @precon - as per invariant/param spec
     * @postcon - as per invariant/return value
     */
    public boolean containsLink(String linkText) {
        if (webDriver != null) {
            try {
                WebElement theLink = webDriver.findByLinkText(linkText);
                if (theLink != null) {
                    return true;
                }
            } catch (Exception theEx) {
                // nothing required
            }
        }

        return false;
    }

    /**
     * 
     * @param initialURL - the URL that is the login page must be non-null
     * @param isStumbleUpon - whether the required functionality is stumble upon
     * or not.
     * @param idString - the user id
     * @param passwordString - the corresponding password.
     * @precon - as per invariant/param spec
     * @postcon - as per invariant/return value
     * @throws LoginException - if the login information is incorrect
     */
    public void start(URL initialURL,
            boolean isStumbleUpon,
            String idString,
            String passwordString) throws LoginException {
        if (isStumbleUpon) {
            webDriver.get(initialURL.toString());

            WebElement userNameElement = findElement(By.name("username"));
            webDriver.sendKeysToElement(userNameElement, idString);

            WebElement passwordElement = findElement(By.name("password"));
            webDriver.sendKeysToElement(passwordElement, passwordString);

            WebElement loginButtonElement = findElement(By.name("login"));
            webDriver.clickElement(loginButtonElement);

            theLogger.log(Level.INFO, "Page title is: {0}", getPageTitle());
            if (getPageTitle().equalsIgnoreCase("StumbleUpon.com: Discover the Best of the Web")) {
                gotoURL("www.stumbleupon.com/to/stumble/go/");
            } else {
                javax.security.auth.login.LoginException theException = new LoginException();
                throw theException;
            }
        } else {
            gotoURL(initialURL.toString());
        }
    }

    /**
     * 
     * @return - the last visited page from cache or null if none has been
     * visited (after the start login pages).
     * @precon - as per invariant spec
     * @postcon - as per invariant/return value
     */
    public Page getCurrentPage() {
        if (pageList.isEmpty()) {
            return null;
        }

        Page theCurrentPage = getLastPage();

        return theCurrentPage;
    }

    /**
     * 
     * @precon - as per invariant spec
     * @postcon - moved back to the last succesfully visited page
     * @postcon -as per invariant
     */
    public void goBack() {
        webDriver.goBack();

        if (hasPageMoved()) {
            addNewPage();
        }
    }

    /**
     *
     * @precon - as per invariant spec
     * @postcon -refresh the page that is current in the firefox browser (note
     * that this page may not be the one returned by getCurrentPage if it was
     * not successfully visited.
     * @postcon -as per invariant
     */
    public void refresh() {
        webDriver.refresh();

        if (hasPageMoved()) {
            addNewPage();
        }
    }

    /**
     *
     * @precon - as per invariant spec
     * @postcon - the associated web driver and firefox browser are killed.
     * @postcon -as per invariant
     */
    public void quit() {
        webDriver.quit();
        webDriver = null;
    }

    /**
     *
     * @param theLink a valid (non-null and contained in the current page)
     * hyperlink.
     * @precon as per param spec.
     * @postcon that the link has been followed and a new page has been added
     * to the visited pages list.
     * @postcon -as per invariant
     */
    public void goForward(Hyperlink theLink) {
        WebElement theElement = theLink.getElement();
        webDriver.clickElement(theElement);

        addNewPage();
    }

    /**
     *
     * @return - the URL of the page that the associated firefox browser is
     * pointing to (note that this is not a cache value).
     * @precon - as per invariant spec
     * @postcon -as per invariant/return value.
     */
    public String getCurrentPageURL() {
        return webDriver.getCurrentPage();
    }

    /**
     *
     * @precon - as per invariant spec
     * @postcon -that the current page is added to the visited list.
     * @postcon -as per invariant.
     */
    public void addNewPage() {
        Page theNewPage = new Page(webDriver, theLogger);
        pageList.add(theNewPage);
    }

    /**
     * Stop the load of the current page.
     * @precon - as per invariant spec
     * @postcon -that the loading of the page in the browser is stopped.
     * @postcon -the current page is not added to visited list.
     * @postcon -as per invariant.
     */
    public void stopPageLoad() {
        webDriver.stopPageLoad();
    }

    /**
     * Restore the browser to the last page successfully visited.
     * @precon - as per invariant
     * @postcon -the browser is pointing to the last page successfully visited.
     * @postcon -as per invariant.
     */
    public void restorePage() {
        Page theLastPage = getLastPage();
        String theLastKnownURL = theLastPage.getURL();
        webDriver.get(theLastKnownURL);
    }

    /**
     *
     * @param theNewURL - a valid URL.
     * @precon - as per invariant/param.
     * @postcon -the browser is pointing to the page represented by the URL
     * param.
     * @postcon -as per invariant.
     */
    public void gotoURL(String theNewURL) {
        webDriver.get(theNewURL);
        addNewPage();
    }

    /**
     *
     * @return - whether the current page in the browser is different from
     * the last one successfully visited.
     * @precon - as per invariant.
     * @postcon -as per invariant.
     */
    public boolean hasPageMoved() {
        String storedCurrentPageURL = getCurrentPageURL();
        Page currentPage = getCurrentPage();
        boolean theResult = false;

        if (currentPage != null) {
            String realCurrentPageURL = currentPage.getURL();
            if (!storedCurrentPageURL.equalsIgnoreCase(realCurrentPageURL)) {
                theLogger.log(Level.INFO, "Moved to new page: {0}",
                        realCurrentPageURL);
                theLogger.log(Level.INFO, "from page: {0}",
                        storedCurrentPageURL);
                theResult = true;
            }
        }

        return theResult;
    }
    
    /**
     * @param link - a valid hyperlink
     * @return - whether the URL referred to in the link has recently been
     * visited.
     * @precon - as per invariant.
     * @postcon -as per invariant/return spec.
     */

    public boolean hasAlreadyBeenVisited(Hyperlink link) {
        boolean isFound = false;
        theLogger.log(Level.INFO, "Checking the link {0}", link.theLinkIdStr);

        if (!pageList.isEmpty()) {
            String theLinkHref = link.getHref();

            if(theLinkHref.indexOf('/') == 0){
                String theCurrentPagesURL = getCurrentPage().getURL();
                try {
                    URL theURL = new URL(theCurrentPagesURL);
                    String webSite = theURL.getProtocol() + "://";
                    webSite += theURL.getHost();
                    theLinkHref = webSite + theLinkHref;
                } catch (MalformedURLException ex) {
                    // no action taken here because there is nothing that can
                    // usefully be done
                }
            }

            ListIterator<Page> iter = pageList.listIterator(pageList.size());
            int i = 0;

            while (iter.hasPrevious()
                    && !isFound
                    && i < HISTORY_LIMIT) {
                Page thePage = iter.previous();
                ++i;
//                theLogger.log(Level.INFO, "The page URL is {0}", thePage.getURL());
                if (thePage.getURL().equalsIgnoreCase(link.theLinkIdStr)) {
                    theLogger.log(Level.INFO, "The link {0}already visited", link.theLinkIdStr);
                    isFound = true;
                }
            }
        }

        return isFound;
    }

    /**
     * @param name - the element spec
     * @return - the required element or null
     * @precon - as per invariant/param spec.
     * @postcon -as per invariant/return spec.
     */
    private WebElement findElement(By name) {
        WebElement theResult = webDriver.findElement(name);
        return theResult;
    }

    /**
     * @return - the title of the page that the attached browser is pointing to.
     * @precon - as per invariant/param spec.
     * @postcon -as per invariant/return spec.
     */
    private String getPageTitle() {
        String theResult = webDriver.getTitle();
        return theResult;
    }

    /**
     * @return - the last successfully visited page.
     * @precon - as per invariant.
     * @postcon -as per invariant/return spec.
     */
    private Page getLastPage() {
        int index = pageList.size() - 1;
        Page theResult = null;

        if (index >= 0) {
            theResult = pageList.get(index);
        }

        return theResult;
    }
}
