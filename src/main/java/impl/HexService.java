package impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import enums.Direction;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.TheRandom;
import models.plants.Forest;
import models.plants.Grass;
import models.plants.Jungle;
import models.plants.Plant;
import models.plants.Thicket;

@Component
public class HexService {

	public HexService() {

	}

	HexMap map = HexMap.getInstance();

	public List<Pair> getSharedNeighbors(Pair pair1, Pair pair2){

		List<Pair> neighborList = pair1.getNeighbors();
		Set<Pair> neighborSet = pair1.getNeighborsSet();
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

	public Pair getAreaPair(Pair ID){

		List<Pair> neighborhood = ID.getNeighbors();
		neighborhood.add(ID);
		TheRandom rand = TheRandom.getInstance();
		return neighborhood.get(rand.get().nextInt(neighborhood.size()));
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
		
		TheRandom rand = TheRandom.getInstance();
		
		//if (rand.flipCoin()) return returnBool;

		if (hex.getStandingWater() > 1
				&& (hex.getMoistureInAir() + hex.getIncomingMoistureInAir()) > Environment.CLOUD
				&& map.alterMoisture(hex, -1) > 0){// 

			//WATER MOVEMENT
			hex.alterMoistureInAir(1);

			returnBool = true;
		}
		else{

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

		return returnBool;
	}

	/**
	 * Returns hex with lowest pressure
	 */
	public Pair[] getEasyFirePath(Hex hex) {

		Pair[] path = new Pair[2];
		int[] resistance = new int[2];
		resistance[0] = 1000;
		resistance[1] = 1000;

		List<Pair> neighbors = hex.getHexID().getNeighbors();

		for (Pair hexId : neighbors) {

			Hex adjHex = map.getHex(hexId);

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

		if (shouldTopple(Math.abs(slope), stability, from)){

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

	private boolean shouldTopple(int slope, int stability, Hex from){
		// Slope is naturally unstable
		return (slope > stability
				/*           // slope is steeper than should be possible above ground
            || (slope > Environment.MAX_SLOPE 
                  && HexMap.getInstance().getPairToWaterBodies().get(from) == null)
            // slope is steeper than it should be anywhere
            || (slope > Environment.MAX_UNDERWATER_SLOPE)*/);
	}

	private void handleAvalanche(Hex from, Hex to, int slope){

		from.setElevation(from.getElevation() - slope/4);

		to.setElevation(to.getElevation() + slope/4);

		int left = to.setDensity(to.getDensity() - 1);
		from.setDensity(from.getDensity() + 1 + left);

		if (slope > Environment.CATACLISMIC_AVALANCHE){
			removeAllVegetation(from);
			removeAllVegetation(to);
		}
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
	public boolean flood(Hex hex) {

		int standingWater = hex.getStandingWater();
		boolean flooded = false;

		//If there is standing water, shove it around
		if (standingWater > 1) {
			int elev = hex.getCombinedElevation();
			List<Pair> neighbors = hex.getHexID().getNeighbors();

			//Kill plants that aren't strong enough
			drownPlant(hex, standingWater);

			int lowest = elev;
			Hex flowTo = null;

			//Find a hex for it to flow to
			for (Pair neighbor : neighbors) {

				Hex adjHex = map.getHex(neighbor);
				int adjElev = adjHex.getCombinedElevation();

				if (adjElev < lowest) {
					lowest = adjElev;
					flowTo = adjHex;
				}
			}

			if (Environment.QUICK_FLOW && flowTo != null) {
				flooded = flowHexToHex(hex, standingWater, elev, lowest, flowTo, false);
			}
		}

		return flooded;
	}

	private boolean flowHexToHex(Hex hex, int standingWater, int realElev, int realLowest, Hex flowTo, boolean beenHereOnce) {
		boolean flooded;
		flooded = realLowest < realElev;
		
		int waterElev = realElev * Environment.WATER_PER_ELEVATION;
		int waterLowest = realLowest * Environment.WATER_PER_ELEVATION;
		int flowToStandingWater = flowTo.getStandingWater();
		int toDistribute = 0;

		if (realLowest < realElev && flowTo != null){

			if (realElev > map.getSnowLevel()){

				toDistribute = getSnowMelt(waterElev, waterLowest);
			}else{
				
				toDistribute = getWaterDistribution(waterElev, waterLowest, flowToStandingWater);
			}

			toDistribute = normalizeForLowFlowAreas(waterElev, waterLowest, toDistribute);

			if (toDistribute > 0){

				if (toDistribute > standingWater) toDistribute = standingWater;

				erode(hex, flowTo, toDistribute);

				map.alterMoisture(flowTo, map.alterMoisture(hex, - toDistribute));
			}
		}

		// if running madly down dry hill, just keep going
		continueFlowing(hex, flowTo, flowToStandingWater, toDistribute, beenHereOnce);
		
		return flooded;
	}

	private void continueFlowing(Hex hex, Hex flowTo, int oldFlowToStandingWater, int toDistribute, boolean beenHereOnce) {
		int realElev;
		int realLowest;
		int newFlowToStandingWater = flowTo.getStandingWater();
		
		if ((newFlowToStandingWater > 0) 
				&& (oldFlowToStandingWater == 0 || toDistribute > Environment.FLOOD_WATER_CONTINUE_SIZE )){
			
			List<Pair> flowToNeighbors = flowTo.getHexID().getNeighbors();
			realElev = flowTo.getCombinedElevation();
			realLowest = realElev;
			Hex nextFlowTo = null;
			
			for (Pair neighberPair : flowToNeighbors){
				
				Hex neighbor = map.getHex(neighberPair);
				int neighborCombined = neighbor.getCombinedElevation();
				
				if (neighborCombined < realLowest){
					
					realLowest = neighborCombined;
					nextFlowTo = neighbor;
				}
			}
			
			if (nextFlowTo != null && !beenHereOnce) {
				flowHexToHex(flowTo, flowTo.getStandingWater(), realElev,
						realLowest, nextFlowTo, true);
			}
		}
	}

	private int normalizeForLowFlowAreas(int elev, int lowest, int toDistribute) {
		if (toDistribute == 0) toDistribute = (elev - lowest)/2 + 1;
		return toDistribute;
	}

	private int getWaterDistribution(int elev, int lowest, int flowToStandingWater) {
		
		int waterDiff = elev - lowest;
		
		if (flowToStandingWater == 0) return waterDiff;
		
		return waterDiff / Environment.HOW_SLOW_WATER_MOVES;
	}

	private int getSnowMelt(int elev, int lowest) {
		return 1 + (elev - lowest) / (Environment.SNOW_MELT * Environment.HOW_SLOW_WATER_MOVES);
	}

	// Deletes plants if the standing water is greater than the rootstrength of the plant
	private void drownPlant(Hex hex, int standingBodyWater) {

		int standing = hex.getStandingWater();
		int saturation = hex.getStandingWater();

		for (int i = 0; i < hex.getVegetation().length; i++){

			if (hex.getVegetation()[i] != null && saturation > hex.getVegetation()[i].getMaxSaturation()){

				int strength = hex.getVegetation()[i].getRootstrength();
				TheRandom rand = TheRandom.getInstance();

				if (rand.get().nextInt(1 + (int) (standing * Environment.FLOOD_STRENGTH)) > (strength - hex.getVegetation()[i].getRot())){

					hex.deletePlant(i);
				}

				else if (rand.get().nextFloat() < Environment.ROT_RATE){

					hex.getVegetation()[i].rot();
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

		if (slope > 2 && toHex.getStandingWater() < Environment.STANDING_WATER_EROSION_CUTOFF){

			TheRandom rand = TheRandom.getInstance();
			slope -= 2;
			slope *= slope * slope;
			int strength = Math.abs(maxStrength) * slope - slope;

			if (strength > 0){

				// replaced 'Environment.EROSION_INDEX with slope
				int erosionStrength = rand.get().nextInt(strength);

				if (fromHex.getSoilStability() < erosionStrength){

					fromHex.setElevation(fromHex.getElevation() - 1);

					toHex.setElevation(toHex.getElevation() + 1);

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

		Hex hex = map.getHex(id);
		boolean grew = false;

		//Shouldn't grow out when on fire
		if (hex.getFire() <= 0){

			Plant[] vegetation = hex.getVegetation();
			List<Pair> neighbors = id.getNeighbors();

			for (Plant plant : vegetation){

				for (Pair neighbor : neighbors){

					Hex adjHex = map.getHex(neighbor);

					if (adjHex.getFire() <= 0){

						if (adjHex.addPlant(plant)){

							grew = true;

							if (TheRandom.getInstance().flipCoin()){

								return grew;
							} else{
								break;
							}
						}
					}
				}
			}
		}

		return grew;
	}

	public boolean ignite(Pair id, int flame){

		Hex newHex = map.getHex(id);

		if (newHex != null && flame > newHex.hexFireResistence() && newHex.getSaturation() < 1 && newHex.getHighestVegetation() != null){

			newHex.setFire(newHex.getFireStrength());

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

			if (hex.getSaturation() > 1){

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

	public void forceGrow(Hex hex) {

		int plant = TheRandom.getInstance().get().nextInt(4);

		switch(plant){

		case 0:
			hex.addPlant(new Grass());
			break;

		case 1:
			hex.addPlant(new Thicket());
			break;

		case 2:
			hex.addPlant(new Forest());
			break;

		case 3:
			hex.addPlant(new Jungle());
			break;
		}

	}

	/**
	 * Spreads excess elevation to all neighboring hexes
	 * @param hexID
	 */
	public int topple(Pair id, int toppleCount) {

		List<Pair> neighbors = id.getNeighbors();
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

	// Returns null if no dry neighbors found
	public Pair getTallestDryNeighbor(Pair pair){
		
		List<Pair> neighbors = pair.getNeighbors();
		Hex tallest = null;
		
		for (Pair neighbor : neighbors){
			
			Hex hex = map.getHex(neighbor);
			
			if (hex.getStandingWater() == 0
					&& (tallest == null || hex.getElevation() > tallest.getElevation())){
				
				tallest = hex;
			}
		}
		
		return tallest == null ? null : tallest.getHexID();
	}
}
