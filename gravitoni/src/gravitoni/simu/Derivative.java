package gravitoni.simu;

/** Takes position and velocity derivatives and simplifies the code */
public class Derivative {
	public Vec3 pos, vel;
	
	public Derivative(Vec3 pos, Vec3 vel) {
		this.pos = pos;
		this.vel = vel;
	}
	public Derivative() {
		this.pos = new Vec3();
		this.vel = new Vec3();
	}
}
