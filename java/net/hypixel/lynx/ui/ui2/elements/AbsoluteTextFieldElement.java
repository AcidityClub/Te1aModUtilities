package net.hypixel.lynx.ui.ui2.elements;

import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;
import net.minecraft.client.gui.Gui;

public class AbsoluteTextFieldElement implements AbsoluteContentElement {
   private static final int VERTICAL_MARGIN = 1;
   private static final int VERTICAL_PADDING = 1;
   private static final int HORIZONTAL_MARGIN = 2;
   private static final int HORIZONTAL_PADDING = 2;
   private static final int TEXT_HEIGHT = 11;
   private static final int VERTICAL_ADD = 4;
   private static final int HORIZONTAL_ADD = 8;
   private static final int DEFAULT_RGB = Regions.colorFromRGB(255, 255, 255, 153);
   private final int length;
   private int background;
   private String prefix;
   private String value;

   public AbsoluteTextFieldElement() {
      this(50);
   }

   public AbsoluteTextFieldElement(int length) {
      this.background = DEFAULT_RGB;
      this.prefix = "";
      this.value = "";
      this.length = length;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public int getBackgroundRGB() {
      return this.background;
   }

   public void setBackgroundRGB(int rgb) {
      this.background = rgb;
   }

   public String getValue() {
      return this.value;
   }

   public AbsoluteTextFieldElement setValue(String value) {
      this.value = value;
      return this;
   }

   public int getWidth() {
      return this.length + 8;
   }

   public int getHeight() {
      return 15;
   }

   public void onDraw(int x, int y) {
      Gui.drawRect(x + 2, y + 1, x + this.getWidth() - 2, y + this.getHeight() - 1, this.background);
      UIS.drawText(Regions.trimToWidth(this.prefix + this.value, this.length), x + 2 + 2, y + 1 + 1);
   }

   public void onClick(int x, int y) {
   }
}
