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
import gravitoni.ui.UI;
import demos.common.TextureReader;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;
import javax.swing.event.*;

public class Renderer implements GLEventListener, ActionListener, KeyListener, MouseMotionListener, MouseInputListener, MouseWheelListener {

	private World world;
	private ArrayList<GfxBody> bodies = new ArrayList<GfxBody>();
	private int activeBody = -1;
	private Body origin = null;
	
	private double speed = 1;
	private GLUquadric qua;
	private int texture;
	private GLU glu = new GLU();
	private GL gl;
	
	private Navigator navigator;
	
	private JMenuBar menuBar;
	private JPopupMenu popup;
	
	private UI ui;
	
    private Matrix4f LastRot = new Matrix4f();
    private Matrix4f ThisRot = new Matrix4f();
    private final Object matrixLock = new Object();
    private float[] matrix = new float[16];

    private ArcBall arcBall = new ArcBall(640.0f, 480.0f);
    
    private double zoom = 1500;
    
    private Point pan = new Point(), panStart, panCurrent = new Point();

	
	public Renderer(World world, UI ui, GLCanvas canvas) {
		this.world = world;
		this.ui = ui;
		
		navigator = new Navigator();

		
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
		canvas.addMouseMotionListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseWheelListener(this);
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
		System.out.println("SPD"+speed);
		speed = Math.exp(19 * speed);
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
			System.out.println(pos);
			gl.glTranslated(-10/1e7 * pos.x, -10/1e7 * pos.y, pos.z);
		}
		
		Body active = getActiveBody();
		for (GfxBody b: bodies) {
			b.render(gl, b.getBody() == active);
			/*
			gl.glPushMatrix();
			//if (!b.getName().equals("Moon")) continue;
			gl.glColor3d(1, 1, 1);
			Vec3 pos = b.getPos();
			// System.out.println("Hox! " + b.getName() + "; " + (1/1e7*pos.x) + ", " + (1/1e7*pos.y) + ", " + pos.z + "   " + (1/1e3 * b.getRadius()));
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
			gl.glEnable(GL.GL_TEXTURE_GEN_S);
			gl.glEnable(GL.GL_TEXTURE_GEN_T);
			gl.glTranslated(10/1e7 * pos.x, 10/1e7 * pos.y, -1500f);
			//gl.glTranslated(100, 100, -1000f);
			glu.gluSphere(qua, .01/1e3 * b.getRadius(), 20, 20);
			//glu.gluCylinder(qua, 100, 100, 100, 100, 100);
			//glu.gluSphere(qua, 100, 20, 20);
			gl.glPopMatrix();
			*/
		}
		ui.refreshWidgets();
		//System.out.println("Render!");
		world.run(speed * world.dt);
	}
	
	private void drawCursor(GL gl) {
		gl.glPushMatrix();
		gl.glDisable(GL.GL_TEXTURE_GEN_S);
		gl.glDisable(GL.GL_TEXTURE_GEN_T);
		
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
		texture = genTexture(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        TextureReader.Texture atexture = null;
        try {
            atexture = TextureReader.readTexture("textures/earth.png");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        makeRGBTexture(gl, glu, atexture, GL.GL_TEXTURE_2D, false);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        
        gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
    	gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
		qua = glu.gluNewQuadric();
    	
    	
		for (Body b: world.getBodies()) {
			bodies.add(new GfxBody(b, texture, qua));
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
		
        LastRot.setIdentity();                                // Reset Rotation
        ThisRot.setIdentity();                                // Reset Rotation
        ThisRot.get(matrix);

        
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
		
		gl.setSwapInterval(1);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		glu.gluPerspective(
				45.0f, 
				(double) width / (double) height, 
				0.1f,
				3000.0f);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
        arcBall.setBounds((float) width, (float) height);

	}


	public void keyPressed(KeyEvent e) {
		double amount = 30, ramount = 1;
		switch (e.getKeyCode()) {
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
				
			case KeyEvent.VK_SPACE:
				// navigator.reset();
				origin = getActiveBody();
				break;
				
			case KeyEvent.VK_LESS:
				if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)
					nextActive();
				else
					prevActive();
				break;
			case KeyEvent.VK_GREATER:
				
				break;
				
			default:
				System.out.println("Unknown keypress on canvas: " + e);
		}
		
	}
	public void keyReleased(KeyEvent e) {
	}
	public void keyTyped(KeyEvent e) {
	}

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            startDrag(e.getPoint());
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
        	startPan(e.getPoint());
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            drag(e.getPoint());
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
        	pan(e.getPoint());
        }
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


	public void mouseMoved(MouseEvent e) {
		//System.out.println(e.getX() + " " + e.getY());
	}
	
	public void nextActive() {
		if (++activeBody == bodies.size()) activeBody = -1;
	}
	public void prevActive() {
		if (--activeBody == -2) activeBody = bodies.size() - 1;
	}

	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isMiddleMouseButton(e)) {
			stopPan();
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int rot = e.getWheelRotation();
		zoom += 50 * rot;
	}

}
