package gravitoni.simu;

/** Takes position and velocity derivatives and simplifies the code */
public class Derivative {
	public Vec3 dpos, dvel;
	
	public Derivative(Vec3 dpos, Vec3 dvel) {
		this.dpos = dpos;
		this.dvel = dvel;
	}
	public Derivative() {
		this.dpos = new Vec3();
		this.dvel = new Vec3();
	}
}
