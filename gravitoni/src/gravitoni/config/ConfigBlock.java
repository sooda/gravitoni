package gravitoni.config;

import gravitoni.simu.Vec3;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;

public class ConfigBlock {
	private Hashtable<String, String> table = new Hashtable<String, String>();
	private String name;
	
	public ConfigBlock(String name) {
		this.name = name;
	}
	
	public ConfigBlock(String name, ConfigBlock orig) {
		this(name);
		merge(orig);
	}
	
	public String getName() {
		return name;
	}
	
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
		Vec3 v = Vec3.parse(table.get(key));
		return v;
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
	
	public void apply(Object obj, Class<?> cls) {
		System.out.println("Doing at " + this);
		try {
			for (Field f : cls.getDeclaredFields()) {
				//System.out.println("Trying: " + f.getName());
				if (f.isAnnotationPresent(ConfigVar.class)) {
					ConfigVar ann = f.getAnnotation(ConfigVar.class);
					String setting = ann.value();
					//System.out.println("CONFIG: found " + setting + "/" + f.getName() + " for " + obj.getClass().getName());
					f.setAccessible(true);
					try {
						if (f.getType().equals(int.class)) {
							f.set(obj, getInt(setting));
						} else if (f.getType().equals(double.class)) {
							f.set(obj, getDouble(setting));
						} else if (f.getType().equals(String.class)) {
							f.set(obj, get(setting));
						} else if (f.getType().equals(Vec3.class)) {
							f.set(obj, getVec(setting));
						} else {
							System.out.println(f.getClass());
						}
					} catch (NullPointerException e) {
						// couldn't transform the type as the value could not be found
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
