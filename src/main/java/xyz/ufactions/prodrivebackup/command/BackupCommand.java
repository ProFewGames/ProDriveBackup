package xyz.ufactions.prodrivebackup.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import xyz.ufactions.prodrivebackup.ProDriveBackup;
import xyz.ufactions.prodrivebackup.thread.UploadThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BackupCommand implements CommandExecutor, TabExecutor {

    private final ProDriveBackup plugin;

    public BackupCommand(ProDriveBackup plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(plugin.format("Reloading..."));
                plugin.reload();
                sender.sendMessage(plugin.format("Reloaded!"));
                return true;
            }
            if (args[0].equalsIgnoreCase("backup")) {
                if (plugin.isBackupInProgress()) {
                    sender.sendMessage(plugin.format("&cThere is already a backup in progress."));
                    return true;
                }
                sender.sendMessage(plugin.format("Creating backup..."));
                new UploadThread(uploaded -> {
                    if (uploaded) {
                        sender.sendMessage(plugin.format("Backup success!"));
                    } else {
                        sender.sendMessage(plugin.format("&cBackup failed!"));
                    }
                }, plugin).start();
                return true;
            }
        }
        sender.sendMessage(plugin.line);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/" + label + " backup &bCreate and upload a backup."));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/" + label + " reload &bReload the plugin."));
        sender.sendMessage(plugin.line);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return getMatches(args[0], Arrays.asList("backup", "reload"));
        }
        return Collections.EMPTY_LIST;
    }

    private List<String> getMatches(String start, List<String> possibleMatches) {
        if (start.isEmpty()) return possibleMatches;
        List<String> matches = new ArrayList<>();
        for (String possibleMatch : possibleMatches) {
            if (possibleMatch.toLowerCase().startsWith(start.toLowerCase()))
                matches.add(possibleMatch);
        }
        return matches;
    }
}