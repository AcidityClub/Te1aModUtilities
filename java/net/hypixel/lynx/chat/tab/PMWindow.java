package net.hypixel.lynx.chat.tab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.util.Symbols;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Mouse;

public class PMWindow extends Window {
   private static final float NAME_WINDOW_WIDTH = 0.38F;
   private static final int CLOSE_WINDOW_WIDTH = 10;
   private static final int CLOSE_HORIZ_BUFFER = 2;
   private static final int NAME_VERTICAL_BUFFER = 6;
   private static final int CHAT_HORIZ_BUFFER = 3;
   private static final int NAME_HEIGHT = 21;
   private static final int NAME_HORIZ_BUFFER = 3;
   private static final int Y_TRANSLATE = 20;
   private static final int X_TRANSLATE = 2;
   private final Set<String> newMessageState = new HashSet();
   private final Map<String, List<ChatLine>> splitChatLines = new HashMap();
   private final Map<String, String> displayNames = new LinkedHashMap();
   private String activeName = null;
   private int nameScroll = 0;

   public PMWindow() {
      this.adjustLeftAlignment();
      this.addMessageMutator((chat) -> {
         String text = chat.getFormattedText();
         String val = this.getName(text, false);
         String dval = this.getDisplayName(text);
         if (!this.displayNames.containsKey(val)) {
            Lynx.getMode().onPM(val);
         }

         this.displayNames.put(val, dval);
         this.newMessageState.add(val);
         if (this.activeName == null) {
            this.setNameActive(val);
         }

         return chat;
      });
      this.addMessageMutator((chat) -> {
         ChatComponentText txt = new ChatComponentText("");
         boolean name = chat.getUnformattedText().startsWith("To");
         List<IChatComponent> sibs = chat.getSiblings();
         if (name) {
            sibs = sibs.subList(this.colorsBeforeName(sibs, this.getName(chat.getUnformattedText(), false)), sibs.size());
         }

         Object comp;
         for(Iterator var5 = sibs.iterator(); var5.hasNext(); txt.appendSibling((IChatComponent)comp)) {
            comp = (IChatComponent)var5.next();
            if (name) {
               name = false;
               String val = Lynx.getMetaInfo("display-name");
               comp = new ChatComponentText(Symbols.stripChatColor(val) + ": ");
               ((IChatComponent)comp).getChatStyle().setColor(this.getColor(val));
            }
         }

         return txt;
      });
   }

   private EnumChatFormatting getColor(String username) {
      if (username.startsWith("\u00A7") && username.length() >= 2) {
         String pre = username.substring(0, 2);
         return (EnumChatFormatting)Arrays.stream(EnumChatFormatting.values()).filter((c) -> {
            return c.toString().equalsIgnoreCase(pre);
         }).findFirst().orElse(null);
      } else {
         return null;
      }
   }

   public String getCurrentChatter() {
      return this.activeName;
   }

   public void openPMWindow(String username) {
      this.displayNames.put(username, username);
      this.activeName = username;
      Minecraft.getMinecraft().displayGuiScreen(new InputWindow(""));
   }

   private void adjustLeftAlignment() {
      float width = (float)this.getChatWidth();
      float textLeftAlignment = width * 0.38F;
      this.textLeftAlignment = (int)textLeftAlignment;
   }

   public void drawChat(int tick) {
      if (Minecraft.getMinecraft().gameSettings.chatVisibility != EnumChatVisibility.HIDDEN && this.getChatOpen() && this.displayNames.size() > 0) {
         float scale = this.getChatScale();
         this.adjustLeftAlignment();
         super.renderChat(tick, true);
         GlStateManager.pushMatrix();
         GlStateManager.translate(2.0F, 20.0F, 0.0F);
         GlStateManager.scale(scale, scale, 1.0F);
         int height = this.getChatHeight();
         int i = height / 21;
         int remainder = height % 21;
         int offset = height - i * 21;
         Function<String, String> toDisplay = (sx) -> {
            return (Boolean)ClientConfig.PMS_USE_DISPLAY_NAMES.as(Boolean.TYPE) ? (String)this.displayNames.get(sx) : sx;
         };
         List<String> names = new ArrayList(this.displayNames.keySet());
         int lowerBound = Math.max(0, -this.nameScroll);
         int upperBound = Math.min(names.size(), -this.nameScroll + height / 21);
         names = names.subList(lowerBound, upperBound);
         ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
         int x = this.widthScale(Mouse.getX(), res);
         int y = -this.heightScale(Mouse.getY(), res);
         i *= -1;

         for(Iterator var14 = names.iterator(); var14.hasNext(); ++i) {
            String s = (String)var14.next();
            boolean closed = false;
            boolean name = false;
            if (x < this.textLeftAlignment - (int)(3.0F * scale) && y > 21 * i - offset && y < 21 * i + 21 - offset) {
               if ((float)x < 10.0F * scale) {
                  closed = true;
               } else {
                  name = true;
               }
            }

            this.drawName((String)toDisplay.apply(s), 21 * i + 21 - offset, name, closed);
         }

         while(i < 0) {
            this.drawName((String)null, 21 * i + 21 - offset, false, false);
            ++i;
         }

         Gui.drawRect(0, Window.getTop(), this.textLeftAlignment - (int)(3.0F * scale), Window.getTop() - remainder, Regions.colorFromRGB(0, 0, 0, 127.0F * Window.getOpacity()));
         if (remainder > 6) {
         }

         GlStateManager.popMatrix();
      }

   }

   private void drawName(String name, int top, boolean hovered, boolean hoveringClose) {
      Minecraft mc = Minecraft.getMinecraft();
      int nameColor = hovered ? Regions.colorFromRGB(102, 102, 102, 127.0F * Window.getOpacity()) : Regions.colorFromRGB(0, 0, 0, 127.0F * Window.getOpacity());
      int closeColor = hoveringClose ? 102 : 0;
      String cname = name == null ? null : this.getName(name, true);
      if (name != null && this.newMessageState.contains(cname) && !hovered) {
         nameColor = Regions.colorFromRGB(102, 102, 0, 127.0F * Window.getOpacity());
      }

      if (name != null && cname.equals(this.activeName) && !hovered) {
         nameColor = Regions.colorFromRGB(32, 32, 32, 127.0F * Window.getOpacity());
      }

      Gui.drawRect(0, top, 10, top - 21, Regions.colorFromRGB(closeColor, closeColor, closeColor, 127.0F * Window.getOpacity()));
      Gui.drawRect(10, top, this.textLeftAlignment - 3, top - 21, nameColor);
      if (name != null) {
         GlStateManager.enableBlend();
         name = mc.fontRendererObj.trimStringToWidth(name, this.textLeftAlignment - 6 - 3 - 10);
         int text = Regions.colorFromRGB(255, 255, 255, 255.0F * Window.getOpacity());
         mc.fontRendererObj.drawStringWithShadow("\u00A7d" + name, 13.0F, (float)(top - 21 + 6), text);
         mc.fontRendererObj.drawStringWithShadow("⨉", 2.0F, (float)(top - 21 + 6), text);
         GlStateManager.disableAlpha();
         GlStateManager.disableBlend();
      }

   }

   public void clearChatMessages() {
      super.clearChatMessages();
      this.displayNames.clear();
      this.splitChatLines.clear();
   }

   private int getNameCount() {
      return this.getChatHeight() / 21;
   }

   public void scroll(int linesMoved) {
      ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
      int x = this.widthScale(Mouse.getX(), res);
      int y = -this.heightScale(Mouse.getY(), res);
      if (x < this.textLeftAlignment - 3 && y > Window.getBottom() && y < Window.getTop()) {
         if (Math.abs(linesMoved) > 1) {
            linesMoved /= Math.abs(linesMoved);
            linesMoved *= 2;
         }

         this.nameScroll += linesMoved;
         int i = this.displayNames.size();
         if (-this.nameScroll > i - this.getNameCount()) {
            this.nameScroll = -(i - this.getNameCount());
         }

         if (this.nameScroll > 0) {
            this.nameScroll = 0;
         }

         Util.out("new scroll: " + this.nameScroll);
      } else {
         super.scroll(linesMoved);
      }

   }

   public void click(int x, int y, boolean rightClick) {
      x -= 2;
      y -= 20;
      if (x < this.textLeftAlignment && y > Window.getBottom() && y < Window.getTop()) {
         int index = (this.getChatHeight() - Math.abs(y)) / 21;
         Util.out("initial index: %d, scroll: %d, new index: %d", index, this.nameScroll, index - this.nameScroll);
         index -= this.nameScroll;
         List<String> names = new ArrayList(this.displayNames.keySet());
         Util.out("PM window click: %d, %d", x, y);
         if (index >= 0 && index < names.size()) {
            String name = (String)names.get(index);
            if (x < 7) {
               this.close(name);
               Util.out("Removing window for: %s", this.activeName);
            } else {
               this.setNameActive(name);
               Util.out("Setting window to: %s", this.activeName);
            }
         }
      } else {
         super.click(x, y, rightClick);
      }

   }

   public int getCustomChatWidth() {
      return super.getCustomChatWidth() - this.textLeftAlignment;
   }

   private void close(String name) {
      this.displayNames.remove(name);
      if (name.equals(this.activeName)) {
         if (this.displayNames.isEmpty()) {
            this.activeName = null;
         } else {
            this.activeName = (String)this.displayNames.keySet().iterator().next();
            this.nameScroll = 0;
         }
      }

   }

   private void setNameActive(String name) {
      this.newMessageState.remove(name);
      this.activeName = name;
      InputWindow iw = ChatFacade.get().getInputWindow();
      if (iw != null) {
         iw.setMaxChatLength(this.getMaxStringLength());
      }

   }

   public int getMaxStringLength() {
      return 100 - (this.activeName == null ? 0 : this.activeName.length() + 6);
   }

   protected List<ChatLine> getSplitChatLines(String input) {
      String name = input == null ? this.activeName : this.getName(input, false);
      return this.getSplitChatLines(input, name);
   }

   public List<ChatLine> getSplitChatLines(String message, String username) {
      return (List)this.splitChatLines.computeIfAbsent(username, (s) -> {
         return new LinkedList();
      });
   }

   private String getName(String message, boolean suppliedIsDisplayName) {
      String disp = suppliedIsDisplayName ? message : this.getDisplayName(message);
      return Symbols.stripChatColor(disp.substring(disp.lastIndexOf(32) + 1));
   }

   private String getDisplayName(String message) {
      try {
         String message2;
         if (!message.startsWith("\u00A7dTo") && !message.startsWith("\u00A7dFrom")) {
            message2 = message;
         } else {
            int index = message.indexOf(" ");
            message2 = message.substring(index + 1);
         }

         int index2 = message2.indexOf(":");
         String message3 = message2.substring(0, index2);
         return message3;
      } catch (Exception var10) {
         return "";
      } finally {
         ;
      }
   }

   private int colorsBeforeName(List<IChatComponent> comps, String name) {
      int colors = 0;

      for(Iterator var4 = comps.iterator(); var4.hasNext(); ++colors) {
         IChatComponent comp = (IChatComponent)var4.next();
         if (comp.getUnformattedText().contains(name)) {
            return colors;
         }
      }

      return 0;
   }
}
