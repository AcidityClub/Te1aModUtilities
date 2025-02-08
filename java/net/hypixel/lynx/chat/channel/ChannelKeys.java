package net.hypixel.lynx.chat.channel;

import java.util.UUID;
import net.hypixel.lynx.util.Symbols;

public class ChannelKeys {
   public static class ViewBans {
      public static final String BANS = "Ban";
      public static final String TEMPBANS = "Tempban";
      public static final String KICKS = "Kick";

      public static ChannelKeys.ViewBans.BanInfo getInfo(String ban) {
         return new ChannelKeys.ViewBans.BanInfo(ban);
      }

      public static final class BanInfo {
         private final String reason;
         private final boolean cancelled;
         private final boolean active;
         private final long lengthMS;
         private final UUID banner;
         private final long start;

         private BanInfo(String info) {
            String[] parts = info.split("Ω");
            if (parts[0].equalsIgnoreCase("[WATCHDOG]")) {
               parts[0] = "\u00A7c" + parts[0];
            } else if (parts[0].contains("Compromised Account")) {
               parts[0] = "Compromised Account";
            }

            this.reason = Symbols.stripChatColor(parts[0].split("(\n|, visit|\\.)")[0]);
            this.cancelled = Boolean.valueOf(parts[1]);
            this.active = Boolean.valueOf(parts[2]);
            this.lengthMS = parts[3].equals("null") ? -1L : Long.parseLong(parts[3]);
            this.banner = UUID.fromString(parts[4]);
            this.start = Long.parseLong(parts[5]);
         }

         public String getReason() {
            return this.reason;
         }

         public boolean isCancelled() {
            return this.cancelled;
         }

         public boolean isPunished() {
            return !this.isCancelled();
         }

         public boolean isActive() {
            return this.active;
         }

         public boolean isPermanent() {
            return this.lengthMS < 0L;
         }

         public UUID getBanner() {
            return this.banner;
         }

         public long getStart() {
            return this.start;
         }

         public long getDuration() {
            return this.lengthMS;
         }

         public long getRemainderMS() {
            return this.start + this.lengthMS - System.currentTimeMillis();
         }

         // $FF: synthetic method
         BanInfo(String x0, Object x1) {
            this(x0);
         }
      }
   }

   public static class UserInfo {
      public static final String DISPLAY_NAME = "display-name";
      public static final String RECENT_NAME = "recent-name";
      public static final String UUID = "uuid";
      public static final String RANK = "rank";
      public static final String PACKAGE_RANK = "package-rank";
      public static final String PACKAGE_RANK_OLD = "old-package-rank";
      public static final String NETWORK_LEVEL = "network-level";
      public static final String NETWORK_EXP = "network-exp";
      public static final String GUILD = "guild";
      public static final String CURRENT_SERVER = "current-server";
      public static final String FIRST_LOGIN = "first-login";
      public static final String LAST_LOGIN = "last-login";
      public static final String PACKAGES = "packages";
      public static final String BOOSTERS = "boosters";
      public static final String BANS = "bans";
      public static final String MUTES = "mutes";
      public static final String KICKS = "kicks";
   }
}
