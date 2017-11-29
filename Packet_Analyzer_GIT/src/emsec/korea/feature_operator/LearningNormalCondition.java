package emsec.korea.feature_operator;

import java.util.ArrayList;
import java.util.Map;

import emsec.korea.Monitor;
import emsec.korea.data_structure.ShowObject;

public class LearningNormalCondition extends Thread {

	private long from_index;
	private long to_index;
	
	public LearningNormalCondition(long from_index, long to_index){
		this.from_index = from_index;
		this.to_index = to_index;
	}
	
	public void run()
	{
		while(true){
			
			long count = Monitor.final_features.getCount();
			
			if( (count >= this.from_index) && (count <= this.to_index) )
			{
				Monitor.normal_condition.update(Monitor.final_features);
			}
			else if( count > this.to_index )
			{
				ArrayList<String> ids = new ArrayList<String>();
				
				for (Map.Entry<String, ShowObject> entry : Monitor.final_features.f10_IDfeatures.entrySet()) {
					ids.add((String)entry.getKey());
				}
				
				Monitor.normal_condition.update_list_of_ID(ids);
				
				Monitor.normal_condition.isready = true;
				
				Monitor.normal_condition.f4_list_of_IDs.add("0392");
				Monitor.normal_condition.f5_frequence_per_ID.put("0392", (float)20.0);
				
				
				
				
				
				
				
				System.out.println("");
				System.out.println("==================================================================");
				System.out.println("Learning is done.");
				Monitor.normal_condition.print();
				System.out.println("==================================================================");
				System.out.println("");
				
				break;
			}
		}
	}

	
}