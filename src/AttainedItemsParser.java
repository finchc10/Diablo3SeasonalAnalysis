import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Read json files
 * get item tooltip from json files
 * store item tooltip in json files.
 * @author johng
 *
 */

public class AttainedItemsParser{
	
	public static final String ITEM_DIRECTORY = "D:\\mars-ws\\DW\\src\\json\\items";
	public static final String SEASON_DIRECTORY = "D:\\mars-ws\\DW\\src\\json\\season";
	private final static String[] itemSlots = {"head", "torso", "feet", "hands", "shoulders", "legs", "bracers", "mainHand", "waist", "rightFinger", "leftFinger", "neck", "offHand"};
	private final static int MAX_NUM_API_REQUEST_PER_HOUR = 36000;
	
	/**
	 * Get character json files for each season in a single list
	 * extract item data from the character files
	 * query api to get specific item information.
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
		List<File> nonRiftFiles;
		List<AttainedItem> attainedItems;
		AttainedItemsParser parser = new AttainedItemsParser();
		nonRiftFiles = parser.getCharacterFiles(new File(SEASON_DIRECTORY));
		attainedItems = parser.getAttainedItemData(nonRiftFiles);
		System.out.println("num items: " + attainedItems.size());
		parser.writeItemDataToJson(attainedItems);
	}
	
	/**
	 * Writes item data to json files with a limit of 
	 * 36,000 item data's per file. 
	 * @param itemData
	 * @throws IOException
	 */
	private void writeItemDataToJson(List<AttainedItem> itemData) throws IOException{
		FileWriter fw;
		File jsonFile;
		AttainedItem item;
		int itemDataCount = 0;
		for(int fileCount=1; itemDataCount < itemData.size()-1; fileCount++){
			jsonFile = new File(ITEM_DIRECTORY+"\\items-"+fileCount+".json");
			if(jsonFile.exists())
				jsonFile.delete();
			jsonFile.createNewFile();
			fw = new FileWriter(jsonFile);
			System.out.println("Writing to : "+jsonFile.getName());
			for(int lineCount=0; lineCount < MAX_NUM_API_REQUEST_PER_HOUR; lineCount++){
				item = itemData.get(itemDataCount); 
				itemDataCount++;
				fw.write(item.toString());
			}
			fw.close();
		}	
	}
	
	private List<AttainedItem> getAttainedItemData(List <File> jsonFiles) throws FileNotFoundException, IOException, ParseException{
		List<AttainedItem> result;
		JsonObject jsonCharacter, jsonItems;
		JsonParser parser;
		String heroid, itemid, tooltip, jsonLine;
		AttainedItem item;
		Scanner scanner;

		//extract heroid, itemid, and item tooltip from the json files
		//add these fields to an AttainedItem class
		//add the class to the list
		
		//file
		result = new ArrayList<AttainedItem>();
		parser = new JsonParser();
		for(File file: jsonFiles){
			System.out.println("scanning file: " + file.getAbsolutePath());
			//line
			scanner = new Scanner(file);
			while(scanner.hasNextLine()){
				jsonLine = scanner.nextLine();
				jsonCharacter =  parser.parse(jsonLine).getAsJsonObject();
				jsonItems     =  jsonCharacter.get("items").getAsJsonObject();
				
				//item slot
				heroid = jsonCharacter.get("id").toString();
				for(String itemSlot: itemSlots){
//					if(!jsonItems.has(itemSlot)){
//						System.out.println("		missing: " +itemSlot);
//					}
					if(jsonItems.has(itemSlot)){
						itemid  = jsonItems.get(itemSlot).getAsJsonObject().get("id").toString();
						tooltip  = jsonItems.get(itemSlot).getAsJsonObject().get("tooltipParams").toString();
						tooltip = tooltip.substring(tooltip.indexOf("item/"), tooltip.length()); // strip "item/". It is not necessary for the http request later.
						item = new AttainedItem(heroid, itemid, tooltip);
						result.add(item);
					}
				}
			}
		}
		return result;
	}
	
	
	/**
	 * Retrieves non-rift character json files within the 
	 * given parent Folder.
	 * 
	 * @param parentFolder
	 * @return List<File> - list of non-rift json files.
	 */
	public List<File> getCharacterFiles(File parentFolder){
		return getCharacterFiles(parentFolder, 0);
	}
	
	private List<File> getCharacterFiles(File parentFolder, int depth){
		List<File> result;
		List<File> filesInFolder;
		
		result = new ArrayList<File>();
		if(depth > 3){
			//prevent infinite recursion
			return result;
		}
		
		//get list of fields in the parent folder
		filesInFolder = Arrays.asList(parentFolder.listFiles());
		for(File file: filesInFolder){
			if(file.getName().startsWith("character")){
				//found a character file
				result.add(file);
			}
			else{
				if(file.isDirectory()){
					result.addAll(getCharacterFiles(file,depth+1)); //season directory
				}
			}
		}
		
		return result;
	}
	
	private class AttainedItem{
		private String heroId;
		private String itemId;
		private String itemToolTip;
		
		public AttainedItem(String heroId, String itemId, String itemToolTip){
			this.heroId      = heroId     ;
			this.itemId      = itemId     ;
			this.itemToolTip = itemToolTip;
		}
		
		public String getHeroId(){
			return heroId;
		}
		
		public String getItemId(){
			return heroId;
		}
		
		public String getItemToolTip(){
			return heroId;
		}
		
		/**
		 * Returns the Json String representation of
		 * this object.
		 */
		public String toString(){
			String result;
			result = "";
			result += "{\"heroid\":\""+heroId+"\","+
					  " \"itemId\":\""+itemId+"\","+
					  " \"tooltip\":\""+itemToolTip+"\"}\n";
			return result;
		}
	}
}
