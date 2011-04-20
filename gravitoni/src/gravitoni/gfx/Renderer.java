package gravitoni.gfx;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.nio.IntBuffer;
import java.util.ArrayList;

import gravitoni.simu.*;
import gravitoni.ui.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;
import javax.swing.event.*;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.j2d.TextRenderer;

/** Renderer is the uppermost level of 3D drawing. 
 * 
 * This contains the GfxBodies and asks them to render themselves when needed. 
 * Also handles general OpenGL stuff. */
public class Renderer implements GLEventListener, ActionListener {
	private World world;
	private ArrayList<GfxBody> bodies = new ArrayList<GfxBody>();
	
	/** Index to the active one in bodies list. Can be changed with keyboard. */
	private int activeBody = -1;
	/** Relative origin. If null, global world origin is used. */
	private GfxBody origin = null;
	private GLCanvas canvas;
	
	/** How frequently should we update the graphics when simulating? */
	private double speed = 1;
	private GLUquadric qua;
	private GLU glu = new GLU();
	
	private UI ui;
	
	/** Popup menu for the canvas' right-click. */
	private JPopupMenu popup;	
	
	/** Current camera transformation since last operations. */
    private Matrix4f currCam = new Matrix4f();
    
	/** Last camera transformation how it was before the current drag. */
    private Matrix4f lastCam = new Matrix4f();
    
    /** Lock for rotations as the matrices are used from drawing and user input threads. */
    private final Object matrixLock = new Object();
    
    /** Raw matrix for opengl */
    private float[] matrix = new float[16];

    /** Rotation */
    private ArcBall arcBall = new ArcBall(800f, 600f);
    /** Panning start mouse position */
    private Point panStart;
    
    /** How small the world should look like */
    private double zoom = 1.5e6;
    
    /** How large do the planets look like */
    private double planetzoom = 1;    
    
    private boolean paused = false;
    /** Cursor visible in the world origin? */
    private boolean showCursor = true;
    
    /** Show sun's equator helper plane? */
    private boolean showEclPlane = true;
    
    /** When selecting, drawing is done in a funny way */
    private boolean selectMode = false;
    
    /** Where we picked with the mouse? */
    private Point selectPt;
    
    /** Where should we go while drawing? This is used only in paused mode (cannot be changed while in realtime). */
	private int timePercent = 100;
	
	/** Collision happened? Cannot continue */
	private boolean stopped = false;
	
	/** What was selected in the popup menu, used for picking */ 
	private String menuAction = "";
	
	TextRenderer tr;
    
	public Renderer(World world, UI ui, GLCanvas canvas) {
		this.world = world;
		this.ui = ui;
		this.canvas = canvas;
		
		canvas.addGLEventListener(this);
		
		buildMenu(canvas);
		
		UserInputHandler ih = new UserInputHandler(this);
		canvas.addKeyListener(ih);
		canvas.addMouseMotionListener(ih);
		canvas.addMouseListener(ih);
		canvas.addMouseWheelListener(ih);
		setSpeed(0);
	}
	
	void buildMenu(GLCanvas canvas) {
		popup = new JPopupMenu();
		
		JMenuItem itm = new JMenuItem("Detailselect");
		popup.add(itm);
		itm.addActionListener(this);
		
		itm = new JMenuItem("Distanceselect A");
		popup.add(itm);
		itm.addActionListener(this);
		
		itm = new JMenuItem("Distanceselect B");
		popup.add(itm);
		itm.addActionListener(this);

		itm = new JMenuItem("Cursorselect");
		popup.add(itm);
		itm.addActionListener(this);

		PopupListener l = new PopupListener();
		canvas.addMouseListener(l);		
		popup.addPopupMenuListener(l);
	}
	
	/** Start rotation dragging (mouse down) */
	public void startDrag(Point p) {
        synchronized(matrixLock) {
            lastCam.set(currCam);
        }
        arcBall.click(p);
		
	}
	
	/** Rotation drag */
	public void drag(Point p) {
        Quat ThisQuat = new Quat();

        arcBall.drag(p, ThisQuat);
        synchronized(matrixLock) {
            currCam.setRotation(ThisQuat);
            currCam.mul(currCam, lastCam);
        }
	}
	
	/** Start mouse panning (moving) */
	public void startPan(Point p) {   
    	canvas.display();
    	synchronized(matrixLock) {
    		panStart = p;
    		lastCam.set(currCam);
    	}
    }
    
	/** Pan the view */
    public void pan(Point p) {
    	synchronized(matrixLock) {
    		double dx = p.x - panStart.x, dy = -(p.y - panStart.y);
    		double scale = zoom / 750;
    		dx *= scale;
    		dy *= scale;
	        currCam.setTranslation(dx, dy, 0);
	    	currCam.mul(currCam, lastCam);
    	}
    }
    
    /** Zero the camera matrices */
	public void resetOrigin() {
        lastCam.setIdentity();
        currCam.setIdentity();
        currCam.get(matrix);
	}
	
	/** Zoom in if amount > 0, out if amount < 0 */
	public void zoom(double amount) {
		if (amount > 0) zoom *= 1.1 * amount;
		else if (amount < 0) zoom /= 1.1 * -amount;
	}
	
	public void zoomBodies(double amount) {
		if (amount > 0) planetzoom /= 1.1 * amount;
		else planetzoom *= 1.1 * -amount;		
	}

	/** Handle the popup events from the canvas */
	class PopupListener extends MouseInputAdapter implements PopupMenuListener {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	popup.setLightWeightPopupEnabled(false);
	            popup.show(e.getComponent(), e.getX(), e.getY());
	            selectPt = new Point(e.getX(), e.getY());
	        }
	    }
	    
		public void popupMenuCanceled(PopupMenuEvent e) {
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			cont();
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			pause();
		}
	}
	
	/** Popup menu clicks come here */
	public void actionPerformed(ActionEvent e) {
		menuAction = e.getActionCommand();
		selectMode = true;
	}

	/** This is called from the UI scrollbar */
	public void setSpeed(double speed) {
		speed = Math.exp(10 * speed);
		this.speed = speed;
	}

	/** Redraw the scene */
	public void display(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		
		// if new bodies have been added in the UI, we need to add them here because we need the GL instance
		if (bodies.size() < world.getBodies().size()) {
			for (int i = bodies.size(); i < world.getBodies().size(); i++)
				bodies.add(new GfxBody(world.getBodies().get(i), gl, qua, this));
		}
		
		if (selectMode) {
			select(gl, selectPt.x, canvas.getHeight() - selectPt.y);
			selectMode = false;
		} else {
			render(gl);
			runSimulation();
		}
	}
	
	/** Simulate some time depending on the speed */
	public void runSimulation() {
		if (!paused && !stopped) {
			int iters = (int)speed;
			double laststep = speed - iters;
			for (int i = 0; i < iters; i++) {
				if (!world.run(world.dt)) {
					stopped = true;
					break;
				}
				for (GfxBody b: bodies) b.update();
			}
			if (speed < world.dt)
			if (!stopped) {
				if (!world.run(laststep*world.dt))
					stopped = true;
			}
			for (GfxBody b: bodies) b.update();
			ui.refreshWidgets();
		}
	}

    public void toggleCursor() {
    	showCursor = !showCursor;
    }
    
    public void toggleEclPlane() {
    	showEclPlane = !showEclPlane;
    }
    
	/** Handle picking, ie selecting with mouse. 
	 * Go to GL_SELECT mode, render, find out the picked object and get back to GL_RENDER. */
	public void select(GL gl, int x, int y) {
		int[] buff = new int[64];
		IntBuffer buf = BufferUtil.newIntBuffer(64);		
		gl.glSelectBuffer(64, buf);
		gl.glRenderMode(GL.GL_SELECT);
		
		gl.glInitNames();
		gl.glPushName(0);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		int[] view = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, view, 0);
		IntBuffer vbuf = BufferUtil.newIntBuffer(4);
		vbuf.put(view);
		vbuf.rewind();
		
		glu.gluPickMatrix(x, y, 1, 1, vbuf);
		setPerspective(view[2], view[3]);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		render(gl);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
		
		int hits = gl.glRenderMode(GL.GL_RENDER);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		buf.rewind();
		buf.get(buff);
		for (int i = 0; i < hits; i++) {
			int id = buff[i * 4 + 3];
			if (id > 0) {
				pick(bodies.get(id - 1));
				break;
			}
		}
	}
	
	/** Do something when a body is picked with the mouse */
	private void pick(GfxBody b) {
		System.out.println("Picked " + b);
		if (menuAction.equals("Detailselect")) {
			ui.getSettings().setSelected(b.getBody());
		} else if (menuAction.equals("Distanceselect A")) {
			ui.getSettings().setDistanceBodies(b.getBody(), null);
		} else if (menuAction.equals("Distanceselect B")) {
			ui.getSettings().setDistanceBodies(null, b.getBody());
		} else if (menuAction.equals("Cursorselect")) {
			for (int i = 0; i < bodies.size(); i++) {
				if (bodies.get(i) == b) {
					activeBody = i;
					break;
				}
			}
		}
	}
	
	/** Actually draw the scene */
	public void render(GL gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		gl.glTranslated(0, 0, -zoom);
        currCam.get(matrix);
        gl.glMultMatrixf(matrix, 0);

		if (!selectMode && showCursor) drawCursor(gl);
		if (showEclPlane) drawEclPlane(gl);
		drawBodies(gl);
		
		if (stopped) {
			tr.beginRendering(800, 600);
			tr.setColor(1, 1, 1, 1);
			tr.draw("[STOPPED]", 10, 10);
			tr.endRendering();
		}
	}
	
	private void drawBodies(GL gl) {
		int i = 0;
		for (GfxBody b: bodies) {
			gl.glLoadName(++i); // for picking
			b.render(gl, b == getActiveBody(), planetzoom, timePercent);
		}
	}
	
	/** Cursor is three arrows in world origin */
	private void drawCursor(GL gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
		
		float size = 40000, base = 1000;
		
		gl.glColor3d(0, 0, 1);
		glu.gluCylinder(qua, base, 1, size, 24, 240);
		
		gl.glColor3d(0, 1, 0);
		gl.glRotated(90, 1, 0, 0);
		glu.gluCylinder(qua, base, 1, size, 24, 240);
		gl.glRotated(-90, 1, 0, 0);
		
		gl.glColor3d(1, 0, 0);
		gl.glRotated(90, 0, 1, 0);
		glu.gluCylinder(qua, base, 1, size, 24, 240);
		gl.glRotated(-90, 0, 1, 0);
	}
	
	private void drawEclPlane(GL gl) {
		gl.glColor3d(.5,.5,.5);
		glu.gluQuadricDrawStyle(qua, GLU.GLU_LINE);
		glu.gluDisk(qua, 0, 800000, 36, 1);
		glu.gluQuadricDrawStyle(qua, GLU.GLU_FILL);
	}

	public void pause() {
		paused = true;
		ui.getSettings().setPause();
	}
	public void cont() {
		paused = false;
		timePercent = 100;
		ui.getSettings().setPause(false);
	}
	
	public void togglePause() {
		paused = !paused;
		if (!paused) timePercent = 100; // continue at the end
		ui.getSettings().setPause(paused);
	}
	public void setPause(boolean state) {
		paused = state;
	}
	
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// unimplemented in jogl?
	}

	/** Initialize opengl */
	public void init(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		
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
			bodies.add(new GfxBody(b, gl, qua, this));
		}
		
        lastCam.setIdentity();
        currCam.setIdentity();
        currCam.get(matrix);    
        tr = new TextRenderer(new Font("Courier New", 0, 12));
	}
	
	/** Opengl canvas size changed (not used, I guess) */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL gl = drawable.getGL();
		
		gl.setSwapInterval(1);

		gl.glViewport(0, 0, width, height);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		setPerspective(width, height);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
        arcBall.setBounds((float)width, (float)height);
	}
	
	void setPerspective(double width, double height) {
		glu.gluPerspective(45, width / height, 100, 15000000);
	}
	
	/** Set the relative origin to be the selected body */
	public void setOriginActive() {
		origin = getActiveBody();
		System.out.println("Set origin: " + (origin == null ? "(null)" : origin.getBody().getName()));
	}
	
	/** Active is the one that is hilighted white and can be choosed to be origin by pressing space */
	public GfxBody getActiveBody() {
		return activeBody == -1 ? null : bodies.get(activeBody);
	}
	
	public GfxBody getOrigin() {
		return origin;
	}
    
	public void nextActive() {
		if (++activeBody == bodies.size()) activeBody = -1;
		System.out.println("Selected: " + getActiveBody());
	}
	
	public void prevActive() {
		if (--activeBody == -2) activeBody = bodies.size() - 1;
		System.out.println("Selected: " + getActiveBody());
	}

	/** Draw only until the given time percent. */
	public void setTime(int percent) {
		timePercent = percent;
	}

}
