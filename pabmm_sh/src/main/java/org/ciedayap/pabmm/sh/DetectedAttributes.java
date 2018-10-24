/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.pabmm.pd.requirements.Attributes;
import org.ciedayap.utils.StringUtils;

/**
 * This class is responsible for keeping in memory all the read attributes from 
 * the M&E project definition. The hashing is computed from the Attribute ID.
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class DetectedAttributes implements Serializable{
    /**
     * Data structure for keeping in memory the attribute information
     */
    private ConcurrentHashMap<String,Attribute> map;
    
    /**
     * Default constructor
     */
    public DetectedAttributes()
    {

    }

    @Override
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        sb=sb.append(" Initialized?: ").append((getMap()!=null)?"Yes":"No")
          .append(" Size: ").append((getMap()!=null)?String.valueOf(getMap().size()):"0");
        
        return sb.toString();
    }
    
    /**
     * Factory method for the class. In case of no specification about the initialSize parameter,
     * the method will asign 100 as initialSize and 0.75 as loadFactor.
     * @param initialSize The initial size related to the hash map
     * @return a new DetectedAttributes instance with the initialized concurrent hash table 
     */
    public static synchronized DetectedAttributes create(Integer initialSize)
    {
        DetectedAttributes da=new DetectedAttributes();
        if(initialSize!=null && initialSize>1)
            da.setMap(new ConcurrentHashMap(initialSize,(float)0.75));
        else
            da.setMap(new ConcurrentHashMap(100,(float)0.75));
        
        return da;
    }

    /**
     * @return the map
     */
    public synchronized ConcurrentHashMap<String,Attribute> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public synchronized void setMap(ConcurrentHashMap<String,Attribute> map) {        
        this.map = map;
    }
    
    /**
     * It Incorporates a new attribute into the hashtable
     * @param at the attribute to be incorporated
     * @return TRUE when the attribute is successful incorporated, FALSE otherwise.
     */
    public boolean add(Attribute at)
    {
        if(at==null || org.ciedayap.utils.StringUtils.isEmpty(at.getID()) ||
                org.ciedayap.utils.StringUtils.isEmpty(at.getDefinition())){
            return false;
        }
        
        if(map==null) return false;
        
        map.put(at.getID(), at); 
        
        return map.get(at.getID())!=null;
    }
    
    /**
     * It incorporates a collection of attributes into the hashmap
     * @param list The list to be incorporated
     * @return FALSE in case of the empty list (or null), TRUE otherwise.
     */
    public boolean addCollection(Attributes list)
    {
        if(list==null || list.getCharacteristics()==null || list.getCharacteristics().isEmpty()) return false;
        
        list.getCharacteristics().forEach(at->add(at));
        
        return true;
    }
    
     /**
     * It removes an attribute from the hashtable
     * @param atid the attribute to be removed
     * @return TRUE when the attribute is successful removed, FALSE otherwise.
     */
    public boolean remove(String atid)
    {
        if(StringUtils.isEmpty(atid)) return false;
        
        if(map==null) return false;      
        
        return (map.remove(atid)!=null);
    }
    
    /**
     * It removes an attribute from the hashtable
     * @param at the attribute to be removed
     * @return TRUE when the attribute is successful removed, FALSE otherwise.
     */
    public boolean remove(Attribute at)
    {
        if(at==null || org.ciedayap.utils.StringUtils.isEmpty(at.getID()) ||
                org.ciedayap.utils.StringUtils.isEmpty(at.getDefinition())) return false; 
        
        return remove(at.getID());
    }

    /**
     * It cleans the hashmap
     * @return TRUE when the hashmap is cleaned, FALSE otherwise
     */
    public boolean clear()
    {
        if(map==null) return false; 
        
        map.clear();
        
        return true;
    }    
    /**
     * It verifies whether an attribute is present or not in the hashtable.
     * @param atid the attribute ID to be verified
     * @return TRUE the attribute is in the table, FALSE otherwise
     */
    public boolean contains(String atid)
    {
        if(StringUtils.isEmpty(atid)) return false;
        
        if(map==null) return false;      
        
        return map.containsKey(atid);
    }
    
    public synchronized Enumeration<Attribute> getUniqueAttributeList()
    {
        if(map==null) return null;
        
        return map.elements();
    }
}
