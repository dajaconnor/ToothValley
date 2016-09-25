package propogation;

import models.Pair;

public interface DirectionalEvaluator {

   public boolean actAndEvaluate(Pair pairToEvaluate, Pair originatingPair);
}
