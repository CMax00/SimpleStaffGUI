package com.cmax.simpleStaffGUI.service;


import com.cmax.simpleStaffGUI.data.Database;
import com.cmax.simpleStaffGUI.data.PunishmentRecord;
import com.cmax.simpleStaffGUI.SimpleStaffGUI;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PunishmentService {

    private final SimpleStaffGUI plugin;
    private final Database database;
    private final TextService text;

    public PunishmentService(
            SimpleStaffGUI plugin,
            Database database,
            TextService text
    ) {
        this.plugin = plugin;
        this.database = database;
        this.text = text;
    }

    public boolean isBanned(UUID uuid)
            throws SQLException {

        return database.hasActivePunishment(
                "BAN",
                uuid
        );
    }

    public boolean isMuted(UUID uuid)
            throws SQLException {

        return database.hasActivePunishment(
                "MUTE",
                uuid
        );
    }

    public void ban(
            Player moderator,
            OfflinePlayer target,
            String reason
    ) throws SQLException {

        if (isBanned(target.getUniqueId())) {
            throw new IllegalStateException(
                    "already_banned"
            );
        }

        long now =
                System.currentTimeMillis();

        database.insert(
                "BAN",
                target.getUniqueId(),
                target.getName(),
                moderator.getUniqueId(),
                moderator.getName(),
                reason,
                now,
                null
        );

        Bukkit.getBanList(
                BanList.Type.NAME
        ).addBan(
                target.getName(),
                reason,
                null,
                moderator.getName()
        );

        if (target.isOnline()) {

            Player player =
                    target.getPlayer();

            if (player != null) {

                player.kick(
                        text.get("player.ban_message")
                );
            }
        }
    }

    public void tempBan(
            Player moderator,
            OfflinePlayer target,
            Duration duration,
            String reason
    ) throws SQLException {

        if (isBanned(target.getUniqueId())) {
            throw new IllegalStateException(
                    "already_banned"
            );
        }

        long now =
                System.currentTimeMillis();

        long expires =
                now + duration.toMillis();

        database.insert(
                "BAN",
                target.getUniqueId(),
                target.getName(),
                moderator.getUniqueId(),
                moderator.getName(),
                reason,
                now,
                expires
        );

        Bukkit.getBanList(
                BanList.Type.NAME
        ).addBan(
                target.getName(),
                reason,
                new java.util.Date(expires),
                moderator.getName()
        );

        if (target.isOnline()) {

            Player player =
                    target.getPlayer();

            if (player != null) {

                player.kick(
                        text.get("player.ban_message")
                );
            }
        }
    }

    public void unban(
            OfflinePlayer target
    ) throws SQLException {

        if (!isBanned(target.getUniqueId())) {
            throw new IllegalStateException(
                    "not_banned"
            );
        }

        Bukkit.getBanList(
                BanList.Type.NAME
        ).pardon(target.getName());

        database.expireActive(
                "BAN",
                target.getUniqueId()
        );
    }

    public void mute(
            Player moderator,
            OfflinePlayer target,
            String reason
    ) throws SQLException {

        if (isMuted(target.getUniqueId())) {
            throw new IllegalStateException(
                    "already_muted"
            );
        }

        database.insert(
                "MUTE",
                target.getUniqueId(),
                target.getName(),
                moderator.getUniqueId(),
                moderator.getName(),
                reason,
                System.currentTimeMillis(),
                null
        );
    }

    public void tempMute(
            Player moderator,
            OfflinePlayer target,
            Duration duration,
            String reason
    ) throws SQLException {

        if (isMuted(target.getUniqueId())) {
            throw new IllegalStateException(
                    "already_muted"
            );
        }

        long now =
                System.currentTimeMillis();

        database.insert(
                "MUTE",
                target.getUniqueId(),
                target.getName(),
                moderator.getUniqueId(),
                moderator.getName(),
                reason,
                now,
                now + duration.toMillis()
        );
    }

    public void unmute(
            OfflinePlayer target
    ) throws SQLException {

        if (!isMuted(target.getUniqueId())) {
            throw new IllegalStateException(
                    "not_muted"
            );
        }

        database.expireActive(
                "MUTE",
                target.getUniqueId()
        );
    }

    public long warn(
            Player moderator,
            OfflinePlayer target,
            String reason
    ) throws SQLException {

        long id =
                database.insert(
                        "WARNING",
                        target.getUniqueId(),
                        target.getName(),
                        moderator.getUniqueId(),
                        moderator.getName(),
                        reason,
                        System.currentTimeMillis(),
                        null
                );

        if (target.isOnline()) {

            Player player =
                    target.getPlayer();

            if (player != null) {

                player.sendMessage(
                        text.get(
                                "warning.received",
                                Map.of(
                                        "reason",
                                        reason
                                )
                        )
                );
            }
        }

        return id;
    }

    public List<PunishmentRecord> getActiveBans()
            throws SQLException {

        return database.getActive("BAN");
    }

    public List<PunishmentRecord> getActiveMutes()
            throws SQLException {

        return database.getActive("MUTE");
    }

    public List<PunishmentRecord> getWarnings(
            UUID uuid
    ) throws SQLException {

        return database.getHistory(
                "WARNING",
                uuid
        );
    }
}