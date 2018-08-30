package impl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.Display;

import models.Hex;
import models.HexMap;

public class WaterThread implements Runnable {

    private WaterService waterService;
    
    private Thread t;
	private String threadName;
	private List<Hex> hexes;

	public WaterThread( String name, WaterService waterService){
	   threadName = name;
	   this.waterService = waterService;
	   System.out.println("Creating " +  threadName );
   }
	
	@Override
	public void run() {

		while(!Display.isCloseRequested()){
			
			moveAllTheWater();
		}
	}
	
	public void start ()
	   {
	      System.out.println("Starting " +  threadName );
	      if (t == null)
	      {
	         t = new Thread (this, threadName);
	         t.start ();
	      }
	   }

	private void moveAllTheWater() {
		
		if (hexes == null) hexes = new ArrayList<Hex>(HexMap.getInstance().getHexes().values());

		else {
			
			int numHexes = hexes.size();
			
			for (int i = 0; i < numHexes; i++) {

				int third = (i * 3) / numHexes;
				int index = (i * 3 + third ) % numHexes;
				Hex hex = hexes.get( index );
				
				waterService.flood( hex, hex.getStandingWater() );
			}
		}
		
	}
}
