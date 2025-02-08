package net.hypixel.lynx.moderate;

import com.codelanx.commons.util.Scheduler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.chat.channel.ChannelKeys;
import net.hypixel.lynx.chat.channel.CommandChannels;
import net.hypixel.lynx.ui.Regions;
import net.hypixel.lynx.ui.UIS;
import net.hypixel.lynx.ui.key.KeyBindings;
import net.hypixel.lynx.ui.key.Keys;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

public class LynxUI extends GuiScreen {
   private static final int TEXT_HORIZ_BUFFER = 3;
   private static final String TITLE = "\u00A7cTargeting";
   private static final String BRIDGE_ACTIVE = "\u00A7a>";
   private static final String BRIDGE_INACTIVE = "\u00A7c>";
   private static final int BRIDGE_WIDTH = 10;
   private static final int BRIDGE_BUFFER = 2;
   private static final int TITLE_WIDTH;
   private static final int LEFT = 10;
   private static final int TOP = 38;
   private static final int TEXT_VERT_BUFFER = 4;
   private static final int HEIGHT = 15;
   private static final int META_TOP = 68;
   private static final int TITLE_HEIGHT = 18;
   private static final int ITEMS_PER_PAGE = 10;
   private static final int TITLE_TOP = 20;
   private static final int[] NUMKEYS;
   private static final Map<String, String> META_KEYS;
   private static final int BANINFO_HEIGHT = 11;
   private final String username;
   private final Map<String, String> meta;
   private final Map<String, List<String>> banInfo;
   private String displayName;
   private List<Action> prevSelections;
   private Class<? extends Enum> currentOptionList;
   private Supplier<Action[]> getActions;
   private int page;

   public List<Action> getPrevSelections() {
      return this.prevSelections;
   }

   public Map<String, String> getMetaKeys() {
      return this.meta;
   }

   public LynxUI(String username) {
      this.meta = new HashMap();

      this.meta.put("current-server", Minecraft.getMinecraft().getCurrentServerData().serverIP);

      this.banInfo = new HashMap();
      this.prevSelections = new LinkedList();
      this.currentOptionList = Option.class;
      this.getActions = () -> {
         return (Action[])((Action[])this.currentOptionList.getEnumConstants());
      };
      this.page = 0;
      this.username = username;
      this.displayName = username;
      UIS.player = new UIS.RenderLynxPlayer(Minecraft.getMinecraft().getRenderManager());
      this.pollInfo();
      /*
      Scheduler.runAsyncTask(() -> {
         CommandChannels.VIEW_BANS_2.execute(this.username, (output) -> {
            Util.out("#output now");
            List<String> data = output.getList("data");
            Util.out("Data: %s", data);
            if (data != null && !data.isEmpty()) {
               try {
                  data.stream().map((s) -> {
                     Util.out("Mapping: %s", s);
                     return ChannelKeys.ViewBans.getInfo(s);
                  }).filter((s) -> {
                     Util.out("Is %s filtered?: %b", s.getReason(), s.isPunished());
                     return s.isPunished();
                  }).forEach((s) -> {
                     Util.out("Parsing: %s", s.getReason());
                     String key = s.isPermanent() ? "Ban" : "Tempban";
                     ((List)this.banInfo.computeIfAbsent(key, (k) -> {
                        return new LinkedList();
                     })).add(s.getReason());
                  });
               } catch (Exception var4) {
                  Util.error(var4, "Error parsing ban info");
               }
            } else {
               Util.out("Null or empty?");
            }

         });
      }, 1L);
       */
   }

   public LynxUI() {
      this((String)null);
      this.currentOptionList = GuideSelect.class;
   }

   private void safetyPoll() {
      if (Lynx.getTargetingUI() == this) {
         this.pollInfo();
      }

   }

   private void pollInfo() {
      /*
      CommandChannels.USERINFO.execute(this.username, (output) -> {
         if (this.username.equalsIgnoreCase(output.get("recent-name"))) {
            this.displayName = output.get("display-name");
            this.meta.putAll(output.getOneToOneMap());
            Scheduler.runAsyncTask(this::safetyPoll, 5L);
         }

      });
       */
   }

   public Action getPreviousSelection() {
      return this.prevSelections.isEmpty() ? null : (Action)this.prevSelections.get(this.prevSelections.size() - 1);
   }

   public void page(int amount) {
      int max = this.getActionValues().length / 8;
      this.page += amount;
      if (this.page < 0) {
         this.back();
         this.page = 0;
      }

      if (this.page > max) {
         this.page = max;
      }

   }

   void back() {
      if (this.prevSelections.isEmpty()) {
         Lynx.untarget();
      } else {
         this.setOptionList((Class)((Action)this.prevSelections.get(this.prevSelections.size() - 1)).getClass());
         this.prevSelections.remove(this.prevSelections.size() - 1);
      }
   }

   public String getTargetName() {
      return this.username;
   }

   public EntityPlayer getTarget() {
      return Minecraft.getMinecraft().theWorld.getPlayerEntityByName(this.username);
   }

   public void root() {
      this.prevSelections.clear();
      this.currentOptionList = Option.class;
   }

   public void typed() {
      Action[] a = this.getCurrentActions();
      if (this.isPageable()) {
         if (KeyBindings.ACTION_CANCEL.isApplicable()) {
            this.back();
            return;
         }

         int i;
         if (this.getActionValues().length <= 10) {
            for(i = 0; i < a.length && i < 10; ++i) {
               if (Keys.isActivated(NUMKEYS[i])) {
                  a[i].onAction(this, this.username);
                  break;
               }
            }
         } else {
            for(i = 0; i < a.length && i < 10; ++i) {
               if (Keys.isActivated(NUMKEYS[i])) {
                  a[i].onAction(this, this.username);
                  break;
               }
            }

            if (PageAction.PREVIOUS_PAGE.getKeyBinding().isApplicable() && this.page > 0) {
               PageAction.PREVIOUS_PAGE.onAction(this, this.username);
            }

            if (PageAction.NEXT_PAGE.getKeyBinding().isApplicable() && (this.getActionValues().length - 1) / 10 > this.page) {
               PageAction.NEXT_PAGE.onAction(this, this.username);
            }
         }
      } else {
         if (KeyBindings.ACTION_CANCEL.isApplicable()) {
            this.back();
            return;
         }

         Action ac = (Action)Arrays.stream(a).filter((e) -> {
            return !e.getKeyBinding().isEmpty();
         }).filter((e) -> {
            return e.getKeyBinding().isApplicable();
         }).findFirst().orElse(null);
         if (ac != null) {
            ac.onAction(this, this.username);
         }
      }

   }

   public void drawScreen(int x, int y, float partialTicks) {
      GlStateManager.pushMatrix();
      GlStateManager.enableAlpha();
      GlStateManager.enableBlend();
      this.drawPlate(Float.valueOf(partialTicks).intValue());
      GlStateManager.popMatrix();
   }

   private void drawPlate(int tick) {
      int nameWidth = Regions.getStringWidth(this.displayName);
      int width = Math.max(nameWidth, TITLE_WIDTH);
      width = this.drawMeta(width);
      width += 6;
      int displayLeft = (width - nameWidth) / 2;
      int titleLeft = (width - TITLE_WIDTH) / 2;
      Gui.drawRect(10, 20, 10 + width, 38, Regions.colorFromRGB(0, 0, 0, 204));
      UIS.drawText("\u00A7cTargeting", 10 + titleLeft, 24);
      Gui.drawRect(10, 38, 10 + width, 53, Regions.colorFromRGB(0, 0, 0, 166));
      UIS.drawText(this.displayName, 10 + displayLeft, 42);
      AtomicInteger left = new AtomicInteger(10 + width);
      int color = Regions.colorFromRGB(0, 0, 0, 127);
      this.prevSelections.stream().forEach((a) -> {
         this.bridge(left.get(), false);
         int w = Regions.getStringWidth(a.properName()) + 6;
         Gui.drawRect(left.get() + 10, 38, left.get() + 10 + w, 53, color);
         UIS.drawText(a.properName(), left.get() + 10 + 3, 42);
         left.set(left.get() + 10 + w);
      });
      this.bridge(left.get(), true);
      this.drawOptionList(left.get() + 10);
      this.drawBanInfo();
   }

   private void drawBanInfo() {
      List<String> bans = (List)this.banInfo.getOrDefault("Ban", Collections.emptyList());
      List<String> tempbans = (List)this.banInfo.getOrDefault("Tempban", Collections.emptyList());
      if (!bans.isEmpty() || !tempbans.isEmpty()) {
         AtomicInteger width = new AtomicInteger();
         Stream.of(bans, tempbans).flatMap(Collection::stream).forEach((s) -> {
            width.set(Math.max(width.get(), Regions.getStringWidth("- " + s)));
         });
         ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
         int right = res.getScaledWidth() - 10;
         int top = 10;
         int left = right - width.get() - 2;
         int bottom = bans.size() * 11 + tempbans.size() * 11 + 22;
         Gui.drawRect(left, top, right, bottom, Regions.colorFromRGB(0, 0, 0, 127));
         AtomicInteger index = new AtomicInteger(0);
         Consumer<String> draw = (s) -> {
            UIS.drawText(s, left + 1, top + 11 * index.get() + 1);
            index.set(index.get() + 1);
         };
         if (!bans.isEmpty()) {
            draw.accept("\u00A7cBans\u00A7f:");
         }

         bans.stream().map((s) -> {
            return "- " + s;
         }).forEach(draw);
         if (!tempbans.isEmpty()) {
            draw.accept("\u00A7eTempbans\u00A7f:");
         }

         tempbans.stream().map((s) -> {
            return "- " + s;
         }).forEach(draw);
      }
   }

   public String getBans() {
      return (String)this.meta.getOrDefault("bans", "0");
   }

   public String getMutes() {
      return (String)this.meta.getOrDefault("mutes", "0");
   }

   private int drawMeta(int preWidth) {
      if (this.meta.isEmpty()) {
         return preWidth;
      } else {
         AtomicInteger width = new AtomicInteger(preWidth);
         AtomicInteger done = new AtomicInteger(0);
         int color = Regions.colorFromRGB(0, 0, 0, 127);
         List<String> write = (List)META_KEYS.keySet().stream().map((s) -> {
            s = (String)META_KEYS.get(s) + ": " + (String)this.meta.get(s);
            width.set(Math.max(width.get(), Regions.getStringWidth(s)));
            return s;
         }).collect(Collectors.toList());
         done.set(0);
         boolean playerShown = UIS.drawPlayerTarget(10 + width.get() / 2 + 3, 166);
         if (playerShown) {
            Gui.drawRect(10, 61, 10 + width.get() + 6, 180, Regions.colorFromRGB(0, 0, 0, 127));
         }

         write.forEach((s) -> {
            int top = 68 + 15 * done.get() + (playerShown ? 120 : 0);
            Gui.drawRect(10, top, 10 + width.get() + 6, top + 15, color);
            UIS.drawText(s, 13, top + 4);
            done.set(done.get() + 1);
         });
         return width.get();
      }
   }

   private void bridge(int left, boolean active) {
      UIS.drawText(active ? "\u00A7a>" : "\u00A7c>", left + 2, 42);
   }

   private Action[] getCurrentActions() {
      Action[] vals = this.getActionValues();
      Action[] action;
      if (this.isPageable()) {
         if (vals.length <= 10) {
            action = new Action[vals.length + 1];
            System.arraycopy(vals, 0, action, 0, vals.length);
            action[action.length - 1] = PageAction.CANCEL;
         } else {
            boolean previous = this.page != 0;
            boolean next = (this.getActionValues().length - 1) / 10 > this.page;
            int min = Math.max(this.page * 10, 0);
            int max = Math.min(min + 10, vals.length);
            int length = max - min;
            int amount = 1 + (previous ? 1 : 0) + (next ? 1 : 0);
            action = new Action[length + amount];
            if (previous) {
               action[action.length - amount] = PageAction.PREVIOUS_PAGE;
            }

            if (next) {
               action[action.length - amount + (previous ? 1 : 0)] = PageAction.NEXT_PAGE;
            }

            action[action.length - 1] = PageAction.CANCEL;
            System.arraycopy(vals, min, action, 0, length);
         }
      } else {
         action = new Action[vals.length + 1];
         System.arraycopy(vals, 0, action, 0, vals.length);
         action[action.length - 1] = PageAction.CANCEL;
      }

      return action;
   }

   private Action[] getActionValues() {
      return this.getActions == null ? (Action[])((Action[])this.currentOptionList.getEnumConstants()) : (Action[])this.getActions.get();
   }

   private int getPagingCount(int elementCount) {
      if (elementCount < 9) {
         return 0;
      } else if (elementCount < 17) {
         return 1;
      } else {
         return this.page != 0 && this.page != 1 + (elementCount - 8) / 7 ? 2 : 1;
      }
   }

   private boolean hasNextPageElem(int elementCount) {
      if (elementCount < 10) {
         return false;
      } else {
         return this.page != 1 + (elementCount - 8) / 7;
      }
   }

   private boolean hasPrevPageElem(int elementCount) {
      return this.page != 0;
   }

   private boolean isPageable() {
      return Arrays.stream(this.getActionValues()).anyMatch(Pageable::isPageable);
   }

   private void drawOptionList(int left) {
      Action[] action = this.getCurrentActions();
      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
      AtomicInteger top = new AtomicInteger(38);
      int color = Regions.colorFromRGB(0, 0, 0, 127);
      AtomicInteger prepref = new AtomicInteger(1);
      Stream var10000 = Arrays.stream(action).map((a) -> {
         return (this.isPageable() && prepref.get() >= action.length - 3 ? prepref.getAndSet(prepref.get() + 1) : a.getKeyShortcut()) + ": " + a.properName();
      });

      int width = 0;
      for (Object obj : (List)var10000.collect(Collectors.toList())) {
         if (obj instanceof String) {
            width += (fr.getStringWidth((String) obj) + 6);
         }
         if (width > 50) {
            width = 50;
         }
      }

      AtomicInteger pref = new AtomicInteger(0);
      int finalWidth = width;
      Arrays.stream(action).forEach((act) -> {
         Gui.drawRect(left, top.get(), left + finalWidth, top.get() + 15, color);
         String pr = act.getKeyShortcut();
         if (act.getKeyBinding().isEmpty() && this.isPageable()) {
            pr = "" + Keyboard.getKeyName(NUMKEYS[pref.get()]);
         }

         UIS.drawText(pr + ": " + act.properName(), left + 3, top.get() + 4);
         top.set(top.get() + 15);
         pref.getAndSet(pref.get() + 1);
      });
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
   }

   public Action popPrevious() {
      return (Action)this.prevSelections.remove(this.prevSelections.size() - 1);
   }

   public void addPrevious(Action action) {
      this.prevSelections.add(action);
   }

   public <T extends Enum, Action> void setOptionList(Class<T> optionList) {
      this.page = 0;
      this.currentOptionList = optionList;
      this.getActions = null;
   }

   public void setOptionList(Supplier<Action[]> optionList) {
      this.page = 0;
      this.getActions = optionList;
   }

   static {
      TITLE_WIDTH = Minecraft.getMinecraft().fontRendererObj.getStringWidth("\u00A7cTargeting");
      NUMKEYS = new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
      META_KEYS = new LinkedHashMap<String, String>() {
         {
            this.put("bans", "Bans");
            this.put("mutes", "Mutes");
            this.put("kicks", "Kicks");
            this.put("network-level", "Net Level");
            this.put("current-server", "Server");
         }
      };
   }
}
