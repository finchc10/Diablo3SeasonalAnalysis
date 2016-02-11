import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class InfoGatherer
{
	private static final String jsonPath  = "./src/json/season/";
	private static final String uri       = "https://us.api.battle.net/d3/profile/:battletag/hero/:heroid?locale=en_US&apikey=y2q8jpp27jpg7abfmpv5gs94erquqrnr";
	private static final String BARB      = "barbarian";
	private static final String DH        = "demon hunter";
	private static final String CRU       = "crusader";
	private static final String MONK      = "monk";
	private static final String WIZ       = "wizard";
	private static final String WD        = "witch doctor";
	private static final String[] classes = {BARB, DH, CRU, MONK, WIZ, WD};
	
	//Request for Truffl-1586 : 62840373 success
	
	public static void main(String[] args)
	{
		InfoGatherer ig = new InfoGatherer();
		ig.gather();
	}
	
    public InfoGatherer()
    {
    	
    }
    
    public void gather()
    {
    	HashMap<String, FileWriter> writers = new HashMap<String, FileWriter>();
		Connection        connect = null;
		PreparedStatement prepStm = null;
		ResultSet         results = null;
		int requests = 0;
		
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/diablo3?user=cory&password=woodelf1");
			prepStm = connect.prepareStatement("select season, heroid, battletag, heroclass from leaderboards order by season, heroclass");
			results = prepStm.executeQuery();
			HttpClient client = HttpClientBuilder.create().build();
			JSONParser parser = new JSONParser();
			Long       badSeason = new Long(5);
			
			for(int seasn = 1; seasn < 5; seasn++)
			{
				for(int heroClass = 0; heroClass < classes.length; heroClass++)
				{
					File jsonOutput = new File(jsonPath + seasn + "/characters_" + classes[heroClass] + ".json");
					jsonOutput.createNewFile();
					FileWriter writer = new FileWriter(jsonOutput);
					writers.put(classes[heroClass] + "_" + seasn, writer);
				}
			}
			
		    while (results.next() && requests < 36000)
		    {
		        String battleTag = results.getString("battletag").replaceFirst("#", "-");
		        String heroID    = Integer.toString(results.getInt("heroid"));
		        int    season    = results.getInt("season");
		        String heroClass = results.getString("heroclass");
		        
				String uri = InfoGatherer.uri.replaceAll(":battletag", battleTag).replaceAll(":heroid", heroID);
				HttpGet request = new HttpGet(uri);
				request.addHeader("accept", "application/json");
				
				HttpResponse response = client.execute(request);
				
				if(response.getStatusLine().getStatusCode() == 200)
				{
					System.out.println("Request for " + battleTag + " : " + heroID + " success");
					
					BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					
					String line = "";
					StringBuilder json = new StringBuilder();
					
					while ((line = rd.readLine()) != null) 
					{
						json.append(line);
					}
					
					JSONObject jsonObject = (JSONObject) parser.parse(json.toString());
					
					if(!badSeason.equals((Long) jsonObject.get("seasonCreated")) || "NOTFOUND".equals((String) jsonObject.get("code")))
					{
						System.out.println("Good season, write json");
						writers.get(heroClass + "_" + season).write(json.toString() + System.getProperty("line.separator"));
					}
					else
					{
						System.err.println("Bad season, ignore");
					}
					
					System.out.println();
					
					rd.close();
				}
				
				requests++;
		    }
		    
		    Iterator<String> itr = writers.keySet().iterator();
		    
		    while(itr.hasNext())
		    {
		    	writers.get(itr.next()).close();
		    }
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClientProtocolException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void getItemInfo(String battleTag, Integer heroID)
    {
    	
    }
}
