package randomwebwalk;

import java.util.concurrent.Callable;

/**
 *
 * @author al
 */
public class StepWorker implements Callable<Boolean> {
    private final RandomWebWalkRunner theRunner;

    public StepWorker(RandomWebWalkRunner theRunner) {
        this.theRunner = theRunner;
    }

    public Boolean call() {
        Boolean result = true;

        try {
            theRunner.step();
        } catch (Exception exc) {
            System.out.println(exc.toString());
            result = false;
            // TODO - if its a timeout then stop it and carry on
            // otherwise??
            // TODO - if stumble not found refresh
        }

        return result;
    }
}
