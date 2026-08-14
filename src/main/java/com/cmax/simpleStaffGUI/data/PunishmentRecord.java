package com.cmax.simpleStaffGUI.data;

import java.util.UUID;

public record PunishmentRecord(
        long id,
        String type,
        UUID uuid,
        String playerName,
        UUID moderatorUuid,
        String moderatorName,
        String reason,
        long createdAt,
        Long expiresAt
) {
}