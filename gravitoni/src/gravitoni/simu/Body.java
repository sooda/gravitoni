package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigBlock;
import gravitoni.config.ConfigVar;

public class Body {
	@ConfigVar("position")
	private Vec3 pos = new Vec3();
	
	@ConfigVar("velocity")
	private Vec3 vel = new Vec3();
	
	@ConfigVar(value="name", mandatory=true)
	private String name;
	
	@ConfigVar("mass")
	private double mass;
	
	@ConfigVar(value="radius", mandatory=true)
	private double radius;
	
	private Config cfg;
	
	public Body(Config cfg) {
		ConfigBlock vars = cfg.getVars();
		vars.apply(this, Body.class);
		if (vars.has("origin"))
			pos.add(vars.getVec("origin"));
		if (vars.has("vorigin"))
			vel.add(vars.getVec("vorigin"));
		this.cfg = cfg;
	}
	
	public void init(World world) {
		if (cfg.hasSections("orb")) {
			Kepler elements = new Kepler(cfg.getFirstSection("orb").getVars());
			elements.transform(pos, vel, world.getBody(elements.center));
			ConfigBlock vars = cfg.getVars();
			if (vars.has("origin"))
				pos.add(vars.getVec("origin"));
			if (vars.has("vorigin"))
				vel.add(vars.getVec("vorigin"));
		}
		System.out.println("init'd: " + this);
	}
	
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
				//System.out.println(err + " ");
			} while (Math.abs(err) >= eps);
			return E;
		}
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
		//System.out.println("Set state!!!!!1111111111111" + pos + ";" + vel);
	}
	
	public void setState(State state) {
		setState(state.pos, state.vel);
	}
	
	public String toString() {
		return name + "\t" + mass + "\t" + pos + "\t" + vel + "\t";
	}
}
