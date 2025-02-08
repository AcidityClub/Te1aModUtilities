package net.hypixel.lynx.util;

import java.util.regex.Pattern;
import net.hypixel.lynx.Lynx;

public class Symbols {
   public static final String CHAT_COLOR = "\u00A7";
   public static final char CHAT_COLOR_CHAR = '\u00A7';
   public static final String CHAT_COLOR_RESET = "\u00A7r";
   public static final String NAME = "Te1a";
   public static final String TARGET_SYMBOL = "➤";
   public static final String CHECK_MARK = "✓";
   public static final String MULTIPLICATION = "⨉";
   public static final String BAN_LIST_DELIMITER = "Ω";
   private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\u00A7[0-9A-FK-OR]");

   public static String stripChatColor(String in) {
      return STRIP_COLOR_PATTERN.matcher(in).replaceAll("");
   }

   public interface Ident {
      String PROWLER_PREFIX = "\u00A7e[PROWLER] ";
      String CONFIG_DIR = (Lynx.DEBUGGING ? System.getenv("APPDATA").replace('\\', '/') + ".minecraft/" : "") + "hypixel-lynx/";
      String GLOBAL_CONFIG = CONFIG_DIR + "global-config.json";
   }
}
