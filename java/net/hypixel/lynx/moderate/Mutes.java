package net.hypixel.lynx.moderate;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.tab.InputWindow;
import net.hypixel.lynx.config.ClientConfig;
import net.minecraft.client.Minecraft;

public enum Mutes implements Action, Expirable {
   SPAMMING("Spamming"),
   ASKING_FOR_TIPS("Asking for Personal Tips"),
   TROLLING("Trolling"),
   INAPPROPRIATE_CHAT("Inappropriate Chat"),
   NEGATIVE_BEHAVIOUR("Negative Behaviour"),
   VERBAL_ABUSE("Verbal Abuse"),
   DEATH_THREATS("Death Threats"),
   SCAMMING_OR_PHISHING("Scamming / Phishing"),
   ADVERTISING_SOCIAL_MEDIA("Advertising Social Media"),
   ADVERTISING_SERVER("Advertising Server/Domain"),
   ADVERTISING_WEBSITE("Advertising Website"),
   CUSTOM("Custom", true);

   private final String reason;
   private final boolean custom;

   private Mutes(String reason, boolean custom) {
      this.reason = reason;
      this.custom = custom;
   }

   private Mutes(String reason) {
      this(reason, false);
   }

   public void onAction(LynxUI ui, String username) {
      ui.addPrevious(this);
      ui.setOptionList(Mutes.MuteLengths.class);
   }

   public void onAction(LynxUI ui, String username, LengthsEnum length) {
      boolean custom = length.isCustomEntry();
      String def = "/mute " + username + " " + (custom ? "" : length.getLength() + " ") + (this.custom ? "" : this.reason);
      Minecraft.getMinecraft().displayGuiScreen((new InputWindow(def, custom ? (this.custom ? 10 : 11) + username.length() : def.length(), !custom || !this.custom)).onSubmit((s) -> {
         if (s.startsWith("/mute ")) {
            if ((Boolean)ClientConfig.CLOSE_AFTER_ACTION.as(Boolean.TYPE)) {
               Lynx.untarget();
            } else {
               ui.root();
            }
         }

      }));
   }

   static enum MuteLengths implements LengthsEnum {
      ONE_HOUR("1h"),
      ONE_DAY("1d"),
      THREE_DAY("3d"),
      SEVEN_DAYS("7d"),
      FOURTEEN_DAYS("14d"),
      CUSTOM((String)null);

      private final String length;

      private MuteLengths(String length) {
         this.length = length;
      }

      public String getLength() {
         return this.length;
      }

      public boolean isCustomEntry() {
         return this == CUSTOM;
      }
   }
}
