package impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.TectonicPlate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.TectonicEdgeDirection;

/**
 * Evaporation > Wind > rain > flood > grow > burn
 * @author dconnor
 *
 */
@Component
public class EnvironmentService {

	@Autowired
	private HexService hexService;
	
	@Autowired
	private HexMapService hexMapService;
	
	public void waterCycle(boolean findLeak){
		
		HexMap map = HexMap.getInstance();
		int totalWater = 0;
		boolean leak = false;
		
		List<Pair> allHexes = new ArrayList<Pair>( map.getHexes().keySet() );
		
		//Attempt to alter wind direction
		Random rand = new Random();
		switch(rand.nextInt(Environment.WIND_CHANGE)){
		
		case 0:
			map.setWindDirection(-1);
			break;
		
		case 1: //Just toggles wind
			map.setWindDirection(0);
			break;
			
		case 2:
			map.setWindDirection(1);
			break;
			
		default:
		
		}
		
		if (findLeak){
			
			totalWater = hexMapService.allWater()[0];
		}
		
		hexService.blow(findLeak);
		
		if (findLeak && totalWater != hexMapService.allWater()[0] && !leak){
			
			System.out.println("blow leak");
			leak = true;
		}
		
/*		hexService.rainAll();
		
		if (findLeak && totalWater != hexMapService.allWater()[0] && !leak){
			
			System.out.println("rain leak");
			leak = true;
		}*/
		
		map.setUpdatingMap(true);
		
		for (Pair hexID : allHexes){
			
			if (hexID != null){
			
				Hex hex = map.getHex(hexID);
				
				hexService.evaporate(hex, findLeak);
				hexService.flood(hex, findLeak);
				
				map.updateHexDisplay(hex);

			}
		}
		
		map.setUpdatingMap(false);
		
		if (findLeak && totalWater != hexMapService.allWater()[0] && !leak){
			
			System.out.println("evaporate or flood leak");
			leak = true;
		}
	}
	


	/**
	 * Grows the whole map
	 */
	public void grow(){
		
		HexMap map = HexMap.getInstance();
		
		if (map.getGreenHexes().size() > 0){
		
			for (int i = 0; i < map.getHexes().size() * Environment.GROW_RATE; i++){
				
				Pair hexId = map.getGreenHexes().getRandom();
				
				if (hexId != null){
				
					hexService.grow(hexId);
				}
				
				else{
					
					break;
				}
			}
		}
		if (map.getGreenHexes().size() < map.getHexes().size()/3){
			Hex randomHex = hexMapService.pickRandomHex();
			hexService.forceGrow(randomHex);
		}
	}
	
	/**
	 * Burns the whole map
	 */
	public void burn(){
		
		HexMap map = HexMap.getInstance();
		List<Pair> burning = new ArrayList<Pair>(map.getBurningHexes());
		
		//Spread from and destroy burning hexes
		for (Pair hex : burning){
			
			hexService.igniteNext(map.getHex(hex));
			hexService.burnDown(map.getHex(hex));
		}
		
		//Find new hexes to burn
		Random generator = new Random();
		
		int burnInt = (int) (generator.nextDouble() * Environment.BURN_RATE * map.getHexes().size());
		
		while (burnInt > 1){
			
			Hex randomHex = hexMapService.pickRandomHex();
			hexService.ignite(randomHex.getHexID(), Environment.LIGHTNING_STRENGTH);
			
			burnInt --;
		}
	}
	

	public void volcano(){
		
		int lowestElevation = 500;
		int numberOfHexes = 0;
		
		HexMap map = HexMap.getInstance();
		
		List<Pair> allHexes = new ArrayList<Pair>( map.getHexes().keySet() );
		
		for (Pair hexID : allHexes){
			
			numberOfHexes++;
			
			if (hexID != null && map.getHex(hexID).getElevation() < lowestElevation){
				
				lowestElevation = map.getHex(hexID).getElevation();
			}
		}
		
		Random rand = new Random();
		int randomHex = rand.nextInt(numberOfHexes);
		int count = 0;
		
		for (Pair hexID : allHexes){
			
			count++;
			
			if (count == randomHex){
				
				int elev = map.getHex(hexID).getElevation();
				elev += lowestElevation * (numberOfHexes - 1);
				
				map.getHex(hexID).setElevation(elev);
				
				hexService.topple(hexID, 0);
			}
			
			else{
				
				map.getHex(hexID).setElevation(map.getHex(hexID).getElevation() - lowestElevation);
			}
		}
	}
	
	public void shiftTectonics(){

	   HexMap map = HexMap.getInstance();
	   
	   if (map.getPlates().size() > 0){
	   
   		TectonicPlate plate = hexMapService.pickRandomPlate();
   		
   
   	   for (Pair keyPair : plate.getActiveEdges().keySet()){
   	    	
   	    	if (plate.getActiveEdges().get(keyPair) == TectonicEdgeDirection.UP){
   	    		
   	    		map.getHex(hexService.getAreaPair(keyPair)).setElevation(map.getHex(keyPair).getElevation() + 1);
   	    	}
   	    	
   	    	if (plate.getActiveEdges().get(keyPair) == TectonicEdgeDirection.DOWN){
   	    		
   	    		map.getHex(hexService.getAreaPair(keyPair)).setElevation(map.getHex(keyPair).getElevation() - 1);
   	    	}
   	   }
	   }
	}
}
