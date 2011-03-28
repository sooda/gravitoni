package gravitoni.simu;

import gravitoni.config.ConfigBlock;
import gravitoni.config.ConfigVar;

public class Body {
	@ConfigVar("position")
	private Vec3 pos;
	
	@ConfigVar("velocity")
	private Vec3 vel;
	
	@ConfigVar("name")
	private String name;
	
	@ConfigVar("mass")
	private double mass;
	
	@ConfigVar("radius")
	private double radius;
	
	public Body(ConfigBlock cfg) {
		vel = new Vec3();
		/*
		pos = cfg.getVec("position");
		if (cfg.has("velocity")) {
			vel = cfg.getVec("velocity");
		} else {
			vel = new Vec3();
		}
		name = cfg.get("name");
		mass = cfg.getDouble("mass");
		radius = cfg.getDouble("radius");
		*/
		cfg.apply(this, Body.class);
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
		return name + "\t" + pos + "\t" + vel + "\t";
	}
}
