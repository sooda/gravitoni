package gravitoni.simu;

/** Takes position and velocity and simplifies the code */
public class State {
	public Vec3 pos, vel;
	
	public State(Vec3 pos, Vec3 vel) {
		this.pos = pos;
		this.vel = vel;
	}
}

