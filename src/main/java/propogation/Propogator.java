package propogation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import impl.HexService;
import models.Pair;

//http://stackoverflow.com/questions/2186931/java-pass-method-as-parameter
@Component
public class Propogator {

   @Autowired
   private HexService hexService;

   public void propogate(Evaluator evaluator, Pair startingPair) 
   {
      Set<Pair> traversedPairs = new HashSet<Pair>();
      List<Pair> nextList = new ArrayList<Pair>();
      nextList.add(startingPair);

      while(nextList.size() > 0){

         Set<Pair> neighbors = new HashSet<Pair>();

         for (Pair pair : nextList){

            neighbors.addAll(hexService.getNeighborsSet(pair));
         }

         nextList = new ArrayList<Pair>();

         for (Pair pair : neighbors){

            if (!traversedPairs.contains(pair) && evaluator.actAndEvaluate(pair)){

               nextList.add(pair);
            }
         }
      }
   }
   
   public void propogateFromOrigin(DirectionalEvaluator evaluator, Pair startingPair) 
   {
      Set<Pair> traversedPairs = new HashSet<Pair>();
      
      List<Pair> thisList = new ArrayList<Pair>();
      thisList.add(startingPair);

      while(thisList.size() > 0){

         List<Pair> nextList = new ArrayList<Pair>();

         for (Pair thisPair : thisList){

            List<Pair> neighbors = hexService.getNeighbors(thisPair);
            
            for (Pair neighbor : neighbors){
               
               if (!traversedPairs.contains(neighbor) && evaluator.actAndEvaluate(neighbor, thisPair)){
                  
                  nextList.add(neighbor);
               }
            }
         }
      }
   }
}
