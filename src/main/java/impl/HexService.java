package impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Component;

import enums.Direction;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Jungle;
import models.Pair;
import models.Plant;

@Component
public class HexService {

	public HexService() {

	}

	public Pair N(Pair ID) {

		return wrap(ID.getX(), ID.getY() -1);
	}

	public Pair NE(Pair ID) {

		return wrap(ID.getX() + 1, ID.getY());
	}

	public Pair SE(Pair ID) {
		
		return wrap(ID.getX() + 1, ID.getY() + 1);
	}

	public Pair S(Pair ID) {

		return wrap(ID.getX(), ID.getY() + 1);
	}

	public Pair SW(Pair ID) {

		return wrap(ID.getX() - 1, ID.getY());
	}

	public Pair NW(Pair ID) {

		return wrap(ID.getX() - 1, ID.getY() - 1);
	}
	
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

	public Pair mergePairs(Pair pair1, Pair pair2){
	   
	   return wrap(new Pair(pair1.getX() + pair2.getX(), pair1.getY() + pair2.getY()));
	}
	
	public Pair wrap(Pair pair) {

      return wrap(pair.getX(), pair.getY());
   }

   public Pair wrap(int x, int y){
		
		int newX = x;
		int newY = y;
		
		HexMap map = HexMap.getInstance();
		
		int mapX = map.getSize()[0];
		int mapY = map.getSize()[1];
		
		if (x >= mapX){
			
			newX %= mapX;
		}
		
		if (x < 0){
			
			newX += (mapX * (Math.abs(newX / mapX) + 1));
		}
		
		if (newY >= mapY + newX/2){
			
			newY %= mapY;
		}
		
		if (newY < newX/2){
			
		   newY = (newY - newX/2 + mapY*mapY) % mapY + newX/2;
		}

		Pair pair = new Pair(newX, newY);
		
		return pair;
	}
	
	public Pair getRandomPair(){
		
		HexMap map = HexMap.getInstance();
		Random rand = new Random();
		
		int seedInt = rand.nextInt(map.getSize()[0] * map.getSize()[1]);
		
		return wrap(seedInt % map.getSize()[0],seedInt / map.getSize()[0]);
	}
	
	public Direction getRandomDirection() {

		Random rand = new Random();
		
		return Direction.values()[rand.nextInt(6)];
	}
	
	public boolean inBounds(Pair ID, HexMap map) {
		
		return map.getHexes().containsKey(ID);
	}
	
	public Pair getAreaPair(Pair ID){
		
		List<Pair> neighborhood = getNeighbors(ID);
		neighborhood.add(ID);
		return neighborhood.get(new Random().nextInt(neighborhood.size()));
	}

	public List<Pair> getNeighbors(Pair hex) {
		
		Pair pair = new Pair(hex.getX(), hex.getY());
		
		HexMap map = HexMap.getInstance();
		List<Pair> neighbors = new ArrayList<Pair>();

		Pair newID = N(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = NW(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = SW(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = S(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = SE(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = NE(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		return neighbors;
	}
	
	public Set<Pair> getNeighborsSet(Pair hex) {
		
		Pair pair = new Pair(hex.getX(), hex.getY());
		
		HexMap map = HexMap.getInstance();
		Set<Pair> neighbors = new HashSet<Pair>();

		Pair newID = N(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = NW(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = SW(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = S(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = SE(pair);
		if (inBounds(newID, map)) {
			neighbors.add(newID);
		}
		newID = NE(pair);
		if (inBounds(newID, map)) {
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
	public boolean evaporate(Hex hex, boolean findLeak) {
		
		boolean returnBool = false;
		int total = 0;
		
		if (findLeak){
			total = hex.getTotalWater();
		}

		if (hex.getStandingWater() > 1 && hex.alterMoisture(-1) > 0){// 
			
			//WATER MOVEMENT
			hex.setMoistureInAir(hex.getMoistureInAir() + 1);
			
			returnBool = true;
		}
		else{
			
			Random rand = new Random();
			
			if (0 == rand.nextInt(4)){
			
				Plant plant = hex.getHighestVegetation();
				
				if(plant != null && rand.nextInt(Environment.DRY_PLANT) < plant.getMoisture()){
	
					hex.alterMoistureInAir(1);
					
					if (hex.alterMoisture(-1) == 0){
						
						plant.setMoisture(plant.getMoisture() - 1);
					}
					
					if (plant.getMoisture() <= 0){
						
						hex.deletePlant(plant.getIndex());
					}
				}
			}
		}
		
		if (hex.getMoistureInAir() >= Environment.CLOUD){

			HexMap.getInstance().addCloud(hex.getHexID());
		}
		
		if (findLeak && total != hex.getTotalWater()){
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
	
	private boolean rainSingle(Hex hex, int amount){

		int changed = hex.alterMoistureInAir(-Math.abs(amount));
		hex.alterMoisture(changed);
		
		return changed == amount;
	}

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
			
			Hex adjHex = HexMap.getInstance().getHex(hexId);
			
			if (adjHex.getFire() <= 0){
			
				int resist = adjHex.hexFireResistence() + adjHex.getMoistureInAir() - adjHex.getElevation();
				
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
		
			if (destination.equals(N(origin))){
				
				direction = Direction.north;
			}
			
			else if (destination.equals(NE(origin))){
				
				direction = Direction.northeast;
			}
	
			else if (destination.equals(SE(origin))){
		
				direction = Direction.southeast;
			}
			
			else if (destination.equals(S(origin))){
				
				direction = Direction.south;
			}
			
			else if (destination.equals(SW(origin))){
				
				direction = Direction.southwest;
			}
			
			else if (destination.equals(NW(origin))){
				
				direction = Direction.northwest;
			}
		}
		
		return direction;
	}
	
	public Pair getHexIdFromDirection(Pair origin, Direction direction){
		
		Pair returnPair = origin;
		
		if (direction != null){
		
			switch (direction){
			
			case north: 
				
				returnPair = N(origin);
	
				break;
				
			case northeast: 
				
				returnPair = NE(origin);
				
				break;
				
			case southeast: 
				
				returnPair = SE(origin);
				
				break;
				
			case south: 
				
				returnPair = S(origin);
				
				break;
				
			case southwest: 
				
				returnPair = SW(origin);
				
				break;
				
			case northwest: 
				
				returnPair = NW(origin);
				
				break;
				
			default:
				
				break;
			}
		}
		
		return returnPair;
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
			
			if (map.getClouds().containsKey(cloud)){
			
				if (rainSingle(map.getHex(cloud), map.getClouds().get(cloud))){
					
					map.getClouds().put(cloud, map.getClouds().get(cloud) + 1);
				} else {
					
					map.getClouds().remove(cloud);
				}
			}

			if (map.getClouds().containsKey(cloud)){
			
				for (Direction direction : Direction.VALUES){

					Pair pair = getHexIdFromDirection(cloud, direction);
					map.removeCloud(pair);
					int cloudElevation = map.getHex(cloud).getElevation();

					if (map.getClouds().containsKey(cloud) && blowSingleHex(pair, cloud, cloudElevation, findLeak)){

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
		
		while(layer.size() > 0){
			
			List<Pair> nextLayer = new ArrayList<Pair>();
			
			for (int n = 0; n < layer.size(); n++){
				
				if (n == 0){
					
					Pair pair = getHexIdFromDirection(layer.get(n), direction);
					map.removeCloud(pair);

					if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)){
					
						nextLayer.add(pair);
					}
				}
				
				Pair pair = getHexIdFromDirection(layer.get(n), direction.turnRight());
				map.removeCloud(pair);

				if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)){
				
					nextLayer.add(pair);
				}
			}
			
			layer = nextLayer;
		}
	}

	private boolean blowSingleHex(Pair from, Pair to, int cloudElevation, boolean findLeak){
		
	   HexMap map = HexMap.getInstance();
	   
		int total = 0;
		Hex fromHex = map.getHex(from);
		Hex toHex = map.getHex(to);
		
		int strangeBehaviorCount = 0;

		while (fromHex == null || toHex == null){
		   
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
		
		if (strangeBehaviorCount > 0){
		   System.out.println("Had to retry getting hex "+strangeBehaviorCount+"times");
		}
		
		if (findLeak){
			total = fromHex.getMoistureInAir() + toHex.getMoistureInAir();
		}
		
		int moisturetoMove = Math.abs(cloudElevation - fromHex.getElevation()) + fromHex.getMoistureInAir() + Environment.WIND_POWER 
				- toHex.getMoistureInAir();
		
		if (moisturetoMove < 0){ // negative wind, to becomes from, etc
			
			fromHex.alterMoistureInAir(toHex.alterMoistureInAir(moisturetoMove));
			return false;
		}

		boolean returnBool = (toHex.alterMoistureInAir(fromHex.alterMoistureInAir(-moisturetoMove)) == moisturetoMove);
		
		if (findLeak && fromHex.getMoistureInAir() + toHex.getMoistureInAir() != total){
			
			System.out.println("blowSingleHex leak");
		}
		
		return returnBool;
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
		
		if ((slope*slope*Environment.SLOPE_CONSTANT > stability || Math.abs(slope) > stability)
				&& Math.abs(slope) > Environment.MAX_SLOPE){
		
			if (slope > 0){
				
				from.setElevation(from.getElevation() - slope/4);
				to.setElevation(to.getElevation() + slope/4);
				
				removeAllVegetation(from);
				removeAllVegetation(to);
			}
			
			else{
				
				from.setElevation(from.getElevation() + Math.abs(slope/4));
				to.setElevation(to.getElevation() - Math.abs(slope/4));
				
				removeAllVegetation(from);
				removeAllVegetation(to);
			}
			
			returnValue = true;
		}
		
		return returnValue;
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
	public boolean flood(Hex hex, boolean findLeak) {

		boolean returnBool = false;
		int total = 0;
		
		if (findLeak){
			total = hex.getTotalWater();
		}

		//If there is standing water, shove it around
		if (hex.getSaturation() > 1) {
			int elev = hex.getCombinedElevation();
			List<Pair> neighbors = getNeighbors(hex.getHexID());
			
			//Kill plants that aren't strong enough
			drownPlant(hex);
			
			if (findLeak && total != hex.getTotalWater()){
				System.out.println("drown plant leak!");
				total = hex.getTotalWater();
			}

			int lowest = elev;
			Hex flowTo = null;
			
			//Find a hex for it to flow to
			for (Pair neighbor : neighbors) {

				Hex adjHex = HexMap.getInstance().getHex(neighbor);
				int adjElev = adjHex.getCombinedElevation();
				
				if (adjElev < elev && adjElev < lowest) {
					returnBool = true;
					lowest = adjElev;
					flowTo = adjHex;
				}
			}
			
			if (returnBool && flowTo != null){
				
				int hexAdjElev = hex.getElevation() * 4 + hex.getStandingWater();
				int flowToAdjElev = flowTo.getElevation() * 4 + flowTo.getStandingWater();
				
				int difference = hexAdjElev - flowToAdjElev;
				
				int toDistribute = difference / 2; //totalNeed * flooded.size() / (flooded.size() + 1);
				
				if (toDistribute > hex.getStandingWater()){
					
					toDistribute = hex.getStandingWater();
				}
				
				
				if (toDistribute > 0){
				
					int flowToTotal = flowTo.getTotalWater();
					erode(hex, flowTo, toDistribute);
					flowTo.alterMoisture(hex.alterMoisture(- toDistribute));
					
					if (findLeak){
						if (total + flowToTotal != hex.getTotalWater() + flowTo.getTotalWater()){
							System.out.println("flow leak!");
							
						}
						total = hex.getTotalWater();
					}
				}
			}
		}

		else{
			
			Hex lowest = getLowestNeighber(hex.getHexID());
			
			if (lowest.getElevation() < hex.getElevation() && hex.alterMoisture(- 1) > 0){
				
				int lowestTotal = lowest.getTotalWater();
				lowest.alterMoisture(1);
				
				if (findLeak){
					if (total + lowestTotal != hex.getTotalWater() + lowest.getTotalWater()){
						System.out.println("seep leak!");
						
					}
				}
			}
		}

		return returnBool;
	}

	private Hex getLowestNeighber(Pair id){
		
		List<Pair> neighbors = getNeighbors(id);
		HexMap map = HexMap.getInstance();
		
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
	private void drownPlant(Hex hex) {
		
		int standing = hex.getStandingWater();
		double saturation = hex.getSaturation();
		
		for (int i = 0; i < hex.getVegetation().length; i++){
			
			if (hex.getVegetation()[i] != null && saturation > hex.getVegetation()[i].getMaxSaturation()){
				
				int strength = hex.getVegetation()[i].getRootstrength();
				Random rand = new Random();
				
				if (rand.nextInt(1 + (int) (standing * Environment.FLOOD_STRENGTH)) > strength){
					
					hex.deletePlant(i);
				}
				
				else if (rand.nextFloat() < Environment.ROT_RATE){
					
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
		
			Random rand = new Random();
			slope -= 2;
			slope *= slope * slope;
			int strength = Math.abs(maxStrength) * slope - slope;
			
			if (strength > 0){
			
				// replaced 'Environment.EROSION_INDEX with slope
				int erosionStrength = rand.nextInt(strength);
				
				if (fromHex.getSoilStability() < erosionStrength){

				   fromHex.setElevation(fromHex.getElevation() - 1);
					toHex.setElevation(toHex.getElevation() + 1);
		
					if (toHex.getDensity() <= 0 && 0 == rand.nextInt(5)){
					
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
		HexMap map = HexMap.getInstance();
		Hex hex = map.getHex(id);

		//Shouldn't grow out when on fire
		if (hex.getFire() <= 0){

			Plant plant = hex.getRandomPlant();
			
			if(plant != null){
			   
			   List<Pair> neighbors = getNeighbors(id);
			   
				for (Pair neighbor : neighbors){
					
					Hex adjHex = map.getHex(neighbor);
					
					if (adjHex.getFire() <= 0){
						
						if (adjHex.addPlant(plant)){
							
							break;
						}
					}
				}
			}
		}
		
		return grew;
	}

	public boolean ignite(Pair id, int flame){
		
		Hex newHex = new Hex();
		newHex = HexMap.getInstance().getHex(id);
		
		if (newHex != null && flame > newHex.hexFireResistence() && newHex.getSaturation() < 1 && newHex.getHighestVegetation() != null){
				
			newHex.setFire(newHex.getFireStrength());
			
			HexMap.getInstance().addBurningHex(id);
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
		
			if (hex.getSaturation() > 1){
				
				hex.setFire(0);
			}
			
			else{
			
				hex.setFire(hex.getFire() - Environment.BURN_DOWN_RATE);
				
				if (hex.getFire() <= 0){
					
					hex.setFire(0);
					
					HexMap.getInstance().removeBurningHex(hex.getHexID());
			
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
		
		switch(HexMap.getInstance().getWindDirection()){
		
		case 0:
			
			returnPair = N(pair);
			break;
			
		case 1:
			
			returnPair = NE(pair);
			break;
			
		case 2:
			
			returnPair = SE(pair);
			break;
			
		case 3:
			
			returnPair = S(pair);
			break;
			
		case 4:
			
			returnPair = SW(pair);
			break;
			
		case 5:
			
			returnPair = NW(pair);
			break;
		}	
		
		return returnPair;
	}

	public void forceGrow(Hex hex) {
					
		hex.addPlant(new Jungle());
	}

	/**
	 * Spreads excess elevation to all neighboring hexes
	 * @param hexID
	 */
	public int topple(Pair id, int toppleCount) {
		
		List<Pair> neighbors = getNeighbors(id);
		HexMap map = HexMap.getInstance();
		Hex hexToTopple = map.getHex(id);
		Random rand = new Random();
		toppleCount ++;
		
		while (neighbors.size() > 0 && toppleCount < Environment.TOPPLE_DEPTH) {
			
			int index = rand.nextInt(neighbors.size());
			Pair targetPair = neighbors.get(index);
			neighbors.remove(index);
			
			if (targetPair != null && avalanche(hexToTopple, map.getHex(targetPair))){
				
				toppleCount = topple(targetPair, toppleCount);
			}
		}
		
		return toppleCount;
	}
	
}
