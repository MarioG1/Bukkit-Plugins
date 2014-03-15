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

//Java Import
import java.util.logging.Logger;

//Bukkit Import
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

//Vault Import
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

/**
 * MCDocs Plugin for Bukkit
 * 
 * @author Tazzernator
 *(Andrew Tajsic - atajsicDesigns - http://atajsic.com)
 *
 */

public class MCDocs extends JavaPlugin {
	
	//Vault
	public static Permission permission = null;
	public static Economy economy = null;
	public static Chat chat = null;
	
	public static Boolean economyEnabled = false;
	  
	//Listener, Logger, Config.
	private final MCDocsUtil playerListener = new MCDocsUtil(this);
	public static final Logger log = Logger.getLogger("Minecraft");
	FileConfiguration config;	
	
	
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "] (Tazzernator/Andrew Tajsic) - v" + pdfFile.getVersion() + " shutdown.");
		getServer().getPluginManager().disablePlugin(this);
	}
	
	public void onEnable() {
		
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log.info("[MCDocs] - ERROR: MCDocs requires Vault as a dependency. MCDocs has been disabled!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		try{
			setupPermissions();
		}
		catch(Exception e){
			log.info("[MCDocs] ERROR: Vault failed to load a permission scheme!");
		}
		
		try{
			setupChat();
		}
		catch(Exception e){
			log.info("[MCDocs] ERROR: Vault failed to load the chat scheme!");
		}
		
		try{
			setupEconomy();
			economyEnabled = true;
		}
		catch(Exception e){
			log.info("[MCDocs] WARNING: No Economy plugin found..");
		}
		
				
		config = this.getConfig();
		this.playerListener.setupConfig(config);
		
		//Register our events [Changed for R5 March 2012]
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.playerListener, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName() + "] (Tazzernator/Andrew Tajsic) - v" + pdfFile.getVersion() + " loaded.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PluginDescriptionFile pdfFile = this.getDescription();
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
	 
		if (cmd.getName().equalsIgnoreCase("mcdocs")){
			try{
				if(args[0].equalsIgnoreCase("-reload")){
					if(player == null){
						this.playerListener.loadConfig();
						this.playerListener.logit("MCDocs has been reloaded through console");
						return true;
					}
					else if(MCDocs.permission.has(player, "mcdocs.reload") || MCDocs.permission.has(player, "mcdocs.*") || player.isOp()){
						this.playerListener.loadConfig();
						player.sendMessage("MCDocs has been reloaded.");
						this.playerListener.logit("Reloaded by " + player.getName());
						return true;
					}
					return true;
				}
			}
			catch(Exception e){
				sender.sendMessage("MCDocs version " + pdfFile.getVersion() + " by (Tazzernator/Andrew Tajsic)");
			}
		}
		return false;
	}


	
	private Boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) {
		  permission = (Permission)permissionProvider.getProvider();
		}
		if (permission != null) return Boolean.valueOf(true); return Boolean.valueOf(false);
	}
	
	private Boolean setupChat()
	{
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}

		return (chat != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		economy = (Economy)economyProvider.getProvider();
		return economy != null;
	}
	
}
	
