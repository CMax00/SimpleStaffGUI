package com.cmax.simpleStaffGUI.data;

import com.cmax.simpleStaffGUI.SimpleStaffGUI;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Database {

    private final SimpleStaffGUI plugin;

    private Connection connection;

    public Database(SimpleStaffGUI plugin) {
        this.plugin = plugin;
    }

    public void initialize() throws SQLException {

        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                throw new SQLException(
                        "Could not create plugin directory."
                );
            }
        }

        String filename =
                plugin.getConfig().getString(
                        "database.file",
                        "simplestaffgui.db"
                );

        File databaseFile =
                new File(plugin.getDataFolder(), filename);

        connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databaseFile.getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS punishments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    moderator_uuid TEXT NOT NULL,
                    moderator_name TEXT NOT NULL,
                    reason TEXT,
                    created_at INTEGER NOT NULL,
                    expires_at INTEGER
                )
            """);

            statement.executeUpdate("""
                CREATE INDEX IF NOT EXISTS
                idx_punishments_uuid
                ON punishments(uuid)
            """);

            statement.executeUpdate("""
                CREATE INDEX IF NOT EXISTS
                idx_punishments_type
                ON punishments(type)
            """);
        }
    }

    public synchronized long insert(
            String type,
            UUID uuid,
            String playerName,
            UUID moderatorUuid,
            String moderatorName,
            String reason,
            long createdAt,
            Long expiresAt
    ) throws SQLException {

        String sql = """
            INSERT INTO punishments (
                type,
                uuid,
                player_name,
                moderator_uuid,
                moderator_name,
                reason,
                created_at,
                expires_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, type);
            statement.setString(2, uuid.toString());
            statement.setString(3, playerName);
            statement.setString(4, moderatorUuid.toString());
            statement.setString(5, moderatorName);
            statement.setString(6, reason);
            statement.setLong(7, createdAt);

            if (expiresAt == null) {
                statement.setNull(8, Types.INTEGER);
            } else {
                statement.setLong(8, expiresAt);
            }

            statement.executeUpdate();

            try (ResultSet result =
                         statement.getGeneratedKeys()) {

                if (result.next()) {
                    return result.getLong(1);
                }
            }
        }

        return -1;
    }

    public synchronized boolean hasActivePunishment(
            String type,
            UUID uuid
    ) throws SQLException {

        String sql = """
            SELECT id
            FROM punishments
            WHERE type = ?
              AND uuid = ?
              AND (
                  expires_at IS NULL
                  OR expires_at > ?
              )
            ORDER BY id DESC
            LIMIT 1
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, type);
            statement.setString(2, uuid.toString());
            statement.setLong(
                    3,
                    System.currentTimeMillis()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

                return result.next();
            }
        }
    }

    public synchronized void expireActive(
            String type,
            UUID uuid
    ) throws SQLException {

        String sql = """
            UPDATE punishments
            SET expires_at = ?
            WHERE type = ?
              AND uuid = ?
              AND (
                  expires_at IS NULL
                  OR expires_at > ?
              )
        """;

        long now = System.currentTimeMillis();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, now);
            statement.setString(2, type);
            statement.setString(3, uuid.toString());
            statement.setLong(4, now);

            statement.executeUpdate();
        }
    }

    public synchronized List<PunishmentRecord> getActive(
            String type
    ) throws SQLException {

        String sql = """
            SELECT *
            FROM punishments
            WHERE type = ?
              AND (
                  expires_at IS NULL
                  OR expires_at > ?
              )
            ORDER BY id DESC
        """;

        List<PunishmentRecord> records =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    type
            );

            statement.setLong(
                    2,
                    System.currentTimeMillis()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    records.add(fromResultSet(result));
                }
            }
        }

        return records;
    }

    public synchronized List<PunishmentRecord> getHistory(
            String type,
            UUID uuid
    ) throws SQLException {

        String sql = """
            SELECT *
            FROM punishments
            WHERE type = ?
              AND uuid = ?
            ORDER BY id DESC
        """;

        List<PunishmentRecord> records =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, type);
            statement.setString(2, uuid.toString());

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    records.add(fromResultSet(result));
                }
            }
        }

        return records;
    }

    private PunishmentRecord fromResultSet(
            ResultSet result
    ) throws SQLException {

        Object expires =
                result.getObject("expires_at");

        return new PunishmentRecord(
                result.getLong("id"),
                result.getString("type"),
                UUID.fromString(
                        result.getString("uuid")
                ),
                result.getString("player_name"),
                UUID.fromString(
                        result.getString("moderator_uuid")
                ),
                result.getString("moderator_name"),
                result.getString("reason"),
                result.getLong("created_at"),
                expires == null
                        ? null
                        : ((Number) expires).longValue()
        );
    }

    public void close() {

        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}