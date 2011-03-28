package gravitoni.simu;

import gravitoni.config.ConfigBlock;

import java.io.IOException;

public class GnuplotWriter extends LogWriter {
	public GnuplotWriter(ConfigBlock cfg) throws IOException {
		super(cfg);
	}
	public void write(World w, Body body, int iteration) {
		if (filter != null && !filter.equals(body.getName()))
			return;
		
		if ((iteration % tick) == 0) {
			outStream.println(w.getTime() + " " + body.getName() + " " + body.getPos() + " " + body.getVel());
			outStream.flush(); // TODO fix close() and other cleanup stuff
		}
	}
}
