import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class Scraper
{
	private static final String userName     = "newbuu2";
	private static final String password     = "Warehousing1!";
	private static final String clientID     = "y2q8jpp27jpg7abfmpv5gs94erquqrnr";
	private static final String clientSecret = "YNcKTFYx5DXffh36c6rHpu5wybxBvcNM";
	private static final String authCode     = "h5y2ceq8t2btm3s5r7kk53gr";
	private static final String token        = "bp7a4emrf48663fmg5njeny8";
	private static final String jsonPath     = "./src/json/season/";
	
	private static final String[] leaderBoards = {"rift-barbarian", "rift-crusader", "rift-dh", "rift-monk", "rift-wd", "rift-wizard"};
	private static final String[] seasons      = {"1", "2", "3", "4"};
	
	private static final String uri      = "https://us.api.battle.net/data/d3/season/:season/leaderboard/:leaderboard?access_token=" + token;
	
	public static void main(String[] args) throws InterruptedException
	{
		Scraper scrape = new Scraper();
	}
	
	public Scraper() throws InterruptedException
	{
		scrapeLeaderBoards();
	}
	
	private void scrapeLeaderBoards() throws InterruptedException
	{
		HttpClient client = HttpClientBuilder.create().build();
		
		for(int season = 1; season < 5; season++)
		{
			System.out.println("Season " + season + ": ");
			
			for(int leaderBoard = 0; leaderBoard < leaderBoards.length; leaderBoard++)
			{
				System.out.println("\tLeaderBoard " + leaderBoards[leaderBoard] + ": ");
				
				String uri = Scraper.uri.replaceAll(":season", seasons[season - 1]).replaceAll(":leaderboard", leaderBoards[leaderBoard]);
				HttpGet request = new HttpGet(uri);
				request.addHeader("accept", "application/json");
				
				System.out.println("Sending request to " + uri );
				
				try
				{
					HttpResponse response = client.execute(request);
					
					if(response.getStatusLine().getStatusCode() == 200)
					{
						System.out.println("Request to " + uri + " success");
						
						BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						
						String line = "";
						File jsonFile = new File(Scraper.jsonPath + seasons[season - 1] + "/" + leaderBoards[leaderBoard] + ".json");
						jsonFile.createNewFile();
						FileWriter fw = new FileWriter(jsonFile);
						
						System.out.println("\t\tWriting Json");
						
						while ((line = rd.readLine()) != null) 
						{
							fw.write(line);
						}
						
						System.out.println("\t\tDone writing Json");
						
						fw.close();
						rd.close();
					}
					else
					{
						System.out.println(response.getStatusLine().getStatusCode());
					}
				} 
				catch (ClientProtocolException e)
				{
					System.out.println(e.getMessage());
				} 
				catch (IOException e)
				{
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
