package propogation;

import models.Pair;

public interface DirectionalEvaluator {

   public boolean initialEvaluate(Pair pairToEvaluate);
   public void onInitialSuccess(Pair pairToEvaluate);
   public void onInitialFail(Pair pairToEvaluate);
   public boolean evaluate(Pair pairToEvaluate, Pair originatingPair);
   public void onSuccess(Pair pairToEvaluate, Pair originatingPair);
   public void onFail(Pair pairToEvaluate, Pair originatingPair);
}
