package net.hypixel.lynx.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.hypixel.lynx.util.DependencyUtil;
import net.minecraft.client.gui.Gui;

public class Region implements Cloneable, Boundable {
   public static final int TEXT_BUFFER = 2;
   public static final int ELEMENT_BUFFER = 1;
   private final List<Region> children;
   private final List<DependencyUtil.QuadConsumer<? super Region, Integer, Integer, Boolean>> onClick;
   private final List<DependencyUtil.TriConsumer<? super Region, Integer, Integer>> onHover;
   private int left;
   private int right;
   private int top;
   private int bottom;
   private int absTop;
   private int absLeft;
   private Integer color;
   private boolean element;
   private boolean inheritBoundsFromChildren;
   private boolean childrenVisible;
   private Supplier<String> text;

   private Region(Supplier<String> text, boolean element, int left, int top, int right, int bottom, Integer color) {
      this.children = new LinkedList();
      this.onClick = new LinkedList();
      this.onHover = new LinkedList();
      this.inheritBoundsFromChildren = true;
      this.childrenVisible = true;
      this.text = text;
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
      this.color = color;
      this.element = element;
   }

   protected Region(Region.Builder build) {
      this(build.text, build.element, build.left, build.top, build.right, build.bottom, build.color);
   }

   public static Region.Builder builder() {
      return new Region.Builder();
   }

   public Region onClick(DependencyUtil.QuadConsumer<? super Region, Integer, Integer, Boolean> onClick) {
      this.onClick.add(onClick);
      return this;
   }

   public void click(int parentX, int parentY, boolean rightClick) {
      int x = parentX - this.left;
      int y = parentY - this.top;
      if (this.contains(parentX, parentY)) {
         this.onClick.forEach((f) -> {
            f.accept(this, parentX, parentY, rightClick);
         });
      }

      if (this.isChildrenVisible()) {
         this.children.forEach((c) -> {
            c.click(x, y, rightClick);
         });
      }

   }

   private boolean contains(int x, int y) {
      return x >= this.left && x <= this.right && y <= this.bottom && y >= this.top;
   }

   public Region onHover(DependencyUtil.QuadConsumer<? super Region, Integer, Integer, Boolean> onClick) {
      this.onClick.add(onClick);
      return this;
   }

   public void hover(int parentX, int parentY) {
      int x = parentX - this.left;
      int y = parentY - this.top;
      this.onHover.forEach((f) -> {
         f.accept(this, x, y);
      });
      if (this.isChildrenVisible()) {
         this.children.forEach((c) -> {
            c.hover(x, y);
         });
      }

   }

   public boolean overlap(Region other) {
      return this.left < other.right || this.right > other.left || this.top < other.bottom || this.bottom > other.top;
   }

   public int getLeft() {
      return this.left;
   }

   public Region setLeft(int left) {
      this.left = left;
      return this;
   }

   public int getAbsoluteLeft() {
      return this.absLeft;
   }

   public int getAbsoluteTop() {
      return this.absTop;
   }

   public int getRight() {
      int right;
      if (this.inheritBoundsFromChildren) {
         right = this.left + (Integer)this.children.stream().map(Region::getRight).max(Integer::compare).orElse(this.right - this.left);
      } else {
         right = this.right;
      }

      if (this.text != null) {
         right = Math.max(right, this.left + (this.isElement() ? 1 : 2) * 2 + Regions.getStringWidth((String)this.text.get()));
      }

      return right;
   }

   public Region setRight(int right) {
      this.right = right;
      return this;
   }

   public int getTop() {
      return this.top;
   }

   public Region setTop(int top) {
      this.top = top;
      return this;
   }

   public int getBottom() {
      int bottom;
      if (this.inheritBoundsFromChildren) {
         bottom = this.top + (Integer)this.children.stream().map(Region::getBottom).max(Integer::compare).orElse(this.bottom - this.top);
      } else {
         bottom = this.bottom;
      }

      if (this.top < bottom) {
         bottom = this.top + 1;
      }

      if (this.text != null && bottom - this.top < 9) {
         bottom = this.top + 9;
      }

      return Math.max(this.bottom, bottom);
   }

   public Region setBottom(int bottom) {
      this.bottom = bottom;
      return this;
   }

   public Integer getColor() {
      return this.color;
   }

   public Region setColor(Integer color) {
      this.color = color;
      return this;
   }

   public Region setColor(int red, int green, int blue) {
      return this.setColor(red, green, blue, 255);
   }

   public Region setColor(int red, int green, int blue, int alpha) {
      return this.setColor(Regions.colorFromRGB(red, green, blue, alpha));
   }

   public String getText() {
      return (String)this.text.get();
   }

   public Region setText(Supplier<String> text) {
      this.text = text;
      return this;
   }

   public Region setText(String text) {
      return this.setText(() -> {
         return text;
      });
   }

   public void render(Boundable parent) {
      this.render(parent.getLeft(), parent.getTop());
   }

   public void render(int left, int top) {
      this.absLeft = left + this.getLeft();
      this.absTop = top + this.getTop();
      if (this.color != null) {
         Gui.drawRect(this.absLeft, this.absTop, left + this.getRight(), top + this.getBottom(), this.getColor());
      }

      if (this.text != null) {
         int textPlacement = (this.getBottom() - this.getTop() - 9) / 2;
         UIS.drawText((String)this.text.get(), this.absLeft + (this.isElement() ? 1 : 2), this.absTop + textPlacement);
      }

      if (this.isChildrenVisible()) {
         this.children.forEach((r) -> {
            r.render(this.absLeft, this.absTop);
         });
      }

   }

   public Region addChild(Region r) {
      this.children.add(r);
      return this;
   }

   public boolean hasChildren() {
      return this.children.size() > 0;
   }

   public List<Region> getChildren() {
      return Collections.unmodifiableList(this.children);
   }

   public boolean isChildrenVisible() {
      return this.childrenVisible;
   }

   public Region setChildrenVisible(boolean childrenVisible) {
      this.childrenVisible = childrenVisible;
      return this;
   }

   public boolean isInheritBoundsFromChildren() {
      return this.inheritBoundsFromChildren;
   }

   public Region setInheritBoundsFromChildren(boolean inheritBoundsFromChildren) {
      this.inheritBoundsFromChildren = inheritBoundsFromChildren;
      return this;
   }

   protected Region clone() {
      try {
         return (Region)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new Error(var2);
      }
   }

   public String toString() {
      return "Region{text='" + (this.text == null ? null : (String)this.text.get()) + '\'' + ", left=" + this.left + ", right=" + this.right + ", bottom=" + this.bottom + ", top=" + this.top + ", color=" + this.color + ", element=" + this.element + ", children=" + this.children + '}';
   }

   public boolean isElement() {
      return this.element;
   }

   public void setElement(boolean element) {
      this.element = element;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Region region = (Region)o;
         return this.getLeft() == region.getLeft() && this.getRight() == region.getRight() && this.getTop() == region.getTop() && this.getBottom() == region.getBottom() && this.getColor() == region.getColor() && this.isElement() == region.isElement();
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.getLeft();
      result = 31 * result + this.getRight();
      result = 31 * result + this.getTop();
      result = 31 * result + this.getBottom();
      result = 31 * result + (this.getColor() == null ? 0 : this.getColor());
      result = 31 * result + (this.isElement() ? 1 : 0);
      return result;
   }

   // $FF: synthetic method
   Region(Supplier x0, boolean x1, int x2, int x3, int x4, int x5, Integer x6, Object x7) {
      this(x0, x1, x2, x3, x4, x5, x6);
   }

   public static class Builder {
      private int left;
      private int right;
      private int top;
      private int bottom;
      private Integer color = null;
      private boolean element;
      private Supplier<String> text;

      public Region.Builder left(int left) {
         this.left = left;
         return this;
      }

      public Region.Builder isElement() {
         this.element = true;
         return this;
      }

      public Region.Builder right(int right) {
         this.right = right;
         return this;
      }

      public Region.Builder top(int top) {
         this.top = top;
         return this;
      }

      public Region.Builder bottom(int bottom) {
         this.bottom = bottom;
         return this;
      }

      public Region.Builder text(String text) {
         return this.text(() -> {
            return text;
         });
      }

      public Region.Builder text(Supplier<String> text) {
         this.text = text;
         return this;
      }

      public Region.Builder color(int red, int green, int blue) {
         return this.color(red, green, blue, 255);
      }

      public Region.Builder color(int red, int green, int blue, int alpha) {
         return this.color(Regions.colorFromRGB(red, green, blue, alpha));
      }

      public Region.Builder color(int color) {
         this.color = color;
         return this;
      }

      public Region build() {
         return new Region(this.text, this.element, this.left, this.top, this.right, this.bottom, this.color);
      }

      public <R extends Region> R build(Function<Region.Builder, R> regionMaker) {
         return (R) regionMaker.apply(this);
      }
   }
}
