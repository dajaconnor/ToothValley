package controllers;


import graphics.DisplayThread;
import impl.EnvironmentService;
import impl.HexMapService;

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

		boolean findLeak = false;
		int ticks = 0;
		boolean leak = false;
		
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

			if (findLeak){
				cycleTotal = hexMapService.allWater()[0];
				leak = false;
			}
			
			lastMark = new Date().getTime();
			environmentService.burn();
			burnTime += new Date().getTime() - lastMark;
			
			if (findLeak && cycleTotal != hexMapService.allWater()[0]){

				System.out.println("Burn leak");
				leak = true;
			}

			lastMark = new Date().getTime();
			environmentService.waterCycle(findLeak);
			waterCycleTime += new Date().getTime() - lastMark;

			if (findLeak && cycleTotal != hexMapService.allWater()[0] && !leak){
				
				System.out.println("water leak");
				leak = true;
			}
			
			lastMark = new Date().getTime();
			environmentService.grow();
			growTime += new Date().getTime() - lastMark;
			
			if (findLeak && cycleTotal != hexMapService.allWater()[0] && !leak){
				
				System.out.println("Grow leak");
				leak = true;
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
