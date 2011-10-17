
package trailwebwalk;

import java.net.URL;

/**
 *
 * @author al
 */
public class TrailItem {
    private final String theLabel;
    private final URL theURL;
    private final String theTargetAttribute;    
    private final String theTargetValue;
    private final String theTargetType;
    
    TrailItem(String theLabel,
            URL theURL,
            String theTargetType,
            String theTargetAttribute,
            String theTargetValue){
        this.theLabel = theLabel;
        this.theURL = theURL;
        this.theTargetType = theTargetType;
        this.theTargetAttribute = theTargetAttribute;
        this.theTargetValue = theTargetValue;   
    }

    public String getLabel() {
        return theLabel;
    }

    public String getTargetValue() {
        return theTargetValue;
    }
    
    public String getTargetAttribute() {
        return theTargetAttribute;
    }
    
    public String getTargetType() {
        return theTargetType;
    }

    public URL getURL() {
        return theURL;
    }   
}
