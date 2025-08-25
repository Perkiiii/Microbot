package net.runelite.client.plugins.microbot.playerindicatorsextended;

public enum PlayerNameLocation {
   DISABLED("Disabled"),
   ABOVE_HEAD("Above head"),
   MODEL_CENTER("Center of model"),
   MODEL_RIGHT("Right of model");

   private final String name;

   public String toString() {
      return this.name;
   }

   private PlayerNameLocation(String name) {
      this.name = name;
   }
}
