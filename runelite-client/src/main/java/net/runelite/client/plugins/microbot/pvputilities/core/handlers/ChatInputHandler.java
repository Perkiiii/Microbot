package net.runelite.client.plugins.microbot.pvputilities.core.handlers;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Microbot;

/**
 * Handles chat input detection to suspend hotkeys while typing
 */
@Slf4j
public class ChatInputHandler {

    private boolean chatActive = false;

    /**
     * Updates the chat input state every game tick
     */
    public void updateChatInputState() {
        boolean wasActive = chatActive;
        chatActive = isTypingInChat();

        // Log state changes for debugging
        if (wasActive != chatActive) {
            log.debug("Chat input state changed: {}", chatActive ? "ACTIVE" : "INACTIVE");
        }
    }

    /**
     * Checks if the player is currently typing in chat
     */
    private boolean isTypingInChat() {
        try {
            // Check for active chat input with text content
            Widget chatboxInput = Microbot.getClient().getWidget(WidgetInfo.CHATBOX_INPUT);
            if (chatboxInput != null && !chatboxInput.isHidden()) {
                String inputText = chatboxInput.getText();
                if (inputText != null) {
                    // IGNORE the default "Press Enter to Chat..." prompt - this is NOT active input
                    if (inputText.contains("Press Enter to Chat...") || inputText.contains("Press Enter to Chat")) {
                        return false;
                    }

                    // Only consider it active typing if there's a cursor indicator "*"
                    // AND it's not just the empty prompt AND it's not the default prompt
                    if (inputText.contains("*") && !inputText.trim().equals("*") &&
                        !inputText.equals("Enter amount:") && !inputText.equals("Enter name:")) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking chat input state: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns whether chat is currently active
     */
    public boolean isChatActive() {
        return chatActive;
    }
}
