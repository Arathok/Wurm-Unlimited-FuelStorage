package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.creatures.Communicator;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FuelStorage
        implements WurmServerMod, Initable, PreInitable, Configurable, ItemTemplatesCreatedListener, ServerStartedListener, ServerPollListener, PlayerMessageListener{

        public static final Logger logger = Logger.getLogger("FuelStorage");


        @Override
        public void configure(Properties properties) {




        }


        @Override
        public void preInit() {

            ModActions.init();

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
        RefillHandler.PollFurnaces();
        RefillHandler.Refill();
        }





}
