
package trailwebwalk;

import java.util.concurrent.Callable;

/**
 *
 * @author al
 */
public class StartWorker implements Callable<Boolean> {
    private final WebWalkRunner theRunner;

    public StartWorker(WebWalkRunner theRunner) {
        this.theRunner = theRunner;
    }

    public Boolean call() {
        Boolean result = true;
        theRunner.startUp();

        return result;
    }   
}
