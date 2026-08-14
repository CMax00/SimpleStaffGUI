package com.cmax.simpleStaffGUI.gui;

import com.nexomc.nexo.api.NexoItems;
import com.cmax.simpleStaffGUI.SimpleStaffGUI;
import com.cmax.simpleStaffGUI.data.PunishmentRecord;
import com.cmax.simpleStaffGUI.service.PunishmentService;
import com.cmax.simpleStaffGUI.service.TextService;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public final class GuiManager {

    private final SimpleStaffGUI plugin;
    private final PunishmentService punishmentService;
    private final TextService text;

    private final Map<UUID, InputSession> sessions =
            new HashMap<>();

    public GuiManager(
            SimpleStaffGUI plugin,
            PunishmentService punishmentService,
            TextService text
    ) {
        this.plugin = plugin;
        this.punishmentService = punishmentService;
        this.text = text;
    }

    /*
     * ---------------------------------------------------------
     * INPUT SESSION
     * ---------------------------------------------------------
     */

    public enum InputType {

        BAN_PLAYER,
        TEMP_BAN_PLAYER,
        UNBAN_PLAYER,

        MUTE_PLAYER,
        TEMP_MUTE_PLAYER,
        UNMUTE_PLAYER,

        WARNING_PLAYER,
        WARNING_REASON,

        WARNING_HISTORY_PLAYER
    }

    public static final class InputSession {

        private final InputType type;

        private UUID targetUuid;
        private String targetName;
        private String duration;
        private String reason;

        public InputSession(InputType type) {
            this.type = type;
        }

        public InputType getType() {
            return type;
        }

        public UUID getTargetUuid() {
            return targetUuid;
        }

        public void setTargetUuid(UUID targetUuid) {
            this.targetUuid = targetUuid;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public void startInput(
            Player player,
            InputType type
    ) {

        sessions.put(
                player.getUniqueId(),
                new InputSession(type)
        );
    }

    public InputSession getSession(Player player) {

        return sessions.get(
                player.getUniqueId()
        );
    }

    public void removeSession(Player player) {

        sessions.remove(
                player.getUniqueId()
        );
    }

    /*
     * ---------------------------------------------------------
     * NEXO ITEMS
     * ---------------------------------------------------------
     */

    public ItemStack nexoItem(
            String id,
            String fallbackName
    ) {

        try {

            if (NexoItems.exists(id)) {

                ItemStack stack =
                        NexoItems.itemFromId(id).build();

                if (stack != null) {
                    return stack;
                }
            }

        } catch (Exception exception) {

            plugin.getLogger().warning(
                    "Could not create Nexo item '" +
                            id +
                            "': " +
                            exception.getMessage()
            );
        }

        ItemStack fallback =
                new ItemStack(Material.PAPER);

        ItemMeta meta =
                fallback.getItemMeta();

        meta.displayName(
                text.raw(
                        "<white>" +
                                fallbackName +
                                "</white>"
                )
        );

        fallback.setItemMeta(meta);

        return fallback;
    }

    private ItemStack namedNexoItem(
            String id,
            String name
    ) {

        ItemStack item =
                nexoItem(id, name);

        ItemMeta meta =
                item.getItemMeta();

        if (meta != null) {

            meta.displayName(
                    text.raw(
                            "<bold><white>" +
                                    name +
                                    "</white></bold>"
                    )
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    /*
     * ---------------------------------------------------------
     * MAIN MENU
     * ---------------------------------------------------------
     */

    public void openMainMenu(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.MAIN
                        ),
                        27,
                        text.raw("<dark_gray>Staff Panel")
                );

        inventory.setItem(
                11,
                namedNexoItem(
                        "judge_gavel_icon",
                        "Ban Management"
                )
        );

        inventory.setItem(
                13,
                namedNexoItem(
                        "essential_icons_dialogue",
                        "Chat Management"
                )
        );

        inventory.setItem(
                15,
                namedNexoItem(
                        "essential_icons_exclamation",
                        "Warning Management"
                )
        );

        inventory.setItem(
                22,
                namedNexoItem(
                        "essential_icons_no",
                        "Close"
                )
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * BAN MENU
     * ---------------------------------------------------------
     */

    public void openBanMenu(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.BAN
                        ),
                        27,
                        text.raw("<dark_gray>Ban Management")
                );

        inventory.setItem(
                10,
                namedNexoItem(
                        "judge_gavel_icon",
                        "Ban"
                )
        );

        inventory.setItem(
                12,
                namedNexoItem(
                        "hourglass_icon",
                        "Temp Ban"
                )
        );

        inventory.setItem(
                14,
                namedNexoItem(
                        "essential_icons_yes",
                        "Unban"
                )
        );

        inventory.setItem(
                16,
                namedNexoItem(
                        "skull_icon",
                        "Banned Players"
                )
        );

        inventory.setItem(
                22,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * BAN DURATION
     * ---------------------------------------------------------
     */

    public void openBanDurationMenu(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.BAN_DURATION
                        ),
                        27,
                        text.raw("<dark_gray>Temp Ban Duration")
                );

        List<String> durations =
                plugin.getConfig().getStringList(
                        "durations.tempban"
                );

        for (int i = 0; i < durations.size() && i < 5; i++) {

            String duration =
                    durations.get(i);

            inventory.setItem(
                    9 + (i * 2),
                    namedNexoItem(
                            "hourglass_icon",
                            duration
                    )
            );
        }

        inventory.setItem(
                22,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * CHAT MENU
     * ---------------------------------------------------------
     */

    public void openChatMenu(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.CHAT
                        ),
                        27,
                        text.raw("<dark_gray>Chat Management")
                );

        inventory.setItem(
                10,
                namedNexoItem(
                        "essential_icons_dialogue",
                        "Mute"
                )
        );

        inventory.setItem(
                12,
                namedNexoItem(
                        "hourglass_icon",
                        "Temp Mute"
                )
        );

        inventory.setItem(
                14,
                namedNexoItem(
                        "essential_icons_yes",
                        "Unmute"
                )
        );

        inventory.setItem(
                16,
                namedNexoItem(
                        "essential_icons_lock",
                        "Muted Players"
                )
        );

        inventory.setItem(
                22,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * MUTE DURATION
     * ---------------------------------------------------------
     */

    public void openMuteDurationMenu(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.MUTE_DURATION
                        ),
                        27,
                        text.raw("<dark_gray>Temp Mute Duration")
                );

        List<String> durations =
                plugin.getConfig().getStringList(
                        "durations.tempmute"
                );

        for (int i = 0; i < durations.size() && i < 5; i++) {

            String duration =
                    durations.get(i);

            inventory.setItem(
                    9 + (i * 2),
                    namedNexoItem(
                            "hourglass_icon",
                            duration
                    )
            );
        }

        inventory.setItem(
                22,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * WARNING MENU
     * ---------------------------------------------------------
     */

    public void openWarningMenu(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.WARNING
                        ),
                        27,
                        text.raw("<dark_gray>Warning Management")
                );

        inventory.setItem(
                11,
                namedNexoItem(
                        "essential_icons_exclamation",
                        "Give Warning"
                )
        );

        inventory.setItem(
                15,
                namedNexoItem(
                        "magnifying_glass_icon",
                        "Check Warnings"
                )
        );

        inventory.setItem(
                22,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * CONFIRMATION MENUS
     * ---------------------------------------------------------
     */

    public void openBanConfirmation(Player player) {

        InputSession session =
                getSession(player);

        if (session == null) {
            openBanMenu(player);
            return;
        }

        openConfirmation(
                player,
                GuiHolder.Menu.BAN_CONFIRM,
                "Confirm Ban",
                session.getTargetName()
        );
    }

    public void openMuteConfirmation(Player player) {

        InputSession session =
                getSession(player);

        if (session == null) {
            openChatMenu(player);
            return;
        }

        openConfirmation(
                player,
                GuiHolder.Menu.MUTE_CONFIRM,
                "Confirm Mute",
                session.getTargetName()
        );
    }

    public void openWarningConfirmation(Player player) {

        InputSession session =
                getSession(player);

        if (session == null) {
            openWarningMenu(player);
            return;
        }

        openConfirmation(
                player,
                GuiHolder.Menu.WARNING_CONFIRM,
                "Confirm Warning",
                session.getTargetName()
        );
    }

    private void openConfirmation(
            Player player,
            GuiHolder.Menu menu,
            String title,
            String target
    ) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(menu),
                        27,
                        text.raw(
                                "<dark_gray>" +
                                        title
                        )
                );

        inventory.setItem(
                11,
                namedNexoItem(
                        "essential_icons_yes",
                        "Confirm"
                )
        );

        inventory.setItem(
                15,
                namedNexoItem(
                        "essential_icons_no",
                        "Cancel"
                )
        );

        ItemStack targetItem =
                new ItemStack(Material.PAPER);

        ItemMeta meta =
                targetItem.getItemMeta();

        meta.displayName(
                text.raw(
                        "<yellow>" +
                                target
                )
        );

        targetItem.setItemMeta(meta);

        inventory.setItem(
                13,
                targetItem
        );

        player.openInventory(inventory);
    }

    /*
     * ---------------------------------------------------------
     * PLAYER LISTS
     * ---------------------------------------------------------
     */

    public void openBannedPlayers(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.BANNED_PLAYERS
                        ),
                        54,
                        text.raw("<dark_gray>Banned Players")
                );

        try {

            List<PunishmentRecord> records =
                    punishmentService.getActiveBans();

            for (int i = 0; i < records.size() && i < 45; i++) {

                PunishmentRecord record =
                        records.get(i);

                inventory.setItem(
                        i,
                        listItem(
                                record,
                                "Banned"
                        )
                );
            }

        } catch (Exception exception) {

            plugin.getLogger().warning(
                    "Could not load banned players: " +
                            exception.getMessage()
            );
        }

        inventory.setItem(
                49,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    public void openMutedPlayers(Player player) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.MUTED_PLAYERS
                        ),
                        54,
                        text.raw("<dark_gray>Muted Players")
                );

        try {

            List<PunishmentRecord> records =
                    punishmentService.getActiveMutes();

            for (int i = 0; i < records.size() && i < 45; i++) {

                PunishmentRecord record =
                        records.get(i);

                inventory.setItem(
                        i,
                        listItem(
                                record,
                                "Muted"
                        )
                );
            }

        } catch (Exception exception) {

            plugin.getLogger().warning(
                    "Could not load muted players: " +
                            exception.getMessage()
            );
        }

        inventory.setItem(
                49,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    private ItemStack listItem(
            PunishmentRecord record,
            String type
    ) {

        ItemStack item =
                new ItemStack(Material.PAPER);

        ItemMeta meta =
                item.getItemMeta();

        meta.displayName(
                text.raw(
                        "<white>" +
                                record.playerName()
                )
        );

        List<Component> lore =
                new ArrayList<>();

        lore.add(
                text.raw(
                        "<gray>Type: <white>" +
                                type
                )
        );

        lore.add(
                text.raw(
                        "<gray>Moderator: <white>" +
                                record.moderatorName()
                )
        );

        if (record.expiresAt() == null) {

            lore.add(
                    text.raw(
                            "<gray>Duration: <red>Permanent"
                    )
            );

        } else {

            lore.add(
                    text.raw(
                            "<gray>Expires: <white>" +
                                    formatDate(
                                            record.expiresAt()
                                    )
                    )
            );
        }

        meta.lore(lore);

        item.setItemMeta(meta);

        return item;
    }

    /*
     * ---------------------------------------------------------
     * WARNING HISTORY
     * ---------------------------------------------------------
     */

    public void openWarningHistory(
            Player player,
            UUID targetUuid,
            String targetName
    ) {

        Inventory inventory =
                Bukkit.createInventory(
                        new GuiHolder(
                                GuiHolder.Menu.WARNING_HISTORY
                        ),
                        54,
                        text.raw(
                                "<dark_gray>Warnings: " +
                                        targetName
                        )
                );

        try {

            List<PunishmentRecord> records =
                    punishmentService.getWarnings(
                            targetUuid
                    );

            for (int i = 0; i < records.size() && i < 45; i++) {

                PunishmentRecord record =
                        records.get(i);

                ItemStack item =
                        new ItemStack(Material.PAPER);

                ItemMeta meta =
                        item.getItemMeta();

                meta.displayName(
                        text.raw(
                                "<yellow>Warning #" +
                                        record.id()
                        )
                );

                meta.lore(
                        List.of(
                                text.raw(
                                        "<gray>Reason: <white>" +
                                                record.reason()
                                ),
                                text.raw(
                                        "<gray>Moderator: <white>" +
                                                record.moderatorName()
                                ),
                                text.raw(
                                        "<gray>Date: <white>" +
                                                formatDate(
                                                        record.createdAt()
                                                )
                                )
                        )
                );

                item.setItemMeta(meta);

                inventory.setItem(i, item);
            }

        } catch (Exception exception) {

            plugin.getLogger().warning(
                    "Could not load warning history: " +
                            exception.getMessage()
            );
        }

        inventory.setItem(
                49,
                namedNexoItem(
                        "essential_icons_no",
                        "Back"
                )
        );

        player.openInventory(inventory);
    }

    private String formatDate(long timestamp) {

        return new SimpleDateFormat(
                "yyyy-MM-dd HH:mm"
        ).format(
                new Date(timestamp)
        );
    }
}