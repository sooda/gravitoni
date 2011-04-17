package gravitoni;
import java.io.FileNotFoundException;
import java.io.FileReader;

import gravitoni.config.*;
import gravitoni.simu.*;
import gravitoni.ui.*;

import java.util.ArrayList;

public class Gravitoni {
	private World world = new World();

	public static void main(String[] args) {
		try {
			if (args.length == 0) new Gravitoni().run();
			else if (args.length == 1) new Gravitoni(args[0]).run();
			else new Gravitoni(args[0]).run(Integer.parseInt(args[1]));
		} catch (FileNotFoundException e) {
			System.out.println("404 tms " + e);
		}
	}
	
	public Gravitoni() {
	}
	
	public Gravitoni(String configFile) throws FileNotFoundException {
		FileReader rdr = new FileReader(configFile);
		loadConfig(new Config(rdr));
	}
	
	public void loadConfig(Config cfg) {
		System.out.println("Can haz config: >>>>>>>>>>\n" + cfg + "<<<<<<<<<<\n");
		world.loadConfig(cfg);
	}
	
	public void run() {
		new UI(world);
	}
	
	public void run(double steps) {
		double dt = world.dt;
		for (int i = 0; i < steps; i++) {
			world.run(dt);
		}
	}
}
