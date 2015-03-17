package net.kaikk.mc.hqmeditor;

import hardcorequesting.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.entity.player.EntityPlayer;

public class HQMeditor extends JavaPlugin {
	ArrayList<UUID> justReset = new ArrayList<UUID>();
	
	public void onEnable() {
		int count=0;
		
		for (OfflinePlayer player : this.getServer().getOfflinePlayers()) {
			EntityPlayer eplayer = hardcorequesting.QuestingData.getPlayer(player.getName());
			if (eplayer!=null && player.getLastPlayed()<(System.currentTimeMillis()-604800000)) {
				hardcorequesting.QuestingData.remove(eplayer);
				count++;
			}
		}
		
		if (count>0) {
			getLogger().info("Removed HQM data for "+count+" player(s) for inactivity.");
		}
	}
	
	public void onDisable() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player=null;
		
		if ((sender instanceof Player)) {
			player = (Player) sender;
		}
		
		if (cmd.getName().equalsIgnoreCase("hqmreset") && player!=null) {
			if (!player.hasPermission("hqmreset")) {
				player.sendMessage("No permission!");
				return false;
			}
			
			if (justReset.contains(player.getUniqueId())) {
				player.sendMessage("You won't be able to reset HQM data for a while!");
				return false;
			}
			
			if (hardcorequesting.QuestingData.hasData(player.getName())) {
				EntityPlayer eplayer = hardcorequesting.QuestingData.getPlayer(player.getName());
				hardcorequesting.QuestingData.remove(eplayer);
				justReset.add(player.getUniqueId());
				player.sendMessage("Your HQM data has been deleted!");
				return true;
			}
			
			player.sendMessage("No HQM data found!");
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("hqmeditor")) {
			if (!player.hasPermission("hqmeditor")) {
				player.sendMessage("No permission!");
				return false;
			}
			
			if (args[0].equalsIgnoreCase("delete")) {
				if (args.length<2) {
					sender.sendMessage("Usage: /hqmeditor [teamlist|delete|deleteteam] [name]");
					return false;
				}
				EntityPlayer eplayer = hardcorequesting.QuestingData.getPlayer(args[1]);
				if (eplayer==null) {
					sender.sendMessage("Wrong name!");
					return true;
				}
				
				hardcorequesting.QuestingData.remove(eplayer);
				return true;
			}
			
			if (args[0].equalsIgnoreCase("deleteteam")) {
				if (args.length<2) {
					sender.sendMessage("Usage: /hqmeditor [teamlist|delete|deleteteam] [name]");
					return false;
				}
				
				List<Team> teams = hardcorequesting.QuestingData.getTeams();
				String name = mergeStringArrayFromIndex(args, 1);

				boolean found=false;
				for(Team team : teams) {
					if (team.getName().equalsIgnoreCase(name)) {
						team.deleteTeam();
						found=true;
						break;
					}
				}
				
				if (found) {
					sender.sendMessage("Team "+name+" deleted");
					return true;
				} else {
					sender.sendMessage("Team "+name+" not found");
					return false;
				}
			}
			
			if (args[0].equalsIgnoreCase("teamlist")) {
				List<Team> teams = hardcorequesting.QuestingData.getTeams();
				
				if (args.length==1) {
					sender.sendMessage("Teams list (team name - owner)");
					for(Team team : teams) {
						Team.PlayerEntry owner=null;
						for(Team.PlayerEntry playerEntry : team.getPlayers()) {
							if (playerEntry.isOwner()) {
								owner=playerEntry;
								break;
							}
						}
						sender.sendMessage(team.getName()+" - "+owner.getName());
					}
					return true;
				}
				
				String name = mergeStringArrayFromIndex(args, 1);

				Team team=null;
				
				for(Team rowTeam : teams) {
					if (rowTeam.getName().equalsIgnoreCase(name)) {
						team=rowTeam;
						break;
					}
				}
				
				if (team!=null) {
					sender.sendMessage("Team "+name+" players list");
					for (Team.PlayerEntry playerEntry : team.getPlayers()) {
						player.sendMessage((playerEntry.isOwner()?"*":"")+playerEntry.getName());
					}
					
					return true;
				} else {
					sender.sendMessage("Team "+name+" not found");
					return false;
				}
			}
		}
		
		return false; 
	}
	
	
	/** This static method will merge an array of strings from a specific index 
	 * @return null if arrayString.length < i*/
	static String mergeStringArrayFromIndex(String[] arrayString, int i) {
		if (i<arrayString.length){
			String string=arrayString[i];
			i++;
			for(;i<arrayString.length;i++){
				string=string+" "+arrayString[i];
			}
			return string;
		}
		return null;
	}
}
