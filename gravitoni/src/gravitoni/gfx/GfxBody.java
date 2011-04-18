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

public class GfxBody {
	private Body body;
	private GLU glu = new GLU();
	private int texture;
	private GLUquadric qua;
	private ArrayList<Vec3> posHistory = new ArrayList<Vec3>();
	private Renderer rendr;
	private TextRenderer tr;
	
	GfxBody(Body original, GL gl, GLUquadric q, Renderer r) {
		body = original;
		loadTexture(gl);
		qua = q;
		rendr=r;
		tr = new TextRenderer(new Font("SansSerif", 0, 30));
	}

	public void loadTexture(GL gl) {
		texture = genTexture(gl);
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
			gl.glTranslated(-1e-6 * opos.x, -1e-6 * opos.y, opos.z);
		}
		gl.glTranslated(1e-6 * pos.x, 1e-6 * pos.y, 1e-6 * pos.z);
		gl.glScaled(zoom, zoom, zoom);
		glu.gluSphere(qua, r, 20, 20);
		renderVectors(gl);
		gl.glPopMatrix();
		tr.begin3DRendering(); // 800, 600);
		Vec3 color = body.getCfg().getFirstSection("gfx").getVars().getVec("color");
		tr.setColor((float)color.x, (float)color.y, (float)color.z, 0.7f);
		tr.draw3D(body.getName(), 1e-6f * (float)(pos.x + r), 1e-6f * (float)(pos.y + r), 1e-6f * (float)(pos.z + r), 300);
		tr.end3DRendering();
		
		renderHistory(gl);
		posHistory.add(pos.clone());

	}
	
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
