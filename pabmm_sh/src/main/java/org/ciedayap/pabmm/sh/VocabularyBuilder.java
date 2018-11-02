/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.utils.StopWords;

/**
 * It is responsible for the vocabulary creation from the detected attributes.
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class VocabularyBuilder {
    /**
     * The detected attributes for the list of projects
     */
    private final DetectedAttributes detected;
    /**
     * The vocanulary constituted by the words and its frequency
     */
    private final ConcurrentHashMap<String, Integer> vocabulary;
    /**
     * It is used as auxiliar variable for the coding of the words in the hash map
     */
    private Integer sequenceNumber;
    /**
     * It indicates whether the stemming option has been enabled or not
     */
    private boolean stemming;
    /**
     * It indicates whether the stemming option has been enabled or not
     */
    private boolean removeStopWords;
    /**
     * The constructor is responsible for building the HashMap associated with the vocabulary from
     * the total word count available on the DetectedAttributes instance.
     * @param detat The DetectedAttribute instance who containsall the attributes from the projects
     */
    VocabularyBuilder(DetectedAttributes detat)
    {
        detected=detat;
        if(detected==null) vocabulary=null;
        else 
        {
            Integer data[]=detected.getMaxLengthDefinition();
            if(data==null || data.length==0) vocabulary=null;
            else vocabulary=new ConcurrentHashMap(data[DetectedAttributes.TOTAL_WORDS],(float)0.75);
        }       
        
        createVocabulary();
    }
    
    /**
     * It incorporates the word in the Vocabulary with frequency 1. When the word exist, then the frequency is incremented.
     * @param word The word to be incorporated in the vocabulary
     */
    protected void addWord(String word)
    {
        if(word==null || word.trim().length()==0) return;
        if(getVocabulary()==null || detected==null) return;
        
        Integer freq=getVocabulary().get(word);
        if(freq==null) getVocabulary().put(word, 1);
        else getVocabulary().put(word, (++freq));                        
    }
    
    /**
     * It removes the word from the vocabulary
     * @param word  The word to be removed
     */
    protected void remove(String word)
    {
        if(word==null || word.trim().length()==0) return;
        if(getVocabulary()==null || detected==null) return;
        
        getVocabulary().remove(word);
    }
    
    protected final boolean createVocabulary()
    {   
        //Clear the previous vocabulary
        this.getVocabulary().clear();
        
        Enumeration<String> list=detected.getMap().keys();
        if(list==null) return false;
        
        Runnable task=()->{
            while(list.hasMoreElements())
            {
                String key=list.nextElement();
                Attribute at=detected.getMap().get(key);
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
                            this.addWord(wvec);//Incorporate the word in the dictionary
                        }                       
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
        
        return getVocabulary().size()>0;
    }

    /**
     * @return the vocabulary processed at the creation instant
     */
    public ConcurrentHashMap<String, Integer> getVocabulary() {
        return vocabulary;
    }
    
    /**
     * When there exists the vocabulary, it returns a new vocabulary in where each
     * word is stemmed. It incorporates the possibility of keeping out the stop words.
     * @param removeStopWords It indicates whether a stop word should be retained or not
     * @param stemming TRUE: Enabled, FALSE: Disabled.
     * @return null in case of the absence of the original vocabulary, a new copy of the vocabulary (stemmed
     * and/or without stopwords) depending on the selected options.
     */
    public ConcurrentHashMap<String, Integer> getFittedVocabulary(boolean stemming,boolean removeStopWords)
    {
        if(vocabulary==null || vocabulary.isEmpty()) return null;
        this.stemming=stemming;
        this.removeStopWords=removeStopWords;
        
        Enumeration<String> mylist=vocabulary.keys();
        
        final ConcurrentHashMap<String,Integer> ret=new ConcurrentHashMap();
        
        Runnable task=()->{
            while(mylist.hasMoreElements())
            {
                String key=mylist.nextElement();
                String mstem=key;
                if(stemming)
                {
                    mstem=StopWords.stemString(key);
                    mstem=(mstem==null)?key:mstem;
                }
                
                Integer freq=vocabulary.get(key);
                freq=(freq==null || freq<0)?1:freq;

                //Warning. It is possible than previously mstem there is not exist 
                Integer prev=ret.get(mstem);
                prev=(prev==null || prev<0)?0:prev;

                if(removeStopWords && StopWords.isStopword(key))
                {
                }
                else
                {
                    ret.put(mstem, prev+freq);
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
        
        return ret;
    }    
    
    /**
     * It replaces the frequencies related to the words in the Hash Map by an unique code
     * @param vocab The vocabulary with the words (as key) and the associated frequencies 
     * @return The same HAshMap replacing the frequencies with the unique codes
     */
    public boolean coding(ConcurrentHashMap vocab)
    {
        if(vocab==null || vocab.isEmpty()) return false;
        this.setSequenceNumber(-1);
        
        Enumeration<String> words=vocab.keys();
        Runnable task=()->{
            while(words.hasMoreElements())
            {
                String word=words.nextElement();
                vocab.put(word, addNumberToSequence(1));
            }            
        };
        
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<5;i++)
        { 
            executor.execute(task);
        }
        
        executor.shutdown();
        
        while(!executor.isTerminated()){}
        
        return true;
    }

    /**
     * @return the sequenceNumber
     */
    public synchronized Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * @param sequenceNumber the sequenceNumber to set
     */
    public synchronized void  setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    /**
     * It incorporates the value in the current value of the sequenceNumber variable. The value
     * to be incorporated could be positive or not.
     * @param value The value to be incorporated into the sequence number.
     * @return The sequenceNumber once it was modified.
     */
    public synchronized Integer addNumberToSequence(Integer value)
    {
        if(value==null) return this.sequenceNumber;
        
        this.sequenceNumber= this.sequenceNumber + value;
        
        return this.sequenceNumber;
    }

    /**
     * @return the stemming
     */
    public boolean isStemming() {
        return stemming;
    }

    /**
     * @return the removeStopWords
     */
    public boolean isRemoveStopWords() {
        return removeStopWords;
    }
}
