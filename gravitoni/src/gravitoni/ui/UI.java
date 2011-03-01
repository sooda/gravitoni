package gravitoni.ui;

import gravitoni.simu.World;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.media.opengl.GLCanvas;
import com.sun.opengl.util.Animator;

@SuppressWarnings("serial")
public class UI extends JFrame implements ChangeListener {
	protected GLCanvas canvas;
	protected Animator animator;
	protected Renderer renderer;
	protected World world;
	protected SettingPane settings;
	
	public UI(World world) {
		super("Eippää, behold maailmankaikkeus!");
		this.world = world;
		canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(640, 480));
		
		renderer = new Renderer(world, canvas);
		canvas.addGLEventListener(renderer);
		canvas.addKeyListener(renderer);
		
		settings = new SettingPane(this);
		
		insertContents();
		pack();
		
		animator = new Animator(canvas);
		addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	          new Thread(new Runnable() {
	              public void run() {
	                animator.stop();
	                System.exit(0);
	              }
	            }).start();
	        }
	      });
		
		setVisible(true);
		animator.start();
	}
	
	private void insertContents() {
		Container content = getContentPane();
	    JPanel controlArea = settings;
	    content.add(controlArea, BorderLayout.WEST);
		content.add(canvas, BorderLayout.EAST);
	}
		
	//public void start() {
	//}
	
	public void stateChanged(ChangeEvent e) {
		int val = ((JSlider)e.getSource()).getValue();
		double zomg = val / 100.0 - 0.5;
		zomg = Math.exp(19 * zomg);
		renderer.setSpeed(zomg);
	}
}
