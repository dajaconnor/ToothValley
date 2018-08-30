package impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.Direction;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import propogation.DirectionalEvaluator;
import propogation.Propogator;

@Component
public class WeatherService {

	@Autowired
	HexService hexService;

	public void handleWeather(Pair pair){

		HexMap map = HexMap.getInstance();

		// collect the cloud
		if (new Propogator().propogateFromOrigin(new CloudCollector(), pair)){
			
			List<Pair> hexesInCloud = map.getHexesInCloud();
			Direction direction = map.getCoriolis(pair.getX());
			if (direction == null) direction = map.getDirectionToTarget();
			if (direction == null) direction = Direction.randomDirection();
			
			for (Pair cloudPair : hexesInCloud){
				
				Pair moveTo = cloudPair.getHexIdFromDirection(direction);
				map.getWindBlown().add(cloudPair);
				moveAllMoistureInAir(map.getHex(cloudPair), moveTo);
			}
			
			map.resetHexesInCloud();
		}
	}

	private void moveAllMoistureInAir(Hex from, Pair to) {

		HexMap map = HexMap.getInstance();
		int amount = from.getMoistureInAir();

		if (amount <= 0)
			return;

		// this does the sanity checks for you
		map.getHex(to).alterMoistureInAir(from.alterMoistureInAir(-amount));
	}

	public class CloudCollector implements DirectionalEvaluator {

		public boolean initialEvaluate(Pair pairToEvaluate) {

			HexMap map = HexMap.getInstance();
			
			if (map.getWindBlown().contains(pairToEvaluate))
				return false;
			map.getWindBlown().add(pairToEvaluate);

			Hex hex = map.getHex(pairToEvaluate);

			return isCloud(hex);
		}

		// reset values to start a new cloud formation
		public void onInitialSuccess(Pair pairToEvaluate) {

			HexMap map = HexMap.getInstance();
			
			map.setTallestTargetForCloud(-1000);
			map.setDirectionToTarget(null);

			List<Pair> hexesInCloud = new ArrayList<Pair>();
			hexesInCloud.add(pairToEvaluate);

			map.setHexesInCloud(hexesInCloud);
		}

		// it's not a cloud, so just move it like normal
		public void onInitialFail(Pair pairToEvaluate) {

			Pair blowTo = hexService.getTallestDryNeighbor(pairToEvaluate);

			if (blowTo == null)
				blowTo = pairToEvaluate.getRandomNeighbor();

			moveAllMoistureInAir(HexMap.getInstance().getHex(pairToEvaluate), blowTo);
		}

		// update direction data
		public boolean evaluate(Pair pairToEvaluate, Pair originatingPair) {
			
			HexMap map = HexMap.getInstance();
			
			if (map.getWindBlown().contains(pairToEvaluate))
				return false;
			map.getWindBlown().add(pairToEvaluate);
			
			Hex hex = map.getHex(pairToEvaluate);

			if (hex.getElevation() > map.getTallestTargetForCloud()) {

				map.setTallestTargetForCloud(hex.getElevation());
				map.setDirectionToTarget(originatingPair.getDirectionToPair(pairToEvaluate));
			}

			return isCloud(hex);
		}

		private boolean isCloud(Hex hex) {
			return (hex.getMoistureInAir() > Environment.CLOUD 
					&& HexMap.getInstance().getHexesInCloud().size() <= Environment.MAX_CLOUD_SIZE);
		}

		public void onSuccess(Pair pairToEvaluate, Pair originatingPair) {

			HexMap map = HexMap.getInstance();
			map.getWindBlown().add(pairToEvaluate);
			map.getHexesInCloud().add(pairToEvaluate);
		}

		public void onFail(Pair pairToEvaluate, Pair originatingPair) {

			// do nothing
		}
	}
}
