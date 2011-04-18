package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigBlock;

import java.io.IOException;
import java.util.ArrayList;

/** World logger that contains all logwriters and handles them */
public class Logger {
	private World world;
	private ArrayList<LogWriter> writers = new ArrayList<LogWriter>();
	/** For skipping some writers */
	private int iteration = 0;
	
	public Logger(World world) {
		this.world = world;
	}
	
	/** Setup this from configuration */
	public void loadConfig(Config cfg) {
		Config defaults = cfg.getFirstSection("log.defaults");
		for (Config blk: cfg.getSubsections().get("log")) {
			if (defaults != null) {
				Config orig = defaults.clone();
				orig.merge(blk);
				blk = orig;
			}
			add(blk.getVars());
		}
	}
	
	/** Get a writer instance based on a type string */
	private LogWriter getWriter(String type, ConfigBlock cfg) {
		try {
			if (type.equals("gnuplot")) return new GnuplotWriter(cfg);
			if (type.equals("csv")) return new CsvWriter(cfg);
		} catch (IOException e) {System.out.println(e);}
		System.out.println("Warning: couldn't get writer for " + cfg.get("file") + " (" + type + ")");
		return null;
	}
	
	/** Add a writer that is specified by the given block */
	private void add(ConfigBlock cfg) {
		String type = cfg.get("type");
		LogWriter writer = getWriter(type, cfg);
		if (writer == null) return;
		writers.add(writer);
	}
	
	/** Log the world state */
	public void log() {
		for (Body body : world.getBodies()) {
			for (LogWriter writer : writers) {
				writer.write(world, body, iteration);
			}
		}
		iteration++;
	}
	
	/** Close the writer streams */
	public void close() {
		for (LogWriter writer : writers) {
			writer.close(); // TODO: put this to work
		}	
	}
}
