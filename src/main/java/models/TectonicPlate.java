package models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import enums.Direction;
import enums.TectonicEdgeDirection;
import impl.WeatherService.CloudCollector;
import propogation.Evaluator;
import propogation.Propogator;

public class TectonicPlate {
	
	private Map<Pair, TectonicEdgeDirection> activeEdges;
	private Set<Pair> allEdges;
	private Set<Pair> innerRing;
	private Direction direction;
	private int verticalDirection = 0;
	private float velocity;
	
	public TectonicPlate(){
		
		setActiveEdges(new HashMap<Pair, TectonicEdgeDirection>());
		setAllEdges(new HashSet<Pair>());
		setInnerRing(new HashSet<Pair>());
	}
	
	public void handleVerticalChange(){
		
		if (innerRing.isEmpty()) return;
		
		for (Pair pair : innerRing){
			
			new Propogator().propogate(new PlateCollector(), pair);
			break;
		}
	}
	
	public Map<Pair, TectonicEdgeDirection> getActiveEdges() {
		return activeEdges;
	}
	public void setActiveEdges(Map<Pair, TectonicEdgeDirection> edges) {
		this.activeEdges = edges;
	}
	public Set<Pair> getAllEdges() {
		return allEdges;
	}
	public void setAllEdges(Set<Pair> allEdges) {
		this.allEdges = allEdges;
	}
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction plateDirection) {
		
		activeEdges = new HashMap<Pair, TectonicEdgeDirection>();
		
		for (Pair edge : allEdges){

			TectonicEdgeDirection edgeDirection = getEdgeDirection(edge, plateDirection);
			
			if (edgeDirection != TectonicEdgeDirection.STABLE){
				
				activeEdges.put(edge, edgeDirection);
			}
		}
		
		this.direction = plateDirection;
	}
	public int getVerticalDirection() {
		return verticalDirection;
	}

	public void setVerticalDirection(int verticalDirection) {
		
		if (verticalDirection > 1) this.verticalDirection = 1;
		if (verticalDirection < -1) this.verticalDirection = -1;
		
		this.verticalDirection = verticalDirection;
	}

	public float getVelocity() {
		return velocity;
	}
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}
	
	private TectonicEdgeDirection getEdgeDirection(Pair edge, Direction plateDirection){
		
		TectonicEdgeDirection returnDirection = TectonicEdgeDirection.STABLE;
		Pair targetPair = edge.getHexIdFromDirection(plateDirection);

		if (innerRing.contains(targetPair)){
			returnDirection = TectonicEdgeDirection.DOWN;
		} else if (!allEdges.contains(targetPair)){
			returnDirection = TectonicEdgeDirection.UP;
		}
		
		HexMap.getInstance().getHex(targetPair).setTectonicState(returnDirection);
		
		return returnDirection;
	}
	public Set<Pair> getInnerRing() {
		return innerRing;
	}
	public void setInnerRing(Set<Pair> innerRing) {
		this.innerRing = innerRing;
	}
	
	private class PlateCollector implements Evaluator {

		public boolean evaluate(Pair pairToEvaluate) {

			return !allEdges.contains(pairToEvaluate);
		}

		public void onSuccess(Pair pairToEvaluate) {
			
			HexMap.getInstance().getHex(pairToEvaluate).alterElevation(verticalDirection);
		}

		public void onFail(Pair pairToEvaluate) {
			
		}
		
		
	}
}
