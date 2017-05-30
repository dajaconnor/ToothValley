package impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.Direction;
import enums.DisplayType;
import models.BodyOfWater;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.PairOfBodies;
import models.TheRandom;
import propogation.Evaluator;
import propogation.Propogator;

@Component
public class WaterService {

   @Autowired
   HexService hexService;
   
   @Autowired
   private HexMapService hexMapService;
   
   HexMap map = HexMap.getInstance();
   
   public void bodyOfWaterCycle(){

      Set<BodyOfWater> bodies = map.getAllWaterBodies();
      List<Pair> orphans = new ArrayList<Pair>();
      
      if (!bodies.isEmpty()){
         for (BodyOfWater body : bodies){
            
            evaporateBody(body);
            
            orphans.addAll( handleWaterLineChanges(body) );
            
            if (body.getAllMembers().size() == 0) map.getWaterBodiesToBeRemoved().add(body);
         }

         handleMergingBodies();

         // cleanup dead bodies
         map.getAllWaterBodies().removeAll(map.getWaterBodiesToBeRemoved());
         map.resetWaterBodiesToBeRemoved();
         handleOrphans(orphans);
      }
   }
   
   private void evaporateBody(BodyOfWater body) {
      
      Integer point = body.getExtremePoint(Environment.LOWEST);
      
      int size = body.getElevationMap().get(point).size();
      int bodySize = body.getAllMembers().size();
      
      for (Pair pair : body.getElevationMap().get(point)){
         
         int adjusted = body.adjustTotalWater((int)-((bodySize * Environment.BODY_EVAPORATION)/size));
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

      return map.getPairToWaterBodies().get(node);
   }

   private void addToMapWaterBodies(Pair node, BodyOfWater body){

      BodyOfWater conflictingBody = inBody(node);
      
      if (conflictingBody != null && !body.equals(conflictingBody)){
         
         map.getBodiesThatNeedToBeJoined().add(new PairOfBodies(body, conflictingBody));
      }
      
      map.getPairToWaterBodies().put(node, body);
   }

   private BodyOfWater createBodyFromHex(Pair pair){

      BodyOfWater body = null;
      
      if (inBody(pair) == null && map.getHex(pair).getStandingWater(0) > Environment.WATER_BODY_MIN){
         
         body = new BodyOfWater(pair);
         computeAndSetMembers(body, pair);
         body.buildConnectivityMap();
      }
      
      if (body != null){
         
         map.getAllWaterBodies().add(body);
      }
      
      return body;
   }
   
   private void handleMergingBodies(){

      for (PairOfBodies toBeJoined : map.getBodiesThatNeedToBeJoined()){

         handleEmptyBodyCheck(toBeJoined.getBodyOne());
         handleEmptyBodyCheck(toBeJoined.getBodyTwo());         

         if (!map.getWaterBodiesToBeRemoved().contains(toBeJoined.getBodyOne()) 
               && !map.getWaterBodiesToBeRemoved().contains(toBeJoined.getBodyTwo())){
            
            map.getWaterBodiesToBeRemoved().add(joinBodies(toBeJoined));
         }
         
      }
      
      map.resetBodiesThatNeedToBeJoined();
   }
   
   private void handleEmptyBodyCheck(BodyOfWater body){
      
      if (body.getAllMembers().size() == 0) {
         map.getWaterBodiesToBeRemoved().add(body);
      }
   }
   
   // Returns the one that needs to be destroyed
   private BodyOfWater joinBodies(PairOfBodies bodyPair){
      
      BodyOfWater biggerOne = bodyPair.getBodyOne();
      BodyOfWater smaller = bodyPair.getBodyTwo();
      
      if (bodyPair.getBodyOne().getAllMembers().size() < bodyPair.getBodyTwo().getAllMembers().size()){
         
         biggerOne = bodyPair.getBodyTwo();
         smaller = bodyPair.getBodyOne();
      }
      
      biggerOne.mergeInOtherBody(smaller);
      
      return smaller;
   }
   
   /*
    * Old water system
    */
   
   public void waterCycle(boolean findLeak, boolean leakFound, int ticks) {

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
      Map<Pair,Pair> displayMap = new HashMap<Pair,Pair>();
      Map<Pair, Integer> bodyDisplayMap = new HashMap<Pair, Integer>();
      DisplayType displayType = map.getDisplayType();
      Set<Pair> candidates = new HashSet<Pair>();

      for (Pair hexID : allHexes) {
         
         BodyOfWater inBody = inBody(hexID);
         Hex hex = map.getHex(hexID);

         if (inBody == null) {
            
            if (isBodyOfWaterCandidate(hexID)){
               
               candidates.add(hexID);
            }

            else{
            
               hexService.evaporate(hex, findLeak, leakFound);
               hexService.flood(hex, findLeak, map.getHexBodyStandingWater(hex));
               hexService.topple(hexID, 0);
            }
         } else if(ticks % Environment.UNDERWATER_TOPPLE_FREQUENCY == 0){
            hexService.topple(hexID, 0);
         }
         
         Pair displayPair = map.updateHexDisplay(hex, displayType);

         displayMap.put(hexID, displayPair);
      }
      
      map.setDisplayMap(displayMap);
      
      for (BodyOfWater body : map.getAllWaterBodies()){
         
         int waterLine = body.getWaterLine();
         
         for (Pair pair : body.getAllMembers()){
            
            bodyDisplayMap.put(pair, waterLine);
         }
      }
      
      map.setBodyDisplayMap(bodyDisplayMap);
         
      for (Pair pair : candidates){
         
         if (inBody(pair) == null){
         
            boolean stillGood = true;
            
            for (Pair neighbor : pair.getNeighbors()){
               
               if (!candidates.contains(neighbor)){
                  
                  stillGood = false;
                  break;
               }
            }
            
            if (stillGood){
               
               createBodyFromHex(pair);
            }
         }
      }

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
   private void blow(boolean findLeak) {

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

               Pair pair = cloud.getHexIdFromDirection(direction);
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

            Pair pair = layer.get(n).getHexIdFromDirection(direction.turnRight());
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

      int changed = hex.alterMoistureInAir(-Math.abs(amount));
      
      map.alterMoisture(hex, changed);

      return changed == amount;
   }
   
   // BodyOfWater stuff
   
   private void computeAndSetMembers(BodyOfWater body, Pair startingMember) {

      HexMap map = HexMap.getInstance();

      if (map.getHexes().get(startingMember).getStandingWater(0) < Environment.WATER_BODY_MIN) {

         return;
      }

      new Propogator().propogate(new FloodCommand(body), startingMember);
      addToMapWaterBodies(startingMember, body);
      body.setWaterLineLastChecked();
   }
   
   private void addMember(BodyOfWater body, Pair member) {

      body.getAllMembers().add(member);
      addToMapWaterBodies(member, body);

      HexMap map = HexMap.getInstance();
      Hex hex = map.getHex(member);
      
      // Once it's in a body, all the plants should die
      hex.killAllPlants();

      body.adjustTotalWater(hex.alterMoisture(-hex.getStandingWater(0), false));

      int elevation = map.getHex(member).getElevation();
      body.adjustFloorElevation(elevation);
      body.addToElevationMap(member, elevation);
   }

   private void removeMember(BodyOfWater body, Pair member) {
    
      if (body.getAllMembers().contains(member)){
      
         body.getAllMembers().remove(member); // allMembers
         map.getPairToWaterBodies().remove(member);
   
         if (body.getAllMembers().size() == 0){
            map.getWaterBodiesToBeRemoved().add(body);
         }
         
         Hex hex = map.getHex(member);
         
         int leftover = body.adjustTotalWater(-hex.alterMoisture(hex.getStandingWater(0), false));
   
         // Just in case... but this should never happen
         if (leftover > 0) hex.alterMoistureInAir(leftover);
   
         int elevation = map.getHex(member).getElevation();
         body.adjustFloorElevation(-elevation);
         body.removeFromElevationMap(member, elevation); // elevationMap
      }
   }
   
   // Returns all disconnected pairs
   private List<Pair> handleWaterLineChanges(BodyOfWater body){
      
      // make sure to handle connectivity in here
      
      List<Pair> returnedOrphans = new ArrayList<Pair>();
      int currentWaterLine = body.getWaterLine();
      
      //if (currentWaterLine == body.getWaterLineLastChecked()) return new ArrayList<Pair>();
      
      // Add members
      // Consume any other bodies encountered, removing them from map
      if (currentWaterLine > body.getWaterLineLastChecked()){

         Set<Pair> shallowHexes = new HashSet(body.getShallowHexes(Environment.ELEVS_TO_FLOOD));
         
         for(Pair shallow : shallowHexes){
            
            for (Pair beach : shallow.getNeighbors()){
            
               new Propogator().propogate(new FloodCommand(body), beach);
            }
         }
      }

      Set<Pair> orphanedMembers = new HashSet<Pair>();
      
      int point = body.getExtremePoint(true);
      List<Pair> markedForRemoval = new ArrayList<Pair>();
      
      while(point > body.getWaterLineLastChecked()){
         
         if (body.getElevationMap().get(point) != null) markedForRemoval.addAll(body.getElevationMap().get(point));

         point--;
      }
      
      for (Pair pair : markedForRemoval){
         
         removeMember(body, pair);
         Set<Pair> orphans = body.removeFromConnectivityMap(pair);
         
         if (orphans != null){
            orphanedMembers.addAll(orphans);
         }
      }

      for (Pair orphan : orphanedMembers){
         
         if (body.getAllMembers().contains(orphan)){
            
            if (!body.tryConnectOrphan(orphan)){
               
               returnedOrphans.add(orphan);
            }
         }
      }

      
      return returnedOrphans;
   }
   
   private boolean isBodyOfWaterCandidate(Pair pair){
      
      return inBody(pair) == null && map.getHex(pair).getStandingWater(0) > Environment.WATER_BODY_MIN;
   }
   
   private class FloodCommand implements Evaluator{

      HexMap map = HexMap.getInstance();
      BodyOfWater body;
      
      FloodCommand(BodyOfWater withBody){
         body = withBody;
      }

      public boolean evaluate(Pair pairToEvaluate) {
         
         BodyOfWater anotherBody = inBody(pairToEvaluate);
         boolean ranIntoAnotherBody = anotherBody != null && !anotherBody.equals(body);
         
         if (ranIntoAnotherBody){

            map.getBodiesThatNeedToBeJoined().add(new PairOfBodies(body, anotherBody));
         }

         return !ranIntoAnotherBody && isBodyOfWaterFloodCandidate(pairToEvaluate);
      }
      
      private boolean isBodyOfWaterFloodCandidate(Pair pair){
         
         return !body.getAllMembers().contains(pair) && map.getHex(pair).getStandingWater(0) > Environment.WATER_BODY_MIN;
      }

      public void onSuccess(Pair pairToEvaluate) {

         addMember(body, pairToEvaluate);         
      }

      public void onFail(Pair pairToEvaluate) {
         
         //body.addToShore(pairToEvaluate);
      }
   }
}
