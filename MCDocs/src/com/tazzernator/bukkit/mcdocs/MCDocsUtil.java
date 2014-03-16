/*
 * MCDocs by Tazzernator 
 * (Andrew Tajsic ~ atajsicDesigns ~ http://atajsic.com) 
 * 
 * THIS PLUGIN IS LICENSED UNDER THE WTFPL - (Do What The Fuck You Want To Public License)
 * 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 * 
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *   
 * 0. You just DO WHAT THE FUCK YOU WANT TO.
 *   
 * */

package com.tazzernator.bukkit.mcdocs;

//Java Imports
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

//Bukkit Imports
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

//GeoIPTools Import
import uk.org.whoami.geoip.GeoIPLookup;
import uk.org.whoami.geoip.GeoIPTools;

//Other Imports
import com.tazzernator.bukkit.mcdocs.cache;
import org.bukkit.Bukkit;

//Listener Class
public class MCDocsUtil implements Listener {
		
	//Some Variables for the class.
	private MCDocs plugin;
	FileConfiguration config;
	static GeoIPLookup geoIP = null;
	public static final Logger log = Logger.getLogger("Minecraft");
	private ArrayList<String> fixedLines = new ArrayList<String>();
	
	private ArrayList<MCDocsCommands> commandsList = new ArrayList<MCDocsCommands>();
	private ArrayList<MCDocsPlayerJoin> joinList = new ArrayList<MCDocsPlayerJoin>();
	private ArrayList<MCDocsPlayerQuit> quitList = new ArrayList<MCDocsPlayerQuit>();
	private ArrayList<MCDocsMOTD> motdList = new ArrayList<MCDocsMOTD>();
	private ArrayList<MCDocsOnlineFiles> onlineFiles = new ArrayList<MCDocsOnlineFiles>();
	
	//Configuration Defaults
	private String headerFormat = "[color=red][b]%commandname[/b][/color] | [color=yellow]Page %current of %count[/color] | [color=gray]%command <page>[/color]";
	private String onlinePlayersFormat = "%prefix%name";
	private String newsFile = "news.txt";
	private int newsLines = 1;
	private int linesPerPage = 9;
	private boolean motdEnabled = true;
	private boolean commandLogEnabled = true;
	private boolean errorLogEnabled = true;
	private boolean playerBroadcastMessageEnabled = true;
	private int cacheTime = 5;
        
        private final cache cache = new cache();
	
	/*
	 * -- Constructor for MCDocsListener --
	 * All we do here is import the instance.
	 */
	
	public MCDocsUtil(MCDocs instance) {
		this.plugin = instance;
	}
	
	
	/*
	 * -- Configuration Methods --
	 * We check for config.yml, if it doesn't exists we create a default (defaultConfig), then we load (loadConfig). 
	 */
	
	public void setupConfig(FileConfiguration config){
		
		this.config = config;
		
		if (!(new File(plugin.getDataFolder(), "config.yml")).exists()){
			logit("Configuration not found, making a default one for you! <3");
			defaultConfig();
		}
		loadConfig();
	}
	
	private void defaultConfig(){
		try {
			PrintWriter stream = null;
			File folder = plugin.getDataFolder();
			if (folder != null) {
				folder.mkdirs();
			}
			String folderName = folder.getParent();
			PluginDescriptionFile pdfFile = this.plugin.getDescription();

			stream = new PrintWriter(folderName + "/MCDocs/config.yml");
			//Let's write our goods ;)
				stream.println("#MCDocs " + pdfFile.getVersion() + " by Tazzernator / Andrew Tajsic");
				stream.println("#Configuration File.");
				stream.println("#For detailed assistance please visit: http://dev.bukkit.org/server-mods/mcdocs/");
				stream.println();
				stream.println("#Here we determine which command will show which file. ");
				stream.println("commands:");
				stream.println("    /motd:");
				stream.println("        file: 'motd.txt'");
				stream.println("        groups:");
				stream.println("            Admin: 'motd-admin.txt'");
				stream.println("            Moderator: 'motd-moderator.txt'");
				stream.println("    /rules:");
				stream.println("        file: 'rules.txt'");
				stream.println("    /news:");
				stream.println("        file: 'news.txt'");
				stream.println("    /register:");
				stream.println("        file: 'register.txt'");
				stream.println("    /server:");
				stream.println("        file: 'server.txt'");
				stream.println("    /about:");
				stream.println("        file: 'http://tazzernator.com/files/bukkit/plugins/MCDocs/about.txt'");
				stream.println("    /help:");
				stream.println("        file: 'help/default.txt'");
				stream.println("        groups:");
				stream.println("            Admin: 'help/admin.txt'");
				stream.println("            Moderator: 'help/moderator.txt'");
				stream.println();
				stream.println("#Show a MOTD at login? Yes: true | No: false");
				stream.println("motd-enabled: " + motdEnabled);
				stream.println();
				stream.println("#Here we determine which files are shown when a player joins the server.");
				stream.println("motd:");
				stream.println("    file: 'motd.txt'");
				stream.println("    groups:");
				stream.println("        Admin: 'motd-admin.txt'");
				stream.println("        Moderator: 'motd-moderator.txt'");
				stream.println();
				stream.println("#Replace the vanilla join and quit messages? Yes: true | No: false");
				stream.println("broadcast-enabled: " + playerBroadcastMessageEnabled);
				stream.println();
				stream.println("#Here we determine what is announced to the server for each group on join and quit. ");
				stream.println("#If you don't define a group it's own specific string, the default message is used.");	
				stream.println("join:");
				stream.println("    message: '%prefix%group%suffix (%prefix%name%suffix) has joined from %country.'");
				stream.println("    groups:");
				stream.println("        Admin: '%prefix%group%suffix (%prefix%name%suffix) has joined the server. Respect the admins.'");
				stream.println("    players:");
				stream.println("        Tazzernator: '%prefix%group%suffix (%prefix%name%suffix) has come to steal all your cute Ocelots!'");
				stream.println("quit:");
				stream.println("    message: '%prefix%group%suffix (%prefix%name%suffix) has left the server.'");
				stream.println("    groups:");
				stream.println("        Admin: '%prefix%group%suffix (%prefix%name%suffix) has left the server. You can relax.'");
				stream.println("    players:");
				stream.println("        Tazzernator: '%prefix%group%suffix (%prefix%name%suffix) has made away with all your cute Ocelots!'");
				stream.println();
				stream.println("#This changes the pagination header that is added to MCDocs automatically when there is > 10 lines of text.");
				stream.println("header-format: '" + headerFormat + "'");
				stream.println();
				stream.println("#Format to use when using %online or %online_group.");
				stream.println("online-players-format: '" + onlinePlayersFormat + "'");
				stream.println();
				stream.println("#The file to displayed when using %news.");
				stream.println("news-file: '" + newsFile + "'");
				stream.println();
				stream.println("#How many lines to show when using %news.");
				stream.println("news-lines: " + newsLines);
				stream.println();
				stream.println("#How many lines should be shown per page? 0 = unlimited");
				stream.println("lines-per-page: " + linesPerPage);
				stream.println();
				stream.println("#How long, in minutes, do you want online files to be cached locally? 0 = disable");
				stream.println("cache-time: " + cacheTime);
				stream.println();
				stream.println("#Inform the console when a player uses a command from the commands list.");
				stream.println("command-log-enabled: " + commandLogEnabled);
				stream.println();
				stream.println("#Send warnings and errors to the main server log? Yes: true | No: false");
				stream.println("error-log-enabled: " + errorLogEnabled);
				stream.close();
				
		} catch (FileNotFoundException e) {
			logit("Error saving the config.yml.");
		}
	}
	
	public void loadConfig(){
		commandsList.clear();
		motdList.clear();
		
		String folderName = plugin.getDataFolder().getParent();
				
		try {
			config.load(folderName + "/MCDocs/config.yml");
		} catch (FileNotFoundException e1) {
			logit("Error: MCDocs configuration file 'config.yml' was not found!");
		} catch (IOException e) {
			logit("Error: MCDocs IOException on config.yml load!");
		} catch (InvalidConfigurationException e) {
			logit("Error: Invalid Configuration");
		}
		
		headerFormat = config.getString("header-format", headerFormat);
		onlinePlayersFormat = config.getString("online-players-format", onlinePlayersFormat);
		motdEnabled = config.getBoolean("motd-enabled", motdEnabled);
		commandLogEnabled = config.getBoolean("command-log-enabled", commandLogEnabled);
		errorLogEnabled = config.getBoolean("error-log-enabled", errorLogEnabled);
		newsFile = config.getString("news-file", newsFile);
		newsLines = config.getInt("news-lines", newsLines);
		linesPerPage = config.getInt("lines-per-page", linesPerPage);
		cacheTime = config.getInt("cache-time", cacheTime);
		playerBroadcastMessageEnabled = config.getBoolean("broadcast-enabled", playerBroadcastMessageEnabled);
		
		//import our data and force find commands, and motd information.
		Map<String, Object> map = config.getValues(true);
		
		try{
			for (String key : map.keySet()){
				
				//Commands Import
				if(key.startsWith("commands.")){
					String[] split = key.split("\\.");
					if(split.length == 2){
						MCDocsCommands commandRecord = new MCDocsCommands(split[1].toString(), map.get(key + ".file").toString(), "MCDocsGlobal");
						commandsList.add(commandRecord);
					}
					if(split.length == 4){
						MCDocsCommands commandRecord = new MCDocsCommands(split[1].toString(), map.get(key).toString(), split[3].toString());
						commandsList.add(commandRecord);
					}
				}
				
				if(motdEnabled){
					//Default MOTD import
					try{
						MCDocsMOTD motdRecord = new MCDocsMOTD(map.get("motd.file").toString(), "MCDocsGlobal");
						motdList.add(motdRecord);
					}
					catch(Exception e){
						logit("motd not defined in the config. No default motd will be shown...");
					}
					
					//MOTD Import
					if(key.startsWith("motd.groups.")){
						String[] split = key.split("\\.");
						MCDocsMOTD motdGroupRecord = new MCDocsMOTD(map.get(key).toString(), split[2].toString());
						motdList.add(motdGroupRecord);
					}
				}
				
				if(playerBroadcastMessageEnabled){
					//Join Import
					try{
						MCDocsPlayerJoin joinRecord = new MCDocsPlayerJoin(map.get("join.message").toString(), "MCDocsGlobal", null);
						joinList.add(joinRecord);
					}
					catch(Exception e){
						logit("join string not defined in the config. No default join message will be shown...");
					}
					if(key.startsWith("join.groups.")){
						String[] split = key.split("\\.");
						MCDocsPlayerJoin joinGroupRecord = new MCDocsPlayerJoin(map.get(key).toString(), split[2].toString(), null);
						joinList.add(joinGroupRecord);
					}
					if(key.startsWith("join.players.")){
						String[] split = key.split("\\.");
						MCDocsPlayerJoin joinNameRecord = new MCDocsPlayerJoin(map.get(key).toString(), null, split[2].toString());
						joinList.add(joinNameRecord);
					}
					
					//Quit Import
					try{
						MCDocsPlayerQuit quitRecord = new MCDocsPlayerQuit(map.get("quit.message").toString(), "MCDocsGlobal", null);
						quitList.add(quitRecord);
					}
					catch(Exception e){
						logit("quit string not defined in the config. No default quit message will be shown...");
					}
					if(key.startsWith("quit.groups.")){
						String[] split = key.split("\\.");
						MCDocsPlayerQuit quitGroupRecord = new MCDocsPlayerQuit(map.get(key).toString(), split[2].toString(), null);
						quitList.add(quitGroupRecord);
					}
					if(key.startsWith("quit.players.")){
						String[] split = key.split("\\.");
						MCDocsPlayerQuit quitNameRecord = new MCDocsPlayerQuit(map.get(key).toString(), null, split[2].toString());
						quitList.add(quitNameRecord);
					}
				}
			}
		}
		catch(Exception e){
			logit("Your config.yml is incorrect." + e);
		}
		
		//reverse the list so that the group files are placed before the global files.
		Collections.reverse(commandsList);
	}	
	
	/*
	 * -- Main Methods --
	 * ~ onPlayerCommandPreprocess:
	 * Is checked whenever a user uses /$
	 * check to see if the user's command matches
	 * Performs a Permissions Node check - if failed, do nothing.
	 * if pass: read command's file to list lines, then forward the player, command, and page number to linesProcess.
	 *  
	 * ~ variableSwap
	 * For each line in a txt file, various %variables are replaced with their corresponding match.
	 *  
	 * ~ linesProcess:				
	 * How many lines in a document are determined, and thus how many pages to split it into.
	 * The header is loaded from header-format and the variables are replaced
	 * Finally the lines are sent to the player.
	 * 
	 * ~ onlineFile
	 * Takes in a url that is wanted to be parsed, and returns a list of lines that are to be used.
	 * It also includes a basic cache, as to not constantly request the file from the net.
	 * The cache time limit can be modified in the config.yml
	 */
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {		
		//Find the current Player, Message
		final String[] split = event.getMessage().split(" ");
		final Player player = event.getPlayer();
		boolean groupMessageSent = false;
		
		
		//Here we are going to support spaces in commands.
		int count = split.length;
		final int lastInput = count - 1;
		String playerCommand = "";
		if(checkIfNumber(split[lastInput])){
			for (int i=0; i<lastInput; i++){
				playerCommand = playerCommand + split[i] + " ";
			}
		}
		else {
			for (int i=0; i<count; i++){
				playerCommand = playerCommand + split[i] + " ";
			}
		}
		
		//Cut off the final space.
		playerCommand = playerCommand.trim();
		
		
		for (MCDocsCommands r : commandsList){
			fixedLines.clear();
                        final MCDocsCommands a = r;
			final String command = r.getCommand();
			String cleanCommand = command.replaceFirst("/", "");
			cleanCommand = cleanCommand.replaceFirst(" ", "-");
			String permission = "allow";
			
			if (playerCommand.equalsIgnoreCase(command)){
				
				String[] groupInfo = getGroupInfo(player);
				if((r.getGroup().equalsIgnoreCase(groupInfo[0])) || (r.getGroup().equals("MCDocsGlobal"))){
					permission = "allow";
				}
				else{
					permission = "deny";
				}
				
				//Bukkit Permissions
				if(!MCDocs.permission.has(player, "mcdocs.*") && !MCDocs.permission.has(player, "mcdocs.command.*")){
					if((!MCDocs.permission.has(player, "mcdocs.command." + command)) && (!MCDocs.permission.has(player, "mcdocs.command." + cleanCommand))){
						permission = "deny";
					}
				}
				
				if ((permission == "allow") && (!groupMessageSent)){
					if(!r.getGroup().equals("MCDocsGlobal")){
						groupMessageSent = true;
					}
                                        
					//Finally - Process our lines!
                                        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {

                                            @Override
                                            public void run() {
                                                
                                                //List of lines we read our first file into.
                                                ArrayList<String> lines = new ArrayList<String>();
                                                lines.clear();
                                                
                                                int page = 0;
                                                String fileName = a.getFile();
                                                fileName = basicVariableSwap(player, fileName);

					
                                                //Online file use
                                                if(fileName.startsWith("http")){
                                                	ArrayList<String> onlineLines = new ArrayList<String>();
                                                        onlineLines = onlineFile(fileName);
                                                        for(String o : onlineLines){
                                                                lines.add(o);
                                                        }
                                                }
                                                //Regular Files
                                                else{
                                                        lines = fileReader(fileName);  						
                                                }
					
					
                                                //If split[lastInput] does not exist, or has a letter, page = 1
                                                try{
                                                        page = Integer.parseInt(split[lastInput]);
                                                }
                                                catch(Exception ex){
                                                        page = 1;
                                                }  
					
					
                                                variableSwap(player, lines);
                                                linesProcess(player, command, page, false);
                                            }
                                        });
					
					if (commandLogEnabled){
						log.info("MCDocs: " + player.getName() + ": " + event.getMessage());
					}
                                        
				}
				event.setCancelled(true);
                                //Don't beed to check for other commands if the comman was already executed;
                                break;
			}
		}
	}
	
	private void variableSwap(Player player, ArrayList<String> lines){
			
		//Swapping out some variables with their respective replacement.
		for(String l : lines){
			
			//Basics
			String fixedLine = basicVariableSwap(player, l);
			
			//Time Based
			
			//--> WorldTime
                        if(fixedLine.contains("%time")){
			double worldTime = player.getWorld().getTime() + 6000;
			double relativeTime = worldTime % 24000;
			long worldHours = (long) (relativeTime / 1000);
			long worldMinutes = (long) (((relativeTime % 1000) * 0.6) / 10); //I'm assuming this is how it works. lel.
			String worldMinutesResult = "";
			String worldTimeResult = "";
			
			if(worldMinutes < 10){
				worldMinutesResult = "0" + worldMinutes;
			}
			else{
				worldMinutesResult = worldMinutes + "";
			}
			
			if(worldHours >= 12){
				worldHours -= 12;
				worldTimeResult = worldHours + ":" + worldMinutesResult + " PM";
			}
			else{
				worldTimeResult = worldHours + ":" + worldMinutesResult + " AM";
			}
                        fixedLine = fixedLine.replace("%time", worldTimeResult);
                        }
			
			
			
			//Permissions related variables
			if(fixedLine.contains("%online_")){
				String tempString = fixedLine.trim();
				String[] firstSplit = tempString.split(" ");
				for(String s : firstSplit){
					if(s.contains("%online_")){
						String[] secondSplit = s.split("_");
						String groupName = secondSplit[1].toLowerCase();
						fixedLine = fixedLine.replace("%online_" + secondSplit[1], onlineGroup(groupName));
					}
				}
			}
			
			//iConomy
			if(MCDocs.economyEnabled && fixedLine.contains("%balance")){
				try{
					fixedLine = fixedLine.replace("%balance", Double.toString(MCDocs.economy.getBalance(player.getName())));
				}
				catch(Exception e){
					logit("Warning: Vault could not find " + player.getName() + "'s balance.");
				}
			}   
			
			//More Basics
			fixedLine = (onlineNames() != null) ? fixedLine.replace("%online", onlineNames()) : fixedLine;
			fixedLine = colourSwap(fixedLine);
			fixedLine = fixedLine.replace("&#!", "&");
						
			//If the line currently in the for loop has "%include", we find out which file to load in by splitting the line up intensively.
			ArrayList<String> files = new ArrayList<String>();
				   	
			if (l.contains("%include") || l.contains("%news")){
				if (l.contains("%include")){
					String tempString = l.trim();
					String[] firstSplit = tempString.split(" ");
					for(String s : firstSplit){
						if(s.contains("%include")){
							s = " " + s;
							String[] secondSplit = s.split("%include_");
							s = s.replace(" ", "");
							fixedLine = fixedLine.replace(s, "");
							files.add(secondSplit[1]);
						}
					}
					if(!fixedLine.equals(" ")){
						fixedLines.add(fixedLine);
					}
					for(String f : files){
						includeAdd(f, player);
					}
				}
				if (l.contains("%news")){
					fixedLine = fixedLine.replace("%news", "");	
					if(!fixedLine.equals(" ")){
						fixedLines.add(fixedLine);
					}
					newsLine(player);
				}
			}
			else{
				fixedLines.add(fixedLine);
			}
		}
	}
		
	private void linesProcess(Player player, String command, int page, boolean motd){		
		//Define our page numbers
		int size = fixedLines.size();
		int pages;
		
		if(linesPerPage == 0){
			//just skipping the header.
			pages = 1;
		}
		else if(size % linesPerPage == 0){
			pages = size / linesPerPage;
		}
		else{
			pages = size / linesPerPage + 1;
		}
		
		//This here grabs the specified 9 lines, or if it's the last page, the left over amount of lines.
		String commandName = command.replace("/", "");
		commandName = commandName.toUpperCase();
		String header = null;
		
		if(pages != 1){
			//Custom Header
			header = headerFormat;
			
			//Replace variables.
			header = colourSwap(header);
			header = header.replace("%commandname", commandName);
			header = header.replace("%current", Integer.toString(page));
			header = header.replace("%count", Integer.toString(pages));
			header = header.replace("%command", command);
			header = header + " ";
			
			player.sendMessage(header);
		}
		//Some math, magic, and wizards.
		if(linesPerPage == 0){
			for(String line : fixedLines){
				player.sendMessage(line);
			}
		}
		else{
			int highNum = (page * linesPerPage);
			int lowNum = (page - 1) * linesPerPage;
			for (int number = lowNum; number < highNum; number++){
				if(number >= size){
					if(!motd && pages != 1){
						player.sendMessage(" ");
					}
				}
				else{
					player.sendMessage(fixedLines.get(number));	 
				}
					   	
			}
		}
	}
	
	private String[] getGroupInfo(Player player){
		
		String group = "";
		String prefix = "";
		String suffix = "";
		
		//Relying on Vault isn't always that great.
		try{group = MCDocs.permission.getPrimaryGroup(player);}catch(Exception e){}
		try{prefix = MCDocs.chat.getPlayerPrefix(player);}catch(Exception e){}
		try{suffix = MCDocs.chat.getPlayerSuffix(player);}catch(Exception e){}
		
		//Seriously. Can't rely on it.
		group = (group != null) ? group : "";
		prefix = (prefix != null) ? prefix : "";
		suffix = (suffix != null) ? suffix : "";
		
		String[] ret = {group, prefix, suffix};
		return ret;
	}
	
	private ArrayList<String> onlineFile(String url){
		
		//Some variables for the method
		MCDocsOnlineFiles file = null;
		ArrayList<String> onlineLines = new ArrayList<String>();
		
		URL u;
		InputStream is = null;
		DataInputStream dis;
		Date now = new Date();
		long nowTime = now.getTime();
		int foundFile = 0;
		
		//create a new list to store our wanted objects
		ArrayList<MCDocsOnlineFiles> newOnlineFiles = new ArrayList<MCDocsOnlineFiles>();
		
		//go through all the existing online files found in the cache, and check if they are still under the cache limit.
		//delete all files, and entries, that are old.
		for(MCDocsOnlineFiles o : onlineFiles){
			long fileTimeMs = o.getMs();
			long timeDif = nowTime - fileTimeMs;
			int cacheTimeMs = cacheTime * 60 * 1000;
						
			if(timeDif > cacheTimeMs){
				File f = new File(plugin.getDataFolder(), "cache/" + fileTimeMs + ".txt");
				f.delete();
			}
			else{
				//add the objects we wish to keep.
				newOnlineFiles.add(o);
			}
		}
		
		//clear out the old, and replace them with the objects we wanted to keep.
		onlineFiles.clear();
		for(MCDocsOnlineFiles n : newOnlineFiles){
			onlineFiles.add(n);
		}

		//now sinply go through our cache files and check if our url has been cached before...
		for(MCDocsOnlineFiles o : onlineFiles){
			if(o.getURL().equalsIgnoreCase(url)){
				String fileName = "cache/" + Long.toString(o.getMs()) + ".txt";
				onlineLines = fileReader(fileName);
				foundFile = 1;
			}
		}
		
		//finally if there was no cache, or the url was not in the cache, download the online file and cache it.
		if(foundFile == 0){
			try{
				//import our online file
				u = new URL(url);
				is = u.openStream();  
				dis = new DataInputStream(new BufferedInputStream(is));
				Scanner scanner = new Scanner(dis, "UTF-8");
				while (scanner.hasNextLine()) {
					try{
						onlineLines.add(scanner.nextLine() + " ");
					}
					catch(Exception ex){
						onlineLines.add(" ");
					}
				}	
				scanner.close();
				dis.close();
				
				//Add our new file to the cache
				file = new MCDocsOnlineFiles(nowTime, url);
				onlineFiles.add(file);
				
				//save our new file to the dir
				PrintWriter stream = null;
				File folder = plugin.getDataFolder();
				String folderName = folder.getParent();		
				try {
					new File(plugin.getDataFolder() + "/cache/").mkdir();
					stream = new PrintWriter(folderName + "/MCDocs/cache/" + nowTime + ".txt");
					for(String l : onlineLines){
						stream.println(l);
					}
					stream.close();
				} catch (FileNotFoundException e) {
					log.info("[MCDocs]: Error saving " + nowTime + ".txt");
				}
				
			}
			catch (MalformedURLException mue) {
				log.info("[MCDocs] Ouch - a MalformedURLException happened.");
			}
			catch (IOException ioe) {
				log.info("[MCDocs] Oops - an IOException happened.");
			}
			finally {
				 try {
					is.close();
				 } 
				 catch (IOException ioe) {
			 	}
			}
		}
		//and finally return what we have found.
		return onlineLines;
	}
	
	private ArrayList<String> fileReader(String fileName){
		
		ArrayList<String> tempLines = new ArrayList<String>();
		String folderName = plugin.getDataFolder().getParent();
		
                if(cache.exist(fileName)){
                    return cache.get(fileName);
                } else {
                    try{
                            FileInputStream fis = new FileInputStream(folderName + "/MCDocs/" + fileName);
                            Scanner scanner = new Scanner(fis, "UTF-8");
				while (scanner.hasNextLine()) {
					try{
						tempLines.add(scanner.nextLine() + " ");
					}
					catch(Exception ex){
						tempLines.add(" ");
					}
				}
                            scanner.close();
                            fis.close();
                    }
                    catch (Exception ex){
			logit("Error: File '" + fileName + "' could not be read.");
                    }
                    cache.add(fileName, tempLines);
                    return tempLines;
              }		
	}
	
	public void logit(String message){
		if(errorLogEnabled){
			log.info("[MCDocs] " + message);
		}				
	}
	
	/*
	 * -- Variable Methods --
	 * The following methods are used for various %variables in the txt files.
	 * 
	 * basicVariableSwap: Used for dynamic file names.
	 * includeAdd: Is used to insert more lines into the current working doc.
	 * onlineNames: Finds the current online players, and using online-players-format, applies some permissions variables.
	 * onlineGroup: Finds the current online players, check if they're in the group specified, and using online-players-format, applies some permissions variables.
	 * onlineCount: Returns the current amount of users online.
	 * newsLine: Is used to insert the most recent lines (# defined in config.yml) from the defined news file (defined in the config.yml)
	 * checkIfNumber: simple try catch to determine if a space is in a command. Example: /help iconomy 2
	 * colorSwap: Uses the API to colour swap instead of manually doing it.
	 */
	
	private String basicVariableSwap(Player player, String string){
		
		String[] groupInfo = getGroupInfo(player);
		
		string = (player.getName() != null) ? string.replace("%name", player.getName()) : string;
		string = (player.getName() != null) ? string.replace("%displayname", player.getDisplayName()) : string;
		string = (onlineCount() != null) ? string.replace("%size", onlineCount()) : string;
		string = (player.getWorld().getName() != null) ? string.replace("%world", player.getWorld().getName()) : string;
		string = (player.getAddress().getAddress().getHostAddress() != null) ? string.replace("%ip", player.getAddress().getAddress().getHostAddress()) : string;  	
		string = string.replace("%group", groupInfo[0]);
		string = string.replace("%prefix", groupInfo[1]);
		string = string.replace("%suffix", groupInfo[2]);
		string = locationSwap(player, string);
		string = (this.plugin.getServer().getBukkitVersion() != null) ? string.replace("%server_version", this.plugin.getServer().getBukkitVersion()) : string;
		string = (this.plugin.getServer().getIp() != null) ? string.replace("%server_ip", this.plugin.getServer().getIp()) : string;
		string = (Integer.toString(this.plugin.getServer().getPort()) != null) ? string.replace("%server_port", Integer.toString(this.plugin.getServer().getPort())) : string;
		string = (Integer.toString(this.plugin.getServer().getMaxPlayers()) != null) ? string.replace("%server_max", Integer.toString(this.plugin.getServer().getMaxPlayers())) : string;
		string = (this.plugin.getServer().getServerName() != null) ? string.replace("%server_name", this.plugin.getServer().getServerName()) : string;		
		
		return string;
	}
	
	private void includeAdd(String fileName, Player player){
		//Define some variables
		ArrayList<String> tempLines = new ArrayList<String>();
		
		//Ok, let's import our new file, and then send them into another variableSwap [ I   N   C   E   P   T   I   O   N ]
		try {
			//Online file use
			if(fileName.startsWith("http")){
				ArrayList<String> onlineLines = new ArrayList<String>();
				onlineLines = onlineFile(fileName);
				for(String o : onlineLines){
					tempLines.add(o);
				}
			}
			//Regular files
			else{
				tempLines = fileReader(fileName);
			}
			//Methods inside of methods!
			variableSwap(player, tempLines);
		 }					
		 catch (Exception ex) {
			 logit("Included file " + fileName + " not found!");
		 }
	}
	
	private String onlineNames(){
		Player online[] = plugin.getServer().getOnlinePlayers();
		String onlineNames = null;
		String nameFinal = null;
		for (Player o : online){
			
			nameFinal = (o.getDisplayName() != null) ?onlinePlayersFormat.replace("%name", o.getDisplayName()) : nameFinal;
			
			String[] groupInfo = getGroupInfo(o);
			nameFinal = nameFinal.replace("%group", groupInfo[0]);
			nameFinal = nameFinal.replace("%prefix", groupInfo[1]);
			nameFinal = nameFinal.replace("%suffix", groupInfo[2]);
			
			nameFinal = colourSwap(nameFinal);
			
			if (onlineNames == null){
				onlineNames = nameFinal;
			}
			else{
				onlineNames = onlineNames.trim() + "&f, " + nameFinal;
			}
			
		}
		return onlineNames;
	}

	private String onlineGroup(String group){
		Player online[] = plugin.getServer().getOnlinePlayers();
		String onlineGroup = null;
		String nameFinal = null;
		for (Player o : online){
			String groupInfo[] = getGroupInfo(o);
			if (groupInfo[0].toLowerCase().equals(group)){

					nameFinal = onlinePlayersFormat.replace("%name", o.getDisplayName());
					nameFinal = nameFinal.replace("%group", groupInfo[0]);
					nameFinal = nameFinal.replace("%prefix", groupInfo[1]);
					nameFinal = nameFinal.replace("%suffix", groupInfo[2]);
					nameFinal = colourSwap(nameFinal);


				if (onlineGroup == null){
					onlineGroup = nameFinal;
				}
				else{
					onlineGroup = onlineGroup + "&f, " + nameFinal;
				}
			}
		}
		if (onlineGroup == null){
			onlineGroup = "";
		}
		return onlineGroup;
	}
	
	private String onlineCount(){
		Player online[] = plugin.getServer().getOnlinePlayers();
		int onlineCount = online.length;
		return Integer.toString(onlineCount);
	}	
	
	private void newsLine(Player player){
		
		ArrayList<String> newsLinesList = new ArrayList<String>();
		File folder = plugin.getDataFolder();
		String folderName = folder.getParent();
		int current = 0;
		
		try {
			FileInputStream fis = new FileInputStream(folderName + "/MCDocs/" + newsFile);
			Scanner scanner = new Scanner(fis, "UTF-8");
				while (current < newsLines) {
					try{
						newsLinesList.add(scanner.nextLine() + " ");
					}
					catch(Exception ex){
						newsLinesList.add(" ");
					}
					current++;
				}
			scanner.close();
			fis.close();  
			variableSwap(player, newsLinesList);
		}					
		catch (Exception ex) {
		 logit("news-file was not found.");
		}
	}
		
	private boolean checkIfNumber(String in) {
		
		try {
			Integer.parseInt(in);
		}
		catch (NumberFormatException ex) {
			return false;
		}
		
		return true;
	}
	
	private String colourSwap(String line){
		String[] Colours = { 	"&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7",
								"&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f",
								"[color=black]", "[color=darkblue]", "[color=darkgreen]", "[color=darkaqua]", "[color=darkred]", "[color=darkpurple]", "[color=gold]", "[color=gray]",
								"[color=darkgray]", "[color=blue]", "[color=green]", "[color=aqua]", "[color=red]", "[color=lightpurple]", "[color=yellow]", "[color=white]", "[color=magic]",  "[/color]",
								"[b]", "[s]", "[u]", "[i]",
								"[/b]", "[/s]", "[/u]", "[/i]",
							  };
		ChatColor[] cCode = {	ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
								ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE,
								ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
								ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE, ChatColor.MAGIC, ChatColor.WHITE,
								ChatColor.BOLD, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.ITALIC,
								ChatColor.RESET, ChatColor.RESET, ChatColor.RESET, ChatColor.RESET,
							  };
		
		for (int x = 0; x < Colours.length; x++) {
			CharSequence cChkU = null;
			CharSequence cChkL = null;

			cChkU = Colours[x].toUpperCase();
			cChkL = Colours[x].toLowerCase();
			if (line.contains(cChkU) || line.contains(cChkL)) {
				line = line.replace(cChkU, cCode[x].toString());
				line = line.replace(cChkL, cCode[x].toString());
			}
		}
		return line;
	}
	
	private String locationSwap(Player player, String line){
		
		if (this.plugin.getServer().getPluginManager().getPlugin("GeoIPTools") != null) {
			Plugin GeoIPTools = this.plugin.getServer().getPluginManager().getPlugin("GeoIPTools");
			geoIP = ((GeoIPTools) GeoIPTools).getGeoIPLookup();
			
			String country = "";
			
			try{
				country = geoIP.getCountry(player.getAddress().getAddress()).getName();
			}
			catch(Exception e){
				logit("GeoIPTools has thrown an exception. Perhaps update it?");
			}
			if(country == "N/A"){
				country = "Unknown"; 
			}
			
			line = (country != null) ? line.replace("%country", country) : line;			
		}
		
		return line;
		
	}
	
	/*
	 * -- On Player Join // Quit -- 
	 * Message of the day, and server announcements for player join and quit.
	 */
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
                final Player player = event.getPlayer();
		if(motdEnabled){
                    Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {

                        @Override
                        public void run() {
                            motdProcess(player);    
                        }
                    });
		}
		if(playerBroadcastMessageEnabled){
			String message = null;			
			String[] group = getGroupInfo(player);
			
			for(MCDocsPlayerJoin j : joinList){
				if(group[0].equalsIgnoreCase(j.getGroup())){
					message = j.getMessage();
                                        break;
				}
			}
			
			//Iterate the players last, as to prioritise over groups.
			for(MCDocsPlayerJoin j : joinList){
				if(player.getName().equalsIgnoreCase(j.getPlayerName())){
					message = j.getMessage();
                                        break;
				}
			}
			
			if(message == null){
				message = joinList.get(0).getMessage();
			}
			
			message = basicVariableSwap(player, message);
			message = colourSwap(message);
			event.setJoinMessage(message);
		}
	}
	
	@EventHandler	
	public void onPlayerQuit(PlayerQuitEvent event){
				
		if(playerBroadcastMessageEnabled){
			
			Player player = event.getPlayer();
			String message = null;			
			String[] group = getGroupInfo(player);
			
			for(MCDocsPlayerQuit q : quitList){
				if(group[0].equalsIgnoreCase(q.getGroup())){
					message = q.getMessage();
                                        break;
				}
			}
			
			//Iterate the players last, as to prioritise over groups.
			for(MCDocsPlayerQuit q : quitList){
				if(player.getName().equalsIgnoreCase(q.getPlayerName())){
					message = q.getMessage();
                                        break;
				}
			}
			
			if(message == null){
				message = quitList.get(0).getMessage();
			}
			
			message = basicVariableSwap(player, message);
			message = colourSwap(message);
			event.setQuitMessage(message);
		}
	}
	
	private void motdProcess(Player player){
		ArrayList<String> lines = new ArrayList<String>();
		lines.clear();
		fixedLines.clear();
		
		String[] group = getGroupInfo(player);
		String fileName = null;
		
		for(MCDocsMOTD m : motdList){
			if(group[0].equalsIgnoreCase(m.getGroup())){
				fileName = m.getFile();
                                break;
			}
		}
		
		if(fileName == null){
			fileName = motdList.get(0).getFile();
		}
		
		fileName = basicVariableSwap(player, fileName);
		
		//Online file use
		if(fileName.startsWith("http")){
			ArrayList<String> onlineLines = new ArrayList<String>();
			onlineLines = onlineFile(fileName);
			for(String o : onlineLines){
				lines.add(o);
			}
		}
		//Regular Files
		else{
			lines = fileReader(fileName);			  
		}
		variableSwap(player, lines);
		linesProcess(player, "/motd", 1, true);
	}
}
