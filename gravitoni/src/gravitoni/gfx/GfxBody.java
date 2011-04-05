package gravitoni.gfx;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.BufferUtil;

import demos.common.TextureReader;


import gravitoni.simu.Body;
import gravitoni.simu.Vec3;

public class GfxBody {
	private Body body;
	private GLU glu = new GLU();
	private int texture;
	private GLUquadric qua;
	
	GfxBody(Body original, GL gl, GLUquadric q) {
		body = original;
		loadTexture(gl);
		qua = q;
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
	
	void render(GL gl, boolean selected) {
		gl.glPushMatrix();
		
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
		
		gl.glTranslated(10/1e7 * pos.x, 10/1e7 * pos.y, 0);
		glu.gluSphere(qua, r, 20, 20);
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glPushMatrix();
		gl.glColor3d(1, 1.0, 0);
		Vec3 orig = new Vec3(0, 0, 1); // the cylinder goes originally in this direction
		Vec3 dir = body.getVel().clone().unit(); // where's the speed going?
		Vec3 cross = orig.clone().cross(dir); // rotation axis
		double cos = dir.dot(orig); // rotation amount
		double spdamount = body.getVel().len();
		spdamount *= 0.05;// / body.getRadius();
		gl.glRotated(180 / Math.PI * Math.acos(cos), cross.x, cross.y, cross.z);
		glu.gluCylinder(qua, r - 1, 0, (1 + spdamount) * r, 100, 100);
		gl.glPopMatrix();
		
		gl.glPopMatrix();
	}
	
}
