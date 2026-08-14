package com.cmax.simpleStaffGUI.service;

import com.cmax.simpleStaffGUI.SimpleStaffGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public final class TextService {

    private final SimpleStaffGUI plugin;

    private final MiniMessage miniMessage =
            MiniMessage.miniMessage();

    private YamlConfiguration messages;

    public TextService(SimpleStaffGUI plugin) {

        this.plugin = plugin;

        reload();
    }

    public void reload() {

        File file =
                new File(
                        plugin.getDataFolder(),
                        "messages.yml"
                );

        messages =
                YamlConfiguration.loadConfiguration(file);
    }

    public Component get(String path) {

        return miniMessage.deserialize(
                messages.getString(path, "")
        );
    }

    public Component get(
            String path,
            Map<String, String> replacements
    ) {

        String message =
                messages.getString(path, "");

        for (Map.Entry<String, String> entry :
                replacements.entrySet()) {

            message = message.replace(
                    "%" + entry.getKey() + "%",
                    entry.getValue()
            );
        }

        return miniMessage.deserialize(message);
    }

    public Component raw(String message) {

        return miniMessage.deserialize(
                message == null ? "" : message
        );
    }
}