package gravitoni.config;

import gravitoni.simu.Vec3;

import java.util.Enumeration;
import java.util.Hashtable;

public class ConfigBlock {
	private Hashtable<String, String> table = new Hashtable<String, String>();
	
	public void add(String key, String value) {
		table.put(key, value);
	}
	
	public String get(String key) {
		return table.get(key);
	}
	
	public int getInt(String key) {
		return Integer.parseInt(table.get(key));
	}
	
	public double getDouble(String key) {
		return Double.parseDouble(table.get(key));
	}
	
	public Vec3 getVec(String key) {
		String s = table.get(key);
		String[] parts = s.split(",");
		if (parts.length != 3) return null;
		return new Vec3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
	}
	
	public Enumeration<String> keys() {
		return table.keys();
	}
	
	public void merge(ConfigBlock other) {
		table.putAll(other.table);
	}
	
	public String toString() {
		return "ConfigBlock: " + table.toString();
	}
	
	public boolean has(String key) {
		return table.containsKey(key);
	}
}
