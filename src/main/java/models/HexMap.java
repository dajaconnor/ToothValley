package models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import enums.Direction;
import enums.DisplayType;
import graphics.OpenGLWindow;

public class HexMap {

	private int mapID;

	private Hashtable<Pair,Hex> hexes = new Hashtable<Pair,Hex>();
	
	private int ticks = 0;

	private Set<Pair> saturatedHexes = new HashSet<Pair>();

	private SetList<Pair> greenHexes = new SetList<Pair>();

	private Set<Pair> burningHexes = new HashSet<Pair>();

	private List<TectonicPlate> plates = new ArrayList<TectonicPlate>();

	// for new cloud system to know what's already moved
	private Set<Pair> windBlown = new HashSet<Pair>();
	private Set<Pair> appliedBlown = new HashSet<Pair>();
	
	// got to be a better way to handle this guy's scope...
	private int tallestTargetForCloud = -1000;
	private Direction directionToTarget = null;
	private List<Pair> hexesInCloud = null;

	private int waterChangedByUser = 0;
	private static ReadWriteLock waterChangedByUserLock = new ReentrantReadWriteLock();
	
	private Map<Pair,Pair> displayMap = new HashMap<Pair,Pair>();
	private Map<Pair,Pair> readMap = new HashMap<Pair,Pair>();

	private boolean closeProgram = false;
	private static ReadWriteLock closeProgramLock = new ReentrantReadWriteLock();
	
	private static ReadWriteLock displayMapLock = new ReentrantReadWriteLock();

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

	public Pair getHighestNeighbor(Pair pair){

		List<Pair> neighbors = pair.getNeighbors();

		int elev = -100000;
		Pair highest = pair;

		for (Pair neighbor : neighbors){

			int thisElev = getHex(neighbor).getElevation();

			if (thisElev > elev){

				elev = thisElev;
				highest = neighbor;
			}
		}

		return highest;
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

	public Pair updateHexDisplay(Hex hex, DisplayType displayType){

		Pair displayPair = null;

		int elevation = hex.getElevation();
		int standingBodyWater = hex.getStandingWater();

		// Not until we can avoid those stupid water pyramids
		DisplayType display = OpenGLWindow.getInstance().getDisplayType();

		if (display == DisplayType.NORMAL && standingBodyWater != 0){
			elevation = hex.getDisplayElevation(getSnowLevel());
		}

		Color color = hex.getColor(displayType, getSnowLevel());

		displayPair = new Pair(colorToInt(color), elevation);


		return displayPair;
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

	public int alterMoisture(Hex hex, int change) {

		return hex.alterMoisture(change);
	}

	public int alterMoisture(Pair hexId, int change) {

		return alterMoisture(getHex(hexId), change);
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
	
	public void resetDisplayToBlack() {
		
		Map<Pair,Pair> display = getDisplayMap();
		
		for (Pair hexId : display.keySet()) {
			
			display.get(hexId).setX(0);
		}
		
		setDisplayMap(display);
	}

	public DisplayType getDisplayType(){

		return OpenGLWindow.getInstance().getDisplayType();
	}

	public Set<Pair> getWindBlown() {
		return windBlown;
	}

	public void resetWindBlown() {
		this.windBlown = new HashSet<Pair>();
	}

	public Set<Pair> getAppliedBlown() {
		return appliedBlown;
	}

	public void resetAppliedBlown() {
		this.appliedBlown = new HashSet<Pair>();
	}
	
	public Direction getDirectionToTarget() {
		return directionToTarget;
	}

	public void setDirectionToTarget(Direction directionToTarget) {
		this.directionToTarget = directionToTarget;
	}

	public int getTallestTargetForCloud() {
		return tallestTargetForCloud;
	}

	public void setTallestTargetForCloud(int tallestTargetForCloud) {
		this.tallestTargetForCloud = tallestTargetForCloud;
	}

	public List<Pair> getHexesInCloud() {
		return hexesInCloud == null ? new ArrayList<Pair>() : hexesInCloud;
	}

	public void setHexesInCloud(List<Pair> hexesInCloud) {
		this.hexesInCloud = hexesInCloud;
	}

	public void resetHexesInCloud() {
		hexesInCloud = new ArrayList<Pair>();
	}

	public int getTicks() {
		return ticks;
	}

	public void tick() {
		ticks++;
	}
	
	// returns null if no effect
	public Direction getCoriolis(int longitude){
		
		Direction returnDir = null;
		
		double chance = Environment.CORIOLIS_RELIANCE * Math.sin(((double)longitude * Math.PI) / (Environment.MAP_GRID[0]/2));
		
		if (TheRandom.getInstance().get().nextDouble() <= Math.abs(chance)){
			
			if (chance < 0){
				returnDir = Direction.north;
			} else{
				returnDir = Direction.south;
			}
		}
		
		return returnDir;
	}
	
	public int getSnowLevel(){
		
		return Environment.AVE_ELEVATION * 2 + 
				(int) (Environment.SNOW_LEVEL_AMPLITUDE * 
						Math.sin(((double)ticks) / Environment.YEAR_IN_TICKS));
	}

	public int getWaterChangedByUser() {
		
		int change = 0;
		waterChangedByUserLock.writeLock().lock();

		try{ 
			change = waterChangedByUser;
			waterChangedByUser = 0;
		} finally{
			waterChangedByUserLock.writeLock().unlock();
		}

		return change;
	}

	public void incrementWaterChangedByUser(int change) {
		
		waterChangedByUserLock.writeLock().lock();

		try{ 

			this.waterChangedByUser += change;
		} finally{
			waterChangedByUserLock.writeLock().unlock();
		}
	}

	public boolean isCloseProgram() {
		return closeProgram;
	}

	public void setCloseProgram() {
		
		closeProgramLock.writeLock().lock();

		try{ 

			closeProgram = true;
		} finally{
			closeProgramLock.writeLock().unlock();
		}
	}
}
