package propogation;

import models.Pair;

// http://stackoverflow.com/questions/2186931/java-pass-method-as-parameter
public interface Evaluator {

   public boolean evaluate(Pair pairToEvaluate);
   public void onSuccess(Pair pairToEvaluate);
   public void onFail(Pair pairToEvaluate);
}
