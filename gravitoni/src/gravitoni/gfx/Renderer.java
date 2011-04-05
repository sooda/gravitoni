package gravitoni.gfx;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;

import gravitoni.simu.*;
import gravitoni.ui.*;
import demos.common.TextureReader;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;
import javax.swing.event.*;

public class Renderer implements GLEventListener, ActionListener {

	private World world;
	private ArrayList<GfxBody> bodies = new ArrayList<GfxBody>();
	private int activeBody = -1;
	private Body origin = null;
	
	private double speed = 1;
	private GLUquadric qua;
	private GLU glu = new GLU();
	private GL gl;
	
	//private Navigator navigator;
	
	private JMenuBar menuBar;
	private JPopupMenu popup;
	
	private UI ui;
	
    private Matrix4f LastRot = new Matrix4f();
    private Matrix4f ThisRot = new Matrix4f();
    private final Object matrixLock = new Object();
    private float[] matrix = new float[16];

    private ArcBall arcBall = new ArcBall(640.0f, 480.0f);
    
    private double zoom = 1500, planetzoom = 1;
    
    private Point pan = new Point(), panStart, panCurrent = new Point();
    
    private boolean paused = false;

	
	public Renderer(World world, UI ui, GLCanvas canvas) {
		this.world = world;
		this.ui = ui;
		
		//navigator = new Navigator();

		
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
		canvas.addGLEventListener(this);

		
		UserInputHandler ih = new UserInputHandler(this);
		canvas.addKeyListener(ih);
		canvas.addMouseMotionListener(ih);
		canvas.addMouseListener(ih);
		canvas.addMouseWheelListener(ih);
		setSpeed(0.3);
	}
	
	public void startDrag(Point p) {
        synchronized(matrixLock) {
            LastRot.set( ThisRot );                                        // Set Last Static Rotation To Last Dynamic One
        }
        arcBall.click( p );                                 // Update Start Vector And Prepare For Dragging
		
	}
	public void drag(Point p) {
        Quat4f ThisQuat = new Quat4f();

        arcBall.drag( p, ThisQuat);                         // Update End Vector And Get Rotation As Quaternion
        synchronized(matrixLock) {
            ThisRot.setRotation(ThisQuat);     // Convert Quaternion Into Matrix3fT
            ThisRot.mul( ThisRot, LastRot);                // Accumulate Last Rotation Into This One
        }
	}
	
	class PopupListener extends MouseInputAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("JEAAAAAAH" + e);
		System.out.println("ASDDDDF" + e.getSource());
		
	}


	public Body getActiveBody() {
		return activeBody == -1 ? null : bodies.get(activeBody).getBody();
	}
	
	public void setSpeed(double speed) {
		System.out.println("SPD "+speed);
		speed = Math.exp(3 * speed);
		this.speed = speed;
	}
	
	public void display(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		this.gl = gl;

        ThisRot.get(matrix);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		gl.glTranslated(0, 0, -zoom);
        gl.glMultMatrixf(matrix, 0);
        Point tmppan = new Point(pan);
        tmppan.x += panCurrent.x;
        tmppan.y += panCurrent.y;
		gl.glTranslated(tmppan.x, -tmppan.y, 0);
		
		drawCursor(gl);
		
		if (origin != null) {
			Vec3 pos = origin.getPos();
			//System.out.println(pos);
			gl.glTranslated(-10/1e7 * pos.x, -10/1e7 * pos.y, pos.z);
		}
		
		Body active = getActiveBody();
		for (GfxBody b: bodies) {
			b.render(gl, b.getBody() == active, planetzoom);
		}
		ui.refreshWidgets();
		if (!paused) {
			int iters = (int)speed;
			double laststep = speed - iters;
			for (int i = 0; i < iters; i++) {
				world.run(world.dt);
			}
			world.run(laststep*world.dt);
		}
	}
	
	public void pause() {
		paused = true;
	}
	public void cont() {
		paused = false;
	}
	
	private void drawCursor(GL gl) {
		gl.glPushMatrix();
		gl.glDisable(GL.GL_TEXTURE_2D);
		
		gl.glColor3d(0, 0, 1);
		glu.gluCylinder(qua, 20, 1, 200, 24, 240);
		
		gl.glColor3d(0, 1, 0);
		gl.glRotated(90, 1, 0, 0);
		glu.gluCylinder(qua, 20, 1, 200, 24, 240);
		gl.glRotated(-90, 1, 0, 0);
		
		gl.glColor3d(1, 0, 0);
		gl.glRotated(90, 0, 1, 0);
		glu.gluCylinder(qua, 20, 1, 200, 24, 240);
		gl.glRotated(-90, 0, 1, 0);
		gl.glPopMatrix();
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// unimplemented in jogl?
	}

	public void init(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		this.gl = gl;
		
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);		
		gl.glEnable(GL.GL_TEXTURE_2D);

    	qua = glu.gluNewQuadric();
    	glu.gluQuadricTexture(qua, true);
    	
		for (Body b: world.getBodies()) {
			bodies.add(new GfxBody(b, gl, qua));
		}
		
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
		
        LastRot.setIdentity();
        ThisRot.setIdentity();
        ThisRot.get(matrix);

        
	}
	
	/*private int genTexture(GL gl) {
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
    */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL gl = drawable.getGL();
		
		gl.setSwapInterval(1);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		glu.gluPerspective(
				45.0f, 
				(double) width / (double) height, 
				1f,
				300000.0f);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
        arcBall.setBounds((float) width, (float) height);

	}
	
	public void resetOrigin() {
		origin = getActiveBody();
	}
    public void startPan(Point p) {
    	panStart = p;
    }
    public void pan(Point p) {
    	panCurrent.x = p.x - panStart.x;
    	panCurrent.y = p.y - panStart.y;
    }
    public void stopPan() {
		pan.x += panCurrent.x;
		pan.y += panCurrent.y;
		panCurrent.x = 0;
		panCurrent.y = 0;
    }
    
	public void nextActive() {
		if (++activeBody == bodies.size()) activeBody = -1;
	}
	public void prevActive() {
		if (--activeBody == -2) activeBody = bodies.size() - 1;
	}

	public void zoom(double amount) {
		if (amount > 0) zoom *= 1.1 * amount;
		else zoom /= 1.1 * -amount;
	}
	
	public void zoomBodies(double amount) {
		if (amount > 0) planetzoom *= 1.1 * amount;
		else planetzoom /= 1.1 * -amount;		
	}

}
