package gravitoni.simu;

import gravitoni.config.*;

import java.util.ArrayList;

public class World {
	private ArrayList<Body> bodies = new ArrayList<Body>();
	private Integrator integrator = new BadRK4(this);
	private double G = 0;
	public double dt = 0;
	
	public void add(Body body) {
		bodies.add(body);
	}
	
	public ArrayList<Body> getBodies() {
		return bodies;
	}
	
	public void loadConfig(Config cfg) {
		ConfigBlock globals = cfg.getGlobals();
		if (globals.has("G")) G = globals.getDouble("G");
		if (globals.has("dt")) dt = globals.getDouble("dt");

		if (cfg.getBlocks().containsKey("body")) {
			for (ConfigBlock blk: cfg.getBlocks().get("body")) {
				Body b = null;
				try {
					b = new Body(blk);
					bodies.add(b);
				} catch (Exception e) {
					System.out.println("BAD BAD BAD." + e);
				}
			}
		}
	}
	
	public void run(double dt) {
		integrator.run(dt);
	}
	
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
}
