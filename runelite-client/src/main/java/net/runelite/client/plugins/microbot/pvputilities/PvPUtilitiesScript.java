package net.runelite.client.plugins.microbot.pvputilities;

import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pvputilities.enums.SpellType;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PvPUtilitiesScript extends Script {
    public static double version = 1.2;

    private PvPUtilitiesConfig config;

    public boolean run(PvPUtilitiesConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !config.enablePlugin()) return;
                if (!Microbot.isLoggedIn()) return;

                // Main script loop - mostly idle, profiles are triggered by hotkeys

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    // -------- Profile execution methods --------
    public void executeProfile(int n) {
        try {
            switch(n) {
                case 1:
                    if (config.enableProfile1()) {
                        doActions(config.gear1(), config.prayers1(), config.spell1(),
                                config.useSpec1(), config.specThreshold1(), config.attack1());
                    }
                    break;
                case 2:
                    if (config.enableProfile2()) {
                        doActions(config.gear2(), config.prayers2(), config.spell2(),
                                config.useSpec2(), config.specThreshold2(), config.attack2());
                    }
                    break;
                case 3:
                    if (config.enableProfile3()) {
                        doActions(config.gear3(), config.prayers3(), config.spell3(),
                                config.useSpec3(), config.specThreshold3(), config.attack3());
                    }
                    break;
                case 4:
                    if (config.enableProfile4()) {
                        doActions(config.gear4(), config.prayers4(), config.spell4(),
                                config.useSpec4(), config.specThreshold4(), config.attack4());
                    }
                    break;
                case 5:
                    if (config.enableProfile5()) {
                        doActions(config.gear5(), config.prayers5(), config.spell5(),
                                config.useSpec5(), config.specThreshold5(), config.attack5());
                    }
                    break;
            }
        } catch (Exception e) {
            Microbot.log("[PvP] Profile " + n + " failed: " + e.getMessage());
        }
    }

    private void doActions(String gear, Rs2PrayerEnum[] prayers, SpellType spell,
                          boolean useSpec, int specThreshold, boolean attack) {
        Microbot.log("[PvP] Executing profile...");

        // 1. Equip gear
        if (!gear.isEmpty()) {
            Arrays.stream(gear.split(","))
                .map(String::trim)
                .forEach(item -> {
                    try {
                        if (item.matches("\\d+")) {
                            // Item ID
                            Rs2Inventory.equip(Integer.parseInt(item));
                        } else {
                            // Item name
                            Rs2Inventory.equip(item);
                        }
                        sleep(120, 200);
                    } catch (Exception ignored) {
                        // Continue if item not found
                    }
                });
        }

        // 2. Toggle prayers
        Arrays.stream(prayers).forEach(prayer -> {
            try {
                Rs2Prayer.toggle(prayer, true);
                sleep(100, 200);
            }
            catch (Exception ignored) {}
        });

        // 3. Cast spell - using the correct Rs2Magic.cast method
        if (spell != SpellType.NONE) {
            try {
                MagicAction magicAction = MagicAction.fromString(spell.getName());
                if (magicAction != null) {
                    Rs2Magic.cast(magicAction);
                }
                sleep(300, 400);
            }
            catch (Exception ignored) {}
        }

        // 4. Handle special attack
        if (useSpec) {
            Rs2Combat.setSpecState(true, specThreshold);
        }

        // 5. Attack current target - using the correct method
        if (attack && Rs2Player.getInteracting() != null) {
            try {
                if (Rs2Player.getInteracting() instanceof net.runelite.api.NPC) {
                    Rs2Npc.attack((net.runelite.api.NPC) Rs2Player.getInteracting());
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
