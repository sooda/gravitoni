package gravitoni.simu;

import gravitoni.config.ConfigBlock;

public class Body {
	private Vec3 pos, vel;
	private String name;
	private double mass;
	private double radius;
	
	public Body(ConfigBlock cfg) {
		pos = cfg.getVec("position");
		if (cfg.has("velocity")) {
			vel = cfg.getVec("velocity");
		} else {
			vel = new Vec3();
		}
		name = cfg.get("name");
		mass = cfg.getDouble("mass");
		radius = cfg.getDouble("radius");
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
