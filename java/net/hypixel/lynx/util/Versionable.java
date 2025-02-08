package net.hypixel.lynx.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.hypixel.lynx.Lynx;

public enum Versionable {
   PERSISTANT_CHAT_GUI_FIELD,
   GUICHAT_INPUT_FIELD,
   GUICHAT_TEXT_FIELD,
   GUICHAT_DEFAULT_TEXT,
   CHAT_MAX_WIDTH;

   private static final Map<String, String> MAPPING = Lynx.DEBUGGING ? Collections.unmodifiableMap(DependencyUtil.Remapper.getObfMapping()) : null;
   private static final Versionable.Version CURRENT = Versionable.Version.V1_8_9;

   public static String getMapping(String input) {
      return (String)MAPPING.getOrDefault(input, input);
   }

   public String get() {
      return (String)CURRENT.vals.get(this);
   }

   private static enum Version {
      V1_8_9(new EnumMap<Versionable, String>(Versionable.class) {
         {
            this.put(Versionable.PERSISTANT_CHAT_GUI_FIELD, "field_73840_e");
            this.put(Versionable.GUICHAT_INPUT_FIELD, "field_146415_a");
            this.put(Versionable.GUICHAT_TEXT_FIELD, "field_146216_j");
            this.put(Versionable.GUICHAT_DEFAULT_TEXT, "field_146409_v");
            this.put(Versionable.CHAT_MAX_WIDTH, "field_148272_O");
         }
      }),
      V1_9(new HashMap());

      private final Map<Versionable, String> vals;

      private Version(Map<Versionable, String> vals) {
         this.vals = vals;
      }
   }
}
