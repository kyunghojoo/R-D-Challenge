package emsec.korea.feature_operator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.Queue;
import emsec.korea.data_structure.ShowObject;
import emsec.korea.ui.MainFrame_1080p;

public class Worker_using_queue extends Thread {
	
	private Queue queue = null;

	private HashMap<String, ShowObject> f10 = null;
	
	public Worker_using_queue(Queue queue)
	{
		this.queue = queue;

		f10 = new HashMap<String, ShowObject>();
		
		Monitor.final_features.set_IDfeatures(f10);
	}
	
	private long count = 0;
	private int loc = 0;
	
	public void run()
	{	
		while(true)
		{
			DataObject obj = this.queue.dequeue();
			if(obj != null)
			{
				Monitor.final_features.update_count();
				MainFrame_1080p.updateCanRxField(" " + (count++) + " : " + obj.toString());
				
				//update_features_per_ID(obj);
				
				if(Monitor.normal_condition.isready)
				{
					byte obj_id1 = (byte)0x20;
					byte obj_id2 = (byte)0x40;
					//F4
					if( !Monitor.normal_condition.f4_list_of_IDs.contains(obj.getid()) )
					{
						
						//System.out.println("f4 detedted");
						MainFrame_1080p.setSuspicious(obj_id1);
						
						AnomalObject temp = new AnomalObject(obj.ts, obj.ts, obj.getid(), AnomalDetector.Invalid_ID_SIG); //"Invalid ID (" + obj.getid()+")");
						while(true)
						{
							if(Monitor.analyzer.f4_enqueue(temp))
								break;
							
							System.out.println("Process is delayed by Anomal Queue F4");
						}
						
						MainFrame_1080p.invID_noti.updateCanRxField(obj.toString());
						MainFrame_1080p.invID_noti.showframe();
					}
					//F5
					else
					{
						update_features_per_ID(obj);
						
						if( f10.get(obj.getid()).elapsed_time < (Monitor.normal_condition.f5_frequence_per_ID.get(obj.getid())*1000/2) )
						{
							//System.out.println("f5 detedted");
							MainFrame_1080p.setSuspicious(obj_id2);
							
							AnomalObject temp = new AnomalObject(obj.ts, obj.ts, obj.getid(), AnomalDetector.High_Frequence_SIG); //"High frequence (" + obj.getid() + ")");
							while(true)
							{
								if(Monitor.analyzer.f5_enqueue(temp))
									break;
								
								System.out.println("Process is delayed by Anomal Queue F5");
							}
						}
						else if( (Monitor.normal_condition.f5_frequence_per_ID.get(obj.getid()) * 100000) < f10.get(obj.getid()).elapsed_time ) 
						{
							//System.out.println("f5 detedted");
							MainFrame_1080p.setSuspicious(obj_id2);
							
							AnomalObject temp = new AnomalObject(obj.ts, obj.ts, obj.getid(), AnomalDetector.Low_Frequence_SIG); //"Low frequence (" + obj.getid() + ")");
							while(true)
							{
								if(Monitor.analyzer.f5_enqueue(temp))
									break;
								
								System.out.println("Process is delayed by Anomal Queue F5");
							}
						}
						else
						{
							MainFrame_1080p.releaseSuspicious(obj_id1);
							MainFrame_1080p.releaseSuspicious(obj_id2);
						}
					}
					
				}
				else
				{
					update_features_per_ID(obj);
					
					if( count >= (Monitor.to_index_for_learning - Monitor.size_of_base_similarity) )
					{
						if(loc < Monitor.size_of_base_similarity)
							Monitor.normal_condition.similarity_base[loc++] = Short.parseShort(obj.getid(), 16);
					}
				}
				
			}

		}
	}
	
	public void update_features_per_ID(DataObject item)
	{
		String id = item.getid();
		
		f10.put(id, f10.containsKey(id) ? f10.get(id).update(item) : new ShowObject(item) );
		
		Set<String> key = this.f10.keySet();
		for (Iterator<String> iterator = key.iterator(); iterator.hasNext();) {
			String keyName = ((String) iterator.next());
			if(!keyName.equals(id)){
				f10.put(keyName, f10.get(keyName).updateElapTime(item.ts) );
			}
		}
	}
}