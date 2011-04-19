package gravitoni.simu;

import gravitoni.config.*;

import java.io.*;

/** This writes the world state to one file */
public abstract class LogWriter {
	protected PrintWriter outStream;
	
	/** How often to log */
	@ConfigVar("tick")
	protected int tick = 1;
	
	/** Which body to log (null for all of them) */
	@ConfigVar("filter")
	protected String filter = null;

	public LogWriter(ConfigBlock cfg) throws IOException {
		// TODO: implement better option error handling so we don't nullpointerexcept if can't get some params
		outStream = new PrintWriter(new BufferedWriter(new FileWriter(cfg.get("file"))));
		cfg.apply(this, LogWriter.class);
	}
	
	public void close() {
		outStream.close();
	}
	
	/** Write the state once */
	public void write(World w, Body body, int iteration) {
		if (filter != null && !filter.equals(body.getName()))
			return;
		
		if ((iteration % tick) == 0) {
			outStream.println(parse(w, body));
			// outStream.flush(); // TODO: make sure that close() works correctly
		}
	}
	
	/** Parse the world and current body and return one line to be written */
	protected abstract String parse(World world, Body b);
}
