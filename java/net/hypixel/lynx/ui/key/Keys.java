package net.hypixel.lynx.ui.key;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.lwjgl.input.Keyboard;

public class Keys {
   private static Map<KeyBinding, Runnable> actions = new HashMap();
   private static Map<Integer, Long> down = new HashMap();
   private static long tick = 0L;

   public static boolean isActivated(int key) {
      updateKey(key);
      return (Long)down.getOrDefault(key, 0L) == tick;
   }

   public static void updateTick() {
      ++tick;
      Iterator itr = down.entrySet().iterator();

      while(itr.hasNext()) {
         Entry<Integer, Long> ent = (Entry)itr.next();
         long next = getKeyUpdate((Integer)ent.getKey());
         if (next < 0L) {
            itr.remove();
         } else {
            ent.setValue(next);
         }
      }

      actions.entrySet().stream().filter((e) -> {
         return ((KeyBinding)e.getKey()).isApplicable();
      }).map(Entry::getValue).forEach(Runnable::run);
   }

   private static void updateKey(int key) {
      long val = getKeyUpdate(key);
      if (val < 0L) {
         down.remove(key);
      } else {
         down.putIfAbsent(key, val);
      }

   }

   private static long getKeyUpdate(int key) {
      boolean held = Keyboard.isKeyDown(key);
      return !held ? -1L : tick;
   }

   public static boolean isHeld(int key) {
      updateKey(key);
      return (Long)down.getOrDefault(key, Long.MAX_VALUE) <= tick;
   }

   public static boolean registerBinding(KeyBinding bind, Runnable action) {
      actions.put(bind, action);
      return false;
   }

   public static boolean registerBinding(KeyBinding bind, Supplier<Boolean> predicate, Runnable action) {
      actions.put(bind, () -> {
         if ((Boolean)predicate.get()) {
            action.run();
         }

      });
      return false;
   }
}
