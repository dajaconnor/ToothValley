package controllers;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import graphics.DisplayThread;
import impl.EnvironmentService;
import impl.HexMapService;
import impl.WaterService;
import models.Environment;
import models.HexMap;
import models.UserActions;

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

		initializeBasicVars();
		DisplayThread display = new DisplayThread("displayThread", hexMapService);
		display.start();

		boolean findLeak = false;
		boolean foundLeak = false;
		HexMap map = HexMap.getInstance();

		while (display.isCreatingMap() && map.getHexes().size() != Environment.MAP_GRID[0] * Environment.MAP_GRID[1]){
			sleep();
		}

		sleep();

		int totalWater = hexMapService.allWater()[0];
		int cycleTotal = hexMapService.allWater()[0];

		long burnTime = 0;
		long waterCycleTime = 0;
		long growTime = 0;
		long tectonicsTime = 0;
		long lastMark = 0;
		
//		WaterThread water = new WaterThread("waterThread", waterService);
//		water.start();

		while (!UserActions.getInstance().isCloseProgram()){

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
			environmentService.grow();
			growTime += new Date().getTime() - lastMark;

			if (findLeak && cycleTotal != hexMapService.allWater()[0] && !foundLeak){

				System.out.println("Grow leak");
				foundLeak = true;
			}

			lastMark = new Date().getTime();
			environmentService.shiftTectonics();
			tectonicsTime += new Date().getTime() - lastMark;
			
			map.tick();

			if (map.getTicks() % 100 == 0){

				System.out.println("Run ticks: " + map.getTicks());
				System.out.println("Burn time: " + burnTime / 1000);
				System.out.println("Water cycle time: " + waterCycleTime / 1000);
				System.out.println("Grow time: " + growTime / 1000);
				System.out.println("Tectonics time: " + tectonicsTime / 1000);
				System.out.println("Total water: " + hexMapService.allWater()[0]);

				burnTime = 0;
				waterCycleTime = 0;

				if (findLeak && hexMapService.allWater()[0] < totalWater){

					System.out.println("We have a leak... " + hexMapService.allWater()[0]);

					totalWater = hexMapService.allWater()[0];
				}

				if (map.getTicks() == 100){
					
					UserActions.getInstance().setRealisticWaterFlow(false);
				}
			}
		}
		
		System.exit(0);
	}

	private void sleep(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initializeBasicVars(){

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int height = (screenSize.height * 3/2) / (int) Environment.HEX_HEIGHT;
		int width = screenSize.width / (int) (Environment.HEX_SIDE_WIDTH + Environment.HEX_BODY_WIDTH);

		if (width % 2 == 1){

			width ++;
		}

		int[] hexSize = new int[2];
		hexSize[0] = width;
		hexSize[1] = height;
		
		int[] trueCenter = new int[2];
		trueCenter[0] = (int) ((width/2) * (Environment.HEX_BODY_WIDTH + Environment.HEX_SIDE_WIDTH) + Environment.HEX_SIDE_WIDTH);
		trueCenter[1] = (int) ((height/2) * Environment.HEX_HEIGHT + Environment.HEX_HEIGHT / 2);

		Environment.MAP_GRID = hexSize;
		Environment.TRUE_CENTER = trueCenter;
	}
}
