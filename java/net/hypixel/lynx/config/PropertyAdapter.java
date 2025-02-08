package net.hypixel.lynx.config;

import com.codelanx.commons.util.Reflections;
import com.google.common.primitives.Primitives;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import net.hypixel.lynx.util.Util;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries.IArrayEntry;

public class PropertyAdapter extends Property {
   private final ForgeConfig conf;

   public PropertyAdapter(ForgeConfig conf) {
      super(getName(conf), String.valueOf(conf.get()), getType(conf));
      this.conf = conf;
   }

   public static String getName(ForgeConfig conf) {
      return conf instanceof Enum ? Reflections.properEnumName((Enum)conf) : conf.getPath();
   }

   public static Type getType(ForgeConfig val) {
      Class<?> cl = val.get().getClass();
      Type back = getType(cl, val.get());
      return back == null ? Type.STRING : back;
   }

   private static Type getType(Class<?> cl, Object val) {
      if (cl == String.class) {
         return Type.STRING;
      } else if (!(val instanceof Number) && !(val instanceof Boolean)) {
         if (cl.isAssignableFrom(List.class)) {
            List l = (List)val;
            if (!l.isEmpty()) {
               Object o = l.get(0);
               return getType(o.getClass(), o);
            }
         }

         return null;
      } else {
         if (Primitives.isWrapperType(cl)) {
            cl = Primitives.unwrap(cl);
         }

         if (cl != Float.TYPE && cl != Double.TYPE) {
            return cl == Boolean.TYPE ? Type.BOOLEAN : Type.INTEGER;
         } else {
            return Type.DOUBLE;
         }
      }
   }

   public void set(String value) {
      this.setConf(value);
   }

   public void set(int value) {
      this.setConf(value);
   }

   public void set(boolean value) {
      this.setConf(value);
   }

   public void set(double value) {
      this.setConf(value);
   }

   public void set(String[] values) {
      this.setConf(values);
   }

   public void set(boolean[] values) {
      this.setConf(values);
   }

   public void set(int[] values) {
      this.setConf(values);
   }

   public void set(double[] values) {
      this.setConf(values);
   }

   public boolean getBoolean() {
      return (Boolean)this.conf.as(Boolean.TYPE);
   }

   public boolean getBoolean(boolean def) {
      return this.conf.get() == null ? def : (Boolean)this.conf.as(Boolean.TYPE);
   }

   public boolean[] getBooleanList() {
      return super.getBooleanList();
   }

   public double getDouble() {
      return super.getDouble();
   }

   public double getDouble(double _default) {
      return super.getDouble(_default);
   }

   public Class<? extends IArrayEntry> getArrayEntryClass() {
      return super.getArrayEntryClass();
   }

   public Class<? extends IConfigEntry> getConfigEntryClass() {
      return super.getConfigEntryClass();
   }

   public double[] getDoubleList() {
      return super.getDoubleList();
   }

   public int getInt() {
      return super.getInt();
   }

   public int getInt(int _default) {
      return super.getInt(_default);
   }

   public int getMaxListLength() {
      return super.getMaxListLength();
   }

   public int[] getIntList() {
      return super.getIntList();
   }

   public Pattern getValidationPattern() {
      return super.getValidationPattern();
   }

   public String getDefault() {
      return String.valueOf(this.conf.getDefault());
   }

   public String getString() {
      return (String)this.conf.as(String.class);
   }

   public String[] getDefaults() {
      return super.getDefaults();
   }

   public String[] getStringList() {
      return super.getStringList();
   }

   public String[] getValidValues() {
      return super.getValidValues();
   }

   public Type getType() {
      return getType(this.conf);
   }

   public String getName() {
      return getName(this.conf);
   }

   public void setConf(Object obj) {
      this.conf.set(obj);

      try {
         this.conf.save();
      } catch (IOException var3) {
         Util.error(var3, "Error saving " + this.getName());
      }

   }
}
