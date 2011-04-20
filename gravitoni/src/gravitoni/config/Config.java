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
	/** My type name. */
	private String name = "";
	
	/** For scanning the configuration stream. */
	private Scanner scn = null;
	
	/** My own name/variable pairs. */
	private ConfigBlock myVars = new ConfigBlock();
	
	/** Name-keyed arrays of subsections. */
	private HashMap<String, ArrayList<Config>> subsections = new HashMap<String, ArrayList<Config>>();
	
	/** Active section while parsing a file. */
	private Config activeSection;
	
	/** Comment parsing state. This would include more stuff if we had a better tokenizer. */
	private enum State { CODE, COMMENT };
	
	/** Are we in a code or comment block currently? */
	private State state = State.CODE;
	
	/** The upper config sections while parsing a subconfiguration */
	private ArrayDeque<Config> sectionStack = new ArrayDeque<Config>(); 
	
	public void setName(String n) {
		name = n;
	} 

	public String getName() {
		return name;
	}
	
	/** Empty config with no source. Mainly for local private recursing. */
	public Config(String name) {
		this.name = name;
	}
	
	public static Config fromString(String cfg) {
		return new Config("main", new StringReader(cfg));
	}
	
	/** Read everything from rdr. */
	public Config(String name, Reader rdr) {
		this(name);
		scn = new Scanner(rdr);
		read();
	}
	
	/**
	 * Open the given file and read everything from it.
	 *  
	 * @throws FileNotFoundException
	 */
	public Config(String name, String configFile) throws FileNotFoundException {
		this(name, new FileReader(configFile));
	}
	
	/**
	 * For internal use, read all lines from this.scn.
	 */
	public void read() {
		sectionStack.clear();
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
	 * Find out if we have these sections.
	 */
	public boolean hasSections(String key) {
		return subsections.containsKey(key);
	}
	
	/**
	 * Find the first block of the given key. Don't care about others.
	 * 
	 * @param name The one to look for
	 * @return null, if not found.
	 */
	public Config getFirstSection(String name) {
		ArrayList<Config> blks = subsections.get(name);
		if (blks == null || blks.size() == 0) return null;
		return blks.get(0);
	}
	
	/** Strip out comments, handle the comment state machine. */
	private String doComments(String line) {
		int startPos, endPos;
		do {
			startPos = line.indexOf("/*");
			endPos = line.indexOf("*/");
			if (state == State.COMMENT) { // a comment has been started beforehand
				if (endPos == -1) return ""; // does not end here, whole line is a comment
				state = State.CODE; // comment ended
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
	
	/** Add the given section to the list of 'key' named sections. Create the list, if there's not one yet. */
	private void addSection(String key, Config section) {
		ArrayList<Config> sects = subsections.get(key);
		if (sects == null) {
			sects = new ArrayList<Config>();
			subsections.put(key, sects);
		}
		sects.add(section);
	}
	
	/** Handle the stack and create a new configuration section by the given line. */
	private void descendToSection(String line) {
		String sectName = line.substring(0, line.length() - 1).trim(); // off goes the '{'
		sectionStack.push(activeSection); // remember where we came from...
		Config newSection = new Config(sectName);
		activeSection.addSection(sectName, newSection);
		activeSection = newSection; // and set the current configuration to the new one.
	}
	
	/** Handle the stack while quitting the parsing of a section. */
	private void ascendFromSection() {	
		if (sectionStack.size() == 0)
			throw new RuntimeException("One does not simply ascend out from the topmost config section!");
		activeSection = sectionStack.pop();
	}
	
	/** Assuming there's a name/value var pair in the line, handle it (or start an include, if it's one). */
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
	
	/** Handle an include directive. Read the given file and merge it to currently handled section. */
	private void mergeInclude(String line) {
		String[] opts = line.split(" ");
		String fName = opts[0];
		ConfigBlock baseVars = new ConfigBlock();
		// read options (e.g. origins)
		for (int i = 1; i < opts.length; i++) {
			if (opts[i].indexOf('=') != -1) {
				String[] pair = opts[i].split("=", 2);
				baseVars.add(pair[0], pair[1]);
			}
		}
		Config cfg = null;
		try {
			cfg = new Config("(merge)", fName);
		} catch (FileNotFoundException e) {
			System.out.println("Warning: couldn't load " + fName);
			return;
		}
		// TODO: better options handling
		// currently, we can't merge baseVars to cfg, because 
		// it'd overwrite activeSection's globals too, which is not what we want
		// options are used only for a few special cases, so for now, just handle them here
		if (cfg.getSubsections("body") != null) {
			for (Config blk : cfg.getSubsections("body")) {
				if (baseVars.get("origin") != null) 
					blk.getVars().add("origin", baseVars.get("origin"));
				if (baseVars.get("vorigin") != null) 
					blk.getVars().add("vorigin", baseVars.get("vorigin"));
			}
		}
		activeSection.merge(cfg);
	}
	
	/** Whaddya got? */
	public ConfigBlock getVars() {
		return myVars;
	}
	
	/**
	 * For internal use, parse the given line string and do whatever we need to.
	 * 
	 * This works in quite a weird way, instantiating other Configs. 
	 * Should maybe be recursive (or rather, read() should), or we could have a static factory method.
	 */
	private void parseLine(String line) {
		String origLine = line;
		
		// skip whitespace and comments
		line = line.trim();
		line = doComments(line);
		line = line.trim();
		if (line.length() == 0) return;
		
		//System.out.println(name + " PARSING:"+line);
		
		if (line.endsWith("{")) { // beginning of a new cfg section
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
	 * Merge the given block to this' variables.
	 */
	public void mergeGlobals(ConfigBlock other) {
		myVars.merge(other);
	}
	
	/**
	 * Merge everything from the given configuration to this.
	 * 
	 * Merge globals, and add all config subsections that the given config has. 
	 * TODO: this doesn't make deep copies. Not so bad as we mostly just read these.
	 * 
	 * @param other The configuration to merge to this.
	 */
	public void merge(Config other) {
		mergeGlobals(other.myVars);
		for (String key: other.subsections.keySet())
			for (Config blk: other.subsections.get(key))
				addSection(key, blk);
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
	
	/** Make another. */
	public Config clone() {
		Config c = new Config(name);
		c.merge(this);
		return c;
	}
}

