package net.hypixel.lynx.ui.ui2.elements;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.ui2.AbsoluteContentElement;
import net.hypixel.lynx.ui.ui2.AbsoluteGroup;
import net.minecraft.client.gui.Gui;

public class AbsoluteDropdownElement<T> extends AbsoluteGroup {
   private static final int PADDING = 4;
   private final Collection<T> options;
   private final Consumer<T> onSelect;
   private final Function<T, String> toString;
   private final AbsoluteDropdownElement<T>.DropDownView view;
   private final AbsoluteDropdownElement<T>.DropDownBox box;
   private T active;

   public AbsoluteDropdownElement(Collection<T> options, Consumer<T> onSelect) {
      this(options, onSelect, null);
   }

   public AbsoluteDropdownElement(Collection<T> options, Consumer<T> onSelect, T active) {
      this(options, onSelect, Objects::toString, active);
   }

   public AbsoluteDropdownElement(Collection<T> options, Consumer<T> onSelect, Function<T, String> toString, T active) {
      super();
      this.options = options;
      this.onSelect = onSelect;
      this.toString = toString;
      this.active = active;
      this.view = new AbsoluteDropdownElement.DropDownView(this.options);
      this.box = new AbsoluteDropdownElement.DropDownBox();
      this.add(new AbsoluteContentElement[]{this.box, this.view});
   }

   public int getWidth() {
      return this.box.getWidth() + 4;
   }

   public T getActive() {
      return this.active;
   }

   public int getHeight() {
      return this.box.getHeight() + 4;
   }

   public void onDraw(int x, int y) {
   }

   public void onClick(int x, int y) {
   }

   public boolean vertical() {
      return true;
   }

   private class DropDownButton implements AbsoluteContentElement {
      private DropDownButton() {
      }

      public int getWidth() {
         return 4;
      }

      public int getHeight() {
         return 11;
      }

      public void onDraw(int x, int y) {
         UIS.drawText("v", x + 1, y + 1);
      }

      // $FF: synthetic method
      DropDownButton(Object x1) {
         this();
      }
   }

   private class DropDownSelection implements AbsoluteContentElement {
      private DropDownSelection() {
      }

      public int getWidth() {
         return 0;
      }

      public int getHeight() {
         return 11;
      }

      public void onDraw(int x, int y) {
         UIS.drawText((String)AbsoluteDropdownElement.this.toString.apply(AbsoluteDropdownElement.this.active), x + 1, y);
      }

      // $FF: synthetic method
      DropDownSelection(Object x1) {
         this();
      }
   }

   private class DropDownBox extends AbsoluteGroup {
      private final AbsoluteDropdownElement<T>.DropDownSelection selection = AbsoluteDropdownElement.this.new DropDownSelection();
      private final AbsoluteDropdownElement<T>.DropDownButton button = AbsoluteDropdownElement.this.new DropDownButton();

      public DropDownBox() {
         super();
         this.add(new AbsoluteContentElement[]{this.selection, this.button});
      }

      public int getWidth() {
         return AbsoluteDropdownElement.this.view.getWidth() + this.button.getWidth();
      }

      public int getHeight() {
         return Math.max(this.selection.getHeight(), this.button.getHeight());
      }

      public void onDraw(int x, int y) {
      }

      public void onClick(int x, int y) {
         AbsoluteDropdownElement.this.view.toggleVisible();
      }

      public boolean vertical() {
         return false;
      }
   }

   private class DropDownView implements AbsoluteContentElement {
      private final T[] options;
      private boolean visible;

      public DropDownView(T... options) {
         this.visible = false;
         this.options = options;
      }

      public DropDownView(Collection<T> options) {
         this((T) options.toArray());
      }

      public DropDownView(Stream<? extends T> options) {
         this((Collection)((Collection)options.collect(Collectors.toSet())));
      }

      public int getWidth() {
         return (Integer)Arrays.stream(this.options).map(AbsoluteDropdownElement.this.toString).map(Regions::getStringWidth).max(Integer::compare).map((i) -> {
            return i + 2;
         }).orElse(0);
      }

      public int getHeight() {
         return AbsoluteDropdownElement.this.options.size() * (AbsoluteDropdownElement.this.getHeight() - 4);
      }

      public void onDraw(int x, int y) {
         if (this.visible) {
            int width = this.getWidth();
            if (width <= 0) {
               return;
            }

            AtomicInteger aY = new AtomicInteger(y);
            int background = this.getBackroundRGB();
            int height = this.getHeight();
            Arrays.stream(this.options).forEach((o) -> {
               int mY = aY.get();
               Gui.drawRect(x, mY, x + width, mY + height, background);
               UIS.drawText((String)AbsoluteDropdownElement.this.toString.apply(o), x + 1, mY + 1);
               aY.set(mY + height);
            });
         }

      }

      public void onClick(int x, int y) {
         int incr = this.getHeight() / this.options.length;
         int index = y / incr;
         index = index < 0 ? 0 : Math.max(index, this.options.length - 1);
         AbsoluteDropdownElement.this.active = this.options[index];
      }

      private void toggleVisible() {
         this.visible = !this.visible;
      }
   }
}
