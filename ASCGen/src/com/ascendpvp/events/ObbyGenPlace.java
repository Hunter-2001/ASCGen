package com.ascendpvp.events;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.ascendpvp.ASCGenMain;

public class ObbyGenPlace implements Listener {

	ASCGenMain plugin;
	public ObbyGenPlace(ASCGenMain plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent e) {

		Player p = e.getPlayer();
		Double pBalance = ASCGenMain.econ.getBalance(p.getName());
		double price = plugin.getConfig().getDouble("obby_per_place_price");
		String printPrice = String.valueOf(price);

		//Basic checks
		if(e.isCancelled()) return;
		if(p.getItemInHand() == null || p.getItemInHand().getItemMeta() == null || p.getItemInHand().getItemMeta().getDisplayName() == null) return;
		if (!p.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("obby_gen_name")))) return;
		if(!(pBalance >= price)) {
			e.setCancelled(true);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not_enough_money_per_place")));
		}

		p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.place_success").replace("#pricePerGen#", printPrice)));
		ASCGenMain.econ.withdrawPlayer(p.getName(), price);
		//Create runnable to begin generating blocks downwards
		new BukkitRunnable() {
			int x = 0;
			@Override
			public void run() {
				x--;
				Location newblock = e.getBlock().getLocation().add(0, x, 0);
				int bx = newblock.getBlockX();
				int by = newblock.getBlockY();
				int bz = newblock.getBlockZ();
				int radius = 2;
				//For loop to determine whether there is a sponge within a 5x5 radius of the next block queued
				for (int fx = -radius; fx <= radius; fx++) {
					for (int fy = -radius; fy <= radius; fy++) {
						for (int fz = -radius; fz <= radius; fz++) {
							if (newblock.getWorld().getBlockAt(bx + fx, by + fy, bz + fz).getType().equals(Material.SPONGE) || !newblock.getBlock().getType().equals(Material.AIR)) {
								cancel();
								return;
							}
						}
					}
				}
				newblock.getBlock().setType(Material.OBSIDIAN);
			}
		}.runTaskTimer(plugin, plugin.getConfig().getLong("flow_speed"), plugin.getConfig().getLong("flow_speed"));
	}
}
