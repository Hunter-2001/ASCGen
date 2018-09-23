package com.ascendpvp;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.ascendpvp.events.CobbleGenPlace;
import com.ascendpvp.events.HorizontalGenPlace;
import com.ascendpvp.events.ObbyGenPlace;

import net.milkbowl.vault.economy.Economy;

public class ASCGenMain extends JavaPlugin {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Economy econ = null;

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new CobbleGenPlace(this), this);
		Bukkit.getPluginManager().registerEvents(new ObbyGenPlace(this), this);
		Bukkit.getPluginManager().registerEvents(new HorizontalGenPlace(this), this);
		saveDefaultConfig();
		setupEconomy();
	}
	
	//Eco setup
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) return false;
		econ = rsp.getProvider();
		return econ != null;
	}
	public static Economy getEcononomy() {
		return econ;
	}
}
