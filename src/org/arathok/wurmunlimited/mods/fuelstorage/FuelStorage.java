package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
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
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FuelStorage
        implements WurmServerMod, Initable, PreInitable, Configurable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerMessageListener{

        public static final Logger logger = Logger.getLogger("FuelStorage");
        public static Connection dbconn;


        @Override
        public void configure(Properties properties) {
                Config.classhook=Boolean.parseBoolean(properties.getProperty("classhook","false"));
              Config.refuelForges=Boolean.parseBoolean(properties.getProperty("refuelForges","true"));
              Config.refuelOvens=Boolean.parseBoolean(properties.getProperty("refuelOvens","true"));
              Config.refuelSmelters=Boolean.parseBoolean(properties.getProperty("refuelSmelters","true"));
              Config.refuelKilns=Boolean.parseBoolean(properties.getProperty("refuelKilns","true"));
              Config.refuelStills=Boolean.parseBoolean(properties.getProperty("refuelStills","true"));
        }

        @Override
        public void preInit() {

            ModActions.init();

        }

        @Override
        public boolean onPlayerMessage(Communicator communicator, String message) {
                if (message != null&&message.startsWith("#FuelStorageVersion"))
                {

                        communicator.sendSafeServerMessage("You are on FuelStorage Version 3.0 ");

                }

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
                try {
                        dbconn = ModSupportDb.getModSupportDb();

                        // check if the ModSupportDb table exists
                        // if not, create the table and update it with the server's last crop poll time
                        if (!ModSupportDb.hasTable(dbconn, "FuelStorage")) {
                                // table create
                                try (PreparedStatement ps = dbconn.prepareStatement("CREATE TABLE FuelStorage (itemId LONG PRIMARY KEY NOT NULL DEFAULT 0, targetTemp LONG NOT NULL DEFAULT 4000), isActive BOOLEAN NOT NULL DEFAULT false")) {
                                        ps.execute();

                                }

                        }
                        RefillHandler refillHandler = new RefillHandler();
                        RefillHandler.readFromSQL(dbconn, RefillHandler.fuelStorages);
                        ModActions.registerBehaviourProvider(new FuelStorageBehavior());

                } catch (SQLException e) {
                        logger.severe("something went wrong with the database!" + e);
                        e.printStackTrace();
                } catch (NoSuchItemException e) {
                        logger.severe("no item for that id!" + e);
                        e.printStackTrace();
                }
        }

                @Override
        public void init() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServerPoll() {
        if (!Config.classhook)
                {


                        try
                        {
                                RefillHandler.Refill();
                        }
                        catch (NoSuchItemException e)
                        {
                                e.printStackTrace();
                                logger.log(Level.SEVERE,"Fuel Storage somehow got deleted?",e);

                        } catch (SQLException e) {
                                e.printStackTrace();
                                logger.log(Level.SEVERE,"Something went wrong with the DB",e);
                        }

                }
        }





}
