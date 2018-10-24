/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.SorensenDice;
import java.util.ArrayList;
import java.util.Enumeration;
import org.ciedayap.cincamimisconversor.QueueException;
import org.ciedayap.pabmm.pd.CINCAMIPD;
import org.ciedayap.pabmm.pd.CINCAMIPDQueue;
import org.ciedayap.pabmm.pd.TabularTranslator;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.utils.TranslateJSON;

/**
 *
 * @author Mario
*/
public class test {
   
    public static void main(String args[]) throws Exception
    {

        //1 Quitar StopWords
        //2 STEM de la palabra
        //3 Frecuencia de la palabra en Hash
        //4 Vectorizar Definición para el concepto (true (1), false (0)) en orden
        //5 LSH para similaridad de vectores
        //Comparar con las otras distancias de texto
        testAt();
    } 
    
    public static void testCoef()
    {
        ArrayList<String> lista=new ArrayList();
        
        lista.add("La frecuencia cardiaca es la cantidad de pulsos por unidad de tiempo");
        lista.add("heathbeat es la cantidad de latidos del corazón por minuto");
        lista.add("La cantidad de pulsaciones por minuto es la frecuencia del corazón");
        lista.add("El corazón desconoce la razón");
        
        JaroWinkler jw = new JaroWinkler();
        System.out.println("JaroWinkler: "+jw.similarity(lista.get(0), lista.get(3)));
        Cosine c=new Cosine();
        System.out.println("Cosine: "+c.similarity(lista.get(0), lista.get(3)));
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        System.out.println("Levenshtein: "+l.similarity(lista.get(0), lista.get(3)));
        Jaccard j=new Jaccard();
        System.out.println("Jacard: "+j.similarity(lista.get(0), lista.get(3)));
        SorensenDice sd=new SorensenDice();
        System.out.println("Sorensen-Dice: "+sd.similarity(lista.get(0), lista.get(3)));        
    }
    
    public static void testAt() throws QueueException
    {
        String jsonPD="{\"version\":\"1.0\",\"creation\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"ctemp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value of the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heartrate\",\"name\":\"The Heart Rate\",\"definition\":\"Number of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_humi\",\"name\":\"The Environmental Humidity\",\"definition\":\"Amount of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_temp\",\"name\":\"The Environmental Temperature\",\"definition\":\"Value of the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_press\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures resulting from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\"}]}}";
        
        DetectedAttributes da=DetectedAttributes.create(10);
        CINCAMIPDQueue queue=new CINCAMIPDQueue();
        TabularTranslator tm=new TabularTranslator(queue,da);
        Thread t=new Thread(tm);
        t.start();
        
        for(int i=0;i<4;i++)
        {
            CINCAMIPD pd=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class,jsonPD);
            
            queue.add(pd);
        }    
        
        while(!queue.isEmpty())
        {
            
        }
        
        tm.setAutoSense(false);//stop thread
        Enumeration<Attribute> lat=da.getUniqueAttributeList();
        System.out.println("ID\t\tName\t\t\tDefinition");
        while(lat.hasMoreElements())
        {
            Attribute at=lat.nextElement();
            System.out.println(at.getID().trim()+"\t\t"+at.getName().trim()+"\t\t\t"+at.getDefinition());            
        }
        
    }
}
