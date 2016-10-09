package graphics;

import impl.HexMapService;

public class DisplayThread implements Runnable {

   private HexMapService hexMapService;
   
	private Thread t;
	private String threadName;
	private volatile boolean creatingMap;

	public DisplayThread( String name, HexMapService hexMapService){
	   setCreatingMap(true);
	   threadName = name;
	   this.hexMapService = hexMapService;
	   System.out.println("Creating " +  threadName );
   }

   public void run() {

      OpenGLWindow window = OpenGLWindow.getInstance();
      hexMapService.createMap();
      int ticks = 0;
      setCreatingMap(false);

      while(true){
         window.printMap();
         ticks++;
         if (ticks % 10000 == 0){
            System.out.println("Print ticks: " + ticks);
         }
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

   public boolean isCreatingMap() {
      return creatingMap;
   }

   private void setCreatingMap(boolean creatingMap) {
      this.creatingMap = creatingMap;
   }
}
