package net.hypixel.lynx.ui.ui2.elements;

import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;

public class AbsoluteButtonElement implements AbsoluteContentElement {
   private static final int PADDING = 4;
   private final int trimToWidth;
   private String value;
   private int actualTrim;

   public AbsoluteButtonElement(String value) {
      this(value, -1);
   }

   public AbsoluteButtonElement(String value, int trimToWidth) {
      this.trimToWidth = trimToWidth;
      this.setValue(value);
   }

   public String getValue() {
      return this.value;
   }

   public final void setValue(String value) {
      this.value = value;
      int len = Regions.getStringWidth(this.value);
      this.actualTrim = this.trimToWidth > 0 ? Math.min(this.trimToWidth, len) : len;
   }

   public int getWidth() {
      return this.actualTrim + 4;
   }

   public int getHeight() {
      return 15;
   }

   public void onDraw(int x, int y) {
      UIS.drawText(this.value, x + 2, y + 2);
   }
}
