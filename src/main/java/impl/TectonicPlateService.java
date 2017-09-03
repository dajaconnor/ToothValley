package impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.Direction;
import models.Environment;
import models.HexMap;
import models.Pair;
import models.PlateBucket;
import models.TectonicPlate;
import models.TheRandom;

@Component
public class TectonicPlateService {

	@Autowired
	HexService hexService;
	
	public List<TectonicPlate> generateTectonicPlates(Pair mapSize) {

		PlateBucket bucket = new PlateBucket();

		do{

			generateStartPoint(bucket);
			bucket = drawEdge(bucket);
		}while(bucket.getAllEdges().size() < mapSize.getX() * mapSize.getY() / Environment.PLATE_EDGE_FREQUENCY
				&& bucket.getAllElbows().size() > 0);

		return drawPlates(bucket);
	}
	
	private List<TectonicPlate> drawPlates(PlateBucket bucket) {
		
		List<TectonicPlate> plates = new ArrayList<TectonicPlate>();
		Set<Pair> claimedEdgeSides = new HashSet<Pair>();
		
		// first look for the side of an edge that's not claimed
		for (Pair pair : HexMap.getInstance().getHexes().keySet()){
	
			if (isUnclaimedEdgeSide(bucket, claimedEdgeSides, pair)){
				
				plates.add(tracePlate(bucket, claimedEdgeSides, pair, new TectonicPlate()));
			}
		}
		
		for (TectonicPlate plate : plates){
			
			plate.setDirection(Direction.randomDirection());
		}

		return plates;
	}

	private boolean isUnclaimedEdgeSide(PlateBucket bucket, Set<Pair> claimedEdgeSides, Pair pair) {
		
		if (!bucket.getAllEdges().contains(pair) && !claimedEdgeSides.contains(pair)){
			
			List<Pair> neighbors = pair.getNeighbors();
			
			for (Pair neighbor : neighbors){
				
				if (bucket.getAllEdges().contains(neighbor)){
					
					return true;
				}	
			}
		}
		
		return false;
	}
	
	private TectonicPlate tracePlate(PlateBucket bucket,
			Set<Pair> claimedEdgeSides, Pair pair, TectonicPlate plate) {
		
		plate.getInnerRing().add(pair);
		HexMap.getInstance().getHex(pair).setTectonicState((Integer)null);
		claimedEdgeSides.add(pair);
		List<Pair> neighbors = pair.getNeighbors();
		
		for (Pair neighbor : neighbors){
			
			if (bucket.getAllEdges().contains(neighbor)){
				
				plate.getAllEdges().add(neighbor);
			}
			
			else if (isUnclaimedEdgeSide(bucket, claimedEdgeSides, neighbor)){

				plate = tracePlate(bucket, claimedEdgeSides, neighbor, plate);
			}
		}
		
		return plate;
	}

	private PlateBucket drawEdge(PlateBucket bucket) {

		Set<Pair> edge = new HashSet<Pair>();
		
		edge.add(bucket.getStartPoint());
		HexMap.getInstance().getHex(bucket.getStartPoint()).setTectonicState(0);

		Pair nextPair = bucket.getStartPoint().getHexIdFromDirection(bucket.getDirection());
		Direction nextDirection = bucket.getDirection();
		
		int firstStretch = 0;
		int afterTurn = 0;
		
		while(!bucket.getAllEdges().contains(nextPair) && !edge.contains(nextPair)){
			
			edge.add(nextPair);

			if (firstStretch < Environment.TECTONIC_FIRST_STRETCH){
				firstStretch++;
			} else if(afterTurn++ >= Environment.TECTONIC_MIN_STRAIGHT){

				nextDirection = getEdgeDirection(bucket, nextPair);
			}

			if (bucket.getDirection() != nextDirection){
				bucket.getAllElbows().add(nextPair);
				afterTurn = 0;
			}

			nextPair = nextPair.getHexIdFromDirection(nextDirection);
			bucket.setDirection(nextDirection);
		}

		for (Pair pair : edge){

			HexMap.getInstance().getHex(pair).setTectonicState(0);
			bucket.getAllEdges().add(pair);
		}

		return bucket;
	}
	
	private Direction getEdgeDirection(PlateBucket bucket, Pair currentPair) {
		
		Direction returnDirection = bucket.getDirection();
		Direction headToFinish = findCloseEdgeAndGoToIt(bucket, currentPair);
		
		if (headToFinish == null){
		
		   
			int randInt = TheRandom.getInstance().get().nextInt(100);
	
			if (randInt >= Environment.PERCENT_STRAIGHT_PLATE_EDGE){
				
				randInt -= Environment.PERCENT_STRAIGHT_PLATE_EDGE;
				
				if (randInt < (100 - Environment.PERCENT_STRAIGHT_PLATE_EDGE) / 2){
					returnDirection = Direction.values()[(returnDirection.ordinal() + Direction.values().length - 1) % Direction.values().length];
				} else {
					returnDirection = Direction.values()[(returnDirection.ordinal() + 1) % Direction.values().length];
				}
			}
		} else {
			returnDirection = headToFinish;
		}

		return returnDirection;
	}

	private Direction findCloseEdgeAndGoToIt(PlateBucket bucket, Pair currentPair) {

		List<Pair> layer = new ArrayList<Pair>();
		
		layer.add(currentPair);
		
		for (int i = 0; i < Environment.TECTONIC_FIRST_STRETCH; i++){
			
			List<Pair> nextLayer = new ArrayList<Pair>();
			
			for (int n = 0; n < layer.size(); n++){
				
				if (n == 0){
					
					Pair pair = layer.get(n).getHexIdFromDirection(bucket.getDirection().turnLeft());

					if (bucket.getAllEdges().contains(pair)){
						return bucket.getDirection().turnLeft();
					}
					
					nextLayer.add(pair);
				}
				
				Pair pair = layer.get(n).getHexIdFromDirection(bucket.getDirection());

				if (bucket.getAllEdges().contains(pair)){
					return bucket.getDirection();
				}
				
				nextLayer.add(pair);
				
				if (n + 1 == layer.size()){
					
					pair = layer.get(n).getHexIdFromDirection(bucket.getDirection().turnRight());

					if (bucket.getAllEdges().contains(pair)){
						return bucket.getDirection().turnRight();
					}
					
					nextLayer.add(pair);
				}
			}
			
			layer = nextLayer;
		}

		return null;
	}

	private void generateStartPoint(PlateBucket bucket) {

		if (bucket.getTectonicPlates().size() != 1){
			if (bucket.getAllElbows() != null && bucket.getAllElbows().size() > 0){
				bucket.setStartPoint(bucket.getAllElbows().get(TheRandom.getInstance().get().nextInt(bucket.getAllElbows().size())));
				bucket.setDirection(getElbowStartDirection(bucket, bucket.getStartPoint()));
			} else{
				bucket.setStartPoint(hexService.getRandomPair());
				bucket.setDirection(hexService.getRandomDirection());
			}
			
		} else {// just use the other end of the first plate
			bucket.setDirection(getElbowStartDirection(bucket, bucket.getStartPoint()));
		}
		
		// If it didn't work... try again?  Hope this doesn't blow up...
		if (!bucket.getStartPoint().inBounds()){
			
			generateStartPoint(bucket);
		}
	}

	private Direction getElbowStartDirection(PlateBucket bucket, Pair elbowPair) {

		List<Pair> neighbors = elbowPair.getNeighbors();
		Pair nextPair = null;
		Direction returnDirection = null;

		bucket.getAllElbows().remove(elbowPair);
		int openSpaces = 0;

		for (int i = 0; i <= neighbors.size(); i++){
			
			if (bucket.getAllEdges().contains(neighbors.get(i % neighbors.size()))){
				
				openSpaces = 0;
			} else {
				
				if (openSpaces > 1){
				
					nextPair = neighbors.get((i + neighbors.size() - 1) % neighbors.size());
					break;
				}
				
				openSpaces++;
			}
		}
		
		if (nextPair != null){
		
			returnDirection = elbowPair.getDirectionToPair(nextPair);

		} else {
			generateStartPoint(bucket);
		}

		return returnDirection;
	}
}
