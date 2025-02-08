package net.hypixel.lynx.config;

import com.codelanx.commons.config.DataHolder;
import com.codelanx.commons.config.RelativePath;
import com.codelanx.commons.data.types.Json;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RelativePath("hypixel-lynx/global-config.json")
public enum ClientConfig implements ForgeConfig {
   LAST_MESSAGE_GLOBAL("messages.sent-message-history-global", true),
   TABS("messages.tabs", new ArrayList()),
   PMS_USE_DISPLAY_NAMES("messages.pm-window-uses-display-names", true),
   TAB_FLASH_ON_NEW_MSG("tabs.flash-on-new-msg", true),
   TAB_FLASH_ON_PING("tabs.flash-on-ping", true),
   KEEP_CHATWINDOW_MAXIMIZED("chat.keep-maximized", true),
   PING_NOTIFICATIONS("chat.pings", new ArrayList()),
   ALWAYS_SHOW_CHAT_TABS("chat.always-show-tabs", false),
   CLOSE_AFTER_ACTION("target.close-after-action", false),
   MACROS("macros", macroDefaults()),
   CLEAR_CHAT_ON_LOGOUT("chat.clear-on-disconnect", true),
   CHAT_CHANNELS("chat.channels", new HashMap());

   private static final DataHolder<Json> DATA = new DataHolder(Json.class);
   private final String path;
   private Object def;

   private ClientConfig(String path, Object def) {
      this.path = path;
      this.def = def;
   }

   private static Map<String, Object> macroDefaults() {
      Map<String, Object> back = new LinkedHashMap();
      back.put("Warning: Spamming", "Please don't spam chat");
      back.put("Warning: Advertising", "Please don't advertise in chat");
      back.put("Warning: Capslock", "Please don't use all caps in chat");
      back.put("Warning: Inappropriate", "Please be appropriate in chat");
      back.put("Warning: Polite", "Please be respectful in chat");
      back.put("Guide: Youtuber", "To be eligible for the YouTube rank you must have 30,000 subscribers. You can apply via www.hypixel.net/threads/351480/");
      back.put("Guide: Join Staff", "To become Helper you must be VIP, 16+ and have access to TS3. For more information: www.hypixel.net/volunteer");
      back.put("Guide: Allowed mods", "To find a list of allowed modifications please visit www.hypixel.net/allowed-mods");
      back.put("Guide: Billing support", "If you have not received an item after one hour of purchasing, go to support.hypixel.net");
      back.put("Guide: Appeal", Arrays.asList("To appeal a ban or a mute, head over to hypixel.net/appeal", "You can appeal a ban or mute at hypixel.net/appeal", "Bans and mutes are appealed at: hypixel.net/appeal"));
      back.put("Guide: Report Hacker", "To report hackers, please use /wdr and then their name and type of hacks");
      back.put("Guide: Report Chat", "To report people in chat, please use /chatreport and then their name");
      back.put("Guide: Resource Pack", "Type /resourcehelp for help fixing resource packs.");
      return back;
   }

   public String getPath() {
      return this.path;
   }

   public Object getDefault() {
      return this.def;
   }

   public boolean ignoreInConfig() {
      return this == TABS || this == MACROS || this == CHAT_CHANNELS || this == PING_NOTIFICATIONS;
   }

   public DataHolder<Json> getData() {
      return DATA;
   }
}
