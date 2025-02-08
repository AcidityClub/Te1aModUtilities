package net.hypixel.lynx.config;

import com.codelanx.commons.util.Reflections;
import net.hypixel.lynx.Lynx;

public enum GameMode {
   COMMAND('a'),
   GAME('e'),
   DND('c');

   private final char prefix;

   private GameMode(char prefix) {
      this.prefix = prefix;
   }

   public GameMode next() {
      switch(this) {
      case COMMAND:
         return GAME;
      case GAME:
         return DND;
      case DND:
         return COMMAND;
      default:
         return COMMAND;
      }
   }

   public GameMode prev() {
      return this.next().next();
   }

   public String getDescription() {
      return "\u00A7" + this.prefix + "Current mode: " + Reflections.properEnumName(this) + "\u00A7r";
   }

   public String getFormattedName() {
      return "\u00A7" + this.prefix + Reflections.properEnumName(this);
   }

   public void onPM(String username) {
      switch(this) {
      case GAME:
         Lynx.getChat().sendPMWithDelays(true, username, "Sorry I'm currently in the middle of a game and not accepting new reports", "Please contact another staff member or report via www.hypixel.net/report");
         break;
      case DND:
         Lynx.getChat().sendPMWithDelays(true, username, "I'm currently dealing with another report and I will answer ASAP", "You can also report via www.hypixel.net/report with evidence");
      }

   }
}
