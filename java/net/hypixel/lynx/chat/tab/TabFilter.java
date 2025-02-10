package net.hypixel.lynx.chat.tab;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.hypixel.lynx.chat.Filter;
import net.hypixel.lynx.util.DependencyUtil;
import net.minecraft.util.IChatComponent;

public enum TabFilter implements Filter {
   CHEAT_FEEDBACK("((Testing, please wait.*)|(Hit the player ([0-9]*) times!.*)|(Hit them ([0-9]*)\\/([0-9]*))|(Tested '([A-Za-z0-9_]{3,16})' for anti kb!))"),
   STAFF_JOIN("\\[STAFF\\] (\\[[A-Za-z0-9\\-\\s]+\\]+\\s)*[A-Za-z0-9_]{3,16} (joined|disconnected)\\."),
   LOBBY_JOIN("(\\[[A-Za-z0-9\\-\\s]+\\+?\\]+\\s)*[A-Za-z0-9_]{3,16} joined the lobby\\!"),
   FRIEND_JOIN("[A-Za-z0-9_]{3,16} (joined|disconnected)\\."),
   WATCHDOG_REPORT("\\[STAFF\\] \\[ADMIN\\] Fishy: (?<text>((Report of )(?<username>[A-Za-z0-9_]{3,16}))?.*)"),
   JUNKMAIL("\\[STAFF\\] \\[ADMIN\\] JunkMail: (?<text>((Report of )(?<username>[A-Za-z0-9_]{3,16}))?.*)"),
   STAFF("\\[STAFF\\] (\\[(WEB|SLACK)\\] )?(\\[(HELPER|MOD|ADMIN|OWNER|BUILD TEAM(\\+?))\\]) [A-Za-z0-9_]{3,16}: (?<text>(([Rr][Ee][Pp][Oo][Rr][Tt]([Ss]?) (of |on )?)(?<username>[A-Za-z0-9_]{3,16}))?.*)"),
   WATCHDOG("\\[STAFF\\] \\[WATCHDOG\\].*"),
   STAFF_ACTIONS("\\[STAFF\\].*"),
   NCP(false, ".*\u00A7cNCP: (\u00A7[fr])+(Your configuration|(?<username>[A-Za-z0-9_]{3,16})) .*"),
   PRIVATE_MESSAGE(false, "(\u00A7dFrom |\u00A7dTo )(?<text>.*)"),
   TUNNEL(false, "([\u00A7ar]+Tunneled to [a-z0-9]+|[\u00A75r]+\\[T\\] (\\* )?(([\u00A7a-fr0-9]*\\[[\u00A7A-Za-z✫0-9\\-\\s]+(\\+([\u00A7rab]*))?\\][\u00A7a-f0-9]*\\s)*)(([\u00A7r7]{4})?)(?<username>[A-Za-z0-9_]{3,16})([\u00A7rf7]*)?\\: (?<text>.*))"),
   CHAT("(\\[\\W\\] )?(\\* )?(\\[[A-Z]{3}\\])?(([\u00A7a-fr0-9]*\\[[\u00A7A-Za-z✫0-9\\-\\s]+(\\+([\u00A7rab]*))?\\][\u00A7a-f0-9]*\\s)*)(([\u00A7r7]{4})?)(?<username>[A-Za-z0-9_]{3,16})([\u00A7rf7]*)?(\\s(?<tag>[A-Za-z0-9\\-\\s]+))?\\s*\\: (?<text>.*)"),
   PARTY("Party \\> (\\[[A-Za-z0-9\\-\\s]+\\+?\\]+\\s)*(?<username>[A-Za-z0-9_]{3,16}): (?<text>.*)"),
   GUILD("Guild \\> (\\[[A-Za-z0-9\\-\\s]+\\+?\\]+\\s)*(?<username>[A-Za-z0-9_]{3,16}): (?<text>.*)"),
   GENERAL(false, ".*");

   private static final TabFilter[] VALUES = values();
   private final boolean scrapColor;
   private final Pattern pattern;

   private TabFilter(String regex) {
      this(true, regex);
   }

   private TabFilter(boolean scrapColor, String regex) {
      this.scrapColor = scrapColor;
      this.pattern = regex == null ? null : Pattern.compile(regex);
   }

   public static void actFilter(IChatComponent chat, DependencyUtil.TriConsumer<TabFilter, String, String> filterAndUsername) {
      TabFilter filt = null;
      String username = null;
      String text = null;
      if (chat == null) {
         filt = GENERAL;
      } else {
         String message = chat.getFormattedText();
         TabFilter[] var6 = VALUES;
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            TabFilter f = var6[var8];
            Matcher m = f.getMatcher(message);
            if (m != null && m.matches()) {
               filt = f;
               if (f.hasUsernameGroup()) {
                  username = m.group("username");
               }

               if (f.hasTextGroup()) {
                  text = m.group("text");
               }
               break;
            }
         }

         if (filt == null) {
            filt = GENERAL;
         }
      }

      filterAndUsername.accept(filt, username, text);
   }

   public boolean usesColor() {
      return !this.scrapColor;
   }

   public Pattern getPattern() {
      return this.pattern;
   }

   public boolean hasUsernameGroup() {
      switch(this) {
      case CHAT:
      case TUNNEL:
      case WATCHDOG_REPORT:
      case JUNKMAIL:
      case PARTY:
      case GUILD:
      case STAFF:
         return true;
      default:
         return false;
      }
   }

   public boolean hasTextGroup() {
      switch(this) {
      case CHAT:
      case TUNNEL:
      case PARTY:
      case GUILD:
      case STAFF:
      case PRIVATE_MESSAGE:
         return true;
      case WATCHDOG_REPORT:
      case JUNKMAIL:
      default:
         return false;
      }
   }
}
