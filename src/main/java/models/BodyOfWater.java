package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import propogation.DirectionalEvaluator;
import propogation.Propogator;

public class BodyOfWater {

	private Set<Pair> allMembers = new HashSet<Pair>();
	private int floorElevation; // sum of elevation of all members
	private int water; // sum of standing water of all members
	private Map<Integer, Set<Pair>> elevationMap = new HashMap<Integer, Set<Pair>>();
	//private Set<Pair> shore = new HashSet<Pair>();
	private Map<Pair, Pair> connectivityMap = new HashMap<Pair, Pair>();
	private Map<Pair, Set<Pair>> connectivityChildMap = new HashMap<Pair, Set<Pair>>();
	private int waterLineLastChecked;

   private Pair originalMember;
	private List<Pair> hexesToCheckForElevation = new ArrayList<Pair>();
	private List<Pair> markedForRemoval = new ArrayList<Pair>();

	public BodyOfWater(Pair startingMember) {

	   originalMember = startingMember;
	}
	
	@Override
   public int hashCode() {

      return originalMember.hashCode();
   }
	
	@Override
   public boolean equals(Object obj) {
      
      if (obj instanceof BodyOfWater){
         
         return originalMember.equals(((BodyOfWater) obj).getOriginalMember());
      }
      
      return false;
   }
	
	public void markForRemoval(Pair pair){
	   markedForRemoval.add(pair);
	}
	
	public void markForRemoval(Collection<Pair> pairs){
      markedForRemoval.addAll(pairs);
   }
	
	public List<Pair> getMarkedForRemoval(){
	   
	   return markedForRemoval;
	}
	
	public void clearMarkedForRemoval(){
	   
	   markedForRemoval = new ArrayList<Pair>();
	}
	
	private Object getOriginalMember() {

      return originalMember;
   }

   public void mergeInOtherBody(BodyOfWater otherBody){

	   // find all shared pairs
      Set<Pair> intersection = new HashSet<Pair>(otherBody.getAllMembers()); // use the copy constructor
      intersection.retainAll(getAllMembers());
      
      adjustFloorElevation(otherBody.getFloorElevation());
      adjustTotalWater(otherBody.getTotalWater());
      
      subtractDoubleCountedPairs(intersection, otherBody);
      
      mergeElevationMap(otherBody.getElevationMap());
      
      getAllMembers().addAll(otherBody.getAllMembers());
      
      //mergeShore(otherBody.getShore()); // must happen after all member merge
      buildConnectivityMap();
	}
	
	private void subtractDoubleCountedPairs(Set<Pair> doubleCounted, BodyOfWater otherBody){
	   
	   HexMap map = HexMap.getInstance();
	   
	   for (Pair pair : doubleCounted){
         
         adjustFloorElevation(-map.getHex(pair).getElevation());
         adjustTotalWater(-map.getHexBodyStandingWater(pair));
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
/*	private void mergeShore(Set<Pair> otherShore){
	   
	   Set<Pair> otherIntersection = new HashSet<Pair>(otherShore); // use the copy constructor
	   otherIntersection.retainAll(getAllMembers());
	   
	   Set<Pair> intersection = new HashSet<Pair>(shore); // use the copy constructor
	   intersection.retainAll(getAllMembers());
      
	   shore.addAll(otherShore);
	   shore.removeAll(otherIntersection);
	   shore.removeAll(intersection);
	}*/
	

	
	public boolean tryConnectOrphan(Pair orphan){
	   
	   List<Pair> neighbors = orphan.getNeighbors();
	   
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

	
	
/*	private class RetreatCommand implements Evaluator{
	   
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
	}*/
	


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
		
		new Propogator().propogateFromOrigin(new ConnectivityMapCommandBuilder(), startingPoint);
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

			if (!getElevationMap().get(key).isEmpty()
			   && (extremestPoint == null || ((!highest && key < extremestPoint) || (highest && key > extremestPoint)))) {

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
	      traversedPairs.add(pair);
	   }

	   return false;
	}

/*	private void computeElevationMap() {

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
	}*/

	public Set<Pair> getAllMembers() {
		return allMembers;
	}

	public void setAllMembers(Set<Pair> allMembers) {
		this.allMembers = allMembers;
	}

	public int getFloorElevation() {
		return floorElevation;
	}

	public void adjustFloorElevation(int change) {
		this.floorElevation += change;
	}

	public int getTotalWater() {
		return water;
	}

	public void setTotalWater(int water) {
		this.water = water;
	}

	public int adjustTotalWater(int change) {
	   
	   if (change + water < 0){
	      
	      int changed = water;
	      water = 0;
	      return changed;
	   }
	   
		this.water += change;
		return Math.abs(change);
	}

	public int getWaterLine() {
	   
	   if (getAllMembers() == null || getAllMembers().isEmpty()) return 0;
		return (getTotalWater() / Environment.WATER_PER_ELEVATION + getFloorElevation()) / getAllMembers().size();
	}
	
	public void addToElevationMap(Pair member, int elevation) {
		if (!getElevationMap().containsKey(elevation)) {
			getElevationMap().put(elevation, new HashSet<Pair>());
		}
		getElevationMap().get(elevation).add(member);
	}

	public void removeFromElevationMap(Pair member, int elevation) {
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
   
   public Map<Pair, Set<Pair>> getConnectivityChildMap(){
      return connectivityChildMap;
   }
   
   private void addToConnectivityMap(Pair child, Pair parent) {
      connectivityMap.put(child, parent);
      addToConnectivityChildMap(child,parent);
   }
   
   // returns orphaned children
   public Set<Pair> removeFromConnectivityMap(Pair pair) {
      
      connectivityMap.remove(pair);
      Set<Pair> orphans = connectivityChildMap.get(pair);
      connectivityChildMap.remove(pair);
      
      return orphans;
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
   
/*   public void addToShore(Pair pair){
      
      if (shore == null){
         shore = new HashSet<Pair>();
      }
      
      shore.add(pair);
   }
   
   public void removeFromShore(Pair pair){
      
      if (shore == null){
         shore = new HashSet<Pair>();
      }
      
      shore.remove(pair);
   }
   
   public Set<Pair> getShore(){
      
      return shore;
   }*/

   public List<Pair> getHexesToCheckForElevation() {
      return hexesToCheckForElevation;
   }

   public void setHexesToCheckForElevation(List<Pair> hexesToCheckForElevation) {
      this.hexesToCheckForElevation = hexesToCheckForElevation;
   }
   
   public void addToHexesToCheckForElevation(Pair pair) {
      hexesToCheckForElevation.add(pair);
   }
   
   public void setWaterLineLastChecked(int waterLineLastChecked){
      this.waterLineLastChecked = waterLineLastChecked;
   }
   
   public void setWaterLineLastChecked(){
      this.waterLineLastChecked = getWaterLine();
   }
   
   public int getWaterLineLastChecked() {
      return waterLineLastChecked;
   }

   public Set<Pair> getShallowHexes(int iterations) {

      int lowPoint = getExtremePoint(false);
      Set<Pair> returnSet = getElevationMap().get(lowPoint);
      
      while(--iterations > 0){
         
         Set<Pair> hexes = getElevationMap().get(--lowPoint);
         
         if (hexes != null) returnSet.addAll(hexes);
         
         
      }

      return returnSet;
   }

}
