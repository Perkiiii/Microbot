package net.runelite.client.plugins.microbot.pvputilities.core.handlers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.pvputilities.core.managers.TargetManager;

/**
 * Handles interaction events for target management
 */
@Slf4j
public class InteractionHandler {

    /**
     * Processes interaction changed events for target detection
     */
    public void onInteractingChanged(InteractingChanged event) {
        // Defensive checks to prevent issues with other plugins
        if (!Microbot.isLoggedIn() || event == null) {
            return;
        }

        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return;
            }

            // Additional null checks for event data
            Actor source = event.getSource();
            Actor target = event.getTarget();

            if (source == null) {
                return;
            }

            // Clean up stale targets before processing new interaction
            TargetManager.cleanupStaleTargets();

            // Case 1: Local player starts interacting with someone
            if (source.equals(localPlayer) && target instanceof Player) {
                Player newTarget = (Player) target;
                if (TargetManager.isValidTarget(newTarget)) {
                    TargetManager.setTargetWithPersistence(newTarget, "Player interaction");
                    TargetManager.setInCombat(true);
                }
            }
            // Case 2: Someone starts interacting with local player
            else if (target != null && target.equals(localPlayer) && source instanceof Player) {
                Player newTarget = (Player) source;
                if (TargetManager.isValidTarget(newTarget)) {
                    TargetManager.setTargetWithPersistence(newTarget, "Being attacked");
                    TargetManager.setInCombat(true);
                }
            }
            // Case 3: Interaction ends (target becomes null)
            else if (source.equals(localPlayer) && target == null) {
                if (TargetManager.getCurrentTarget() != null) {
                    log.debug("Interaction ended with {} - maintaining persistence",
                             TargetManager.getCurrentTarget().getName());
                    TargetManager.setInCombat(false);
                }
            }
        } catch (Exception e) {
            // Enhanced error logging to help debug issues
            log.error("Error in interaction handler: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            // Don't let exceptions in our event handler crash other plugins
        }
    }
}
