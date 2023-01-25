package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;

public class FuelStorageFeederPerformer implements ActionPerformer {
    public ActionEntry actionEntry;
    int targetTemp;



    public FuelStorageFeederPerformer(String name,int targetTemp) {
        actionEntry = new ActionEntryBuilder((short) ModActions.getNextActionId(), name, "setting temperature",
                new int[]{
                        6 /* ACTION_TYPE_NOMOVE */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                        35 /* DONT CARE WHETHER SOURCE OR TARGET */,

                }).range(4).build();
        this.targetTemp=targetTemp;
        ModActions.registerAction(actionEntry);
        ModActions.registerActionPerformer(this);




    }


    @Override
    public short getActionId()
    {
        return (actionEntry.getNumber());
    }

    public static boolean canUse(Creature performer, Item target)
    {
        return performer.isPlayer() && target.getLastOwnerId() == performer.getWurmId() && !target.isTraded();
    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter)
    {
        return action(action, performer, target, num, counter);
    } // NEEDED OR THE ITEM WILL ONLY ACTIVATE IF YOU HAVE NO ITEM ACTIVE


    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter)
    {

        //Alchemy.logger.log(Level.INFO, "BLAH BLAH HE PERFORMS");


        if (!canUse(performer, target))
        {
            performer.getCommunicator().sendAlertServerMessage("You are not allowed to do that");
            return propagate(action,
                    ActionPropagation.FINISH_ACTION,
                    ActionPropagation.NO_SERVER_PROPAGATION,
                    ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);

        }
        else
        {
            try {

                FuelStorageObject aFuelStorage = new FuelStorageObject();
                boolean fuelStorageFound = false;

                Iterator<FuelStorageObject> fuelStorageObjectIterator = RefillHandler.fuelStorages.iterator();
                while (fuelStorageObjectIterator.hasNext()) {

                    aFuelStorage = fuelStorageObjectIterator.next();
                    int index = RefillHandler.fuelStorages.indexOf(aFuelStorage);
                    if (aFuelStorage.itemId == target.getWurmId()) {
                        fuelStorageFound = true;
                        aFuelStorage.targetTemp = targetTemp;


                        RefillHandler.fuelStorages.set(index, aFuelStorage);
                        RefillHandler.updateTemp(FuelStorage.dbconn, aFuelStorage);
                        performer.getCommunicator().sendSafeServerMessage("You change the width Slider on your fuel storages feeder flap, thus allowing it to keep the fire at the new Temperature.");

                        if (aFuelStorage.isActive) {

                            switch ((int) aFuelStorage.targetTemp) {
                                case 15000: {
                                    target.setName(target.getTemplate().getName() + (" - feeder open, full blaze"),true);

                                    break;
                                }

                                case 9000: {
                                    target.setName(target.getTemplate().getName() + (" - feeder open, wild flames"),true);

                                    break;
                                }

                                case 7000: {
                                    target.setName(target.getTemplate().getName() + (" - feeder open, small flames"),true);

                                    break;
                                }

                                case 5000: {
                                    target.setName(target.getTemplate().getName() + (" - feeder open, few flames"),true);

                                    break;
                                }

                                case 4000: {
                                    target.setName(target.getTemplate().getName() + (" - feeder open, glow. coals"),true);

                                    break;
                                }
                            }
                            if (Config.verboseLogging)
                            FuelStorage.logger.log(Level.INFO, performer.getName() + " changed the Temperature Setting on " + target.getTileX() + " " + target.getTileY() + ", is now " + targetTemp + " .");


                        }

                    }
                    VolaTile targetTile= Zones.getTileOrNull(target.getTilePos(),target.isOnSurface());
                    if (targetTile!=null)
                    {
                        targetTile.makeInvisible(target);
                        targetTile.makeVisible(target);
                    }



                }

                if (!fuelStorageFound) {
                    if (Config.verboseLogging)
                    FuelStorage.logger.log(Level.INFO, performer.getName() + " tried to change fuel storage temperature but it didn't exist on the list! (was never used) " + target.getTileX() + " " + target.getTileY() + ", thus removing it from the AutoRefuel list");
                    performer.getCommunicator().sendSafeServerMessage("The width slider springs back to its default position and you realize to properly set it up first, you need to open its feeder at least once.");
                }


            }
            catch(SQLException throwables){
                FuelStorage.logger.severe("something went wrong with writing to the database!" + throwables);
                throwables.printStackTrace();
            }
        }
            return propagate(action,
                ActionPropagation.FINISH_ACTION,
                ActionPropagation.NO_SERVER_PROPAGATION,
                ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
    }
}
