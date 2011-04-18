package gravitoni.gfx;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.j2d.TextRenderer;

import gravitoni.simu.Body;
import gravitoni.simu.Vec3;

/** Handle 3D graphics for a body. Draw the sphere and its name; remember position history and draw it.  */
public class GfxBody {
	/** The physics object */
	private Body body;
	
	/** Helper; we're drawing gluSpheres and gluCylinders. */
	private GLU glu = new GLU();
	
	/** Low-level texture ID */
	private int texture;
	
	/** The quadric object we're drawing on */
	private GLUquadric qua;
	
	/** Last positions */
	private ArrayList<Vec3> posHistory = new ArrayList<Vec3>();
	
	/** We need to ask renderer things about the origin */
	private Renderer rendr;
	
	/** Draws our text */
	private TextRenderer tr;
	
	/** Don't draw in 1:1 scale. */
	private static final double SCALER = 1e-6;
	
	GfxBody(Body original, GL gl, GLUquadric q, Renderer r) {
		body = original;
		loadTexture(gl);
		qua = q;
		rendr = r;
		tr = new TextRenderer(new Font("SansSerif", 0, 30));
	}

	/** Grab our texture based on the config name parameter and setup opengl for it */
	public void loadTexture(GL gl) {
        final int[] tmp = new int[1];
        gl.glGenTextures(1, tmp, 0);
		texture = tmp[0];
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        
        TextureReader.Texture teximg = null;
        try {
            teximg = TextureReader.readTexture("textures/" + body.getCfg().getFirstSection("gfx").getVars().get("texture"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, teximg.getWidth(), 
                teximg.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, teximg.getPixels());
        
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
    	gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
	}
	
	/** Get the simubody object */
	public Body getBody() {
		return body;
	}
	
	/** Draw everyting we need */
	void render(GL gl, boolean selected, double zoom) {
		Vec3 pos = body.getPos();
		double r = .01/1e3 * body.getRadius();
		
		gl.glColor4d(1, 1, 1, 1);
		if (selected) {
			gl.glDisable(GL.GL_TEXTURE_2D);
		} else {
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glBindTexture(GL.GL_TEXTURE_2D, texture);			
		}
		
		gl.glPushMatrix();
		if (rendr.getOrigin() != null) {
			Vec3 opos = rendr.getOrigin().getBody().getPos();
			gl.glTranslated(-SCALER * opos.x, -SCALER * opos.y, opos.z);
		}
		
		gl.glTranslated(SCALER * pos.x, SCALER * pos.y, SCALER * pos.z);
		gl.glScaled(zoom, zoom, zoom);
		
		glu.gluSphere(qua, r, 20, 20);
		renderVectors(gl);
		gl.glPopMatrix();
		
		tr.begin3DRendering();
		Vec3 color = body.getCfg().getFirstSection("gfx").getVars().getVec("color");
		tr.setColor((float)color.x, (float)color.y, (float)color.z, 0.7f);
		tr.draw3D(body.getName(), (float)(SCALER * (pos.x + r)), (float)(SCALER * (pos.y + r)), (float)(SCALER * (pos.z + r)), 300);
		tr.end3DRendering();
		
		renderHistory(gl);
		posHistory.add(pos.clone());

	}
	
	/** Draw the history lines */
	private void renderHistory(GL gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
		// blend?
		gl.glBegin(GL.GL_LINE_STRIP);
		int n = posHistory.size();
		Vec3 color = body.getCfg().getFirstSection("gfx").getVars().getVec("color");
		for (int i = 0; i < n; i++) {
			Vec3 p = posHistory.get(i);
			GfxBody o = rendr.getOrigin();
			if (o != null) {
				Vec3 op = o.getPosAt(i);
				if (op != null) p = p.clone().sub(op);
			}
			//gl.glColor4d(1 - (double)i / n, (double)i / n, 0, 1);
			double ii = (double)i / n, jj = 1 - ii;
			gl.glColor3d(jj * color.x + ii * 0.5, jj * color.y + ii * 0.5, jj * color.z + ii * 0.5);
			gl.glVertex3d(1e-6 *p.x, 1e-6 * p.y, 1e-6 * p.z);
		}
		gl.glEnd();
	}
	
	/** Get the position we were at the given iteration. Needed for drawing history with this as origin */
	public Vec3 getPosAt(int i) {
		return i >= 0 && i < posHistory.size() ? posHistory.get(i) : null;
	}
	
	/** Draw the velocity and acceleration vectors */
	private void renderVectors(GL gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glPushMatrix();
		gl.glColor3d(1, 1.0, 0);
		Vec3 orig = new Vec3(0, 0, 1); // the cylinder goes originally in this direction
		Vec3 dir = body.getVel().clone().unit(); // where's the speed going?
		Vec3 cross = orig.clone().cross(dir); // rotation axis
		double cos = dir.dot(orig); // rotation amount
		double spdamount = body.getVel().len();
		spdamount = Math.log(1+spdamount);
		//spdamount *= 0.05;// / body.getRadius();
		gl.glRotated(180 / Math.PI * Math.acos(cos), cross.x, cross.y, cross.z);
		double r = .01/1e3 * body.getRadius();
		glu.gluCylinder(qua, r - 1, 0, (1 + spdamount) * r, 100, 100);
		gl.glPopMatrix();
	}
	
	public String toString() {
		return "[gfx] " + body;
	}
	
}
