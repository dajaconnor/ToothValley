package propogation;

import models.Pair;

// http://stackoverflow.com/questions/2186931/java-pass-method-as-parameter
public interface Evaluator {

   public boolean actAndEvaluate(Pair pairToEvaluate);
}
