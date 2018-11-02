/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.SorensenDice;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     * It keeps the relation between each attribute and its matrix position
     */
    private ConcurrentHashMap<String,Integer> matrixAttributePosition;
    /**
     * It keeps the relation between each attribute and its matrix position
     */
    private ConcurrentHashMap<Integer,String> matrixPositionAttribute;
    /**
     * It keeps the last similarity computing related to the attributes
     */
    private SimilarityTriangularMatrix lastSimilarityMatrix;
    /**
     * It keeps the last computed type of distance
     */
    private short lastDistanceType;
    
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
    /**
     * Temporary variable used for computing the max lenght definition
     */
    private Integer maxl;
    
    /**
     * The total number of words among all definitions
     */
    private Integer totalWordCount;
    
    /**
     * Position in the integer array related to the max length of the definitions contained at the HashMap instance 
     * @see getMaxLengthDefinition
     */
    public static short MAX_LENGTH=0;
    /**
     * Position in the integer array related to the total word count of the definitions contained at the HashMap instance 
     * @see getMaxLengthDefinition
     */    
    public static short TOTAL_WORDS=1;
    /**
     * It takes the Detected Attributes present in the HashMap and computes the max length in the word definition
     * jointly with the max number of words (including duplicate words)
     * @return An Integer array with two positions. The first position corresponds with the max length of a definition, while the
     * second position is associated with the total number of words among all the definitions.
     */
    public Integer[] getMaxLengthDefinition()
    {   
        //Synchronized methods
        this.maxl=0;
        this.totalWordCount=0;
        
        Enumeration<String> list=map.keys();
        if(list==null) return new Integer[]{0,0};
        
        Runnable task=()->{
            while(list.hasMoreElements())
            {
                String key=list.nextElement();
                Attribute at=map.get(key);
                String definition=at.getDefinition();
                
                //All the chars go to lowecase
                definition= definition.toLowerCase();
                //It replaces the punctuation, exclamation and other chars
                definition=definition.replaceAll("[\\d|\\[\\](.,!?)*+?¿=/&%$\":;\\-_<>]", "");
                //It split the sentence by the whitespace
                String vec[]=definition.split("\\s");
                int countWord=0;
                if(vec!=null)
                {
                    for(String wvec:vec)
                    {
                        wvec=wvec.trim();
                        if(wvec.length()>0) {
                            countWord++;
                            wvec=wvec.replaceAll("\\W", "");//Just Chars in the word                            
                        }                       
                    }
                }
                
                this.processMax(countWord);
                
                //System.out.println("Thread: "+Thread.currentThread().getName()+"Key: "+key+" Length: "+countWord);
            }            
        };
        
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<map.size();i++)
        { 
            executor.execute(task);
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()){}
        
        return new Integer[]{this.maxl,this.totalWordCount};//gral
    }
    
    protected synchronized void processMax(Integer val)
    {
        if(val==null || val<0) return;
        
        //Maximum
        if(maxl==null)
        {
            maxl=val;
        }
        
        maxl=(val>maxl)?val:maxl;
        
        //Accumulator
        if(this.totalWordCount==null) this.totalWordCount=0;
        totalWordCount+=val;
    }
    /**
     * It indicates that the similarity in the matrix is computed using the Jaro Winkler formula
     */
    public final static short SIMILARITY_JARO_WINKLER=0;
    /**
     * It indicates that the similarity in the matrix is computed using the Cosine formula
     */
    public final static short SIMILARITY_COSINE=1;
    /**
     * It indicates that the similarity in the matrix is computed using the Levenshtein formula
     */    
    public final static short SIMILARITY_LEVENSHTEIN=2;
    /**
     * It indicates that the similarity in the matrix is computed using the Jaccard formula
     */    
    public final static short SIMILARITY_JACCARD=3;
    /**
     * It indicates that the similarity in the matrix is computed using the Sorensen-Dice formula
     */    
    public final static short SIMILARITY_SORENSEN_DICE=4;
    
    /**
     * It computes the similarity matrix for all the attributes in the instance
     * @param type The specifical formulta to be used. This can be: SIMILARITY_COSINE,
     * SIMILARITY_JACCARD, SIMILARITY_JARO_WINKLER, SIMILARITY_LEVENSHTEIN, SORENSEN_DICE
     * @return The similarity triangular matrix when the computing is possible, null otherwise.
     */
    public SimilarityTriangularMatrix computeSimilarityMatrix(final short type) throws Exception
    {       
        if(map==null || map.isEmpty()) return null;
        switch(type)
        {
            case DetectedAttributes.SIMILARITY_COSINE:
            case DetectedAttributes.SIMILARITY_JACCARD:
            case DetectedAttributes.SIMILARITY_JARO_WINKLER:
            case DetectedAttributes.SIMILARITY_LEVENSHTEIN:
            case DetectedAttributes.SIMILARITY_SORENSEN_DICE:
                break;
            default:
                return null;
        }                
        
        SimilarityTriangularMatrix matrix=SimilarityTriangularMatrix.createSimilarityTriangularMatrix(map.size());
        
        Enumeration<String> keyList=map.keys();
        if(matrixAttributePosition==null) matrixAttributePosition=new ConcurrentHashMap(matrix.getDim());
        else matrixAttributePosition.clear();
        if(matrixPositionAttribute==null) matrixPositionAttribute=new ConcurrentHashMap(matrix.getDim());
        else matrixPositionAttribute.clear();
        int idx=0;
        while(keyList.hasMoreElements())
        {
            String pkey=keyList.nextElement();
            matrixAttributePosition.put(pkey, idx);
            matrixPositionAttribute.put(idx,pkey);
            idx++;            
        }
        
        Enumeration<String> lis=matrixAttributePosition.keys();
        Runnable task=()->{
            Object ptr=null;
            switch(type)
            {
                case DetectedAttributes.SIMILARITY_COSINE:
                    ptr=new Cosine();
                    break;
                case DetectedAttributes.SIMILARITY_JACCARD:
                    ptr=new Jaccard();
                    break;
                case DetectedAttributes.SIMILARITY_JARO_WINKLER:
                    ptr=new JaroWinkler();
                    break;
                case DetectedAttributes.SIMILARITY_LEVENSHTEIN:
                    ptr=new NormalizedLevenshtein();
                    break;
                case DetectedAttributes.SIMILARITY_SORENSEN_DICE:
                    ptr=new SorensenDice();
                    break;                
            }

            while(lis.hasMoreElements())
            {
                String atid=lis.nextElement();
                int position=matrixAttributePosition.get(atid);
                matrix.set(position,position, 1.0);
                
                for(int i=position+1;i<matrix.getDim();i++)
                {
                    String compareTo=matrixPositionAttribute.get(i);
                    
                    String row_a=map.get(atid).getDefinition();
                    String col_b=map.get(compareTo).getDefinition();

                    switch(type)
                    {
                        case DetectedAttributes.SIMILARITY_COSINE:
                            matrix.set(position, i, ((Cosine)ptr).similarity(row_a, col_b));
                            break;
                        case DetectedAttributes.SIMILARITY_JACCARD:
                            matrix.set(position, i, ((Jaccard)ptr).similarity(row_a, col_b));
                            break;
                        case DetectedAttributes.SIMILARITY_JARO_WINKLER:
                            matrix.set(position, i, ((JaroWinkler)ptr).similarity(row_a, col_b));
                            break;
                        case DetectedAttributes.SIMILARITY_LEVENSHTEIN:
                            matrix.set(position, i, ((NormalizedLevenshtein)ptr).similarity(row_a, col_b));
                            break;
                        case DetectedAttributes.SIMILARITY_SORENSEN_DICE:
                            matrix.set(position, i, ((SorensenDice)ptr).similarity(row_a, col_b));
                            break;                
                    }                    
                }
            }
        };
        
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<map.size();i++)
        { 
            executor.execute(task);
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()){}
        
        this.lastDistanceType=type;        
        this.lastSimilarityMatrix=matrix;
        
        return matrix;
    }
    
    public Attribute getAttribute(int matrixIdx)
    {
        if(this.matrixPositionAttribute==null || matrixIdx<0 || matrixIdx>matrixPositionAttribute.size()) return null;
        
        String id=matrixPositionAttribute.get(matrixIdx);
        if(id==null) return null;
        
        return map.get(id);
    }

    public Integer getPosition(String id)
    {
        if(this.matrixAttributePosition==null || id==null) return null;
        
        Integer idx=matrixAttributePosition.get(id);

        return idx;
    }

    /**
     * @return the lastSimilarityMatrix
     */
    public SimilarityTriangularMatrix getLastSimilarityMatrix() {
        return lastSimilarityMatrix;
    }

    /**
     * @return the lastDistanceType
     */
    public short getLastDistanceType() {
        return lastDistanceType;
    }
}
