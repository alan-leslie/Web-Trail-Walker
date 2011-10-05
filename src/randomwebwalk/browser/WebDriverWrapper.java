/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package randomwebwalk.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.FirefoxWebElement;
import org.openqa.selenium.firefox.internal.ProfilesIni;

/**
 *
 * @author al
 * Facade for the selenium WebDriver class.
 * The documentation for the commands below is found in the selenium
 * documentation - http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebDriver.html.
 * @invariant WebDriver exists and is valid (a corresponding firefox window
 * exists).
 * Note that all commands are routed through the firefox browser. Nothing is
 * cached.
 * All commands here are synchronised on the WebDriver because it uses a
 * single connection manager, so if you are using multiple threads they must be
 * serialised.
 */
public class WebDriverWrapper {

    private final WebDriver webDriver;
    private final FirefoxDriver ffWebDriver;

    WebDriverWrapper(String profileId) {
        ProfilesIni allProfiles = new ProfilesIni();
        FirefoxProfile theProfile = allProfiles.getProfile(profileId);

        if(theProfile == null){
            ffWebDriver = new FirefoxDriver();
        } else {
            ffWebDriver = new FirefoxDriver(theProfile);
        }
        
        webDriver = ffWebDriver;
     }

    synchronized void quit() {
        webDriver.quit();
    }

    synchronized void stopPageLoad() {
        try{
            Object nullArgs = null;
            Object executeScriptResult = ffWebDriver.executeScript("window.stop()", nullArgs);
            Logger.getLogger(WebDriverWrapper.class.getName()).log(Level.INFO, "page load stopped");
        } catch(UnsupportedOperationException ex) {
            Logger.getLogger(WebDriverWrapper.class.getName()).log(Level.INFO, null, ex);
        }
    }

    synchronized String getCurrentPage() {
        String theResult = "";

        try{
            theResult = webDriver.getCurrentUrl();
        } catch(NullPointerException ex) {
            Logger.getLogger(WebDriverWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return theResult;
    }

    synchronized List<WebElement> getAllHyperLinks() {
        List<WebElement> theResult = null;

        try {
            theResult = webDriver.findElements(By.xpath("/html/body//a[@href]"));
        } catch (NoSuchElementException genExc) {
            System.out.println(genExc.toString());
        }

        if (theResult == null) {
            theResult = new ArrayList<WebElement>();
        }

        return theResult;
    }

    synchronized WebElement findByXPath(String xpath) {
        List<WebElement> theElements = null;
        WebElement theResult = null;

        try {
            theElements = webDriver.findElements(By.xpath(xpath));
        } catch (NoSuchElementException genExc) {
            System.out.println(genExc.toString());
        }

        if (theElements != null
                && theElements.size() > 0) {
            theResult = theElements.get(0);
        }

        return theResult;
    }

    synchronized void goBack() {
        webDriver.navigate().back();
    }

    synchronized void refresh() {
        webDriver.navigate().refresh();
    }

    synchronized void get(String linkIdStr) {
        webDriver.get(linkIdStr);
    }

    synchronized WebElement findByLinkText(String linkText) {
        List<WebElement> theElements = null;
        WebElement theResult = null;

        try {
            theElements = webDriver.findElements(By.linkText(linkText));
        } catch (NoSuchElementException genExc) {
            System.out.println(genExc.toString());
        }

        if (theElements != null
                && theElements.size() > 0) {
            theResult = theElements.get(0);
        }

        return theResult;
    }

    synchronized WebElement findElement(By name) {
        return webDriver.findElement(name);
    }

    synchronized String getTitle() {
        return webDriver.getTitle();
    }

    synchronized String getElementAttribute(WebElement theElement, String string) {
        return theElement.getAttribute(string);
    }

    synchronized void sendKeysToElement(WebElement theElement, String theString) {
        theElement.sendKeys(theString);
    }

    synchronized void clickElement(WebElement theElement) {
        theElement.click();
    }

    synchronized boolean isElementEnabled(WebElement webElement) {
        return webElement.isEnabled();
    }

    synchronized boolean isElementVisible(WebElement webElement) {
        FirefoxWebElement theHTMLUnitWebElement = (FirefoxWebElement)webElement;
        return theHTMLUnitWebElement.isDisplayed();
    }

    synchronized String getElementText(WebElement webElement) {
        return webElement.getText();
    }
}
