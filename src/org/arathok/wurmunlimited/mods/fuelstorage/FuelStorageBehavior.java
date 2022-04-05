package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FuelStorageBehavior implements BehaviourProvider {

    private final List<ActionEntry> openFeeder;
    private final List<ActionEntry> closeFeeder;
    private final FuelStorageOpenPerformer openPerformer;
    private final FuelStorageClosePerformer closePerformer;

    public FuelStorageBehavior() {
        this.openPerformer = new FuelStorageOpenPerformer();
        this.openFeeder = Collections.singletonList(openPerformer.actionEntry);
        this.closePerformer = new FuelStorageClosePerformer();
        this.closeFeeder = Collections.singletonList(closePerformer.actionEntry);
        ModActions.registerActionPerformer(openPerformer);
        ModActions.registerActionPerformer(closePerformer);

    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {

        if (target.getTemplateId() == FuelStorageItems.fuelStorageId) {
            if (FuelStorageOpenPerformer.canUse(performer, target)&&!RefillHandler.fuelStorages.contains(target.getWurmId()))
                return new ArrayList<>(openFeeder);

        } else if (target.getTemplateId() == FuelStorageItems.fuelStorageId&&RefillHandler.fuelStorages.contains(target.getWurmId())) {
            if (FuelStorageClosePerformer.canUse(performer, target))
                return new ArrayList<>(closeFeeder);



        }else
            return null;



        return null;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        return getBehavioursFor(performer, target);
    }
}
