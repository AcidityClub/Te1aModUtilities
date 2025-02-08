package net.hypixel.lynx.config;

import com.codelanx.commons.util.Reflections;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.key.KeyBinding;
import net.hypixel.lynx.ui.key.KeyBindings;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public class KeyBindingUI extends GuiListExtended implements ConfigUI {
   private static KeyBindings selected = null;
   private static String subtitle = "";
   private static Set<Integer> keys = new LinkedHashSet();
   private final IGuiListEntry[] listEntries;
   private final GuiScreen parent;
   private int maxListLabelWidth = 0;

   public KeyBindingUI(GuiScreen in) {
      super(Minecraft.getMinecraft(), in.width, in.height, 63, in.height - 32, 20);
      this.parent = in;
      KeyBindings[] binds = KeyBindings.values();
      this.listEntries = new IGuiListEntry[binds.length];
      int i = 0;
      KeyBindings[] var4 = binds;
      int var5 = binds.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         KeyBindings keybinding = var4[var6];
         int width = Regions.getStringWidth(Reflections.properEnumName(keybinding));
         if (width > this.maxListLabelWidth) {
            this.maxListLabelWidth = width;
         }

         this.listEntries[i++] = new KeyBindingUI.KeyEntry(keybinding);
      }

   }

   public ConfigUI getParent() {
      return (ConfigUI)this.parent;
   }

   private void setSelected(KeyBindings bind) {
      selected = bind;
      keys.clear();
      if (selected == null) {
         subtitle = null;
      } else {
         subtitle = "Enter your key combination. ENTER to finish, ESC to cancel";
      }

   }

   protected int getSize() {
      return this.listEntries.length;
   }

   public void keyTyped(int keyCode) {
      if (selected != null) {
         switch(keyCode) {
         case 28:
            if (keys.size() > 0) {
               selected.set(keys.size() == 1 ? keys.iterator().next() : new ArrayList(keys));

               try {
                  selected.save();
               } catch (IOException var3) {
                  Util.error(var3, "Error saving keybinding for: %s", Reflections.properEnumName(selected));
               }
            }
         case 1:
            this.setSelected((KeyBindings)null);
            break;
         default:
            keys.add(keyCode);
         }
      }

   }

   public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) {
      super.drawScreen(mouseXIn, mouseYIn, p_148128_3_);
   }

   public IGuiListEntry getListEntry(int index) {
      return this.listEntries[index];
   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 15;
   }

   public int getListWidth() {
      return super.getListWidth() + 32;
   }

   public String getTitle() {
      return "Keybindings";
   }

   public String getSubtitle() {
      return subtitle;
   }

   public void onDoneButton() {
      try {
         KeyBindings.ACTION_AURABOT.save();
      } catch (IOException var2) {
         Util.error(var2, "Error saving keybindings");
      }

   }

   public void onResetButton() {
      Arrays.stream(KeyBindings.values()).forEach((b) -> {
         b.set(b.getDefault());
      });

      try {
         KeyBindings.ACTION_AURABOT.save();
      } catch (IOException var2) {
         Util.error(var2, "Error saving keybindings");
      }

   }

   public class KeyEntry implements IGuiListEntry {
      private final KeyBindings keybinding;
      private final String keyDesc;
      private final GuiButton btnChangeKeyBinding;
      private final GuiButton btnReset;

      private KeyEntry(KeyBindings key) {
         this.keybinding = key;
         this.keyDesc = Reflections.properEnumName(key);
         this.btnChangeKeyBinding = new GuiButton(0, 0, 0, 75, 20, this.keyDesc);
         this.btnReset = new GuiButton(0, 0, 0, 50, 20, I18n.format("controls.reset", new Object[0]));
      }

      public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
         boolean active = KeyBindingUI.selected == this.keybinding;
         UIS.drawText(this.keyDesc, x + 90 - KeyBindingUI.this.maxListLabelWidth, y + slotHeight / 2 - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2, Regions.colorFromRGB(255, 255, 255, 0));
         this.btnReset.xPosition = x + 190;
         this.btnReset.yPosition = y;
         Object now = this.keybinding.getKeys();
         Object def = this.keybinding.getDefault();
         int[] defx;
         if (def instanceof List) {
            List<Integer> defList = (List)def;
            defx = new int[defList.size()];

            for(int i = 0; i < defList.size(); ++i) {
               ((int[])((int[])defx))[i] = (Integer)defList.get(i);
            }
         } else {
            defx = new int[]{(Integer)def};
         }

         Arrays.sort((int[])((int[])now));
         Arrays.sort((int[])((int[])defx));
         this.btnReset.enabled = !Arrays.equals((int[])((int[])now), (int[])((int[])defx));
         this.btnReset.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
         this.btnChangeKeyBinding.xPosition = x + 105;
         this.btnChangeKeyBinding.yPosition = y;
         int ix;
         if (!KeyBindingUI.keys.isEmpty()) {
            Integer[] val = (Integer[])KeyBindingUI.keys.toArray(new Integer[KeyBindingUI.keys.size()]);
            int[] in = new int[val.length];

            for(ix = 0; ix < val.length; ++ix) {
               in[ix] = val[ix];
            }

            this.btnChangeKeyBinding.displayString = active ? KeyBinding.of(in).getName() : this.keybinding.getName();
         } else {
            this.btnChangeKeyBinding.displayString = this.keybinding.getName();
         }

         boolean red = false;
         if (this.keybinding.getKeys().length > 0 && this.keybinding.getKeys()[0] != 0) {
            KeyBindings[] var21 = KeyBindings.values();
            ix = var21.length;

            for(int var15 = 0; var15 < ix; ++var15) {
               KeyBindings keybinding = var21[var15];
               if (keybinding != this.keybinding && keybinding.getKeys().equals(this.keybinding.getKeys())) {
                  red = true;
                  break;
               }
            }
         }

         if (active) {
            this.btnChangeKeyBinding.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + this.btnChangeKeyBinding.displayString + EnumChatFormatting.WHITE + " <";
         } else if (red) {
            this.btnChangeKeyBinding.displayString = EnumChatFormatting.RED + this.btnChangeKeyBinding.displayString;
         }

         this.btnChangeKeyBinding.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
      }

      public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relX, int relY) {
         if (this.btnChangeKeyBinding.mousePressed(Minecraft.getMinecraft(), x, y)) {
            KeyBindingUI.this.setSelected(this.keybinding);
            return true;
         } else if (this.btnReset.mousePressed(Minecraft.getMinecraft(), x, y)) {
            this.keybinding.set(this.keybinding.getDefault());

            try {
               this.keybinding.save();
            } catch (IOException var8) {
               Util.error(var8, "Error saving keybinding for: %s", Reflections.properEnumName(this.keybinding));
            }

            return true;
         } else {
            return false;
         }
      }

      public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
         this.btnChangeKeyBinding.mouseReleased(x, y);
         this.btnReset.mouseReleased(x, y);
      }

      public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {
      }

      // $FF: synthetic method
      KeyEntry(KeyBindings x1, Object x2) {
         this(x1);
      }
   }
}
