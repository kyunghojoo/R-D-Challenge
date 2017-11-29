package emsec.korea.feature_operator.modules;

import java.util.ArrayList;
import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ListMap;
import emsec.korea.feature_operator.AnomalDetector;
import emsec.korea.ui.MainFrame_1080p;

public class F2_Bus_Load extends Thread
{
	public static final float lower_bound_coeff = (float) 0.7;
	public static final float upper_bound_coeff = (float) 1.5;
	
	private ListMap mainlist;
	private long freq;
	private long duration;
	private int CAN_Speed = 500;

	public F2_Bus_Load(ListMap mlist, long duration, long freq)
	{
		this.mainlist = mlist;
		this.freq = freq;
		this.duration = duration * 1000;
		this.freq = freq;
	}

	public FeatureObject compute(ArrayList<DataObject> list)
	{
		FeatureObject rlt = new FeatureObject();
		
		if( !list.isEmpty() )
		{
			long end, initial = list.get(0).ts;
			float busload;
			int i, size = 0;
			
			for (i = 1; i < list.size()-1; i++)
			{
				if( list.get(i).ts <= (initial + this.duration) )
					size = size + 44 + (Integer.parseInt(list.get(i).getdata_length()) * 8);
				else
					break;
			}
			end = list.get(i-1).ts;
			busload = (float) (((size / (CAN_Speed * 1024 * ((end - initial)/1000000.0))) * 100));					
			
			rlt.set(Float.toString(busload), initial);
		}

		return rlt;
	}
	
	public void run()
	{
		byte obj_id = (byte)0x02;
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
				if( (Float.parseFloat(result.getValue()) < (Monitor.normal_condition.f2_bus_load * F2_Bus_Load.lower_bound_coeff)))
				{
					//System.out.println("f2 detedted");
					MainFrame_1080p.setSuspicious(obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), result.getTimestamp() + this.duration, result.getValue(), AnomalDetector.Low_Busload_SIG);
					while(true)
					{
						if(Monitor.analyzer.f2_enqueue(temp))
							break;
						
						System.out.println("Process is delayed by Anomal Queue F2");
					}
					
				}
				else if(  (Float.parseFloat(result.getValue()) > (Monitor.normal_condition.f2_bus_load * F2_Bus_Load.upper_bound_coeff)) )
				{
					//System.out.println("f2 detedted");
					MainFrame_1080p.setSuspicious(obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), result.getTimestamp() + this.duration, result.getValue(), AnomalDetector.High_Busload_SIG);
					while(true)
					{
						if(Monitor.analyzer.f2_enqueue(temp))
							break;
						
						System.out.println("Process is delayed by Anomal Queue F2");
					}
				}
				else
					MainFrame_1080p.releaseSuspicious(obj_id);
			}
			Monitor.final_features.update_f2(result);

			try {
				Thread.sleep(this.freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
