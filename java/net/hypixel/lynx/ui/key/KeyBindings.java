package net.hypixel.lynx.ui.key;

import com.codelanx.commons.config.DataHolder;
import com.codelanx.commons.config.RelativePath;
import com.codelanx.commons.data.FileDataType;
import com.codelanx.commons.data.types.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import net.hypixel.lynx.config.ForgeConfig;

@RelativePath("hypixel-lynx/keybindings.json")
public enum KeyBindings implements ForgeConfig, KeyBinding {
   NEXT_TAB("next-tab", Arrays.asList(29, 15)),
   INVERT_DIRECTION("invert-direction", 42),
   SELECT_TARGET("select-target", 19),
   PREVIOUS_PAGE("previous-page", 12),
   NEXT_PAGE("next-page", 13),
   ACTION_BANS("action.bans", 48),
   // ACTION_GOTO_SERVER("action.go-to-server", 45),
   // ACTION_PORT("action.port", 47),
   ACTION_MUTE("action.mute", 50),
   ACTION_MACRO("action.macro", 49),
   ACTION_AURABOT("action.aurabot", 51),
   ACTION_REPORT("action.report", 52),
   ACTION_USERINFO("action.userinfo", 22),
   ACTION_VIEWBANS("action.view-bans", 21),
   ACTION_AURAPANIC("action.aura-panic", 44),
   ACTION_TESTANTIKB("action.test-antikb", 37),
   ACTION_PULL("action.pull", 36),
   ACTION_GOLIATH("action.goliath", 34),
   ACTION_SENDPM("action.sendpm", 25),
   ACTION_CANCEL("action.cancel", 46),
   ACTION_TUNNEL_TO("action.tunnelto", 43),
   ACTION_FOLLOW("action.mmc-follow", 45),
   ACTION_CHECK("action.mmc-check", 47),
   ACTION_MMC_REPORT("action.mmc-report", 25),
   ACTION_MMC_JUMPTO("action.mmc-jumpto", 34),
   ACTION_TOGGLE_REACH_ALERTS("action.toggle-reach-alerts", 36),
   ACTION_PING("action.ping", 22);

   private static final DataHolder<Json> DATA = new DataHolder(Json.class);
   private final String path;
   private final Object def;

   private KeyBindings(String path, Object def) {
      this.path = path;
      this.def = def;
   }

   public String getPath() {
      return this.path;
   }

   public Object getDefault() {
      return this.def;
   }

   public int[] getKeys() {
      if (this.get() instanceof List) {
         List sigmaList = this.as(List.class, Number.class);
         // I'm stupid
         // List<Integer> prettySigma = Collections.emptyList();
         List<Integer> prettySigma = new ArrayList<>();
         for (Object sigma : sigmaList) {
            prettySigma.add(((Number)sigma).intValue());
         }

         int[] array = prettySigma.stream().mapToInt(Integer::intValue).toArray();
         return array;
      } else {
         return new int[]{((Number) this.as(Number.class)).intValue()};
      }
   }


   public DataHolder<? extends FileDataType> getData() {
      return DATA;
   }
}
