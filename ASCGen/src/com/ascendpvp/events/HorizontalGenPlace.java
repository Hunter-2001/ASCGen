package com.ascendpvp.events;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.ascendpvp.ASCGenMain;

public class HorizontalGenPlace implements Listener {

	ASCGenMain plugin;
	public HorizontalGenPlace(ASCGenMain plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent e) {

		Player p = e.getPlayer();
		Double pBalance = ASCGenMain.econ.getBalance(p.getName());
		double price = plugin.getConfig().getDouble("horizontal_per_place_price");
		String printPrice = String.valueOf(price);
		
		//Basic checks
		if(e.isCancelled()) return;
		if(p.getItemInHand() == null || p.getItemInHand().getItemMeta() == null || p.getItemInHand().getItemMeta().getDisplayName() == null) return;
		if (!e.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(cc(plugin.getConfig().getString("horizontal_gen_name")))) return;
		if(!(pBalance >= price)) {
			e.setCancelled(true);
			p.sendMessage(cc(plugin.getConfig().getString("messages.not_enough_money_per_place")));
			return;
		}

		p.sendMessage(cc(plugin.getConfig().getString("messages.place_success").replace("#pricePerGen#", printPrice)));
		ASCGenMain.econ.withdrawPlayer(p.getName(), price);
		BlockFace pDirection = getDirection(p);
		//Create runnable to begin generating blocks away from player look vector
		new BukkitRunnable() {
			int x = 0;
			@Override
			public void run() {
				x++;
				//Checks to determine whether gen should continue
				if(x == plugin.getConfig().getInt("horizontal_distance")) {
					cancel();
					return;
				}
				Block newBlock = e.getBlock().getRelative(pDirection.getOppositeFace(), x);
				int radius = 2;
				if (newBlock.getType() != Material.AIR) {
					cancel();
					return;
				}
				//For loop to determine whether there is a sponge within a 5x5 radius of the next block queued
				for (int fx = -radius; fx <= radius; fx++) {
					for (int fy = -radius; fy <= radius; fy++) {
						for (int fz = -radius; fz <= radius; fz++) {
							if (newBlock.getRelative(fx, fy, fz).getType() == Material.SPONGE) {

								cancel();
								return;
							}
						}
					}
				}
				newBlock.setType(Material.COBBLESTONE);
			}
		}.runTaskTimer(plugin, plugin.getConfig().getLong("flow_speed"), plugin.getConfig().getLong("flow_speed"));
	}

	/*
	 * Method to determine player vector/direction 
	 */
	private static final Set<BlockFace> FACES = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
	public BlockFace getDirection(Player player) {
		Vector direction = player.getLocation().getDirection();
		return FACES.stream()
				.max(Comparator.comparingDouble(f -> new Vector(f.getModX(), f.getModY(), f.getModZ()).normalize().dot(direction)))
				.orElse(null);
	}
	public String cc(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}
