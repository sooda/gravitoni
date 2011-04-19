package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigBlock;
import gravitoni.config.ConfigVar;

/** A single body floating around in the universe. */
public class Body {
	@ConfigVar("position")
	protected Vec3 pos = new Vec3();
	
	@ConfigVar("velocity")
	protected Vec3 vel = new Vec3();
	
	@ConfigVar(value="name", mandatory=true)
	protected String name;
	
	@ConfigVar("mass")
	protected double mass;
	
	@ConfigVar(value="radius", mandatory=true)
	protected double radius;
	
	protected Config cfg;
	
	/** Construct a new body from the given configuration, but don't take the kepler parameters into account yet */
	public Body(Config cfg) {
		ConfigBlock vars = cfg.getVars();
		vars.apply(this, Body.class);
		if (vars.has("origin") && vars.get("origin").startsWith("pos:"))
			pos.add(Vec3.parse(vars.get("origin").substring("pos:".length())));
		if (vars.has("vorigin") && vars.get("vorigin").startsWith("pos:"))
			vel.add(Vec3.parse(vars.get("vorigin").substring("pos:".length())));
		this.cfg = cfg;
	}
	
	/** Initialization pass 2: if this has kepler coordinates, apply them */
	public void init(World world) {
		ConfigBlock vars = cfg.getVars();
		if (cfg.hasSections("kepler")) {
			Kepler elements = new Kepler(cfg.getFirstSection("kepler").getVars());
			elements.transform(pos, vel, world.getBody(elements.center));
			if (vars.has("origin") && vars.get("origin").startsWith("pos:"))
				pos.add(Vec3.parse(vars.get("origin").substring("pos:".length())));
			if (vars.has("vorigin") && vars.get("vorigin").startsWith("pos:"))
				vel.add(Vec3.parse(vars.get("vorigin").substring("pos:".length())));			
		}
		if (vars.has("origin") && vars.get("origin").startsWith("body:"))
			pos.add(world.getBody(vars.get("origin").substring("body:".length())).getPos());
		if (vars.has("vorigin") && vars.get("vorigin").startsWith("body:"))
			vel.add(world.getBody(vars.get("vorigin").substring("body:".length())).getVel());
		System.out.println("init'd: " + this);
	}
	
	/** For transforming from kepler to cartesian coordinates */
	class Kepler {
		@ConfigVar("a")
		private double a; // semimajor
		
		@ConfigVar("e")
		private double e; // eccentricity;
		
		@ConfigVar("i")
		private double i; // inclination;
		
		@ConfigVar("w")
		private double w; // argperiapsis;
		
		@ConfigVar("ma")
		private double ma; // meananomaly;
		
		@ConfigVar("O")
		private double o; // longasc;
		
		@ConfigVar("center")
		private String center;
	
		public Kepler(ConfigBlock vars) {
			vars.apply(this, Kepler.class);
			i = d2r(i);
			w = d2r(w);
			ma = d2r(ma);
			o = d2r(o);
		}
		
		private double d2r(double deg) {
			return deg / 180 * Math.PI;
		}
		
		/** Put the cartesian coordinates in pos and vel. */
		public void transform(Vec3 pos, Vec3 vel, Body centerBody) {
			double M = centerBody.getMass();
			// standard gravitational parameter µ
			//double mu = World.G * mass;

			double E = solve_E(ma, e);
			double f = 2 * Math.atan(Math.sqrt((1 + e) / (1 - e)) * Math.tan(E / 2)); // true anomaly, v

			// radius
			double r = a * (1 - e * e) / (1 + e * Math.cos(f));

			// the specific angular momentum (usually h)
			double muu = World.G * (M + mass);
			double l = Math.sqrt(muu * a * (1 - e * e));

			// Find the X-Y-Z cosines for r vector
			double al = w + f; // Arg of Latitude

			double x = Math.cos(o) * Math.cos(al) - Math.sin(o) * Math.sin(al) * Math.cos(i);
			double y = Math.sin(o) * Math.cos(al) + Math.cos(o) * Math.sin(al) * Math.cos(i);
			double z = Math.sin(i) * Math.sin(al);

			// position
			Vec3 r_v = new Vec3(r * x, r * y, r * z);

			// Orbit parameter p (Not period)
			double p = a * (1 - e * e);
			
			Vec3 d_v = new Vec3(
					Math.cos(o) * Math.sin(al) + Math.sin(o) * Math.cos(al) * Math.cos(i),
					Math.sin(o) * Math.sin(al) - Math.cos(o) * Math.cos(al) * Math.cos(i),
					-Math.sin(i) * Math.cos(al)
			);

			// velocity
			Vec3 v_v = r_v.clone().mul((l * e) / (r * p) * Math.sin(f)).sub(d_v.clone().mul(l / r));
			pos.set(r_v);
			vel.set(v_v);
		}
		
		private double solve_E(double ma, double e) {
			double eps = 1e-15;
			double E = ma;
			double err=2*eps;
			do {
				E = e * Math.sin(E) + ma;
				err = ma - (E - e * Math.sin(E));
			} while (Math.abs(err) >= eps);
			return E;
		}
	}
	
	/** Calculate acceleration influence of the given body to this body at the given position */
	protected Vec3 singleAccel(Body b, Vec3 bPos, Vec3 myPos) {
		// F = GmM / r² = ma -> a = G M / r² = G M * r_u / |r²|
		Vec3 v = bPos.clone().sub(myPos); // difference vector r
		double dist = v.len(); // absolute distance |r|
		v.unit().mul(World.G * b.getMass() / (dist * dist)); // r_u * G M / |r²| 
		return v;
	}
	
	/** Calculate my acceleration in the given universe at the given position */
	public Vec3 acceleration(World world, Vec3 location) {
		Vec3 total = new Vec3();
		for (Body b: world.getBodies()) {
			if (b == this) continue;
			total.add(singleAccel(b, b.getPos(), location));
		}
		//System.out.println("Acceleration:" + body.getName() + ":" + total);
		return total;
	}
	
	/** For integrator microsteps: given the current locations of all bodies... */
	public Vec3 acceleration(World world, Vec3[] locations, int myIdx) {
		Vec3 total = new Vec3();
		int i = 0;
		for (Body b: world.getBodies()) {
			if (b == this) {
				i++;
				continue;
			}
			total.add(singleAccel(b, locations[i], locations[myIdx]));
			i++;
		}
		//System.out.println("Acceleration:" + body.getName() + ":" + total);
		return total;
	}
	
	public Vec3 acceleration(World world) {
		return acceleration(world, pos);
	}
	
	public Config getCfg() {
		return cfg;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public Vec3 getPos() {
		return pos;
	}
	
	public Vec3 getVel() {
		return vel;
	}
	
	public String getName() {
		return name;
	}
	
	public double getMass() {
		return mass;
	}
	
	public void setState(Vec3 pos, Vec3 vel) {
		this.pos.set(pos);
		this.vel.set(vel);
	}
	
	public void setState(State state) {
		setState(state.pos, state.vel);
	}
	
	public String toString() {
		return name + "\t" + mass + "\t" + pos + "\t" + vel + "\t";
	}
}
