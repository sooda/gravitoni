package gravitoni.simu;

import gravitoni.config.*;
import java.util.ArrayList;

/** The whole state of the universe */
public class World {
	private ArrayList<Body> bodies = new ArrayList<Body>();
	private Integrator integrator = new BadRK4(this);
	
	//@ConfigVar("G") -- it's sane to have at least this here...
	public static final double G = 6.67e-11;
	
	/** Timestep from the configuration file */
	@ConfigVar("dt")
	public double dt = 0;
	
	/** Current time */
	private double time = 0;
	
	private Logger logger = new Logger(this);
	
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
	}
	
	/** Run the world 'dt' seconds forward */
	public void run(double dt) {
		integrator.run(dt);
		time += dt;
		logger.log();
	}
	
	/** Calculate acceleration for the given body at the given position at current time (TODO: at time N: move all in the integrator) */
	public Vec3 acceleration(Body body, Vec3 bodyPos) {
		Vec3 total = new Vec3();
		for (Body b: bodies) {
			if (b == body) continue;
			// F = GmM / r² = ma -> a = G M / r² = G M * r_u / |r²|
			Vec3 v = b.getPos().clone().sub(bodyPos);
			double dist = v.len();
			v.unit().mul(G * b.getMass() / (dist * dist));
			total.add(v);
		}
		//System.out.println("Acceleration:" + body.getName() + ":" + total);
		return total;
	}

	/** Calculate acceleration for the given body at its current position at current time */
	public Vec3 acceleration(Body body) {
		return acceleration(body, body.getPos());
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
	
	/** Tell the logger to close the streams. */
	public void stop() {
		logger.close();
	}
}
