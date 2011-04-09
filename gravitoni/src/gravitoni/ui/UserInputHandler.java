package gravitoni.ui;

import gravitoni.gfx.Renderer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

public class UserInputHandler implements KeyListener, MouseMotionListener, MouseInputListener, MouseWheelListener {
	private Renderer renderer;
	private boolean shiftDown = false;
	
	public UserInputHandler(Renderer renderer) {
		this.renderer = renderer;
	}
	
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_P:
				renderer.togglePause();
				break;
		
			case KeyEvent.VK_SPACE:
				renderer.originActive();
				break;
				
			case KeyEvent.VK_C:
				renderer.toggleCursor();
				break;
				
			case KeyEvent.VK_E:
				renderer.toggleEclPlane();
				break;
				
			case KeyEvent.VK_R:
				renderer.resetOrigin();
				break;
				
			case KeyEvent.VK_LESS:
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)
					renderer.nextActive();
				else
					renderer.prevActive();
				break;
			case KeyEvent.VK_GREATER:
				System.out.println("Impossible, WTF?");
				break;
				
			case KeyEvent.VK_SHIFT:
				System.out.println("Shift_v");
				shiftDown = true;
				break;
				
			default:
				System.out.println("Unknown keypress on canvas: " + e);
		}
		
	}
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SHIFT:
			System.out.println("Shift_^");
			shiftDown = false;
			break;
		}
	}
	public void keyTyped(KeyEvent e) {
	}

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            renderer.startDrag(e.getPoint());
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
        	renderer.startPan(e.getPoint());
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            renderer.drag(e.getPoint());
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
        	renderer.pan(e.getPoint());
        }
    }

	public void mouseMoved(MouseEvent e) {
		//System.out.println(e.getX() + " " + e.getY());
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!shiftDown) renderer.zoom(e.getWheelRotation());
		else renderer.zoomBodies(e.getWheelRotation());
	}
}
