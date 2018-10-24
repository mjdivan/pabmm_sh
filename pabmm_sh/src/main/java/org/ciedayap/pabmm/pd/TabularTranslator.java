/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.pd;

import java.util.ArrayList;
import org.ciedayap.pabmm.sh.DetectedAttributes;
import org.ciedayap.cincamimisconversor.QueueException;
import org.ciedayap.pabmm.pd.requirements.Attribute;

/**
 * Given a CINCAMIPDQueue (Source) and the HAshMap for the attributes, It is responsible
 * for extracting the attributes from the definition and putting them inside the HashMap instance.
 * @author Mario Diván
 * @version 1.0
 */
public class TabularTranslator implements Runnable{
    /**
     * This flag indicates the behavior once a CINCAMI/PD was processed.
     * TRUE indicates that the processor will continue supervising the queue waiting
     * for new messages. FALSE indicates that once the message was processed, the thread
     * will be finished.
     */
    private boolean autoSense;    
    /**
     * It represents the queue with instances of the CINCAMIPD waiting for extraction of their attributes
     */
    private CINCAMIPDQueue pdQueue;
    /**
     * It represents the HashMap related to the detected attributes along the project definitions
     */
    private DetectedAttributes daHash;
    
    /**
     * It builds an instance responsible for processing the instances inside the queue, extracting 
     * the attributes and putting them into the HashMap (DetectedAttributes).
     * @param queue The queue to be processed
     * @param da The HashMap related to the detected attributes
     * @throws QueueException It happens when the queue or the hash map are null
     */
    public TabularTranslator(CINCAMIPDQueue queue,DetectedAttributes da) throws QueueException 
    {
        if(queue==null) throw new QueueException("The Queue is null");
        if(da==null) throw new QueueException("The DetectedAttributes instances is null");
        
        pdQueue=queue;
        daHash=da;    
        autoSense=true;
    }

    @Override
    public void run() {
        while(autoSense)
        {
         if(!pdQueue.isEmpty())
         {
             CINCAMIPD currentPD=null;
             ArrayList<MeasurementProject> plist=null;                                          
             try {
                 currentPD=pdQueue.firstAvailableandRemove();
                 if(currentPD!=null)
                 {
                    plist=currentPD.getProjects().getProjects();
                 }
             } catch (QueueException ex) {
                 currentPD=null;
                 plist=null;
             }
             
             //Completing the HashMap
             if(plist!=null)
             {
                //Make the translation
               plist.forEach(p->processAttributes(p,daHash));               
             }                         
         }                          
        }
    }
    
    /**
     * It takes the Measurement Project Definition and extracts the attributes and context properties.
     * @param mp The project definition
     * @param da The HashMap where the attributes and context properties should be incorporated
     */
    public static void processAttributes(MeasurementProject mp,DetectedAttributes da)    
    {
        if(mp==null || mp.getInfneed()==null || mp.getInfneed().getSpecifiedEC()==null ||
                mp.getInfneed().getSpecifiedEC().getDescribedBy()==null ||
                mp.getInfneed().getSpecifiedEC().getDescribedBy().getCharacteristics()==null ||
                mp.getInfneed().getSpecifiedEC().getDescribedBy().getCharacteristics().isEmpty()) 
        {
            System.out.println("Project without attributes");
            return;
        }
        
        if(da==null){
            System.out.println("HashMap null");
            return;
        }
        
        //Incorporating the attributes -mandatory-
        da.addCollection(mp.getInfneed().getSpecifiedEC().getDescribedBy());
        
        //Incorporating the context properties -optional-
        if(mp.getInfneed()!=null && mp.getInfneed().getCharacterizedBy()!=null &&
                mp.getInfneed().getCharacterizedBy().getDescribedBy()!=null &&
                mp.getInfneed().getCharacterizedBy().getDescribedBy().getContextProperties()!=null)
        {   //The ContextProperty is an Attribute
            mp.getInfneed().getCharacterizedBy().getDescribedBy().getContextProperties().forEach(p->da.add(p));
        }
    }

    /**
     * @return the pdQueue
     */
    public CINCAMIPDQueue getPdQueue() {
        return pdQueue;
    }

    /**
     * @return the daHash
     */
    public DetectedAttributes getDaHash() {
        return daHash;
    }

    /**
     * @return the autoSense
     */
    public boolean isAutoSense() {
        return autoSense;
    }

    /**
     * @param autoSense the autoSense to set
     */
    public void setAutoSense(boolean autoSense) {
        this.autoSense = autoSense;
    }
}
