package com.cmax.simpleStaffGUI.listener;

import com.cmax.simpleStaffGUI.service.PunishmentService;
import com.cmax.simpleStaffGUI.service.TextService;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class JoinListener implements Listener {

    private final PunishmentService punishment;
    private final TextService text;

    public JoinListener(
            PunishmentService punishment,
            TextService text
    ) {
        this.punishment = punishment;
        this.text = text;
    }

    @EventHandler
    public void onLogin(
            PlayerLoginEvent event
    ) {

        Player player =
                event.getPlayer();

        try {

            if (!punishment.isBanned(
                    player.getUniqueId()
            )) {
                return;
            }

            event.disallow(
                    PlayerLoginEvent.Result.KICK_BANNED,
                    text.get("player.ban_message")
            );

        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }
}