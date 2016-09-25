package impl;

import java.util.ArrayList;
import java.util.List;

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

   public void waterCycle(boolean findLeak) {

      HexMap map = HexMap.getInstance();
      int totalWater = 0;
      boolean leak = false;

      List<Pair> allHexes = new ArrayList<Pair>(map.getHexes().keySet());

      // Attempt to alter wind direction
      TheRandom rand = TheRandom.getInstance();
      switch (rand.get().nextInt(Environment.WIND_CHANGE)) {

      case 0:
         map.setWindDirection(-1);
         break;

      case 1: // Just toggles wind
         map.setWindDirection(0);
         break;

      case 2:
         map.setWindDirection(1);
         break;

      default:

      }

      if (findLeak) {

         totalWater = hexMapService.allWater()[0];
      }

      blow(findLeak);

      if (findLeak && totalWater != hexMapService.allWater()[0] && !leak) {

         System.out.println("blow leak");
         leak = true;
      }

      /*
       * hexService.rainAll();
       * 
       * if (findLeak && totalWater != hexMapService.allWater()[0] && !leak){
       * 
       * System.out.println("rain leak"); leak = true; }
       */

      map.setUpdatingMap(true);

      for (Pair hexID : allHexes) {

         if (hexID != null) {

            Hex hex = map.getHex(hexID);

            hexService.evaporate(hex, findLeak);
            hexService.flood(hex, findLeak);
            hexService.topple(hexID, 1);

            map.updateHexDisplay(hex);

         }
      }

      map.setUpdatingMap(false);

      if (findLeak && totalWater != hexMapService.allWater()[0] && !leak) {

         System.out.println("evaporate or flood leak");
         leak = true;
      }
   }

   /**
    * Grows the whole map
    */
   public void grow() {

      HexMap map = HexMap.getInstance();

      if (map.getGreenHexes().size() > 0) {

         for (int i = 0; i < map.getHexes().size() * Environment.GROW_RATE; i++) {

            Pair hexId = map.getGreenHexes().getRandom();

            if (hexId != null) {

               hexService.grow(hexId);
            }
         }
      }
      if (map.getGreenHexes().size() < map.getHexes().size() / 100) {
         Hex randomHex = hexMapService.pickRandomHex();
         hexService.forceGrow(randomHex);
      }
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

      if (map.getPlates().size() > 0) {

         int i = 0;

         while (i++ < Environment.TECTONIC_ACTIVITY) {

            TectonicPlate plate = hexMapService.pickRandomPlate();

            for (Pair keyPair : plate.getActiveEdges().keySet()) {

               if (plate.getActiveEdges().get(keyPair) == TectonicEdgeDirection.UP) {

                  map.getHex(hexService.getAreaPair(keyPair))
                  .setElevation(map.getHex(keyPair).getElevation() + Environment.TECTONIC_AMPLITUDE);
               }

               if (plate.getActiveEdges().get(keyPair) == TectonicEdgeDirection.DOWN) {

                  map.getHex(hexService.getAreaPair(keyPair))
                  .setElevation(map.getHex(keyPair).getElevation() - Environment.TECTONIC_AMPLITUDE);
               }
            }
         }
      }
   }

   /**
    * If there is any moisture in air of current hex, it all moves to the
    * adjacent hex with the least moisture
    * 
    * @param hex
    * @return the hex blown to, or null if it failed to blow
    */
   public void blow(boolean findLeak) {

      HexMap map = HexMap.getInstance();

      for (Pair cloud : map.getCloudOrder()) {

         if (map.getClouds().containsKey(cloud)) {

            if (rainSingle(map.getHex(cloud), map.getClouds().get(cloud))) {

               map.getClouds().put(cloud, map.getClouds().get(cloud) + 1);
            } else {

               map.getClouds().remove(cloud);
            }
         }

         if (map.getClouds().containsKey(cloud)) {

            for (Direction direction : Direction.VALUES) {

               Pair pair = hexService.getHexIdFromDirection(cloud, direction);
               map.removeCloud(pair);
               int cloudElevation = map.getHex(cloud).getElevation();

               if (map.getClouds().containsKey(cloud) && blowSingleHex(pair, cloud, cloudElevation, findLeak)) {

                  blowCorner(direction, pair, cloudElevation, findLeak);
               }
            }
         }
      }

      map.reorderClouds();
   }

   private void blowCorner(Direction direction, Pair currentPair, int cloudElevation, boolean findLeak) {

      List<Pair> layer = new ArrayList<Pair>();
      HexMap map = HexMap.getInstance();
      layer.add(currentPair);

      while (layer.size() > 0) {

         List<Pair> nextLayer = new ArrayList<Pair>();

         for (int n = 0; n < layer.size(); n++) {

            if (n == 0) {

               Pair pair = hexService.getHexIdFromDirection(layer.get(n), direction);
               map.removeCloud(pair);

               if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)) {

                  nextLayer.add(pair);
               }
            }

            Pair pair = hexService.getHexIdFromDirection(layer.get(n), direction.turnRight());
            map.removeCloud(pair);

            if (blowSingleHex(pair, layer.get(n), cloudElevation, findLeak)) {

               nextLayer.add(pair);
            }
         }

         layer = nextLayer;
      }
   }

   private boolean blowSingleHex(Pair from, Pair to, int cloudElevation, boolean findLeak) {

      HexMap map = HexMap.getInstance();

      int total = 0;
      Hex fromHex = map.getHex(from);
      Hex toHex = map.getHex(to);

      int strangeBehaviorCount = 0;

      while (fromHex == null || toHex == null) {

         try {
            Thread.sleep(50);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         fromHex = map.getHex(from);
         toHex = map.getHex(to);
         strangeBehaviorCount++;
      }

      if (strangeBehaviorCount > 0) {
         System.out.println("Had to retry getting hex " + strangeBehaviorCount + "times");
      }

      if (findLeak) {
         total = fromHex.getMoistureInAir() + toHex.getMoistureInAir();
      }

      int moisturetoMove = Math.abs(cloudElevation - fromHex.getElevation()) + fromHex.getMoistureInAir()
      + Environment.WIND_POWER - toHex.getMoistureInAir();

      if (moisturetoMove < 0) { // negative wind, to becomes from, etc

         fromHex.alterMoistureInAir(toHex.alterMoistureInAir(moisturetoMove));
         return false;
      }

      boolean returnBool = (toHex.alterMoistureInAir(fromHex.alterMoistureInAir(-moisturetoMove)) == moisturetoMove);

      if (findLeak && fromHex.getMoistureInAir() + toHex.getMoistureInAir() != total) {

         System.out.println("blowSingleHex leak");
      }

      return returnBool;
   }

   private boolean rainSingle(Hex hex, int amount) {

      int changed = hex.alterMoistureInAir(-Math.abs(amount));
      hex.alterMoisture(changed);

      return changed == amount;
   }
}
