package enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.TheRandom;

public enum Direction {

	north, northeast, southeast, south, southwest, northwest;
	
	public static final List<Direction> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int SIZE = VALUES.size();
	public static final int DEGREES_PER_DIRECTION = 360 / SIZE;

	public static Direction randomDirection() {
		return VALUES.get(TheRandom.getInstance().get().nextInt(SIZE));
	}
	
	public Direction turnRight() {
		return Direction.values()[(this.ordinal() + 1) % Direction.values().length];
	}
	
	public Direction turnLeft() {
		return Direction.values()[(this.ordinal() + Direction.values().length - 1) % Direction.values().length];
	}
	
	public Direction takeRandomTurn(){
	   if (TheRandom.getInstance().get().nextInt(2) == 1){
	      return turnRight();
	   } else{
	      return turnLeft();
	   }
	}
	
	public static Direction getByDegree(int degrees){
		
		degrees %= 360;
		if (degrees < 0) degrees += 360;
		int shiftRight = (degrees + (DEGREES_PER_DIRECTION / 2)) / DEGREES_PER_DIRECTION;
		return VALUES.get(shiftRight % SIZE);
	}
}
