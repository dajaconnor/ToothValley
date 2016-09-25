package impl;

import org.springframework.beans.factory.annotation.Autowired;

import models.BodyOfWater;
import models.HexMap;
import models.Pair;

public class WaterService {

   @Autowired
   HexService hexService;

   // returns the body its in, or null if not in a body
   public BodyOfWater inBody(Pair node){

      return HexMap.getInstance().getWaterBodies().get(node);
   }

   public void addToMapWaterBodies(Pair node, BodyOfWater body){

      HexMap.getInstance().getWaterBodies().put(node, body);
   }

   public void removeNode(Pair node){

      HexMap.getInstance().getWaterBodies().remove(node);
   }
}
