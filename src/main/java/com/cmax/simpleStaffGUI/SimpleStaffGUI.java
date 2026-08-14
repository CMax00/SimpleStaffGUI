package com.cmax.simpleStaffGUI;

import com.cmax.simpleStaffGUI.command.StaffCommand;
import com.cmax.simpleStaffGUI.data.Database;
import com.cmax.simpleStaffGUI.gui.GuiManager;
import com.cmax.simpleStaffGUI.listener.ChatInputListener;
import com.cmax.simpleStaffGUI.listener.GuiListener;
import com.cmax.simpleStaffGUI.listener.JoinListener;
import com.cmax.simpleStaffGUI.listener.MuteListener;
import com.cmax.simpleStaffGUI.service.PunishmentService;
import com.cmax.simpleStaffGUI.service.TextService;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class SimpleStaffGUI extends JavaPlugin {

    private Database database;
    private TextService textService;
    private PunishmentService punishmentService;
    private GuiManager guiManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        saveResource("messages.yml", false);

        textService = new TextService(this);

        try {
            database = new Database(this);
            database.initialize();
        } catch (SQLException exception) {
            getLogger().severe("Could not initialize SQLite.");
            exception.printStackTrace();

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        punishmentService =
                new PunishmentService(this, database, textService);

        guiManager =
                new GuiManager(this, punishmentService, textService);

        StaffCommand command =
                new StaffCommand(this, guiManager);

        getCommand("staff").setExecutor(command);

        getServer().getPluginManager().registerEvents(
                new GuiListener(
                        guiManager,
                        punishmentService,
                        textService
                ),
                this
        );

        getServer().getPluginManager().registerEvents(
                new ChatInputListener(
                        guiManager,
                        punishmentService,
                        textService
                ),
                this
        );

        getServer().getPluginManager().registerEvents(
                new MuteListener(
                        punishmentService,
                        textService
                ),
                this
        );

        getServer().getPluginManager().registerEvents(
                new JoinListener(
                        punishmentService,
                        textService
                ),
                this
        );

        getLogger().info("SimpleStaffGUI enabled.");
    }

    @Override
    public void onDisable() {

        if (database != null) {
            database.close();
        }

        getLogger().info("SimpleStaffGUI disabled.");
    }

    public Database getDatabase() {
        return database;
    }

    public TextService getTextService() {
        return textService;
    }

    public PunishmentService getPunishmentService() {
        return punishmentService;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}