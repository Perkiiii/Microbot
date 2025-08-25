package net.runelite.client.plugins.microbot.pvputilities.core.managers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced target management system for PvP combat
 * Handles target detection, persistence, and state management
 */
@Slf4j
public class TargetManager {

    private static Actor currentTarget = null;
    private static Actor lastKnownOpponent = null;
    private static long lastInteractionTime = 0;
    private static boolean inCombat = false;
    private static final long TARGET_PERSISTENCE_MS = 10000; // 10 seconds

    /**
     * Gets the current target
     */
    public static Actor getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Gets the last known opponent
     */
    public static Actor getLastKnownOpponent() {
        return lastKnownOpponent;
    }

    /**
     * Checks if currently in combat
     */
    public static boolean isInCombat() {
        return inCombat;
    }

    /**
     * Sets the current target with persistence tracking
     */
    public static void setTargetWithPersistence(Actor target, String reason) {
        if (target == null) return;

        currentTarget = target;
        lastKnownOpponent = target;
        lastInteractionTime = System.currentTimeMillis();
        inCombat = true;

        log.debug("Target set: {} - Reason: {}", target.getName(), reason);
    }

    /**
     * Updates combat state based on player's interacting status
     */
    public static void updateCombatState(Actor interacting) {
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer == null) return;

        long currentTime = System.currentTimeMillis();

        if (interacting != null && interacting instanceof Player) {
            // Player is interacting with someone
            setTargetWithPersistence(interacting, "Direct interaction");
        } else if (currentTime - lastInteractionTime > TARGET_PERSISTENCE_MS) {
            // Target persistence has expired
            if (inCombat) {
                log.debug("Target persistence expired, clearing combat state");
                inCombat = false;
                currentTarget = null;
            }
        }
    }

    /**
     * Gets the best available target for attacking
     * Prioritizes current target, then last known opponent
     */
    public static Actor getBestAvailableTarget() {
        // First priority: current target if still valid
        if (currentTarget != null && isTargetValid(currentTarget)) {
            return currentTarget;
        }

        // Second priority: last known opponent if still valid
        if (lastKnownOpponent != null && isTargetValid(lastKnownOpponent)) {
            log.debug("Using last known opponent as target: {}", lastKnownOpponent.getName());
            return lastKnownOpponent;
        }

        // No valid target available
        return null;
    }

    /**
     * Checks if a target is still valid for attacking
     */
    public static boolean isTargetValid(Actor target) {
        if (target == null) return false;

        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer == null) return false;

        // Check if target is still a player and exists
        if (!(target instanceof Player)) return false;

        Player targetPlayer = (Player) target;

        // Check if target is still in the game world
        if (targetPlayer.getWorldLocation() == null) return false;

        // Check if target is within reasonable distance (e.g., same region)
        int distance = localPlayer.getWorldLocation().distanceTo(targetPlayer.getWorldLocation());
        if (distance > 20) return false; // Arbitrary reasonable distance

        // Check if target is dead (health ratio 0 or less)
        if (targetPlayer.getHealthRatio() <= 0) return false;

        return true;
    }

    /**
     * Alias for backward compatibility
     */
    public static boolean isValidTarget(Actor target) {
        return isTargetValid(target);
    }

    /**
     * Cleans up stale targets - removes invalid targets from memory
     */
    public static void cleanupStaleTargets() {
        if (currentTarget != null && !isTargetValid(currentTarget)) {
            log.debug("Cleaning up stale current target: {}", currentTarget.getName());
            currentTarget = null;
        }

        if (lastKnownOpponent != null && !isTargetValid(lastKnownOpponent)) {
            log.debug("Cleaning up stale last known opponent: {}", lastKnownOpponent.getName());
            lastKnownOpponent = null;
        }

        // Reset combat state if no valid targets
        if (currentTarget == null && lastKnownOpponent == null) {
            inCombat = false;
        }
    }

    /**
     * Sets combat state
     */
    public static void setInCombat(boolean combat) {
        inCombat = combat;
        if (combat) {
            lastInteractionTime = System.currentTimeMillis();
        }
    }

    /**
     * Handles interaction changes for target tracking
     */
    public static void onInteractingChanged(Actor source, Actor target) {
        Player localPlayer = Microbot.getClient().getLocalPlayer();
        if (localPlayer == null) return;

        // If local player started interacting with someone
        if (source == localPlayer && target instanceof Player) {
            setTargetWithPersistence(target, "Player initiated interaction");
        }
        // If someone started interacting with the local player
        else if (target == localPlayer && source instanceof Player) {
            setTargetWithPersistence(source, "Incoming interaction");
        }
    }

    /**
     * Clears all target state (used when plugin shuts down)
     */
    public static void clearAllTargetState() {
        currentTarget = null;
        lastKnownOpponent = null;
        lastInteractionTime = 0;
        inCombat = false;
        log.debug("All target state cleared");
    }

    /**
     * Multi-priority target selection logic:
     * 1. Current interacting target
     * 2. Current target (if still valid)
     * 3. Last known opponent (within persistence window)
     * 4. Find new target nearby
     */
    public static void updateCurrentTarget() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                return;
            }

            // Clean up stale targets first
            cleanupStaleTargets();

            // Priority 1: Current interacting target
            if (localPlayer.getInteracting() instanceof Player) {
                Player interactingTarget = (Player) localPlayer.getInteracting();
                if (isTargetValid(interactingTarget)) {
                    setTargetWithPersistence(interactingTarget, "Interacting target");
                    return;
                }
            }

            // Priority 2: Current target (if still valid)
            if (currentTarget != null && isTargetValid(currentTarget)) {
                log.debug("Maintaining current target: {}", currentTarget.getName());
                return;
            }

            // Priority 3: Last known opponent (within persistence window)
            if (lastKnownOpponent != null && isTargetValid(lastKnownOpponent)) {
                long timeSinceInteraction = System.currentTimeMillis() - lastInteractionTime;
                if (timeSinceInteraction <= TARGET_PERSISTENCE_MS) {
                    currentTarget = lastKnownOpponent;
                    log.debug("Restored target from persistence: {} ({}ms remaining)",
                        lastKnownOpponent.getName(),
                        TARGET_PERSISTENCE_MS - timeSinceInteraction);
                    return;
                }
            }

            // Priority 4: Find new target nearby
            Actor newTarget = findBestTarget();
            if (newTarget != null) {
                setTargetWithPersistence(newTarget, "Auto-detected nearby");
            }

        } catch (Exception e) {
            log.error("Failed to update current target: {}", e.getMessage());
        }
    }

    /**
     * Find the best target for attacking based on proximity and validity
     */
    public static Actor findBestTarget() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) return null;

            // Get all nearby players (excluding self)
            List<Player> nearbyPlayers = new ArrayList<>();

            // This would typically use Rs2Player.getPlayers() but we need to adapt for the target system
            // For now, we'll return null and let the calling code handle target detection
            // In a real implementation, this would scan for nearby valid PvP targets

            return null;
        } catch (Exception e) {
            log.error("Error finding best target: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets debug information about current target state
     */
    public static String getTargetDebugInfo() {
        return String.format("Current: %s, Last: %s, InCombat: %s, LastInteraction: %dms ago",
            currentTarget != null ? currentTarget.getName() : "None",
            lastKnownOpponent != null ? lastKnownOpponent.getName() : "None",
            inCombat,
            System.currentTimeMillis() - lastInteractionTime
        );
    }

    /**
     * Executes sophisticated walk under functionality with advanced target management
     * Based on the original working implementation from PvP Utilities Old
     */
    public static void executeWalkUnder() {
        try {
            Player localPlayer = Microbot.getClient().getLocalPlayer();
            if (localPlayer == null) {
                log.debug("Local player not found");
                return;
            }

            // Priority 1: Use current interacting target (most reliable)
            Player targetPlayer = null;
            if (localPlayer.getInteracting() instanceof Player) {
                targetPlayer = (Player) localPlayer.getInteracting();
                log.debug("Using interacting target: {}", targetPlayer.getName());
            }
            // Priority 2: Use advanced target management system as fallback
            else {
                updateCurrentTarget();
                if (currentTarget == null) {
                    currentTarget = findBestTarget();
                }

                if (currentTarget instanceof Player) {
                    targetPlayer = (Player) currentTarget;
                    log.debug("Using current target: {}", targetPlayer.getName());
                } else {
                    log.debug("No valid player target available to walk under");
                    return;
                }
            }

            if (targetPlayer == null || !isTargetValid(targetPlayer)) {
                log.debug("Target is invalid or not found");
                return;
            }

            // Get target's world location
            WorldPoint targetLocation = targetPlayer.getWorldLocation();
            if (targetLocation == null) {
                log.debug("Could not get target's world location");
                return;
            }

            // Distance check - should be reasonably close for walk under
            int distance = targetLocation.distanceTo(localPlayer.getWorldLocation());
            if (distance > 10) {
                log.debug("Target too far away for walk under (distance: {} tiles)", distance);
                return;
            }

            if (distance == 0) {
                log.debug("Already on same tile as target: {}", targetPlayer.getName());
                return;
            }

            // Use the simple and reliable Rs2Walker.walkFastCanvas method
            log.debug("Walking under {} at {}", targetPlayer.getName(), targetLocation);
            Rs2Walker.walkFastCanvas(targetLocation);
            log.debug("Walk under command executed successfully");

        } catch (Exception e) {
            log.error("Error in walk under execution: {}", e.getMessage());
        }
    }

    /**
     * Walks under the specified target if valid and within range
     * @deprecated Use executeWalkUnder() instead for the full sophisticated implementation
     */
    @Deprecated
    public static void walkUnderTarget() {
        executeWalkUnder();
    }
}
