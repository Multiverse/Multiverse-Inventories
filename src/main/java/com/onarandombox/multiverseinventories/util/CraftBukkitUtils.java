package com.onarandombox.multiverseinventories.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagByte;
import net.minecraft.server.NBTTagByteArray;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagDouble;
import net.minecraft.server.NBTTagFloat;
import net.minecraft.server.NBTTagInt;
import net.minecraft.server.NBTTagIntArray;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagLong;
import net.minecraft.server.NBTTagShort;
import net.minecraft.server.NBTTagString;

import org.json.simple.JSONObject;

import com.onarandombox.multiverseinventories.api.DataStrings;

/**
 * Helper class for dealing with some craftbukkit stuff, mostly nbt tags
 */
public class CraftBukkitUtils {
    
    public static Collection<String> NBTKeysToIgnore = new HashSet<String>();
    
    static {
        NBTKeysToIgnore.add("Count");
        NBTKeysToIgnore.add("Slot");
        NBTKeysToIgnore.add("id");
        NBTKeysToIgnore.add("Damage");
        NBTKeysToIgnore.add("ench");
    }
    
    /**
     * Converts a raw java object to an nbt tag
     * 
     * @param rawValue The raw java object
     * @param name The name of the nbt tag
     * @param type The type of the nbt tag
     * @param extraData Any additional data currently only used to specify the type of objects in a list
     * @return The nbt tag object computed from the data
     */
    public static NBTBase jsonToNBTObject (Object rawValue, String name, byte type, byte extraData) {
        
        NBTBase value = null;
        
        try {
            switch (type) {
                case 1:
                    value = new NBTTagByte(name, (Byte) rawValue);
                    break;
                    
                case 2:
                    value = new NBTTagShort(name, ((Number) rawValue).shortValue());
                    break;
                    
                case 3:
                    value = new NBTTagInt(name, (Integer) rawValue);
                    break;
                    
                case 4:
                    value = new NBTTagLong(name, (Long) rawValue);
                    break;
                    
                case 5:
                    value = new NBTTagFloat(name, (Float) rawValue);
                    break;
                    
                case 6:
                    value = new NBTTagDouble(name, (Double) rawValue);
                    break;
                    
                case 7:
                    value = new NBTTagByteArray(name, (byte[]) rawValue);
                    break;
                    
                case 8:
                    value = new NBTTagString(name, (String) rawValue);
                    break;
                    
                case 9:
                    //Pass the info to another function to convert a java list to an nbt list
                    value = CraftBukkitUtils.jsonToNBTList((List) rawValue, name, extraData);
                    break;
                    
                case 10:
                    //Pass the info to another function to convert the json object to an nbt compound
                    value = CraftBukkitUtils.jsonToNBTTagCompound((JSONObject) rawValue);
                    break;
                    
                case 11:
                    value = new NBTTagIntArray(name, (int[]) rawValue);
                    break;
            }
        } catch (ClassCastException e) {
            
            Logging.warning(e.getMessage() + ", could not parse " + NBTBase.getTagName(type) + ": " + rawValue);
            return null;
        } catch (Exception e) {
            return null;
        }
        
        return value;
    }
    
    /**
     * Converts a parsed nbt compound in json format back to an nbt compound
     * @param json The json object to be converted back
     * @return The converted nbt compound
     */
    public static NBTTagCompound jsonToNBTTagCompound (JSONObject json) {
        
        //The compound to be returned
        NBTTagCompound compound = new NBTTagCompound();
        
        //Iterate over all the keys (names and ids) in the json object
        for (Object key : json.keySet()) {
            //If the name and ids aren't strings something went terrible wrong :<
            if (!(key instanceof String)) {
                Logging.warning("Could not parse NBTTag name: " + key.toString());
                return null;
            }
            
            //The raw data type
            String dataType = (String) key;
                
            //Split up the raw data type by the general delimiter so we can extract the ids from it
            String[] dataTypeString = ((String) dataType).split(DataStrings.GENERAL_DELIMITER);
            
            //If there were more or less than 2 or 3 elements from the split, something went horrible wrong
            if (dataTypeString.length != 2 && dataTypeString.length != 3) {
                Logging.warning("Could not parse NBTTag data type: " + dataType.toString());
                return null;
            }
                
            //Parse the nbt tag type
            byte type = 0;
            try {
                type = Byte.parseByte(dataTypeString[1]);
            } catch (NumberFormatException exception) {
                //Uh oh, the type isn't a byte
                Logging.warning("Could not parse NBTTag data type: " + dataTypeString[1]);
                return null;
            }
            
            //Additional byte for the type of nbt tags in an nbt list
            byte listType = 0;
            if (type == 9) {
                try {
                    listType = Byte.parseByte(dataTypeString[2]);
                } catch (NumberFormatException exception) {
                    //Uh oh, the type isn't a byte
                    Logging.warning("Could not parse NBTTag data type: " + dataTypeString[2]);
                    return null;
                }
            }
            
            //Name should be the first index
            String name = dataTypeString[0];
            NBTBase value = null;
            //Grab the raw value from the json map
            Object rawValue = json.get(key);
            
            //If the tag is a list
            if (type == 9) {
                //Compute the nbt tag value from the raw java object
                NBTBase computedValue = jsonToNBTObject(rawValue, name, type, listType);
                //If the nbt tag was successfully created
                if (computedValue != null) {
                    //Add it to the compound
                    compound.set(name, jsonToNBTObject(rawValue, name, type, listType));
                }
                //Continue to the next object
                continue;
            }
            
            //Compute the nbt tag value from the raw java object
            NBTBase computedValue = jsonToNBTObject(rawValue, name, type, (byte) -1);
            //If the nbt tag was successfully created
            if (computedValue != null) {
                //Add it to the compound
                compound.set(name, jsonToNBTObject(rawValue, name, type, (byte) -1));
            }
            

        }
        
        return compound;
    }
    
    /**
     * Converts a java list of nbt data to an nbt list
     * 
     * @param list The list to be converted to an nbt list
     * @param name The name of the list
     * @param type The type of objects in the list
     * @return The computed nbtlist from the data
     */
    public static NBTTagList jsonToNBTList (List list, String name, byte type) {
        NBTTagList nbtlist = new NBTTagList(name);
        for (Object object : list) {
            NBTBase computedValue = jsonToNBTObject(object, "", type, (byte) -1);
            if (computedValue != null) {
                nbtlist.add(computedValue);
            }           
        }
        
        return nbtlist;
    }
    
    /**
     * Parse an nbt compound (essentially a map of names with nbt bases)
     * 
     * @param iterator An iterator for the collection from the nbt compound taken from <pre>compound.c().iterator()</pre>
     * @return The compound as a JSONObject
     */
    public static JSONObject parseNBTCompound (Iterator iterator) {
        return parseNBTCompound(iterator, null);
    }
    
    /**
     * Parse an nbt compound (essentially a map of names with nbt bases)
     * 
     * @param iterator An iterator for the collection from the nbt compound taken from <pre>compound.c().iterator()</pre>
     * @param keysToIgnore String collection of the key names to ignore, pass null to ignore nothing
     * @return The compound as a JSONObject
     */
    public static JSONObject parseNBTCompound (Iterator iterator, Collection<String> keysToIgnore) {
        
        //Make a json object to map the names and data types along with the values
        JSONObject map = new JSONObject();
        
        //Begin looping over the nbt compound collection
        while (iterator.hasNext()) {
            //Get the nbt base object from the iterator
            NBTBase nbtbase = (NBTBase) iterator.next();
            
            //Get the name from the nbt base object
            String name = nbtbase.getName();
            
            //Ignore the specified values
            if (keysToIgnore != null) {
                if (keysToIgnore.contains(name)) {
                    continue;
                }
            }
            
            //Parse the nbt tag's value into a java object
            Object value = parseNBTValue(nbtbase, nbtbase.getTypeId());
            
            //If the nbt base is of a list type
            if (nbtbase.getTypeId() == 9) {
                //Just in case!
                if (value != null && ((NBTTagList) nbtbase).size() != 0) {
                    //Get the type of objects stored in the list
                    byte listType = ((NBTTagList) nbtbase).get(0).getTypeId();
                    //Store the name of the nbt base in the format "name;type;listtype" along with its value
                    map.put(nbtbase.getName() + DataStrings.GENERAL_DELIMITER + nbtbase.getTypeId() + DataStrings.GENERAL_DELIMITER + listType, value);
                }
                continue;
            }
            
            //Just in case!
            if (value != null) {
              //Store the name of the nbt base in the format "name;type" along with its value
                map.put(nbtbase.getName() + DataStrings.GENERAL_DELIMITER + nbtbase.getTypeId(), value);
            }
            
        }
        
        //Return the map of the names with the values
        return map;
    }
    
    /**
     * Parse an nbt list object to a java list object
     * 
     * @param tag The nbt list to be parsed
     * @return The nbt list parsed to a java list object
     */
    public static List parseNBTList (NBTTagList tag) {
        
        //Define an array list to be used as a fall back
        ArrayList values = new ArrayList();
        
        //Return an empty list if its an empty list
        if (tag.size() == 0){
            return values;
        }
            
        //Create a list with the proper parameters to give simple json a hint on how to serialize the list
        switch (tag.get(0).getTypeId()) {
            case 1:
                values = new ArrayList<Byte>();
            case 2:
                values = new ArrayList<Short>();
            case 3:
                values = new ArrayList<Integer>();
            case 4:
                values = new ArrayList<Long>();
            case 5:
                values = new ArrayList<Float>();
            case 6:
                values = new ArrayList<Double>();
            case 7:
                values = new ArrayList<byte[]>();
            case 8:
                values = new ArrayList<String>();
            case 11:
                values = new ArrayList<int[]>();
        }
        
        //Loop over the values in the nbt list
        for (int i = 0; i < tag.size(); i++) {
            //Parse the nbt value
            Object value = parseNBTValue(tag.get(i), tag.get(i).getTypeId());
            //If the value was parsed properly
            if (value != null) {
                //Add the value to the list
                values.add(value);
            }
        }
        
        //Return the list with the values
        return values;
    }
    
    /**
     * Method to parse an nbt base to a java object
     * 
     * @param nbtbase The nbt base to be parsed
     * @param type The type of the nbt base
     * @return The java object equivalent of the nbt base or null in case of error
     */
    public static Object parseNBTValue (NBTBase nbtbase, byte type) {

        //Define the object to be returned
        Object value = null;
        
        //Surround the value parser with a try catch for a class cast exception
        try {
            switch (type) {
                //Byte value of 1, cast the base to an nbt byte and return its data
                case 1:
                    value = ((NBTTagByte) nbtbase).data;
                    break;
                
                //Byte value of 2, cast the base to an nbt short and return its data
                case 2:
                    value = ((NBTTagShort) nbtbase).data;
                    break;
                
                //Byte value of 3, cast the base to an nbt int and return its data
                case 3:
                    value = ((NBTTagInt) nbtbase).data;
                    break;
               
                //Byte value of 4, cast the base to an nbt int and return its data
                case 4:
                    value = ((NBTTagLong) nbtbase).data;
                    break;
                 
                //Byte value of 5, cast the base to an nbt float and return its data
                case 5:
                    value = ((NBTTagFloat) nbtbase).data;
                    break;
                    
                //Byte value of 6, cast the base to an nbt double and return its data
                case 6:
                    value = ((NBTTagDouble) nbtbase).data;
                    break;
                    
                //Byte value of 7, cast the base to an nbt byte array and return its data
                case 7:
                    value = ((NBTTagByteArray) nbtbase).data;
                    break;
                  
                //Byte value of 8, cast the base to an nbt string and return its data
                case 8:
                    value = ((NBTTagString) nbtbase).data;
                    break;
                    
                //Byte value of 9, cast the base to an nbt list and send it to another function for additional parsing
                case 9:
                    value = parseNBTList((NBTTagList) nbtbase);
                    break;
                    
                //Byte value of 10, cast the base to an nbt compound (essentially a map) and send it to another function for additional parsing
                case 10:
                    value = parseNBTCompound(((NBTTagCompound) nbtbase).c().iterator());
                    break;
                 
                //Byte value of 11, cast the base to an nbt int array and return its data
                case 11:
                    value = ((NBTTagIntArray) nbtbase).data;
                    break;
            }
        } catch (ClassCastException exception) {
            //Uh oh something went wrong
            return null;
        }
        
        //Return the value
        return value;
    }
    
    /**
     * Grabs nbt data from player's data file in json format
     * @param file The player's data file
     * @throws FileNotFoundException 
     */
    public static JSONObject getPlayerDataJson(File file) throws FileNotFoundException {
        return parseNBTCompound(NBTCompressedStreamTools.a((InputStream) (new FileInputStream(file))).c().iterator());
    }

}
