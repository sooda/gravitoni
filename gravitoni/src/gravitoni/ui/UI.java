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
	
	public UI(World world) {
		super("Eippää, maailmankaikkeussimulaattori!");
		this.world = world;
		canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(640, 480));
		renderer = new Renderer(world);
		canvas.addGLEventListener(renderer);
		insertContents();
		//setSize(800, 600);
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
	    JPanel controlArea = new JPanel(new GridLayout(4, 1));
	    controlArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
	    controlArea.add(new Button("Yks"));
	    controlArea.add(new Button("kakx"));
	    controlArea.add(new Button("coo'lme"));
	    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
	    slider.addChangeListener(this);
	    controlArea.add(slider);
	    content.add(controlArea, BorderLayout.WEST);
		content.add(canvas, BorderLayout.EAST);
	}
		
	public void start() {
	}
	
	//public static void main(String[] args) {
		//new UI();
	//}

	public void stateChanged(ChangeEvent e) {
		int val = ((JSlider)e.getSource()).getValue();
		double zomg = val / 1000.0 - 0.5;
		zomg = Math.exp(12 * zomg);
		renderer.setSpeed(zomg);
	}
}
