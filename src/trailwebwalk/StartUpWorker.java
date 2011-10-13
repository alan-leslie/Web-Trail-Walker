/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trailwebwalk;

import java.util.concurrent.Callable;
import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebElement;

/*
 *
 * @author al
 */
public class StartUpWorker implements Callable<Selenium> {
    private final WebDriver webDriver;
    private final String idString;
    private final String passwordString;

    public StartUpWorker(String newIdString,
                         String newPasswordString){
        webDriver = new FirefoxDriver();
        idString = newIdString;
        passwordString = newPasswordString;
    }

    public Selenium call(){
        Selenium selenium = null;
        // TODO - element not found exception - just fail?
        //      - login exception - ditto
        webDriver.get("http://www.stumbleupon.com/login.php");

        WebElement userNameElement = webDriver.findElement(By.name("username"));
        userNameElement.sendKeys(idString);

        WebElement passwordElement = webDriver.findElement(By.name("password"));
        passwordElement.sendKeys(passwordString);

        WebElement loginButtonElement = webDriver.findElement(By.name("login"));
        loginButtonElement.click();

        // Check that the login has succeeded
        System.out.println("Page title is: " + webDriver.getTitle());
        if (webDriver.getTitle().equalsIgnoreCase("StumbleUpon.com: Discover the Best of the Web")) {
            selenium = new WebDriverBackedSelenium(webDriver, "http://www.stumbleupon.com/");
            selenium.open("/to/stumble/go/");
        }
        
        return selenium;
    }
}
