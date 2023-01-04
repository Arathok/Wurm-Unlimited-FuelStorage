package org.arathok.wurmunlimited.mods.fuelstorage;


import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FuelStorage
        implements WurmServerMod, Initable, PreInitable, Configurable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerMessageListener{

        public static final Logger logger = Logger.getLogger("FuelStorage");
        public static Connection dbconn;
        public static boolean finishedReadingDB =false;


        @Override
        public void configure(Properties properties) {
              Config.classhook=Boolean.parseBoolean(properties.getProperty("classhook","false"));
              Config.refuelForges=Boolean.parseBoolean(properties.getProperty("refuelForges","true"));
              Config.refuelOvens=Boolean.parseBoolean(properties.getProperty("refuelOvens","true"));
              Config.refuelSmelters=Boolean.parseBoolean(properties.getProperty("refuelSmelters","true"));
              Config.refuelKilns=Boolean.parseBoolean(properties.getProperty("refuelKilns","true"));
              Config.refuelStills=Boolean.parseBoolean(properties.getProperty("refuelStills","true"));
              Config.verboseLogging=Boolean.parseBoolean(properties.getProperty("verboseLogging","true"));
              Config.minimumSkill=Float.parseFloat(properties.getProperty("minimumSkill","25.0"));
        }

        @Override
        public void preInit() {

            ModActions.init();

        }

        @Override
        public boolean onPlayerMessage(Communicator communicator, String message) {
                if (message != null&&message.startsWith("#FuelStorageVersion"))
                {

                        communicator.sendSafeServerMessage("You are on FuelStorage Version 3.2 ");

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
                        if (!ModSupportDb.hasTable(dbconn, "FuelStorageV2")) {
                                // table create
                                try{ PreparedStatement ps = dbconn.prepareStatement("CREATE TABLE FuelStorageV2 (itemId LONG PRIMARY KEY NOT NULL DEFAULT 0, targetTemp LONG NOT NULL DEFAULT 4000, isActive BOOLEAN NOT NULL DEFAULT false)");
                                        ps.execute();

                                } catch (SQLException e) {
                                        e.printStackTrace();
                                }

                        }
                        if (ModSupportDb.hasTable(dbconn, "FuelStorage")) {
                                // table create
                                try{ PreparedStatement ps = dbconn.prepareStatement("DROP TABLE FuelStorage");
                                        ps.execute();

                                } catch (SQLException e) {
                                        e.printStackTrace();
                                }

                        }

                       
                        RefillHandler refillHandler = new RefillHandler();
                        ModActions.registerBehaviourProvider(new FuelStorageBehavior());
                        ModActions.registerBehaviourProvider(new FuelStorageFeederBehavior());

                } catch (SQLException e) {
                        logger.severe("something went wrong with the database!" + e);
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
                                if (!finishedReadingDB)
                                RefillHandler.readFromSQL(dbconn, RefillHandler.fuelStorages);
                                RefillHandler.Refill();
                        } catch (SQLException e) {
                                e.printStackTrace();
                                logger.log(Level.SEVERE,"Something went wrong with the DB",e);
                        } catch (NoSuchItemException e) {
                                e.printStackTrace();
                        }

                }
        }





}
