package impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import models.HexMap;
import models.Pair;

public class WaterService {
   
   @Autowired
   HexService hexService;

   // returns the body its in, or null if not in a body
   public Pair inBody(Pair node){
      
      HexMap map = HexMap.getInstance();

      if (!map.getWaterConnectivity().containsKey(node)) return null;

      Pair next = node.clone();

      while (next != map.getWaterConnectivity().get(next)){ // make this safe with a count maybe just in case?

         next = map.getWaterConnectivity().get(next);
      }
   
      return next;
   }
   
   public void addNode(Pair node){
      
      if (inBody(node) != null) return;
      
      HexMap map = HexMap.getInstance();
      List<Pair> neighbors = hexService.getNeighbors(node);
      
      for (Pair neighbor : neighbors){
         
         Pair body = inBody(neighbor);
         
         if (body != null){
            
            map.getWaterConnectivity().put(node, body);
            addToChildMap(node, body);
            return;
         }
      }
      
      map.getWaterConnectivity().put(node, node);
   }

   private void addToChildMap(Pair node, Pair body){

      HexMap map = HexMap.getInstance();
      
      if (!map.getWaterChildren().containsKey(body)) map.getWaterChildren().put(body, new ArrayList<Pair>());

      map.getWaterChildren().get(body).add(node);
   }

   public void removeNode(Pair node){

      // don't remove if it's already not there
      if (inBody(node) == null) return;
      
      HexMap map = HexMap.getInstance();
      map.getWaterConnectivity().remove(node);
     
      if (map.getWaterChildren().containsKey(node)){
         
         for (Pair child : map.getWaterChildren().get(node)){
            
            map.getWaterConnectivity().remove(child);
            addNode(child);
         }
      }
   }
}
