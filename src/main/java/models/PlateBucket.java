package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import enums.Direction;

public class PlateBucket {

	private List<TectonicPlate> tectonicPlates;
	private Set<Pair> allEdges;
	private Pair startPoint;
	private Direction direction;
	private List<Pair> allElbows;
	
	public PlateBucket(){
		
		tectonicPlates = new ArrayList<TectonicPlate>();
		allEdges = new HashSet<Pair>();
		allElbows = new ArrayList<Pair>();
	}
	
	public Set<Pair> getAllEdges() {
		return allEdges;
	}
	public void setAllEdges(Set<Pair> allEdges) {
		this.allEdges = allEdges;
	}
	public List<TectonicPlate> getTectonicPlates() {
		return tectonicPlates;
	}
	public void setTectonicPlates(List<TectonicPlate> tectonicPlates) {
		this.tectonicPlates = tectonicPlates;
	}
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	public List<Pair> getAllElbows() {
		return allElbows;
	}
	public void setAllElbows(List<Pair> allElbows) {
		this.allElbows = allElbows;
	}
	public Pair getStartPoint() {
		return startPoint;
	}
	public void setStartPoint(Pair startPoint) {
		this.startPoint = startPoint;
	}
}
