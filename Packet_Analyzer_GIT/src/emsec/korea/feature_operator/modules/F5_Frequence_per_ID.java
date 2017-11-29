package emsec.korea.feature_operator.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import emsec.korea.Monitor;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ListMap;

public class F5_Frequence_per_ID extends Thread{
	
	private ListMap mainlist;
	private long freq;
	private double duration;
	
	
	public F5_Frequence_per_ID(ListMap mlist, double duration, long freq){
		this.mainlist = mlist;
		this.duration = duration * 1000;
		this.freq = freq;
	}
	
	public FeatureObject compute( List<DataObject> list )
	{
		FeatureObject rlt = new FeatureObject();
		Map<String, Integer> count_per_ID = new HashMap<String, Integer>();
		
		if( !list.isEmpty() )
		{
			long initial_time = list.get(0).ts;
			
			for(int i=0;i<list.size()-1;i++)
			{
				String id = list.get(i).getid();
				
				if( count_per_ID.containsKey(id) )
				{
					count_per_ID.put(id, count_per_ID.get(id) + 1);
				}
				else
				{
					count_per_ID.put(id, 1);
				}
				
				if( list.get(i).ts > (initial_time + this.duration) )
					break;
			}
			
			String listIDfrequence = "";
			Set<String> key = count_per_ID.keySet();

			for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
				String keyName = ((String) iterator.next());
				listIDfrequence = listIDfrequence + keyName + ":";
				float f = ( ((float)duration/1000) / count_per_ID.get(keyName) );
				listIDfrequence = listIDfrequence + f + ",";
				//listIDfrequence = listIDfrequence + String.format("%.1f", f/1000) + ",";

			}
			listIDfrequence.subSequence(0, listIDfrequence.length());
			
			rlt.set(listIDfrequence, initial_time);
		}
		
		return rlt;
	}
	
	public void run()
	{
		byte obj_id = (byte)0x08;
		ArrayList<DataObject> list = null;
		
		while(true)
		{
			while(true)
			{
				list = this.mainlist.get_list(obj_id);
				if(list != null)
					break;
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			FeatureObject result = compute( list );
			this.mainlist.releaseFlag(obj_id);
			Monitor.final_features.update_f5(result);
		
			if(Monitor.final_features.getCount() > Monitor.to_index_for_learning){
				return;
			}
		
			try {
				Thread.sleep(this.freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
