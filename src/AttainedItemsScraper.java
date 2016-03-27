import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AttainedItemsScraper {
	
	private static final String uri 	 = "https://us.api.battle.net/d3/data/item/";
	private static final String api_key  = "s6gndac669jv34bfh3q6j88hutv8fmm7";
	private static final String locale   = "en_US";
	
	public static final String TOOLTIP_DIRECTORY  = "D:\\mars-ws\\DW\\src\\json\\items\\tooltips";
	public static final String SPECIFIC_DIRECTORY = "D:\\mars-ws\\DW\\src\\json\\items\\specific";
	
	private static int item_file_to_parse = 10; //10 in total. will change manually per hour.
	
	
	public static void main(String[] args) throws IOException{
		AttainedItemsScraper AttainedItemsScraper = new AttainedItemsScraper();
		AttainedItemsScraper.scrapeItemData();
	}
	
	@SuppressWarnings("resource")
	public void scrapeItemData() throws IOException{
		File jsonToolTipFile, jsonSpecificFile ;
		FileWriter fw;
		Scanner scanner;
		String jsonItemStringShort;
		String jsonItemStringLong;
		JsonParser jParser;
		JsonObject jsonItem;
		int itemCount;
		
		itemCount = 1;
		jsonToolTipFile = new File(TOOLTIP_DIRECTORY+"\\items-"+item_file_to_parse+".json");
		jsonSpecificFile = new File(SPECIFIC_DIRECTORY+"\\items-"+item_file_to_parse+".json"); // file that will store the item specific data
		if(jsonSpecificFile.exists())
			jsonSpecificFile.delete();
		jsonSpecificFile.createNewFile();
		
		scanner = new Scanner(jsonToolTipFile);
		fw = new FileWriter(jsonSpecificFile);
		jParser = new JsonParser();
		
		System.out.println(TOOLTIP_DIRECTORY+"\\items-"+item_file_to_parse+".json");
		System.out.println("Starting scanning file: " + jsonToolTipFile.getName());
		while(scanner.hasNextLine()){
			jsonItemStringShort = scanner.nextLine();
			jsonItem = jParser.parse(jsonItemStringShort).getAsJsonObject();
			jsonItemStringLong = getItemDataFromAPI(jsonItem.get("tooltip").getAsString());
			fw.write(jsonItemStringLong+"\n");
			System.out.println("finished request # " + itemCount);
			itemCount++;
		}
		
		fw.close();
		System.out.println("Finished scanning file: " + jsonToolTipFile.getName());
		System.out.println("item count: " + itemCount);
	}
	
	public String getItemDataFromAPI(String tooltip){
		String result, uri, inputLine;
		StringBuilder sbJson;
		int responseCode;
		
		URL url;
		HttpURLConnection connection;
		BufferedReader in;
		
		
		
		result = "";
		uri = this.uri+tooltip+"?locale="+locale+"&apikey="+api_key;
		
		try {
			url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("accept", "application/json");
			
//			System.out.println("	Sending request to " + uri );
			responseCode =connection.getResponseCode(); 
			if(responseCode==200){
//				System.out.println("	success");
				in = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				sbJson = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					sbJson.append(inputLine);
				}
				in.close();
				result = sbJson.toString();
			}
			else{
				System.out.println("response code: " + responseCode);
			}
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
//		System.out.print(result);
		return result;
	}
	
	public String getItemDataFromAPI2(String tooltip){
		String result;
		String uri;
		result = "";
		
		HttpClient client;
		HttpGet request;
		HttpResponse response;
		
		//https://us.api.battle.net/d3/data/item/Co8BCInix10SBwgEFRu-7hMdgoHGxB1mIwZQHeZqMcAd5hXbDR1LtflLHW4Xx3swi1o4uwFAAEgBUBJYBGCBA2orCgwIABCxkt61gYCAwBUSGwjTsZ_mChIHCAQV9gpf_jCPAjgAQAFYBJABAYABRo0BU6AyiaUB5moxwK0BnAYAy7UBf_lOXbgBzKm56gTAAQIYtvj1vgJQAlgAoAG2-PW-AqABqPK_8QY?locale=en_US&apikey=s6gndac669jv34bfh3q6j88hutv8fmm7
		uri = this.uri+tooltip+"?locale="+locale+"&apikey="+api_key;
		client = HttpClientBuilder.create().build();
		request = new HttpGet(uri);
		request.addHeader("accept", "application/json");
		
		System.out.println("Sending request to " + uri );
		try{
			response = client.execute(request);
			if(response.getStatusLine().getStatusCode() == 200){
				System.out.println("Request to " + uri + " success");
				
				BufferedReader rd;
				String line;
				StringBuilder sbJson;
				
				line = "";
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				sbJson = new StringBuilder();
				
				while ((line = rd.readLine()) != null) {
					sbJson.append(line);
				}
				
				result = sbJson.toString();
			}
			else{
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
		
		
		return result;
	}

}
