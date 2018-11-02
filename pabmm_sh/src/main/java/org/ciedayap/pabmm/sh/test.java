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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ciedayap.cincamimisconversor.QueueException;
import org.ciedayap.pabmm.pd.CINCAMIPD;
import org.ciedayap.pabmm.pd.CINCAMIPDQueue;
import org.ciedayap.pabmm.pd.MeasurementProject;
import org.ciedayap.pabmm.pd.MeasurementProjects;
import org.ciedayap.pabmm.pd.TabularTranslator;
import org.ciedayap.pabmm.pd.context.Context;
import org.ciedayap.pabmm.pd.context.ContextProperties;
import org.ciedayap.pabmm.pd.context.ContextProperty;
import org.ciedayap.pabmm.pd.evaluation.DecisionCriteria;
import org.ciedayap.pabmm.pd.evaluation.DecisionCriterion;
import org.ciedayap.pabmm.pd.evaluation.ElementaryIndicator;
import org.ciedayap.pabmm.pd.evaluation.ElementaryModel;
import org.ciedayap.pabmm.pd.exceptions.EntityPDException;
import org.ciedayap.pabmm.pd.measurement.DataSource;
import org.ciedayap.pabmm.pd.measurement.DataSourceAdapter;
import org.ciedayap.pabmm.pd.measurement.DataSourceAdapters;
import org.ciedayap.pabmm.pd.measurement.DataSources;
import org.ciedayap.pabmm.pd.measurement.DirectMetric;
import org.ciedayap.pabmm.pd.measurement.MeasurementMethod;
import org.ciedayap.pabmm.pd.measurement.Metrics;
import org.ciedayap.pabmm.pd.measurement.Scale;
import org.ciedayap.pabmm.pd.measurement.ScaleType;
import org.ciedayap.pabmm.pd.measurement.TraceGroup;
import org.ciedayap.pabmm.pd.measurement.TraceGroups;
import org.ciedayap.pabmm.pd.measurement.Unit;
import org.ciedayap.pabmm.pd.requirements.Attribute;
import org.ciedayap.pabmm.pd.requirements.Attributes;
import org.ciedayap.pabmm.pd.requirements.CalculableConcept;
import org.ciedayap.pabmm.pd.requirements.CalculableConcepts;
import org.ciedayap.pabmm.pd.requirements.ConceptModel;
import org.ciedayap.pabmm.pd.requirements.ConceptModels;
import org.ciedayap.pabmm.pd.requirements.Entities;
import org.ciedayap.pabmm.pd.requirements.Entity;
import org.ciedayap.pabmm.pd.requirements.EntityCategory;
import org.ciedayap.pabmm.pd.requirements.InformationNeed;
import org.ciedayap.utils.StopWords;
import org.ciedayap.utils.TranslateJSON;
import org.ciedayap.utils.TranslateXML;
import org.ciedayap.utils.ZipUtil;

/**
 *
 * @author Mario
*/
public class test {
   
    public static void main(String args[]) throws Exception
    {
        simulation();
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
    
    public static void simulation() throws QueueException, Exception
    {
        long before,after;
        
        for(int i=100;i<=2000;i+=100)
        {
            before=System.nanoTime();
            testStructuralCoef(i,false,DetectedAttributes.SIMILARITY_COSINE);
            after=System.nanoTime();
        
            System.out.println("Projects: "+i+" Time (ns)"+ (after-before));
        }
    }
    
    public static void testStructuralCoef(int maxPrj,boolean show,short typeDistance) throws QueueException, Exception
    {
        String jsonPD="{\"version\":\"1.0\",\"creation\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"ctemp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value of the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heartrate\",\"name\":\"The Heart Rate\",\"definition\":\"Number of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_humi\",\"name\":\"The Environmental Humidity\",\"definition\":\"Amount of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_temp\",\"name\":\"The Environmental Temperature\",\"definition\":\"Value of the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_press\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures resulting from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\"}]}}";
        String jsonPD2="{\"version\":\"1.0\",\"creation\":\"2018-11-01T22:05:24.916-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"c_temp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value related to the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heart_rate\",\"name\":\"The Heart Rate\",\"definition\":\"Quantity of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_hum\",\"name\":\"The Environmental Humidity\",\"definition\":\"Volume of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_tem\",\"name\":\"The Environmental Temperature\",\"definition\":\"Quantity related to the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_pressure\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures derived from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\"}]}}";
        DetectedAttributes da=DetectedAttributes.create(10);
        CINCAMIPDQueue queue=new CINCAMIPDQueue();
        TabularTranslator tm=new TabularTranslator(queue,da);
               
        ExecutorService executor=Executors.newFixedThreadPool(10);
        for(int i=0;i<5;i++)
        { 
            executor.execute(tm);
        }        
           
        for(int i=0;i<maxPrj;i++)
        {
            CINCAMIPD pd=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class,((i%2==0)?jsonPD:jsonPD2));
            
            queue.add(pd);
        }    
        
        while(!queue.isEmpty())
        {
            
        }
                
        tm.setAutoSense(false);//stop thread
        executor.shutdown();
        
        while(!executor.isTerminated()){}        
        
        if(show)
        {
            Enumeration<Attribute> lat=da.getUniqueAttributeList();
            while(lat.hasMoreElements())
            {
                Attribute at=lat.nextElement();
                System.out.println(at.getID().trim()+"\t\t"+at.getName().trim()+"\t\t\t"+at.getDefinition());            
            }
        }
        
       SimilarityTriangularMatrix matrix=da.computeSimilarityMatrix(typeDistance);
       
       if(show)
       {
            System.out.println();
            final int var=1;
            DecimalFormat df2 = new DecimalFormat("#.##");

            for(int i=0;i<matrix.getDim();i++)
            {
                System.out.println();
                System.out.print(da.getAttribute(i).getID()+" ");
                for(int j=0;j<matrix.getDim();j++)
                {
                    if(j>=i)
                     System.out.print(" "+df2.format(matrix.get(i, j))+" ");
                    else
                      System.out.print("      ");
                }
            }                 
       }
    }
    
    public static void testAt() throws QueueException, Exception
    {
        String jsonPD="{\"version\":\"1.0\",\"creation\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"ctemp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value of the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heartrate\",\"name\":\"The Heart Rate\",\"definition\":\"Number of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_humi\",\"name\":\"The Environmental Humidity\",\"definition\":\"Amount of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_temp\",\"name\":\"The Environmental Temperature\",\"definition\":\"Value of the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_press\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures resulting from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-10-24T20:36:05.276-03:00[America/Buenos_Aires]\"}]}}";
        String jsonPD2="{\"version\":\"1.0\",\"creation\":\"2018-11-01T22:05:24.916-03:00[America/Buenos_Aires]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"c_temp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value related to the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heart_rate\",\"name\":\"The Heart Rate\",\"definition\":\"Quantity of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_hum\",\"name\":\"The Environmental Humidity\",\"definition\":\"Volume of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_tem\",\"name\":\"The Environmental Temperature\",\"definition\":\"Quantity related to the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_pressure\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures derived from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2018-11-01T22:05:24.9-03:00[America/Buenos_Aires]\"}]}}";
        DetectedAttributes da=DetectedAttributes.create(10);
        CINCAMIPDQueue queue=new CINCAMIPDQueue();
        TabularTranslator tm=new TabularTranslator(queue,da);
        Thread t=new Thread(tm);
        t.start();
        
        for(int i=0;i<4;i++)
        {
            CINCAMIPD pd=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class,((i%2==0)?jsonPD:jsonPD2));
            
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
        
        Integer ret[]=da.getMaxLengthDefinition();
        System.out.println("Definition Max Length: "+ret[DetectedAttributes.MAX_LENGTH]);
        System.out.println("Total Words: "+ret[DetectedAttributes.TOTAL_WORDS]);
        
        VocabularyBuilder vb=new VocabularyBuilder(da);
        Enumeration<String> words=vb.getVocabulary().keys();
        System.out.println("Vocabulary Size:"+vb.getVocabulary().size());
        
        while(words.hasMoreElements())
        {
            String word=words.nextElement();
            Integer freq=vb.getVocabulary().get(word);
            System.out.println(word+": "+freq+" ");
           // System.out.print(" StopWord: "+StopWords.isStopword(word)+ 
           //             " StemmedStopWord: "+StopWords.isStemmedStopword(word)+
           //             " Stem: "+StopWords.stemString(word)+"\n");
        }
        
       
        System.out.println("\n\n***STEMMED***");
        ConcurrentHashMap<String,Integer> voc=vb.getFittedVocabulary(false,false);
        words=voc.keys();
        System.out.println("Size: "+voc.size());
        while(words.hasMoreElements())
        {
            String word=words.nextElement();
            Integer freq=voc.get(word);
            System.out.print("Stem: "+word+" Freq: "+freq+" \n");            
        }        
        
        //Coding
        vb.coding(voc);
        words=voc.keys();
        System.out.println("Size: "+voc.size());
        while(words.hasMoreElements())
        {
            String word=words.nextElement();
            Integer freq=voc.get(word);
            System.out.print("Stem: "+word+" Code: "+freq+" \n");            
        }        
        
        //Vectorization
        
        StringToVec st=new StringToVec(voc,vb.isStemming(),vb.isRemoveStopWords());
        Boolean reto[];
        

        reto = st.stringToBooleanVector("Value of the environmental temperature in Celsius degree");
        System.out.println("Vectorization...");
        System.out.print("(");
        for(boolean v:reto)
        {
            if(v) System.out.print(" 1 ");
            else System.out.print(" 0 ");
        }
        System.out.print(")");   
        
        Double retd[]=st.stringToDoubleVector(reto);
        System.out.println();
        System.out.print("(");
        for(Double v:retd)
        {
            
            System.out.print(" "+v+" ");
        }
        System.out.print(")"); 
       
        
      
       System.out.println();
       final int var=1;
       
       SimilarityTriangularMatrix matrix=da.computeSimilarityMatrix(DetectedAttributes.SIMILARITY_COSINE);
       for(int i=0;i<matrix.getDim();i++)
       {
           System.out.println();
           System.out.print(da.getAttribute(i).getID()+" ");
           for(int j=0;j<matrix.getDim();j++)
           {
               System.out.print(" "+matrix.get(i,j)+" ");
           }
       }              
    }
    
 public static void generateJSON2(String args[]) throws EntityPDException, Exception
    {
        //1. Defining the Data Source Adapter and associated data sources
        DataSourceAdapter dsa=DataSourceAdapter.create("DSA_1", "Samsung Galaxy S6");
        DataSourceAdapters adapters=new DataSourceAdapters();
        adapters.getAdapters().add(dsa);

        //2. Defining the TraceGroup
        TraceGroup tg=TraceGroup.create("TG1", "Peter's Galaxy S6");
        TraceGroups tgroups=new TraceGroups();
        tgroups.getGroups().add(tg);
        
        //3. Defining the data sources
        DataSource ds_env_humi=DataSource.create("ds_env_humi", "Environmental Humidity's Sensor", adapters);//Interval
        ds_env_humi.setGroups(tgroups);     
        DataSources list_ds_env_humi=new DataSources();
        list_ds_env_humi.getSources().add(ds_env_humi);
                            
        DataSource ds_env_temp=DataSource.create("ds_env_temp", "Environmental Temperature's Sensor", adapters);//Interval
        ds_env_temp.setGroups(tgroups);      
        DataSources list_ds_env_temp=new DataSources();
        list_ds_env_temp.getSources().add(ds_env_temp);
        
        DataSource ds_env_press=DataSource.create("ds_env_press", "Environmental Pressure's Sensor", adapters);//Ratio
        ds_env_press.setGroups(tgroups);   
        DataSources list_ds_env_press=new DataSources();
        list_ds_env_press.getSources().add(ds_env_press);
        
        DataSource ds_heart=DataSource.create("ds_heart", "Heart Rate's Sensor", adapters);//Ratio
        ds_heart.setGroups(tgroups);     
        DataSources list_ds_heart=new DataSources();
        list_ds_heart.getSources().add(ds_heart);
        
        DataSource ds_temp=DataSource.create("ds_temp", "Corporal Temperature's Sensor", adapters);//Interval
        ds_temp.setGroups(tgroups);     
        DataSources list_ds_temp=new DataSources();
        list_ds_temp.getSources().add(ds_temp);
        
        
        //4 Defining Units and scales
        Unit u_humi=Unit.create("u_humi", "Percentage", "%");
        Unit u_temp=Unit.create("u_temp", "Celsius degreee", "C");
        Unit u_press=Unit.create("u_press", "Hectopascals", "hPa");
        Unit u_heart=Unit.create("u_heart", "Beats per minute", "bpm");
       
        Scale sca_humi=Scale.create("sca_humi","Environmental Humidity's Scale", ScaleType.INTERVAL, u_humi);
        Scale sca_temp=Scale.create("sca_temp","Environmental Temperature's Scale", ScaleType.INTERVAL, u_temp);
        Scale sca_press=Scale.create("sca_press","Environmental Pressure's Scale", ScaleType.RATIO, u_press);
        Scale sca_heart=Scale.create("sca_heart","Heart Rate's Scale", ScaleType.RATIO, u_heart);             
        
        //5. Defining the associated Measurement Method
        MeasurementMethod mm_humi=MeasurementMethod.create("mm_humi", "Direct Observation (Hygrometer)");
        MeasurementMethod mm_temp=MeasurementMethod.create("mm_temp", "Direct Observation (Thermometer)");
        MeasurementMethod mm_press=MeasurementMethod.create("mm_press", "Direct Observation (Barometer)");
        MeasurementMethod mm_heart=MeasurementMethod.create("mm_heart", "Direct Observation (Heart Rate Monitor)");
        
        //6 Defining The Direct Metrics related to the attributes and context properties 
        DirectMetric dm_pc_humi=DirectMetric.create("dm_pc_humi", "Value of Environmental Humidity", "pc_humi", sca_humi, list_ds_env_humi, mm_humi);
        DirectMetric dm_pc_temp=DirectMetric.create("dm_pc_temp", "Value of Environmental Temperature", "pc_temp", sca_temp, list_ds_env_temp, mm_temp);
        DirectMetric dm_pc_press=DirectMetric.create("dm_pc_press", "Value of Environmental Pressure", "pc_press", sca_press, list_ds_env_press, mm_press);
        DirectMetric dm_ctemp=DirectMetric.create("dm_ctemp", "Value of Corporal Temperature", "ctemp", sca_temp, list_ds_temp, mm_temp);
        DirectMetric dm_heart=DirectMetric.create("dm_heart", "Value of Heart Rate", "heartrate", sca_heart, list_ds_heart, mm_heart);
        
        //7 Defining the decision criteria associated to each metric value
            //Environmental Humidity
            DecisionCriteria envHumidityCriteria=new DecisionCriteria();
            DecisionCriterion dc=DecisionCriterion.create("humidity_low", "Low Humidity", BigDecimal.ZERO, BigDecimal.valueOf(40.0));
            dc.setNotifiableUnderLowerThreshold(false);
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(false);
            envHumidityCriteria.getCriteria().add(dc);//Low Humidity
            
            dc=DecisionCriterion.create("humidity_normal", "Normal Humidity", BigDecimal.valueOf(40.01),BigDecimal.valueOf(60));
            dc.setNotifiableUnderLowerThreshold(false);
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(true);
            dc.setNaut_message("The Environmental Humidity is upper than 60%");
            envHumidityCriteria.getCriteria().add(dc);//Normal Humidity

            dc=DecisionCriterion.create("humidity_high", "High Humidity", BigDecimal.valueOf(60.01),BigDecimal.valueOf(100));
            dc.setNotifiableUnderLowerThreshold(false);
            dc.setNotifiableBetweenThreshold(true);
            dc.setNbt_message("The Environmental Humidity is High");
            dc.setNotifiableAboveUpperThreshold(true);
            dc.setNaut_message("The Environmental Humidity is High");
            envHumidityCriteria.getCriteria().add(dc);//High Humidity

            //Environmental Temperature
            DecisionCriteria envTemp=new DecisionCriteria();
            dc=DecisionCriterion.create("temp_low", "Low Temperature", BigDecimal.valueOf(10.0),BigDecimal.valueOf(18));
            dc.setNotifiableUnderLowerThreshold(true);
            dc.setNult_message("The Environmental Temperature is under 10 celsius degree");
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(false);
            envTemp.getCriteria().add(dc);//Low Temperature

            dc=DecisionCriterion.create("temp_normal", "Normal Temperature", BigDecimal.valueOf(18.01),BigDecimal.valueOf(29));
            dc.setNotifiableUnderLowerThreshold(false);
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(false);            
            envTemp.getCriteria().add(dc);//Normal Temperature

            dc=DecisionCriterion.create("temp_high", "High Temperature", BigDecimal.valueOf(29.01),BigDecimal.valueOf(36));
            dc.setNotifiableUnderLowerThreshold(false);
            dc.setNotifiableBetweenThreshold(true);
            dc.setNbt_message("Warning. High Temperature");
            dc.setNotifiableAboveUpperThreshold(true);  
            dc.setNaut_message("Alert. Very High Temperature");
            envTemp.getCriteria().add(dc);//High Temperature

            //Environmental Pressure
            DecisionCriteria envPress=new DecisionCriteria();
            dc=DecisionCriterion.create("press_normal", "Normal Enviromental Pressure", BigDecimal.valueOf(900.0),BigDecimal.valueOf(1100));
            dc.setNotifiableUnderLowerThreshold(false);            
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(true);
            envPress.getCriteria().add(dc);//Normal Pressure

            //Corporal Temperature
            DecisionCriteria corpTemp=new DecisionCriteria();
            dc=DecisionCriterion.create("corptemp_normal", "Corporal Temperature", BigDecimal.valueOf(36.0),BigDecimal.valueOf(37.1));
            dc.setNotifiableUnderLowerThreshold(true);   
            dc.setNult_message("Warning. The Corporal Temperature is Under 36 celsiud degree");
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(true);
            dc.setNaut_message("Warning. The Corporal Temperature is Above 37.1 celsius degree");
            corpTemp.getCriteria().add(dc);//Normal Pressure      
            
            //Heart Rate
            DecisionCriteria heartRate=new DecisionCriteria();
            dc=DecisionCriterion.create("heartRate_normal", "Heart Rate", BigDecimal.valueOf(62.0),BigDecimal.valueOf(75));
            dc.setNotifiableUnderLowerThreshold(true);   
            dc.setNult_message("Warning. The Heart Rate is under than 62 bpm");
            dc.setNotifiableBetweenThreshold(false);
            dc.setNotifiableAboveUpperThreshold(true);
            dc.setNaut_message("Warning. The Heart Rate is upper than 75 bpm");
            heartRate.getCriteria().add(dc);//Normal Pressure      
   
        //8. Defining the Elementary Indicator 
            //Humidity
            ElementaryModel emodel_humi=ElementaryModel.create("elmo_humidity", "Environmental Humidity's Elementary Model ", envHumidityCriteria);
            ElementaryIndicator ind_env_humidity=ElementaryIndicator.create("ind_env_humidity", "Level of the Environmental Humidity", BigDecimal.valueOf(0.34), sca_humi, emodel_humi);
            //Environmental Temperature
            ElementaryModel emodel_env_temp=ElementaryModel.create("elmo_env_temp", "Environmental Temperature's Elementary Model ", envTemp);
            ElementaryIndicator ind_env_temp=ElementaryIndicator.create("ind_env_temp", "Level of the Environmental Temperature", BigDecimal.valueOf(0.33), sca_temp, emodel_env_temp);
            //Environmental Pressure
            ElementaryModel emodel_env_press=ElementaryModel.create("elmo_env_press", "Environmental Pressure's Elementary Model ", envPress);
            ElementaryIndicator ind_env_press=ElementaryIndicator.create("ind_env_press", "Level of the Environmental Pressure", BigDecimal.valueOf(0.33), sca_press, emodel_env_press);
            //Corporal Temperature
            ElementaryModel emodel_corptemp=ElementaryModel.create("elmo_corptemp", "Corporal Temperature's Elementary Model ", corpTemp);
            ElementaryIndicator ind_corpTemp=ElementaryIndicator.create("ind_corpTemp", "Level of the Corporal Temperature", BigDecimal.valueOf(1), sca_temp, emodel_corptemp);
            //Heart Rate
            ElementaryModel emodel_heart=ElementaryModel.create("elmo_hearttemp", "Heart Ratee's Elementary Model ", heartRate);
            ElementaryIndicator ind_heartRate=ElementaryIndicator.create("ind_heartRate", "Level of the Heart Rate", BigDecimal.valueOf(1), sca_heart, emodel_heart);
            
        //9. Defining the entity's attributes
        Metrics heartMetrics=new Metrics();
        heartMetrics.getRelated().add(dm_heart);
        Attribute at_heart=Attribute.create("heart_rate","The Heart Rate", heartMetrics);
        at_heart.setDefinition("Quantity of beats per minute (bpm)");
        at_heart.setIndicator(ind_heartRate);
        
        Metrics corpTempMetrics=new Metrics();
        corpTempMetrics.getRelated().add(dm_ctemp);
        Attribute at_ctemp=Attribute.create("c_temp", "The Corporal Temperature", corpTempMetrics);
        at_ctemp.setDefinition("Value related to the axilar temperature in Celsius degree");
        at_ctemp.setIndicator(ind_corpTemp);
        
        Attributes describedBy=new Attributes();
        describedBy.getCharacteristics().add(at_ctemp);
        describedBy.getCharacteristics().add(at_heart);        

        //10. Defining the Context Properties
        Metrics envHumMetrics=new Metrics();
        envHumMetrics.getRelated().add(dm_pc_humi);
        ContextProperty cp_humi=ContextProperty.create("pc_hum", "The Environmental Humidity", envHumMetrics);
        cp_humi.setDefinition("Volume of the water vapor in the air");
        cp_humi.setIndicator(ind_env_humidity);
                          
        Metrics envTempMetrics=new Metrics();
        envTempMetrics.getRelated().add(dm_pc_temp);
        ContextProperty cp_temp=ContextProperty.create("pc_tem", "The Environmental Temperature", envTempMetrics);
        cp_temp.setDefinition("Quantity related to the environmental temperature in Celsius degree");
        cp_temp.setIndicator(ind_env_temp);
        
        Metrics envPressMetrics=new Metrics();
        envPressMetrics.getRelated().add(dm_pc_press);
        ContextProperty cp_press=ContextProperty.create("pc_pressure", "The Environmental Pressure", envPressMetrics);
        cp_press.setDefinition("Pressures derived from human activities which bring about changes in the state of the environment");
        cp_press.setIndicator(ind_env_press);
        
        //11. Definint the Context
        Context ctxOutpatient=Context.create("ctx_outpatient", "The Outpatient Context");
        ContextProperties props=new ContextProperties();
        props.getContextProperties().add(cp_humi);
        props.getContextProperties().add(cp_temp);
        props.getContextProperties().add(cp_press);
        ctxOutpatient.setDescribedBy(props);

        
        //12. Defining the Entity under Monitoring
        Entity ent=Entity.create("Ent1", "Outpatient A (Peter)");     
        Entities monitored=new Entities();
        monitored.getEntitiesList().add(ent);

        //13. Defining the EntityCategory and associating the monitored entities
        EntityCategory ecat=EntityCategory.create("EC1", "Outpatient");
        ecat.setMonitored(monitored);
        ecat.setDescribedBy(describedBy);
 
        //14. Defining the Concept Model
        ConceptModel myCM=ConceptModel.create("cmod", "Outpatient Monitoring version 1.0");
        ConceptModels myCMs=new ConceptModels();
        myCMs.getRepresentedList().add(myCM);

        //15. Defining the calculable concept
        CalculableConcept calcon=CalculableConcept.create("calcon1", "Health");
        calcon.setRepresentedBy(myCMs);
        CalculableConcepts concepts=new CalculableConcepts();
        concepts.getCalculableConcepts().add(calcon);

        //16. Defining the Information
        InformationNeed IN_1=InformationNeed.create("IN_1", "Monitor the Outpatient", 
                "Avoid severe damages through the prevention of risks with direct impact in the outpatient health", 
                ecat, ctxOutpatient);
        IN_1.setDescribedBy(concepts);
                                        
        //17. Defining the Measurement Project
        MeasurementProject PRJ_1=MeasurementProject.create("PRJ_1","Outpatient Monitoring", ZonedDateTime.now(), IN_1, ZonedDateTime.now());
        MeasurementProjects projs=new MeasurementProjects();
        projs.getProjects().add(PRJ_1);

        //18. Associating the Measurement Project definition with the CINCAMI/PD message                
        CINCAMIPD message=CINCAMIPD.create(projs);
        
        //19. Generating the XML
        long before=System.nanoTime();
        String xml=TranslateXML.toXml(message);    
        long after=System.nanoTime();
        long xml_translating=after-before;
        before=System.nanoTime();
        byte[] compGZIP=ZipUtil.compressGZIP(xml);
        after=System.nanoTime();
        long xml_compression=after-before;
        //System.out.println(xml);
                
        //20. Generating to JSON
        before=System.nanoTime();
        String JSON=TranslateJSON.toJSON(message);
        after=System.nanoTime();
        long json_translating=after-before;
        //System.out.println(JSON);
        
        //21. Testing the GZIP Compression
        before=System.nanoTime();
        String xmld=ZipUtil.decompressGZIP(compGZIP);
        after=System.nanoTime();
        long xml_decompression=after-before;
              
        //21. Testing the conversion from the XML to Object Model        
        CINCAMIPD converted;
        before=System.nanoTime();
        converted = (CINCAMIPD) TranslateXML.toObject(CINCAMIPD.class, xmld);
        after=System.nanoTime();
        long xml_mapping=after-before;
        String xml2="";
        if(converted!=null)
        {
            //21.a Converse the recovered objet model to XML again
            xml2=TranslateXML.toXml(converted);
            
            if(xml2!=null && xml2.equalsIgnoreCase(xmld))
            {
                System.out.println("Object Model <-> CINCAMIPD XML [Compatible]");
            }
            else
            {
                System.out.println("Object Model <-> CINCAMIPD XML [Rejected]");
                //System.out.println(xml2);
                //buscar la posición con diferencia!
            }
        }
        else
        {
            System.out.println("XML Incompatible with CINCAMIPD");
        }
        
        //22. Testing the conversion from JSON to Object Model
        before=System.nanoTime();
        byte compJSON[]=ZipUtil.compressGZIP(JSON);
        after=System.nanoTime();
        long json_compression=after-before;
        before=System.nanoTime();
        String JSON2=ZipUtil.decompressGZIP(compJSON);
        after=System.nanoTime();
        long json_decompression=after-before;
        
        before=System.nanoTime();
        CINCAMIPD convertedJSON=(CINCAMIPD) TranslateJSON.toObject(CINCAMIPD.class, JSON2); 
        after=System.nanoTime();
        long json_mapping=after-before;
        String JSON3="";
        if(convertedJSON!=null)
        {
            JSON3=TranslateJSON.toJSON(convertedJSON);
            String JSON4=TranslateJSON.toJSON(TranslateJSON.toObject(CINCAMIPD.class, JSON3));
            
            //Checking multiple conversions
            if(JSON3!=null && JSON3.equalsIgnoreCase(JSON4))
            {
                System.out.println("Object Model <-> CINCAMIPD JSON [Compatible]");
            }
            else
            {
                System.out.println("Object Model <-> CINCAMIPD JSON [Rejected]");
               
            }
        }
        
        //23. Info XML
        System.out.println();
        System.out.println("XML: "+xml.getBytes().length+" bytes. Length: "+xml.length()+" Time (ns): "+xml_translating);        
        System.out.println("XML GZIP: "+compGZIP.length+" bytes. Time (ns): "+xml_compression);       
        System.out.println("Decompressed XML: "+xmld.getBytes().length+" bytes. Length: "+xmld.length()+
                " Time(ns): "+xml_decompression);
        System.out.println("GZIP: "+xml.equalsIgnoreCase(xmld));
        System.out.println("New XML2: "+xml2.getBytes().length+" bytes. Length: "+xml2.length()+" Time Mapping (ns): "+xml_mapping);
        System.out.println(xml);
        
        //24. Info JSON
        System.out.println();
        System.out.println("JSON: "+JSON.getBytes().length+" bytes. Length: "+JSON.length()+" Time(ns): "+json_translating);
        System.out.println("JSON GZIP: "+compJSON.length+" bytes. Time(ns): "+json_compression);       
        System.out.println("Decompressed JSON: "+JSON2.getBytes().length+" bytes. Length: "+JSON2.length()+
                " Time(ns): "+json_decompression);
        System.out.println("GZIP JSON: "+JSON.equalsIgnoreCase(JSON2));
        System.out.println("Time JSON Mapping (ns): "+json_mapping);
        System.out.println(JSON3);

    }    
}
