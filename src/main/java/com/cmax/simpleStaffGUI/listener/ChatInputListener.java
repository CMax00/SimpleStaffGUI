package com.cmax.simpleStaffGUI.listener;

import com.cmax.simpleStaffGUI.gui.GuiManager;
import com.cmax.simpleStaffGUI.gui.GuiManager.InputSession;
import com.cmax.simpleStaffGUI.gui.GuiManager.InputType;
import com.cmax.simpleStaffGUI.service.PunishmentService;
import com.cmax.simpleStaffGUI.service.TextService;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public final class ChatInputListener implements Listener {

    private final GuiManager gui;
    private final PunishmentService punishment;
    private final TextService text;

    public ChatInputListener(
            GuiManager gui,
            PunishmentService punishment,
            TextService text
    ) {
        this.gui = gui;
        this.punishment = punishment;
        this.text = text;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(
            AsyncPlayerChatEvent event
    ) {

        Player player =
                event.getPlayer();

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        event.setCancelled(true);

        String input =
                event.getMessage().trim();

        if (input.equalsIgnoreCase("cancel")) {

            gui.removeSession(player);

            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager()
                            .getPlugin("SimpleStaffGUI"),
                    () -> gui.openMainMenu(player)
            );

            player.sendMessage(
                    text.get("error.cancelled")
            );

            return;
        }

        switch (session.getType()) {

            case BAN_PLAYER ->
                    handleBanPlayer(
                            player,
                            input,
                            false
                    );

            case TEMP_BAN_PLAYER ->
                    handleBanPlayer(
                            player,
                            input,
                            true
                    );

            case UNBAN_PLAYER ->
                    handleUnbanPlayer(
                            player,
                            input
                    );

            case MUTE_PLAYER ->
                    handleMutePlayer(
                            player,
                            input,
                            false
                    );

            case TEMP_MUTE_PLAYER ->
                    handleMutePlayer(
                            player,
                            input,
                            true
                    );

            case UNMUTE_PLAYER ->
                    handleUnmutePlayer(
                            player,
                            input
                    );

            case WARNING_PLAYER ->
                    handleWarningPlayer(
                            player,
                            input
                    );

            case WARNING_HISTORY_PLAYER ->
                    handleWarningHistoryPlayer(
                            player,
                            input
                    );

            case WARNING_REASON ->
                    handleWarningReason(
                            player,
                            input
                    );
        }
    }

    private void handleBanPlayer(
            Player player,
            String input,
            boolean temporary
    ) {

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(input);

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        session.setTargetUuid(
                target.getUniqueId()
        );

        session.setTargetName(input);

        Bukkit.getScheduler().runTask(
                getPlugin(),
                () -> {

                    if (temporary) {
                        gui.openBanDurationMenu(player);
                    } else {
                        gui.openBanConfirmation(player);
                    }
                }
        );
    }

    private void handleUnbanPlayer(
            Player player,
            String input
    ) {

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(input);

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        session.setTargetUuid(
                target.getUniqueId()
        );

        session.setTargetName(input);

        Bukkit.getScheduler().runTask(
                getPlugin(),
                () -> gui.openBanConfirmation(player)
        );
    }

    private void handleMutePlayer(
            Player player,
            String input,
            boolean temporary
    ) {

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(input);

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        session.setTargetUuid(
                target.getUniqueId()
        );

        session.setTargetName(input);

        Bukkit.getScheduler().runTask(
                getPlugin(),
                () -> {

                    if (temporary) {
                        gui.openMuteDurationMenu(player);
                    } else {
                        gui.openMuteConfirmation(player);
                    }
                }
        );
    }

    private void handleUnmutePlayer(
            Player player,
            String input
    ) {

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(input);

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        session.setTargetUuid(
                target.getUniqueId()
        );

        session.setTargetName(input);

        Bukkit.getScheduler().runTask(
                getPlugin(),
                () -> gui.openMuteConfirmation(player)
        );
    }

    private void handleWarningPlayer(
            Player player,
            String input
    ) {

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(input);

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        session.setTargetUuid(
                target.getUniqueId()
        );

        session.setTargetName(input);

        session =
                new GuiManager.InputSession(
                        InputType.WARNING_REASON
                );

        session.setTargetUuid(
                target.getUniqueId()
        );

        session.setTargetName(input);

        /*
         * The GuiManager needs to retain the target while waiting
         * for the warning reason.
         *
         * This is handled below by starting the warning-reason
         * session and copying the target data.
         */

        gui.removeSession(player);

        gui.startInput(
                player,
                InputType.WARNING_REASON
        );

        InputSession newSession =
                gui.getSession(player);

        newSession.setTargetUuid(
                target.getUniqueId()
        );

        newSession.setTargetName(input);

        player.sendMessage(
                text.get("input.warning_reason")
        );
    }

    private void handleWarningReason(
            Player player,
            String input
    ) {

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            return;
        }

        /*
         * Store the warning reason in targetName temporarily.
         * We use a separate field below in the corrected
         * InputSession implementation.
         */

        session.setReason(input);

        Bukkit.getScheduler().runTask(
                getPlugin(),
                () -> gui.openWarningConfirmation(player)
        );
    }

    private void handleWarningHistoryPlayer(
            Player player,
            String input
    ) {

        OfflinePlayer target =
                Bukkit.getOfflinePlayer(input);

        gui.removeSession(player);

        Bukkit.getScheduler().runTask(
                getPlugin(),
                () -> gui.openWarningHistory(
                        player,
                        target.getUniqueId(),
                        input
                )
        );
    }

    private com.cmax.simpleStaffGUI.SimpleStaffGUI getPlugin() {

        return (com.cmax.simpleStaffGUI.SimpleStaffGUI)
                Bukkit.getPluginManager()
                        .getPlugin("SimpleStaffGUI");
    }
}