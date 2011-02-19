package gravitoni.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import gravitoni.simu.*;
import demos.common.TextureReader;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;

public class Renderer implements GLEventListener, ActionListener {

	protected float pyramidRotation;
	protected float cubeRotation;
	private double speed=1;
	private GLUquadric  qua;
	private World world;
	private int texture;
	private GLU glu = new GLU();
	
	private JMenuBar menuBar;
	private JPopupMenu popup;
	
	public Renderer(World world, GLCanvas canvas) {
		this.world = world;
		menuBar = new JMenuBar();
		JMenu menu = new JMenu("Asdf");
		menuBar.add(menu);
		popup = new JPopupMenu();
		JMenuItem itm = new JMenuItem("ASdfzing");
		menu.add(itm);
		itm.addActionListener(this);
		popup.add(itm);
		itm = new JMenuItem("AZomgf");
		itm.addActionListener(this);
		menu.add(itm);
		popup.add(itm);
		MouseListener l = new PopupListener();
		menuBar.addMouseListener(l);
		canvas.addMouseListener(l);
		
	}
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("JEAAAAAAH" + e);
		System.out.println("ASDDDDF" + e.getSource());
		
	}


	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public void display(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		for (Body b: world.getBodies()) {
			gl.glLoadIdentity();
			gl.glColor3d(1, 1, 1);
			Vec3 pos = b.getPos();
			//System.out.println("Hox! " + b.getName() + "; " + (100/1e11*pos.x) + "," + (100/1e11*pos.y) + "," + pos.z + "   " + (1/1e3 * b.getRadius()));
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
			gl.glEnable(GL.GL_TEXTURE_GEN_S);
			gl.glEnable(GL.GL_TEXTURE_GEN_T);
			gl.glTranslated(100/1e11 * pos.x, 100/1e11 * pos.y, -1000f);
			//gl.glTranslated(100, 100, -1000f);
			glu.gluSphere(qua, 120/7e8 * b.getRadius(), 20, 20);
			//glu.gluSphere(qua, 100, 20, 20);
		}
		//System.out.println("Render!");
		world.run(speed * world.dt);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// unimplemented in jogl?
	}

	public void init(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		
		gl.glEnable(GL.GL_TEXTURE_2D);
		texture = genTexture(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        TextureReader.Texture texture = null;
        try {
            texture = TextureReader.readTexture("textures/earth.png");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        makeRGBTexture(gl, glu, texture, GL.GL_TEXTURE_2D, false);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        
        gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
    	gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
    	
    	/*
    	 * 
    	 * static GLfloat	LightAmb[] = {0.7f, 0.7f, 0.7f, 1.0f};				// Ambient Light
static GLfloat	LightDif[] = {1.0f, 1.0f, 1.0f, 1.0f};				// Diffuse Light
static GLfloat	LightPos[] = {4.0f, 4.0f, 6.0f, 1.0f};				// Light Position
    	 * 
    	glLightfv(GL_LIGHT0, GL_AMBIENT, LightAmb);				// Set The Ambient Lighting For Light0
	glLightfv(GL_LIGHT0, GL_DIFFUSE, LightDif);				// Set The Diffuse Lighting For Light0
	glLightfv(GL_LIGHT0, GL_POSITION, LightPos);				// Set The Position For Light0

	glEnable(GL_LIGHT0);							// Enable Light 0
	glEnable(GL_LIGHTING);							// Enable Lighting
	
    	 */
        
	}
	
	private int genTexture(GL gl) {
        final int[] tmp = new int[1];
        gl.glGenTextures(1, tmp, 0);
        return tmp[0];
    }
	private void makeRGBTexture(GL gl, GLU glu, TextureReader.Texture img, 
            int target, boolean mipmapped) {
        
        if (mipmapped) {
            glu.gluBuild2DMipmaps(target, GL.GL_RGB8, img.getWidth(), 
                    img.getHeight(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, img.getPixels());
        } else {
            gl.glTexImage2D(target, 0, GL.GL_RGB, img.getWidth(), 
                    img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, img.getPixels());
        }
    }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL gl = drawable.getGL();
		//final GLU glu = new GLU();
		
		gl.setSwapInterval(1);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		glu.gluPerspective(
				45.0f, 
				(double) width / (double) height, 
				0.1f,
				1000.0f);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		qua = glu.gluNewQuadric();
	}

}
