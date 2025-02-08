package net.hypixel.lynx.ui;

import net.hypixel.lynx.Lynx;
import net.hypixel.lynx.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class UIS {
   private static RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
   public static final int FULL_COLOR = Regions.colorFromRGB(255, 255, 255, 255);
   public static UIS.RenderLynxPlayer player = null;

   private UIS() {
   }

   public static void drawText(String option, int left, int top) {
      drawText(option, left, top, FULL_COLOR);
   }

   public static void drawText(String option, int left, int top, int color) {
      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
      fr.drawStringWithShadow(option, (float)left, (float)top, color);
   }

   private static EntityPlayer getPlayerEntityByName(String name) {
      World w = Minecraft.getMinecraft().theWorld;
      return (EntityPlayer)w.playerEntities.stream().filter((p) -> {
         return p.getName().equalsIgnoreCase(name);
      }).findFirst().orElse(null);
   }

   public static boolean drawPlayerTarget(int posX, int posY) {
      int scale = 45;
      EntityPlayer ent = getPlayerEntityByName(Lynx.getTargetingUI().getTargetName());
      if (ent == null) {
         return false;
      } else {
         GlStateManager.enableColorMaterial();
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)posX, (float)posY, -1.0F);
         GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
         GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
         RenderHelper.enableGUIStandardItemLighting();
         GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
         ent.rotationYawHead = ent.rotationYaw;
         ent.prevRotationYawHead = ent.rotationYaw;
         GlStateManager.translate(0.0F, 0.0F, 0.0F);
         rendermanager.setPlayerViewY(180.0F);
         renderLynxEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
         GlStateManager.popMatrix();
         RenderHelper.disableStandardItemLighting();
         GlStateManager.disableRescaleNormal();
         GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
         GlStateManager.disableTexture2D();
         GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
         return true;
      }
   }

   private static void renderLynxEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {
      try {
         if (rendermanager.renderEngine != null) {
            try {
               if (player != null) {
                  player.doRender((AbstractClientPlayer)entity, x, y, z, entityYaw, partialTicks);
               }
            } catch (Throwable var11) {
               Util.error(var11, "Error rendering player preview");
            }

            try {
               if (player != null && !player.isRenderOutLines()) {
                  player.doRenderShadowAndFire(entity, x, y, z, entityYaw, partialTicks);
               }
            } catch (Throwable var10) {
               Util.error(var10, "Error post-rendering player preview");
            }
         }
      } catch (Throwable var12) {
         Util.error(var12, "Error during rendering player preview");
      }

   }

   public static class RenderLynxPlayer extends RenderPlayer {
      public RenderLynxPlayer(RenderManager renderManager) {
         super(renderManager);
      }

      protected boolean canRenderName(AbstractClientPlayer entity) {
         return false;
      }

      protected void renderOffsetLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
      }

      public void renderName(AbstractClientPlayer entity, double x, double y, double z) {
      }

      protected void renderLivingLabel(AbstractClientPlayer entityIn, String str, double x, double y, double z, int maxDistance) {
      }

      public boolean isRenderOutLines() {
         return this.renderOutlines;
      }
   }
}
