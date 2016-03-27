import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * The purpose of this class is to encapsulate
 * the methods needed to load data from our
 * json files to a MySQL database.
 * 
 * @author johng
 *
 */
public class ItemDatabaseLoader {
	
	public static final String TOOLTIP_DIRECTORY  = "D:\\mars-ws\\DW\\src\\json\\items\\tooltips";
	public static final String SPECIFIC_DIRECTORY = "D:\\mars-ws\\DW\\src\\json\\items\\specific";
	
	private static int item_file_to_parse = 10; //10 in total. will change manually per hour.
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException{
		File jsonFile;
		JsonParser jParser;
		JsonReader jReader;
		JsonObject jItem;
		Scanner scanner;
		String jItemString;
		String itemId, itemName;
		String twoHanded, itemType, damageRange, armor;
		JsonArray jPrimaryAttr;
		Iterator itr;
		String primaryAttr;
		
		jItemString = "";
		jsonFile = new File(SPECIFIC_DIRECTORY+"\\items-"+item_file_to_parse+".json");
		jParser = new JsonParser();
		scanner = new Scanner(jsonFile);
		while(scanner.hasNextLine()){
			jItemString = scanner.nextLine();
			jItem = jParser.parse(jItemString).getAsJsonObject();
			itemId 		= jItem.get("id").toString();
			itemName 	= jItem.get("name").toString();
			twoHanded 	= jItem.getAsJsonObject("type").get("twoHanded").toString();
			itemType 	= jItem.getAsJsonObject("type").get("id").toString();
			damageRange = jItem.get("damageRange").toString().replace("–", "-");
			try{
				armor 		= jItem.getAsJsonObject("armor").get("min").toString(); // min and max will be the same
			}
			catch(NullPointerException e){
				armor = "";
			}
			System.out.println("string: " + jItemString);
			System.out.println("itemId 		: " + itemId 		);
			System.out.println("itemName 	: " + itemName 	    );
			System.out.println("twoHanded 	: " + twoHanded 	);
			System.out.println("itemType 	: " + itemType 	    );
			System.out.println("damageRange : " + damageRange   );
			System.out.println("armor 		: " + armor 		);
			jPrimaryAttr = jItem.getAsJsonArray("primary");
			itr = jPrimaryAttr.iterator();
		    while(itr.hasNext()){
		    	
		    }
			System.out.println(" ");
			Thread.sleep(2000);
		}
		
	}

}



