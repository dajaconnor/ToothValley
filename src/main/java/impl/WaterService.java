package impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.Direction;
import enums.DisplayType;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.TheRandom;

@Component
public class WaterService {

   @Autowired
   HexService hexService;
   
   @Autowired
   private HexMapService hexMapService;
   
   HexMap map = HexMap.getInstance();

   public void waterCycle(boolean findLeak, boolean leakFound, int ticks) {

      int totalWater = 0;
      int totalElevation = 0;

      List<Pair> allHexes = new ArrayList<Pair>(map.getHexes().keySet());

      // Attempt to alter wind direction
      TheRandom rand = TheRandom.getInstance();
      switch (rand.get().nextInt(Environment.WIND_CHANGE)) {

      case 0:
         map.setWindDirection(-1);
         break;

      case 1: // Just toggles wind
         map.setWindDirection(0);
         break;

      case 2:
         map.setWindDirection(1);
         break;

      default:

      }

      if (findLeak) {

         totalWater = hexMapService.allWater()[0];
      }

      rain(findLeak);

      if (findLeak && totalWater != hexMapService.allWater()[0] && !leakFound) {

         System.out.println("blow leak");
         leakFound = true;
      }

      Map<Pair,Pair> displayMap = new HashMap<Pair,Pair>();
      DisplayType displayType = map.getDisplayType();

      for (Pair hexID : allHexes) {

         Hex hex = map.getHex(hexID);

         hexService.evaporate(hex, findLeak, leakFound);
         hexService.flood(hex, findLeak);
         hexService.topple(hexID, 0);

         Pair displayPair = map.updateHexDisplay(hex, displayType);

         displayMap.put(hexID, displayPair);
         
         if (ticks % Environment.NORMALIZE_EVEL_FREQ == 0){
        	 
        	 totalElevation += hex.getElevation();
         }
      }

      if (ticks % Environment.NORMALIZE_EVEL_FREQ == 0){
    	  
    	  hexMapService.normalizeElevation(allHexes ,totalElevation);
      }
      
      map.setDisplayMap(displayMap);

      if (findLeak && totalWater != hexMapService.allWater()[0] && !leakFound) {

         System.out.println("evaporate or flood leak");
         leakFound = true;
      }
   }
   
   /**
    * If there is any moisture in air of current hex, it all moves to the
    * adjacent hex with the least moisture
    * 
    * @param hex
    * @return the hex blown to, or null if it failed to blow
    */
   private void rain(boolean findLeak) {

      List<Pair> cloudsMoving = new ArrayList<Pair>();
      
      for (Pair cloud : map.getCloudOrder()) {

         if (map.getClouds().containsKey(cloud)) {

            int amount = map.getCloudValue(cloud);
            
            if (map.getHex(cloud).getStandingWater() == 0){
               
               amount *= 200;
            }
            
            if (rainSingle(map.getHex(cloud), amount)) {

               map.getClouds().put(cloud, map.getCloudValue(cloud) + 1);
            } else {

               map.getClouds().remove(cloud);
            }
            
         }

         if (map.getClouds().containsKey(cloud)) {

            for (Direction direction : Direction.VALUES) {

               Pair pair = cloud.getHexIdFromDirection(direction);
               map.removeCloud(pair);
               int cloudElevation = map.getHex(cloud).getElevation();

               if (map.getClouds().containsKey(cloud) && blowSingleHex(pair, cloud, cloudElevation, findLeak)) {

                  rainCorner(direction, pair, cloudElevation, findLeak);
               }
            }
         }
         
         cloudsMoving.add(cloud);
      }
      
      for (Pair cloud : cloudsMoving){
         
         Integer movement = map.getClouds().get(cloud);
         
         if (movement != null && TheRandom.getInstance().get().nextInt(movement) == 1){
         
            Pair moveTo = map.getHighestNeighbor(cloud);
            map.moveCloud(cloud, moveTo);
         }
      }

      map.reorderClouds();
   }

   private void rainCorner(Direction direction, Pair currentPair, int cloudElevation, boolean findLeak) {

      List<Pair> layer = new ArrayList<Pair>();
      layer.add(currentPair);

      while (layer.size() > 0) {

         List<Pair> nextLayer = new ArrayList<Pair>();

         for (int n = 0; n < layer.size(); n++) {

            if (n == 0) {

               Pair pair = layer.get(n).getHexIdFromDirection(direction);
               map.removeCloud(pair);

               if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)) {

                  nextLayer.add(pair);
               }
            }

            Pair pair = layer.get(n).getHexIdFromDirection(direction.takeRandomTurn());
            map.removeCloud(pair);

            if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)) {

               nextLayer.add(pair);
            }
         }

         layer = nextLayer;
      }
   }

   private boolean blowSingleHex(Pair from, Pair to, int cloudElevation, boolean findLeak) {

      int total = 0;
      Hex fromHex = map.getHex(from);
      Hex toHex = map.getHex(to);

      int strangeBehaviorCount = 0;

      while (fromHex == null || toHex == null) {

         try {
            Thread.sleep(50);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         fromHex = map.getHex(from);
         toHex = map.getHex(to);
         strangeBehaviorCount++;
      }

      if (strangeBehaviorCount > 0) {
         System.out.println("Had to retry getting hex " + strangeBehaviorCount + "times");
      }

      if (findLeak) {
         total = fromHex.getMoistureInAir() + toHex.getMoistureInAir();
      }

      int moisturetoMove = Math.abs(cloudElevation - fromHex.getElevation()) + fromHex.getMoistureInAir()
      + Environment.WIND_POWER - toHex.getMoistureInAir();

      if (moisturetoMove < 0) { // negative wind, to becomes from, etc

         fromHex.alterMoistureInAir(toHex.alterMoistureInAir(moisturetoMove));
         return false;
      }

      boolean returnBool = (toHex.alterMoistureInAir(fromHex.alterMoistureInAir(-moisturetoMove)) == moisturetoMove);

      if (findLeak && fromHex.getMoistureInAir() + toHex.getMoistureInAir() != total) {

         System.out.println("blowSingleHex leak");
      }

      return returnBool;
   }

   private boolean rainSingle(Hex hex, int amount) {

      int changed = 0;

      changed = hex.alterMoistureInAir(-Math.abs(amount));
      
      map.alterMoisture(hex, changed);

      return changed == amount;
   }
}
