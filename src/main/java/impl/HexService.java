package impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import enums.Direction;
import models.BodyOfWater;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.TheRandom;
import models.plants.Jungle;
import models.plants.Plant;

@Component
public class HexService {

   public HexService() {

   }
   
   HexMap map = HexMap.getInstance();

   public List<Pair> getSharedNeighbors(Pair pair1, Pair pair2){

      List<Pair> neighborList = getNeighbors(pair1);
      Set<Pair> neighborSet = getNeighborsSet(pair2);
      List<Pair> returnList = new ArrayList<Pair>();

      for (Pair pair : neighborList){

         if (neighborSet.contains(pair)){

            returnList.add(pair);
         }
      }

      return returnList;
   }

   public Pair getRandomPair(){

      TheRandom rand = TheRandom.getInstance();

      int seedInt = rand.get().nextInt(Environment.MAP_GRID[0] * Environment.MAP_GRID[1]);

      return Pair.wrap(seedInt % Environment.MAP_GRID[0],seedInt / Environment.MAP_GRID[0]);
   }

   public Direction getRandomDirection() {

      TheRandom rand = TheRandom.getInstance();

      return Direction.values()[rand.get().nextInt(6)];
   }

   public boolean inBounds(Pair ID) {

      return map.getHexes().containsKey(ID);
   }

   public Pair getAreaPair(Pair ID){

      List<Pair> neighborhood = getNeighbors(ID);
      neighborhood.add(ID);
      TheRandom rand = TheRandom.getInstance();
      return neighborhood.get(rand.get().nextInt(neighborhood.size()));
   }

   public List<Pair> getNeighbors(Pair hex) {

      Pair pair = new Pair(hex.getX(), hex.getY());

      List<Pair> neighbors = new ArrayList<Pair>();

      Pair newID = pair.N();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.NW();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.SW();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.S();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.SE();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.NE();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      return neighbors;
   }

   public Set<Pair> getNeighborsSet(Pair hex) {

      Pair pair = new Pair(hex.getX(), hex.getY());
      Set<Pair> neighbors = new HashSet<Pair>();

      Pair newID = pair.N();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.NW();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.SW();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.S();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.SE();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      newID = pair.NE();
      if (inBounds(newID)) {
         neighbors.add(newID);
      }
      return neighbors;
   }

   /**
    * This will attempt to evaporate. Succeeds if saturation > 100%. Else,
    * returns false.
    * 
    * @param hex
    * @return
    */
   public boolean evaporate(Hex hex, boolean findLeak, boolean foundLeak) {

      boolean returnBool = false;
      int total = 0;

      if (findLeak && !foundLeak){
         total = hex.getTotalWater(map.getStaleHexBodyStandingWater(hex));
      }

      if (hex.getStandingWater(0) > 1 && map.alterMoisture(hex, -1) > 0){// 

         //WATER MOVEMENT
         hex.setMoistureInAir(hex.getMoistureInAir() + 1);

         returnBool = true;
      }
      else{

         TheRandom rand = TheRandom.getInstance();

         if (0 == rand.get().nextInt(Environment.EVAPORATION_RESISTANCE)){

            Plant plant = hex.getHighestVegetation();

            if(plant != null && rand.get().nextInt(Environment.DRY_PLANT) < plant.getMoisture()){

               hex.alterMoistureInAir(1);

               if (map.alterMoisture(hex, -1) == 0){

                  plant.setMoisture(plant.getMoisture() - 1);
               }

               if (plant.getMoisture() <= 0){

                  hex.deletePlant(plant.getIndex());
               }
            }
         }
      }

      if (hex.getMoistureInAir() >= Environment.CLOUD){

         map.addCloud(hex.getHexID());
      }

      if (findLeak && !foundLeak && total != hex.getTotalWater(map.getStaleHexBodyStandingWater(hex))){
         System.out.println("evaporate leak!");
      }

      return returnBool;
   }

   /**
    * Handles rain for the whole map
    */
   /*	public void rainAll() {

		//HexMapImpl hexMapImpl = new HexMapImpl();
		HexMap map = HexMap.getInstance();
		Map<Pair, Integer> raining = map.getRaining();
		Object[] rainingArray = raining.keySet().toArray();

		for (Object objectRaining : rainingArray){

			Pair pair = (Pair) objectRaining;

			if(rainSingle(map.getHex(pair), raining.get(pair))){

				if (raining.get(pair) > 1){
					int woo = 0;
				}

				raining.put(pair, raining.get(pair) + 1);
				List<Pair> neighbors = getNeighbors(pair);

				for (Pair neighbor : neighbors){

					if (!raining.containsKey(neighbor)){

						raining.put(neighbor, 1);
					}
				}

			} else {

				raining.remove(pair);
				map.removeCloud(pair);
			}
		}
	}*/



   /**
    * Returns hex with lowest pressure
    */
   public Pair[] getEasyFirePath(Hex hex) {

      Pair[] path = new Pair[2];
      int[] resistance = new int[2];
      resistance[0] = 1000;
      resistance[1] = 1000;

      List<Pair> neighbors = getNeighbors(hex.getHexID());

      for (Pair hexId : neighbors) {

         Hex adjHex = map.getHex(hexId);

         if (adjHex.getFire() <= 0){

            int resist = adjHex.hexFireResistence(map.getStaleHexBodyStandingWater(hex)) + adjHex.getMoistureInAir() - adjHex.getElevation();

            for (int i = 0; i < resistance.length; i++){

               if (resistance[i] > resist){

                  resistance[i] = resist;
                  path[i] = adjHex.getHexID();
                  break;
               }
            }
         }
      }

      return path;
   }

   /**
    * Takes two adjacent hexes ids and returns the direction 
    * from the first to the second.
    * 
    * Returns '6' if the hexes aren't adjacent
    */
   public Direction getDirectionBetweenHexes(Pair origin, Pair destination){

      Direction direction = null;

      if (destination != null && origin != null){

         if (destination.equals(origin.N())){

            direction = Direction.north;
         }

         else if (destination.equals(origin.NE())){

            direction = Direction.northeast;
         }

         else if (destination.equals(origin.SE())){

            direction = Direction.southeast;
         }

         else if (destination.equals(origin.S())){

            direction = Direction.south;
         }

         else if (destination.equals(origin.SW())){

            direction = Direction.southwest;
         }

         else if (destination.equals(origin.NW())){

            direction = Direction.northwest;
         }
      }

      return direction;
   }

   // for leak test
   /*HexMapImpl hexMapImpl = new HexMapImpl();
		int totalWater = hexMapImpl.allWater()[0];
		boolean found = false;*/

   //Get strong wind start point
   /*
		int strength = map.isBlowing();

		while (strength > 0){

			pseudoId = getStrongWindHex(pseudoId);
			strength--;
		}*/

   //If it can blow
   /*		if (hex.getMoistureInAir() >= Environment.CLOUD && map.inWindWhiteHexes(hex.getHexID())) {

			Direction direction = getWindDirection(hex.getHexID());

			if (direction != null){

				Hashtable<Pair,Integer> cloud = new Hashtable<Pair,Integer>();

				cloud = collectCloud(cloud, hex.getHexID());

				moveCloud(cloud, direction);
			}
		}
	}*/

   /*	private Hashtable<Pair, Integer> collectCloud(
			Hashtable<Pair, Integer> cloud, Pair hexID) {

		if (cloud.size() < Environment.CLOUD_SIZE){

			List<Pair> neighbors = getNeighbors(hexID);
			HexMap map = HexMap.getInstance();

			ArrayList<Pair> hexesToCollect = new ArrayList<Pair>();

			for (Pair neighbor : neighbors){

				if (!cloud.contains(neighbor) && map.inWindWhiteHexes(neighbor)){

					map.removeWindWhiteHex(neighbor);
					Hex neighborHex = map.getHex(neighbor);

					if (neighborHex.getMoistureInAir() < Environment.CLOUD){

						map.getHex(hexID).alterMoistureInAir(neighborHex.getMoistureInAir());
						neighborHex.setMoistureInAir(0);
					}

					else{

						hexesToCollect.add(neighbor);
					}
				}
			}

			cloud.put(hexID, map.getHex(hexID).getMoistureInAir());

			for (Pair hexId : hexesToCollect){

				cloud = collectCloud(cloud, hexId);
			}
		}

		return cloud;
	}*/

   /**
    * Move all adjacent hex pressure too!
    */
   /*	public void moveCloud(Hashtable<Pair,Integer> cloud, Direction direction){

		HexMap map = HexMap.getInstance();

		Enumeration<Pair> ids = cloud.keys();

		while (ids.hasMoreElements()){

			Pair origin = (Pair) ids.nextElement();
			Pair target = getHexIdFromDirection(origin, direction);

			if (map.getHex(origin).alterMoistureInAir(-Math.abs(cloud.get(origin)))){

				map.getHex(target).alterMoistureInAir(Math.abs(cloud.get(origin)));
			}

			map.removeWindWhiteHex(origin);
			map.removeWindWhiteHex(target);
		}
	}*/

   public void removeAllVegetation(Hex hex){

      for (int i = 0; i < hex.getVegetation().length; i++){

         hex.deletePlant(i);
      }
   }

   /**
    * compares elevation in two hexes and if slope is too great, the higher of the two falls
    * Vegetation is removed in both hexes
    * 
    * @param from
    * @param to
    */
   public boolean avalanche(Hex from, Hex to){

      int slope = from.getElevation() - to.getElevation();
      int stability = from.getSoilStability();
      boolean returnValue = false;

      if (Math.abs(slope) > stability
            && Math.abs(slope) > Environment.MAX_SLOPE){

         if (slope > 0){

            handleAvalanche(from, to, slope);
         }

         else{

            handleAvalanche(to, from, Math.abs(slope));
         }

         returnValue = true;
      }

      return returnValue;
   }
   
   private void handleAvalanche(Hex from, Hex to, int slope){
      
      from.setElevation(from.getElevation() - slope/4, false);
      
      BodyOfWater body = map.getPairToWaterBodies().get(to.getHexID());
      
      if (to.setElevation(to.getElevation() + slope/4, body != null)){
         body.addToHexesToCheckForElevation(to.getHexID());
      }
      
      int left = to.setDensity(to.getDensity() - 1);
      from.setDensity(from.getDensity() + 1 + left);

      removeAllVegetation(from);
      removeAllVegetation(to);
   }

   /**
    * Body of water:
    * No erosion in body
    * added or subtracted by unrelated services (grow, blow, etc)... alg might be tricky
    * shared sum of water
    * shared elevation
    */

   /**
    * Attempt to flood a single hex
    */
   public boolean flood(Hex hex, boolean findLeak, int standingBodyWater) {

      boolean returnBool = false;
      int total = 0;
      int bodyStandingWater = map.getStaleHexBodyStandingWater(hex);

      if (findLeak){
         total = hex.getTotalWater(bodyStandingWater);
      }

      //If there is standing water, shove it around
      if (hex.getSaturation(standingBodyWater) > 1) {
         int elev = hex.getCombinedElevation(standingBodyWater);
         List<Pair> neighbors = getNeighbors(hex.getHexID());

         //Kill plants that aren't strong enough
         drownPlant(hex, bodyStandingWater);

         if (findLeak && total != hex.getTotalWater(bodyStandingWater)){
            System.out.println("drown plant leak!");
            total = hex.getTotalWater(bodyStandingWater);
         }

         int lowest = elev;
         Hex flowTo = null;

         //Find a hex for it to flow to
         for (Pair neighbor : neighbors) {

            Hex adjHex = map.getHex(neighbor);
            int adjElev = adjHex.getCombinedElevation(standingBodyWater);

            if (adjElev < elev && adjElev < lowest) {
               returnBool = true;
               lowest = adjElev;
               flowTo = adjHex;
            }
         }

         if (returnBool && flowTo != null){

            int hexAdjElev = hex.getElevation() * 4 + hex.getStandingWater(standingBodyWater);
            int flowToAdjElev = flowTo.getElevation() * 4 + flowTo.getStandingWater(map.getStaleHexBodyStandingWater(flowTo));

            int difference = hexAdjElev - flowToAdjElev;

            int toDistribute = difference / 2; //totalNeed * flooded.size() / (flooded.size() + 1);

            if (toDistribute > hex.getStandingWater(standingBodyWater)){

               toDistribute = hex.getStandingWater(standingBodyWater);
            }


            if (toDistribute > 0){

               int flowToTotal = flowTo.getTotalWater(bodyStandingWater);
               erode(hex, flowTo, toDistribute);
               map.alterMoisture(flowTo, map.alterMoisture(hex, - toDistribute));

               if (findLeak){
                  if (total + flowToTotal != hex.getTotalWater(bodyStandingWater) + flowTo.getTotalWater(bodyStandingWater)){
                     System.out.println("flow leak!");

                  }
                  total = hex.getTotalWater(bodyStandingWater);
               }
            }
         }
      }

      else{

         Hex lowest = getLowestNeighber(hex.getHexID());

         if (lowest.getElevation() < hex.getElevation() && map.alterMoisture(hex, - 1) > 0){

            int lowestTotal = lowest.getTotalWater(bodyStandingWater);
            map.alterMoisture(lowest, 1);

            if (findLeak){
               if (total + lowestTotal != hex.getTotalWater(bodyStandingWater) + lowest.getTotalWater(bodyStandingWater)){
                  System.out.println("seep leak!");

               }
            }
         }
      }

      return returnBool;
   }

   private Hex getLowestNeighber(Pair id){

      List<Pair> neighbors = getNeighbors(id);

      int elevation = 1000;
      Hex lowest = map.getHex(neighbors.get(0));

      for (Pair neighbor : neighbors){

         Hex hex = map.getHex(neighbor);

         if (hex.getElevation() < elevation){

            elevation = hex.getElevation();
            lowest = hex;
         }
      }

      return lowest;
   }

   // Deletes plants if the standing water is greater than the rootstrength of the plant
   private void drownPlant(Hex hex, int standingBodyWater) {

      int standing = hex.getStandingWater(standingBodyWater);
      double saturation = hex.getSaturation(standingBodyWater);

      for (int i = 0; i < hex.getVegetation().length; i++){

         if (hex.getVegetation()[i] != null && saturation > hex.getVegetation()[i].getMaxSaturation()){

            int strength = hex.getVegetation()[i].getRootstrength();
            TheRandom rand = TheRandom.getInstance();

            if (rand.get().nextInt(1 + (int) (standing * Environment.FLOOD_STRENGTH)) > strength){

               hex.deletePlant(i);
            }

            else if (rand.get().nextFloat() < Environment.ROT_RATE){

               hex.getVegetation()[i].setRootstrength(strength - 1);
            }
         }
      }
   }

   /**
    * Erodes, increasing density of fromHex, decreasing density of toHex
    * @param fromHex
    * @param toHex
    * @return Boolean
    */
   public boolean erode(Hex fromHex, Hex toHex, int maxStrength) {

      boolean eroded = false;

      int slope = fromHex.getElevation() - toHex.getElevation();

      if (slope > 2){

         TheRandom rand = TheRandom.getInstance();
         slope -= 2;
         slope *= slope * slope;
         int strength = Math.abs(maxStrength) * slope - slope;

         if (strength > 0){

            // replaced 'Environment.EROSION_INDEX with slope
            int erosionStrength = rand.get().nextInt(strength);

            if (fromHex.getSoilStability() < erosionStrength){
               
               fromHex.setElevation(fromHex.getElevation() - 1, false);
               
               BodyOfWater body = map.getPairToWaterBodies().get(toHex.getHexID());

               boolean needsChecking = toHex.setElevation(toHex.getElevation() + 1, body != null);
               
               if (needsChecking) body.addToHexesToCheckForElevation(toHex.getHexID());

               if (toHex.getDensity() <= 0 && 0 == rand.get().nextInt(5)){

                  fromHex.setDensity(fromHex.getDensity() + 1);
                  toHex.setDensity(toHex.getDensity() - 1);
               }

               eroded = true;
            }
         }
      }

      return eroded;
   }

   /**
    * 
    * @param plants
    * @return int index
    */
   public int getIndexOfWeakestPlant(Plant[] plants) {

      int smallestPlant = 16;
      int smallPlantIndex = 0;

      for (int i = 0; i < plants.length; i++) {

         if (plants[i] == null){
            smallPlantIndex = i;
            break;
         }
         else{
            int moisture = plants[i].getMoisture();
            if (moisture < smallestPlant) {
               smallPlantIndex = i;
            }
         }
      }

      return smallPlantIndex;
   }

   /**
    * The strongest plant
    * @param hexes
    * @return
    */
   public int getStrengthOfStrongestPlantInHexes(List<Hex> hexes) {
      int possiblePlant = 0;
      for (Hex adjHex : hexes) {
         for (Plant plant : adjHex.getVegetation()) {
            if (plant != null) {
               if (plant.getMoisture() > possiblePlant) {
                  possiblePlant = plant.getMoisture();
               }
            }
         }
         if (possiblePlant == Hex.MAX_PLANT_STRENGTH) {
            break;
         }
      }
      return possiblePlant;
   }

   /**
    * Picks a hex, and attempts for it to grow to an adjacent hex
    * @param hex
    * @return
    */
   public boolean grow(Pair id) {

      boolean grew = false;
      Hex hex = map.getHex(id);

      //Shouldn't grow out when on fire
      if (hex.getFire() <= 0){

         Plant plant = hex.getRandomPlant();

         if(plant != null){

            List<Pair> neighbors = getNeighbors(id);

            for (Pair neighbor : neighbors){

               Hex adjHex = map.getHex(neighbor);

               if (adjHex.getFire() <= 0){

                  if (adjHex.addPlant(plant, map.getStaleHexBodyStandingWater(adjHex))){

                     break;
                  }
               }
            }
         }
      }

      return grew;
   }

   public boolean ignite(Pair id, int flame){

      Hex newHex = map.getHex(id);
      int standingBodyWater = map.getStaleHexBodyStandingWater(newHex);

      if (newHex != null && flame > newHex.hexFireResistence(map.getStaleHexBodyStandingWater(newHex)) && newHex.getSaturation(standingBodyWater) < 1 && newHex.getHighestVegetation() != null){

         newHex.setFire(newHex.getFireStrength(map.getStaleHexBodyStandingWater(newHex)));

         map.addBurningHex(id);
         return true;

      }
      return false;
   }

   /**
    * Attempts to burn
    * 
    * @param hex
    * @param strength
    * @return
    */
   public void igniteNext(Hex hex) {

      if (hex != null){

         Pair[] path = getEasyFirePath(hex);

         ignite(path[0], hex.getFire());
         ignite(path[1], hex.getFire());

      }
   }

   public void burnDown(Hex hex) {

      if (hex != null){

         if (hex.getSaturation(0) > 1 || map.getPairToWaterBodies().get(hex.getHexID()) != null){

            hex.setFire(0);
         }

         else{

            hex.setFire(hex.getFire() - Environment.BURN_DOWN_RATE);

            if (hex.getFire() <= 0){

               hex.setFire(0);

               map.removeBurningHex(hex.getHexID());

               for (int i = 0; i < hex.getVegetation().length; i++){

                  hex.deletePlant(i);

               }
            }
         }
      }
   }

   /**
    * If strong wind is blowing, this will pick the hex the wind will pretend to originate from
    * @param pair
    * @return
    */
   public Pair getStrongWindHex(Pair pair){

      Pair returnPair = new Pair(pair.getX(), pair.getY());

      switch(map.getWindDirection()){

      case 0:

         returnPair = pair.N();
         break;

      case 1:

         returnPair = pair.NE();
         break;

      case 2:

         returnPair = pair.SE();
         break;

      case 3:

         returnPair = pair.S();
         break;

      case 4:

         returnPair = pair.SW();
         break;

      case 5:

         returnPair = pair.NW();
         break;
      }	

      return returnPair;
   }

   public void forceGrow(Hex hex) {

      hex.addPlant(new Jungle(), map.getStaleHexBodyStandingWater(hex));
   }

   /**
    * Spreads excess elevation to all neighboring hexes
    * @param hexID
    */
   public int topple(Pair id, int toppleCount) {

      List<Pair> neighbors = getNeighbors(id);
      Hex hexToTopple = map.getHex(id);
      TheRandom rand = TheRandom.getInstance();
      toppleCount ++;

      while (neighbors.size() > 0 && toppleCount < Environment.TOPPLE_DEPTH) {

         int index = rand.get().nextInt(neighbors.size());
         Pair targetPair = neighbors.get(index);
         neighbors.remove(index);

         if (targetPair != null && avalanche(hexToTopple, map.getHex(targetPair))){

            toppleCount = topple(targetPair, toppleCount);
         }
      }

      return toppleCount;
   }

}
