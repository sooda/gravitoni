package gravitoni;
import java.io.FileNotFoundException;
import java.io.FileReader;

import gravitoni.config.*;
import gravitoni.simu.*;
import gravitoni.ui.UI;

import java.util.ArrayList;

public class Gravitoni {
	private World world = new World();
	public static void main(String[] args) {
		try {
			if (args.length == 0) new Gravitoni().run();
			else if (args.length == 1) new Gravitoni(args[0]).run();
			else new Gravitoni(args[0]).run(Integer.parseInt(args[1]));
		} catch (FileNotFoundException e) {
			System.out.println("404 " + e);
		}
	}
	
	public Gravitoni() {
		
	}
	
	public Gravitoni(String configFile) throws FileNotFoundException {
		FileReader rdr;
		rdr = new FileReader(configFile);
		loadConfig(new Config(rdr));
	}
	
	public void loadConfig(Config cfg) {
		System.out.println("Loading configuration: " + cfg);
		world.loadConfig(cfg);
	}
	
	public void run() {
		new UI(world);
	}
	
	public void run(double steps) {
		System.out.println("# step\ttime\tname\tx.x\tx.y\tx.z\tv.x\tv.y\tv.z");
		double dt = world.dt;
		for (int i = 0; i < steps; i++) {
			if (i % 10 == 0) {
				String state = world.toString();
				String[] lines = state.split("\n");
				for (String line: lines) {
					double time = i * dt;
					if (false) {
						System.out.println(i + "\t" + time + "\t" + line);
					} else {
						ArrayList<Body> bodies = world.getBodies();
						Body a = bodies.get(0), b = bodies.get(1);
						System.out.println(i + " " + time + " " + a.getPos().clone().sub(b.getPos()).len());
					}
				}
			}
			world.run(dt);
		}
	}
}
