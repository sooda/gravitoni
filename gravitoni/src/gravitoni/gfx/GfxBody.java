package gravitoni.gfx;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.BufferUtil;


import gravitoni.simu.Body;
import gravitoni.simu.Vec3;

public class GfxBody {
	private Body body;
	private GLU glu = new GLU();
	private int texture;
	private GLUquadric qua;
	
	GfxBody(Body original, int t, GLUquadric q) {
		body = original;
		texture = t;
		qua = q;
	}
	
	public Body getBody() {
		return body;
	}
	
	void render(GL gl, boolean selected) {
		gl.glPushMatrix();
		gl.glColor4d(1, 1, 1, 1);
		Vec3 pos = body.getPos();
		// System.out.println("Hox! " + b.getName() + "; " + (1/1e7*pos.x) + ", " + (1/1e7*pos.y) + ", " + pos.z + "   " + (1/1e3 * b.getRadius()));
		gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
		if (!selected) {
		gl.glEnable(GL.GL_TEXTURE_GEN_S);
		gl.glEnable(GL.GL_TEXTURE_GEN_T);
		}
		gl.glTranslated(10/1e7 * pos.x, 10/1e7 * pos.y, 0);
		double r = .01/1e3 * body.getRadius();
		glu.gluSphere(qua, r, 20, 20);
		// if (selected) cube(gl);

		
		gl.glDisable(GL.GL_TEXTURE_GEN_S);
		gl.glDisable(GL.GL_TEXTURE_GEN_T);
		gl.glColor3d(1, 0.5, 0.5);
		gl.glPushMatrix();
		Vec3 orig = new Vec3(0, 0, 1); // the cylinder goes originally in this direction
		Vec3 dir = body.getVel().clone().unit(); // where's the speed going?
		Vec3 cross = orig.clone().cross(dir); // rotation axis
		double cos = dir.dot(orig); // rotation amount
		double spdamount = body.getVel().len();
		// System.out.println(body.getName() + ">>>>>" + spdamount + "      >>>> " + body.getRadius());
		spdamount *= 0.05;// / body.getRadius();
		gl.glRotated(180/3.14159*Math.acos(cos), cross.x, cross.y, cross.z);
		glu.gluCylinder(qua, r, 0, (1 + spdamount) * r, 100, 100);
		gl.glPopMatrix();
		
		gl.glPopMatrix();
	}
	
	private void cube(GL gl) {
		//gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
		
		double[] vertices = {
				0, 0, 0,
				1, 0, 0,
				1, 1, 0,
				0, 1, 0,
				
				0, 0, 1,
				1, 0, 1,
				1, 1, 1,
				0, 1, 1
		};
		for (int i = 0; i < vertices.length; i++) {
			double lol = .01/1e3 * body.getRadius() * 10; 
			vertices[i] *= lol;
			System.out.println(vertices[i]);
		}
		DoubleBuffer verticesbuf = BufferUtil.newDoubleBuffer(vertices.length);
		verticesbuf.put(vertices, 0, vertices.length);
		int[] indices = {
				0, 1, 2, 3,
				4, 5, 6, 7, 
				1, 2, 6, 5,
				0, 3, 7, 4,
				0, 1, 5, 4,
				3, 2, 6, 7};
		IntBuffer indicesbuf = BufferUtil.newIntBuffer(indices.length);
		indicesbuf.put(indices);
		verticesbuf.rewind();
		indicesbuf.rewind();
		gl.glBegin(GL.GL_QUADS);
		for (int i = 0; i < indices.length; i++) {
			gl.glVertex3d(3*vertices[i], 3*vertices[i]+1, 3*vertices[i]+2);
			//System.out.println("vtx");
		}
		gl.glEnd();
		/*
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL.GL_DOUBLE, 0, verticesbuf);
		gl.glDrawElements(GL.GL_QUADS, 24, GL.GL_INT, indicesbuf);
		gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
		//gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
		 */
	}
}
