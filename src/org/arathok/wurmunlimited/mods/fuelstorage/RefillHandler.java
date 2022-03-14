package org.arathok.wurmunlimited.mods.fuelstorage;


import com.wurmonline.math.TilePos;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class RefillHandler
{
            public static int targetTemp=2000;
            public static VolaTile[] tiles;
            public static long nextpoll=0;
            public static List<Item> furnaces = new LinkedList<>();

            public static void PollFurnaces()
            {
                Long time= System.currentTimeMillis();
                Item[] allItems;
                allItems = Items.getAllItems();

                if (time > nextpoll)
                {
                    for (Item oneItem : allItems)
                    {
                        if (oneItem.getTemplateId() == 180 || oneItem.getTemplateId() == 178 || oneItem.getTemplateId() == 1028 || oneItem.getTemplateId() == 1023 || oneItem.getTemplateId() == 1178)
                        {
                            if (!furnaces.contains(oneItem))
                            furnaces.add(oneItem);
                        }
                    }
                    nextpoll=time+300000;
                }

            }

            public static void Refill()
             {
                 Iterator<Item> furnaceRofler =  furnaces.iterator();
                 while (furnaceRofler.hasNext())
                 {
                     Item furnaceToEdit = furnaceRofler.next();
                     Item fuelStorageItem;
                     VolaTile tileOfFurnaceToEdit;
                     if(furnaceToEdit.isOnSurface())
                     tileOfFurnaceToEdit = Zones.getTileOrNull(furnaceToEdit.getTilePos(),true);
                     else
                         tileOfFurnaceToEdit = Zones.getTileOrNull(furnaceToEdit.getTilePos(),false);

                     Iterator<Item> otherItemsOnTile = Arrays.stream(tileOfFurnaceToEdit.getItems()).iterator();
                        while (otherItemsOnTile.hasNext())
                        {
                            Item compare = otherItemsOnTile.next();
                            if(compare.getTemplate()==FuelStorageItems.fuelStorage)
                            {
                                fuelStorageItem=compare;
                            }


                        }

                     double newTemp = (source.getWeightGrams() * Item.fuelEfficiency(source.getMaterial()));
                     if (target.getTemperature() > 1000) {
                         short maxTemp = 30000;
                         short newPTemp = (short)(int)Math.min(30000.0D, target.getTemperature() + newTemp);
                         target.setTemperature(newPTemp);
                 }
             }

}




