package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigVar;

/** A rocket that has a thrust value that exists for a specific amount of time in the beginning. */
public class Rocket extends Body {
	@ConfigVar("thrust")
	protected Vec3 thrust = new Vec3();

	@ConfigVar("thrusttime")
	protected double thrusttime = 0;

	
	public Rocket(Config cfg) {
		super(cfg);
		cfg.getVars().apply(this, Rocket.class);
	}
	
	/** Add thrust to the general acceleration. */
	public Vec3 acceleration(World world, Vec3 location) {
		Vec3 x = super.acceleration(world, location);
		if (world.getTime() <= thrusttime) x.add(thrust);
		return x;
	}
	
	/** Add thrust to the general acceleration. */
	public Vec3 acceleration(World world, Vec3[] locations, int myIdx) {
		Vec3 x = super.acceleration(world, locations[myIdx]);
		if (world.getTime() <= thrusttime) x.add(thrust);
		return x;
	}
}
