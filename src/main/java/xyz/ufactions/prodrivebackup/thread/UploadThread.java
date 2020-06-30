package xyz.ufactions.prodrivebackup.thread;

import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.Bukkit;
import xyz.ufactions.libs.Callback;
import xyz.ufactions.prodrivebackup.ProDriveBackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UploadThread extends Thread {

    private final ProDriveBackup plugin;
    private final Callback<Boolean> callback;

    public UploadThread(Callback<Boolean> callback, ProDriveBackup plugin) {
        super("Backup Upload");

        this.plugin = plugin;
        this.callback = callback;
    }

    @Override
    public void run() {
        if (plugin.isBackupInProgress()) return;
        plugin.setBackupInProgress(true);
        try {
            File file = zip();
            upload(file);
            callback.run(true);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(plugin.format("&cThere was an error whilst backing up the server."));
            e.printStackTrace();
            callback.run(false);
        }
        plugin.setBackupInProgress(false);
    }

    /**
     * Create a backup zip file.
     */
    private File zip() throws IOException {
        if (plugin.getConfigurationFile().verbose()) {
            Bukkit.getConsoleSender().sendMessage(plugin.format("Creating ZIP..."));
        }
        DateFormat date = new SimpleDateFormat(plugin.getConfigurationFile().getDateFormat());
        File out = new File(plugin.getConfigurationFile().getBackupDirectory(), "backup-" + date.format(new Date()) + ".zip");
        try (FileOutputStream fos = new FileOutputStream(out); ZipOutputStream zos = new ZipOutputStream(fos)) {
            File directory = new File(".");
            for (File file : populate(directory)) { // Will zip everything
                if (plugin.getConfigurationFile().verbose()) {
                    Bukkit.getConsoleSender().sendMessage(plugin.format("Archiving: " + file.getPath()));
                }
                ZipEntry entry = new ZipEntry(file.getPath().substring(directory.getPath().length() + 1));
                zos.putNextEntry(entry);

                try (FileInputStream fis = new FileInputStream(file.getPath())) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
        if (plugin.getConfigurationFile().verbose()) {
            Bukkit.getConsoleSender().sendMessage(plugin.format("ZIP created!"));
        }
        return out;
    }

    private List<File> populate(File directory) {
        List<File> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile())
                files.add(file);
            else {
                if (file.getPath().substring(directory.getPath().length() + 1).equals(plugin.getConfigurationFile().getBackupDirectory().getPath()))
                    continue; // DON'T BACKUP THE BACKUP!
                if (file.getPath().substring(directory.getPath().length() + 1).equals(plugin.getConfigurationFile().getConfig().getCurrentPath()))
                    continue; // DON'T BACKUP THE CONFIGURATION
                files.addAll(populate(file));
            }
        }
        return files;
    }

    /**
     * Upload the file to the drive
     *
     * @param file The file we're uploading
     */
    private void upload(File file) throws IOException {
        if (plugin.getConfigurationFile().verbose()) {
            Bukkit.getConsoleSender().sendMessage(plugin.format("Uploading ZIP..."));
        }

        URL url = new URL(plugin.getConfigurationFile().getHost());

        Sardine sardine = new SardineImpl(plugin.getConfigurationFile().getUsername(), plugin.getConfigurationFile().getString("upload.password")) {

            @Override
            protected HttpClientBuilder configure(ProxySelector selector, CredentialsProvider credentials) {
                return super.configure(selector, credentials).setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
            }
        };

        sardine.enablePreemptiveAuthentication(url);
        sardine.put(url + file.getName(), FileUtils.readFileToByteArray(file));

        if (plugin.getConfigurationFile().verbose()) {
            Bukkit.getConsoleSender().sendMessage(plugin.format("Upload Complete!"));
        }
    }
}