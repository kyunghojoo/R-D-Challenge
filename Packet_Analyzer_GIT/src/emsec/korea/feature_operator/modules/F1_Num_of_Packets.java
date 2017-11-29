package emsec.korea.feature_operator.modules;

import java.util.ArrayList;
import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ListMap;
import emsec.korea.feature_operator.AnomalDetector;
import emsec.korea.ui.MainFrame_1080p;

public class F1_Num_of_Packets extends Thread
{
	
	private ListMap mainlist;
	private long duration = 1000000;
	private long freq;	

	public F1_Num_of_Packets(ListMap mlist, long duration, long freq){
		mainlist = mlist;
		this.duration = duration * 1000;
		this.freq = freq;
	}
	
	public FeatureObject compute(ArrayList<DataObject> list)
	{
		FeatureObject rlt = new FeatureObject();

		if( !list.isEmpty() )
		{
			long initial_time = list.get(0).ts;
			int result = 1, i;
			
			for (i = 1; i < list.size(); i++) { // faster search 할 수 있도록 수정!
				if(list.get(i).ts <= (initial_time + this.duration) )
					result++;
				else
					break;
			}
			
			rlt.set(Integer.toString(result), initial_time);
		}
		
		return rlt;
	}

	public void run()
	{
		byte obj_id = (byte)0x01;
		ArrayList<DataObject> list = null;
		
		while(true)
		{
			while(true)
			{
				list = this.mainlist.get_list(obj_id);
				if(list != null)
					break;
				
				try {
					Thread.sleep(Monitor.list_get_interval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			FeatureObject result = compute( list );
			this.mainlist.releaseFlag(obj_id);
			
			if(Monitor.normal_condition.isready)
			{
				if( (Float.parseFloat(result.getValue()) < (Monitor.normal_condition.f1_number_of_packets * 0.3)))
				{
					//System.out.println("f1 detedted");
					MainFrame_1080p.setSuspicious(obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), result.getTimestamp() + this.duration, result.getValue(), AnomalDetector.Less_Num_of_Packets_SIG);
					while(true)
					{
						if(Monitor.analyzer.f1_enqueue(temp))
							break;
						
						System.out.println("Process is delayed by Anomal Queue F1");
					}
				}
				else if( (Float.parseFloat(result.getValue()) > (Monitor.normal_condition.f1_number_of_packets * 2)) )
				{
					//System.out.println("f1 detedted");
					MainFrame_1080p.setSuspicious(obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), result.getTimestamp() + this.duration, result.getValue(), AnomalDetector.More_Num_of_Packets_SIG);
					while(true)
					{
						if(Monitor.analyzer.f1_enqueue(temp))
							break;
						
						System.out.println("Process is delayed by Anomal Queue F1");
					}
				}
				else
					MainFrame_1080p.releaseSuspicious(obj_id);
			}
			Monitor.final_features.update_f1(result);	
			
			try {
				Thread.sleep(this.freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
	}
}
