package gravitoni.simu;

import gravitoni.config.Config;
import gravitoni.config.ConfigBlock;

import java.io.IOException;
import java.util.ArrayList;

public class Logger {
	private World world;
	private ArrayList<LogWriter> writers = new ArrayList<LogWriter>();
	private int iteration = 0;
	
	public Logger(World world) {
		this.world = world;
	}
	
	public LogWriter getWriter(String type, ConfigBlock cfg) {
		try {
			if (type.equals("gnuplot")) return new GnuplotWriter(cfg);
		} catch (IOException e) {System.out.println(e);}
		System.out.println("Warning: couldn't get writer for " + cfg.get("file") + " (" + type + ")");
		return null;
	}
	
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
	
	public void add(LogWriter writer) {
		writers.add(writer);
	}
	
	public void add(ConfigBlock cfg) {
		String type = cfg.get("type");
		LogWriter writer = getWriter(type, cfg);
		if (writer == null) return;
		add(writer);
	}
	
	public void log() {
		for (Body body : world.getBodies()) {
			for (LogWriter writer : writers) {
				writer.write(world, body, iteration);
			}
		}
		iteration++;
	}
	
	public void close() {
		for (LogWriter writer : writers) {
			writer.close();
		}	
	}
}
