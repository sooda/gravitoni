package gravitoni.config;

import java.io.*;
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

public class Config {
	private Scanner scn = null;
	private ConfigBlock globals = new ConfigBlock();
	private HashMap<String, ArrayList<ConfigBlock>> blocks = new HashMap<String, ArrayList<ConfigBlock>>();
	private ConfigBlock activeBlock = globals;
	private enum State { NOTHING, COMMENT, BLOCK, VARNAME };
	private State state = State.NOTHING;
	
	public Config(Reader rdr) throws FileNotFoundException {
		scn = new Scanner(rdr);  //new BufferedReader(new StringReader("Moro"))); // new FileReader
		read();
	}
	
	public Config(String configFile) throws FileNotFoundException {
		//System.out.println("Parsing: " + configFile);
		scn = new Scanner(new FileReader(configFile));
		read();
	}
	
	public void read() throws FileNotFoundException {
		// BufferedReader rdr;
		
		while (scn.hasNextLine()) {
			parseLine(scn.nextLine());
		}
	}
	
	public ConfigBlock getGlobals() {
		return globals;
	}
	
	public HashMap<String, ArrayList<ConfigBlock>> getBlocks() {
		return blocks;
	}
	
	public ConfigBlock getFirstBlock(String blockName) {
		ArrayList<ConfigBlock> blks = blocks.get(blockName);
		if (blks.size() == 0) return null;
		return blks.get(0);
	}
	
	private void parseLine(String line) throws FileNotFoundException {
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
			activeBlock = new ConfigBlock();
			blks.add(activeBlock);
			return;
		}
		
		// block ends
		if (line.equals("}")) {
			activeBlock = globals;
			return;
		}
		
		// a "key value" setting
		String[] data = line.split(" ", 2);
		if (data.length != 2) {
			System.out.println("Warning: Bad line: " + origLine);
		}
		if (data[0].equals("include")) {
			//try {
			merge(new Config(data[1]));
			//} catch (FileNotFoundException e) {
			//}
			return;
		}
		//System.out.println(data[0] + "!!" + data[1]);

		activeBlock.add(data[0], data[1]);
	}
	
	public void merge(Config other) {
		globals.merge(other.globals);
		for (String key: other.blocks.keySet()) {
			if (!blocks.containsKey(key)) blocks.put(key, new ArrayList<ConfigBlock>());
			ArrayList<ConfigBlock> ownList = blocks.get(key); 
			for (ConfigBlock blk: other.blocks.get(key)) {
				ownList.add(blk);
			}
		}
		//System.out.println("Merged: " + other);
	}
	
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

