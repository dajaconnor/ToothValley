package impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import enums.Direction;
import enums.TectonicEdgeDirection;
import models.Environment;
import models.Hex;
import models.HexMap;
import models.Pair;
import models.TectonicPlate;
import models.TheRandom;

/**
 * Evaporation > Wind > rain > flood > grow > burn
 * 
 * @author dconnor
 *
 */
@Component
public class EnvironmentService {

   @Autowired
   private HexService hexService;

   @Autowired
   private HexMapService hexMapService;

   /**
    * Grows the whole map
    */
   public void grow() {

      HexMap map = HexMap.getInstance();
      boolean quickGrow = quickGrowMode();
      
      if (map.getTicks() % Environment.FORCE_GROW_INTERVAL == 0 && map.getHexes().size() > 0){

         Hex randomHex = hexMapService.pickRandomHex();
         hexService.forceGrow(randomHex);
      }
      
      if (quickGrow){
         
         for (int i = 0; i < 10; i++){
            
            Hex randomHex = hexMapService.pickRandomHex();
            hexService.forceGrow(randomHex);
         }
      }
   }
   
   private boolean quickGrowMode(){
      
      HexMap map = HexMap.getInstance();
      
      return map.getTicks() < Environment.QUICK_GROW_LENGTH;
   }
   
   /**
    * Burns the whole map
    */
   public void burn() {

      HexMap map = HexMap.getInstance();
      List<Pair> burning = new ArrayList<Pair>(map.getBurningHexes());

      // Spread from and destroy burning hexes
      for (Pair hex : burning) {

         hexService.igniteNext(map.getHex(hex));
         hexService.burnDown(map.getHex(hex));
      }

      // Find new hexes to burn
      TheRandom generator = TheRandom.getInstance();

      int burnInt = (int) (generator.get().nextDouble() * Environment.BURN_RATE * map.getHexes().size());

      while (burnInt > 1) {

         Hex randomHex = hexMapService.pickRandomHex();
         hexService.ignite(randomHex.getHexID(), Environment.LIGHTNING_STRENGTH);

         burnInt--;
      }
   }

   public void volcano() {

      int lowestElevation = 500;
      int numberOfHexes = 0;

      HexMap map = HexMap.getInstance();

      List<Pair> allHexes = new ArrayList<Pair>(map.getHexes().keySet());

      for (Pair hexID : allHexes) {

         numberOfHexes++;

         if (hexID != null && map.getHex(hexID).getElevation() < lowestElevation) {

            lowestElevation = map.getHex(hexID).getElevation();
         }
      }

      TheRandom rand = TheRandom.getInstance();
      int randomHex = rand.get().nextInt(numberOfHexes);
      int count = 0;

      for (Pair hexID : allHexes) {

         count++;

         if (count == randomHex) {

            int elev = map.getHex(hexID).getElevation();
            elev += lowestElevation * (numberOfHexes - 1);

            map.getHex(hexID).setElevation(elev);

            hexService.topple(hexID, 0);
         }

         else {

            map.getHex(hexID).setElevation(map.getHex(hexID).getElevation() - lowestElevation);
         }
      }
   }

   public void shiftTectonics() {

      HexMap map = HexMap.getInstance();
      Random rand = TheRandom.getInstance().get();

      if (map.getPlates().size() > 0) {

         int i = 0;
         int leftoverElev = 0;

         while (i++ < Environment.TECTONIC_ACTIVITY) {

            TectonicPlate plate = hexMapService.pickRandomPlate();
            
            if (rand.nextInt(Environment.CHANCE_OF_TECTONIC_PLATE_CHANGE) == 1){
               
               Direction newDirection = plate.getDirection().takeRandomTurn();
               plate.setDirection(newDirection);
               plate.setVerticalDirection(plate.getVerticalDirection() 
            		   + TheRandom.getInstance().get().nextInt(3) - 1);
            }
            
            if (rand.nextInt(Environment.AVE_TICKS_BETWEEN_TECTONIC_VERTICAL_MOVE) == 1) plate.handleVerticalChange();

            for (Pair keyPair : plate.getActiveEdges().keySet()) {

               if (plate.getActiveEdges().get(keyPair) == TectonicEdgeDirection.UP) {

                  map.getHex(hexService.getAreaPair(keyPair))
                  	.setElevation(map.getHex(keyPair).getElevation() + Environment.TECTONIC_AMPLITUDE);
                  
                  hexService.topple(keyPair, 0);
                  
                  leftoverElev -= Environment.TECTONIC_AMPLITUDE;
               }

               if (plate.getActiveEdges().get(keyPair) == TectonicEdgeDirection.DOWN) {

                  map.getHex(hexService.getAreaPair(keyPair))
                  .setElevation(map.getHex(keyPair).getElevation() - Environment.TECTONIC_AMPLITUDE);
                  
                  leftoverElev += Environment.TECTONIC_AMPLITUDE;
               }
            }
            
            if (leftoverElev != 0){
            	
            	for (Pair pair : plate.getAllEdges()){
            		
            		map.getHex(hexService.getAreaPair(pair))
                    .setElevation(map.getHex(pair).getElevation() + leftoverElev);
            		
            		leftoverElev = 0;
            		
            		break;
            	}
            }
         }
      }
   }
}
