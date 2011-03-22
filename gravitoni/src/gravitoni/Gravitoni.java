package gravitoni;
import java.io.FileNotFoundException;
import java.io.FileReader;

import gravitoni.config.*;
import gravitoni.simu.*;
import gravitoni.ui.*;

import java.util.ArrayList;

public class Gravitoni {
	private World world = new World();
	private Config config = null;
	private Vec3 start;
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
		System.out.println("Lols");
	}
	
	public Gravitoni(String configFile) throws FileNotFoundException {
		FileReader rdr;
		rdr = new FileReader(configFile);
		loadConfig(new Config(rdr));
		start = world.getBodies().get(0).getPos().clone();
	}
	
	public void loadConfig(Config cfg) {
		System.out.println("Loading configuration: " + cfg);
		world.loadConfig(cfg);
		config = cfg;
	}
	
	public void run() {
		new UI(world);
	}
	
	public void run(double steps) {
		System.out.println("# step\ttime\tname\tx.x\tx.y\tx.z\tv.x\tv.y\tv.z");
		double dt = world.dt;
		int printsteps = config.getGlobals().getInt("printsteps");
		int minat = 0;
		double min = 99999999999999f;
		for (int i = 0; i < steps; i++) {
			double time = i * dt;
			/*
			if (i % printsteps == 0) {
				String state = world.toString();
				String[] lines = state.split("\n");
				if (false) { // print all bodies
					for (String line: lines)
						System.out.println(i + "\t" + time + "\t" + line);
				} else if (false) { // print distance between 0..1
					ArrayList<Body> bodies = world.getBodies();
					Body a = bodies.get(0), b = bodies.get(1);
					System.out.println(i + " " + time + " " + a.getPos().clone().sub(b.getPos()).len());
				} else if (true) { // print 0 distance from beginning
					double dist = world.getBodies().get(0).getPos().clone().sub(start).len();
					if (dist < min && i > 0) {
						min = dist;
						minat = i;
					}
					System.out.println(i + "\t" + time + "\t" + dist);
				}

			}
			*/
			world.run(dt);
		}
		System.out.println("# Min at " + minat + ": " + min);
	}
}
