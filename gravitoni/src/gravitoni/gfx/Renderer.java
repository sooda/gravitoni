package gravitoni.gfx;

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

public class Renderer implements GLEventListener, ActionListener {
	private World world;
	private ArrayList<GfxBody> bodies = new ArrayList<GfxBody>();
	private int activeBody = -1;
	private GfxBody origin = null;
	private GLCanvas canvas;
	
	private double speed = 1;
	private GLUquadric qua;
	private GLU glu = new GLU();
	
	private UI ui;
	private JPopupMenu popup;	
	
    private Matrix4f LastRot = new Matrix4f(); // transformation since last operations
    private Matrix4f ThisRot = new Matrix4f(); // current state with current pan/rotation
    private final Object matrixLock = new Object();
    private float[] matrix = new float[16];

    private ArcBall arcBall = new ArcBall(640.0f, 480.0f);
    private Point panStart;
    
    private double zoom = 350000, planetzoom = 1;    
    
    private boolean paused = false;
    private boolean showCursor = true;
    private boolean showEclPlane = true;
    
    private boolean selectMode = false;
    private Point selectPt;
    
    public Point startPanPos = null, currPanPos = null;
	
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
		setSpeed(0.3);
	}
	
	void buildMenu(GLCanvas canvas) {
		popup = new JPopupMenu();
		
		JMenuItem itm = new JMenuItem("Select");
		popup.add(itm);
		itm.addActionListener(this);
		
		itm = new JMenuItem("AZomgf");
		itm.setEnabled(false);
		popup.add(itm);
		itm.addActionListener(this);
		
		PopupListener l = new PopupListener();
		canvas.addMouseListener(l);		
		popup.addPopupMenuListener(l);
	}
	
	public void startDrag(Point p) {
        synchronized(matrixLock) {
            LastRot.set(ThisRot);
        }
        arcBall.click(p);
		
	}
	public void drag(Point p) {
        Quat ThisQuat = new Quat();

        arcBall.drag(p, ThisQuat);
        synchronized(matrixLock) {
            ThisRot.setRotation(ThisQuat);
            ThisRot.mul(ThisRot, LastRot);
        }
	}
	
	public void startPan(Point p) {   
    	startPanPos = p;
    	canvas.display();
    	synchronized(matrixLock) {
    		panStart = p;
    		LastRot.set(ThisRot);
    	}
    }
    
    public void pan(Point p) {
    	currPanPos = p;
    	synchronized(matrixLock) {
    		double dx = p.x - panStart.x, dy = -(p.y - panStart.y);
    		double scale = zoom / 750;
    		dx *= scale;
    		dy *= scale;
	        ThisRot.setTranslation(dx, dy, 0);
	    	ThisRot.mul(ThisRot, LastRot);
    	}
    }
    
	public void resetOrigin() {
        LastRot.setIdentity();
        ThisRot.setIdentity();
        ThisRot.get(matrix);
	}
	public void zoom(double amount) {
		if (amount > 0) zoom *= 1.1 * amount;
		else zoom /= 1.1 * -amount;
	}
	
	public void zoomBodies(double amount) {
		if (amount > 0) planetzoom *= 1.1 * amount;
		else planetzoom /= 1.1 * -amount;		
	}

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
			System.out.println("popupcont");
			cont();
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			System.out.println("popuppause");
			pause();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		System.out.println("Tottoroo " + e);
		if ("Select".equals(cmd)) {
			selectMode = true;
		}
	}

	public void setSpeed(double speed) {
		System.out.println("SPD "+speed);
		speed = Math.exp(10 * speed);
		this.speed = speed;
	}
	
	public void display(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		if (selectMode) {
			select(gl, selectPt.x, canvas.getHeight() - selectPt.y);
			selectMode = false;
		} else {
			render(gl);
			runSimulation();
		}
	}
	
    public void toggleCursor() {
    	showCursor = !showCursor;
    }
    
    public void toggleEclPlane() {
    	showEclPlane = !showEclPlane;
    }
    
	
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
		
		System.out.println("HIts: " + hits);
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
	
	private void pick(GfxBody b) {
		System.out.println("Picked " + b);
		ui.getSettings().setSelected(b.getBody());
	}
	
	public void render(GL gl) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		gl.glTranslated(0, 0, -zoom);
        ThisRot.get(matrix);
        gl.glMultMatrixf(matrix, 0);

		if (!selectMode && showCursor) drawCursor(gl);
		if (showEclPlane) drawEclPlane(gl);
		drawBodies(gl);
	}
	
	private void drawBodies(GL gl) {
		int i = 0;
		for (GfxBody b: bodies) {
			gl.glLoadName(++i); // for selections
			b.render(gl, b == getActiveBody(), planetzoom);
		}
	}
	
	private void drawCursor(GL gl) {
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
	}
	
	private void drawEclPlane(GL gl) {
		gl.glColor3d(.5,.5,.5);
		glu.gluQuadricDrawStyle(qua, GLU.GLU_LINE);
		glu.gluDisk(qua, 0, 800, 36, 1);
		glu.gluQuadricDrawStyle(qua, GLU.GLU_FILL);
	}

	public void runSimulation() {
		if (!paused) {
			int iters = (int)speed;
			double laststep = speed - iters;
			for (int i = 0; i < iters; i++) {
				world.run(world.dt);
			}
			world.run(laststep*world.dt);
		}
		ui.refreshWidgets();
	}
	
	public void pause() {
		paused = true;
	}
	public void cont() {
		paused = false;
	}
	
	public void togglePause() {
		paused = !paused;
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

    	qua = glu.gluNewQuadric();
    	glu.gluQuadricTexture(qua, true);
    	
		for (Body b: world.getBodies()) {
			bodies.add(new GfxBody(b, gl, qua, this));
		}
		
        LastRot.setIdentity();
        ThisRot.setIdentity();
        ThisRot.get(matrix);    
	}
	
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
		glu.gluPerspective(45, width / height, 10, 15000000);
	}
	

	public void originActive() {
		origin = getActiveBody();
	}
	
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
}
