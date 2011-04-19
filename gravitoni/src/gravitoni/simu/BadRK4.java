package gravitoni.simu;

import java.util.ArrayList;

/*
RK4:
y' = f(t, y)
y_n+1 = y_n + 1/6*h(k1 + 2 k2 + 2 k3 + k4)
t_n+1 = t_n + h

k_1 = f(t, y)
k_2 = f(t + 0.5 h, y + 0.5 h k1)
k_3 = f(t + 0.5 h, y + 0.5 h k2)
k_4 = f(t + h, y + h k3)

f = ma, a = f / m
*/

/** This assumes that the surrounding world is stationary between timesteps:
 * Faster to implement, but has more error, will do better afterwards. 
 */
class BadRK4 implements Integrator {
	private World world;
	
	public BadRK4(World world) {
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
	private State runOne(Body body, double dt) {
		Derivative k1 = eval(body, 0, null);
		Derivative k2 = eval(body, 0.5 * dt, k1);
		Derivative k3 = eval(body, 0.5 * dt, k2);
		Derivative k4 = eval(body, dt, k3);

		// 1/6 * h * (k1 + 2 k2 + 2 k3 + k4)
		k2.pos.add(k3.pos).mul(2);
		k1.pos.add(k2.pos).add(k4.pos).mul(1.0 / 6.0 * dt);
		
		k2.vel.add(k3.vel).mul(2);
		k1.vel.add(k2.vel).add(k4.vel).mul(1.0 / 6.0 * dt);
		Vec3 pos = body.getPos().clone(), vel = body.getVel().clone();
		return new State(pos.add(k1.pos), vel.add(k1.vel));
	}
	
	/**
	 * Evaluate the state difference at now + dt. y' = f(y, t).
	 * Returns a derivative for pos and vel (i.e. vel, acc).
	 * BAD: This assumes that the world is stationary until next timestep arrives. 
	 */
	public Derivative eval(Body body, double dt, Derivative diff) {
		Vec3 vel = body.getVel().clone();
		if (diff != null) vel.add(diff.vel.clone().mul(dt)); // v += dv/dt * dt
		Vec3 newPos = body.getPos().clone();
		if (diff != null) newPos.add(diff.pos.clone().mul(dt)); // acc(t=dt, x=x0+dt*v)
		// newPos.add(body.getVel().clone().mul(dt));
		Derivative ret = new Derivative(vel, body.acceleration(world, newPos));
		// Derivative ret = new Derivative(vel, world.acceleration(body, newPos)); 
		return ret;
	}
}
