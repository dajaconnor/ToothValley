package controllers;


import graphics.DisplayThread;
import impl.EnvironmentService;
import impl.HexMapService;
import impl.WaterService;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Main {
	
	@Autowired
	EnvironmentService environmentService;
	
	@Autowired
	HexMapService hexMapService;
	
	@Autowired
   WaterService waterService;
	
    public static void main(String[] args) {
    	
    	ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring.xml");
    	
    	Main main = context.getBean(Main.class);
    	main.run();
    }

	/**
	 * Evaporation > Wind > rain > flood > grow > burn
	 */
	public void run() {
	   
	   DisplayThread display = new DisplayThread("displayThread", hexMapService);
	   display.start();

		boolean findLeak = true;
		int ticks = 0;
		boolean foundLeak = false;
		
		while (display.isCreatingMap()){
		   sleep();
		}
		
		sleep();
		
		int totalWater = hexMapService.allWater()[0];
      int cycleTotal = hexMapService.allWater()[0];
      
      long burnTime = 0;
      long waterCycleTime = 0;
      long growTime = 0;
      long tectonicsTime = 0;
      long printTime = 0;
      long lastMark = 0;

		while (true){

			if (findLeak && !foundLeak){
				cycleTotal = hexMapService.allWater()[0];
			}
			
			lastMark = new Date().getTime();
			environmentService.burn();
			burnTime += new Date().getTime() - lastMark;
			
			if (findLeak && cycleTotal != hexMapService.allWater()[0] && !foundLeak){

				System.out.println("Burn leak");
				foundLeak = true;
			}

			lastMark = new Date().getTime();
			waterService.waterCycle(findLeak, foundLeak);
			waterCycleTime += new Date().getTime() - lastMark;

			if (findLeak && cycleTotal != hexMapService.allWater()[0] && !foundLeak){
				
				System.out.println("water leak");
				foundLeak = true;
			}
			
			lastMark = new Date().getTime();
			environmentService.grow(ticks);
			growTime += new Date().getTime() - lastMark;
			
			if (findLeak && cycleTotal != hexMapService.allWater()[0] && !foundLeak){
				
				System.out.println("Grow leak");
				foundLeak = true;
			}
			
			lastMark = new Date().getTime();
			environmentService.shiftTectonics();
			tectonicsTime += new Date().getTime() - lastMark;
			
			ticks ++;
			
			if (ticks % 10000 == 0){
				
				System.out.println("Run ticks: " + ticks);
				System.out.println("Burn time: " + burnTime / 1000);
				System.out.println("Water cycle time: " + waterCycleTime / 1000);
				System.out.println("Grow time: " + growTime / 1000);
				System.out.println("Tectonics time: " + tectonicsTime / 1000);
				System.out.println("Print time: " + printTime / 1000);
				
				burnTime = 0;
				waterCycleTime = 0;
				
				if (hexMapService.allWater()[0] < totalWater){
				
					System.out.println("We have a leak... " + hexMapService.allWater()[0]);
					
					totalWater = hexMapService.allWater()[0];
				}
				
			}
		}
	}
	
	private void sleep(){
	   try {
         Thread.sleep(1000);
      } catch (InterruptedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
	}
}
