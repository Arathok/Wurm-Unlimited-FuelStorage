package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FuelStorage
        implements WurmServerMod, Initable, PreInitable, Configurable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerMessageListener{

        public static final Logger logger = Logger.getLogger("FuelStorage");


        @Override
        public void configure(Properties properties) {
                Config.classhook=Boolean.parseBoolean(properties.getProperty("classhook","false"));

        }

        @Override
        public void preInit() {

            ModActions.init();
            if (Config.classhook)
                try {
                        ClassPool classPool = HookManager.getInstance().getClassPool();
                        CtClass ctItems = classPool.get("com.wurmonline.server.Items.Item");

                        ctItems.getMethod("coolOutsideItem", "(Lcom/wurmonline/server/items/Item;I)V")
                                .insertAt(9803,"org.arathok.wurmunlimited.mods.fuelstorage.RefillHandler.Refill(this,$1);");
                        /*
                        private void coolOutSideItem(boolean everySecond, boolean insideStructure) {
    if (this.temperature > 200) {
      float speed = 1.0F;
      if (insideStructure) {
        speed *= 0.75F;
      } else if (Server.getWeather().getRain() > 0.2D) {
        speed *= 2.0F;
      }
      if (getRarity() > 0)
        speed = (float)(speed * Math.pow(0.8999999761581421D, getRarity()));
      int templateId = this.template.getTemplateId();
      if (getSpellEffects() != null)
        if (templateId == 180 || templateId == 1023 || templateId == 1028 || templateId == 1178 || templateId == 37 || templateId == 178)
          if (Server.rand.nextFloat() < getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FUELUSE) - 1.0F)
            speed = 0.0F;
      if (getTemplateId() == 180 || getTemplateId() == 178 ||
        getTemplateId() == 1023 || getTemplateId() == 1028) {
        if (System.currentTimeMillis() - 60000L > this.lastAuxPoll)
          if (getTemperature() > 200 && getAuxData() < 30) {
            setAuxData((byte)(getAuxData() + 1));
            this.lastAuxPoll = System.currentTimeMillis();
          }
        if (getAuxData() > 30)
          setAuxData((byte)30);
      }
      if (templateId == 180 || templateId == 1023 || templateId == 1028) {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed *
              Math.max(1.0F, 11.0F - Math.max(1.0F, 20.0F * Math.max(30.0F, getCurrentQualityLevel()) / 200.0F))));
      } else if (templateId == 1178) {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 0.5F *
              Math.max(1.0F, 11.0F - Math.max(1.0F, 10.0F * Math.max(30.0F, getCurrentQualityLevel()) / 200.0F))));
      } else if (templateId == 37 || templateId == 178) {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed *
              Math.max(1.0F, 11.0F - Math.max(1.0F, 10.0F * Math.max(30.0F, getCurrentQualityLevel()) / 200.0F))));
        if (templateId == 37)
          if (this.temperature <= 210) {
            if (getItems().isEmpty()) {
              float ql = getCurrentQualityLevel();
              try {
                ItemFactory.createItem(141, ql, getPosX(), getPosY(), getRotation(), isOnSurface(),
                    getRarity(), getBridgeId(), null);
              } catch (NoSuchTemplateException nst) {
                logWarn("No template for ash?" + nst.getMessage(), (Throwable)nst);
              } catch (FailedException fe) {
                logWarn("What's this: " + fe.getMessage(), (Throwable)fe);
              }
            }
            setQualityLevel(0.0F);
            deleteFireEffect();
          }
      } else if ((isLight() && !isLightOverride()) || isFireplace() ||
        getTemplateId() == 1243 || getTemplateId() == 1301) {
        pollLightSource();
      } else if (everySecond) {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 20.0F));
      } else {
        setTemperature((short)(int)Math.max(200.0F, this.temperature - speed * 800.0F * 5.0F));
      }
    }
    if (!isOnFire())
      if (isStreetLamp() || isBrazier() || isFireplace() || getTemplateId() == 1301) {
        checkIfLightStreetLamp();
      } else {
        deleteFireEffect();
      }
  }
                         */

                } catch (Exception e) {
                        logger.log(Level.SEVERE,"something went wrong while hooking into the target class",e);
                        throw new HookException(e);
                }
        }

        @Override
        public boolean onPlayerMessage(Communicator arg0, String arg1) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onItemTemplatesCreated() {
                try
                {
                        FuelStorageItems.registerFuelStorage();
                }
                catch (IOException e)
                {
                        e.printStackTrace();
                        logger.log(Level.SEVERE,"Something went wrong creating the template",e);
                }
                // TODO Auto-generated method stub

        }

        @Override
        public void onServerStarted() {
            // TODO Auto-generated method stub


        }



        @Override
        public void init() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServerPoll() {
        if (!Config.classhook)
                {
                RefillHandler.PollFurnaces();
                RefillHandler.Refill();
                }
        }





}
