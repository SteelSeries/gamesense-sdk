package com.sse3.gamesense;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.json.JSONException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GameSenseMod.MODID,
	 name = "gamesense",
	 version = GameSenseMod.VERSION)

public class GameSenseMod {
    public static final String MODID = "gamesense";
    public static final String VERSION = "1.11";
    
	// Tell Forge what instance to use.
	@Instance(value = GameSenseMod.MODID)
	public static GameSenseMod instance;

    //private HttpURLConnection sse3Connection = null;
	private CloseableHttpClient sseClient = null;
	private HttpPost ssePost = null; 
    private String sse3Address = "";
    private Boolean isConnected = false;
    private long lastTick = 0;
    
    public void SendGameEvent(String eventName, int data, EntityPlayer player) {
    	JSONObject eventData = new JSONObject();
    	eventData.put("value", data);
    	SendGameEvent(eventName, eventData, player);
    }
    
    public void SendGameEvent(String eventName, Boolean data, EntityPlayer player) {
    	JSONObject eventData = new JSONObject();
    	eventData.put("value", data);
    	SendGameEvent(eventName, eventData, player);
    }
    
    public void SendGameEvent(String eventName, String data, EntityPlayer player) {
    	JSONObject eventData = new JSONObject();
    	eventData.put("value", data);
    	SendGameEvent(eventName, eventData, player);
    }
    
    public void SendGameEvent(String eventName, JSONObject dataObject, EntityPlayer player) {
    	JSONObject event = new JSONObject();
    	event.put("game", "SSMCMOD");
    	event.put("event", eventName);
    	event.put("data", dataObject.toString());
    	//System.out.println("Sending " + event.toString());
    	executePost(event.toString(), player);
    }
    
    private void executePost(String urlParameters, EntityPlayer player) {
    	try {
    		
    		// If we're not connected, retry after a certain amount of time has elapsed.
    		if(!isConnected) {
    			// Don't try to reconnect for another 5 seconds
    			if(System.currentTimeMillis() - this.lastTick < 5000) {
    				return;
    			} else {
    				// reset lastTick and continue
    				this.lastTick = System.currentTimeMillis();
    			}
    		}

    		// Assume we're connected.
    		isConnected = true;
    		HttpResponse response;
    		StringEntity se = new StringEntity(urlParameters);
    		se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
    		ssePost.setEntity(se);
    		response = sseClient.execute(ssePost);
    		
    		if(response != null) {
        		// Don't care about response?
    			//InputStream is = response.getEntity().getContent();
    			
    			// reset the post so we can reuse it.
    			ssePost.reset();
    		}
    		
    	} catch (ConnectTimeoutException e) {
    		// Couldn't actually connect.
    		isConnected = false;
    		if(player != null) {
    			player.addChatMessage(new ChatComponentText("There was an error connecting to SteelSeries Engine 3"));
    		}
    	} catch (Exception e) {
    		// Likely a socket timeout w/ "Read timed out" which is fine, we just want to set & forget.
          	//e.printStackTrace();
        }
    }
    
    private void ConnectToSSE3() {
    	String jsonAddress = "";
    	// First open the config file to see what port to connect to.
		// Try to open Windows path one first
    	try {
			String corePropsFileName = System.getenv("PROGRAMDATA") + "\\SteelSeries\\SteelSeries Engine 3\\coreProps.json";
			BufferedReader coreProps = new BufferedReader(new FileReader(corePropsFileName));
	    	jsonAddress = coreProps.readLine();
	    	System.out.println("Opened coreprops.json and read: " + jsonAddress);
	    	coreProps.close();
    	} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("coreprops.json not found (Mac check)");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Something terrible happened looking for coreProps.json");
		}
		
		// If not on Windows, jsonAddress is probably still "", so try to open w/ Mac path
    	if(jsonAddress == "") {
	    	try {
				String corePropsFileName = "/Library/Application Support/SteelSeries Engine 3/coreProps.json";
				BufferedReader coreProps = new BufferedReader(new FileReader(corePropsFileName));
		    	jsonAddress = coreProps.readLine();
		    	System.out.println("Opened coreprops.json and read: " + jsonAddress);
		    	coreProps.close();
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
				System.out.println("coreprops.json not found (Windows check)");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Something terrible happened looking for coreProps.json");
			}
    	}
		
		try {
			// If we got a json string of address of localhost:<port> open a connection to it
			if(jsonAddress != "") {
				JSONObject obj = new JSONObject(jsonAddress);
				sse3Address = "http://" + obj.getString("address") + "/game_event";
			} else {
				// Debug default:
				sse3Address = "http://localhost:3000/game_event";
			}
			
    		sseClient = HttpClients.createDefault();
    		RequestConfig sseReqCfg = RequestConfig.custom()
    				.setSocketTimeout(10)
    				.setConnectTimeout(10)
    				.setConnectionRequestTimeout(50)
    				.build();
    		ssePost = new HttpPost(sse3Address);
    		ssePost.setConfig(sseReqCfg);
			
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("Something terrible happened creating JSONObject from coreProps.json.");
		}
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	ConnectToSSE3();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	MinecraftForge.EVENT_BUS.register(new GameSenseEventReceiver(Minecraft.getMinecraft()));
    }    
}
