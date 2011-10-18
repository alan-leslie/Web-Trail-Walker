
package trailwebwalk;

import java.util.concurrent.Callable;

/**
 *
 * @author al
 */
public class StepToWorker implements Callable<Boolean> {
    private final WebWalkRunner theRunner;
    private final int newIndex;

    public StepToWorker(WebWalkRunner theRunner,
            int theNewIndex) {
        this.theRunner = theRunner;
        this.newIndex = theNewIndex;
    }

    public Boolean call() {
        Boolean result = true;

        try {
            theRunner.stepTo(newIndex);
        } catch (Exception exc) {
            System.out.println(exc.toString());
            result = false;
        }

        return result;
    }   
}
