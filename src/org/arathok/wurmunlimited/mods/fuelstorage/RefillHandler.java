package org.arathok.wurmunlimited.mods.fuelstorage;


import com.wurmonline.math.TilePos;
import com.wurmonline.server.Items;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.zones.Zones;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                        if (oneItem.getTemplate() == FuelStorageItems.fuelStorage&&oneItem.getParentOrNull()==null )
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
                 if (time>nextrefillpoll&&(fuelStorages.size()>0))
                 {
                     FuelStorage.logger.log(Level.INFO, "Refuelling:");

                     for (long fuelStorageToEdit : fuelStorages) {

                         if (Items.getItem(fuelStorageToEdit) == null)
                             fuelStorages.remove(fuelStorageToEdit);
                         else {

                         if (Items.getItem(fuelStorageToEdit).getTemperature()>200)
                             Items.getItem(fuelStorageToEdit).setTemperature((short) 100);

                             tp = Items.getItem(fuelStorageToEdit).getTilePos();

                             if (Items.getItem(fuelStorageToEdit).isOnSurface()) {
                                 for (Item oneItem : Zones.getTileOrNull(tp, true).getItems()) {
                                     if ((oneItem.getTemplateId() == 178 && Config.refuelOvens) || (oneItem.getTemplateId() == ItemList.still && Config.refuelStills) || (oneItem.getTemplateId() == 180 && Config.refuelForges) || (oneItem.getTemplateId() == 1023 && Config.refuelKilns) || (oneItem.getTemplateId() == 1028 && Config.refuelSmelters)) {
                                         accompanyingFurnace = oneItem.getWurmId();

                                         break;
                                     }
                                 }
                             }
                             else
                                 for (Item oneItem : Zones.getTileOrNull(tp, false).getItems()) {
                                     if ((oneItem.getTemplateId() == 178&&Config.refuelOvens)||(oneItem.getTemplateId() == 1178&&Config.refuelStills)||(oneItem.getTemplateId() == 180&&Config.refuelForges)||(oneItem.getTemplateId() == 1023&&Config.refuelKilns)||(oneItem.getTemplateId() == 1028&&Config.refuelSmelters)) {
                                         {
                                             accompanyingFurnace = oneItem.getWurmId();
                                             break;
                                         }

                                     }
                                 }

                             if (accompanyingFurnace!=-10 &&accompanyingFurnace!=0)

                                 if (Items.getItem(accompanyingFurnace).getTemperature() < 4000 && Items.getItem(accompanyingFurnace).getTemperature() > 1000 && !Items.getItem(fuelStorageToEdit).getItems().isEmpty()) {
                                     Item[] itemsInFuelStorage = Items.getItem(fuelStorageToEdit).getItemsAsArray();
                                     byte material= Materials.MATERIAL_MAX;
                                     int weight=30000;
                                     Item itemToBurn=null;

                                     for (Item oneItem:itemsInFuelStorage)
                                     {
                                         if (oneItem.getMaterial()<material&&oneItem.isBurnable())
                                         {

                                             material=oneItem.getMaterial();

                                         }
                                     }

                                     for (Item oneItem:itemsInFuelStorage)
                                     {
                                         if (oneItem.getMaterial()==material&&oneItem.getWeightGrams()<weight&&oneItem.isBurnable())
                                         {
                                             weight=oneItem.getWeightGrams();
                                             itemToBurn=oneItem;

                                         }
                                     }

                                     double newTemp;
                                     if (itemToBurn!=null) {
                                         newTemp = (itemToBurn.getWeightGrams() * Item.fuelEfficiency(material));
                                         if (Items.getItem(accompanyingFurnace).isOnSurface())
                                         FuelStorage.logger.log(Level.INFO,
                                                 "fueled the fire place " + Items.getItem(accompanyingFurnace).getTemplate().getName() + " @ " + Items.getItem(accompanyingFurnace).getTileX() + " " + Items.getItem(accompanyingFurnace).getTileY() + " on surface with " + itemToBurn.getTemplate().getName() + " Which weighed " + itemToBurn.getWeightGrams());
                                         else
                                             FuelStorage.logger.log(Level.INFO,
                                                    "fueled the fire place " + Items.getItem(accompanyingFurnace).getTemplate().getName() + " @ " + Items.getItem(accompanyingFurnace).getTileX() + " " + Items.getItem(accompanyingFurnace).getTileY() + " underground with " + itemToBurn.getTemplate().getName()+" Which weighed " + itemToBurn.getWeightGrams());


                                         short newPTemp = (short) (int) Math.min(30000.0D, Items.getItem(accompanyingFurnace).getTemperature() + newTemp);
                                         FuelStorage.logger.log(Level.INFO,"New Temp = " + newPTemp);
                                         Items.getItem(accompanyingFurnace).setTemperature(newPTemp);
                                         Items.destroyItem(itemToBurn.getWurmId());

                                     }


                                 }

                         }
                     }

                     nextrefillpoll=time+60000;
                 }

             }


    public static void readFromSQL(Connection dbconn, List<Long> fuelStorages) throws SQLException, NoSuchItemException {
        FuelStorage.logger.log(Level.INFO,"reading all previously opened fuelstorages from the DB");
        long afuelStorage=0;
        PreparedStatement ps = dbconn.prepareStatement("SELECT * FROM FuelStorage");
        ResultSet rs=ps.executeQuery();
        while (rs.next()) {



            afuelStorage = rs.getLong("itemId"); // liest quasi den Wert von der Spalte
            FuelStorage.logger.log(Level.INFO,"adding: "+afuelStorage);
            fuelStorages.add(afuelStorage);
            Item aFuelStorageItem = Items.getItem(afuelStorage);
            aFuelStorageItem.setName(aFuelStorageItem.getTemplate().getName() + " (feeder open)");
        }
    }

    public static void insert(Connection dbconn, long itemId) throws SQLException {

        PreparedStatement ps = dbconn.prepareStatement("insert into FuelStorage (itemID) values (?)");
        ps.setLong(1,itemId);

        ps.executeUpdate();



    }




}




