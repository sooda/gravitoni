package gravitoni.config;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/*
 * <name> <value>;
 * blockname {
 * <name> <value>;
 * } 
 * include conf/foo.conf setting=something another=foobar
 *
 */

/** A configuration section, containing blocks of variables, and a list of other configurations.
 * 
 * One Config is one thing that has { and } and contains something in between.
 * A ConfigBlock is a collection of key/value pairs.
 * Parsing is not very sophisticated. "{" should be the last character on the line, and so on, should use a real tokenizer :( 
 *  
 */
public class Config {
	private String name = "";
	private Scanner scn = null;
	private ConfigBlock myVars = new ConfigBlock("(globals)"); // TODO: find out names for the blocks / create names for actual configs
	private HashMap<String, ArrayList<Config>> subsections = new HashMap<String, ArrayList<Config>>(); // name-ordered
	private ArrayList<Config> allSubsections = new ArrayList<Config>(); // all configs that follow straight from this one
	private Config activeSection;
	private enum State { NOTHING, COMMENT, BLOCK, VARNAME };
	private State state = State.NOTHING; // what are we parsing currently
	private ArrayDeque<Config> sectionStack = new ArrayDeque<Config>(); // the outer blocks while parsing current 
	
	public ConfigBlock getBlock() {
		return myVars;
	}
	
	public void setName(String n) {
		name = n;
	} 
	public String getName() {
		return name;
	}
	
	/** Empty config. Mainly for local private recursing */
	public Config() {
		
	}
	
	/**
	 * Read everything from rdr.
	 *  
	 * @param rdr Input data.
	 */
	public Config(Reader rdr) {
		scn = new Scanner(rdr);
		read();
	}
	
	/**
	 * Open the given file and read everything from it.
	 *  
	 * @param configFile The file name.
	 * @throws FileNotFoundException
	 */
	public Config(String configFile) throws FileNotFoundException {
		scn = new Scanner(new FileReader(configFile));
		read();
	}
	
	/**
	 * For internal use, read all lines from this.scn.
	 */
	public void read() {
		sectionStack.clear();
		// blockStack.push(globals);
		activeSection = this;
		while (scn.hasNextLine()) {
			parseLine(scn.nextLine());
		}
	}
	
	
	/**
	 * Get all subsections.
	 * 
	 * @return A key-values-pair of all the blocks.
	 */
	public HashMap<String, ArrayList<Config>> getSubsections() {
		return subsections;
	}
	
	/**
	 * Get all subsections matching the given name.
	 * 
	 * @param key The name to look for.
	 * @return A list of the configs that has the given key, or null if nothing was found.
	 */
	public ArrayList<Config> getSubsections(String key) {
		return subsections.get(key);
	}
	
	/**
	 * Find out if we have these blocks.
	 * 
	 * @param blockName The key to look for.
	 * @return true, if there's at least one block of the given name.
	 */
	public boolean hasSections(String blockName) {
		return subsections.containsKey(blockName);
	}
	
	/**
	 * Find the first block of the given key.
	 * 
	 * @param name The one to look for
	 * @return null, if not found.
	 */
	public Config getFirstSection(String name) {
		ArrayList<Config> blks = subsections.get(name);
		if (blks.size() == 0) return null;
		return blks.get(0);
	}
	
	/** Strip out comments, handle the state machine */
	private String doComments(String line) {
		// TODO: single-line comments
		// handle it to work with multilines correctly
		//int comments = line.indexOf("//");
		//if (comments != -1) line = line.substring(0, comments);
		// multi-line
		int startPos, endPos;
		do {
			startPos = line.indexOf("/*");
			endPos = line.indexOf("*/");
			if (state == State.COMMENT) { // a comment has been started beforehand
				if (endPos == -1) return ""; // does not end here, whole line is a comment
				state = State.NOTHING; // comment ended
				line = line.substring(endPos + 2);
			} else if (startPos != -1) { // not in a comment, but one is starting somewhere?
				if (endPos != -1) { // starts and ends here..
					line = line.substring(0, startPos) + line.substring(endPos + 2); // .. so cut it out
				} else { // does not end yet, discard the end
					line = line.substring(0, startPos);
					state = State.COMMENT;
					break;
				}
			}
		} while (!(startPos == -1 && endPos == -1));
		return line;
	}
	
	private void addSection(String key, Config section) {
		ArrayList<Config> sects = subsections.get(key);
		if (sects == null) {
			sects = new ArrayList<Config>();
			subsections.put(key, sects);
		}
		sects.add(section);
		allSubsections.add(section);
	}
	
	private void descendToSection(String line) {
		String blockName = line.substring(0, line.length() - 1).trim();
		sectionStack.push(activeSection);
		Config newSection = new Config();
		newSection.setName(blockName);
		activeSection.addSection(blockName, newSection);
		activeSection = newSection;
	}
	
	private void ascendFromSection() {	
		if (sectionStack.size() == 0)
			throw new RuntimeException("One does not simply ascend out from the topmost block");
		activeSection = sectionStack.pop();
	}
	
	private boolean handlePair(String line) {
		String[] data = line.split(" ", 2);
		if (data.length != 2)			
			return false;
		
		if (data[0].equals("include")) {
			mergeInclude(data[1]);
		} else {
			activeSection.myVars.add(data[0], data[1]);
		}
		return true;
	}
	
	private void mergeInclude(String line) {
		String[] opts = line.split(" ");
		String fName = opts[0];
		ConfigBlock baseVars = new ConfigBlock("[defaults]");
		// read options (e.g. origins)
		for (int i = 1; i < opts.length; i++) {
			if (opts[i].indexOf('=') != -1) {
				String[] pair = opts[i].split("=", 2);
				baseVars.add(pair[0], pair[1]);
			}
		}
		Config cfg = null;
		try {
			cfg = new Config(fName);
		} catch (FileNotFoundException e) {
			System.out.println("Warning: couldn't load " + fName);
			return;
		}
		// TODO: better options handling
		// currently, we can't merge baseVars to cfg, because 
		// it'd overwrite this's globals too, which is not what we want
		// options are used only for a few special cases, so for now, just handle them here
		if (cfg.getSubsections("body") != null) {
			//cfg.mergeGlobals(baseVars); // this would be neater, but can't be done 
			for (Config blk : cfg.getSubsections("body")) {
				if (baseVars.get("origin") != null) 
					blk.getVars().add("origin", baseVars.get("origin"));
				if (baseVars.get("vorigin") != null) 
					blk.getVars().add("vorigin", baseVars.get("vorigin"));
			}
		}
		activeSection.merge(cfg);
	}
	
	public ConfigBlock getVars() {
		return myVars;
	}
	
	/**
	 * For internal use, parse the given line string and do whatever we need to.
	 * 
	 * This works in quite a weird way, instantiating other Configs. 
	 * Should maybe be recursive (or rather, read() should)
	 * @param line
	 */
	private void parseLine(String line) {
		String origLine = line;
		
		// skip whitespace and comments
		line = line.trim();
		line = doComments(line);
		if (line.length() == 0) return;
		
		//System.out.println(name + " PARSING:"+line);
		
		if (line.endsWith("{")) { // beginning of a new cfg
			descendToSection(line);
		} else if (line.equals("}")) { // cfg ends
			ascendFromSection();
		} else { // now, an actual "key value" setting pair.
			if (!handlePair(line)) {
				System.out.println("Warning: Bad line: " + origLine);				
			}
		}

	}
	
	/**
	 * Merge the given block to this' globals.
	 * 
	 * @param other
	 */
	public void mergeGlobals(ConfigBlock other) {
		myVars.merge(other);
	}
	
	/**
	 * Merge everything from the given configuration to this.
	 * 
	 * Merge globals, and add all config subsections that the given config has.
	 * 
	 * @param other The configuration to merge to this.
	 */
	public void merge(Config other) {
		//System.out.println("Merging: " + other);
		mergeGlobals(other.myVars);
		
		for (String key: other.subsections.keySet()) {
			for (Config blk: other.subsections.get(key)) {
				addSection(key, blk);
			}
		}
		//System.out.println("After merge: " + this);
	}
	
	/**
	 * Return a string that tells what we have here.
	 */
	public String toString() {
		String ret = "{\nVariables { " + myVars.toString() + " }\nSections {\n";
		for (String blkName: subsections.keySet()) {
			ret += "'" + blkName + "' {\n";
			for (Config blk: subsections.get(blkName)) {
				ret += blk + "\n";
			}
			ret += "}\n";
		}
		return ret + "}\n}\n";
	}
	
	public Config clone() {
		Config c = new Config();
		c.setName(name);
		c.merge(this);
		return c;
	}
}

