package com.cmax.simpleStaffGUI.listener;

import com.cmax.simpleStaffGUI.service.PunishmentService;
import com.cmax.simpleStaffGUI.service.TextService;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class MuteListener implements Listener {

    private final PunishmentService punishment;
    private final TextService text;

    public MuteListener(
            PunishmentService punishment,
            TextService text
    ) {
        this.punishment = punishment;
        this.text = text;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(
            AsyncPlayerChatEvent event
    ) {

        Player player =
                event.getPlayer();

        try {

            if (!punishment.isMuted(
                    player.getUniqueId()
            )) {
                return;
            }

            event.setCancelled(true);

            player.sendMessage(
                    text.get("player.muted")
            );

        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }
}