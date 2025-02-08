package net.hypixel.lynx.ui;

import com.codelanx.commons.util.ref.Tuple;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.Tab;
import net.hypixel.lynx.chat.tab.TabConfigMenu;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

public abstract class Menu implements Boundable {
   private static long lastPrint = System.currentTimeMillis();
   private final Set<Region> regions = new LinkedHashSet();
   private final Set<Supplier<Boolean>> displayPredicates = new HashSet();
   private final Map<Region, Set<Region>> bakedRegions = new HashMap();
   protected int width;
   protected int height;
   protected boolean centered = false;
   protected boolean autoHeight = false;
   protected boolean autoWidth = false;
   protected int top;
   protected int left;
   private boolean shown = false;
   private int right;
   private int bottom;
   private int backgroundColor = -1;
   private boolean unbaked = false;

   public Menu() {
      MenuHandler.register(this);
   }

   public static void main(String... args) {
      Menu m = new TabConfigMenu((Tab)null);
      System.out.println("Iterating them all...");
      PrintStream var10001 = System.out;
      m.regions.forEach(var10001::println);
   }

   public void requireForDisplay(Supplier<Boolean> predicate) {
      this.displayPredicates.add(predicate);
   }

   protected void addCloseButton() {
      this.addRegion(Region.builder().left(this.width - 10).right(this.width).top(0).bottom(10).color(127, 0, 0, 127).text("⨉").build().onClick((region, x, y, right) -> {
         if (!right) {
            this.hide();
         }

      }));
   }

   public void doRender(int ticks) {
      if (this.shown) {
         if (this.displayPredicates.stream().allMatch(Supplier::get)) {
            if (this.unbaked) {
               this.bake();
            }

            if (this.centered) {
               this.center();
            }

            this.right = this.left + this.width;
            this.bottom = this.top + this.height;
            GlStateManager.pushMatrix();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            boolean doPrint = false;
            if (System.currentTimeMillis() - lastPrint > 2000L) {
               doPrint = true;
               lastPrint = System.currentTimeMillis();
            }

            if (doPrint) {
               Util.out("Drawing menu: %s", this);
            }

            if (this.backgroundColor > 0) {
               Gui.drawRect(this.left, this.top, this.getRight(), this.bottom, this.backgroundColor);
            }

            this.regions.stream().forEach((r) -> {
               r.render(this);
            });
            this.render(ticks);
            GlStateManager.popMatrix();
         }
      }
   }

   protected void center() {
      int height = Lynx.DEBUGGING ? 1080 : Minecraft.getMinecraft().displayHeight;
      int width = Lynx.DEBUGGING ? 1920 : Minecraft.getMinecraft().displayWidth;
      if (Lynx.DEBUGGING) {
         this.left = (width - this.width) / 2 / 2;
         this.top = (height - this.height) / 2 / 2;
      } else {
         ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());
         this.left = (width - this.width) / 2 / sc.getScaleFactor();
         this.top = (height - this.height) / 2 / sc.getScaleFactor();
      }
   }

   protected abstract void render(int var1);

   protected void addRegion(Region region) {
      this.addRegion(region, false);
   }

   protected void addRegion(Region region, boolean bake) {
      this.regions.add(region);
      if (this.autoHeight) {
         this.top = (Integer)this.regions.stream().map(Region::getTop).min(Integer::compare).orElse(0);
         this.bottom = (Integer)this.regions.stream().map(Region::getBottom).max(Integer::compare).orElse(this.height);
         this.height = this.bottom - this.top;
      }

      if (this.autoWidth) {
         this.right = (Integer)this.regions.stream().map(Region::getRight).max(Integer::compare).orElse(this.width);
         this.left = (Integer)this.regions.stream().map(Region::getLeft).min(Integer::compare).orElse(0);
         this.width = this.right - this.left;
      }

      if (bake) {
         this.bake();
      } else {
         this.unbaked = true;
      }

   }

   protected void backgroundColor(int backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   public int getTop() {
      return this.top;
   }

   public int getBottom() {
      return this.bottom;
   }

   public int getRight() {
      return Math.max(this.right, (Integer)this.regions.stream().map(Region::getRight).max(Integer::compare).orElse(this.right));
   }

   public int getLeft() {
      return this.left;
   }

   public int getHeight() {
      return this.height;
   }

   protected void bake() {
      this.unbaked = false;
      this.bakedRegions.clear();
      if (this.centered) {
         this.center();
      }

      Util.out("Prebake; height: %d, width: %d, left: %d, top: %d", this.height, this.width, this.left, this.top);
      this.regions.forEach(this::bake);
   }

   protected void bake(Region r) {
      if (r.hasChildren()) {
         r.getChildren().forEach(this::bake);
      }

      this.bakeRegion(r);
   }

   private void bakeRegion(Region r) {
   }

   private List<Region> splits(Region main, List<Region> overlaid) {
      List<Region> back = new ArrayList();
      List<Region> splitted = Collections.singletonList(main);
      List<Region> remainder = splitted;
      boolean matchedOnce = false;
      boolean vertical = true;

      for(int runs = 0; runs < 20; ++runs) {
         List filledRegions;
         if (vertical) {
            filledRegions = this.split(splitted, overlaid, Region::getLeft, Region::getRight, (r, min, max) -> {
               return r.clone().setLeft(min).setRight(max);
            });
            if (!filledRegions.isEmpty()) {
               remainder = this.split(splitted, filledRegions, Region::getLeft, Region::getRight, (r, min, max) -> {
                  return r.clone().setLeft(min).setRight(max);
               });
            }
         } else {
            filledRegions = this.split(splitted, overlaid, Region::getBottom, Region::getTop, (r, min, max) -> {
               return r.clone().setBottom(min).setTop(max);
            });
            if (!filledRegions.isEmpty()) {
               remainder = this.split(splitted, filledRegions, Region::getBottom, Region::getTop, (r, min, max) -> {
                  return r.clone().setBottom(min).setTop(max);
               });
            }
         }

         if (filledRegions.equals(splitted)) {
            if (matchedOnce) {
               break;
            }

            matchedOnce = true;
         } else {
            matchedOnce = false;
            back.addAll(filledRegions);
            splitted = remainder;
         }

         vertical = !vertical;
      }

      return back;
   }

   public void onKeyTyped() {
      this.keyTyped();
   }

   protected abstract void keyTyped();

   public void onScroll(boolean up, boolean slow) {
   }

   public void click(boolean right) {
      if (this.shown) {
         ScaledResolution sc = new ScaledResolution(Minecraft.getMinecraft());
         int x = Mouse.getX() / sc.getScaleFactor() - this.left;
         int y = -((Mouse.getY() - this.height) / sc.getScaleFactor() - this.top);
         this.regions.forEach((r) -> {
            r.click(x, y, right);
         });
      }
   }

   public void show() {
      this.shown = true;
   }

   public void hide() {
      this.shown = false;
   }

   private <R> List<R> split(List<R> init, List<R> overlap, Function<R, Integer> min, Function<R, Integer> max, Menu.QuadFunction<R, Integer, Integer, R> remapper) {
      List<R> back = new LinkedList();
      boolean output = false;
      Iterator var8 = init.iterator();

      while(var8.hasNext()) {
         R ini = (R) var8.next();
         if (output) {
            System.out.println("getting bounds for: " + ini);
         }

         List<Tuple<Integer, Integer>> overlaid = new LinkedList();
         overlaid.add(new Tuple(min.apply(ini), max.apply(ini)));
         Iterator var11 = overlap.iterator();

         while(var11.hasNext()) {
            R ent2 = (R) var11.next();
            if (output) {
               System.out.println("\toverlapper: " + ent2);
            }

            int ovMin = (Integer)min.apply(ent2);
            int ovMax = (Integer)max.apply(ent2);
            List<Tuple<Integer, Integer>> overlaidsOverlaid = new LinkedList();
            Iterator var16 = overlaid.iterator();

            while(true) {
               while(var16.hasNext()) {
                  Tuple<Integer, Integer> ove = (Tuple)var16.next();
                  if (output) {
                     System.out.println("\t\ttesting overlaid: " + ove);
                  }

                  int initMin = (Integer)ove.getFirst();
                  int initMax = (Integer)ove.getSecond();
                  if (initMin >= ovMin && initMax <= ovMax) {
                     if (output) {
                        System.out.println("\t\t\ttotally overlaid, removing...");
                     }
                  } else if (initMax >= ovMin && initMin <= ovMax) {
                     Tuple t;
                     if (initMax >= ovMin) {
                        t = new Tuple(initMin, ovMin);
                        if (output) {
                           System.out.println("\t\t\tinitMax >= ovMin, adding: " + t);
                        }

                        overlaidsOverlaid.add(t);
                     }

                     if (initMin <= ovMax) {
                        t = new Tuple(ovMax, initMax);
                        if (output) {
                           System.out.println("\t\t\tinitMin <= ovMax, adding: " + t);
                        }

                        overlaidsOverlaid.add(t);
                     }
                  } else {
                     if (output) {
                        System.out.println("\t\t\thit irrelevancy: " + ent2 + " isn't bounded inside " + ove);
                     }

                     overlaidsOverlaid.add(ove);
                  }
               }

               if (output) {
                  System.out.println("\t\t\t\toverlaidsOverlaid: " + overlaidsOverlaid);
               }

               overlaid.clear();
               overlaid.addAll(overlaidsOverlaid);
               if (output) {
                  System.out.println("\t\t\t\toverlaid: " + overlaid);
               }
               break;
            }
         }

         if (output) {
            System.out.println("\t\t\t\t\tAdding overlaid to splitted: " + overlaid);
         }

         overlaid.removeIf((tx) -> {
            return (Integer)tx.getSecond() <= (Integer)tx.getFirst();
         });
         overlaid.forEach((tx) -> {
            back.add(remapper.map(ini, tx.getFirst(), tx.getSecond()));
         });
      }

      return back;
   }

   public String toString() {
      return "Menu{width=" + this.width + ", height=" + this.height + ", centered=" + this.centered + ", top=" + this.top + ", left=" + this.left + ", shown=" + this.shown + ", right=" + this.getRight() + ", bottom=" + this.bottom + ", unbaked=" + this.unbaked + '}';
   }

   // $FF: synthetic method
   private static boolean lambda$bakeRegion$3(Region br) {
      return br.getColor() >= 0;
   }

   // $FF: synthetic method
   private static Set lambda$bakeRegion$2(Region k) {
      return new HashSet();
   }

   private interface QuadFunction<A, B, C, D> {
      D map(A var1, B var2, C var3);
   }
}
