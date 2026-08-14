package com.cmax.simpleStaffGUI.gui;


import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class GuiHolder implements InventoryHolder {

    public enum Menu {

        MAIN,

        BAN,
        BAN_DURATION,
        BAN_CONFIRM,

        CHAT,
        MUTE_DURATION,
        MUTE_CONFIRM,

        WARNING,
        WARNING_CONFIRM,

        BANNED_PLAYERS,
        MUTED_PLAYERS,
        WARNING_HISTORY
    }

    private final Menu menu;

    public GuiHolder(Menu menu) {
        this.menu = menu;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}