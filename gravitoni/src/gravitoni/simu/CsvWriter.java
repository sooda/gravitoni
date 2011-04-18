package gravitoni.simu;

import gravitoni.config.ConfigBlock;

import java.io.IOException;

/** Formatter for CSV files */
public class CsvWriter extends LogWriter {
	public CsvWriter(ConfigBlock cfg) throws IOException {
		super(cfg);
	}
	protected String parse(World world, Body body) {
		return world.getTime() + "," + body.getName() + "," + body.getPos() + "," + body.getVel();
	}
}
