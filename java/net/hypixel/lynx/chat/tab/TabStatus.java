package net.hypixel.lynx.chat.tab;

public enum TabStatus {
   ACTIVE('a'),
   INACTIVE('7'),
   NEW_MESSAGES('e'),
   PINGED('c');

   private final String color;

   private TabStatus(char color) {
      this.color = "\u00A7" + color;
   }

   public String getPrefix() {
      return this.color;
   }
}
