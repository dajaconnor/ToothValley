package listeners;

import enums.DisplayType;
import graphics.Window;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler  implements KeyListener {

	
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void keyReleased(KeyEvent key) {
		
   		Window window = Window.getInstance();
		
		switch (key.getKeyCode()){
		
		case KeyEvent.VK_F1:
			
			window.setDisplayType(DisplayType.NORMAL);
			
			break;
			
		case KeyEvent.VK_F2:
			
			window.setDisplayType(DisplayType.ELEVATION);
			
			break;
			
		case KeyEvent.VK_F3:
			
			window.setDisplayType(DisplayType.HUMIDITY);
			
			break;
			
		case KeyEvent.VK_F4:
			
			window.setDisplayType(DisplayType.DENSITY);
			
			break;
			
		case KeyEvent.VK_F5:
			
			window.setDisplayType(DisplayType.MOISTURE);
			
			break;
			
		case KeyEvent.VK_UP:
			
			window.moveY(+1);
			
			break;
			
		case KeyEvent.VK_DOWN:
			
			window.moveY(-1);
			
			break;
			
		case KeyEvent.VK_RIGHT:
			
			window.moveX(-1);
			
			break;
			
		case KeyEvent.VK_LEFT:
			
			window.moveX(+1);
			
			break;
			
		case KeyEvent.VK_SPACE:
			
			if (window.isRunning()){
				
				window.setRunning(false);
			}
			else{
				
				window.setRunning(true);
			}
			break;
			
		default:
			
			break;
		}
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}