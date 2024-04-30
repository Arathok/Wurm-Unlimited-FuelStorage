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

import java.sql.Ref;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;

public class FuelStorageOpenPerformer implements ActionPerformer {
    public ActionEntry actionEntry;


    public FuelStorageOpenPerformer() {
        actionEntry = new ActionEntryBuilder((short) ModActions.getNextActionId(), "Open feeder", "opening",
                new int[]{
                        6 /* ACTION_TYPE_NOMOVE */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */,
                        35 /* DONT CARE WHETHER SOURCE OR TARGET */,

                }).range(4).build();

        ModActions.registerAction(actionEntry);

    }

    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    } // NEEDED OR THE ITEM WILL ONLY ACTIVATE IF YOU HAVE NO ITEM ACTIVE

    @Override
    public short getActionId() {
        return actionEntry.getNumber();
    }

    public static boolean canUse(Creature performer, Item target) {
        return performer.isPlayer() && !target.isTraded();
    }

    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        try {
            if (!canUse(performer, target)) {
                performer.getCommunicator().sendAlertServerMessage("You are not allowed to do that");
                return propagate(action,
                        ActionPropagation.FINISH_ACTION,
                        ActionPropagation.NO_SERVER_PROPAGATION,
                        ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);

            }
            FuelStorageObject aFuelStorage = new FuelStorageObject();
            boolean fuelStorageFound=false;
            int index=-1;
            Iterator<FuelStorageObject> fuelStorageObjectIterator = RefillHandler.fuelStorages.iterator();
            while (fuelStorageObjectIterator.hasNext()) {

                aFuelStorage = fuelStorageObjectIterator.next();
                index = RefillHandler.fuelStorages.indexOf(aFuelStorage);
                if (aFuelStorage.itemId == target.getWurmId()) {
                    fuelStorageFound = true;
                    aFuelStorage.isActive = true;

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
                    VolaTile targetTile = Zones.getTileOrNull(target.getTilePos(),target.isOnSurface());
                    if (targetTile!=null)
                    {
                        targetTile.makeInvisible(target);
                        targetTile.makeVisible(target);
                    }

                    if (Config.verboseLogging)
                    FuelStorage.logger.log(Level.INFO, performer.getName() + " opened their fuel storages feeder at " + target.getTileX() + " " + target.getTileY() + ", thus adding it to the AutoRefuel list");
                    performer.getCommunicator().sendSafeServerMessage("You open the flap of your fuel storage and it will now keep your fire lit.");
                }
            }
            if(fuelStorageFound) {
                RefillHandler.fuelStorages.set(index, aFuelStorage);
                RefillHandler.updateStatus(FuelStorage.dbconn, aFuelStorage);
            }

            if (!fuelStorageFound)
            {
                aFuelStorage = new FuelStorageObject();
                aFuelStorage.itemId = target.getWurmId();
                aFuelStorage.isActive = true;
                aFuelStorage.targetTemp = 4000;
                RefillHandler.fuelStorages.add(aFuelStorage);

                RefillHandler.insert(FuelStorage.dbconn, aFuelStorage);
                if (Config.verboseLogging)
                FuelStorage.logger.log(Level.INFO, performer.getName() + " opened their fuel storages feeder at " + target.getTileX() + " " + target.getTileY() + ", thus adding it to the AutoRefuel list");
                performer.getCommunicator().sendSafeServerMessage("You open the feeder flap of the fuel storage. You notice the slider of the feeder is set up so, that it will refill the fire to keep a glowing bed of coals");
                target.setName(target.getTemplate().getName());
                target.setName(target.getName() + " ( - feeder open, glow. coals)");
                VolaTile targetTile = Zones.getTileOrNull(target.getTilePos(),target.isOnSurface());
                if (targetTile!=null)
                {
                    targetTile.makeInvisible(target);
                    targetTile.makeVisible(target);
                }

            }



        } catch (SQLException throwables) {
            FuelStorage.logger.severe("something went wrong with writing to the database!" + throwables);
            throwables.printStackTrace();
        }
        return propagate(action,
                ActionPropagation.FINISH_ACTION,
                ActionPropagation.NO_SERVER_PROPAGATION,
                ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
    }
}
