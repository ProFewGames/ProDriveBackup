package xyz.ufactions.prodrivebackup.file;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.ufactions.libs.FileHandler;

import java.io.File;

public class ConfigurationFile extends FileHandler {

    private File backupDirectory;

    public ConfigurationFile(JavaPlugin plugin) {
        super(plugin, "config.yml", plugin.getDataFolder(), "config.yml");
    }

    public File getBackupDirectory() {
        return backupDirectory;
    }

    public long getBackupInterval() {
        return getLong("backup.interval", 1728000);
    }

    public boolean verbose() {
        return getBoolean("verbose", true);
    }

    public String getDateFormat() {
        return getString("date-format", "Backup-d-M-yyyy--HH-mm");
    }

    public String getHost() {
        return getString("upload.host", "https://prodrive.green-bull.in/");
    }

    public String getUsername() {
        return getString("upload.username", "GreenUser");
    }

    @Override
    public void onReload() {
        backupDirectory = new File(getString("backup.directory", "backup"));
        if (!backupDirectory.exists()) {
            backupDirectory.mkdir();
        }
    }
}