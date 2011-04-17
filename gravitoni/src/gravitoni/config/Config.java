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
 * 
 *
 */

/** A configuration set, containing blocks of variables, and TODO: a list of other configurations. */
public class Config {
	private Scanner scn = null;
	private ConfigBlock globals = new ConfigBlock("(globals)");
	private HashMap<String, ArrayList<ConfigBlock>> blocks = new HashMap<String, ArrayList<ConfigBlock>>();
	private ArrayList<ConfigBlock> allBlocks = new ArrayList<ConfigBlock>();
	private ConfigBlock activeBlock;
	private enum State { NOTHING, COMMENT, BLOCK, VARNAME };
	private State state = State.NOTHING;
	private ArrayDeque<ConfigBlock> blockStack = new ArrayDeque<ConfigBlock>(); // the outer blocks 
	
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
		blockStack.clear();
		// blockStack.push(globals);
		activeBlock = globals;
		while (scn.hasNextLine()) {
			parseLine(scn.nextLine());
		}
		finalTune();
	}
	
	/**
	 * TODO
	 */
	private void finalTune() {
		if (globals.has("origin")) {
		 	ArrayList<ConfigBlock> x = blocks.get("body");
		 	if (x == null) return;
			for (ConfigBlock blk : x) {
				blk.add("origin", globals.get("origin"));
			}
		}
	}
	
	/**
	 * Get global settings.
	 * 
	 * @return The global variables.
	 */
	public ConfigBlock getGlobals() {
		return globals;
	}
	
	/**
	 * Get all blocks.
	 * 
	 * @return A key-values-pair of all the blocks.
	 */
	public HashMap<String, ArrayList<ConfigBlock>> getBlocks() {
		return blocks;
	}
	
	/**
	 * Get all blocks matching the given name.
	 * 
	 * @param key The block name to look for.
	 * @return A list of the blocks that has the given key, or null if nothing was found.
	 */
	public ArrayList<ConfigBlock> getBlocks(String key) {
		return blocks.get(key);
	}
	
	/**
	 * Find out if we have these blocks.
	 * 
	 * @param blockName The key to look for.
	 * @return true, if there's at least one block of the given name.
	 */
	public boolean hasBlocks(String blockName) {
		return blocks.containsKey(blockName);
	}
	
	/**
	 * Find the first block of the given key.
	 * 
	 * @param blockName
	 * @return null, if not found.
	 */
	public ConfigBlock getFirstBlock(String blockName) {
		ArrayList<ConfigBlock> blks = blocks.get(blockName);
		if (blks.size() == 0) return null;
		return blks.get(0);
	}
	
	/**
	 * For internal use, parse the given line string and do whatever we need to.
	 * @param line
	 */
	private void parseLine(String line) {
		String origLine = line;
		//System.out.println("Raw parsing:" + line);
		// skip comments
		line = line.trim();
		int startPos = line.indexOf("/*");
		int endPos = line.indexOf("*/");
		if (state == State.COMMENT) {
			if (endPos == -1) return;
			state = State.NOTHING;
			line = line.substring(endPos + 2);
			startPos = line.indexOf("/*");
		}
		if (startPos != -1) {
			state = State.COMMENT;
			return;
		}
		int comments = line.indexOf("//");
		if (comments != -1) line = line.substring(0, comments);
		if (line.length() == 0) return;
		
		//System.out.println("PARSING:"+line);
		
		// beginning of a new block
		if (line.endsWith("{")) {
			String blockName = line.substring(0, line.length() - 1).trim();
			ArrayList<ConfigBlock> blks = blocks.get(blockName);
			if (blks == null) {
				blks = new ArrayList<ConfigBlock>();
				blocks.put(blockName, blks);
			}
			//System.out.println("Going to block " + blockName);
			blockStack.push(activeBlock);
			activeBlock = new ConfigBlock(blockName);
			blks.add(activeBlock);
			allBlocks.add(activeBlock);
			return;
		}
		
		// block ends
		if (line.equals("}")) {
			// TODO: how about nested blocks! BUG!1
			if (blockStack.size() == 0) {
				throw new RuntimeException("One does not simply descend out from global variable block");
			}
			activeBlock = blockStack.pop();
			//System.out.println("Back to block " + activeBlock);
			return;
		}
		
		// a "key value" setting
		String[] data = line.split(" ", 2);
		if (data.length != 2) {
			System.out.println("Warning: Bad line: " + origLine);
		}
		if (data[0].equals("include")) {
			String[] opts = data[1].split(" ");
			String fName = opts[0];
			ConfigBlock baseDefaults = new ConfigBlock("[defaults]");
			for (int i = 1; i < opts.length; i++) {
				if (opts[i].indexOf('=') != -1) {
					String[] pair = opts[i].split("=", 2);
					baseDefaults.add(pair[0], pair[1]);
					System.out.println("Adding defaults: " + opts[i]);
					// TODO: origin pitäs saada privateks niin ettei merget mergaa pääglobaaliksi
				}
			}
			Config cfg = null;
			try {
				cfg = new Config(fName);
			} catch (FileNotFoundException e) {
				System.out.println("Warning: couldn't load " + fName);
			}
			if (cfg != null && cfg.getBlocks("body") != null) {
				//cfg.mergeGlobals(baseDefaults);
				for (ConfigBlock blk : cfg.getBlocks("body")) {
					if (baseDefaults.get("origin") != null) 
						blk.add("origin", baseDefaults.get("origin"));
					if (baseDefaults.get("vorigin") != null) 
						blk.add("vorigin", baseDefaults.get("vorigin"));
					System.out.println("Adding for body " + blk);
				}
				System.out.println("K, added: " + baseDefaults);
			}
			merge(cfg);
			return;
		}

		activeBlock.add(data[0], data[1]);
	}
	
	/**
	 * Merge the given block to this' globals.
	 * 
	 * @param other
	 */
	public void mergeGlobals(ConfigBlock other) {
		globals.merge(other);
	}
	
	/**
	 * Merge everything from the given configuration to this.
	 * 
	 * Merge globals, and add all blocks that the given config has.
	 * 
	 * @param other The configuration to merge to this.
	 */
	public void merge(Config other) {
		System.out.println("Merging: " + other);
		mergeGlobals(other.globals);
		
		for (String key: other.blocks.keySet()) {
			if (!blocks.containsKey(key)) blocks.put(key, new ArrayList<ConfigBlock>());
			ArrayList<ConfigBlock> ownList = blocks.get(key); 
			for (ConfigBlock blk: other.blocks.get(key)) {
				ownList.add(blk);
				allBlocks.add(blk);
			}
		}
		finalTune();
	}
	
	/**
	 * Return a string that tells what we have here.
	 */
	public String toString() {
		String ret = "Globals: " + globals.toString() + "\n";
		for (String blkName: blocks.keySet()) {
			ret += "Blocks '" + blkName + "':\n";
			for (ConfigBlock blk: blocks.get(blkName)) {
				ret += blk + "\n";
			}
		}
		return ret;
	}
}

