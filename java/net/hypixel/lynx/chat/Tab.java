package net.hypixel.lynx.chat;

import com.codelanx.commons.data.FileSerializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.tab.TabConfigMenu;
import net.hypixel.lynx.chat.tab.TabFilter;
import net.hypixel.lynx.chat.tab.TabStatus;
import net.hypixel.lynx.chat.tab.TabType;
import net.hypixel.lynx.chat.tab.Window;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.ui.Menu;
import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.util.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class Tab implements FileSerializable {
   private static final int HORIZ_BUFFER = 3;
   private static final int HEIGHT = 15;
   private static final int TITLE_BUFFER = 5;
   private static final int Y_OFFSET = 20;
   private static final int TOTAL_Y_OFFSET = 10;
   private static final int TITLE_VERTICAL_BUFFER = -11;
   private static final int FADE_DURATION_MS = 2500;
   private static final int FADE_WAIT_BUFFER = 1000;
   private static final float TAB_SCALE = 0.8F;
   private final Menu menu;
   private final List<String> chatRules;
   private final Set<TabFilter> filters;
   private final Set<Pattern> customFilterBlacklist;
   private final Set<Pattern> customFilterWhitelist;
   private final Set<String> tabPings;
   private boolean hovered;
   private Window window;
   private TabStatus status;
   private long fade;
   private String name;
   private TabType type;
   private boolean filtersAreWhitelist;

   public Tab() {
      this("General");
   }

   public Tab(String name) {
      this(name, TabType.GENERAL);
   }

   public Tab(TabType type) {
      this("Untitled", TabType.GENERAL);
   }

   public Tab(String name, TabType type) {
      this.chatRules = new LinkedList();
      this.filters = EnumSet.noneOf(TabFilter.class);
      this.customFilterBlacklist = new HashSet();
      this.customFilterWhitelist = new HashSet();
      this.tabPings = new LinkedHashSet();
      this.hovered = false;
      this.status = TabStatus.INACTIVE;
      this.fade = Long.MIN_VALUE;
      this.type = TabType.GENERAL;
      this.filtersAreWhitelist = true;
      this.name = name;
      this.setType(type);
      this.menu = new TabConfigMenu(this);
   }

   public Tab(Map<String, Object> config) {
      this.chatRules = new LinkedList();
      this.filters = EnumSet.noneOf(TabFilter.class);
      this.customFilterBlacklist = new HashSet();
      this.customFilterWhitelist = new HashSet();
      this.tabPings = new LinkedHashSet();
      this.hovered = false;
      this.status = TabStatus.INACTIVE;
      this.fade = Long.MIN_VALUE;
      this.type = TabType.GENERAL;
      this.filtersAreWhitelist = true;
      this.name = (String)config.get("name");
      this.setType(TabType.valueOf((String)config.get("type")));
      this.filtersAreWhitelist = (Boolean)config.get("filters-are-whitelist");
      /*
       this.tabPings.addAll((Collection)((List)config.get("custom-pings")).stream().map(String::toLowerCase).collect(Collectors.toList()));
      this.filters.addAll((Collection)((List)config.get("filters")).stream().map(TabFilter::valueOf).collect(Collectors.toList()));
      this.customFilterWhitelist.addAll((Collection)((List)config.get("custom-filter-whitelist")).stream().map(Pattern::compile).collect(Collectors.toList()));
      this.customFilterBlacklist.addAll((Collection)((List)config.get("custom-filter-blacklist")).stream().map(Pattern::compile).collect(Collectors.toList()));
       */
      this.chatRules.addAll((List)config.get("chat-rules"));
      this.window = this.type.getWindow();
      Number max = (Number)config.get("max-lines");
      if (max != null) {
         this.window.setMaxLines(max.intValue());
      }

      this.menu = new TabConfigMenu(this);
   }

   public void openMenu() {
      this.menu.show();
   }

   public Set<TabFilter> getFilters() {
      return Collections.unmodifiableSet(this.filters);
   }

   public TabType getType() {
      return this.type;
   }

   public void setType(TabType type) {
      this.type = type;
      this.window = type.getWindow();
   }

   public boolean filter(TabFilter type) {
      return this.filters.add(type);
   }

   public void unfilter(TabFilter type) {
      this.filters.remove(type);
   }

   public boolean isFiltersAsWhitelist() {
      return this.filtersAreWhitelist;
   }

   public void setFiltersAsWhitelist(boolean filtersAreWhitelist) {
      this.filtersAreWhitelist = filtersAreWhitelist;
   }

   public Window getWindow() {
      return this.window;
   }

   public void printIfApplicable(IChatComponent chat, int id) {
      TabFilter.actFilter(chat, (filter, username, text) -> {
         IChatComponent message = username == null ? chat : this.getLynxComponent(username, chat);
         String fm = message.getUnformattedText().toLowerCase();
         if (filter != TabFilter.GENERAL) {
            fm.substring(fm.indexOf(":") + 1);
         }

         label47: {
            boolean cont = this.filters.contains(filter);
            if (this.filtersAreWhitelist) {
               if (cont) {
                  break label47;
               }
            } else if (!cont) {
               break label47;
            }

            if ((this.type != TabType.PRIVATE_MESSAGES || filter != TabFilter.PRIVATE_MESSAGE) && !this.customFilterWhitelist.stream().anyMatch((f) -> {
               return f.matcher(chat.getUnformattedText()).matches();
            })) {
               return;
            }
         }

         if (!this.customFilterBlacklist.stream().anyMatch((f) -> {
            return f.matcher(chat.getUnformattedText()).matches();
         })) {
            /*
            if (text != null) {
               Stream var10000 = Stream.of(this.tabPings, ChatFacade.get().getGlobalPings()).flatMap(Collection::stream).map(String::toLowerCase);
               text.getClass();
               if (var10000.anyMatch(text::contains)) {
                  this.setTabStatus(TabStatus.PINGED);
                  Sounds.PINGED.playSound();
               }
            }
             */

            this.getWindow().printChatMessageWithOptionalDeletion(message, id);
            this.setTabStatus(TabStatus.NEW_MESSAGES);
         }
      });
   }

   private IChatComponent getLynxComponent(String username, IChatComponent old) {
      IChatComponent comp = new ChatComponentText("\u00A7c[\u00A7e➤\u00A7c]\u00A7r ");
      comp.getChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/target " + username));
      comp.getChatStyle().setChatHoverEvent(new HoverEvent(net.minecraft.event.HoverEvent.Action.SHOW_TEXT, new ChatComponentText("\u00A7cTarget " + username)));
      comp.appendSibling(old);
      return comp;
   }

   public void setTabStatus(TabStatus status) {
      if (status == TabStatus.NEW_MESSAGES) {
         if (this.status == TabStatus.INACTIVE) {
            if ((Boolean)ClientConfig.TAB_FLASH_ON_NEW_MSG.as(Boolean.TYPE) || this.getType() == TabType.PRIVATE_MESSAGES) {
               this.fade = System.currentTimeMillis() + 2500L - 1000L;
            }

            this.status = status;
         }

      } else {
         if (status == TabStatus.PINGED) {
            if (!this.getChatOpen()) {
               this.fade = System.currentTimeMillis() + 2500L;
            }

            if (this.status == TabStatus.PINGED || this.status == TabStatus.ACTIVE) {
               return;
            }
         }

         this.status = status;
      }
   }

   public boolean click(int index, int x, int y, boolean rightClick) {
      float scale = this.getWindow().getChatScale();
      x = (int)((float)x / scale);
      y = (int)((float)y / scale);
      if (this.contains(index, x, y)) {
         if (rightClick) {
            this.menu.show();
         } else {
            ChatFacade.get().setActiveTab(this);
            Sounds.BUTTON_CLICKED.playSound();
         }

         return true;
      } else {
         this.getWindow().click(x, y, rightClick);
         return false;
      }
   }

   public void hover(int index, int x, int y) {
      if (this.contains(index, x, y)) {
         this.hovered = true;
      } else {
         this.hovered = false;
      }

      this.getWindow().hover(x, y);
   }

   public void addChatRule(String format) {
      this.chatRules.add(format);
   }

   public String applyChatRules(String in) {
      StringBuilder sb = new StringBuilder(in);
      this.chatRules.forEach((r) -> {
         String val = String.format(r, sb.toString());
         sb.setLength(0);
         sb.append(val);
      });
      return sb.toString();
   }

   protected boolean contains(int index, int x, int y) {
      int left = this.getLeft(index);
      int top = this.getTop();
      int right = this.getRight(index);
      int bottom = this.getBottom();
      return left < x && x < right && y < top && y > bottom;
   }

   private int getBottomOfScreen() {
      return Minecraft.getMinecraft().displayHeight / 2;
   }

   private int getWidth() {
      return Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.name);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void drawTab(int index, int tick) {
      if (this.status == TabStatus.ACTIVE) {
         this.getWindow().drawChat(tick);
      }

      int left = this.getLeft(index);
      int top = this.getTop();
      int right = this.getRight(index);
      int bottom = this.getBottom();
      int alpha = -1;
      if (!this.getChatOpen() && !(Boolean)ClientConfig.ALWAYS_SHOW_CHAT_TABS.as(Boolean.TYPE)) {
         if (this.fade > 0L) {
            alpha = this.getAlphaFade();
         }
      } else {
         alpha = 255;
      }

      alpha = (int)((float)alpha * Window.getOpacity());
      if (alpha > 3) {
         GlStateManager.pushMatrix();
         int color = this.hovered ? 102 : 0;
         float scale = this.getWindow().getChatScale();
         GlStateManager.scale(scale, scale, 1.0F);
         Gui.drawRect(left, top, right, bottom, Regions.colorFromRGB(color, color, color, alpha / 2));
         this.drawTitle(left, bottom, alpha);
         GlStateManager.popMatrix();
      }

   }

   private int getAlphaFade() {
      long curr = System.currentTimeMillis();
      if (curr >= this.fade) {
         this.fade = Long.MIN_VALUE;
         return -1;
      } else {
         long diff = this.fade - curr;
         if (diff > 1500L) {
            return 255;
         } else {
            diff -= 1000L;
            float percentage = (float)diff / 1500.0F;
            return (int)(255.0F * percentage);
         }
      }
   }

   public void tabSwitched() {
      this.fade = System.currentTimeMillis() + 2500L;
      this.getWindow().windowSwitched();
   }

   private void drawTitle(int left, int bottom, int alpha) {
      GlStateManager.enableBlend();
      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
      fr.drawStringWithShadow(this.status.getPrefix() + this.name, (float)(left + 5), (float)(bottom + 15 + -11), Regions.colorFromRGB(255, 255, 255, alpha));
      GlStateManager.disableAlpha();
      GlStateManager.disableBlend();
   }

   public boolean getChatOpen() {
      return Minecraft.getMinecraft().currentScreen instanceof GuiChat;
   }

   private int getLeft(int index) {
      int prevRight;
      if (index > 0) {
         Tab o = Lynx.getChat().getTab(index - 1);
         prevRight = o.getRight(index - 1);
      } else {
         prevRight = Window.getLeft();
      }

      return 3 + prevRight;
   }

   private int getTop() {
      return this.getBottom() + 15;
   }

   private int getRight(int index) {
      return this.getLeft(index) + this.getWidth() + 10;
   }

   private int getBottom() {
      return Window.getBottom() - 20 + 10;
   }

   public Map<String, Object> serialize() {
      Map<String, Object> back = new HashMap();
      back.put("name", this.name);
      back.put("type", this.type.name());
      back.put("filters-are-whitelist", this.filtersAreWhitelist);
      back.put("custom-pings", new HashSet(this.tabPings));
      back.put("filters", new HashSet(this.filters));
      back.put("custom-filter-blacklist", this.customFilterBlacklist.stream().map(Pattern::pattern).collect(Collectors.toSet()));
      back.put("custom-filter-whitelist", this.customFilterWhitelist.stream().map(Pattern::pattern).collect(Collectors.toSet()));
      back.put("chat-rules", new HashSet(this.chatRules));
      back.put("max-lines", this.window.getMaxLines());
      return back;
   }
}
