package org.arathok.wurmunlimited.mods.fuelstorage;


import com.wurmonline.math.TilePos;
import com.wurmonline.server.Items;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.Zones;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


public class RefillHandler
{


            public static long nextpoll=0;
            public static long nextrefillpoll=0;
            public static List<Long> fuelStorages = new LinkedList<>();

            public static void PollFurnaces()
            {
                long time= System.currentTimeMillis();
                Item[] allItems;


                if (time > nextpoll)
                {
                    allItems = Items.getAllItems();
                    for (Item oneItem : allItems)
                    {
                        if (oneItem.getTemplate() == FuelStorageItems.fuelStorage )
                        {
                            if (!fuelStorages.contains(oneItem.getWurmId())) {
                                fuelStorages.add(oneItem.getWurmId());
                                FuelStorage.logger.log(Level.INFO, "Polling Fireplaces. Found " + fuelStorages.size() + " firing Places");
                            }
                        }
                    }
                    nextpoll=time+300000;
                }

            }
 // OLD

            public static void Refill() throws NoSuchItemException
            {
                 TilePos tp = null;
                 long time = System.currentTimeMillis();
                 long accompanyingFurnace=-10;
                 if (time>nextrefillpoll)
                 {
                     FuelStorage.logger.log(Level.INFO, "Refuelling:");

                     for (long fuelStorageToEdit : fuelStorages) {

                         if (Items.getItem(fuelStorageToEdit) == null)
                             fuelStorages.remove(fuelStorageToEdit);
                         else {

                         if (Items.getItem(fuelStorageToEdit).getTemperature()>200)
                             Items.getItem(fuelStorageToEdit).setTemperature((short) 100);

                             tp = Items.getItem(fuelStorageToEdit).getTilePos();

                             if (Items.getItem(fuelStorageToEdit).isOnSurface()){
                                 for (Item oneItem : Zones.getTileOrNull(tp, true).getItems()) {
                                     if((oneItem.getTemplateId() == 178&&Config.refuelOvens)||(oneItem.getTemplateId() == 1178&&Config.refuelStills)||(oneItem.getTemplateId() == 180&&Config.refuelForges)||(oneItem.getTemplateId() == 1023&&Config.refuelKilns)||(oneItem.getTemplateId() == 1028&&Config.refuelSmelters))
                                         accompanyingFurnace = oneItem.getWurmId();
                                         break;
                                     }
                                 }
                             else
                                 for (Item oneItem : Zones.getTileOrNull(tp, false).getItems()) {
                                     if ((oneItem.getTemplateId() == 178&&Config.refuelOvens)||(oneItem.getTemplateId() == 1178&&Config.refuelStills)||(oneItem.getTemplateId() == 180&&Config.refuelForges)||(oneItem.getTemplateId() == 1023&&Config.refuelKilns)||(oneItem.getTemplateId() == 1028&&Config.refuelSmelters)) {
                                         accompanyingFurnace = oneItem.getWurmId();
                                         break;
                                     }
                                 }

                             if (accompanyingFurnace!=-10 &&accompanyingFurnace!=0)

                                 if (Items.getItem(accompanyingFurnace).getTemperature() < 4000 && Items.getItem(accompanyingFurnace).getTemperature() > 1000 && !Items.getItem(fuelStorageToEdit).getItems().isEmpty()) {
                                     Item[] itemsInFuelStorage = Items.getItem(fuelStorageToEdit).getItemsAsArray();
                                     byte material=30;
                                     Item itemToBurn=null;
                                     for (Item oneItem:itemsInFuelStorage)
                                     {
                                         if (oneItem.getMaterial()<material)
                                         {
                                             material=oneItem.getMaterial();
                                             itemToBurn=oneItem;
                                         }
                                     }

                                     double newTemp;
                                     if (itemToBurn!=null) {
                                         newTemp = (itemToBurn.getWeightGrams() * Item.fuelEfficiency(material));
                                         FuelStorage.logger.log(Level.INFO,
                                                 "fueled the fire place " + Items.getItem(accompanyingFurnace).getTemplate().getName() + "@" + " " + Items.getItem(accompanyingFurnace).getTileX() + " " + Items.getItem(accompanyingFurnace).getTileY() + "with " + itemToBurn.getTemplate().getName());


                                         short newPTemp = (short) (int) Math.min(30000.0D, Items.getItem(accompanyingFurnace).getTemperature() + newTemp);
                                         Items.getItem(accompanyingFurnace).setTemperature(newPTemp);
                                         Items.destroyItem(itemToBurn.getWurmId());
                                     }


                                 }

                         }
                     }

                     nextrefillpoll=time+60000;
                 }

             }


//new
    public static void Refill2(Item item) ///TODO find out if poll is item or not and where items are polled.
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


}




