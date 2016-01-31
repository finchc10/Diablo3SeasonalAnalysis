import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BattleTagExtractor
{
	private static final String[] leaderBoards = {"rift-barbarian", "rift-crusader", "rift-dh", "rift-monk", "rift-wd", "rift-wizard"};
	private static final String   jsonPath     = "./src/json/season/";
	
	public static void main(String[] args)
	{
		BattleTagExtractor.extractBattleTags();
	}
	
	public BattleTagExtractor()
	{
	}
	
	public static void extractBattleTags()
	{
		JSONParser parser = new JSONParser();
		
		for(int season = 1; season < 5; season++)
		{
			for(int board = 0; board < leaderBoards.length; board++)
			{
				File    jsonFile = new File(jsonPath + "/" + season + "/" + leaderBoards[board] + ".json");
				
				try
				{
					Object obj = parser.parse(new FileReader(jsonFile));
					JSONObject jsonObject = (JSONObject) obj;
					
					System.out.println("Processing " + jsonFile.getAbsolutePath());
					
					System.out.println(jsonObject.toJSONString());
				} 
				catch (FileNotFoundException e)
				{
					System.out.println("FILE NOT FOUND: " + jsonFile.getAbsolutePath());
				} 
				catch (IOException e)
				{
					System.out.println("IO EXCEPTION: " + jsonFile.getAbsolutePath());
				} 
				catch (ParseException e)
				{
					System.out.println("PARSE EXCEPTION: " + jsonFile.getAbsolutePath());
				}
				
			}
		}
	}
}
