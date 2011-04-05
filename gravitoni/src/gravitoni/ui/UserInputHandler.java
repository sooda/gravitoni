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
		//double amount = 30, ramount = 1;
		switch (e.getKeyCode()) {
		/*
			case KeyEvent.VK_W:
				navigator.walk(0, 0, amount);
				break;
			case KeyEvent.VK_S:
				navigator.walk(0, 0, -amount);
				break;
			case KeyEvent.VK_A:
				navigator.walk(amount, 0, 0);
				break;
			case KeyEvent.VK_D:
				navigator.walk(-amount, 0, 0);
				break;
				
			case KeyEvent.VK_Z:
				navigator.walk(0, amount, 0);
				break;
			case KeyEvent.VK_X:
				navigator.walk(0, -amount, 0);
				break;
				
			case KeyEvent.VK_LEFT:
				navigator.rotate(0, -ramount, 0);
				break;
			case KeyEvent.VK_RIGHT:
				navigator.rotate(0, ramount, 0);
				break;
			case KeyEvent.VK_UP:
				navigator.rotate(-ramount, 0, 0);
				break;
			case KeyEvent.VK_DOWN:
				navigator.rotate(ramount, 0, 0);
				break;
			*/	
			case KeyEvent.VK_SPACE:
				// navigator.reset();
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
		if (SwingUtilities.isMiddleMouseButton(e)) {
			renderer.stopPan();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!shiftDown) renderer.zoom(e.getWheelRotation());
		else renderer.zoomBodies(e.getWheelRotation());
	}
}
