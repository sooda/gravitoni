package gravitoni.simu;

import gravitoni.config.ConfigBlock;

import java.io.IOException;

/** Formatter for Gnuplot files */
public class GnuplotWriter extends LogWriter {
	public GnuplotWriter(ConfigBlock cfg) throws IOException {
		super(cfg);
	}
	protected String parse(World world, Body body) {
		return world.getTime() + " " + body.getName() + " " + body.getPos() + " " + body.getVel();
	}
}
