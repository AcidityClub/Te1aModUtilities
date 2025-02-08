package net.hypixel.lynx.ui.ui2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AbsoluteGroup implements AbsoluteContentHolder<AbsoluteContentElement> {
   private final List<AbsoluteContentElement> elements = new ArrayList();

   public AbsoluteGroup(AbsoluteContentElement... elems) {
      this.elements.addAll(Arrays.asList(elems));
   }

   public static AbsoluteGroup alignVertical(final boolean vertical, AbsoluteContentElement... elems) {
      return new AbsoluteGroup(elems) {
         public boolean vertical() {
            return vertical;
         }
      };
   }

   public void add(AbsoluteContentElement... element) {
      this.elements.addAll(Arrays.asList(element));
   }

   public void remove(AbsoluteContentElement element) {
      this.elements.remove(element);
   }

   public Stream<AbsoluteContentElement> viewElements() {
      return this.elements.stream();
   }
}
