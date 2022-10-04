package org.arathok.wurmunlimited.mods.fuelstorage;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.shared.constants.IconConstants;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPropagation;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.io.IOException;

public class FuelStorageItems {

    public static int fuelStorageId;
    public static ItemTemplate fuelStorage;

    public static void registerFuelStorage() throws IOException
    {
        fuelStorage = new ItemTemplateBuilder("arathok.fuelStorage.fuelStorage").name("fuel storage", "fuel storage",
                                                                                    "A box made of Wood and Iron Bars designed" +
                                                                                            " to have fuel items drop into a fire place automatically." +
                                                                                            "For it to work you need to open its feeders flap. If you want it to stop feeding fuel to the fire close the feeders flap.\n" +
                                                                                            "There is also a slider on the feeder that allows it to use more fuel to bring the fire to a wanted higher target temperature." +
                                                                                            "Setting it to high temperatures may result in inefficient fuel usage. By default it seems to be on the lowest setting" +
                                                                                            "and will only refuel a fire that only has glowing coals left.")
                .modelName("model.fuelStorage.fuelStorage.")
                .imageNumber((short) IconConstants.ICON_SMALL_CRATE)
                .itemTypes(new short[] {

                        ItemTypes.ITEM_TYPE_PLANTABLE,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_TURNABLE,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                        // ItemTypes.ITEM_TYPE_TRANSPORTABLE,
                        ItemTypes.ITEM_TYPE_HOLLOW,





                }).decayTime(9072000L).dimensions(200, 200, 200).weightGrams(5000).material(Materials.MATERIAL_WOOD_BIRCH)
                .behaviourType((short) 1).primarySkill(SkillList.SMITHING_BLACKSMITHING).difficulty(30) // no hard lock
                .build();

        fuelStorageId = fuelStorage.getTemplateId();
        CreationEntryCreator
                .createAdvancedEntry(SkillList.SMITHING_BLACKSMITHING, ItemList.plank, ItemList.nailsIronLarge, fuelStorageId, true, true, 0.0f, false, false,0,25,
                                     CreationCategories.STORAGE)
                .addRequirement(new CreationRequirement(1, ItemList.nailsIronLarge, 8, true))
                .addRequirement(new CreationRequirement(2, ItemList.plank, 10, true))
                .addRequirement(new CreationRequirement(3, ItemList.fenceBars, 3, true));
    }



}
