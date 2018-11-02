/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ciedayap.utils.StopWords;

/**
 * It transforms a string representation based on its vocabulary in a boolean array representation
 * @author Mario Diván
 */
public class StringToVec {
    /**
     * The vocabulary with the assigned codes
     */
    private final ConcurrentHashMap<String,Integer> coding;
    /**
     * The dimensionality related to the vocabulary depending on the stemming and stopWords configuration
     */
    private final Integer dim;
    /**
     * It indicates whether the stemming option is enabled or not
     */
    private final boolean stemming;
    /**
     * It indicates whether the stop words should be kept or not
     */
    private final boolean removeStopWord;
    
    /**
     * It initializates the vocabulary, its associated codes and their configuration (stemming and stopwords)
     * @param codedVocabulary The coded vocabulary
     * @param stemming It indicates whether the stemming option is enabled or not
     * @param remoSW It indicates whether the stop word optunio  is enabled or not
     */
    public StringToVec(ConcurrentHashMap codedVocabulary, boolean stemming,boolean remoSW)
    {
        coding=codedVocabulary;
        if(codedVocabulary!=null)
            dim=codedVocabulary.size();
        else
            dim=0;
        
        this.stemming=stemming;
        this.removeStopWord=remoSW;
    }
    
    /**
     * It creates a boolean vector with the dimension given by the vocabulary.
     * All the positions are initialized with the default value
     * @param pdefault The initial value
     * @return A boolean vector which each value is initiated to pgdefault
     */
    private Boolean[] createBooleanEmptyVector(boolean pdefault)
    {
        if(dim==null) return null;
        Boolean vec[]=new Boolean[dim];
        Arrays.fill(vec, pdefault);
        
        return vec;
    }

    /**
     * It creates a integer vector with the dimension given by the vocabulary.
     * All the positions are initialized with the default value
     * @param pdefault The initial value
     * @return A integer vector which each value is initiated to pgdefault
     */
    private Integer[] createIntegerEmptyVector(int pdefault)
    {
        if(dim==null) return null;
        Integer vec[]=new Integer[dim];
        Arrays.fill(vec, pdefault);
        
        return vec;
    }

    /**
     * It creates a double vector with the dimension given by the vocabulary.
     * All the positions are initialized with the default value
     * @param pdefault The initial value
     * @return A double vector which each value is initiated to pgdefault
     */
    private Double[] createDoubleEmptyVector(double pdefault)
    {
        if(dim==null) return null;
        Double vec[]=new Double[dim];
        Arrays.fill(vec, pdefault);
        
        return vec;
    }
       
    /**
     * It takes a string and returns its representation as a boolean vector 
     * @param value The string to be converted
     * @return The boolean vector representation for the string
     * @throws Exception  It happens when the coding is not defined, or the indicated value is empty or null.
     */
    public Boolean[] stringToBooleanVector(String value) throws Exception
    {
        if(coding==null || dim==0) throw new Exception("The coding is not available");
        if(value==null || value.trim().length()==0)
            throw new Exception("The value is empty");
        
        String tvalue=value.toLowerCase();
        tvalue=tvalue.replaceAll("[\\d|\\[\\](.,!?)*+?¿=/&%$\":;\\-_<>]", "");
        String vec[]=tvalue.split("\\s");     
        ConcurrentLinkedQueue clq=new ConcurrentLinkedQueue();
        clq.addAll(Arrays.asList(vec));
       
        Boolean bvector[]=createBooleanEmptyVector(false);
        Runnable task=()->{
            while(!clq.isEmpty())
            {
                String word=(String) clq.poll();
                if(word!=null)
                {
                    if(this.removeStopWord && StopWords.isStopword(word))
                    {
                     //Nothing to do   
                    }
                    else
                    {
                     word=(this.stemming)?StopWords.stemString(word):word;
                     Integer pos=coding.get(word);
                     if(pos!=null && pos>=0 && pos<dim)
                         bvector[pos]=true;
                     else
                         System.out.println(word+" not located "+pos);
                    }                    
                }
            }
        };
                
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<5;i++)
        { 
            executor.execute(task);
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()){}

        return bvector;
    }
    /**
     * Auxiliar index used in the StringToVectr funcitons
     */
    private Integer currentIdx;
    
    /**
     * It obtains the vector representation as an integer vector
     * @param value The string to be vectorized
     * @return The integer vector related to the string
     * @throws Exception 
     */
    public Integer[] stringToIntVector(String value) throws Exception
    {
        Boolean ret[]=stringToBooleanVector(value);
        if(ret==null || ret.length==0) return null;        
        Integer[] retcnv=stringToIntVector(ret);
        
        return retcnv;
    }

    /**
     * It obtains the vector representation as an integer vector from tge boolean vector
     * @param value The vectorized string as a boolean array
     * @return The integer vector related to the string
     * @throws Exception 
     */
    public Integer[] stringToIntVector(Boolean[] value) throws Exception
    {
        Boolean ret[]=value;
        if(ret==null || ret.length==0) return null;        
        Integer[] retcnv=new Integer[ret.length];
        this.setCurrentIdx(0);
        
        Runnable task=()->{
           Integer idx=currentAndIncrement();
           while(idx<dim)
           {
               retcnv[idx]=(ret[idx])?1:0;
               idx=currentAndIncrement();
           }
        };
        
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<5;i++)
        { 
            executor.execute(task);
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()){}
        
        return retcnv;
    }
    
    /**
     * It obtains the vector representation as an DOUBLE vector
     * @param value The string to be vectorized
     * @return The double vector related to the string
     * @throws Exception 
     */
    public Double[] stringToDoubleVector(String value) throws Exception
    {
        Boolean ret[]=stringToBooleanVector(value);
        if(ret==null || ret.length==0) return null;        
        Double[] retcnv=stringToDoubleVector(ret);
        
        return retcnv;
    }

    /**
     * It obtains the vector representation as an DOUBLE vector from the boolean vector
     * @param value The vectorized string as a boolean array
     * @return The double vector related to the string
     * @throws Exception 
     */
    public Double[] stringToDoubleVector(Boolean[] value) throws Exception
    {
        Boolean ret[]=value;
        if(ret==null || ret.length==0) return null;        
        Double[] retcnv=new Double[ret.length];
        this.setCurrentIdx(0);
        
        Runnable task=()->{
           Integer idx=currentAndIncrement();
           while(idx<dim)
           {
               retcnv[idx]=(ret[idx])?1.0:0.0;
               idx=currentAndIncrement();
           }
        };
        
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<5;i++)
        { 
            executor.execute(task);
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()){}
        
        return retcnv;
    }
    
    /**
     * @return the currentIdx
     */
    public synchronized Integer getCurrentIdx() {
        return currentIdx;
    }

    /**
     * It returns the current value and next the value is incremented
     * @return the current value of the index before the incrementing
     */
    public synchronized Integer currentAndIncrement()
    {
        Integer old=currentIdx;
        currentIdx++;
        return old;
    }
    
    /**
     * @param currentIdx the currentIdx to set
     */
    public synchronized void setCurrentIdx(Integer currentIdx)
    {
        this.currentIdx = currentIdx;
    }
}
