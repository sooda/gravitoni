package gravitoni.simu;

import java.util.ArrayList;


/** Velocity verlet integration */
class Verlet implements Integrator {
	private World world;
	
	public Verlet(World world) {
		this.world = world;
	}
	
	/**
	 * Run the integrator, calculate one step forwards.
	 */
	public void run(double dt) {
		ArrayList<Body> bodies = world.getBodies();
		State[] newStates = new State[bodies.size()];

		for (int i = 0; i < bodies.size(); i++)
			newStates[i] = runOne(bodies.get(i), dt);
		
		for (int i = 0; i < bodies.size(); i++) {
			//System.out.println("New state for " + bodies.get(i).getName() + ":" + newStates[i].pos + ";" + newStates[i].vel);
			bodies.get(i).setState(newStates[i]);
		}
	}
	
	/**
	 * Calculate new state for one body.
	 */
	// TODO: vectorize :(
	private State runOne(Body body, double dt) {
		// x(t+dt) = x(t) + v(t)dt + 0.5a(t)dt^2
		// a(t+dt) = accfunc(x(t+dt))
		// v(t+dt) = v(t) + 0.5(a(t) + a(t+dt))dt
		Vec3 xt = body.getPos();
		Vec3 vt = body.getVel();
		Vec3 at = body.acceleration(world);

		Vec3 xnew = xt.clone().add(vt.clone().mul(dt)) .add(at.clone().mul(0.5 * dt * dt));
		Vec3 anew = body.acceleration(world, xnew);
		Vec3 vnew = vt.clone().add(at.add(anew).mul(0.5 * dt));

		return new State(xnew, vnew);
	}
}
