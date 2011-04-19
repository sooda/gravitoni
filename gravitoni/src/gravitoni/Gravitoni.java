package gravitoni;
import java.io.FileNotFoundException;
import java.io.FileReader;

import gravitoni.config.*;
import gravitoni.simu.*;
import gravitoni.ui.*;

/** Starting entry point for the whole program. 
 * 
 * This doesn't actually do anything interesting. Just parses a few cli arguments, reads config and starts world/ui. */
public class Gravitoni {
	private World world = new World();

	/** Parse command line arguments and start cli/gui program. */
	public static void main(String[] args) {
		try {
			if (args.length == 0) new Gravitoni().run();
			else if (args.length == 1) new Gravitoni(args[0]).run();
			else new Gravitoni(args[0]).cli(args[1]);
		} catch (FileNotFoundException e) {
			System.out.println("404 tms " + e);
		}
	}
	
	/** For GUI. No configs yet. */
	public Gravitoni() {
	}
	
	/** New program with a configuration file. */
	public Gravitoni(String configFile) throws FileNotFoundException {
		FileReader rdr = new FileReader(configFile);
		loadConfig(new Config("main", rdr));
	}
	
	/** Feed the config to world loader. */
	public void loadConfig(Config cfg) {
		System.out.println("Can haz config: >>>>>>>>>>\n" + cfg + "<<<<<<<<<<\n");
		world.loadConfig(cfg);
	}
	
	/** Run the GUI. */
	public void run() {
		new UI(world);
	}
	
	/** Run the command-line interface */
	public void cli(String arg) {
		run(Integer.parseInt(arg));
		world.stop();
	}
	
	/** Run for a specific amount of time. */
	public void run(double seconds) {
		double dt = world.dt;
		for (int i = 0; i * dt < seconds; i++) {
			world.run(dt);
		}
	}
}
