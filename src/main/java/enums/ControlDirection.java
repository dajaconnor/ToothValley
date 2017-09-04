package enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ControlDirection {

	UP,RIGHT,DOWN,LEFT;
	
	public static final List<ControlDirection> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int DEGREES_PER_DIRECTION = 360 / VALUES.size();
	
	public ControlDirection correctForRotation(int degreeTurn, boolean toggleFlyoverType) {
		
		degreeTurn = normalizeDegreeToType(degreeTurn, toggleFlyoverType);
		
		degreeTurn %= 360;
		if (degreeTurn < 0) degreeTurn += 360;
		int shiftRight = (degreeTurn + (DEGREES_PER_DIRECTION / 2)) / DEGREES_PER_DIRECTION;

		return VALUES.get((this.ordinal() + shiftRight) % VALUES.size());
	}
	
	public Direction convertToDirection(int degreeTurn, boolean toggleFlyoverType){
		
		degreeTurn = normalizeDegreeToType(degreeTurn, toggleFlyoverType);
		degreeTurn += this.ordinal() * DEGREES_PER_DIRECTION;
		
		return Direction.getByDegree(degreeTurn);
	}
	
	private int normalizeDegreeToType(int degreeTurn, boolean toggleFlyoverType){
		if (toggleFlyoverType) degreeTurn+=180;
		return degreeTurn;
	}
}
