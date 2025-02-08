package net.hypixel.lynx.ui.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

public interface KeyBinding {
   Map<Integer, Integer> _ALIASES = Collections.unmodifiableMap(new HashMap<Integer, Integer>() {
      {
         this.put(157, 29);
         this.put(54, 42);
         this.put(29, 157);
         this.put(42, 54);
         this.put(74, 12);
         this.put(12, 74);
      }
   });
   Map<Integer, String> _NAME_ALIASES = Collections.unmodifiableMap(new HashMap<Integer, String>() {
      {
         this.put(157, "Control");
         this.put(54, "Shift");
         this.put(29, "Control");
         this.put(42, "Shift");
         this.put(51, ",");
         this.put(52, ".");
         this.put(78, "+");
         this.put(12, "-");
         this.put(13, "=");
         this.put(82, "0");
         this.put(79, "1");
         this.put(80, "2");
         this.put(81, "3");
         this.put(75, "4");
         this.put(76, "5");
         this.put(77, "6");
         this.put(71, "7");
         this.put(72, "8");
         this.put(73, "9");
         this.put(43, "\\");
      }
   });
   Map<int[], KeyBinding> _CACHE = new HashMap();

   static KeyBinding of(int... keys) {
      int[] key = new int[keys.length];
      System.arraycopy(keys, 0, key, 0, keys.length);
      Arrays.sort(key);
      return (KeyBinding)_CACHE.computeIfAbsent(key, (k) -> {
         return () -> {
            return keys;
         };
      });
   }

   static KeyBinding empty() {
      return (KeyBinding)_CACHE.computeIfAbsent(null, (k) -> {
         return () -> {
            return new int[0];
         };
      });
   }

   static boolean isReversed() {
      return KeyBindings.INVERT_DIRECTION.isApplicable();
   }

   int[] getKeys();

   default boolean isApplicable() {
      int[] keys = this.getKeys();
      if (keys.length <= 0) {
         return false;
      } else {
         return Arrays.stream(keys).allMatch((i) -> {
            return Keys.isHeld(i) || Keys.isHeld((Integer)_ALIASES.getOrDefault(i, i));
         }) && Arrays.stream(keys).anyMatch(Keys::isActivated);
      }
   }

   default String getName() {
      int[] keys = this.getKeys();
      if (keys.length <= 0) {
         return "-1";
      } else {
         return keys.length == 1 ? this.nameFor(keys[0]) : StringUtils.join((Iterable)Arrays.stream(keys).boxed().map(this::nameFor).collect(Collectors.toList()), "+");
      }
   }

   default String nameFor(int key) {
      return (String)_NAME_ALIASES.getOrDefault(key, key > 0 ? Keyboard.getKeyName(key) : "-1");
   }

   default boolean isEmpty() {
      return this.getKeys().length <= 0;
   }
}
