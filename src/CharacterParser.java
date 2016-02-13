import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CharacterParser
{
	private final static String   jsonPath  = "./src/json/season/";
	private final static String[] classes   = {"barbarian", "crusader", "demon hunter", "monk", "witch doctor", "wizard"};
	private final static Long     male      = new Long(0);
	private final static String[] itemSlots = {"head", "torso", "feet", "hands", "shoulders", "legs", "bracers", "mainHand", "waist", "rightFinger", "leftFinger", "neck", "offHand"};
	
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
