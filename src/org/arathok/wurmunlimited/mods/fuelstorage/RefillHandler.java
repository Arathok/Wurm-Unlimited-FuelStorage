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
            public static List<FuelStorageObject> fuelStorages = new LinkedList<>();



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


    public static void readFromSQL(Connection dbconn, List<FuelStorageObject> fuelStorages) throws SQLException, NoSuchItemException {
        FuelStorage.logger.log(Level.INFO,"reading all previously opened fuelstorages from the DB");

        try {
            PreparedStatement ps = dbconn.prepareStatement("SELECT * FROM FuelStorage");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {


                FuelStorageObject afuelStorageObject = new FuelStorageObject();
                afuelStorageObject.itemId = rs.getLong("itemId"); // liest quasi den Wert von der Spalte
                afuelStorageObject.targetTemp = rs.getLong("targetTemp"); // liest quasi den Wert von der Spalte
                afuelStorageObject.isActive = rs.getBoolean("isActive"); // liest quasi den Wert von der Spalte
                FuelStorage.logger.log(Level.INFO, "adding: " + afuelStorageObject.itemId);
                fuelStorages.add(afuelStorageObject);

                Item aFuelStorageItem = Items.getItem(afuelStorageObject.itemId);
                aFuelStorageItem.setName(aFuelStorageItem.getTemplate().getName() + " (feeder open)");
            }
            rs.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE,"something went wrong writing to the DB!",throwables);
            throwables.printStackTrace();
        } catch (NoSuchItemException e) {
            FuelStorage.logger.log(Level.SEVERE,"no item found for this ID",e);
            e.printStackTrace();
        }

    }

    public static void insert(Connection dbconn, FuelStorageObject aFuelStorage) throws SQLException {
        try {
            PreparedStatement ps = dbconn.prepareStatement("insert into FuelStorage (itemID,targetTemp,isActive) values (?,?,?)");
            ps.setLong(1, aFuelStorage.itemId);
            ps.setLong(2, aFuelStorage.targetTemp);
            ps.setBoolean(3, aFuelStorage.isActive);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE,"something went wrong writing to the DB!",throwables);
            throwables.printStackTrace();
        }


    }

    public static void update(Connection dbconn, FuelStorageObject aFuelStorage) throws SQLException {
        try {
            PreparedStatement ps = dbconn.prepareStatement("update FuelStorage UPDATE FuelStorage SET  isActive = ? WHERE id = ?");
            ps.setBoolean(1, aFuelStorage.isActive);
            ps.setLong(2, aFuelStorage.itemId);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE,"something went wrong writing to the DB!",throwables);
            throwables.printStackTrace();
        }


    }




}




