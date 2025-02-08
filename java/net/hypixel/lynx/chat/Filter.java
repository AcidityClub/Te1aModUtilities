package net.hypixel.lynx.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.hypixel.lynx.util.Symbols;

public interface Filter {
   Pattern getPattern();

   boolean usesColor();

   default boolean applicable(String chat) {
      Matcher m = this.getMatcher(chat);
      return m != null && m.matches();
   }

   default Matcher getMatcher(String chat) {
      if (chat != null && this.getPattern() != null) {
         if (!this.usesColor()) {
            chat = Symbols.stripChatColor(chat);
         }

         return this.getPattern().matcher(chat);
      } else {
         return null;
      }
   }

   boolean hasUsernameGroup();
}
