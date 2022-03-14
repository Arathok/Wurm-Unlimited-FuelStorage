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
        fuelStorage = new ItemTemplateBuilder("arathok.fuelStorage.fuelStorage").name("glass mixture", "glass mixtures",
                                                                                    "A mixture made from sand, ash, and sandstone shards. Under high temperatures it will turn into a honey"
                                                                                            + "like paste. cooling it off you will get crystal clear glass. But what would a pile of glass be of use for? If you had some kind of a"
                                                                                            + "mould you could press the hot glass into shape...")
                .modelName("model.fuelStorage.fuelStorage")
                .imageNumber((short) IconConstants.ICON_SMALL_CRATE)
                .itemTypes(new short[] {

                        ItemTypes.ITEM_TYPE_PLANTABLE,
                        ItemTypes.ITEM_TYPE_DECORATION,
                        ItemTypes.ITEM_TYPE_TURNABLE,
                        ItemTypes.ITEM_TYPE_REPAIRABLE,
                        // ItemTypes.ITEM_TYPE_TRANSPORTABLE,
                        ItemTypes.ITEM_TYPE_HOLLOW,
                        ItemTypes.ITEM_TYPE_METAL,

                }).decayTime(9072000L).dimensions(40, 40, 40).weightGrams(5000).material(Materials.MATERIAL_IRON)
                .behaviourType((short) 1).primarySkill(SkillList.ALCHEMY_NATURAL).difficulty(10) // no hard lock
                .build();

        fuelStorageId = fuelStorage.getTemplateId();
        CreationEntryCreator
                .createAdvancedEntry(SkillList.SMITHING_BLACKSMITHING, ItemList.crateSmall, ItemList.nailsIronLarge, fuelStorageId, true, true, 0.0f, false, false,
                                     CreationCategories.STORAGE)
                .addRequirement(new CreationRequirement(1, ItemList.nailsIronLarge, 8, true))
                .addRequirement(new CreationRequirement(2, ItemList.fenceBars, 3, true));
    }



}
