package net.hypixel.lynx.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

public class MultiEntryConfigUI extends GuiListExtended implements ConfigUI {
   private static final List<MultiEntryConfigUI.ConfigEntry> entries = new ArrayList();
   private final GuiScreen parent;
   private final boolean map;
   private final ForgeConfig value;
   private int maxListLabelWidth = 0;
   private MultiEntryConfigUI.AddEntry add;

   public MultiEntryConfigUI(GuiScreen parent, ForgeConfig value) {
      super(Minecraft.getMinecraft(), parent.width, parent.height, 63, parent.height - 32, 20);
      this.parent = parent;
      this.map = value.get() instanceof Map;
      this.value = value;
      this.update();
      this.add = new MultiEntryConfigUI.AddEntry();
   }

   private void update() {
      MultiEntryConfigUI.entries.clear();
      Stream entries;
      if (this.map) {
         entries = this.value.as(Map.class, String.class, String.class).entrySet().stream()
                 .map(ent -> {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) ent;
                    return new MultiEntryConfigUI.ConfigEntry(entry.getKey(), entry.getValue());
                 });
      } else {
         entries = this.value.as(List.class, String.class).stream().map((x$0) -> {
            return new MultiEntryConfigUI.ConfigEntry((String) x$0);
         });
      }

      List var10001 = MultiEntryConfigUI.entries;
      entries.forEach(var10001::add);
   }

   public ConfigUI getParent() {
      return (ConfigUI)this.parent;
   }

   public boolean isMap() {
      return this.map;
   }

   public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
      entries.forEach((ent) -> {
         ent.mouseClicked(mouseX, mouseY, mouseEvent);
      });
      return super.mouseClicked(mouseX, mouseY, mouseEvent);
   }

   public void keyTyped(int keyCode, char keyChar) {
      entries.forEach((ent) -> {
         ent.keyTyped(keyCode, keyChar);
      });
   }

   public String getTitle() {
      return "Lynx Configuration";
   }

   public String getSubtitle() {
      return "Global > " + (this.map ? "Macros" : "Ping Notifications");
   }

   private Object collect(Stream<MultiEntryConfigUI.ConfigEntry> str) {
      if (this.map) {
         Set<String> test = new HashSet();
         List<String> caught = new LinkedList();
         return str.filter((ent) -> {
            return !ent.getKey().isEmpty();
         }).filter((ent) -> {
            if (!test.add(ent.getKey().toLowerCase())) {
               caught.add(ent.getKey().toLowerCase());
               return false;
            } else {
               return true;
            }
         }).filter((ent) -> {
            return !caught.contains(ent.getKey().toLowerCase());
         }).filter((ent) -> {
            return !ent.getValue().isEmpty();
         }).collect(Collectors.toMap(MultiEntryConfigUI.ConfigEntry::getKey, MultiEntryConfigUI.ConfigEntry::getValue));
      } else {
         return str.map(MultiEntryConfigUI.ConfigEntry::getValue).collect(Collectors.toList());
      }
   }

   public void onDoneButton() {
      Util.out("Entries to be saved:");
      entries.stream().map(MultiEntryConfigUI.ConfigEntry::getValue).forEach((x$0) -> {
         Util.out(x$0);
      });
      Object back = this.collect(entries.stream().filter((v) -> {
         return v != null && !v.getValue().isEmpty();
      }));
      Util.out("Val: %s", back);
      this.value.set(back);

      try {
         this.value.save();
      } catch (IOException var3) {
         Util.error(var3, "Error saving ping notifications");
      }

   }

   public void onResetButton() {
      this.value.set(this.map ? new LinkedHashMap() : new LinkedList());

      try {
         this.value.save();
      } catch (IOException var2) {
         Util.error(var2, "Error saving ping notifications");
      }

   }

   public IGuiListEntry getListEntry(int i) {
      return (IGuiListEntry)(this.getSize() - i == 1 ? this.add : (IGuiListEntry)entries.get(i));
   }

   protected int getSize() {
      return entries.size() + 1;
   }

   public class ConfigEntry implements IGuiListEntry {
      private GuiTextField keyField;
      private GuiTextField valueField;
      private GuiButton deleteButton;

      public ConfigEntry(String value) {
         this((String)null, value);
      }

      public ConfigEntry(String key, String value) {
         if (key != null) {
            this.keyField = new GuiTextField(10, Minecraft.getMinecraft().fontRendererObj, 0, 0, 100, 16);
            this.keyField.setText(key);
         }

         this.valueField = new GuiTextField(10, Minecraft.getMinecraft().fontRendererObj, 0, 0, 150, 16);
         this.valueField.setText(value);
         this.deleteButton = new GuiButton(69, 0, 0, 50, 16, "Delete");
      }

      public String getKey() {
         return this.keyField.getText();
      }

      public String getValue() {
         return this.valueField.getText();
      }

      public void setSelected(int i, int i1, int i2) {
      }

      public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
         if (this.keyField != null) {
            this.keyField.xPosition = x;
            this.keyField.yPosition = y;
            this.keyField.drawTextBox();
         }

         this.valueField.xPosition = this.keyField == null ? x : this.keyField.width + 10 + x;
         this.valueField.yPosition = y;
         this.valueField.drawTextBox();
         this.deleteButton.xPosition = this.valueField.xPosition + this.valueField.width + 10;
         this.deleteButton.yPosition = y;
         this.deleteButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
      }

      public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relX, int relY) {
         if (this.deleteButton.mousePressed(Minecraft.getMinecraft(), x, y)) {
            Util.out("Deleting: %d", slotIndex);
            MultiEntryConfigUI var10000 = MultiEntryConfigUI.this;
            MultiEntryConfigUI.entries.removeIf((ent) -> {
               return ent.deleteButton == this.deleteButton;
            });
            return true;
         } else {
            return false;
         }
      }

      public void keyTyped(int eventKey, char eventChar) {
         if (this.keyField != null) {
            this.keyField.textboxKeyTyped(eventChar, eventKey);
         }

         this.valueField.textboxKeyTyped(eventChar, eventKey);
      }

      public void mouseClicked(int x, int y, int mouseEvent) {
         if (this.keyField != null) {
            this.keyField.mouseClicked(x, y, mouseEvent);
         }

         this.valueField.mouseClicked(x, y, mouseEvent);
      }

      public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
         this.deleteButton.mouseReleased(x, y);
      }
   }

   public class AddEntry implements IGuiListEntry {
      private final GuiButton button = new GuiButton(69, 0, 0, 100, 16, "Add");

      public void setSelected(int i, int i1, int i2) {
      }

      public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
         this.button.xPosition = x + 100;
         this.button.yPosition = y;
         this.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
      }

      public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relX, int relY) {
         if (this.button.mousePressed(Minecraft.getMinecraft(), x, y)) {
            MultiEntryConfigUI var10000 = MultiEntryConfigUI.this;
            MultiEntryConfigUI.entries.add(MultiEntryConfigUI.this.new ConfigEntry(MultiEntryConfigUI.this.map ? "" : null, ""));
         }

         return false;
      }

      public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
         this.button.mouseReleased(x, y);
      }
   }
}
