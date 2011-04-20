package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigVar;

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

/** Fourth-order runge-kutta */
class RK4 extends Integrator {
	private World world;
	
	public RK4(World world, Config cfg) {
		super(cfg);
		this.world = world;
	}
	
	/**
	 * Run the integrator, calculate one step forwards.
	 * @return true, if the simulation can continue on from this moment
	 */
	public boolean run(double dt) {
		State[] newStates = runAll(dt);

		ArrayList<Body> bodies = world.getBodies();

		for (int i = 0; i < bodies.size(); i++)
			bodies.get(i).setState(newStates[i]);
		
		return collide(world);
	}
	
	/** Calculate new states for all of the bodies */
	private State[] runAll(double dt) {
		Derivative[] k1 = evalAll(0, null);
		Derivative[] k2 = evalAll(0.5 * dt, k1);
		Derivative[] k3 = evalAll(0.5 * dt, k2);
		Derivative[] k4 = evalAll(dt, k3);
		int n = k1.length;
		State[] ret = new State[n];
		
		ArrayList<Body> bodies = world.getBodies();
		for (int i = 0; i < bodies.size(); i++) {
			// 1/6 * h * (k1 + 2 k2 + 2 k3 + k4)
			k2[i].dpos.add(k3[i].dpos).mul(2); // tmp = 2 k2 + 2 k3
			k1[i].dpos.add(k2[i].dpos).add(k4[i].dpos).mul(1.0 / 6.0 * dt); // ret = 1/6 * h * (k1 + foo + k4)
			
			k2[i].dvel.add(k3[i].dvel).mul(2);
			k1[i].dvel.add(k2[i].dvel).add(k4[i].dvel).mul(1.0 / 6.0 * dt);
			
			Vec3 pos = bodies.get(i).getPos().clone(), vel = bodies.get(i).getVel().clone();
			ret[i] = new State(pos.add(k1[i].dpos), vel.add(k1[i].dvel));
		}
		return ret;
	}
	
	/** Evaluate all bodies' derivatives after dt, given their derivatives now. */
	private Derivative[] evalAll(double dt, Derivative[] diff) {
		ArrayList<Body> bodies = world.getBodies();
		int n = world.getBodies().size();
		
		Derivative[] ret = new Derivative[n];
		Vec3[] newPos = new Vec3[n]; // move the whole world.
		
		// collect X'_n+1 and X_n+1 for later on
		for (int i = 0; i < n; i++) {
			Vec3 dpos = bodies.get(i).getVel().clone();
			if (diff != null) dpos.add(diff[i].dvel.clone().mul(dt)); // X'_n+1 = V + dt * V' = V + dV/dt*dt
			
			newPos[i] = bodies.get(i).getPos().clone();
			if (diff != null) newPos[i].add(diff[i].dpos.clone().mul(dt)); // X_n+1 = X + dt * X'
			ret[i] = new Derivative(dpos, null); // just X'
		}
		
		// run the accelerator to get speed derivatives
		for (int i = 0; i < n; i++) {
			Vec3 dvel = bodies.get(i).acceleration(world, newPos, i); // V'_n+1 = A(X_n+1)
			ret[i].dvel = dvel;
		}
		
		return ret;
	}
}
