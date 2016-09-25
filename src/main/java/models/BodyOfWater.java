package models;

import impl.HexService;
import impl.WaterService;
import propogation.DirectionalEvaluator;
import propogation.Evaluator;
import propogation.Propogator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

public class BodyOfWater {

	@Autowired
	private HexService hexService;

	@Autowired
	private WaterService waterService;

	@Autowired
	private Propogator propogator;

	private Set<Pair> allMembers;
	private int floorElevation; // sum of elevation of all members
	private int water; // sum of standing water of all members
	private Map<Integer, Set<Pair>> elevationMap;
	private Map<Pair, Pair> connectivityMap;
	private Map<Pair, Set<Pair>> connectivityChildMap;
	private int waterLineLastChecked;

//	public BodyOfWater(Set<Pair> newMembers) {
//
//		setAllMembers(newMembers);
//		computeFloorElevation();
//		computeWater();
//		computeElevationMap();
//		buildConnectivityMap();
//		setWaterLineLastChecked(getWaterLine());
//	}

	public BodyOfWater(Pair startingMember) {

		computeAndSetMembers(startingMember);
		buildConnectivityMap();
		waterLineLastChecked = getWaterLine();
	}
	
	// Returns all disconnected pairs
	public List<Pair> HandleWaterLineChanges(){
	   
	   // make sure to handle connectivity in here
	   
	   int currentWaterLine = getWaterLine();
	   
	   if (currentWaterLine == waterLineLastChecked) return new ArrayList<Pair>();
	   
	   if (currentWaterLine > waterLineLastChecked){
	      
	      // Add members
	      // Consume any other bodies encountered, removing them from map
	      
	      
	   }
	   
	   if (currentWaterLine < waterLineLastChecked){
	      
	      // remove members
	      // add all detached members to return list and remove them
	      
	      Set<Entry<Integer, Set<Pair>>> entrySet = getElevationMap().entrySet();
	      Set<Pair> orphanedMembers = new HashSet<Pair>();
	      
	      for (Entry<Integer, Set<Pair>> entry : entrySet){
	         
	         if (entry.getKey() >= currentWaterLine){
	            
	            for (Pair pair : entry.getValue()){
	               
	               removeMember(pair);
	               Set<Pair> orphans = removeFromConnectivityMap(pair);
	               
	               if (orphans != null){
	                  orphanedMembers.addAll(orphans);
	               }
	            }
	         }
	      }
	      
	      for (Pair orphan : orphanedMembers){
	         
	         if (getAllMembers().contains(orphan)){
	            
	         }
	      }
	   }
	   
	   return null;
	}

	private void computeAndSetMembers(Pair startingMember) {

		HexMap map = HexMap.getInstance();

		if (map.getHexes().get(startingMember).getStandingWater() == 0) {

			return;
		}

		waterService.addNode(startingMember);

		propogator.propogate(new AllBodyMembersCommandBuilder(), startingMember);
	}
	
	private class AllBodyMembersCommandBuilder implements Evaluator{

      public boolean actAndEvaluate(Pair pairToEvaluate) {
         
         HexMap map = HexMap.getInstance();
         
         boolean addingToBody = map.getHexes().get(pairToEvaluate).getStandingWater() > 0;
         
         if (addingToBody) addMember(pairToEvaluate);
         
         return addingToBody;
      }
   }

	/*
	 * Must have an updated elevation map to function right
	 */
	private void buildConnectivityMap() {

		Pair startingPoint = getDeepestPair();

		if (startingPoint == null) {
			try {
				throw new Exception("Elevation map is empty when building connectivity map");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		propogator.propogateFromOrigin(new ConnectivityMapCommandBuilder(), startingPoint);
	}

	private class ConnectivityMapCommandBuilder implements DirectionalEvaluator{

      public boolean actAndEvaluate(Pair pairToEvaluate, Pair originatingPair) {
         
         boolean isInBody = getAllMembers().contains(pairToEvaluate);
         
         if (isInBody) addToConnectivityMap(pairToEvaluate, originatingPair);
         
         return isInBody;
      }
	}

	private Pair getDeepestPair() {

		Set<Entry<Integer, Set<Pair>>> keySet = getElevationMap().entrySet();
		Entry<Integer, Set<Pair>> lowestSet = null;

		for (Entry<Integer, Set<Pair>> entry : keySet) {

			if (lowestSet == null || (entry.getKey() < lowestSet.getKey() && !entry.getValue().isEmpty())) {
				lowestSet = entry;
			}
		}

		if (lowestSet == null)
			return null;

		return lowestSet.getValue().iterator().next();
	}
	
	private boolean isConnected(Pair pair) throws Exception{

	   HashSet<Pair> traversedPairs = new HashSet<Pair>();

	   while(getConnectivityMap().containsKey(pair)){

	      if (traversedPairs.contains(pair)){
	         
	         throw new Exception("Circular connectivity detected.  Rebuild connectivity map.");
	      }
	      
	      if (getConnectivityMap().get(pair).equals(pair)) return true;

	      pair = getConnectivityMap().get(pair);
	   }

	   return false;
	}

	// this needs to be redone... going to just create a whole new body and then
	// remove all those members
	// from the original body
//	public void handleSplits() {
//
//		Pair startingPair = getAllMembers().iterator().next();
//		List<Pair> nextList = new ArrayList<Pair>();
//		nextList.add(startingPair);
//		Set<Pair> connectedPairs = new HashSet<Pair>();
//		connectedPairs.add(startingPair);
//
//		do {
//
//			Set<Pair> neighbors = new HashSet<Pair>();
//
//			for (Pair pair : nextList) {
//
//				neighbors.addAll(hexService.getNeighborsSet(pair));
//			}
//
//			nextList = new ArrayList<Pair>();
//
//			for (Pair pair : neighbors) {
//
//				if (getAllMembers().contains(pair) && !connectedPairs.contains(pair)) {
//
//					connectedPairs.add(pair);
//					nextList.add(pair);
//				}
//			}
//		} while (nextList.size() > 0);
//
//		if (connectedPairs.size() != getAllMembers().size()) {
//
//			Set<Pair> disconnectedPairs = new HashSet<Pair>();
//
//			for (Pair pair : getAllMembers()) {
//
//				if (!connectedPairs.contains(pair)) {
//
//					disconnectedPairs.add(pair);
//				}
//			}
//
//			handleDisconnectedPairs(disconnectedPairs);
//		}
//	}

//	private void handleDisconnectedPairs(Set<Pair> disconnectedPairs) {
//		// TODO Auto-generated method stub
//
//	}

	public void updateHexElevation(Hex hex, int newElevation) {

		if (getAllMembers().contains(hex.getHexID())) {

			if (hex.getElevation() != newElevation) {

				setFloorElevation(getFloorElevation() + newElevation - hex.getElevation());
				removeFromElevationMap(hex.getHexID(), hex.getElevation());
				addToElevationMap(hex.getHexID(), newElevation);
			}
		}
	}

	private void computeElevationMap() {

		Iterator<Pair> iterator = getAllMembers().iterator();
		HexMap map = HexMap.getInstance();

		while (iterator.hasNext()) {

			Pair nextPair = iterator.next();
			int elevation = map.getHex(nextPair).getElevation();

			addToElevationMap(nextPair, elevation);
		}
	}

//	private void removeFromElevationMap(Pair pair, Integer elevation) {
//
//		if (getElevationMap().containsKey(elevation)) {
//
//			getElevationMap().get(elevation).remove(pair);
//		}
//	}
//
//	private void addToElevationMap(Pair pair, Integer elevation) {
//
//		if (getElevationMap().get(elevation) == null) {
//
//			getElevationMap().put(elevation, new HashSet<Pair>());
//		}
//
//		getElevationMap().get(elevation).add(pair);
//	}

	private void computeFloorElevation() {

		Iterator<Pair> iterator = getAllMembers().iterator();
		HexMap map = HexMap.getInstance();
		setFloorElevation(0);

		while (iterator.hasNext()) {

			setFloorElevation(getFloorElevation() + map.getHex(iterator.next()).getElevation());
		}
	}

	private void computeWater() {

		Iterator<Pair> iterator = getAllMembers().iterator();
		HexMap map = HexMap.getInstance();
		setWater(0);

		while (iterator.hasNext()) {

			setWater(getWater() + map.getHex(iterator.next()).getStandingWater());
		}
	}

	private void addMember(Pair member) {

		getAllMembers().add(member);
		waterService.addNode(member);

		HexMap map = HexMap.getInstance();

		adjustWater(map.getHex(member).getStandingWater());

		int elevation = map.getHex(member).getElevation();
		adjustFloorElevation(elevation);
		addToElevationMap(member, elevation);
	}

	private void removeMember(Pair member) {

		getAllMembers().remove(member);
		waterService.removeNode(member); // TODO handle split

		HexMap map = HexMap.getInstance();

		adjustWater(-map.getHex(member).getStandingWater());

		int elevation = map.getHex(member).getElevation();
		adjustFloorElevation(-elevation);
		removeFromElevationMap(member, elevation);
	}

	public Set<Pair> getAllMembers() {
		return allMembers;
	}

	public void setAllMembers(Set<Pair> allMembers) {
		this.allMembers = allMembers;
	}

	public int getFloorElevation() {
		return floorElevation;
	}

	public void setFloorElevation(int floorElevation) {
		this.floorElevation = floorElevation;
	}

	public void adjustFloorElevation(int change) {
		this.floorElevation += change;
	}

	public int getWater() {
		return water;
	}

	public void setWater(int water) {
		this.water = water;
	}

	public void adjustWater(int change) {
		this.water += change;
	}

	public int getWaterLine() {
		return (getWater() / Environment.WATER_PER_ELEVATION + getFloorElevation()) / getAllMembers().size();
	}

	private void addToElevationMap(Pair member, int elevation) {
		if (!getElevationMap().containsKey(elevation)) {
			getElevationMap().put(elevation, new HashSet<Pair>());
		}
		getElevationMap().get(elevation).add(member);
	}

	private void removeFromElevationMap(Pair member, int elevation) {
		if (getElevationMap().containsKey(elevation)) {
			getElevationMap().get(elevation).remove(member);
		}
	}

	// TODO removeElevationFromBody
	public Map<Integer, Set<Pair>> getElevationMap() {
	   
	   if (elevationMap == null){
	      elevationMap = new HashMap<Integer, Set<Pair>>();
      }
      
		return elevationMap;
	}

   private Map<Pair, Pair> getConnectivityMap() {
      
      if (connectivityMap == null){
         connectivityMap = new HashMap<Pair, Pair>();
      }
      
      return connectivityMap;
   }
   
   private void addToConnectivityMap(Pair child, Pair parent) {
      connectivityMap.put(child, parent);
      addToConnectivityChildMap(child,parent);
   }
   
   // returns orphaned children
   private Set<Pair> removeFromConnectivityMap(Pair pair) {
      connectivityMap.remove(pair);
      return connectivityChildMap.get(pair);
   }

   private void addToConnectivityChildMap(Pair child, Pair parent){
      
      if (connectivityChildMap == null){
         connectivityChildMap = new HashMap<Pair, Set<Pair>>();
      }

      if (!connectivityChildMap.containsKey(parent)){
         connectivityChildMap.put(parent, new HashSet<Pair>());
      }
      connectivityChildMap.get(parent).add(child);
   }
}
