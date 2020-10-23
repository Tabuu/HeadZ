package nl.tabuu.headz;

import nl.tabuu.headz.commands.HeadZCommand;
import nl.tabuu.headz.data.HeadDatabase;
import nl.tabuu.headz.metric.Metrics;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.configuration.file.JsonConfiguration;
import nl.tabuu.tabuucore.configuration.file.YamlConfiguration;
import nl.tabuu.tabuucore.plugin.TabuuCorePlugin;
import nl.tabuu.tabuucore.util.Dictionary;

import java.util.function.Supplier;

public class HeadZ extends TabuuCorePlugin {

    private static HeadZ INSTANCE;

    private Dictionary _local;
    private HeadDatabase _database;
    private IConfiguration _config, _data;

    @Override
    public void onEnable(){
        INSTANCE = this;

        _database = new HeadDatabase();
        _local = getConfigurationManager().addConfiguration("lang.yml", YamlConfiguration.class).getDictionary("");
        _data = getConfigurationManager().addConfiguration("data.json", JsonConfiguration.class);
        _config = getConfigurationManager().addConfiguration("config.yml", YamlConfiguration.class);

        _database = load();

        getCommand("headz").setExecutor(new HeadZCommand());

        new Metrics(this, 7202);
        getInstance().getLogger().info("HeadZ is now enabled.");
    }

    @Override
    public void onDisable(){
        save();
        getInstance().getLogger().info("HeadZ is now disabled.");
    }

    private void save() {
        _data.set("", _database);
        _data.save();
    }

    public IConfiguration getConfiguration() {
        return _config;
    }

    private HeadDatabase load() {
        return _data.getSerializable("", HeadDatabase.class, (Supplier<HeadDatabase>) HeadDatabase::new);
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