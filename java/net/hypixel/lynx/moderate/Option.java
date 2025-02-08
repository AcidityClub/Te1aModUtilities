package net.hypixel.lynx.moderate;

import com.codelanx.commons.util.Scheduler;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.chat.Tab;
import net.hypixel.lynx.chat.tab.PMWindow;
import net.hypixel.lynx.chat.tab.TabType;
import net.hypixel.lynx.ui.key.KeyBinding;
import net.hypixel.lynx.ui.key.KeyBindings;
import net.hypixel.lynx.util.Browsable;
import net.minecraft.util.ChatComponentText;

public enum Option implements Action {
   FOLLOW(KeyBindings.ACTION_FOLLOW, (ui, user) -> {
      ChatFacade.get().sendMessage("/fol " + user, false);
   }),
   CHECK(KeyBindings.ACTION_CHECK, (ui, user) -> {
      ChatFacade.get().sendMessage("/c " + user, false);
   }),
   REPORT(KeyBindings.ACTION_MMC_REPORT, (ui, user) -> {
      ChatFacade.get().sendMessage("/report " + user, false);
   }),
   /*
   TUNNEL_TO(KeyBindings.ACTION_TUNNEL_TO, (ui, user) -> {
      String server;
      if (ui.getMetaKeys() != null && !(server = (String)ui.getMetaKeys().get("current-server")).contains("Offline")) {
         ChatFacade.get().sendMessage("/tunnel " + server, false);
      } else {
         ChatFacade.get().printChatMessage(new ChatComponentText("\u00A7cThat player's not in a server"));
      }

   }),
    */
   BAN(KeyBindings.ACTION_BANS, true, (ui, user) -> {
      ui.setOptionList(Bans.class);
   }),
   MUTE(KeyBindings.ACTION_MUTE, true, (ui, user) -> {
      ui.setOptionList(Mutes.class);
   }),
   /*
   MACRO(KeyBindings.ACTION_MACRO, true, (ui, user) -> {
      ui.setOptionList(GuideSelect.class);
   }),
   AURABOT(KeyBindings.ACTION_AURABOT, (ui, user) -> {
      ChatFacade.get().sendMessage("/aurabot " + user, false);
   }),
   AURA_PANIC(KeyBindings.ACTION_AURAPANIC, (ui, user) -> {
      ChatFacade.get().sendMessage("/acpanic " + user + " 5", false);
   }),
   TEST_ANTIKB(KeyBindings.ACTION_TESTANTIKB, (ui, user) -> {
      ChatFacade.get().sendMessage("/testkb " + user, false);
   }),
   PULL(KeyBindings.ACTION_PULL, (ui, user) -> {
      ChatFacade.get().sendMessage("/pull " + user, false);
   }),
   REPORT_USER(KeyBindings.ACTION_REPORT, true, (ui, user) -> {
      ui.setOptionList(Reporting.class);
   }),
   USERINFO(KeyBindings.ACTION_USERINFO, (ui, user) -> {
      ChatFacade.get().sendMessage("/userinfo -f " + user, false);
   }),
   VIEW_BANS(KeyBindings.ACTION_VIEWBANS, (ui, user) -> {
      ChatFacade.get().sendMessage("/bans " + user, false);
   }),
   GOLIATH(KeyBindings.ACTION_GOLIATH, (ui, user) -> {
      Scheduler.runAsyncTask(() -> {
         Browsable.goliath(user);
      });
   }),
    */
   JUMP_TO(KeyBindings.ACTION_MMC_JUMPTO, (ui, user) -> {
      ChatFacade.get().sendMessage("/jtp " + user, false);
   }),
   TOGGLE_REACH_ALERTS(KeyBindings.ACTION_TOGGLE_REACH_ALERTS, (ui, user) -> {
      Lynx.get().setDoingReachCheckForTarget(!Lynx.get().isDoingReachCheckForTarget());

      if (Lynx.get().isDoingReachCheckForTarget()) {
         ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU] " + "You have toggled reach alerts \u00A7bon\u00A73."));
      }
      else {
         ChatFacade.get().printChatMessage(new ChatComponentText("\u00A73[TMU] " + "You have toggled reach alerts \u00A7boff\u00A73."));
      }
   }),

   PING(KeyBindings.ACTION_PING, (ui, user) -> {
      ChatFacade.get().sendMessage("/ping " + user, false);
   });





   private final KeyBinding key;
   private final Action onExec;
   private final boolean addToPrev;
   private final boolean admin;

   private Option(int key, Action onExec) {
      this(key, false, onExec);
   }

   private Option(KeyBinding key, Action onExec, boolean admin) {
      this(key, false, onExec, admin);
   }

   private Option(int key, boolean addToPrev, Action onExec) {
      this(KeyBinding.of(key), addToPrev, onExec, false);
   }

   private Option(KeyBinding key, Action onExec) {
      this(key, false, onExec, false);
   }

   private Option(KeyBinding key, boolean addToPrev, Action onExec, boolean admin) {
      this.key = key;
      this.addToPrev = addToPrev;
      this.onExec = onExec;
      this.admin = admin;
   }

   private Option(KeyBinding key, boolean addToPrev, Action onExec) {
      this(key, addToPrev, onExec, false);
   }

   public void onAction(LynxUI ui, String username) {
      if (this.addToPrev) {
         ui.addPrevious(this);
      }

      this.onExec.onAction(ui, username);
   }

   public boolean isPageable() {
      return false;
   }

   public KeyBinding getKeyBinding() {
      return this.key;
   }

   public boolean isAdminOption() {
      return this.admin;
   }
}
