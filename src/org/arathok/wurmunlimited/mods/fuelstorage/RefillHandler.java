package org.arathok.wurmunlimited.mods.fuelstorage;


import com.wurmonline.math.TilePos;
import com.wurmonline.server.Items;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MaterialUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;


public class RefillHandler {


    public static long nextpoll = 0;
    public static long nextrefillpoll = 0;
    public static List<FuelStorageObject> fuelStorages = new LinkedList<>();


    public static void Refill() throws SQLException, NoSuchItemException {
        TilePos tp = null;
        long time = System.currentTimeMillis();
        long accompanyingFurnace = -10;
        if (time > nextrefillpoll && (fuelStorages.size() > 0))          // if poll time is reached and there is any registered fuelStorage start teh refill routine
        {
            if (Config.verboseLogging)
                FuelStorage.logger.log(Level.INFO, "Refuelling furnaces...");

            int count = 0;
            for (FuelStorageObject fuelStorageToEdit : fuelStorages) { // from a list of all fuelstorages go through each one


                // PREVENT ITEMS FROM BURNING INSIDE!!!
                Item aFuelstorage = null;
                Item[] itemsInside = null;
                Optional<Item> maybeFuelStorage = Items.getItemOptional(fuelStorageToEdit.itemId);



                if (maybeFuelStorage.isPresent()) {

                    aFuelstorage = maybeFuelStorage.get();
                    itemsInside = aFuelstorage.getItemsAsArray();
                    for (Item oneItemInside : itemsInside) {
                        if (oneItemInside.getTemperature() > 200) {
                            oneItemInside.setTemperature((short) 100);
                        }
                    }

                    if (fuelStorageToEdit.isActive)    // is the fuel storage even turned on? if yes check if the accompanying furnace is lit
                    {

                        tp = aFuelstorage.getTilePos();   //get the tile pos and make sure that an underground fuel Storage does not fuel a surface furnace and vice versa

                        if (aFuelstorage.isOnSurface()) {
                            for (Item oneItem : Zones.getTileOrNull(tp, true).getItems()) {
                                if ((oneItem.getTemplateId() == 178 && Config.refuelOvens) || (oneItem.getTemplateId() == ItemList.still && Config.refuelStills) || (oneItem.getTemplateId() == 180 && Config.refuelForges) || (oneItem.getTemplateId() == 1023 && Config.refuelKilns) || (oneItem.getTemplateId() == 1028 && Config.refuelSmelters)) {
                                    accompanyingFurnace = oneItem.getWurmId();

                                    break;
                                }
                            }
                        } else
                            for (Item oneItem : Zones.getTileOrNull(tp, false).getItems()) {
                                if ((oneItem.getTemplateId() == 178 && Config.refuelOvens) || (oneItem.getTemplateId() == 1178 && Config.refuelStills) || (oneItem.getTemplateId() == 180 && Config.refuelForges) || (oneItem.getTemplateId() == 1023 && Config.refuelKilns) || (oneItem.getTemplateId() == 1028 && Config.refuelSmelters)) {
                                    {
                                        accompanyingFurnace = oneItem.getWurmId();
                                        break;
                                    }

                                }
                            }
                        Optional<Item> maybeAccompanyingFurnace = Items.getItemOptional(accompanyingFurnace);

                        if (maybeAccompanyingFurnace.isPresent()) {
                            Item accompanyingFurnaceItem = maybeAccompanyingFurnace.get();

                            if (accompanyingFurnaceItem.getTemperature() < fuelStorageToEdit.targetTemp && accompanyingFurnaceItem.getTemperature() > 1000 && !aFuelstorage.getItems().isEmpty()) {

                                Item[] itemsInFuelStorage = aFuelstorage.getItemsAsArray();
                                byte material = Materials.MATERIAL_MAX;
                                int weight = 30000;
                                Item itemToBurn = null;

                                for (Item oneItem : itemsInFuelStorage)             // if current temp < target temp check all the items in the fuelStorage and select the one with the lowest fuel value
                                {
                                    if (oneItem.getMaterial() < material && oneItem.isBurnable()) {

                                        material = oneItem.getMaterial();

                                    }
                                }

                                for (Item oneItem : itemsInFuelStorage)             // and go through all items again with that fuel byte value to select the one with the lowest weight. CONSERVE FUEL GODDAMNIT
                                {
                                    if (oneItem.getMaterial() == material && oneItem.getWeightGrams() < weight && oneItem.isBurnable()) {
                                        weight = oneItem.getWeightGrams();
                                        itemToBurn = oneItem;

                                    }
                                }

                                double newTemp;
                                if (itemToBurn != null) {                    // if an item that is burnable was found, call the vanilla "burn" action effect and burn that item

                                    newTemp = (itemToBurn.getWeightGrams() * Item.fuelEfficiency(material));
                                    if (accompanyingFurnaceItem.isOnSurface() && Config.verboseLogging)
                                        FuelStorage.logger.log(Level.INFO,
                                                "fueled the fire place " + accompanyingFurnaceItem.getTemplate().getName() + " with id: " + accompanyingFurnace + " @ " + accompanyingFurnaceItem.getTileX() + " " + accompanyingFurnaceItem.getTileY() + " on surface with " + itemToBurn.getTemplate().getName() + " Which weighed " + itemToBurn.getWeightGrams());
                                    else if (!accompanyingFurnaceItem.isOnSurface() && Config.verboseLogging)
                                        FuelStorage.logger.log(Level.INFO, "fueled the fire place " + accompanyingFurnaceItem.getTemplate().getName() + " with id: " + accompanyingFurnace + " @ " + accompanyingFurnaceItem.getTileX() + " " + accompanyingFurnaceItem.getTileY() + " underground with " + itemToBurn.getTemplate().getName() + " Which weighed " + itemToBurn.getWeightGrams());

                                    float qlbonus = (1 + (aFuelstorage.getCurrentQualityLevel() / 50));
                                    short newPTemp = (short) (int) Math.min((Short.MAX_VALUE - 1), (accompanyingFurnaceItem.getTemperature() + (newTemp * qlbonus)));
                                    if (Config.verboseLogging)
                                        FuelStorage.logger.log(Level.INFO, "OldTemp:" + accompanyingFurnaceItem.getTemperature() + " New Temp = " + newPTemp);
                                    accompanyingFurnaceItem.setTemperature(newPTemp);
                                    Items.destroyItem(itemToBurn.getWurmId());
                                    count++;
                                } else { // NO Fuelable item present
                                    if (Config.verboseLogging) {
                                        FuelStorage.logger.log(Level.INFO, "Fuel Storage has run out of fuel and closed: " + accompanyingFurnace + " @ " + Items.getItem(accompanyingFurnace).getTileX() + " " + Items.getItem(accompanyingFurnace).getTileY());

                                    }
                                    // CLOSE FUELSTORAGE
                                    fuelStorageToEdit.isActive = false;
                                    aFuelstorage.setName(aFuelstorage.getTemplate().getName() + " (feeder closed)");// target.setName(target.getName() + " (feeder closed)");
                                    updateStatus(FuelStorage.dbconn, fuelStorageToEdit);
                                    SoundPlayer.playSound("sound.object.lockunlock",aFuelstorage,1.6F);
                                }
                            } else if (aFuelstorage.getItems().isEmpty()) { // No Item present
                                if (Config.verboseLogging) {
                                    FuelStorage.logger.log(Level.INFO, "Fuel Storage has run out of fuel and closed: " + accompanyingFurnace + " @ " + Items.getItem(accompanyingFurnace).getTileX() + " " + Items.getItem(accompanyingFurnace).getTileY());

                                }
                                // CLOSE FUELSTORAGE
                                fuelStorageToEdit.isActive = false;
                                aFuelstorage.setName(aFuelstorage.getTemplate().getName() + " (feeder closed)");// target.setName(target.getName() + " (feeder closed)");
                                updateStatus(FuelStorage.dbconn, fuelStorageToEdit);
                                SoundPlayer.playSound("sound.object.lockunlock",aFuelstorage,1.6F);
                            }
                        }
                    }
                } else {
                    FuelStorage.logger.log(Level.SEVERE, "fuelstorage with stored wurmId "+fuelStorageToEdit.itemId+" somehow got deleted or decayed? Rebuilding DB and List.");
                    remove(FuelStorage.dbconn, fuelStorageToEdit.itemId);
                    fuelStorages.clear();
                    readFromSQL(FuelStorage.dbconn, fuelStorages);
                }

            }
            if (count > 0)
                FuelStorage.logger.log(Level.INFO, "Refuelled " + count + " furnaces");

            nextrefillpoll = time + Config.nextRefillPoll;
        }

    }


    public static void readFromSQL(Connection dbconn, List<FuelStorageObject> fuelStorages) throws SQLException, NoSuchItemException {
        FuelStorage.logger.log(Level.INFO, "reading all previously opened fuelstorages from the DB");

        try {
            PreparedStatement ps = dbconn.prepareStatement("SELECT * FROM FuelStorageV2");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                FuelStorageObject afuelStorageObject = new FuelStorageObject();
                afuelStorageObject.itemId = rs.getLong("itemId"); // liest quasi den Wert von der Spalte
                afuelStorageObject.targetTemp = rs.getLong("targetTemp"); // liest quasi den Wert von der Spalte
                afuelStorageObject.isActive = rs.getBoolean("isActive"); // liest quasi den Wert von der Spalte
                Optional<Item> maybeaFuelStorageItem = Items.getItemOptional(afuelStorageObject.itemId);
                if (maybeaFuelStorageItem.isPresent()) {
                    Item aFuelStorageItem = maybeaFuelStorageItem.get();

                    FuelStorage.logger.log(Level.INFO, "adding: " + afuelStorageObject.itemId);
                    fuelStorages.add(afuelStorageObject);

                    if (afuelStorageObject.isActive) {
                        aFuelStorageItem.setName(aFuelStorageItem.getTemplate().getName() + " - feeder open");

                        switch ((int) afuelStorageObject.targetTemp) {
                            case 15000: {
                                aFuelStorageItem.setName(aFuelStorageItem.getName() + (", full blaze"), true);
                                aFuelStorageItem.setHidden(true);
                                aFuelStorageItem.setHidden(false);

                                break;
                            }

                            case 9000: {
                                aFuelStorageItem.setName(aFuelStorageItem.getName() + (", wild flames"), true);
                                aFuelStorageItem.setHidden(true);
                                aFuelStorageItem.setHidden(false);
                                break;
                            }

                            case 7000: {
                                aFuelStorageItem.setName(aFuelStorageItem.getName() + (", small flames"), true);
                                aFuelStorageItem.setHidden(true);
                                aFuelStorageItem.setHidden(false);
                                break;
                            }

                            case 5000: {
                                aFuelStorageItem.setName(aFuelStorageItem.getName() + (", few flames"), true);
                                aFuelStorageItem.setHidden(true);
                                aFuelStorageItem.setHidden(false);
                                break;
                            }

                            case 4000: {
                                aFuelStorageItem.setName(aFuelStorageItem.getName() + (", glow. coals"), true);
                                aFuelStorageItem.setHidden(true);
                                aFuelStorageItem.setHidden(false);
                                break;
                            }
                        }
                    }
                    else
                    {
                        aFuelStorageItem.setName(aFuelStorageItem.getTemplate().getName()+" - feeder closed");
                    }

                }
                else
                    remove(dbconn, afuelStorageObject.itemId);

            }
            FuelStorage.finishedReadingDB = true;
            rs.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE, "something went wrong writing to the DB!" + throwables.getMessage(), throwables);

        }

    }

    public static void insert(Connection dbconn, FuelStorageObject aFuelStorage) throws SQLException {
        try {
            PreparedStatement ps = dbconn.prepareStatement("INSERT OR REPLACE INTO FuelStorageV2 (itemId,targetTemp,isActive) VALUES (?,?,?)");
            ps.setLong(1, aFuelStorage.itemId);
            ps.setLong(2, aFuelStorage.targetTemp);
            ps.setBoolean(3, aFuelStorage.isActive);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE, "something went wrong writing to the DB!", throwables);
            throwables.printStackTrace();
        }


    }

    public static void updateStatus(Connection dbconn, FuelStorageObject aFuelStorage) throws SQLException {
        try {
            PreparedStatement ps = dbconn.prepareStatement("UPDATE FuelStorageV2 SET  isActive = ? WHERE itemId = ?");
            ps.setBoolean(1, aFuelStorage.isActive);
            ps.setLong(2, aFuelStorage.itemId);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE, "something went wrong updating status to the DB!", throwables);
            throwables.printStackTrace();
        }

    }

    public static void updateTemp(Connection dbconn, FuelStorageObject aFuelStorage) throws SQLException {
        try {
            PreparedStatement ps = dbconn.prepareStatement("UPDATE FuelStorageV2 SET  targetTemp = ? WHERE itemId = ?");
            ps.setLong(1, aFuelStorage.targetTemp);
            ps.setLong(2, aFuelStorage.itemId);

            ps.executeUpdate();
            ps.close();
        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE, "something went wrong updating temp to the DB!", throwables);
            throwables.printStackTrace();
        }


    }

    public static void remove(Connection dbconn, long aItemId) {
        try {

            PreparedStatement ps = dbconn.prepareStatement("DELETE FROM FuelStorageV2 WHERE itemId = ?");
            ps.setLong(1, aItemId);
            ps.execute();
            ps.close();

        } catch (SQLException throwables) {
            FuelStorage.logger.log(Level.SEVERE, "something went wrong deleting a Fuelstroage from the DB!", throwables);
            throwables.printStackTrace();
        }
    }


}




