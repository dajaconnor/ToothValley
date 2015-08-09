package models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HexMap {

	private int mapID;
	private int[] size;

	private Hashtable<Pair,Hex> hexes = new Hashtable<Pair,Hex>();
	
	private Set<Pair> saturatedHexes = new HashSet<Pair>();
	
	private List<Pair> cloudOrder = new ArrayList<Pair>();
	
	private Map<Pair, Integer> clouds = new HashMap<Pair, Integer>();
	
	private SetList<Pair> greenHexes = new SetList<Pair>();
	
	private Set<Pair> burningHexes = new HashSet<Pair>();
	
	private List<TectonicPlate> plates = new ArrayList<TectonicPlate>();
	
	private int windDirection = 0;
	
	private int blowing = 5;
	
	private boolean updatingMap = false;
	
	private Map<Pair,Pair> displayMap = new HashMap<Pair,Pair>();
	
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
	public int[] getSize() {
		return size;
	}
	public void setSize(int[] size) {
		this.size = size;
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
		
		if (greenHexes.size() > size[0] * size[1]){
			
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

	public Map<Pair,Pair> getDisplayMap() {
		return displayMap;
	}

	public void setDisplayMap(HashMap<Pair,Pair> displayMap) {
		this.displayMap = displayMap;
	}
	
	public void updateHexDisplay(Hex hex){
		
		int elevation = hex.getElevation();
		Color color = hex.getColor();
		
		Pair displayPair = new Pair(colorToInt(color), elevation);
		
		displayMap.put(hex.getHexID(), displayPair);
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

   public boolean isUpdatingMap() {
      return updatingMap;
   }

   public void setUpdatingMap(boolean updatingMap) {
      this.updatingMap = updatingMap;
   }

}
