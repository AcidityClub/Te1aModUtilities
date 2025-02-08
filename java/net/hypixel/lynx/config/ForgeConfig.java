package net.hypixel.lynx.config;

import com.codelanx.commons.config.ConfigFile;
import com.codelanx.commons.util.Reflections;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import net.hypixel.lynx.util.Util;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.Validate;

public interface ForgeConfig extends ConfigFile {
   default boolean ignoreInConfig() {
      return false;
   }

   public static class ForgeMenu {
      private final ConfigCategory parent;

      public <T extends ForgeConfig> ForgeMenu(String name, Class<T> val, T... categories) {
         this.parent = new ConfigCategory(name, ConfigFactory.PRIMARY_CATEGORY);
         Validate.isTrue(val.isEnum(), "Can only support enum-based ConfigFiles", new Object[0]);
         Field f = Util.getField(ConfigCategory.class, "properties");

         try {
            Map<String, Property> children = (Map)f.get(this.parent);
            Arrays.stream(val.getEnumConstants()).filter((v) -> {
               return !v.ignoreInConfig();
            }).map(PropertyAdapter::new).forEach((v) -> {
               Property var10000 = (Property)children.put(v.getName(), v);
            });
            Arrays.stream(categories).forEach((c) -> {
               new ConfigCategory(Reflections.properEnumName((Enum)c), this.parent);
            });
         } catch (IllegalAccessException var6) {
            var6.printStackTrace();
         }

      }

      public ConfigCategory getParent() {
         return this.parent;
      }
   }
}
