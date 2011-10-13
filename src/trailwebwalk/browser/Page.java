package trailwebwalk.browser;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author al
 * Wrapper for selenium web driver to represent a page that the WebDriver has
 * visited. Used to cache data that otherwise would have to be obtained through 
 * the web driver each time.
 * @invariant - WebDriver is valid (this requires the invariants of the
 * WebDriver class and also that the browser has not moved from this page).
 * @invariant - the URL string matches the page's URL
 */

public class Page {
    private final WebDriverWrapper webDriver;
    private List<WebElement> theLinks = null;
    private final String theURL;
    private static final int LINK_THRESHHOLD = 5;
    private static final int RETRY_COUNT = 10;
    private final Logger theLogger;

    /**
     * @param newDriver - valid WebDriver
     */
    Page(WebDriverWrapper newDriver,
         Logger newLogger) {
        theLogger = newLogger;
        webDriver = newDriver;
        theURL = webDriver.getCurrentPage();
    }

    /**
     * 
     * @return a valid not editing hyperlink from the current page
     * the hyperilink is random generated so it should be a different link for
     * each call
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public Hyperlink getRandomLink() {
        Hyperlink theResult = null;
        Random generator = new Random();
        int i = 0;

        theLinks = webDriver.getAllHyperLinks();

        if (theLinks.size() < LINK_THRESHHOLD) {
            Logger.getLogger(Page.class.getName()).log(Level.INFO, "Links size: {0}", Integer.toString(theLinks.size()));
            Logger.getLogger(Page.class.getName()).log(Level.INFO, "The URL: {0}", theURL);
        }

        while (theResult == null &&
                i < RETRY_COUNT) {
            int randomElementIndex = generator.nextInt(theLinks.size());
            WebElement randomElement = theLinks.get(randomElementIndex);
            Hyperlink tmpLink = new Hyperlink(webDriver, randomElement);

            if (tmpLink.canBeFollowed()) {
                String theLinkText = randomElement.getText();

                if(!(theLinkText.equals("edit") ||
                        theLinkText.equalsIgnoreCase("log in"))) {
                    //System.out.println("Link id is:" + linkIdStr);
                    theResult = tmpLink;
                }
            }
        }

        if(i >= RETRY_COUNT){
            theLogger.log(Level.INFO, "Out of retries in Page.GetRandomLink");
            // Exception will be generated when attempting to follow link
        }

        return theResult;
    }

    /**
     * 
     * @param theLinkText - the text of the link as it appears on the web page,
     * for example 'random article', 'stumble!'.
     * @return
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public Hyperlink getLinkFromText(String theLinkText) {
        Hyperlink theResult = null;
        WebElement randomElement = null;

        randomElement = webDriver.findByLinkText(theLinkText);

        if (randomElement == null) {
            theLogger.log(Level.INFO, "Default link id:{0} not found.", theLinkText);
        } else {
            theResult = new Hyperlink(webDriver, randomElement);
        }

        return theResult;
    }

    /**
     * 
     * @param theLinkId 
     * @return
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public Hyperlink getLinkFromId(String theLinkId) {
        Hyperlink theResult = null;
        WebElement randomElement = null;

        randomElement = webDriver.findElement(By.id(theLinkId));

        if (randomElement == null) {
            theLogger.log(Level.INFO, "Link id:{0} not found.", theLinkId);
        } else {
            theResult = new Hyperlink(webDriver, randomElement);
        }

        return theResult;
    }
    
    /**
     * 
     * @return - whether the header definition states that the page is in
     * english.
     * note that it looks at the tag in HTML but search is case
     * sensitive.
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public boolean isInEnglish() {
        WebElement mainHtml = webDriver.findByXPath("/html");

        if (mainHtml != null) {
            String langCode = webDriver.getElementAttribute(mainHtml, "lang");
            String langPrefix = "";

            if (langCode != null && langCode.length() > 1) {
                langPrefix = langCode.substring(0, 2);
                if (langPrefix.equalsIgnoreCase("en")) {
                    return true;
                } else {
                    return false;
                }
            }

            langCode = webDriver.getElementAttribute(mainHtml, "xml:lang");
            if (langCode != null && langCode.length() > 1) {
                langPrefix = langCode.substring(0, 2);
                if (langPrefix.equalsIgnoreCase("en")) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        WebElement languageMetaLink = webDriver.findByXPath("/html/head/meta[contains(@http-equiv,'Content-Language')]");

        if (languageMetaLink != null) {
            String metaLangCode = webDriver.getElementAttribute(languageMetaLink, "content");
            String langPrefix = "";
            if (metaLangCode != null) {
                if (metaLangCode.length() > 1) {
                    langPrefix = metaLangCode.substring(0, 2);
                }
                if (langPrefix.equalsIgnoreCase("en")) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        languageMetaLink = webDriver.findByXPath("/html/head/meta[contains(@http-equiv,'content-language')]");

        if (languageMetaLink != null) {
            String metaLangCode = webDriver.getElementAttribute(languageMetaLink, "content");
            String langPrefix = "";
            if (metaLangCode != null) {
                if (metaLangCode.length() > 1) {
                    langPrefix = metaLangCode.substring(0, 2);
                }
                if (langPrefix.equalsIgnoreCase("en")) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        WebElement charsetMetaLink = webDriver.findByXPath("/html/head/meta[contains(@http-equiv,'Content-Type')]");

        if (charsetMetaLink != null) {
            String metaCharsetCode = webDriver.getElementAttribute(charsetMetaLink, "content");
            if (metaCharsetCode != null) {
                if (metaCharsetCode.contains("utf-8")
                        || metaCharsetCode.contains("UTF-8")
                        || metaCharsetCode.contains("iso-8859-1")
                        || metaCharsetCode.contains("ISO-8859-1")){
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     *
     * @return - whether the number of out links is greater that the threshhold
     * LINK_THRESHHOLD.
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public boolean isDeadEnd() {
        if (theLinks == null) {
            theLinks = webDriver.getAllHyperLinks();
        }

        if (theLinks.size() < LINK_THRESHHOLD) {
            return true;
        }

        return false;
    }

    /**
     * 
     * @return - the URL that corresponds to this page (cached).
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    public String getURL() {
        return theURL;
    }

    // standard overrides
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Page other = (Page) obj;
        if ((this.theURL == null) ? (other.theURL != null) : !this.theURL.equals(other.theURL)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.theURL != null ? this.theURL.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Page{" + "theURL=" + theURL + '}';
    }
}
