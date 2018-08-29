package impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.DisplayType;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.UserActions;

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

		for (Pair hexID : allHexes) {

			Hex hex = map.getHex(hexID);

			weatherService.handleWeather(hexID);
			
			hexService.evaporate(hex, findLeak);
			hex.alterMoisture(alterWaterBy);
			//leakFound = checkForLeak(findLeak, totalWater, leakFound, "evaporate leak");
			applyCloudMovementAndRain(hex);
			//leakFound = checkForLeak(findLeak, totalWater, leakFound, "applyCloudMovementAndRain leak");
			hexService.flood(hex);
			//leakFound = checkForLeak(findLeak, totalWater, leakFound, "flood leak");
			hexService.topple(hexID, 0);
			//leakFound = checkForLeak(findLeak, totalWater, leakFound, "topple leak");
			hexService.grow(hexID);

			Pair displayPair = map.updateHexDisplay(hex, displayType);

			displayMap.put(hexID, displayPair);

			if (map.getTicks() % Environment.NORMALIZE_EVEL_FREQ == 0){

				totalElevation += hex.getElevation();
			}
		}
		
		if (UserActions.getInstance().realisticWaterFlow()) flowAllHexesAgain(allHexes);
		
		map.resetAppliedBlown();

		if (map.getTicks() % Environment.NORMALIZE_EVEL_FREQ == 0){

			hexMapService.normalizeElevation(allHexes ,totalElevation);
		}

		map.setDisplayMap(displayMap);

		leakFound = checkForLeak(findLeak, totalWater, leakFound, "evaporate or flood leak");
	}

	private void flowAllHexesAgain(List<Pair> allHexes) {
		for (Pair hexID : allHexes) {

			Hex hex = map.getHex(hexID);
			hexService.flood(hex);
		}
	}

	private boolean checkForLeak(boolean findLeak, int totalWater, boolean leakFound, String message) {

		if (!leakFound){
			
			if (findLeak && totalWater != hexMapService.allWater()[0]) {
	
				System.out.println(message + ": " + (hexMapService.allWater()[0] - totalWater));
				leakFound = true;
			}
		}
		
		return leakFound;
	}
	
	private void applyCloudMovementAndRain(Hex hex){
		
		HexMap.getInstance().getAppliedBlown().add(hex.getHexID());
		//hex.resolveMoistureInAir();
		//hex.resolveMoisture();
		
		// If max rain
		if (hex.rain()){
			
			List<Pair> neighbors = hex.getHexID().getNeighbors();
			int toDistribute = Environment.MAX_RAINFALL_PER_TICK / neighbors.size();
			
			for (Pair neighbor : neighbors){
				
				map.alterMoisture(neighbor, map.alterMoisture(hex, - toDistribute));
			}
		}
	}
}
