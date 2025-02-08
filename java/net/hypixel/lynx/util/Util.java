package net.hypixel.lynx.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import net.hypixel.lynx.Lynx;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Level;

public class Util {
   private static Runnable test;

   public static void setTestable(Runnable test) {
      Util.test = test;
   }

   public static void runTestable() {
      if (test != null) {
         test.run();
      }

   }

   public static void out(String out, Object... args) {
      out = String.format(out, args);
      FMLLog.log("Lynx", Level.INFO, out.replace("%", "%%"), new Object[0]);
   }

   public static void error(Throwable ex, String out, Object... args) {
      out = String.format(out, args);
      FMLLog.log("Lynx", Level.INFO, ex, out.replace("%", "%%"), new Object[0]);
   }

   public static Field getField(Class<?> clazz, Versionable field) {
      return getField(clazz, field.get());
   }

   public static Field getField(Class<?> clazz, String field) {
      Field back = null;
      if (Lynx.DEBUGGING) {
         return null;
      } else {
         try {
            back = clazz.getDeclaredField(field);
            back.setAccessible(true);
         } catch (NoSuchFieldException var4) {
            var4.printStackTrace();
         }

         return back;
      }
   }

   public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
      Method back = null;
      if (Lynx.DEBUGGING) {
         return null;
      } else {
         try {
            back = clazz.getDeclaredMethod(method, params);
            back.setAccessible(true);
         } catch (NoSuchMethodException var5) {
            var5.printStackTrace();
         }

         return back;
      }
   }

   public static void register(Object o, EventBus bus) {
      if (!Lynx.DEBUGGING) {
         bus.register(o);
      }

   }

   public static void printFor(Class<?> cl, Function<Class<?>, ? extends Object[]> mapper) {
      Arrays.stream((Object[])mapper.apply(cl)).map(Object::toString).forEach((x$0) -> {
         out(x$0);
      });
   }
}
