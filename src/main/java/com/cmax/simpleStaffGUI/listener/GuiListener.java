package com.cmax.simpleStaffGUI.listener;

import com.cmax.simpleStaffGUI.gui.GuiHolder;
import com.cmax.simpleStaffGUI.gui.GuiManager;
import com.cmax.simpleStaffGUI.gui.GuiManager.InputSession;
import com.cmax.simpleStaffGUI.service.DurationParser;
import com.cmax.simpleStaffGUI.service.PunishmentService;
import com.cmax.simpleStaffGUI.service.TextService;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GuiListener implements Listener {

    private final GuiManager gui;
    private final PunishmentService punishment;
    private final TextService text;

    public GuiListener(
            GuiManager gui,
            PunishmentService punishment,
            TextService text
    ) {
        this.gui = gui;
        this.punishment = punishment;
        this.text = text;
    }

    @EventHandler
    public void onClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder()
                instanceof GuiHolder holder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() < 0 ||
                event.getRawSlot() >=
                        event.getInventory().getSize()) {
            return;
        }

        int slot =
                event.getRawSlot();

        switch (holder.getMenu()) {

            case MAIN ->
                    mainClick(player, slot);

            case BAN ->
                    banClick(player, slot);

            case BAN_DURATION ->
                    banDurationClick(player, slot);

            case BAN_CONFIRM ->
                    banConfirmClick(player, slot);

            case CHAT ->
                    chatClick(player, slot);

            case MUTE_DURATION ->
                    muteDurationClick(player, slot);

            case MUTE_CONFIRM ->
                    muteConfirmClick(player, slot);

            case WARNING ->
                    warningClick(player, slot);

            case WARNING_CONFIRM ->
                    warningConfirmClick(player, slot);

            case BANNED_PLAYERS ->
                    listBack(player, slot, true);

            case MUTED_PLAYERS ->
                    listBack(player, slot, false);

            case WARNING_HISTORY ->
                    warningHistoryBack(player, slot);
        }
    }

    private void mainClick(
            Player player,
            int slot
    ) {

        switch (slot) {

            case 11 ->
                    gui.openBanMenu(player);

            case 13 ->
                    gui.openChatMenu(player);

            case 15 ->
                    gui.openWarningMenu(player);

            case 22 ->
                    player.closeInventory();
        }
    }

    private void banClick(
            Player player,
            int slot
    ) {

        switch (slot) {

            case 10 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.ban"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.BAN_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 12 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.tempban"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.TEMP_BAN_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 14 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.unban"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.UNBAN_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 16 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.banlist"
                )) {
                    noPermission(player);
                    return;
                }

                gui.openBannedPlayers(player);
            }

            case 22 ->
                    gui.openMainMenu(player);
        }
    }

    private void banDurationClick(
            Player player,
            int slot
    ) {

        if (slot == 22) {
            gui.openBanMenu(player);
            return;
        }

        String duration =
                durationFromSlot(
                        player,
                        slot,
                        "tempban"
                );

        if (duration == null) {
            return;
        }

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            gui.openBanMenu(player);
            return;
        }

        session.setDuration(duration);

        gui.openBanConfirmation(player);
    }

    private void banConfirmClick(
            Player player,
            int slot
    ) {

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            gui.openBanMenu(player);
            return;
        }

        if (slot == 15) {
            gui.removeSession(player);
            gui.openBanMenu(player);
            return;
        }

        if (slot != 11) {
            return;
        }

        try {

            OfflinePlayer target =
                    Bukkit.getOfflinePlayer(
                            session.getTargetUuid()
                    );

            switch (session.getType()) {

                case BAN_PLAYER -> {

                    punishment.ban(
                            player,
                            target,
                            "Staff GUI"
                    );

                    player.sendMessage(
                            text.get(
                                    "success.ban",
                                    Map.of(
                                            "player",
                                            session.getTargetName()
                                    )
                            )
                    );
                }

                case TEMP_BAN_PLAYER -> {

                    Duration duration =
                            DurationParser.parse(
                                    session.getDuration()
                            );

                    punishment.tempBan(
                            player,
                            target,
                            duration,
                            "Staff GUI"
                    );

                    player.sendMessage(
                            text.get(
                                    "success.tempban",
                                    Map.of(
                                            "player",
                                            session.getTargetName(),
                                            "duration",
                                            session.getDuration()
                                    )
                            )
                    );
                }

                case UNBAN_PLAYER -> {

                    punishment.unban(target);

                    player.sendMessage(
                            text.get(
                                    "success.unban",
                                    Map.of(
                                            "player",
                                            session.getTargetName()
                                    )
                            )
                    );
                }

                default -> {
                    return;
                }
            }

            gui.removeSession(player);
            gui.openBanMenu(player);

        } catch (Exception exception) {

            handlePunishmentError(
                    player,
                    exception
            );
        }
    }

    private void chatClick(
            Player player,
            int slot
    ) {

        switch (slot) {

            case 10 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.mute"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.MUTE_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 12 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.tempmute"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.TEMP_MUTE_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 14 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.unmute"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.UNMUTE_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 16 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.mutelist"
                )) {
                    noPermission(player);
                    return;
                }

                gui.openMutedPlayers(player);
            }

            case 22 ->
                    gui.openMainMenu(player);
        }
    }

    private void muteDurationClick(
            Player player,
            int slot
    ) {

        if (slot == 22) {
            gui.openChatMenu(player);
            return;
        }

        String duration =
                durationFromSlot(
                        player,
                        slot,
                        "tempmute"
                );

        if (duration == null) {
            return;
        }

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            gui.openChatMenu(player);
            return;
        }

        session.setDuration(duration);

        gui.openMuteConfirmation(player);
    }

    private void muteConfirmClick(
            Player player,
            int slot
    ) {

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            gui.openChatMenu(player);
            return;
        }

        if (slot == 15) {

            gui.removeSession(player);
            gui.openChatMenu(player);
            return;
        }

        if (slot != 11) {
            return;
        }

        try {

            OfflinePlayer target =
                    Bukkit.getOfflinePlayer(
                            session.getTargetUuid()
                    );

            switch (session.getType()) {

                case MUTE_PLAYER -> {

                    punishment.mute(
                            player,
                            target,
                            "Staff GUI"
                    );

                    player.sendMessage(
                            text.get(
                                    "success.mute",
                                    Map.of(
                                            "player",
                                            session.getTargetName()
                                    )
                            )
                    );
                }

                case TEMP_MUTE_PLAYER -> {

                    Duration duration =
                            DurationParser.parse(
                                    session.getDuration()
                            );

                    punishment.tempMute(
                            player,
                            target,
                            duration,
                            "Staff GUI"
                    );

                    player.sendMessage(
                            text.get(
                                    "success.tempmute",
                                    Map.of(
                                            "player",
                                            session.getTargetName(),
                                            "duration",
                                            session.getDuration()
                                    )
                            )
                    );
                }

                case UNMUTE_PLAYER -> {

                    punishment.unmute(target);

                    player.sendMessage(
                            text.get(
                                    "success.unmute",
                                    Map.of(
                                            "player",
                                            session.getTargetName()
                                    )
                            )
                    );
                }

                default -> {
                    return;
                }
            }

            gui.removeSession(player);
            gui.openChatMenu(player);

        } catch (Exception exception) {

            handlePunishmentError(
                    player,
                    exception
            );
        }
    }

    private void warningClick(
            Player player,
            int slot
    ) {

        switch (slot) {

            case 11 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.warn"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.WARNING_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 15 -> {

                if (!player.hasPermission(
                        "SimpleStaffGUI.warnings"
                )) {
                    noPermission(player);
                    return;
                }

                gui.startInput(
                        player,
                        GuiManager.InputType.WARNING_HISTORY_PLAYER
                );

                player.closeInventory();

                player.sendMessage(
                        text.get("input.player")
                );
            }

            case 22 ->
                    gui.openMainMenu(player);
        }
    }

    private void warningConfirmClick(
            Player player,
            int slot
    ) {

        InputSession session =
                gui.getSession(player);

        if (session == null) {
            gui.openWarningMenu(player);
            return;
        }

        if (slot == 15) {

            gui.removeSession(player);
            gui.openWarningMenu(player);
            return;
        }

        if (slot != 11) {
            return;
        }

        try {

            OfflinePlayer target =
                    Bukkit.getOfflinePlayer(
                            session.getTargetUuid()
                    );

            punishment.warn(
                    player,
                    target,
                    session.getReason()
            );

            player.sendMessage(
                    text.get(
                            "success.warning",
                            Map.of(
                                    "player",
                                    session.getTargetName()
                            )
                    )
            );

            gui.removeSession(player);
            gui.openWarningMenu(player);

        } catch (Exception exception) {

            handlePunishmentError(
                    player,
                    exception
            );
        }
    }

    private void listBack(
            Player player,
            int slot,
            boolean banned
    ) {

        if (slot == 49) {

            if (banned) {
                gui.openBanMenu(player);
            } else {
                gui.openChatMenu(player);
            }
        }
    }

    private void warningHistoryBack(
            Player player,
            int slot
    ) {

        if (slot == 49) {
            gui.openWarningMenu(player);
        }
    }

    private String durationFromSlot(
            Player player,
            int slot,
            String configPath
    ) {

        List<String> durations =
                player.getServer()
                        .getPluginManager()
                        .getPlugin("SimpleStaffGUI")
                        instanceof com.cmax.simpleStaffGUI.SimpleStaffGUI plugin
                        ? plugin.getConfig().getStringList(
                        "durations." + configPath
                )
                        : java.util.Collections.emptyList();

        for (int i = 0; i < durations.size() && i < 5; i++) {

            if (slot == 9 + (i * 2)) {
                return durations.get(i);
            }
        }

        return null;
    }

    private void noPermission(
            Player player
    ) {

        player.sendMessage(
                text.get("error.no_permission")
        );
    }

    private void handlePunishmentError(
            Player player,
            Exception exception
    ) {

        if (exception instanceof IllegalStateException) {

            String key =
                    exception.getMessage();

            if (key != null) {

                player.sendMessage(
                        text.get(
                                "error." + key
                        )
                );

                return;
            }
        }

        player.sendMessage(
                text.get("error.database")
        );

        exception.printStackTrace();
    }

    @EventHandler
    public void onClose(
            InventoryCloseEvent event
    ) {

        /*
         * We intentionally do NOT remove the chat input session
         * here. A player must be able to close the inventory while
         * the plugin is waiting for chat input.
         */
    }
}
