package models;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserActions {

	//For singletonhood
	private static UserActions instance = new UserActions();

	public UserActions() {
		// Exists only to defeat instantiation.
	}
	
	public static UserActions getInstance() {

		return instance;
	}
	
	private int waterChangedByUser = 0;
	private static ReadWriteLock waterChangedByUserLock = new ReentrantReadWriteLock();
	
	private int extraTectonicActivity = 0;
	private static ReadWriteLock extraTectonicActivityLock = new ReentrantReadWriteLock();
	
	private boolean[] plantGrowth = new boolean[Environment.NUM_PLANT_TYPES];
	private static ReadWriteLock plantGrowthLock = new ReentrantReadWriteLock();
	
	private boolean realisticWaterFlow = true;
	private static ReadWriteLock realisticWaterFlowUserLock = new ReentrantReadWriteLock();
	
	private boolean closeProgram = false;
	private static ReadWriteLock closeProgramLock = new ReentrantReadWriteLock();
	
	public int getAndResetWaterChangedByUser() {
		
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
	
	public int getAndResetTectonicActivity() {
		
		int change = 0;
		extraTectonicActivityLock.writeLock().lock();

		try{ 
			change = extraTectonicActivity;
			extraTectonicActivity = 0;
		} finally{
			extraTectonicActivityLock.writeLock().unlock();
		}

		return change;
	}
	
	public void incrementTectonicActivity(int change) {
		
		extraTectonicActivityLock.writeLock().lock();

		try{ 

			this.extraTectonicActivity += change;
		} finally{
			extraTectonicActivityLock.writeLock().unlock();
		}
	}
	
	public boolean[] getPlantGrowth() {
		
		boolean[] change;
		plantGrowthLock.writeLock().lock();

		try{ 
			change = plantGrowth;
			plantGrowth = new boolean[Environment.NUM_PLANT_TYPES];
		} finally{
			plantGrowthLock.writeLock().unlock();
		}

		return change;
	}
	
	public void addPlants(int typeOfPlant) {
		
		plantGrowthLock.writeLock().lock();

		try{ 

			this.plantGrowth[typeOfPlant] = true;
		} finally{
			plantGrowthLock.writeLock().unlock();
		}
	}

	public boolean realisticWaterFlow(){
		realisticWaterFlowUserLock.readLock().lock();
		boolean tempBool = false;

		try{
			tempBool = realisticWaterFlow;
		} finally{
			realisticWaterFlowUserLock.readLock().unlock();
		}

		return tempBool;
	}

	public void toggleRealisticWaterFlow() {
		
		realisticWaterFlowUserLock.writeLock().lock();

		try{ 

			this.realisticWaterFlow = !this.realisticWaterFlow;
		} finally{
			realisticWaterFlowUserLock.writeLock().unlock();
		}
	}
	
	public void setRealisticWaterFlow(boolean waterFlowIsRealistic) {
		
		realisticWaterFlowUserLock.writeLock().lock();

		try{ 

			this.realisticWaterFlow = waterFlowIsRealistic;
		} finally{
			realisticWaterFlowUserLock.writeLock().unlock();
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
