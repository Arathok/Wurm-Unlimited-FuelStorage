package org.arathok.wurmunlimited.mods.fuelstorage;


import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.VolaTileItems;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


public class RefillHandler
{


            public static long nextpoll=0;
            public static long nextrefillpoll=0;
            public static List<Item> fuelStorages = new LinkedList<>();

            public static void PollFurnaces()
            {
                long time= System.currentTimeMillis();
                Item[] allItems;
                allItems = Items.getAllItems();

                if (time > nextpoll)
                {
                    for (Item oneItem : allItems)
                    {
                        if (oneItem.getTemplate() == FuelStorageItems.fuelStorage )
                        {
                            if (!fuelStorages.contains(oneItem)) {
                                fuelStorages.add(oneItem);
                                FuelStorage.logger.log(Level.INFO, "Polling Fireplaces. Found " + fuelStorages.size() + " firing Places");
                            }
                        }
                    }
                    nextpoll=time+300000;
                }

            }
 // OLD
            public static void Refill()
             {
                 TilePos tp = null;
                 long time = System.currentTimeMillis();
                 Item accompanyingFurnace=null;
                 if (time>nextrefillpoll)
                 {
                     FuelStorage.logger.log(Level.INFO, "Refuelling:");
                     for (Item fuelStorageToEdit : fuelStorages)
                     {
                         tp=fuelStorageToEdit.getTilePos();

                         if (fuelStorageToEdit.isOnSurface())
                           for (Item oneItem : Zones.getTileOrNull(tp,true).getItems())
                            {
                            if (oneItem.getTemplate() == FuelStorageItems.fuelStorage)
                                {
                                accompanyingFurnace = oneItem;
                                break;
                                }
                            }
                         else
                               for (Item oneItem : Zones.getTileOrNull(tp,true).getItems())
                                {
                                 if (oneItem.getTemplate() == FuelStorageItems.fuelStorage)
                                    {
                                    accompanyingFurnace = oneItem;
                                    break;
                                    }
                                }


                        if (accompanyingFurnace != null)
                        if (accompanyingFurnace.getTemperature() < 2000 && accompanyingFurnace.getTemperature()>1000&&fuelStorageToEdit.getFirstContainedItem() != null)
                            {
                                FuelStorage.logger.log(Level.INFO,
                               "fueled the fire place"+ accompanyingFurnace.getTemplate().getName()+ "@" +" "+ accompanyingFurnace.getTileX() +" "+ accompanyingFurnace.getTileY() + "with " + fuelStorageToEdit.getFirstContainedItem().getTemplate().getName());
                                 double newTemp = (fuelStorageToEdit.getFirstContainedItem().getWeightGrams() * Item.fuelEfficiency(fuelStorageToEdit.getFirstContainedItem().getMaterial()));
                                 short maxTemp = 30000;
                                 short newPTemp = (short) (int) Math.min(30000.0D, accompanyingFurnace.getTemperature() + newTemp);
                                 accompanyingFurnace.setTemperature(newPTemp);
                                 Items.destroyItem(fuelStorageToEdit.getFirstContainedItem().getWurmId());


                             }

                     }

                 }
                 nextrefillpoll=time+60000;
             }


//new
/*    public static void Refill(Item item) ///TODO find out if poll is item or not and where items are polled.
    {
        if (item.getTemplateId() == 180 || item.getTemplateId() == 1023 || item.getTemplateId() == 178 || item.getTemplateId() == 1178 || item.getTemplateId() == 1028)
        {
            TilePos tp = item.getTilePos();

            Item fuelStorage = null;
            byte fuelStorageFirstMaterial;
            if (item.isOnSurface())

            for (Item oneItem : Zones.getTileOrNull(tp,true).getItems())
            {
                if (oneItem.getTemplate() == FuelStorageItems.fuelStorage)
                {
                    fuelStorage = oneItem;
                    break;
                }
            }
            else
            {
                for (Item oneItem : Zones.getTileOrNull(tp,false).getItems())
                {
                    if (oneItem.getTemplate() == FuelStorageItems.fuelStorage)
                    {
                        fuelStorage = oneItem;
                        break;
                    }
                }
            }
            if (fuelStorage != null)
            {

                if (item.getTemperature() < 2000 && item.getTemperature()>1000&&fuelStorage.getFirstContainedItem() != null)
                {

                    double newTemp = (fuelStorage.getFirstContainedItem().getWeightGrams() * fuelStorage.getFirstContainedItem().getMaterial());
                    short maxTemp = 30000;
                    short newPTemp = (short) (int) Math.min(30000.0D, item.getTemperature() + newTemp);
                    item.setTemperature(newPTemp);
                    Items.destroyItem(fuelStorage.getFirstContainedItem().getWurmId());
                }
            }
        }

    }

 */
}




