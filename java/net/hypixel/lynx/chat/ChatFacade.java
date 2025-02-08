package net.hypixel.lynx.chat;

import com.codelanx.commons.logging.Debugger;
import com.codelanx.commons.logging.Logging;
import com.codelanx.commons.util.RNG;
import com.codelanx.commons.util.Reflections;
import com.codelanx.commons.util.Scheduler;
import com.codelanx.commons.util.ref.Box;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.channel.CommandChannels;
import net.hypixel.lynx.chat.tab.InputWindow;
import net.hypixel.lynx.chat.tab.TabFilter;
import net.hypixel.lynx.chat.tab.TabStatus;
import net.hypixel.lynx.chat.tab.TabType;
import net.hypixel.lynx.chat.tab.Window;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.util.Util;
import net.hypixel.lynx.util.Versionable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Chat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatFacade extends GuiNewChat {
   private static final Tab addTab = new Tab("+") {
      public boolean click(int index, int x, int y, boolean rightClick) {
         return this.contains(index, x, y);
      }
   };
   private static final Field DEFAULT_TEXT_FIELD;
   private static boolean replace = true;
   private final List<String> sentMessages = new LinkedList();
   private final List<Tab> tabs = new ArrayList();
   private Tab active;
   private int currentTabIndex;
   private int translateY;
   private int translateX;

   public ChatFacade() {
      super(Minecraft.getMinecraft());
      this.active = TabType.GENERAL.newTab("All");
      this.currentTabIndex = 0;
      Logging.simple().print("raw tabs: %s", ClientConfig.TABS.get());
      List<Tab> tabs = (List)ClientConfig.TABS.as(List.class, Tab.class);
      Logging.simple().print("retrieved tabs: %s", tabs.toString());
      if (tabs.isEmpty()) {
         this.setupDefaultTabs();
      } else {
         this.tabs.addAll(tabs);
         this.setActiveTab((Tab)this.tabs.get(0));
      }

      Util.register(this, MinecraftForge.EVENT_BUS);
   }

   public static ChatFacade get() {
      return Lynx.getChat();
   }

   private void setupDefaultTabs() {
      Tab var10001 = this.active;
      for (TabFilter obj : TabFilter.values()) {
         var10001.filter(obj);
      }

      this.tabs.add(this.active);
      Tab t = TabType.GENERAL.newTab("Chat");
      t.filter(TabFilter.CHAT);
      this.tabs.add(t);
      t = TabType.GENERAL.newTab("Party");
      t.filter(TabFilter.PARTY);
      t.addChatRule("/pchat %s");
      this.tabs.add(t);
      t = TabType.GENERAL.newTab("Guild");
      t.filter(TabFilter.GUILD);
      t.addChatRule("/gchat %s");
      this.tabs.add(t);
      t = TabType.GENERAL.newTab("Tunnel");
      t.filter(TabFilter.TUNNEL);
      t.addChatRule("/c %s");
      this.tabs.add(t);
      t = TabType.GENERAL.newTab("Staff");
      t.filter(TabFilter.STAFF);
      t.filter(TabFilter.STAFF_ACTIONS);
      t.filter(TabFilter.WATCHDOG);
      t.filter(TabFilter.STAFF_JOIN);
      t.addChatRule("/s %s");
      this.tabs.add(t);
      t = TabType.PRIVATE_MESSAGES.newTab("Private Msg");
      t.filter(TabFilter.PRIVATE_MESSAGE);
      this.tabs.add(t);
      t = TabType.GENERAL.newTab("Feedback");
      t.filter(TabFilter.GENERAL);
      t.filter(TabFilter.NCP);
      this.tabs.add(t);
      /*
      t = TabType.GENERAL.newTab("WDR");
      t.filter(TabFilter.WATCHDOG_REPORT);
      t.filter(TabFilter.JUNKMAIL);
      t.filter(TabFilter.CHEAT_FEEDBACK);
      this.tabs.add(t);
       */
      this.active.setTabStatus(TabStatus.ACTIVE);
      ClientConfig.TABS.set(this.tabs);

      try {
         ClientConfig.TABS.save();
      } catch (IOException var3) {
         Debugger.error(var3, "Error saving default tabs");
      }

   }

   public Set<String> getGlobalPings() {
      List<String> one = (List)ClientConfig.PING_NOTIFICATIONS.as(List.class, String.class);
      List<String> two = Collections.singletonList(Minecraft.getMinecraft().getSession().getUsername().toLowerCase());
      return (Set)Stream.of(one, two).flatMap(Collection::stream).collect(Collectors.toSet());
   }

   @SubscribeEvent
   public void fixTranslation(Chat event) {
      this.translateX = event.posX;
      this.translateY = event.posY;
   }

   public InputWindow getInputWindow() {
      return Minecraft.getMinecraft().currentScreen instanceof InputWindow ? (InputWindow)Minecraft.getMinecraft().currentScreen : null;
   }

   public void deleteTab(Tab t) {
      if (this.tabs.size() > 1) {
         if (this.active.equals(t)) {
            this.shiftTab(this.currentTabIndex == 0);
         }

         this.tabs.remove(t);
      }
   }

   public Tab addTab() {
      Tab t = TabType.GENERAL.newTab();
      this.tabs.add(t);
      t.openMenu();
      return t;
   }

   public void shiftTab(boolean right) {
      if (right) {
         this.currentTabIndex = this.currentTabIndex >= this.tabs.size() - 1 ? 0 : this.currentTabIndex + 1;
      } else {
         this.currentTabIndex = this.currentTabIndex <= 0 ? this.tabs.size() - 1 : this.currentTabIndex - 1;
      }

      Tab t = (Tab)this.tabs.get(this.currentTabIndex);
      this.setActiveTab(t);
      this.active.getWindow().setLastTab(Minecraft.getMinecraft().ingameGUI.getUpdateCounter());
   }

   public void drawChat(int tick) {
      GuiScreen sc = Minecraft.getMinecraft().currentScreen;
      if (replace && sc instanceof GuiChat && !(sc instanceof InputWindow)) {
         String def = "";

         try {
            def = (String)DEFAULT_TEXT_FIELD.get(sc);
         } catch (IllegalAccessException var7) {
            Util.error(var7, "Error reading default chat input string");
         }

         try {
            Minecraft.getMinecraft().displayGuiScreen(new InputWindow(def));
         } catch (IllegalStateException var6) {
            replace = false;
            Util.error(var6, "Error replacing chat input screen");
         }
      }

      int i = 0;
      Iterator var4 = this.tabs.iterator();

      while(var4.hasNext()) {
         Tab tab = (Tab)var4.next();
         tab.drawTab(i++, tick);
      }

      addTab.drawTab(i++, tick);
   }

   public void click(int x, int y, boolean right) {
      x -= this.translateX;
      y -= this.translateY;
      int i = 0;
      Iterator var5 = this.tabs.iterator();

      Tab t;
      do {
         if (!var5.hasNext()) {
            if (addTab.click(i++, x, y, right)) {
               this.addTab();
            }

            return;
         }

         t = (Tab)var5.next();
      } while(!t.click(i++, x, y, right));

      --i;
      this.currentTabIndex = i;
   }

   public void hover(int x, int y) {
      x -= this.translateX;
      y -= this.translateY;
      int i = 0;
      Iterator var4 = this.tabs.iterator();

      while(var4.hasNext()) {
         Tab t = (Tab)var4.next();
         t.hover(i++, x, y);
      }

   }

   public int getTranslateY() {
      return this.translateY;
   }

   public int getTranslateX() {
      return this.translateX;
   }

   public List<Tab> getTabs() {
      return this.tabs;
   }

   public Tab getTab(int index) {
      return (Tab)this.tabs.get(index);
   }

   public Window nab() {
      return this.active.getWindow();
   }

   public Tab getActiveTab() {
      return this.active;
   }

   public void setActiveTab(Tab tab) {
      if (this.active != null) {
         this.active.setTabStatus(TabStatus.INACTIVE);
      }

      this.active = tab;
      tab.setTabStatus(TabStatus.ACTIVE);
      if (!this.getChatOpen()) {
         this.active.tabSwitched();
      }

   }

   public void clearChatMessages() {
      if ((Boolean)ClientConfig.CLEAR_CHAT_ON_LOGOUT.as(Boolean.TYPE)) {
         this.tabs.stream().map(Tab::getWindow).forEach(Window::clearChatMessages);
      }

   }

   public void printChatMessageWithOptionalDeletion(IChatComponent message, int id) {
      if (!CommandChannels.tryInput(message.getFormattedText())) {
         if (message.getFormattedText().startsWith("\u00A7e[PROWLER] ")) {
            Lynx.getInterpeter().interpret(message.getFormattedText().substring("\u00A7e[PROWLER] ".length()));
         } else {
            this.tabs.forEach((t) -> {
               t.printIfApplicable(message, id);
            });
         }

         Util.out("[CHAT] %s", message.getFormattedText());
      }

   }

   public void refreshChat() {
      this.nab().refreshChat();
   }

   public List<String> getSentMessages() {
      return (Boolean)ClientConfig.LAST_MESSAGE_GLOBAL.as(Boolean.TYPE) ? this.sentMessages : this.nab().getSentMessages();
   }

   public void addToSentMessages(String message) {
      if ((Boolean)ClientConfig.LAST_MESSAGE_GLOBAL.as(Boolean.TYPE)) {
         String last = this.sentMessages.isEmpty() ? null : (String)this.sentMessages.get(this.sentMessages.size() - 1);
         if (!message.equals(last)) {
            this.sentMessages.add(message);
         }
      } else {
         this.nab().addToSentMessages(message);
      }

   }

   public void resetScroll() {
      this.nab().resetScroll();
   }

   public void scroll(int linesScrolled) {
      this.nab().scroll(linesScrolled);
   }

   public IChatComponent getChatComponent(int width, int height) {
      return this.nab().getChatComponent(width, height);
   }

   public void deleteChatLine(int messageID) {
      this.nab().deleteChatLine(messageID);
   }

   public void sendMessage(String message, boolean history) {
      Minecraft mc = Minecraft.getMinecraft();
      if (history) {
         mc.ingameGUI.getChatGUI().addToSentMessages(message);
      }

      if (ClientCommandHandler.instance.executeCommand(mc.thePlayer, message) == 0) {
         mc.thePlayer.sendChatMessage(message);
      }

   }

   public void sendPMWithDelays(boolean initialDelay, String username, String... out) {
      String prefix = "/msg " + username + " ";
      if (out.length <= 0) {
         Util.out("Error: received empty sendPM for '%s'", Reflections.getCaller());
      }

      AtomicInteger index = new AtomicInteger();
      Box<Runnable> op = new Box();
      op.value = () -> {
         String s = out[index.get()];
         if (!s.isEmpty()) {
            get().sendMessage(prefix + String.format(s, username), false);
            index.set(index.get() + 1);
            if (index.get() < out.length) {
               Scheduler.getService().schedule((Runnable)op.value, (long)(RNG.THREAD_LOCAL.current().nextInt(1250) + 4000), TimeUnit.MILLISECONDS);
            }
         }
      };
      if (initialDelay) {
         Scheduler.getService().schedule((Runnable)op.value, (long)(RNG.THREAD_LOCAL.current().nextInt(1250) + 4000), TimeUnit.MILLISECONDS);
      } else {
         ((Runnable)op.value).run();
      }

   }

   static {
      DEFAULT_TEXT_FIELD = Util.getField(GuiChat.class, Versionable.GUICHAT_DEFAULT_TEXT);
   }
}
