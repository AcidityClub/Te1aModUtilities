package net.hypixel.lynx.config;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import net.hypixel.lynx.ui.Regions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;

public class ConfigMenu extends GuiConfig implements ConfigUI {
   private static ConfigMenu root;
   private KeyBindingUI keybindings;
   private ConfigMenu global;
   private MultiEntryConfigUI macros;
   private MultiEntryConfigUI pings;
   private ConfigUI active;
   private List<GuiButton> buttons;

   public ConfigMenu(GuiScreen parent) {
      this(parent, (new ConfigElement(ConfigFactory.PRIMARY_CATEGORY)).getChildElements());
      root = this;
      this.setActive(this);
   }

   public ConfigMenu(GuiScreen parent, List<IConfigElement> elements) {
      super(parent, elements, "Lynx", false, false, "Lynx Configuration");
      this.active = this;
      this.buttons = new LinkedList();
   }

   public ConfigUI getParent() {
      return (ConfigUI)(this == root ? this : (ConfigUI)this.parentScreen);
   }

   public void setActive(ConfigUI screen) {
      this.active = screen;
      this.title = screen.getTitle();
      this.titleLine2 = screen.getSubtitle();
   }

   public void initGui() {
      super.initGui();
      if (this == root) {
         this.global = new ConfigMenu(this, (new ConfigElement(ConfigFactory.GLOBAL)).getChildElements());
         this.keybindings = new KeyBindingUI(this);
      } else {
         this.pings = new MultiEntryConfigUI(this, ClientConfig.PING_NOTIFICATIONS);
      }

      this.buttons.add(new GuiButton(200, this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("gui.done", new Object[0])));
      this.buttons.add(new GuiButton(201, this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("controls.resetAll", new Object[0])));
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      switch(this.getState()) {
      case KEYBINDINGS:
         this.keybindings.drawScreen(mouseX, mouseY, partialTicks);
         break;
      case MACROS:
         this.macros.drawScreen(mouseX, mouseY, partialTicks);
         break;
      case PINGS:
         this.pings.drawScreen(mouseX, mouseY, partialTicks);
      }

      if (this.getState() != ConfigMenu.State.CONFIG) {
         this.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, this.active.getTitle(), this.width / 2, 8, Regions.colorFromRGB(255, 255, 255, 0));
         String title2 = this.active.getSubtitle();
         if (title2 != null) {
            int strWidth = Regions.getStringWidth(title2);
            int elipsisWidth = Regions.getStringWidth("...");
            if (strWidth > this.width - 6 && strWidth > elipsisWidth) {
               title2 = this.mc.fontRendererObj.trimStringToWidth(title2, this.width - 6 - elipsisWidth).trim() + "...";
            }

            this.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, title2, this.width / 2, 18, Regions.colorFromRGB(255, 255, 255, 0));
         }

         this.buttons.forEach((b) -> {
            b.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
         });
      }

   }

   protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
      switch(this.getState()) {
      case KEYBINDINGS:
         this.keybindings.mouseClicked(x, y, mouseEvent);
         break;
      case MACROS:
         this.macros.mouseClicked(x, y, mouseEvent);
         break;
      case PINGS:
         this.pings.mouseClicked(x, y, mouseEvent);
         break;
      default:
         if (this.entryList.isMouseYWithinSlotBounds(y)) {
            int i = this.entryList.getSlotIndexFromScreenCoords(x, y);
            if (i >= 0) {
               int j = this.entryList.left + this.width / 2 - this.entryList.getListWidth() / 2 + 2;
               int k = this.entryList.top + 4 - this.entryList.getAmountScrolled() + i * this.entryList.slotHeight + this.entryList.headerPadding;
               int l = x - j;
               int i1 = y - k;
               IConfigEntry ent = this.entryList.getListEntry(i);
               if (ent.mousePressed(i, x, y, mouseEvent, l, i1)) {
                  if (ent.getConfigElement().isProperty()) {
                     ent.getConfigElement().set(ent.getCurrentValue());
                     return;
                  }

                  this.entryList.setEnabled(false);
                  String var10 = ent.getName().toLowerCase();
                  byte var11 = -1;
                  switch(var10.hashCode()) {
                  case -1325147462:
                     if (var10.equals("ping notifications")) {
                        var11 = 1;
                     }
                     break;
                  case -1243020381:
                     if (var10.equals("global")) {
                        var11 = 3;
                     }
                     break;
                  case -1081745881:
                     if (var10.equals("macros")) {
                        var11 = 2;
                     }
                     break;
                  case 3288564:
                     if (var10.equals("keys")) {
                        var11 = 0;
                     }
                  }

                  switch(var11) {
                  case 0:
                     this.setActive(this.keybindings);
                     Minecraft.getMinecraft().displayGuiScreen(this);
                     break;
                  case 1:
                     this.setActive(this.pings);
                     Minecraft.getMinecraft().displayGuiScreen(this);
                     break;
                  case 2:
                     this.setActive(this.macros);
                     Minecraft.getMinecraft().displayGuiScreen(this);
                     break;
                  case 3:
                     this.setActive(this.global);
                     Minecraft.getMinecraft().displayGuiScreen(this.global);
                  }
               }
            }
         } else {
            super.mouseClicked(x, y, mouseEvent);
         }
      }

      if (this.getState() != ConfigMenu.State.CONFIG) {
         this.buttons.stream().filter((b) -> {
            if (b.mousePressed(Minecraft.getMinecraft(), x, y)) {
               b.playPressSound(Minecraft.getMinecraft().getSoundHandler());
               this.actionPerformed(b);
               return true;
            } else {
               return false;
            }
         }).findFirst();
      }

   }

   protected void keyTyped(char eventChar, int eventKey) {
      switch(this.getState()) {
      case KEYBINDINGS:
         this.keybindings.keyTyped(eventKey);
         break;
      case MACROS:
         this.macros.keyTyped(eventKey, eventChar);
         break;
      case PINGS:
         this.pings.keyTyped(eventKey, eventChar);
         break;
      default:
         super.keyTyped(eventChar, eventKey);
      }

   }

   protected void mouseReleased(int x, int y, int mouseEvent) {
      switch(this.getState()) {
      case KEYBINDINGS:
         if (!this.keybindings.mouseReleased(x, y, mouseEvent)) {
            super.mouseReleased(x, y, mouseEvent);
         }
         break;
      case MACROS:
         if (!this.macros.mouseReleased(x, y, mouseEvent)) {
            super.mouseReleased(x, y, mouseEvent);
         }
         break;
      case PINGS:
         if (!this.pings.mouseReleased(x, y, mouseEvent)) {
            super.mouseReleased(x, y, mouseEvent);
         }
         break;
      default:
         super.mouseReleased(x, y, mouseEvent);
      }

      if (this.getState() != ConfigMenu.State.CONFIG) {
         this.buttons.forEach((b) -> {
            b.mouseReleased(x, y);
         });
      }

   }

   protected void actionPerformed(GuiButton button) {
      switch(this.getState()) {
      case KEYBINDINGS:
      default:
         switch(button.id) {
         case 200:
            this.active.onDoneButton();
            this.setActive(this.active.getParent());
            break;
         case 201:
            this.active.onResetButton();
            break;
         default:
            super.actionPerformed(button);
         }

      }
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      switch(this.getState()) {
      case KEYBINDINGS:
         this.keybindings.handleMouseInput();
         break;
      case MACROS:
         this.macros.handleMouseInput();
         break;
      case PINGS:
         this.pings.handleMouseInput();
      }

   }

   public ConfigMenu.State getState() {
      if (this.active.getClass() == KeyBindingUI.class) {
         return ConfigMenu.State.KEYBINDINGS;
      } else if (this.active.getClass() == ConfigMenu.class) {
         return ConfigMenu.State.CONFIG;
      } else if (this.active.getClass() == MultiEntryConfigUI.class) {
         return ((MultiEntryConfigUI)this.active).isMap() ? ConfigMenu.State.MACROS : ConfigMenu.State.PINGS;
      } else {
         return ConfigMenu.State.HOME;
      }
   }

   public String getTitle() {
      return "Lynx Configuration";
   }

   public String getSubtitle() {
      return null;
   }

   public void onDoneButton() {
      Minecraft.getMinecraft().displayGuiScreen(this.parentScreen);
   }

   public void onResetButton() {
   }

   private static enum State {
      HOME,
      CONFIG,
      MACROS,
      PINGS,
      KEYBINDINGS;
   }
}
