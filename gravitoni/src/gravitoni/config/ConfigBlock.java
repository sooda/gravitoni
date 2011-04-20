package gravitoni.config;

import gravitoni.simu.Vec3;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;

/** A block of configuration variable pairs of name/value, that can be parsed into several types */
public class ConfigBlock {
	private Hashtable<String, String> table = new Hashtable<String, String>();
	
	/** Construct an empty block. */
	public ConfigBlock() {
	}
	
	/** Merge from the given block. */
	public ConfigBlock(ConfigBlock orig) {
		merge(orig);
	}
	
	/** Set the given key to tell about the given value. */
	public void add(String key, String value) {
		table.put(key, value);
	}
	
	/** What value does key have? */
	public String get(String key) {
		return table.get(key);
	}
	
	/** Try to parse the given key as integer. */
	public int getInt(String key) {
		return Integer.parseInt(table.get(key));
	}
	
	/** Try to parse the given key as double. */
	public double getDouble(String key) {
		return Double.parseDouble(table.get(key));
	}
	
	/** Try to parse the given key as a vector. */
	public Vec3 getVec(String key) {
		Vec3 v = Vec3.parse(table.get(key));
		return v;
	}
	
	/** Get variable names. */
	public Enumeration<String> keys() {
		return table.keys();
	}
	
	/** Read (and possibly overwrite) the given block's variables here. */
	public void merge(ConfigBlock other) {
		table.putAll(other.table);
	}
	
	/** Human-readable representation. */
	public String toString() {
		return "ConfigBlock: " + table.toString();
	}
	
	/** Do we have this name? */
	public boolean has(String key) {
		return table.containsKey(key);
	}
	
	/** Magic! Read the given object's annotations with reflection, and try to apply my variables there. 
	 * 
	 * Go through the object's fields' ConfigVar annotations, find out the variables' types, and try to set them to
	 * the ones that their annotations' values match in my variable names.  
	 * */
	public void apply(Object obj, Class<?> cls) {
		try {
			for (Field f : cls.getDeclaredFields()) {
				if (f.isAnnotationPresent(ConfigVar.class)) {
					ConfigVar ann = f.getAnnotation(ConfigVar.class);
					String setting = ann.value();
					f.setAccessible(true); // Rape!
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
						System.out.println("Warning: couldn't apply cfg to " + f.getName());
					} catch (NumberFormatException e) {
						// ignore bad values
						System.out.println("Warning: couldn't apply cfg to " + f.getName());
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
