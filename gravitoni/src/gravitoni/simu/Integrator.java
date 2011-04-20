package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigVar;

import java.util.ArrayList;

/** Integrator interface for abstraction */
public abstract class Integrator {
	/** 0: do nothing, 1: stop, 2: bounce */
	@ConfigVar("collisiontype")
	private int collisionType = 0;

	public Integrator(Config cfg) {
		cfg.getVars().apply(this, Integrator.class);
	}
	
	public abstract boolean run(double dt);
	
	/** Check for collisions, return true if we can continue from this. */
	protected boolean collide(World world) {
		if (collisionType < 1 || collisionType > 2) return true;

		ArrayList<Body> bodies = world.getBodies();

		for (int i = 0; i < bodies.size(); i++) {
			for (int j = i + 1; j < bodies.size(); j++) {
				if (bodies.get(i).collides(bodies.get(j))) {
					if (collisionType == 1) return false;
					doCollision(bodies.get(i), bodies.get(j));
				}
			}
		}
		return true;
	}
	
	/** Handle a two-body collision, perform an elastic bounce */
	private void doCollision(Body a, Body b) {
		double m1 = a.getMass(), m2 = b.mass;
		double coef = 2 / (m1 + m2); // delta v1 = delta p1 / m = 2 * m2 / (m1 + m2) * (v2 - v1)
		Vec3 dv = a.vel.clone().sub(b.vel);
		Vec3 dv1 = dv.clone().neg().mul(m2 * coef);
		Vec3 dv2 = dv.mul(m1 * coef);
		a.vel.add(dv1);
		b.vel.add(dv2);
	}
}
