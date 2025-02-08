package net.hypixel.lynx.ui.ui2.impl;

import com.codelanx.commons.util.Reflections;
import java.util.Arrays;
import java.util.List;

import net.hypixel.lynx.chat.Tab;
import net.hypixel.lynx.chat.tab.TabFilter;
import net.hypixel.lynx.chat.tab.TabType;
import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;
import net.hypixel.lynx.ui.ui2.AbsoluteGroup;
import net.hypixel.lynx.ui.ui2.Menu;
import net.hypixel.lynx.ui.ui2.elements.AbsoluteCheckboxElement;
import net.hypixel.lynx.ui.ui2.elements.AbsoluteDropdownElement;
import net.hypixel.lynx.ui.ui2.elements.AbsoluteLabelElement;
import net.hypixel.lynx.ui.ui2.elements.AbsoluteTextFieldElement;

public class TabConfig extends Menu<AbsoluteContentElement> {
   private final Tab tab;

   public TabConfig(Tab tab) {
      super("Edit tab: " + tab.getName());
      this.tab = tab;
      this.add(new AbsoluteContentElement[]{this.header()});
      AbsoluteGroup panes = new AbsoluteGroup(new AbsoluteContentElement[0]);
      panes.add(this.paneOne());
      panes.add(this.paneTwo());
      this.add(new AbsoluteContentElement[]{panes});
   }

   private AbsoluteGroup header() {
      AbsoluteGroup back = AbsoluteGroup.alignVertical(true);
      back.add(new AbsoluteGroup(new AbsoluteContentElement[]{new AbsoluteLabelElement("Type"), new AbsoluteTextFieldElement()}));
      back.add(new AbsoluteGroup(new AbsoluteContentElement[]{new AbsoluteLabelElement("Name"), new AbsoluteTextFieldElement()}));

      List<TabType> tabTypes = Arrays.asList(TabType.values());

      back.add(new AbsoluteGroup(new AbsoluteContentElement[]{
              new AbsoluteLabelElement("Something"),
              new AbsoluteDropdownElement(tabTypes, tabType -> this.tab.setType((TabType) tabType), this.tab.getType())
      }));

      return back;
   }

   private AbsoluteGroup paneOne() {
      AbsoluteGroup back = AbsoluteGroup.alignVertical(true);
      TabFilter[] fs = TabFilter.values();
      Arrays.stream(fs).forEach((f) -> {
         back.add(new AbsoluteGroup(new AbsoluteContentElement[]{new AbsoluteLabelElement(Reflections.properEnumName(f)), new AbsoluteCheckboxElement(this.tab.getFilters().contains(f), () -> {
            if (!this.tab.filter(f)) {
               this.tab.unfilter(f);
            }

         })}));
      });
      return back;
   }

   private AbsoluteGroup paneTwo() {
      AbsoluteGroup back = AbsoluteGroup.alignVertical(true);
      back.add(new AbsoluteGroup(new AbsoluteContentElement[]{new AbsoluteLabelElement("Max Lines"), new AbsoluteTextFieldElement()}));
      return back;
   }

   public boolean vertical() {
      return true;
   }
}
