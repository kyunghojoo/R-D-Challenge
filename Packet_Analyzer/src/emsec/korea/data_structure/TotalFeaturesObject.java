package emsec.korea.data_structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TotalFeaturesObject {

	private long f0_count;
	
	public FeatureObject f1_num_of_packets;
	public FeatureObject f2_bus_load;
	public FeatureObject f3_number_of_IDs;
	public FeatureObject f5_frequence_per_ID;
	public FeatureObject f6_similarity;
	
	private final Object syncObj0 = new Object();
	private final Object syncObj1 = new Object();
	private final Object syncObj2 = new Object();
	private final Object syncObj3 = new Object();
	private final Object syncObj5 = new Object();
	private final Object syncObj6 = new Object();
	
	public HashMap<String, ShowObject> f10_IDfeatures = null;

	public HashMap<String, ArrayList<DataObject>> data_perID = null;
	
	public TotalFeaturesObject(){
		
		this.f1_num_of_packets = new FeatureObject();
		this.f2_bus_load = new FeatureObject();
		this.f3_number_of_IDs = new FeatureObject();
		this.f5_frequence_per_ID = new FeatureObject();
		this.f6_similarity = new FeatureObject();
		
		this.f0_count = 0;
	}
	
	public TotalFeaturesObject(FeatureObject f1, FeatureObject f2, FeatureObject f3, FeatureObject f4, HashMap<String, ShowObject> perID, HashMap<String, ArrayList<DataObject>> data_during_interval)
	{	
		this.f1_num_of_packets = f1;
		this.f2_bus_load = f2;
		this.f3_number_of_IDs = f3;
		//this.f4_list_of_IDs = f4;
		
		this.f10_IDfeatures = new HashMap<String, ShowObject>(perID);
		this.data_perID = new HashMap<String, ArrayList<DataObject>>(data_during_interval);
	}
	
	public synchronized void set_IDfeatures(HashMap<String, ShowObject> input)
	{
		f10_IDfeatures = input;
	}
	
	public void update_count(){
        synchronized (syncObj0) {
            f0_count++;
        }
    }
	
	public void update_f1(FeatureObject input){
        synchronized (syncObj1) {
            f1_num_of_packets.update(input);
        }
    }
	
	public void update_f2(FeatureObject input){
        synchronized (syncObj2) {
            f2_bus_load.update(input);
        }
    }
	
	public void update_f3(FeatureObject input){
        synchronized (syncObj3) {
            f3_number_of_IDs.update(input);
        }
    }
	
	public void update_f5(FeatureObject input){
        synchronized (syncObj5) {
            f5_frequence_per_ID.update(input);
        }
    }
	
	public void update_f6(FeatureObject input){
        synchronized (syncObj6) {
            f6_similarity.update(input);
        }
    }
	
	
	
	public long getCount()
	{
		synchronized (syncObj0) {
			return this.f0_count;
        }
	}
	
	public FeatureObject get_f1_FeatureObject(){
        synchronized (syncObj1) {
            return new FeatureObject(f1_num_of_packets);
        }
    }
	
	public FeatureObject get_f2_FeatureObject(){
        synchronized (syncObj2) {
        	return new FeatureObject(f2_bus_load);
        }
    }
	
	public FeatureObject get_f3_FeatureObject(){
        synchronized (syncObj3) {
        	return new FeatureObject(f3_number_of_IDs);
        }
    }
	
	public FeatureObject get_f5_FeatureObject(){
        synchronized (syncObj5) {
        	return new FeatureObject(f5_frequence_per_ID);
        }
    }
	
	public FeatureObject get_f6_Similarity(){
        synchronized (syncObj6) {
        	return new FeatureObject(f6_similarity);
        }
    }
	
	private void clearScreen() {  
	    for (int i = 0; i < 40; i++) System.out.println();
	} 

	public synchronized void print(){
		clearScreen();
		
		System.out.println(this.f0_count);
		System.out.println("==============Total Features==============");
		
		f1_num_of_packets.print("Num of Packet");
		f2_bus_load.print("Bus Load");
		f3_number_of_IDs.print("Num of IDs");
		f5_frequence_per_ID.print("ID freq");
		
		System.out.println(" ID  ::: " + String.format("%10s", "freq") + " \t\t " + String.format("%12s", "elapsed_time") + " \t\t " + String.format("%10s", "count") + " \t\t " + String.format("%10s", "length") + " \t\t " + String.format("%-20s", "data") + " \t\t " + String.format("%10s", "timestamp"));
		
		for (Map.Entry<String, ShowObject> entry : f10_IDfeatures.entrySet()) {
		    System.out.println(entry.getKey() + " ::: " + String.format("%10.1f", ((ShowObject)entry.getValue()).freq) + " \t\t " + String.format("%12d", ((ShowObject)entry.getValue()).elapsed_time) + " \t\t " + String.format("%10d", ((ShowObject)entry.getValue()).count) + " \t\t " + String.format("%10d", ((ShowObject)entry.getValue()).data_length) + " \t\t " + String.format("%-20s", ((ShowObject)entry.getValue()).data) + " \t\t " + ((ShowObject)entry.getValue()).timestamp );
		}
	}
	
}
