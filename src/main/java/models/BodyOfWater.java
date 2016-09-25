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
	private Pair originalMember;
	private List<Pair> hexesToCheckForElevation = new ArrayList<Pair>();

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
	
	public void mergeInOtherBody(BodyOfWater otherBody){

	   otherBody.recomputeWaterline();
	   
	   // find all shared pairs
      Set<Pair> intersection = new HashSet<Pair>(otherBody.getAllMembers()); // use the copy constructor
      intersection.retainAll(getAllMembers());
      
      adjustFloorElevation(otherBody.getFloorElevation());
      adjustWater(otherBody.getWater());
      
      subtractDoubleCountedPairs(intersection, otherBody);
      
      mergeElevationMap(otherBody.getElevationMap());
      
      getAllMembers().addAll(otherBody.getAllMembers());
      
      mergeShore(otherBody.getShore()); // must happen after all member merge
      buildConnectivityMap();
	}
	
	private void subtractDoubleCountedPairs(Set<Pair> doubleCounted, BodyOfWater otherBody){
	   
	   HexMap map = HexMap.getInstance();
	   
	   for (Pair pair : doubleCounted){
         
         adjustFloorElevation(-map.getHex(pair).getElevation());
         adjustWater(-map.getStaleHexBodyStandingWater(pair));
      }
	}
	
	private void mergeElevationMap(Map<Integer, Set<Pair>> mapToMerge){
	   
	   for (Entry<Integer, Set<Pair>> entry : mapToMerge.entrySet()){
	      
	      mergeElevationEntry(entry);
	   }
	}
	
	private void mergeElevationEntry(Entry<Integer, Set<Pair>> entry){
	   
	   Set<Pair> value = getElevationMap().get(entry.getKey());
	   
	   if (value != null){
	      
	      getElevationMap().get(entry.getKey()).addAll(entry.getValue());
	   } else {
	      
	      getElevationMap().put(entry.getKey(), entry.getValue());
	   }
	}
	
	// must happen after all members are merged
	private void mergeShore(Set<Pair> otherShore){
	   
	   Set<Pair> otherIntersection = new HashSet<Pair>(otherShore); // use the copy constructor
	   otherIntersection.retainAll(getAllMembers());
	   
	   Set<Pair> intersection = new HashSet<Pair>(shore); // use the copy constructor
	   intersection.retainAll(getAllMembers());
      
	   shore.addAll(otherShore);
	   shore.removeAll(otherIntersection);
	   shore.removeAll(intersection);
	}
	
	// Returns all disconnected pairs
	public List<Pair> handleWaterLineChanges(){
	   
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

		if (map.getHexes().get(startingMember).getStandingWater(0) < Environment.WATER_BODY_MIN) {

			return;
		}

		waterService.addToMapWaterBodies(startingMember, this);

		propogator.propogate(new FloodCommand(), startingMember);
	}
	
	private class RetreatCommand implements Evaluator{
	   
	   HexMap map = HexMap.getInstance();
      
      public boolean evaluate(Pair pairToEvaluate) {
         
         return map.getHexes().get(pairToEvaluate).getStandingWater(map.getStaleHexBodyStandingWater(pairToEvaluate)) <= 0;
      }

      public void onSuccess(Pair pairToEvaluate) {

         removeMember(pairToEvaluate);         
      }

      public void onFail(Pair pairToEvaluate) {

         // Do nothing
      }
	}
	
	private BodyOfWater getThisBody(){
	   return this;
	}
	
	private class FloodCommand implements Evaluator{

	   HexMap map = HexMap.getInstance();

      public boolean evaluate(Pair pairToEvaluate) {
         
         BodyOfWater anotherBody = waterService.inBody(pairToEvaluate);
         boolean ranIntoAnotherBody = anotherBody != null;
         
         if (ranIntoAnotherBody){

            List<BodyOfWater> joinList = new ArrayList<BodyOfWater>();
            joinList.add(getThisBody());
            joinList.add(anotherBody);
            
            map.getBodiesThatNeedToBeJoined().add(joinList);
         }

         return !ranIntoAnotherBody && !getAllMembers().contains(pairToEvaluate) && map.getHexes().get(pairToEvaluate).getStandingWater(0) > Environment.WATER_BODY_MIN;
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

		Integer lowestPoint = getExtremePoint(Environment.LOWEST);

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

	public Integer getExtremePoint(boolean highest) {

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

			setWater(getWater() + map.getHex(iterator.next()).getStandingWater(0));
		}
	}

	private void addMember(Pair member) {

	   removeFromShore(member);
		getAllMembers().add(member);
		waterService.addToMapWaterBodies(member, this);

		HexMap map = HexMap.getInstance();
		Hex hex = map.getHex(member);

		adjustWater(map.alterMoisture(hex, -hex.getStandingWater(0)));

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
		waterService.removeNode(member);
		Hex hex = map.getHex(member);

		int leftover = adjustWater(-map.alterMoisture(hex, hex.getStandingWater(0)));
		
		// Just in case... but this should never happen
		if (leftover > 0) hex.alterMoistureInAir(leftover);

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

	public int adjustWater(int change) {
	   
	   if (change + water < 0){
	      
	      int changed = water;
	      water = 0;
	      return changed;
	   }
	   
		this.water += change;
		return Math.abs(change);
	}

	private int getWaterLine() {
		return (getWater() / Environment.WATER_PER_ELEVATION + getFloorElevation()) / getAllMembers().size();
	}
	
	public void recomputeWaterline(){
	   waterLineLastChecked = getWater();
	}
	
	public int getSlightlyStaleWaterLine(){
	   return waterLineLastChecked;
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
			
			if (getElevationMap().get(elevation).isEmpty()){
			   getElevationMap().remove(elevation);
			}
		}
	}

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
   
   public Set<Pair> getShore(){
      
      return shore;
   }

   public List<Pair> getHexesToCheckForElevation() {
      return hexesToCheckForElevation;
   }

   public void setHexesToCheckForElevation(List<Pair> hexesToCheckForElevation) {
      this.hexesToCheckForElevation = hexesToCheckForElevation;
   }
   
   public void addToHexesToCheckForElevation(Pair pair) {
      hexesToCheckForElevation.add(pair);
   }
}
