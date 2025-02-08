package net.hypixel.lynx.ui.ui2.elements;

import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;

public class AbsoluteLabelElement implements AbsoluteContentElement {
   private static final int PADDING = 4;
   private final String label;
   private String suffix = ": ";

   public AbsoluteLabelElement(String label) {
      this.label = label;
   }

   public String getSuffix() {
      return this.suffix;
   }

   public void setSuffix(String suffix) {
      this.suffix = suffix;
   }

   public int getWidth() {
      return Regions.getStringWidth(this.label + this.suffix) + 4;
   }

   public int getHeight() {
      return 11;
   }

   public void onDraw(int x, int y) {
      UIS.drawText(this.label + this.getSuffix(), x + 2, y);
   }

   public void onClick(int x, int y) {
   }
}
