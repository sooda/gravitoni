package gravitoni.simu;

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
	
	private ConfigBlock cfg;
	
	public Body(ConfigBlock cfg) {
		vel = new Vec3();
		cfg.apply(this, Body.class);
		if (cfg.has("origin"))
			pos.add(cfg.getVec("origin"));
		if (cfg.has("vorigin"))
			vel.add(cfg.getVec("vorigin"));
		this.cfg = cfg;
		System.out.println(this);
	}
	
	public ConfigBlock getCfg() {
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
