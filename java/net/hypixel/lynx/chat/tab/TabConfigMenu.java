package net.hypixel.lynx.chat.tab;

import com.codelanx.commons.logging.Debugger;
import com.codelanx.commons.util.Reflections;
import java.io.IOException;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.chat.Tab;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.ui.Menu;
import net.hypixel.lynx.ui.Region;
import net.hypixel.lynx.ui.Regions;
import net.minecraft.client.Minecraft;

public class TabConfigMenu extends Menu {
   private static TabConfigMenu shown = null;

   public TabConfigMenu(Tab t) {
      this.centered = true;
      this.width = 200;
      this.height = 200;
      int absY = 30;
      Region filterRegion = Region.builder().left(3).top(absY + 3).right(Regions.CHECKBOX_WIDTH + 35).bottom(this.height).build();
      int typeWidth = Regions.getStringWidth("Type:");
      int nameWidth = Regions.getStringWidth("Name:");
      this.addRegion(Region.builder().text("Name:").top(16).bottom(25).left(5).right(5 + nameWidth).build());
      int var10001 = 8 + nameWidth;
      String var10003 = t.getName();
      t.getClass();
      this.addRegion(Regions.textField(var10001, 16, var10003, t::setName).setColor(102, 102, 102, 127));
      this.addRegion(Region.builder().text("Type:").top(5).bottom(15).left(5).right(5 + typeWidth).build());
      this.addRegion(Regions.dropDown(typeWidth + 8, 5, 70, () -> {
         return Reflections.properEnumName(t.getType());
      }, (s) -> {
         TabType type = TabType.valueOf(s.toUpperCase().replace(' ', '_'));
         filterRegion.setChildrenVisible(type != TabType.PRIVATE_MESSAGES);
         t.setType(type);
      }, TabType.names()));
      this.addRegion(Region.builder().top(absY + 1).bottom(absY + 2).left(0).right(this.width).color(255, 255, 255, 127).build());
      absY = absY + 3;
      int relY = 0;
      filterRegion.addChild(Region.builder().text("Filters:").top(relY + 5).bottom(relY + 15).left(5).right(30).build());
      relY = relY + 20;
      TabFilter[] var7 = TabFilter.values();
      int var8 = var7.length;

      int maxLinesWidth;
      for(maxLinesWidth = 0; maxLinesWidth < var8; ++maxLinesWidth) {
         TabFilter filt = var7[maxLinesWidth];
         filterRegion.addChild(Regions.checkbox(Reflections.properEnumName(filt), () -> {
            return t.getFilters().contains(filt);
         }, 0, relY + 2).onClick((region, x, y, right) -> {
            boolean act = t.getFilters().contains(filt);
            if (!right) {
               act = !act;
               if (act) {
                  t.filter(filt);
               } else {
                  t.unfilter(filt);
               }
            }

         }));
         relY += 11;
      }

      filterRegion.setChildrenVisible(t.getType() != TabType.PRIVATE_MESSAGES);
      this.addRegion(filterRegion);
      int filtRight = filterRegion.getRight();
      this.addRegion(Region.builder().top(absY - 1).bottom(this.height).left(filtRight + 1).right(filtRight + 2).color(255, 255, 255, 127).build());
      Region settingsRegion = Region.builder().left(filtRight + 4).top(absY).right(this.width).bottom(this.height).build();
      relY = 20;
      maxLinesWidth = Regions.getStringWidth("Max lines:");
      settingsRegion.addChild(Region.builder().text("Max lines:").top(relY).bottom(relY + 11).build());
      settingsRegion.addChild(Regions.textField(maxLinesWidth + 3, relY, 40, "" + t.getWindow().getMaxLines(), (s) -> {
         t.getWindow().setMaxLines(Integer.valueOf(s));
      }).addConstraint((i, c) -> {
         return Character.isDigit(c);
      }).setColor(102, 102, 102, 127));
      relY = relY + 11;
      relY += 10;
      settingsRegion.addChild(Regions.checkbox("Filters = whitelist?", t::isFiltersAsWhitelist, 0, relY).onClick((region, x, y, right) -> {
         if (!right) {
            t.setFiltersAsWhitelist(!t.isFiltersAsWhitelist());
         }

      }));
      relY += 11;
      relY += 10;
      settingsRegion.addChild(Regions.button(0, relY, "Save", Regions.colorFromRGB(51, 153, 51, 204), (right) -> {
         ClientConfig.TABS.set(ChatFacade.get().getTabs());

         try {
            ClientConfig.TABS.save();
         } catch (IOException var2) {
            Debugger.error(var2, "Error saving tab data");
         }

      }));
      relY += 15;
      relY += 10;
      settingsRegion.addChild(Regions.button(0, relY, "Clear", Regions.colorFromRGB(51, 51, 153, 204), (right) -> {
         t.getWindow().clearChatMessages();
      }));
      relY += 15;
      relY += 10;
      settingsRegion.addChild(Regions.button(0, relY, "Delete", Regions.colorFromRGB(153, 51, 51, 204), (right) -> {
         ChatFacade.get().deleteTab(t);
         this.hide();
      }));
      relY += 11;
      relY += 10;
      this.addRegion(settingsRegion);
      this.addCloseButton();
      this.backgroundColor(Regions.colorFromRGB(0, 0, 0, 127));
      this.requireForDisplay(() -> {
         return Minecraft.getMinecraft().currentScreen != null;
      });
   }

   protected void render(int ticks) {
   }

   protected void keyTyped() {
   }

   public void show() {
      super.show();
      if (shown != null) {
         shown.hide();
      }

      shown = this;
   }

   public void hide() {
      super.hide();
      shown = null;
   }
}
