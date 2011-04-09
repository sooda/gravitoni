package gravitoni.gfx;

import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import gravitoni.simu.Body;
import gravitoni.simu.Vec3;

public class GfxBody {
	private Body body;
	private GLU glu = new GLU();
	private int texture;
	private GLUquadric qua;
	private ArrayList<Vec3> posHistory = new ArrayList<Vec3>();
	private Renderer rendr;
	
	GfxBody(Body original, GL gl, GLUquadric q, Renderer r) {
		body = original;
		loadTexture(gl);
		qua = q;
		rendr=r;
	}

	public void loadTexture(GL gl) {
		texture = genTexture(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        
        TextureReader.Texture teximg = null;
        try {
            teximg = TextureReader.readTexture("textures/" + body.getCfg().get("texture"));
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
	
	private int genTexture(GL gl) {
        final int[] tmp = new int[1];
        gl.glGenTextures(1, tmp, 0);
        return tmp[0];
    }
	
	public Body getBody() {
		return body;
	}
	
	void render(GL gl, boolean selected, double zoom) {
		Vec3 pos = body.getPos();
		posHistory.add(pos.clone());
		// System.out.println("Hox! " + b.getName() + "; " + (1/1e7*pos.x) + ", " + (1/1e7*pos.y) + ", " + pos.z + "   " + (1/1e3 * b.getRadius()));
		double r = .01/1e3 * body.getRadius();
		
		gl.glColor4d(1, 1, 1, 1);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
		if (selected) {
			gl.glDisable(GL.GL_TEXTURE_2D);
			//gl.glEnable(GL.GL_TEXTURE_GEN_S);
			//gl.glEnable(GL.GL_TEXTURE_GEN_T);
		}
		
		gl.glPushMatrix();
		if (rendr.getOrigin() != null) {
			Vec3 opos = rendr.getOrigin().getBody().getPos();
			gl.glTranslated(-10/1e7 * opos.x, -10/1e7 * opos.y, opos.z);
		}
		gl.glTranslated(10/1e7 * pos.x, 10/1e7 * pos.y, 10/1e7 * pos.z);
		gl.glScaled(zoom, zoom, zoom);
		glu.gluSphere(qua, r, 20, 20);
		renderVectors(gl);
		gl.glPopMatrix();
		
		renderHistory(gl);

	}
	
	private void renderHistory(GL gl) {
		gl.glDisable(GL.GL_TEXTURE_2D);
		//gl.glPushMatrix();
		// blend?
		//gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINE_STRIP);
		//System.out.println("----------- " + posHistory.size());
		GfxBody origin = rendr.getOrigin();
		if (origin != null) {
			Vec3 pos = origin.getBody().getPos();
			//gl.glTranslated(10/1e7 * pos.x, 10/1e7 * pos.y, -pos.z);
		}
		int n = posHistory.size();
		for (int i = 0; i < n; i++) {
			Vec3 p = posHistory.get(i);
			GfxBody o = rendr.getOrigin();
			if (o != null) {
				Vec3 op = o.getPosAt(i);
				if (op != null) p = p.clone().sub(op);
			}
			//System.out.println(i + ":" + p);
			gl.glColor4d(1 - (double)i / n, (double)i / n, 0, 1);
			gl.glVertex3d(10/1e7 *p.x, 10/1e7 *p.y, p.z);
		}
		gl.glEnd();
		
		//gl.glPopMatrix();
	}
	public Vec3 getPosAt(int i) {
		return i >= 0 && i < posHistory.size() ? posHistory.get(i) : null;
	}
	
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
