package nl.tabuu.headz;

import nl.tabuu.headz.commands.HeadZCommand;
import nl.tabuu.headz.data.HeadDatabase;
import nl.tabuu.headz.metric.Metrics;
import nl.tabuu.tabuucore.plugin.TabuuCorePlugin;
import nl.tabuu.tabuucore.util.Dictionary;

import java.io.*;

public class HeadZ extends TabuuCorePlugin {

    private static HeadZ INSTANCE;

    private HeadDatabase _database;
    private Dictionary _local;

    @Override
    public void onEnable(){
        INSTANCE = this;

        _database = new HeadDatabase();
        _local = getConfigurationManager().addConfiguration("lang").getDictionary("");
        getConfigurationManager().addConfiguration("config");

        load(new File(this.getDataFolder(), "head.db"));

        getCommand("headz").setExecutor(new HeadZCommand());

        new Metrics(this, 7202);
        getInstance().getLogger().info("HeadZ is now enabled.");
    }

    @Override
    public void onDisable(){
        save(new File(this.getDataFolder(), "head.db"));
        getInstance().getLogger().info("HeadZ is now disabled.");
    }

    private void save(File file){
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){

            objectOutputStream.writeObject(_database);
        } catch (IOException exception){
            exception.printStackTrace();
            getLogger().severe("Could not save data!");
        }
    }

    private void load(File file){
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            _database = (HeadDatabase) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            getLogger().warning("No data found!");
        }
    }

    public Dictionary getLocal(){
        return _local;
    }

    public HeadDatabase getDatabase(){
        return _database;
    }

    public static HeadZ getInstance(){
        return INSTANCE;
    }
}
