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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;

public class FuelStorageClosePerformer implements ActionPerformer {
    public ActionEntry actionEntry;


    public FuelStorageClosePerformer() {
        actionEntry = new ActionEntryBuilder((short) ModActions.getNextActionId(), "Close feeder", "closing",
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


        if (!canUse(performer, target)) {
            performer.getCommunicator().sendAlertServerMessage("You are not allowed to do that");
            return propagate(action,
                    ActionPropagation.FINISH_ACTION,
                    ActionPropagation.NO_SERVER_PROPAGATION,
                    ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);

        }

        try {

            FuelStorageObject aFuelStorage = new FuelStorageObject();
            boolean fuelStorageFound = false;

            Iterator<FuelStorageObject> fuelStorageObjectIterator = RefillHandler.fuelStorages.iterator();
            while (fuelStorageObjectIterator.hasNext()) {

                aFuelStorage = fuelStorageObjectIterator.next();
                int index = RefillHandler.fuelStorages.indexOf(aFuelStorage);
                if (aFuelStorage.itemId == target.getWurmId()) {
                    fuelStorageFound = true;
                    aFuelStorage.isActive = false;



                    RefillHandler.fuelStorages.set(index, aFuelStorage);
                    RefillHandler.updateStatus(FuelStorage.dbconn, aFuelStorage);
                    if (Config.verboseLogging)
                    FuelStorage.logger.log(Level.INFO, performer.getName() + " closed their fuel storages feeder at " + target.getTileX() + " " + target.getTileY() + ", thus removing it from the AutoRefuel list");
                    performer.getCommunicator().sendSafeServerMessage("You close the feeder flap of the fuel storage. Its temperature setting will stay as is");
                    target.setName(target.getTemplate().getName());
                    target.setName(target.getName() + " (feeder closed)");

                    VolaTile targetTile= Zones.getTileOrNull(target.getTilePos(), target.isOnSurface());
                    if (targetTile!=null)

                    {
                        targetTile.makeInvisible(target);
                        targetTile.makeVisible(target);
                    }

                }
            }

            if (!fuelStorageFound) {
                if (Config.verboseLogging)
                FuelStorage.logger.log(Level.INFO, performer.getName() + " tried to close flap but it didn't exist on the list! " + target.getTileX() + " " + target.getTileY() + ", thus removing it from the AutoRefuel list");
                performer.getCommunicator().sendSafeServerMessage("You try to close the fuel storages flap but it appears to be already closed. Weird");
            }


        }
     catch(SQLException throwables){
            FuelStorage.logger.severe("something went wrong with writing to the database!" + throwables);
            throwables.printStackTrace();
        }

        return propagate(action,
                ActionPropagation.FINISH_ACTION,
                ActionPropagation.NO_SERVER_PROPAGATION,
                ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
    }

}
