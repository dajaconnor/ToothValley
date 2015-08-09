package enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Direction {

	north, northeast, southeast, south, southwest, northwest;
	
	public static final List<Direction> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();

	public static Direction randomDirection() {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}
	
	public Direction turnRight() {
		return Direction.values()[(this.ordinal() + 1) % Direction.values().length];
	}
	
	public Direction turnLeft() {
		return Direction.values()[(this.ordinal() + Direction.values().length - 1) % Direction.values().length];
	}
}
