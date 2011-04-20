package tests;

import org.junit.*;

import java.io.*;

import gravitoni.config.*;
import gravitoni.simu.Vec3;
import gravitoni.simu.World;

public class SimuTest {
	private Config getCfg(String str) {
		return new Config("", new StringReader(str));	
	}
	
	@Test
	public void rotator() {
		double G = 6.67e-11;
		double vel = Math.sqrt(G * 1e3 / 100);
		Config c = getCfg("body {\n name sun\n mass 1e3 \n }\n body {\n name earth\n mass 1\n position 100, 0, 0 \nvelocity 0, " + vel + ", 0\n }");
		World world = new World();
		world.loadConfig(c);
		world.dt = 1;
		Vec3 start = new Vec3(100, 0, 0);
		int i = 0;
		double maxDiff = -1;
		double minDiff = 1000;
		do {
			world.run(1000);
			i++;
			double dist = world.getBody("earth").getPos().clone().sub(world.getBody("sun").getPos()).len();
			System.out.println(world.getBody("earth").getPos() 
					+ " " + world.getBody("earth").getPos().clone().sub(start).len()
					+ " " + dist);
			if (dist > maxDiff) maxDiff = dist;
			if (dist < minDiff) minDiff = dist;
			//assertEquals("Right distance", 100.0, dist, 0.1);
		} while (world.getBody("earth").getPos().clone().sub(start).len() >= 0.001 && i < 30000);
		System.out.println("Max difference: " + maxDiff);
		System.out.println("Min difference: " + minDiff);
	}
}
