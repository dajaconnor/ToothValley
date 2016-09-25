package impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.Direction;
import models.BodyOfWater;
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
   
   public void bodyOfWaterCycle(){
      
      HexMap map = HexMap.getInstance();
      
      Set<BodyOfWater> bodies = map.getAllWaterBodies();
      List<BodyOfWater> removeTheseBodies = new ArrayList<BodyOfWater>();
      List<Pair> orphans = new ArrayList<Pair>();
      
      for (BodyOfWater body : bodies){
         
         evaporateBody(body);
         
         orphans.addAll( body.handleWaterLineChanges() );
         
         if (body.getAllMembers().size() == 0) removeTheseBodies.add(body);
      }
      
      // cleanup dead bodies
      map.getAllWaterBodies().removeAll(removeTheseBodies);
      handleOrphans(orphans);
   }
   
   private void evaporateBody(BodyOfWater body) {
      
      Integer point = body.getExtremePoint(Environment.LOWEST);
      HexMap map = HexMap.getInstance();
      
      int size = body.getElevationMap().get(point).size();
      int bodySize = body.getAllMembers().size();
      
      for (Pair pair : body.getElevationMap().get(point)){
         
         int adjusted = body.adjustWater((int)-((bodySize * Environment.BODY_EVAPORATION)/size));
         map.getHex(pair).alterMoistureInAir(adjusted);
      }
   }

   private void handleOrphans(List<Pair> orphans){
      
      for (Pair orphan : orphans){
         
         if (inBody(orphan) == null){
            
            createBodyFromHex(orphan);
         }
      }
   }

   // returns the body its in, or null if not in a body
   public BodyOfWater inBody(Pair node){

      return HexMap.getInstance().getPairToWaterBodies().get(node);
   }

   public void addToMapWaterBodies(Pair node, BodyOfWater body){

      HexMap map = HexMap.getInstance();
      
      BodyOfWater conflictingBody = inBody(node);
      
      if (conflictingBody != null){
         
         List<BodyOfWater> joinList = new ArrayList<BodyOfWater>();
         joinList.add(conflictingBody);
         joinList.add(body);
         
         map.getBodiesThatNeedToBeJoined().add(joinList);
      }
      
      map.getPairToWaterBodies().put(node, body);
   }

   public void removeNode(Pair node){

      HexMap map = HexMap.getInstance();
      BodyOfWater body = map.getPairToWaterBodies().get(node);

      map.getPairToWaterBodies().remove(node);

      if (body.getAllMembers().size() == 0){
         map.getAllWaterBodies().remove(body);
      }
   }
   
   public BodyOfWater createBodyFromHex(Pair pair){
      
      HexMap map = HexMap.getInstance();
      BodyOfWater body = null;
      
      if (inBody(pair) == null && map.getHex(pair).getStandingWater(0) > Environment.WATER_BODY_MIN){
         
         body = new BodyOfWater(pair);
      }
      
      if (body != null){
         
         map.getAllWaterBodies().add(body);
      }
      
      return body;
   }
   
   public Set<BodyOfWater> handleMergingBodies(){
      
      HexMap map = HexMap.getInstance();
      Set<BodyOfWater> destroyThese = new HashSet<BodyOfWater>();
      
      for (List<BodyOfWater> toBeJoined : map.getBodiesThatNeedToBeJoined()){
         
         if (toBeJoined.size() == 2 
               && !destroyThese.contains(toBeJoined.get(0)) 
               && !destroyThese.contains(toBeJoined.get(1))){
         
            destroyThese.add(joinBodies(toBeJoined.get(0), toBeJoined.get(1)));
         }
      }
      
      map.resetBodiesThatNeedToBeJoined();
      return destroyThese;
   }
   
   // Returns the one that needs to be destroyed
   private BodyOfWater joinBodies(BodyOfWater body, BodyOfWater otherBody){
      
      BodyOfWater biggerOne = body;
      BodyOfWater smaller = otherBody;
      
      if (body.getAllMembers().size() < otherBody.getAllMembers().size()){
         
         biggerOne = otherBody;
         smaller = body;
      }
      
      biggerOne.mergeInOtherBody(smaller);
      
      return smaller;
   }
   
   /*
    * Old water system
    */
   
   public void waterCycle(boolean findLeak, boolean leakFound) {

      HexMap map = HexMap.getInstance();
      int totalWater = 0;

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

      blow(findLeak);

      if (findLeak && totalWater != hexMapService.allWater()[0] && !leakFound) {

         System.out.println("blow leak");
         leakFound = true;
      }

      /*
       * hexService.rainAll();
       * 
       * if (findLeak && totalWater != hexMapService.allWater()[0] && !leak){
       * 
       * System.out.println("rain leak"); leak = true; }
       */

      map.setUpdatingMap(true);

      for (Pair hexID : allHexes) {

         if (hexID != null && inBody(hexID) == null) {

            Hex hex = map.getHex(hexID);

            hexService.evaporate(hex, findLeak, leakFound);
            hexService.flood(hex, findLeak, map.getStaleHexBodyStandingWater(hex));
            hexService.topple(hexID, 0);

            map.updateHexDisplay(hex);

         }
      }

      map.setUpdatingMap(false);

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
   public void blow(boolean findLeak) {

      HexMap map = HexMap.getInstance();

      for (Pair cloud : map.getCloudOrder()) {

         if (map.getClouds().containsKey(cloud)) {

            if (rainSingle(map.getHex(cloud), map.getClouds().get(cloud))) {

               map.getClouds().put(cloud, map.getClouds().get(cloud) + 1);
            } else {

               map.getClouds().remove(cloud);
            }
         }

         if (map.getClouds().containsKey(cloud)) {

            for (Direction direction : Direction.VALUES) {

               Pair pair = hexService.getHexIdFromDirection(cloud, direction);
               map.removeCloud(pair);
               int cloudElevation = map.getHex(cloud).getElevation();

               if (map.getClouds().containsKey(cloud) && blowSingleHex(pair, cloud, cloudElevation, findLeak)) {

                  blowCorner(direction, pair, cloudElevation, findLeak);
               }
            }
         }
      }

      map.reorderClouds();
   }

   private void blowCorner(Direction direction, Pair currentPair, int cloudElevation, boolean findLeak) {

      List<Pair> layer = new ArrayList<Pair>();
      HexMap map = HexMap.getInstance();
      layer.add(currentPair);

      while (layer.size() > 0) {

         List<Pair> nextLayer = new ArrayList<Pair>();

         for (int n = 0; n < layer.size(); n++) {

            if (n == 0) {

               Pair pair = hexService.getHexIdFromDirection(layer.get(n), direction);
               map.removeCloud(pair);

               if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)) {

                  nextLayer.add(pair);
               }
            }

            Pair pair = hexService.getHexIdFromDirection(layer.get(n), direction.turnRight());
            map.removeCloud(pair);

            if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)) {

               nextLayer.add(pair);
            }
         }

         layer = nextLayer;
      }
   }

   private boolean blowSingleHex(Pair from, Pair to, int cloudElevation, boolean findLeak) {

      HexMap map = HexMap.getInstance();

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

      int changed = hex.alterMoistureInAir(-Math.abs(amount));
      
      HexMap.getInstance().alterMoisture(hex, changed);

      return changed == amount;
   }
}
