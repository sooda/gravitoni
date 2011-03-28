package gravitoni.gfx;

import gravitoni.simu.Vec3;

import javax.media.opengl.GL;

public class Navigator {
	double x, y, z, rx, ry, rz;
	
	public void apply(GL gl) {
		gl.glTranslated(x, y, z);
		gl.glRotated(rx, 1, 0, 0);
		gl.glRotated(ry, 0, 1, 0);
		gl.glRotated(rz, 0, 0, 1);
		// gl.glGetDoublev(pname, params, params_offset)
	}
	
	public void walk(double x, double y, double z) {
		/*
		double ra = rx * Math.PI / 180;
		double rb = rx * Math.PI / 180;
		double rc = rx * Math.PI / 180;
		this.x += x * Math.cos(rb) * Math.cos(rc);
		this.y += y * Math.cos(rb) * Math.sin(rc);
		this.z += z * Math.sin(rb);
		*/
		double c = Math.PI/180;
		Vec3 fuck = Mat44.rxyz(rx*c, ry*c, rz*c).mul(new Vec3(x, y, z));
		this.x += fuck.x;
		this.y += fuck.y;
		this.z += fuck.z;
	}
	public void rotate(double x, double y, double z) {
		this.rx += x;
		this.ry += y;
		this.rz += z;
	}
	public void reset() {
		x = y = z = 0;
		rx = ry = rz = 0;
	}
}
