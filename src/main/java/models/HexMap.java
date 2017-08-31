package models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import enums.DisplayType;
import graphics.OpenGLWindow;

public class HexMap {

	private int mapID;

	private Hashtable<Pair,Hex> hexes = new Hashtable<Pair,Hex>();
	
	private Set<Pair> saturatedHexes = new HashSet<Pair>();
	
	private List<Pair> cloudOrder = new ArrayList<Pair>();
	
	private Map<Pair, Integer> clouds = new HashMap<Pair, Integer>();
	
	private SetList<Pair> greenHexes = new SetList<Pair>();
	
	private Set<Pair> burningHexes = new HashSet<Pair>();
	
	private List<TectonicPlate> plates = new ArrayList<TectonicPlate>();
	
	private int windDirection = 0;
	
	private int blowing = 5;
	
	private Map<Pair,BodyOfWater> pairToWaterBodies = new HashMap<Pair,BodyOfWater>();
	
	private Set<BodyOfWater> allWaterBodies = new HashSet<BodyOfWater>();
	
	private Set<BodyOfWater> waterBodiesToBeRemoved = new HashSet<BodyOfWater>();
	
	private Set<PairOfBodies> bodiesThatNeedToBeJoined = new HashSet<PairOfBodies>();

	private Map<Pair,Pair> displayMap = new HashMap<Pair,Pair>();
	private Map<Pair,Pair> readMap = new HashMap<Pair,Pair>();
	private static ReadWriteLock displayMapLock = new ReentrantReadWriteLock();
	
	private Map<Pair, Integer> bodyDisplayMap = new HashMap<Pair, Integer>();
   private Map<Pair, Integer> bodyReadMap = new HashMap<Pair, Integer>();
   private static ReadWriteLock bodyDisplayMapLock = new ReentrantReadWriteLock();
   
   private List<List<Pair>> bodyConnectivityDisplayMap = new ArrayList<List<Pair>>();
   private List<List<Pair>> bodyConnectivityReadMap = new ArrayList<List<Pair>>();
   private static ReadWriteLock bodyConnectivityDisplayMapLock = new ReentrantReadWriteLock();
	
	//For singletonhood
	private static HexMap instance = new HexMap();
	
	public HexMap() {
		// Exists only to defeat instantiation.
	}
	
	public static HexMap getInstance() {
		
		return instance;
	}
	
	public int getMapID() {
		return mapID;
	}
	public void setMapID(int mapID) {
		this.mapID = mapID;
	}
	public Hashtable<Pair, Hex> getHexes() {
		return hexes;
	}
	public void setHexes(Hashtable<Pair, Hex> hexes) {
		this.hexes = hexes;
	}
	
	public Hex getHex(Pair ID){
		
		Hex returnHex = null;
		
		if (ID != null && hexes.containsKey(ID)){
			returnHex = hexes.get(ID);
		}
		return returnHex;
	}
	
	public void reorderClouds(){
		
		List<Pair> newOrder = new ArrayList<Pair>();
		
		for (Pair cloud : cloudOrder){
			
			if (clouds.containsKey(cloud)){
				
				newOrder.add(cloud);
			}
		}
		
		this.cloudOrder = newOrder;
	}
	
	public void addCloud(Pair newCloud){
		
		cloudOrder.add(newCloud);
		clouds.put(newCloud, 1);
	}
	
	public void removeCloud(Pair oldCloud){

		clouds.remove(oldCloud);
	}
	
	public List<Pair> getCloudOrder(){
		
		return cloudOrder;
	}

	public Map<Pair, Integer> getClouds() {
		return clouds;
	}
	
	public void addHex(Hex hex){
		
		hexes.put(hex.getHexID(), hex);
	}

	public Set<Pair> getSaturatedHexes() {
		return saturatedHexes;
	}

	public void setSaturatedHexes(Set<Pair> saturatedHexes) {
		this.saturatedHexes = saturatedHexes;
	}
	
	public void addSaturatedHex(Pair hexId){
		
		this.saturatedHexes.add(hexId);
	}

	public void removeSaturatedHex(Pair hexID) {
		
		this.saturatedHexes.remove(hexID);	
	}
	
	public SetList<Pair> getGreenHexes() {
		return greenHexes;
	}

	public void setGreenHexes(SetList<Pair> blackHexes) {
		this.greenHexes = blackHexes;
	}
	
	public void addGreenHex(Pair hex){
		
		if (greenHexes.size() > Environment.MAP_GRID[0] * Environment.MAP_GRID[1]){
			
			System.out.println();
		}
		
		greenHexes.add(hex);
		

	}
	
	public void removeGreenHex(Pair hex){
		
		greenHexes.remove(hex);
	}
	
	public boolean inGreenHexes(Pair hex){
		
		return greenHexes.contains(hex);
	}

	public void addBurningHex(Pair hex){
		
		burningHexes.add(hex);
	}
	
	public void removeBurningHex(Pair hex){
		
		burningHexes.remove(hex);
	}
	
	public Set<Pair> getBurningHexes(){
		
		return burningHexes;
	}

	public int getWindDirection() {
		return windDirection;
	}

	/**
	 * alters wind direction (if change is not 0) and always toggles wind
	 * @param change
	 */
	public void setWindDirection(int change) {
		
		if (change > 0){
			
			windDirection ++;
		}
		if (change < 0){
			
			windDirection --;
		}
		
		windDirection = (windDirection + 6*6) % 6;
		
		if (change == 0){
			
			blowing = 0;
		}
		else{
			blowing++;
		}
	}

	public int isBlowing() {
		return blowing;
	}

	public Pair updateHexDisplay(Hex hex, DisplayType displayType){
	   
	   Pair displayPair = null;

      int elevation = hex.getElevation();
      int standingBodyWater = hex.getStandingWater(0);
      
      // Not until we can avoid those stupid water pyramids
      DisplayType display = OpenGLWindow.getInstance().getDisplayType();
      
      if ((display == DisplayType.NORMAL) && standingBodyWater != 0){
         elevation = hex.getCombinedElevation(standingBodyWater);
      }
      
      Color color = hex.getColor(standingBodyWater, displayType);

      displayPair = new Pair(colorToInt(color), elevation);
	   

		return displayPair;
	}
	
	public int getHexBodyStandingWater(Pair id){
	   Hex hex = getHexes().get(id);
	   return getHexBodyStandingWater(hex);
	}
	
	public int getHexBodyStandingWater(Hex hex){

	   BodyOfWater body = getPairToWaterBodies().get(hex.getHexID());
	   
	   if (body == null) return hex.getStandingWater(0);

	   int bodyWaterline = body.getWaterLine();
	   
	   int standingWaterElevation = bodyWaterline - hex.getElevation();
	   
	   if (standingWaterElevation <= 0) return 0;
	   
	   return standingWaterElevation * Environment.WATER_PER_ELEVATION;
   }
	
	public static int colorToInt(Color color){
		
		int returnInt = 0;
		
		returnInt += color.getRed() * 1000000;
		returnInt += color.getGreen() * 1000;
		returnInt += color.getBlue();
		
		return returnInt;
	}
	
	public static Color intToColor(int toConvert){
		
		int blue = toConvert % 1000;
		int green = (toConvert/1000) % 1000;
		int red = toConvert/1000000;
		
		return new Color(red,green,blue);
	}
	
	public static int intToRed(int toConvert){
		
		return toConvert/1000000;
	}
	
	public static int intToGreen(int toConvert){
		
		return (toConvert/1000) % 1000;
	}
	
	public static int intToBlue(int toConvert){
		
		return toConvert % 1000;
	}

	public List<TectonicPlate> getPlates() {
		return plates;
	}

	public void setPlates(List<TectonicPlate> plates) {
		this.plates = plates;
	}

   public Map<Pair,BodyOfWater> getPairToWaterBodies() {
      return pairToWaterBodies;
   }

   public int alterMoisture(Hex hex, int change) {

      BodyOfWater body = getPairToWaterBodies().get(hex.getHexID());
      if (body == null) return hex.alterMoisture(change, false);
      return body.adjustTotalWater(change);
   }
   
   public int alterMoisture(Pair hexId, int change) {

      return alterMoisture(getHex(hexId), change);
   }

   public Set<PairOfBodies> getBodiesThatNeedToBeJoined() {
      return bodiesThatNeedToBeJoined;
   }

   public void resetBodiesThatNeedToBeJoined() {
      this.bodiesThatNeedToBeJoined = new HashSet<PairOfBodies>();
   }

   public Set<BodyOfWater> getAllWaterBodies() {
      return allWaterBodies;
   }
   
   public void setDisplayMap(Map<Pair, Pair> newDisplayMap){
      displayMapLock.writeLock().lock();

      try{ 
         displayMap = newDisplayMap; // map3.putAll(map1);
      } finally{
         displayMapLock.writeLock().unlock();
      }
   }
   
   public Map<Pair,Pair> getDisplayMap() {
      
      displayMapLock.readLock().lock();

      try{
         readMap = displayMap;
      } finally{
         displayMapLock.readLock().unlock();
      }
      
      return readMap;
   }
   
   public void setBodyConnectivityDisplayMap(List<Map<Pair, Set<Pair>>> updatedBodyConnectivityDisplayMap){
      bodyConnectivityDisplayMapLock.writeLock().lock();

      try{ 
         
      // Build the map from the first hex (nest[0]) to all the others
         bodyConnectivityDisplayMap = new ArrayList<List<Pair>> ();
         
         for (Map<Pair, Set<Pair>> map : updatedBodyConnectivityDisplayMap){
            
            for (Entry<Pair, Set<Pair>> entry : map.entrySet()){
               
               List<Pair> nest = new ArrayList<Pair>();
               nest.add(entry.getKey());
               nest.addAll(entry.getValue());

               bodyConnectivityDisplayMap.add(nest);
            }
         }
      } finally{
         bodyConnectivityDisplayMapLock.writeLock().unlock();
      }
   }
   
   public List<List<Pair>> getBodyConnectivityDisplayMap() {
      
      bodyConnectivityDisplayMapLock.readLock().lock();

      try{
         
         bodyConnectivityReadMap = bodyConnectivityDisplayMap;
         
      } finally{
         bodyConnectivityDisplayMapLock.readLock().unlock();
      }

      return bodyConnectivityReadMap;
   }
   
   public void setBodyDisplayMap(Map<Pair, Integer> updatedBodyDisplayMap){
      bodyDisplayMapLock.writeLock().lock();

      try{ 
         bodyDisplayMap = updatedBodyDisplayMap;
      } finally{
         bodyDisplayMapLock.writeLock().unlock();
      }
   }
   
   public Map<Pair, Integer> getBodyDisplayMap() {
      
      bodyDisplayMapLock.readLock().lock();

      try{
         bodyReadMap = bodyDisplayMap;
      } finally{
         bodyDisplayMapLock.readLock().unlock();
      }

      return bodyReadMap;
   }
   
   public DisplayType getDisplayType(){
      
      return OpenGLWindow.getInstance().getDisplayType();
   }

   public Set<BodyOfWater> getWaterBodiesToBeRemoved() {
      return waterBodiesToBeRemoved;
   }
   
   public void resetWaterBodiesToBeRemoved() {
      waterBodiesToBeRemoved = new HashSet<BodyOfWater>();
   }
}
