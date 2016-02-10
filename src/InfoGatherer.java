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
	private static final String jsonPath = "./src/json/season/";
	private static final String uri      = "https://us.api.battle.net/d3/profile/Braab-1307/hero/70101120?locale=en_US&apikey=y2q8jpp27jpg7abfmpv5gs94erquqrnr";
	
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
		Connection        connect = null;
		PreparedStatement prepStm = null;
		ResultSet         results = null;
		int requests = 0;
		
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/diablo3?user=cory&password=woodelf1");
			prepStm = connect.prepareStatement("select season, heroid, battletag from leaderboards order by season, heroclass");
			results = prepStm.executeQuery();
			HttpClient client = HttpClientBuilder.create().build();
			JSONParser parser = new JSONParser();
			Long       badSeason = new Long(5);
			
			File season1 = new File(jsonPath + "1/characters.json");
			File season2 = new File(jsonPath + "2/characters.json");
			File season3 = new File(jsonPath + "3/characters.json");
			File season4 = new File(jsonPath + "4/characters.json");
			
			season1.createNewFile();
			season2.createNewFile();
			season3.createNewFile();
			season4.createNewFile();
			
			FileWriter s1 = new FileWriter(season1);
			FileWriter s2 = new FileWriter(season2);
			FileWriter s3 = new FileWriter(season3);
			FileWriter s4 = new FileWriter(season4);
			
		    while (results.next() && requests < 30001)
		    {
		        String battleTag = results.getString("battletag").replaceFirst("#", "-");
		        String heroID    = Integer.toString(results.getInt("heroid"));
		        int    season    = results.getInt("season");
		        
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
					
					if(!badSeason.equals((Long) jsonObject.get("seasonCreated")))
					{
						System.out.println("Good season, write json");
						switch(season)
						{
						    case 1: s1.write(json.toString()); break;
						    case 2: s2.write(json.toString()); break;
						    case 3: s3.write(json.toString()); break;
						    case 4: s4.write(json.toString()); break;
						}
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
		    
		    s1.close();
		    s2.close();
		    s3.close();
		    s4.close();
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
