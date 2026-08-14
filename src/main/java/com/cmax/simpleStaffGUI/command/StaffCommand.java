package com.cmax.simpleStaffGUI.command;

import com.cmax.simpleStaffGUI.SimpleStaffGUI;
import com.cmax.simpleStaffGUI.gui.GuiManager;
import com.cmax.simpleStaffGUI.service.TextService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class StaffCommand implements CommandExecutor {

    private final SimpleStaffGUI plugin;
    private final GuiManager guiManager;
    private final TextService text;

    public StaffCommand(
            SimpleStaffGUI plugin,
            GuiManager guiManager
    ) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.text = plugin.getTextService();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    text.get("error.player_only")
            );
            return true;
        }

        if (!player.hasPermission("simplestaffgui.use")) {
            player.sendMessage(
                    text.get("error.no_permission")
            );
            return true;
        }

        guiManager.openMainMenu(player);

        return true;
    }
}