package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigVar;

public class Rocket extends Body {
	@ConfigVar("thrust")
	protected Vec3 thrust = new Vec3();

	@ConfigVar("thrusttime")
	protected double thrusttime = 0;

	
	public Rocket(Config cfg) {
		super(cfg);
		cfg.getVars().apply(this, Rocket.class);
	}
	
	public Vec3 acceleration(World world, Vec3 location) {
		Vec3 x = super.acceleration(world, location);
		if (world.getTime() <= thrusttime) x.add(thrust);
		return x;
	}
}
