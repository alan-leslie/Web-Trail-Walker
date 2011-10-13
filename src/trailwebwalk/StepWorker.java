package trailwebwalk;

import java.util.concurrent.Callable;

/**
 *
 * @author al
 */
public class StepWorker implements Callable<Boolean> {
    private final WebWalkRunner theRunner;

    public StepWorker(WebWalkRunner theRunner) {
        this.theRunner = theRunner;
    }

    public Boolean call() {
        Boolean result = true;

        try {
            theRunner.step();
        } catch (Exception exc) {
            System.out.println(exc.toString());
            result = false;
        }

        return result;
    }
}
