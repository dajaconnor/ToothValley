package enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ControlDirection {

	RIGHT,DOWN,LEFT,UP;
	
	public static final List<ControlDirection> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

	public ControlDirection correctForRotation(int degreeTurn, boolean toggleFlyoverType) {
		
		if (toggleFlyoverType) degreeTurn+=180;
		
		degreeTurn %= 360;
		if (degreeTurn < 0) degreeTurn += 360;
		int shiftRight = (degreeTurn + 45) / 90; // should be 1 - 4

		return VALUES.get((this.ordinal() + shiftRight) % 4);
	}
}
