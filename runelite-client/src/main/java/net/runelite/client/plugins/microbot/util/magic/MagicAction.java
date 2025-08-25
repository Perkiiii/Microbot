package net.runelite.client.plugins.microbot.util.magic;

import java.util.HashMap;
import java.util.Map;

public class MagicAction {
    private static final Map<String, MagicAction> actions = new HashMap<>();
    private final String name;

    static {
        // Add supported spells here (expand as needed)
        register("Wind Strike");
        register("Water Strike");
        register("Earth Strike");
        register("Fire Strike");
        register("Wind Bolt");
        register("Water Bolt");
        register("Earth Bolt");
        register("Fire Bolt");
        register("Wind Blast");
        register("Water Blast");
        register("Earth Blast");
        register("Fire Blast");
        register("Wind Wave");
        register("Water Wave");
        register("Earth Wave");
        register("Fire Wave");
        register("Wind Surge");
        register("Water Surge");
        register("Earth Surge");
        register("Fire Surge");
        register("Ice Rush");
        register("Ice Burst");
        register("Ice Blitz");
        register("Ice Barrage");
        register("Blood Rush");
        register("Blood Burst");
        register("Blood Blitz");
        register("Blood Barrage");
        register("Shadow Rush");
        register("Shadow Burst");
        register("Shadow Blitz");
        register("Shadow Barrage");
        register("Smoke Rush");
        register("Smoke Burst");
        register("Smoke Blitz");
        register("Smoke Barrage");
    }

    private static void register(String name) {
        actions.put(name.toLowerCase(), new MagicAction(name));
    }

    public static MagicAction fromString(String name) {
        if (name == null) return null;
        return actions.get(name.toLowerCase());
    }

    public MagicAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

