package net.hypixel.lynx.moderate;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.tab.InputWindow;
import net.hypixel.lynx.config.ClientConfig;
import net.minecraft.client.Minecraft;

public enum Bans implements Action, Expirable {
   BLACKLISTED_MODIFICATIONS("Blacklisted Modifications"),
   COMPROMISED_ACCOUNT(true, "Compromised Account"),
   SERVER_ADVERTISING("Server Advertising"),
   VERBAL_ABUSE("Verbal Abuse"),
   DEATH_THREATS("Death Threats"),
   GAMEPLAY_RULE_BREAKING("Gameplay Rule Breaking", true, Bans.GameplayRuleBreaking.class),
   EXTREME_NEGATIVE_BEHAVIOUR("Extreme Negative Behaviour"),
   INAPPROPRIATE_AESTHETICS("Inappropriate Aesthetics", true, Bans.InappropriateAesthetics.class),
   INAPPROPRIATE_CONTENT("Inappropriate Content"),
   INAPPROPRIATE_PLAYER_NAME("Inappropriate Name. Please change then appeal"),
   FALSIFIED_INFORMATION("Falsified Information"),
   CUSTOM("Custom");

   private final boolean perma;
   private final String reason;
   private boolean openable;
   private Class<? extends Enum> subMenu;

   private Bans(String reason) {
      this(false, reason, false, (Class)null);
   }

   private Bans(boolean perma, String reason) {
      this(perma, reason, false, (Class)null);
   }

   private Bans(boolean perma, String reason, boolean openable, Class<? extends Enum> subMenu) {
      this.perma = perma;
      this.reason = reason;
      this.openable = openable;
      this.subMenu = subMenu;
   }

   private Bans(String reason, boolean openable, Class<? extends Enum> subMenu) {
      this(false, reason, openable, subMenu);
   }

   public void onAction(LynxUI ui, String username) {
      if (this.perma) {
         this.onAction(ui, username, Bans.BanLengths.PERMANENT);
      } else if (this.openable) {
         ui.addPrevious(this);
         ui.setOptionList(this.subMenu);
      } else {
         if (this.reason.equalsIgnoreCase("Inappropriate Name. Please change then appeal")) {
            this.onAction(ui, username, Bans.ExtraLengths.FOURTY_DAY);
            return;
         }

         ui.addPrevious(this);
         ui.setOptionList(Bans.BanLengths.class);
      }

   }

   public void onAction(LynxUI ui, String username, LengthsEnum length) {
      String reason = this.reason.equalsIgnoreCase("Custom") ? "" : this.reason;
      boolean isCustomReason = this.reason.equalsIgnoreCase("Custom");
      String msg;
      int pos;
      if (length.getLength() == null && !length.isCustomEntry()) {
         msg = "/ban " + username + " " + reason;
         pos = msg.length() + 1;
      } else {
         msg = "/tb " + username + " " + (length.isCustomEntry() ? "" : length.getLength()) + " " + reason;
         if (length.getLength() != null) {
            pos = isCustomReason ? 10 + username.length() + length.getLength().length() + 1 : 10 + username.length();
         } else {
            pos = isCustomReason ? 10 + username.length() : 10 + username.length();
         }
      }

      doSubmitToWindow(ui, msg, pos, !isCustomReason);
   }

   public static void doSubMenuOnAction(LynxUI ui, String username, LengthsEnum length, String reason) {
      String msg;
      int pos;
      if (length.getLength() == null && !length.isCustomEntry()) {
         msg = "/ban " + username + " " + reason;
         pos = msg.length();
      } else {
         msg = "/tempban " + username + " " + (length.isCustomEntry() ? "" : length.getLength()) + " " + reason;
         pos = 10 + username.length();
      }

      doSubmitToWindow(ui, msg, pos, !length.isCustomEntry());
   }

   public static void doSubmitToWindow(LynxUI ui, String msg, int pos, boolean lock) {
      Minecraft.getMinecraft().displayGuiScreen((new InputWindow(msg, pos, lock)).onSubmit((s) -> {
         if (s.startsWith("/ban") || s.startsWith("/tempban")) {
            if ((Boolean)ClientConfig.CLOSE_AFTER_ACTION.as(Boolean.TYPE)) {
               Lynx.untarget();
            } else {
               ui.root();
            }
         }

      }));
   }

   static enum BanLengths implements LengthsEnum {
      ONE_DAY("1d"),
      THREE_DAY("3d"),
      SEVEN_DAY("7d"),
      FOURTEEN_DAY("14d"),
      THIRTY_DAY("30d"),
      NINETY_DAY("90d"),
      ONEEIGHTY_DAY("180d"),
      CUSTOM((String)null),
      PERMANENT((String)null);

      private final String length;

      private BanLengths(String length) {
         this.length = length;
      }

      public String getLength() {
         return this.length;
      }

      public boolean isCustomEntry() {
         return this == CUSTOM;
      }
   }

   static enum ExtraLengths implements LengthsEnum {
      FOURTY_DAY("40d");

      private String length;

      private ExtraLengths(String length) {
         this.length = length;
      }

      public String getLength() {
         return this.length;
      }

      public boolean isCustomEntry() {
         return false;
      }
   }

   static enum GameplayRuleBreaking implements Action, Expirable {
      TEAMING_IN_SOLO("Teaming in Solo"),
      CROSS_TEAMING("Crossteaming"),
      INAPPROPRIATE_BUILD("Inappropriate Build"),
      INAPPROPRIATE_ITEM_NAME("Inappropriate Item Name");

      private String reason;

      private GameplayRuleBreaking(String reason) {
         this.reason = reason;
      }

      public String getReason() {
         return this.reason;
      }

      public void onAction(LynxUI ui, String username) {
         ui.addPrevious(this);
         ui.setOptionList(Bans.BanLengths.class);
      }

      public void onAction(LynxUI ui, String username, LengthsEnum length) {
         Bans.doSubMenuOnAction(ui, username, length, this.reason);
      }
   }

   static enum InappropriateAesthetics implements Action, Expirable {
      INAPPROPRIATE_CAPE("Inappropriate Cape. Change before returning"),
      INAPPROPRIATE_SKIN("Inappropriate Skin. Change before returning");

      private String reason;

      private InappropriateAesthetics(String reason) {
         this.reason = reason;
      }

      public String getReason() {
         return this.reason;
      }

      public void onAction(LynxUI ui, String username, LengthsEnum length) {
         Bans.doSubMenuOnAction(ui, username, length, this.reason);
      }

      public void onAction(LynxUI ui, String username) {
         ui.addPrevious(this);
         ui.setOptionList(Bans.BanLengths.class);
      }
   }
}
