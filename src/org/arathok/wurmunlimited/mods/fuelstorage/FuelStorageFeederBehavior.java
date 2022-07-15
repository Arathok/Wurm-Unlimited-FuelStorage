package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FuelStorageFeederBehavior implements BehaviourProvider {

    private final List<ActionEntry> feederWidth;




    public FuelStorageFeederBehavior() {
        this.feederWidth = new LinkedList<>();
        feederWidth.add(new ActionEntry((short) -5, "Set target temperature to:", ""));
        feederWidth.add(new FuelStorageFeederPerformer("'Glowing Coals' ( refuel at < 4 min burn time left )",4000).actionEntry);
        feederWidth.add(new FuelStorageFeederPerformer("'Few Flames' ( refuel at < 5 min burn time left )",5000).actionEntry);
        feederWidth.add(new FuelStorageFeederPerformer("'Small Flames' ( refuel at < 7 min burn time left )",7000).actionEntry);
        feederWidth.add(new FuelStorageFeederPerformer("'Wild Flames' ( refuel at < 9 min burn time left)",9000).actionEntry);
        feederWidth.add(new FuelStorageFeederPerformer("'Full Blaze' ( refuel at < 15 min burn time left)",15000).actionEntry);



    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {

        if (target.getTemplateId() == FuelStorageItems.fuelStorageId) {
            if (FuelStorageOpenPerformer.canUse(performer, target)) {
                return feederWidth;

            }

        } else
            return null;
        return null;

    }
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        return getBehavioursFor(performer, target);
    }
}
