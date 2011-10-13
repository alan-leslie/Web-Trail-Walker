package trailwebwalk.browser;

import org.openqa.selenium.WebElement;

/**
 *
 * @author al
 * Wrapper for selenium web element used to cache data that otherwise would
 * have to be obtained through the web driver each time.
 * @invariant - WebElement is valid (thus requires the invariants of the
 * WebDriver class and also that the browser has not moved from the page
 * that contains this link).
 * @invariant - the id string matches the href of this element
 */
public class Hyperlink {

    WebDriverWrapper webDriver = null;
    WebElement webElement = null;
    String theLinkIdStr = null;
    String theLinkText = null;

    /**
     * @param newDriver - correctly initialised WebDriver
     * @param newElement - WebElement obtained from the page that the
     * WebDriver browser is pointing to.
     * @postcon - as per invariant
     */
    Hyperlink(WebDriverWrapper newDriver,
            WebElement newElement) {
        webDriver = newDriver;
        webElement = newElement;

        theLinkIdStr = webDriver.getElementAttribute(webElement, "href");
        theLinkText = webDriver.getElementText(webElement);
        // todo - use this to disallow edit/login etc
    }

    /**
     *
     * @return whether the link is visitable (can be seen on the current web
     * page, is the correct protocol and is not a subsection of the current
     * page).
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    boolean canBeFollowed() {
        boolean linkIncludesProtocol = theLinkIdStr.contains(":/");
        boolean linkToOtherSection = theLinkIdStr.contains(":") && !linkIncludesProtocol;

        if (webDriver.isElementVisible(webElement)) {
            if (webDriver.isElementEnabled(webElement)) {
                if (!(theLinkIdStr.contains("#")
                        || linkToOtherSection)) {
                    return true;
                }
            }
        } else {
            System.out.println("Element not displayed");
        }

        return false;
    }

    /**
     * 
     * @return - the associated selenium WebElement
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    
    WebElement getElement() {
        return webElement;
    }

    /**
     * 
     * @return - the URL that this link is pointing to.
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    String getHref() {
        return theLinkIdStr;
    }

    /**
     *
     * @return - the text displayed for this link.
     * @precon - as per invariant
     * @postcon - as per invariant/return value
     */
    String getText() {
        return theLinkText;
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
        final Hyperlink other = (Hyperlink) obj;
        if ((this.theLinkIdStr == null) ? (other.theLinkIdStr != null) : !this.theLinkIdStr.equals(other.theLinkIdStr)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.theLinkIdStr != null ? this.theLinkIdStr.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Hyperlink{" + "theLinkIdStr=" + theLinkIdStr + '}';
    }

}
