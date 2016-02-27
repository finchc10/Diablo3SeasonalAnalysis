import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CharacterParser
{
	private final static String   jsonPath    = "./src/json/season/";
	private final static String[] classes     = {"barbarian", "crusader", "demon hunter", "monk", "witch doctor", "wizard"};
	private final static String[] itemSlots   = {"head", "torso", "feet", "hands", "shoulders", "legs", "bracers", "mainHand", "waist", "rightFinger", "leftFinger", "neck", "offHand"};
	private final static String   activesQry  = "insert into characteractives (heroid, active1, rune1, active2, rune2, active3, rune3, active4, rune4) values (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private final static String   itemsQry    = "insert into characteritems (heroid, slot, params, id, name) values (?, ?, ?, ?, ?)";
	private final static String   passivesQry = "insert into characterpassives (heroid, passive1, passive2, passive3, passive4) values (?, ?, ?, ?, ?)";
	
	public static void main(String[] args)
	{
		CharacterParser p = new CharacterParser();
		p.parse();
	}
	
	public CharacterParser()
	{
	}
	
	public void parse()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/diablo3?user=cory&password=woodelf1");
			PreparedStatement activesStm  = connect.prepareStatement(activesQry);
			PreparedStatement passivesStm = connect.prepareStatement(passivesQry);
			PreparedStatement itemsStm    = connect.prepareStatement(itemsQry);
		
			for(int season = 1; season < 5; season++)
			{
				for(int clss = 0; clss < classes.length; clss++)
				{
					String heroClass = classes[clss];
					Scanner scan;
					File charFile    = new File(jsonPath + season + "/characters_" + heroClass + ".json");
					JSONParser parser = new JSONParser();
					
					try
					{
						scan = new Scanner(charFile);
						
						while(scan.hasNextLine())
						{
							String jsonLine       = scan.nextLine();
							try
							{
								JSONObject json     = (JSONObject) parser.parse(jsonLine);
								JSONObject skills   = (JSONObject) json.get("skills");
								JSONArray  actives  = (JSONArray) skills.get("active");
								JSONArray  passives = (JSONArray) skills.get("passive");
								Long gender         = (Long) json.get("gender");
								JSONObject jItems   = (JSONObject) json.get("items");
								Long    heroID   = (Long) json.get("id");
								
								ArrayList<String> activeSkills = new ArrayList<String>();
								ArrayList<String> activeRunes  = new ArrayList<String>();
								ArrayList<String> passiveNames = new ArrayList<String>();
								ArrayList<Item>   items        = new ArrayList<Item>();
	 							
								for(int skillIdx = 0; skillIdx < actives.size(); skillIdx++)
								{
									JSONObject skillRune = (JSONObject) actives.get(skillIdx);
									JSONObject skill     = (JSONObject) skillRune.get("skill");
									JSONObject rune      = (JSONObject) skillRune.get("rune");
									
									if(skill != null && rune != null)
									{
										activeSkills.add((String) skill.get("name"));
										activeRunes.add((String) rune.get("name"));
									}
									else
									{
										throw new IllegalSkillException();
									}
								}
								
								for(int pass = 0; pass < passives.size(); pass++)
								{
									JSONObject passive = (JSONObject) ((JSONObject) passives.get(pass)).get("skill");
									passiveNames.add((String) passive.get("name"));
								}
								
								for(int slot = 0; slot < itemSlots.length; slot++)
								{
									JSONObject item = (JSONObject) jItems.get(itemSlots[slot]);
									
									if(item != null)
									{
										String itemID  = (String) item.get("id");
										String name    = (String) item.get("name");
										String params  = ((String) item.get("tooltipParams")).replaceFirst("item/", "");
										String itmSlot = itemSlots[slot];
										
										items.add(new Item(itemID, name, params, itmSlot));
									}
								}
								
								for(Item i : items)
								{
									itemsStm.setLong(1, heroID);
									itemsStm.setString(2, i.getSlot());
									itemsStm.setString(3, i.getParams());
									itemsStm.setString(4, i.getID());
									itemsStm.setString(5, i.getName());
									itemsStm.execute();
								}
								
								passivesStm.setLong(1, heroID);
								passivesStm.setString(2, passiveNames.get(0));
								passivesStm.setString(3, passiveNames.get(1));
								passivesStm.setString(4, passiveNames.get(2));
								passivesStm.setString(5, passiveNames.get(3));
								passivesStm.execute();
								
								activesStm.setLong(1, heroID);
								activesStm.setString(2, activeSkills.get(0));
								activesStm.setString(3, activeRunes.get(0));
								activesStm.setString(4, activeSkills.get(1));
								activesStm.setString(5, activeRunes.get(1)); 
								activesStm.setString(6, activeSkills.get(2));
								activesStm.setString(7, activeRunes.get(2));
								activesStm.setString(8, activeSkills.get(3));
								activesStm.setString(9, activeRunes.get(3));
								activesStm.execute();
							}
							catch(ParseException e)
							{
								System.err.println("Invalid JSON");
							}
							catch(IllegalSkillException e)
							{
							}
						}
						
						scan.close();
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
				}
			}
		} 
		catch (ClassNotFoundException e1) 
		{
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public class IllegalSkillException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 7799454938729001227L;
		
	}
	
	public class Item
	{
		private String id;
		private String name;
		private String params;
		private String slot;
		
		public Item(String id, String name, String params, String slot)
		{
			this.id     = id;
			this.name   = name;
			this.params = params;
			this.slot   = slot;
		}
		
		public String getID()
		{
			return id;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getParams()
		{
			return params;
		}
		
		public String getSlot()
		{
			return slot;
		}
		
		public String toString()
		{
			return id + " : " + slot + "\t" + name + "\t" + params + "\n";
		}
	}
}
