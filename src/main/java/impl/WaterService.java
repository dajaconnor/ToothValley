package impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.ControlDirection;
import enums.DisplayType;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.UserActions;
import models.WaterElevationPair;
import propogation.DirectionalEvaluator;
import propogation.Propogator;

@Component
public class WaterService {

	@Autowired
	HexService hexService;

	@Autowired
	HexMapService hexMapService;

	@Autowired
	WeatherService weatherService;
	
	HexMap map = HexMap.getInstance();

	public void waterCycle(boolean findLeak, boolean leakFound) {

		int totalWater = 0;
		int totalElevation = 0;

		List<Pair> allHexes = new ArrayList<Pair>(map.getHexes().keySet());

		if (findLeak) {

			totalWater = hexMapService.allWater()[0];
		}

		Map<Pair,Pair> displayMap = new HashMap<Pair,Pair>();
		DisplayType displayType = map.getDisplayType();
		map.resetWindBlown();

		leakFound = checkForLeak(findLeak, totalWater, leakFound, "handleWeather leak");
		int alterWaterBy = UserActions.getInstance().getAndResetWaterChangedByUser();
		int numHexes = allHexes.size();
		
		for (int i = 0; i < numHexes; i++) {

			int third = (i * 3) / numHexes;
			int index = (i * 3 + third ) % numHexes;
			Hex hex = map.getHex(allHexes.get( index ));
			int standingWater = hex.getStandingWater();
			
			totalElevation = waterHex(findLeak && !leakFound, totalElevation, displayMap, displayType, alterWaterBy, hex, standingWater);
		}
		
		calibrateWaterLevel();
		calibrateRainLevel();
		calibrateCloudCover();

		if (map.getTicks() % Environment.NORMALIZE_EVEL_FREQ == 0){

			hexMapService.normalizeElevation(allHexes ,totalElevation);
		}

		map.setDisplayMap(displayMap);

		leakFound = checkForLeak(findLeak, totalWater, leakFound, "evaporate or flood leak");
	}
	
	private void calibrateWaterLevel() {
		
		HexMap map = HexMap.getInstance();
		
		int percentStandingWater = (map.getNumStandingWater() * 100) / map.getHexes().size();
		
		if (percentStandingWater > Environment.TARGET_PERCENT_STANDING_WATER) UserActions.getInstance().incrementWaterChangedByUser(1);
		if (percentStandingWater < Environment.TARGET_PERCENT_STANDING_WATER) UserActions.getInstance().incrementWaterChangedByUser(-1);
	
		map.resetNumRained();
	}
	
	private void calibrateRainLevel() {
		
		HexMap map = HexMap.getInstance();
		
		int percentRained = (map.getNumRained() * 100) / map.getHexes().size();
		
		if (percentRained > Environment.TARGET_PERCENT_RAIN) map.adjustRainThreshhold(ControlDirection.UP);
		if (percentRained < Environment.TARGET_PERCENT_RAIN) map.adjustRainThreshhold(ControlDirection.DOWN);
	
		map.resetNumRained();
	}
	
	private void calibrateCloudCover() {
		
		HexMap map = HexMap.getInstance();
		
		int percentInCloud = (map.getNumberOfHexesInCloud() * 100) / map.getHexes().size();
		
		if (percentInCloud > Environment.TARGET_PERCENT_CLOUD_COVER) map.adjustRainThreshhold(ControlDirection.UP);
		if (percentInCloud < Environment.TARGET_PERCENT_CLOUD_COVER) map.adjustRainThreshhold(ControlDirection.DOWN);
	
		map.resetNumberOfHexesInCloud();
	}

	public boolean flood(Hex hex, int standingWater) {

		boolean flooded = false;

		//If there is standing water, shove it around
		if (standingWater > 1) {
			WaterElevationPair elev = hex.getCombinedElevation();
			List<Pair> neighbors = hex.getHexID().getNeighbors();
			flooded = floodInAllDirections(hex, elev, neighbors);
		}

		return flooded;
	}
	
	private boolean floodInAllDirections(Hex hex, WaterElevationPair elev, List<Pair> neighbors) {
		
		WaterElevationPair total = elev.clone();
		Map<Hex, WaterElevationPair> combinedElevTable = new HashMap<Hex, WaterElevationPair>(6);
		WaterElevationPair[] elevs = new WaterElevationPair[6];
		
		int i = 0;
		for (Pair neighbor : neighbors) {
			
			Hex adjHex = map.getHex(neighbor);
			WaterElevationPair combined = adjHex.getCombinedElevation();
			elevs[i] = combined;
			
			if (combined.compareTo(elev) < 0) {
				
				combinedElevTable.put(adjHex, combined);
				total.setX(total.getX() + combined.getX());
				total.setY(total.getY() + combined.getY());
			}
			
			i++;
		}
		
		int average = total.fullWaterHeight() / (combinedElevTable.size() + 1);
		int supply = elev.fullWaterHeight() - average;
		boolean flood = supply > 0;
		
		if (flood) {
			
			for(Hex neighbor : combinedElevTable.keySet()) {
				
				int difference = (average - combinedElevTable.get(neighbor).fullWaterHeight());
				
				if (difference > 0) {
					
					hexService.erode(hex, neighbor, difference);
					neighbor.alterMoisture(hex.alterMoisture(-difference));
				}
			}
		}
		
		return flood;
	}

	private int waterHex(boolean findLeak, int totalElevation, Map<Pair, Pair> displayMap, DisplayType displayType,
			int alterWaterBy, Hex hex, int standingWater ) {

		int neighborhoodWater = 0;
		Pair hexID = hex.getHexID();

		weatherService.handleWeather(hexID);

		if (findLeak) neighborhoodWater = getNeighborhoodTotalWater(hexID);
		
		hexService.evaporate(hex, findLeak, standingWater);
		hex.alterMoisture(alterWaterBy);
		findLeak = checkForLeakInNeighborhood(findLeak, neighborhoodWater, hexID, "evaporate leak");
		applyCloudMovementAndRain(hex);
		findLeak = checkForLeakInNeighborhood(findLeak, neighborhoodWater, hexID, "applyCloudMovementAndRain leak");
		flood(hex, standingWater);
		findLeak = checkForLeakInNeighborhood(findLeak, neighborhoodWater, hexID, "flood leak");
		hexService.topple(hexID, 0);
		findLeak = checkForLeakInNeighborhood(findLeak, neighborhoodWater, hexID, "topple leak");
		hexService.grow(hexID, standingWater);

		Pair displayPair = map.updateHexDisplay(hex, displayType);

		displayMap.put(hexID, displayPair);

		if (map.getTicks() % Environment.NORMALIZE_EVEL_FREQ == 0){

			totalElevation += hex.getElevation();
		}
		
		if (standingWater > 0) {
			map.incrementNumStandingWater();
		}
		
		return totalElevation;
	}

	private boolean checkForLeak(boolean findLeak, int totalWater, boolean leakFound, String message) {

		if (!leakFound){
			
			int newTotal = hexMapService.allWater()[0];
			
			if (findLeak && totalWater != newTotal) {
	
				System.out.println(message + ": " + (newTotal - totalWater));
				leakFound = true;
			}
		}
		
		return leakFound;
	}
	
	private boolean checkForLeakInNeighborhood(boolean findLeak, int neighborhoodWater, Pair centerPair, String message) {

		if (findLeak){
			
			int newNeighborhoodTotal = getNeighborhoodTotalWater(centerPair);
			
			if (neighborhoodWater != newNeighborhoodTotal) {
	
				System.out.println(message + ": " + (newNeighborhoodTotal - neighborhoodWater));
				findLeak = false;
			}
		}
		
		return findLeak;
	}
	
	private int getNeighborhoodTotalWater(Pair centerPair) {
		
		HexMap map = HexMap.getInstance();
		
		int total = map.getHex(centerPair).getTotalWater();
		List<Pair> neighbors = centerPair.getNeighbors();
		
		for (Pair neighbor : neighbors) {
			
			total += map.getHex(neighbor).getTotalWater();
		}
		
		return total;
	}
	
	private void applyCloudMovementAndRain(Hex hex){
		
		int rained = hex.rain();
		
		if (rained > 0) {
			
			new Propogator().propogateFromOrigin(new Flooder(), hex.getHexID());
			HexMap.getInstance().incrementNumRained();
		}
	}
	
	private class Flooder implements DirectionalEvaluator {

		//int total;
		WaterElevationPair elev;
		WaterElevationPair[] elevs;
		int average;
		int supply;
		Map<Hex, WaterElevationPair> combinedElevTable;
		
		@Override
		public boolean initialEvaluate(Pair pairToEvaluate) {
			
			Hex hex = HexMap.getInstance().getHex(pairToEvaluate);
			int standing =  hex.getStandingWater();
			boolean success = standing > 0;
			
			if (success) {
				elev = HexMap.getInstance().getHex(pairToEvaluate).getCombinedElevation();
			
				WaterElevationPair total = elev.clone();
				combinedElevTable = new HashMap<Hex, WaterElevationPair>(6);
				elevs = new WaterElevationPair[6];
				List<Pair> neighbors = pairToEvaluate.getNeighbors();
				
				int i = 0;
				for (Pair neighbor : neighbors) {
					
					Hex adjHex = map.getHex(neighbor);
					WaterElevationPair combined = adjHex.getCombinedElevation();
					elevs[i] = combined;
					
					if (combined.compareTo(elev) < 0) {
						
						combinedElevTable.put(adjHex, combined);
						total.addPair(combined);
					}
					
					i++;
				}
				
				average = total.fullWaterHeight() / (combinedElevTable.size() + 1);
				supply = ((elev.fullWaterHeight() - average) * Environment.WATER_VELOCITY) / 100;
			}
			
			return success;
		}

		@Override
		public void onInitialSuccess(Pair pairToEvaluate) {

			
		}

		@Override
		public void onInitialFail(Pair pairToEvaluate) {
			
		}

		@Override
		public boolean evaluate(Pair pairToEvaluate, Pair originatingPair) {

			int difference = 0;

			if (supply > 0) {
				
				Hex neighbor = HexMap.getInstance().getHex(pairToEvaluate);

				if (combinedElevTable.containsKey(neighbor)) {

					difference = (average - combinedElevTable.get(neighbor).fullWaterHeight());
					
					if (difference > 0) {
						
						Hex hex = HexMap.getInstance().getHex(originatingPair);
						difference = neighbor.alterMoisture(hex.alterMoisture(-difference));
					}
				}
			}
			
			return difference > 0;
		}

		@Override
		public void onSuccess(Pair pairToEvaluate, Pair originatingPair) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFail(Pair pairToEvaluate, Pair originatingPair) {
			// TODO Auto-generated method stub
			
		}
		
		
	}
}
