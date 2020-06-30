package xyz.ufactions.prodrivebackup;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.ufactions.prodrivebackup.command.BackupCommand;
import xyz.ufactions.prodrivebackup.file.ConfigurationFile;
import xyz.ufactions.prodrivebackup.thread.UploadThread;

public class ProDriveBackup extends JavaPlugin {

    private int backupID = -1;

    private ConfigurationFile configurationFile;
    private boolean backupInProgress = false;

    public final String line = ChatColor.translateAlternateColorCodes('&', "&3&m--------------------------------------------");

    @Override
    public void onEnable() {
        this.configurationFile = new ConfigurationFile(this);
        BackupCommand command = new BackupCommand(this);
        getCommand("prodrivebackup").setExecutor(command);
        getCommand("prodrivebackup").setTabCompleter(command);

        rescheduleBackup();
    }

    public void reload() {
        configurationFile.reload();
        rescheduleBackup();
    }

    private void rescheduleBackup() {
        if (configurationFile.getBackupInterval() <= 0) {
            getLogger().info("Backup not scheduling (Interval <= 0) * This is not a bug, edit backup.interval in your configuration to re-enable *");
            return;
        }
        if (backupID != -1) {
            getServer().getScheduler().cancelTask(backupID);
            getLogger().info("Previous Scheduler Cancelled.");
        }
        backupID = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getLogger().info("Creating backup...");
            new UploadThread(uploaded -> {
                if (uploaded) {
                    getLogger().info("Backup Complete.");
                } else {
                    getLogger().warning("Backup Failed!");
                }
            }, this).start();
        }, configurationFile.getBackupInterval(), configurationFile.getBackupInterval());
        getLogger().info("Backup Task Scheduled");
    }

    public String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', "&7[&3ProDriveBackup&7] &b" + message);
    }

    public ConfigurationFile getConfigurationFile() {
        return configurationFile;
    }

    public boolean isBackupInProgress() {
        return backupInProgress;
    }

    public void setBackupInProgress(boolean backupInProgress) {
        this.backupInProgress = backupInProgress;
    }
}