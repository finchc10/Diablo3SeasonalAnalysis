import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		
		Connection        connect = null;
		PreparedStatement prepStm = null;
		ResultSet         results = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/diablo3?user=cory&password=woodelf1");
			
			StringBuilder query = new StringBuilder();
			
			query.append("    insert into                                                  ");
			query.append("        diablo3.leaderboard                                      ");
			query.append("        (season, heroid, battletag, paragon, rank, riftlevel)    ");
			query.append("    values                                                       ");
			query.append("        (?, ?, ?, ?, ? , ?)                                      ");
			prepStm = connect.prepareStatement(query.toString());
			
			for(int season = 1; season < 5; season++)
			{
				for(int board = 0; board < leaderBoards.length; board++)
				{
					File    jsonFile = new File(jsonPath + "/" + season + "/" + leaderBoards[board] + ".json");
					
					try
					{
						JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonFile));
						JSONArray ary = ((JSONArray) jsonObject.get("row"));
						
						for(int character = 0; character < ary.size(); character++)
						{
							JSONArray data   = (JSONArray)((JSONObject) ary.get(character)).get("data");
							JSONArray player = (JSONArray)((JSONObject) ((JSONArray)((JSONObject) ary.get(character)).get("player")).get(0)).get("data");
							
							Long   rank       = (Long)   ((JSONObject) data.get(0)).get("number");
							Long   riftLevel  = (Long)   ((JSONObject) data.get(1)).get("number");
							String heroClass  = (String) ((JSONObject) player.get(2)).get("string");
							Long   paragonLvl = (Long)   ((JSONObject) player.get(5)).get("number");
							Long   heroID     = null;
							
							for(int i = 0; i < player.size(); i++)
							{
								JSONObject arrayItem = (JSONObject) player.get(i);
	
								if(arrayItem.containsValue("HeroId"))
								{
									heroID = (Long) arrayItem.get("number");
								}
							}
							
							try
							{
							    String battleTag = (String) ((JSONObject) data.get(4)).get("string");
							    
							    if(heroID != null)
							    {
									prepStm.setLong(1, season);
									prepStm.setLong(2, heroID.longValue());
									prepStm.setString(3, battleTag);
									prepStm.setLong(4, paragonLvl.longValue());
									prepStm.setLong(5, rank.longValue());
									prepStm.setLong(6, riftLevel.longValue());
									prepStm.executeUpdate();
							    }
							}
							catch(IndexOutOfBoundsException e)
							{
								
							}
						}
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
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (SQLException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally
		{
		    try
		    {
		        if (prepStm != null) 
		        {
		          prepStm.close();
		        }

		        if (connect != null) 
		        {
		          connect.close();
		        }
		      } 
		    catch (Exception e) 
		    {

		    }
		}
	}
}
