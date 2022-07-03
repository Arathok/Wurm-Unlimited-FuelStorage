package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.sql.Ref;
import java.sql.SQLException;
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
        return performer.isPlayer()  && !target.isTraded() ;
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
        if(!RefillHandler.fuelStorages.contains(target.getWurmId())) {
            RefillHandler.fuelStorages.add(target.getWurmId());
            try {
                RefillHandler.insert(FuelStorage.dbconn,target.getWurmId());
            } catch (SQLException e) {
                FuelStorage.logger.severe("something went wrong with writing to the database!" + e);
                e.printStackTrace();

            }
            FuelStorage.logger.log(Level.INFO,performer.getName() + " opened their fuel storages feeder at "+ target.getTileX()+" "+ target.getTileY()+ ", thus adding it to the AutoRefuel list");
            performer.getCommunicator().sendSafeServerMessage("You open the feeder flap of the fuel storage");
            target.setName(target.getTemplate().getName());
            target.setName(target.getName()+" (feeder open)");
        }
        else
            performer.getCommunicator().sendSafeServerMessage("The Flap of the fuel storages feeder was already open. Weird.");

        return propagate(action,
                ActionPropagation.FINISH_ACTION,
                ActionPropagation.NO_SERVER_PROPAGATION,
                ActionPropagation.NO_ACTION_PERFORMER_PROPAGATION);
    }
}
