package emsec.korea.feature_operator.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ListMap;
import emsec.korea.feature_operator.AnomalDetector;
import emsec.korea.ui.MainFrame_1080p;

public class F3_Num_of_IDs extends Thread{

	private ListMap mainlist;
	private long freq;
	private long duration;
	
	public F3_Num_of_IDs(ListMap mlist, long duration, long freq){
		this.mainlist = mlist;
		this.duration = duration * 1000;
		this.freq = freq;
	}
	
	public FeatureObject compute(List<DataObject> list)
	{
		FeatureObject rlt = new FeatureObject();
		HashSet<String> set = new HashSet<String>();
		
		if( !list.isEmpty() )
		{
			long initial = list.get(0).ts;
			
			for(int i=0;i<list.size()-1;i++)
			{
				if( list.get(i).ts <= (initial + this.duration) )
				{
					if ( !set.contains(list.get(i).getid()) )
					{
						set.add( list.get(i).getid() );
					}
				}
				else
					break;
			}
			
			rlt.set(Integer.toString(set.size()), initial);
		}
		
		return rlt;
	}
	
	public void run()
	{
		byte obj_id = (byte)0x04;
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
				if( (Float.parseFloat(result.getValue()) < (Monitor.normal_condition.f3_number_of_IDs * 0.99)) )
				{
					//System.out.println("f3 detedted");
					MainFrame_1080p.setSuspicious(obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), result.getTimestamp() + this.duration, result.getValue(), AnomalDetector.Less_Num_of_IDs_SIG);
					while(true)
					{
						if(Monitor.analyzer.f3_enqueue(temp))
							break;
						
						System.out.println("Process is delayed by Anomal Queue F3");
					}
				}
				else if(  (Float.parseFloat(result.getValue()) > (Monitor.normal_condition.f3_number_of_IDs * 1.01)) )
				{
					//System.out.println("f3 detedted");
					MainFrame_1080p.setSuspicious(obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), result.getTimestamp() + this.duration, result.getValue(), AnomalDetector.More_Num_of_IDs_SIG);
					while(true)
					{
						if(Monitor.analyzer.f3_enqueue(temp))
							break;
						
						System.out.println("Process is delayed by Anomal Queue F3");
					}
				}
				else
					MainFrame_1080p.releaseSuspicious(obj_id);
			}
			Monitor.final_features.update_f3(result);		

			try {
				Thread.sleep(this.freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
}
