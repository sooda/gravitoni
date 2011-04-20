package gravitoni.simu;

import gravitoni.config.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/** The whole state of the universe */
public class World {
	private ArrayList<Body> bodies = new ArrayList<Body>();
	/** The integrator we use now. Can be changed to BadRK4 or Verlet straight away by replacing it here */
	private Integrator integrator = new RK4(this);
	
	/** 0: do nothing, 1: stop, 2: bounce */
	@ConfigVar("collisiontype")
	private int collisionType = 0;
	
	//@ConfigVar("G") -- don't load from config.. it's just sane to have at least this here
	public static final double G = 6.67e-11;
	
	/** Timestep from the configuration file */
	@ConfigVar("dt")
	public double dt = 1; // sane defaults so we simulate at least something
	
	/** Current time */
	private double time = 0;
	
	private Logger logger = new Logger(this);
	private Config config;
	
	public void add(Body body) {
		bodies.add(body);
	}
	
	public ArrayList<Body> getBodies() {
		return bodies;
	}
	
	public double getTime() {
		return time;
	}

	/** Load basic options and the bodies from the cfg section. */
	public void loadConfig(Config cfg) {
		ConfigBlock globals = cfg.getVars();
		globals.apply(this, World.class);

		if (cfg.hasSections("body")) {
			for (Config blk: cfg.getSubsections("body")) {
				Body b = null;
				try {
					if (blk.getVars().has("rocket"))
						b = new Rocket(blk);
					else
						b = new Body(blk);
					bodies.add(b);
				} catch (Exception e) {
					System.out.println("BAD BAD BAD body." + e);
					e.printStackTrace();
				}
			}
			for (Body b: bodies) b.init(this);
		}
		
		logger.loadConfig(cfg);
		config = cfg;
	}
	
	/** Run the world 'dt' seconds forward. Return true, if we can continue (no bodies have collided) */
	public boolean run(double dt) {
		integrator.run(dt);
		time += dt;
		logger.log();
		return collide();
	}
	/** Check for collisions, return true if we can continue from this. */
	protected boolean collide() {
		if (collisionType < 1 || collisionType > 2) return true;

		for (int i = 0; i < bodies.size(); i++) {
			for (int j = i + 1; j < bodies.size(); j++) {
				if (bodies.get(i).collides(bodies.get(j))) {
					System.out.println("Collision! a=" + bodies.get(i) + " b=" + bodies.get(j));
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


	public String toString() {
		String s = "";
		if (bodies.size() == 0) return s;
		for (Body body: bodies) {
			s += "\n" + body.toString();
		}
		return s.substring(1);
	}

	/** Find a body by its name */
	public Body getBody(String name) {
		for (Body b: bodies)
			if (b.getName().equals(name)) return b;
		return null;
	}
	
	/** Tell the logger to close the streams, and write the world state to a final logfile. */
	public void stop() {
		logger.close();
		try {
		config.write(new FileOutputStream(new File("gravitoni-endstate.log")));
		} catch (IOException e) {
			System.out.println("IO exception :(");
			e.printStackTrace();
		}
	}
}
