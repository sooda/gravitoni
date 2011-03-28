package gravitoni.simu;

import gravitoni.config.*;

import java.io.*;

public abstract class LogWriter {
	protected PrintWriter outStream;
	
	@ConfigVar("tick")
	protected int tick = 1;
	@ConfigVar("filter")
	protected String filter = null;

	public LogWriter(ConfigBlock cfg) throws IOException {
		outStream = new PrintWriter(new BufferedWriter(new FileWriter(cfg.get("file")))); // TODO implement default/mandatory option handling so we don't nullpointerexcept.
		cfg.apply(this, LogWriter.class);
	}
	public void close() {
		outStream.close();
	}
	public abstract void write(World w, Body b, int iteration);
}
