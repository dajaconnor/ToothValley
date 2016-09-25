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
	private Set<Pair> shore;
	private Map<Pair, Pair> connectivityMap;
	private Map<Pair, Set<Pair>> connectivityChildMap;
	private int waterLineLastChecked;
	private final static boolean HIGHEST = true;
	private final static boolean LOWEST = false;
	private Pair originalMember;

	public BodyOfWater(Pair startingMember) {

	   originalMember = startingMember;
		computeAndSetMembers(startingMember);
		buildConnectivityMap();
		waterLineLastChecked = getWaterLine();
	}
	
	@Override
   public int hashCode() {

      return originalMember.hashCode();
   }
	
	// Returns all disconnected pairs
	public List<Pair> HandleWaterLineChanges(){
	   
	   // make sure to handle connectivity in here
	   
	   List<Pair> returnedOrphans = new ArrayList<Pair>();
	   int currentWaterLine = getWaterLine();
	   
	   if (currentWaterLine == waterLineLastChecked) return new ArrayList<Pair>();
	   
	   // Add members
      // Consume any other bodies encountered, removing them from map
	   if (currentWaterLine > waterLineLastChecked){
	      
	      List<Pair> shoreList = new ArrayList<Pair>(shore);
	      
	      for(Pair beach : shoreList){
	         
	         propogator.propogate(new FloodCommand(), beach);
	      }
	   }
	   
	   // remove members
      // add all detached members to return list and remove them
	   if (currentWaterLine < waterLineLastChecked){

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
	            
	            if (!tryConnectOrphan(orphan)){
	               
	               returnedOrphans.add(orphan);
	            }
	         }
	      }
	   }
	   
	   return returnedOrphans;
	}
	
	private boolean tryConnectOrphan(Pair orphan){
	   
	   List<Pair> neighbors = hexService.getNeighbors(orphan);
	   
	   for (Pair neighbor : neighbors){
	      
	      try {
            if (isConnected(neighbor)){
               
               addToConnectivityMap(orphan, neighbor);
               return true;
            }
         } catch (Exception e) {
            
            return false;
         }
	   }
	   
	   return false;
	}

	private void computeAndSetMembers(Pair startingMember) {

		HexMap map = HexMap.getInstance();

		if (map.getHexes().get(startingMember).getStandingWater() == 0) {

			return;
		}

		waterService.addToMapWaterBodies(startingMember, this);

		propogator.propogate(new FloodCommand(), startingMember);
	}
	
	private class RetreatCommand implements Evaluator{
	   
	   HexMap map = HexMap.getInstance();
      
      public boolean evaluate(Pair pairToEvaluate) {
         
         return map.getHexes().get(pairToEvaluate).getStandingWater() <= 0;
      }

      public void onSuccess(Pair pairToEvaluate) {

         removeMember(pairToEvaluate);         
      }

      public void onFail(Pair pairToEvaluate) {

         // Do nothing
      }
	}
	
	private class FloodCommand implements Evaluator{

	   HexMap map = HexMap.getInstance();
	   
      public boolean evaluate(Pair pairToEvaluate) {
         
         return !getAllMembers().contains(pairToEvaluate) && map.getHexes().get(pairToEvaluate).getStandingWater() > 0;
      }

      public void onSuccess(Pair pairToEvaluate) {

         addMember(pairToEvaluate);         
      }

      public void onFail(Pair pairToEvaluate) {
         
         addToShore(pairToEvaluate);
      }
   }

	/*
	 * Must have an updated elevation map to function right
	 */
	public void buildConnectivityMap() {

		Integer lowestPoint = getExtremePoint(LOWEST);

		if (lowestPoint == null) {
			try {
				throw new Exception("Elevation map is empty when building connectivity map");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Pair startingPoint = getElevationMap().get(lowestPoint).iterator().next();
		
		propogator.propogateFromOrigin(new ConnectivityMapCommandBuilder(), startingPoint);
	}

	private class ConnectivityMapCommandBuilder implements DirectionalEvaluator{

      public boolean initialEvaluate(Pair pairToEvaluate) {

         return getAllMembers().contains(pairToEvaluate);
      }

      public void onInitialSuccess(Pair pairToEvaluate) {

         addToConnectivityMap(pairToEvaluate, pairToEvaluate);
      }

      public void onInitialFail(Pair pairToEvaluate) {
         
      }

      public boolean evaluate(Pair pairToEvaluate, Pair originatingPair) {

         return getAllMembers().contains(pairToEvaluate);
      }

      public void onSuccess(Pair pairToEvaluate, Pair originatingPair) {

         addToConnectivityMap(pairToEvaluate, originatingPair);
      }

      public void onFail(Pair pairToEvaluate, Pair originatingPair) {

      }
	}

	private Integer getExtremePoint(boolean highest) {

		Set<Integer> keySet = getElevationMap().keySet();
		Integer extremestPoint = null;

		for (Integer key : keySet) {

			if (extremestPoint == null || (!getElevationMap().get(key).isEmpty())
			      && ((!highest && key < extremestPoint) || (highest && key > extremestPoint))) {

			   extremestPoint = key;
			}
		}

		return extremestPoint;
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

	   removeFromShore(member);
		getAllMembers().add(member);
		waterService.addToMapWaterBodies(member, this);

		HexMap map = HexMap.getInstance();

		adjustWater(map.getHex(member).getStandingWater());

		int elevation = map.getHex(member).getElevation();
		adjustFloorElevation(elevation);
		addToElevationMap(member, elevation);
	}

	private void removeMember(Pair member) {

	   HexMap map = HexMap.getInstance();	   
	   List<Pair> neighbors = hexService.getNeighbors(member);
	   
	   for (Pair neighbor : neighbors){
	      
	      if (getAllMembers().contains(neighbor)){
	         
	         addToShore(neighbor);
	         break;
	      }
	   }
	   
		getAllMembers().remove(member);
		waterService.removeNode(member); // TODO handle split

		

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
   
   private void addToShore(Pair pair){
      
      if (shore == null){
         shore = new HashSet<Pair>();
      }
      
      shore.add(pair);
   }
   
   private void removeFromShore(Pair pair){
      
      if (shore == null){
         shore = new HashSet<Pair>();
      }
      
      shore.remove(pair);
   }
}
