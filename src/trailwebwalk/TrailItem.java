
package trailwebwalk;

import java.net.URL;

/**
 *
 * @author al
 */
public class TrailItem {
    private String theLabel;
    private URL theURL;
    private String theTarget;
    
    TrailItem(String theLabel,
            URL theURL,
            String theTarget){
        this.theLabel = theLabel;
        this.theURL = theURL;
        this.theTarget = theTarget;
    }

    public String getLabel() {
        return theLabel;
    }

    public String getTarget() {
        return theTarget;
    }

    public URL getURL() {
        return theURL;
    }   
}
