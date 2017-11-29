package emsec.korea.data_structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import emsec.korea.Monitor;

public class NormalConditionObject {
	
	public float f1_number_of_packets=0;
	public float f2_bus_load = 0;
	public float f3_number_of_IDs;
	public ArrayList<String> f4_list_of_IDs = new ArrayList<String>();
	public Map<String, Float> f5_frequence_per_ID = new HashMap<String, Float>();
	
	public short[] similarity_base = new short[Monitor.size_of_base_similarity];
	
	int count;
	public volatile boolean isready = false;
	
	public NormalConditionObject(){
		this.count = 0;
	}
	
	public void update(TotalFeaturesObject input)
	{
		this.count++;
		
		f1_number_of_packets = (f1_number_of_packets* (((float)this.count-1)/(float)this.count) + Float.parseFloat(input.get_f1_FeatureObject().getValue())*(1/(float)this.count));
		f2_bus_load = f2_bus_load * (((float)this.count-1)/(float)this.count) + Float.parseFloat(input.get_f2_FeatureObject().getValue())*(1/(float)this.count);
		f3_number_of_IDs = (f3_number_of_IDs* (((float)this.count-1)/(float)this.count) + Float.parseFloat(input.get_f3_FeatureObject().getValue())*(1/(float)this.count));
				
		String[] sep1 = input.get_f5_FeatureObject().getValue().split(",{1}");		
		for(int i = 0 ; i<sep1.length ; i++){
			String[] sep2 = sep1[i].split(":{1}");
			if(Float.parseFloat(sep2[1]) != Float.POSITIVE_INFINITY)
			{
				if(this.f5_frequence_per_ID.containsKey(sep2[0])){
					this.f5_frequence_per_ID.put(sep2[0], (float)(this.f5_frequence_per_ID.get(sep2[0])*(count-1) + Float.parseFloat(sep2[1]))/count);
				}else{
					this.f5_frequence_per_ID.put(sep2[0], Float.parseFloat(sep2[1]));
				}
			}
		}
	}
	
	public void update_list_of_ID(ArrayList<String> ids)
	{
		this.f4_list_of_IDs = ids; 
	}
	
	public void print(){
		System.out.println("number of packets "+f1_number_of_packets);
		System.out.println("bus load "+f2_bus_load);
		System.out.println("number of IDs "+f3_number_of_IDs);
		System.out.println(f4_list_of_IDs.size()+" "+"list of IDs " + f4_list_of_IDs);
		System.out.println("frequence per ID " + f5_frequence_per_ID);
		System.out.println("Similarity Base [0] : " + String.format("%04X", similarity_base[0]) + ", [" + Monitor.size_of_base_similarity + "] : " + String.format("%04X", similarity_base[Monitor.size_of_base_similarity-1]));
	}
}