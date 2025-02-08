package net.hypixel.lynx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class StatusUI {
   private static final int ITEM_HEIGHT = 11;
   private static final int COLOR = Regions.colorFromRGB(0, 0, 0, 127);
   private static final int TEXT_VERT_BUFFER = 1;
   private static final int TEXT_HORIZ_BUFFER = 2;
   private static final int BOX_RIGHT_BUFFER = 10;
   private static final int BOX_BOTTOM_BUFFER = 10;
   private static final Map<String, String> KEYS = new LinkedHashMap<String, String>() {
      {
         this.put(null, "Mode");
         this.put("current-server", "Server");
      }
   };

   public void draw() {
      if (Lynx.hasMeta()) {
         ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
         int farRight = res.getScaledWidth();
         int farBottom = res.getScaledHeight();
         farRight -= 10;
         farBottom -= 10;
         AtomicInteger width = new AtomicInteger();
         List<String> out = (List)KEYS.entrySet().stream().map((ent) -> {
            String val = ent.getKey() == null ? Lynx.getMode().getFormattedName() : Lynx.getMetaInfo((String)ent.getKey());
            val = (String)ent.getValue() + ": " + val;
            width.set(Math.max(width.get(), Regions.getStringWidth(val)));
            return val;
         }).collect(Collectors.toList());
         int left = farRight - width.get() - 4;
         int top = farBottom - out.size() * 11;
         Gui.drawRect(left, top, farRight, farBottom, COLOR);
         AtomicInteger index = new AtomicInteger();
         out.forEach((s) -> {
            UIS.drawText(s, left + 2, top + 11 * index.get() + 1);
            index.set(index.get() + 1);
         });
      }
   }
}
