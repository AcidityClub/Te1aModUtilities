package net.hypixel.lynx;

import com.codelanx.commons.logging.Debugger;
import com.codelanx.commons.logging.Logging;
import com.codelanx.commons.util.Scheduler;
import com.codelanx.commons.util.ref.Box;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;

import net.hypixel.lynx.anticheat.ReachNotifier;
import net.hypixel.lynx.chat.ChatFacade;
import net.hypixel.lynx.chat.Tab;
import net.hypixel.lynx.chat.TempChatInterceptor;
import net.hypixel.lynx.chat.channel.CommandChannel;
import net.hypixel.lynx.chat.channel.CommandChannels;
import net.hypixel.lynx.config.ClientConfig;
import net.hypixel.lynx.config.GameMode;
import net.hypixel.lynx.moderate.LynxUI;
import net.hypixel.lynx.moderate.Option;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.key.KeyBindings;
import net.hypixel.lynx.ui.key.KeyListener;
import net.hypixel.lynx.util.LynxInterpreter;
import net.hypixel.lynx.util.Util;
import net.hypixel.lynx.util.Versionable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.message.Message;
import org.lwjgl.input.Mouse;

@Mod(
   modid = "fortnite1337",
   name = "Te1aModUtils",
   guiFactory = "net.hypixel.lynx.config.ConfigFactory",
   clientSideOnly = true
)

public class Lynx {
   public static final boolean DEBUGGING = false;
   private static final Field CHAT_GUI_FIELD;
   @Instance("Lynx")
   private static Lynx instance;
   private static int last = -1000;
   private final LynxInterpreter interpreter = new LynxInterpreter();
   private final KeyListener keyListener = new KeyListener();
   private final Map<String, String> meta = new HashMap();
   private GameMode mode;
   private ChatFacade chat;
   private LynxUI currentTarget;
   private StatusUI stats;
   private ReachNotifier reachNotifier;

   private boolean doingReachCheckForTarget = false;

   public Lynx() {
      this.mode = GameMode.COMMAND;
      this.currentTarget = null;
   }

   public static GameMode getMode() {
      return get().mode;
   }

   public static boolean isTargeting() {
      return get().currentTarget != null;
   }

   public static LynxUI getTargetingUI() {
      return get().currentTarget;
   }

   public static void target(String username) {
      Util.out("Now targeting: %s", username);
      get().currentTarget = new LynxUI(username);
      UIS.player = new UIS.RenderLynxPlayer(Minecraft.getMinecraft().getRenderManager());
   }

   public static void untarget() {
      get().currentTarget = null;
      UIS.player = null;
      get().setDoingReachCheckForTarget(false);
   }

   public boolean isDoingReachCheckForTarget() {
      return doingReachCheckForTarget;
   }

   public void setDoingReachCheckForTarget(boolean doingReachCheckForTarget) {
      this.doingReachCheckForTarget = doingReachCheckForTarget;
   }

   public static Lynx get() {
      return instance;
   }

   public static LynxInterpreter getInterpeter() {
      return get().interpreter;
   }

   public static ChatFacade getChat() {
      return get().chat;
   }

   public static boolean hasMeta() {
      return !get().meta.isEmpty();
   }

   public static String getMetaInfo(String key) {
      String back = (String)get().meta.get(key);
      return key.equals("display-name") && back == null ? getInterpeter().getDisplayName() : back;
   }

   public static void main(String... args) throws IOException {
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      instance = this;
      this.chat = new ChatFacade();
      this.stats = new StatusUI();
      this.attemptForceLoad();
      AtomicBoolean terrain = new AtomicBoolean();
      Scheduler.getService().scheduleWithFixedDelay(() -> {
         if (Minecraft.getMinecraft().currentScreen instanceof GuiDownloadTerrain) {
            terrain.set(true);
         } else if (terrain.getAndSet(false)) {
            this.updateMeta();
         }

      }, 0L, 200L, TimeUnit.MILLISECONDS);
      Box<Consumer<CommandChannel.Output>> out = new Box();
      out.value = (output) -> {
         target(output.get("target"));
         CommandChannels.TARGETED.execute((String)null, (Consumer)out.value);
      };
      CommandChannels.TARGETED.execute((String)null, (Consumer)out.value);
      Field f = Util.getField(Options.class, Versionable.CHAT_MAX_WIDTH.get());

      try {
         if (f != null) {
            f.set(Options.CHAT_WIDTH, 2.0F);
         } else {
            Util.out("Null chat width field, chat width will not be customizable");
         }
      } catch (IllegalAccessException var7) {
         Util.error(var7, "Error reflecting chat width maximum");
      }

      try {
         Logging.setNicerFormat();
         Logging.info("test");
         Handler h = new FileHandler("hypixel-lynx" + File.separator + "debug.log");
         h.setFormatter(new SimpleFormatter());
         Debugger.DebugUtil.getOpts().getLogger().addHandler(h);
      } catch (IOException var6) {
         Util.out("Error hooking debug logging to file");
         var6.printStackTrace(System.err);
         System.err.flush();
      }

      new TempChatInterceptor();
      Util.register(this, MinecraftForge.EVENT_BUS);
      Util.register(this, FMLCommonHandler.instance().bus());

      this.reachNotifier = new ReachNotifier();

      new Thread(() -> {
         try {
            PrintStream ps = new PrintStream(new FileOutputStream(new File("hypixel-lynx", "saving.log")));
            System.setOut(ps);
            System.setErr(ps);
            System.out.println("set out to file");
         } catch (IOException var3) {
            var3.printStackTrace();
         }

         System.out.println("Saving chat tab configuration...");
         System.out.println("in-client tabs: " + this.chat.getTabs());
         System.out.println("in-config tabs: " + ClientConfig.TABS.as(List.class, Tab.class));
         System.out.println("setting...");
         ClientConfig.TABS.set(this.chat.getTabs());
         System.out.println("set");

         try {
            System.out.println("Saving...");
            ClientConfig.TABS.save();
            KeyBindings.ACTION_AURABOT.save();
            System.out.println("Saved.");
         } catch (IOException var2) {
            System.err.println("Error saving file");
            var2.printStackTrace(System.err);
         }

         System.out.flush();
         System.err.flush();
      });
   }

   @SubscribeEvent
   public void onRender(Pre event) {
      if (event.type == ElementType.CROSSHAIRS && this.mode != GameMode.GAME) {
         if (this.currentTarget != null) {
            this.currentTarget.drawScreen(Mouse.getX(), Mouse.getY(), (float)Minecraft.getMinecraft().ingameGUI.getUpdateCounter() + event.partialTicks);
         }

         this.stats.draw();
      }

   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL,
      receiveCanceled = true
   )
   public void onJoin(ClientConnectedToServerEvent event) {
      Util.out("#onJoin fired: " + event.getResult());
      this.subjugateChat();
   }

   public void cycleMode(boolean backward) {
      this.mode = backward ? this.mode.prev() : this.mode.next();
   }

   private void updateMeta() {
      Util.out("#updateMeta fired");
      /*
      CommandChannels.USERINFO.execute(Minecraft.getMinecraft().getSession().getUsername(), (output) -> {
         this.meta.putAll(output.getOneToOneMap());
      });
       */

      this.meta.put("display-name", Minecraft.getMinecraft().getSession().getUsername());
      this.meta.put("recent-name", Minecraft.getMinecraft().getSession().getUsername());
      this.meta.put("uuid", String.valueOf(Minecraft.getMinecraft().thePlayer.getUniqueID()));
      this.meta.put("rank", "ADMIN");
      this.meta.put("network-level", "0");
      this.meta.put("network-exp", "0");
      this.meta.put("current-server", Minecraft.getMinecraft().getCurrentServerData().serverIP);
   }

   private void subjugateChat() {
      GuiIngame gui = Minecraft.getMinecraft().ingameGUI;

      try {
         if (gui == null) {
            Util.out("No GUI yet...");
            return;
         }

         CHAT_GUI_FIELD.set(gui, this.chat);
      } catch (IllegalAccessException var3) {
         Util.error(var3, "Error intercepting GuiNewChat");
      }

   }

   public void attemptForceLoad() {
      ClassLoader cl = this.getClass().getClassLoader();
      Method m = Util.getMethod(cl.getClass(), "getClassBytes", String.class);
      Arrays.asList("net.hypixel.lynx.moderate.Option", "net.hypixel.lynx.moderate.Bans", "net.hypixel.lynx.moderate.PageAction", "net.hypixel.lynx.ui.UIS", "net.hypixel.lynx.moderate.GuideSelect", "net.hypixel.lynx.moderate.Bans$BanLengths", "net.hypixel.lynx.moderate.Mutes$MuteLengths", "com.codelanx.commons.config.RelativePath", "net.hypixel.lynx.util.Sounds").stream().forEach((s) -> {
         try {
            Util.out("#getClassBytes for '%s': %s", s, Arrays.toString((byte[])((byte[])m.invoke(cl, s))));
         } catch (InvocationTargetException | IllegalAccessException var4) {
            var4.printStackTrace();
         }

      });
   }

   static {
      CHAT_GUI_FIELD = Util.getField(GuiIngame.class, Versionable.PERSISTANT_CHAT_GUI_FIELD);
   }
}
