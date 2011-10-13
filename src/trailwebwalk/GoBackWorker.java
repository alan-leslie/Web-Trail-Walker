package trailwebwalk;

import java.util.concurrent.Callable;

/**
 *
 * @author al
 */
public class GoBackWorker implements Callable<Boolean> {
    private final WebWalkRunner theRunner;

    public GoBackWorker(WebWalkRunner theRunner) {
        this.theRunner = theRunner;
    }

    public Boolean call() {
        Boolean result = true;
        try {
            theRunner.goBack();
        } catch (Exception exc) {
            System.out.println(exc.toString());
            result = false;
        }

        return result;
    }
}
