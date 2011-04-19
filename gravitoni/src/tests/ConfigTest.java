package tests;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.*;

import gravitoni.config.*;

public class ConfigTest {
	
	private Config getCfg(String str) {
		return new Config("", new StringReader(str));	
	}
	
	@Test
	public void descendfromparent() {
		try {
			getCfg("}");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("One"));
		}
		try {
			getCfg("foo{\n}\n}");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().startsWith("One"));
			return;
		}
		assertTrue("Exception?", false);
	}
	
	@Test
	public void havevars() {
		Config c = getCfg("a 123\nb /* asdf */ {\nc 456\n}");
		assertTrue("Have a", c.getVars().has("a"));
		assertTrue("Correct a", c.getVars().get("a").equals("123"));
		assertTrue("Have b", c.hasSections("b"));
		assertTrue("Correct b", c.getFirstSection("b").getVars().get("c").equals("456"));
	}
	
	@Test
	public void parsevars() {
		Config c = getCfg("a 123\nb 1, 2 ,4\nd lol\ne 12,lol,34");
		ConfigBlock b = c.getVars();
		assertEquals("Get int", b.getInt("a"), 123);
		assertEquals("Get double", b.getDouble("a"), 123.0, 0.01);
		assertTrue("Get vec", b.getVec("b").x == 1 && b.getVec("b").y == 2 && b.getVec("b").z == 4);
		int a = 0;
		try {
			b.getInt("d");
		} catch (NumberFormatException e) {
			a = 1;
		}
		assertEquals("Exception", a, 1);
		try {
			b.getVec("e");
		} catch (NumberFormatException e) {
			a = 2;
		}
		assertEquals("Exception?", a, 2);
	}
}
