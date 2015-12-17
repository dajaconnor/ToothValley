package models;

import impl.HexService;
import impl.WaterService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

public class BodyOfWater {
   
   @Autowired
   private HexService hexService;
   
   @Autowired
   private WaterService waterService;
	
	private Set<Pair> allMembers;
	private int floorElevation; // sum of elevation of all members
	private int water; // sum of standing water of all members
	private Map<Integer,Set<Pair>> elevationMap;
	
	public BodyOfWater(Set<Pair> newMembers){

	   setAllMembers(newMembers);
	   computeFloorElevation();
      computeWater();
      computeElevationMap();
	}
	
	public BodyOfWater(Pair startingMember){

      computeAndSetMembers(startingMember);
   }
	
	public void computeAndSetMembers(Pair startingMember){
	   
	   HexMap map = HexMap.getInstance();
	   
	   if (map.getHexes().get(startingMember).getStandingWater() == 0){
	      
	      return;
	   }
	   
	   waterService.addNode(startingMember);

	   
	   List<Pair> nextList = new ArrayList<Pair>();
      nextList.add(startingMember);
      addMember(startingMember);
      
      while(nextList.size() > 0){
         
         Set<Pair> neighbors = new HashSet<Pair>();
         
         for (Pair pair : nextList){
            
            neighbors.addAll(hexService.getNeighborsSet(pair));
         }
         
         nextList = new ArrayList<Pair>();
         
         for (Pair pair : neighbors){
            
            if (!getAllMembers().contains(pair) && map.getHexes().get(pair).getStandingWater() > 0){
               
               addMember(pair);
               nextList.add(pair);
            }
         }
      }
	}
	
	public void handleSplits(){

	   Pair startingPair = getAllMembers().iterator().next();
	   List<Pair> nextList = new ArrayList<Pair>();
	   nextList.add(startingPair);
	   Set<Pair> connectedPairs = new HashSet<Pair>();
	   connectedPairs.add(startingPair);
	   
	   do{
	      
	      Set<Pair> neighbors = new HashSet<Pair>();
         
	      for (Pair pair : nextList){
	         
	         neighbors.addAll(hexService.getNeighborsSet(pair));
	      }
         
	      nextList = new ArrayList<Pair>();
         
         for (Pair pair : neighbors){
            
            if (getAllMembers().contains(pair) && !connectedPairs.contains(pair)){
               
               connectedPairs.add(pair);
               nextList.add(pair);
            }
         }
      } while(nextList.size() > 0);
	   
	   if(connectedPairs.size() != getAllMembers().size()){
	      
	      Set<Pair> disconnectedPairs = new HashSet<Pair>();
	      
	      for (Pair pair : getAllMembers()){
	         
	         if (!connectedPairs.contains(pair)){
	            
	            disconnectedPairs.add(pair);
	         }
	      }
	      
	      handleDisconnectedPairs(disconnectedPairs);
	   }
	}
	
	private void handleDisconnectedPairs(Set<Pair> disconnectedPairs) {
      // TODO Auto-generated method stub
      
   }

   public void updateHex(Hex hex, int newElevation){

	   if (getAllMembers().contains(hex.getHexID())){

	      if (hex.getElevation() != newElevation){

	         setFloorElevation(getFloorElevation() + newElevation - hex.getElevation());
	         removeFromElevationMap(hex.getHexID(), hex.getElevation());
	         addToElevationMap(hex.getHexID(), newElevation);
	      }
	   }
	}
	
   private void computeElevationMap() {
      
      Iterator<Pair> iterator = getAllMembers().iterator();
      HexMap map = HexMap.getInstance();
      setElevationMap(new HashMap<Integer, Set<Pair>>());
      
      while(iterator.hasNext()){

         Pair nextPair = iterator.next();
         int elevation = map.getHex(nextPair).getElevation();
         
         addToElevationMap(nextPair, elevation);
      }
   }
   
   private void removeFromElevationMap(Pair pair, Integer elevation){
      
      if(getElevationMap().containsKey(elevation)){
         
         getElevationMap().get(elevation).remove(pair);
      }
   }
   
   
   private void addToElevationMap(Pair pair, Integer elevation){
      
      if (getElevationMap().get(elevation) == null){

         getElevationMap().put(elevation, new HashSet<Pair>());
      }
      
      getElevationMap().get(elevation).add(pair);
   }
   
   private void computeFloorElevation() {
      
      Iterator<Pair> iterator = getAllMembers().iterator();
      HexMap map = HexMap.getInstance();
      setFloorElevation(0);
      
      while(iterator.hasNext()){

         setFloorElevation(getFloorElevation() + map.getHex(iterator.next()).getElevation());
      }
   }
   private void computeWater() {
      
      Iterator<Pair> iterator = getAllMembers().iterator();
      HexMap map = HexMap.getInstance();
      setWater(0);
      
      while(iterator.hasNext()){

         setWater(getWater() + map.getHex(iterator.next()).getStandingWater());
      }
   }

   public void addMember(Pair member){
      
      getAllMembers().add(member);
      waterService.addNode(member);
      
      HexMap map = HexMap.getInstance();

      adjustWater(map.getHex(member).getStandingWater());
      
      int elevation = map.getHex(member).getElevation();
      adjustFloorElevation(elevation);
      addToElevationMap(member, elevation);
   }
   
   public void removeMember(Pair member){

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
   public void adjustFloorElevation(int change){
      this.floorElevation += change;
   }
   public int getWater() {
      return water;
   }
   public void setWater(int water) {
      this.water = water;
   }
   public void adjustWater(int change){
      this.water += change;
   }
	public int getWaterLine(){
	   return (getWater() / Environment.WATER_PER_ELEVATION + getFloorElevation()) / getAllMembers().size();
	}
	public void addToElevationMap(Pair member, int elevation){
	   if (!getElevationMap().containsKey(elevation)){
	      getElevationMap().put(elevation, new HashSet<Pair>());
	   }
	   getElevationMap().get(elevation).add(member);
	}
	public void removeFromElevationMap(Pair member, int elevation){
	   if (getElevationMap().containsKey(elevation)){
	      getElevationMap().get(elevation).remove(member);
	   }
	}
	// TODO removeElevationFromBody
   public Map<Integer,Set<Pair>> getElevationMap() {
      return elevationMap;
   }
   public void setElevationMap(Map<Integer,Set<Pair>> elevationMap) {
      this.elevationMap = elevationMap;
   }
	
}
