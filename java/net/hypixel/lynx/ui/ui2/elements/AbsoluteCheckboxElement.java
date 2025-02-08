package net.hypixel.lynx.ui.ui2.elements;

import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;
import net.minecraft.client.gui.Gui;

public class AbsoluteCheckboxElement implements AbsoluteContentElement {
   private static final int PADDING = 4;
   private static final int WIDTH = Regions.getStringWidth("✓");
   private final Runnable onClick;
   private boolean checked;

   public AbsoluteCheckboxElement(boolean checked, Runnable onClick) {
      this.checked = checked;
      this.onClick = onClick;
   }

   public int getWidth() {
      return WIDTH + 4;
   }

   public int getHeight() {
      return 15;
   }

   public boolean isChecked() {
      return this.checked;
   }

   public int getBackroundRGB() {
      return this.isChecked() ? Regions.colorFromRGB(0, 0, 0, 153) : Regions.colorFromRGB(0, 0, 0, 51);
   }

   public void onDraw(int x, int y) {
      int xW = x + this.getWidth();
      int yW = y + this.getHeight();
      Gui.drawRect(x, y, xW, y, UIS.FULL_COLOR);
      Gui.drawRect(x, y, x, yW, UIS.FULL_COLOR);
      Gui.drawRect(x, yW, xW, yW, UIS.FULL_COLOR);
      Gui.drawRect(xW, y, xW, yW, UIS.FULL_COLOR);
      if (this.isChecked()) {
         UIS.drawText("✓", x + 2, y + 2);
      }

   }

   public void onClick(int x, int y) {
      this.onClick.run();
   }
}
