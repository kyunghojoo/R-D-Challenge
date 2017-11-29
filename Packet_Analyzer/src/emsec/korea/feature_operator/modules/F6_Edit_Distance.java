package emsec.korea.feature_operator.modules;

import java.util.ArrayList;
import java.util.List;
import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ListMap;
import emsec.korea.feature_operator.AnomalDetector;
import emsec.korea.ui.MainFrame_1080p;

public class F6_Edit_Distance extends Thread{

	private ListMap mainlist;
	private long freq;
	//private long duration;
	
	private final byte obj_id = (byte)0x10;
	
	public F6_Edit_Distance(ListMap mlist, long duration, long freq){
		this.mainlist = mlist;
		//this.duration = duration * 1000;
		this.freq = freq;
	}
	
	private int min(int x, int y, int z)
    {
        if( (x <= y) && (x <= z) )
        	return x;
        
        if( (y <= x) && (y <= z) )
        	return y;
        
        return z;
    }
 
    private int editDistDP(short[] str1, short[] str2, int m, int n)
    {
        int dp[][] = new int[m+1][n+1];
      
        for (int i=0; i<=m; i++)
        {
            for (int j=0; j<=n; j++)
            {
                if (i==0)
                    dp[i][j] = j;
                else if (j==0)
                    dp[i][j] = i;
                else if (str1[i-1] == str2[j-1])
                    dp[i][j] = dp[i-1][j-1];
                else
                    dp[i][j] = 1 + min(dp[i][j-1], dp[i-1][j], dp[i-1][j-1]);
            }
        }
  
        return dp[m][n];
    }
	
	public FeatureObject compute(List<DataObject> list, long[] lasttime)
	{
		int i;
		FeatureObject rlt = new FeatureObject();
		short[] target = new short[Monitor.size_of_target_similarity];
		int target_len = 0, final_distance = 0, cur_distance = 0;
		
		if( !list.isEmpty() )
		{
			long initial = list.get(0).ts;
			
			for(i=0;i<list.size()-1;i++)
			{
				if( i < Monitor.size_of_target_similarity)
				{
					target[i] = Short.parseShort(list.get(i).getid(), 16);
				}
				else
					break;
			}
			
			lasttime[0] = list.get(i-1).ts;
			
			this.mainlist.releaseFlag(this.obj_id);
			
			target_len = i;
			final_distance = target_len;
			
			short[] base = new short[target_len];

	        for(i=0; (i + target_len) <= Monitor.size_of_base_similarity; i++)
	        {
	        	System.arraycopy(Monitor.normal_condition.similarity_base, i, base, 0, target_len);
	        	   	
	        	cur_distance = editDistDP(target, base, target_len, target_len);
	        	
	        	if(cur_distance < final_distance)
	        		final_distance = cur_distance;
	        	
	        	if(final_distance < Monitor.similarity_minimum_threshold)
	        		break;
	        }
	        
	        rlt.set(Integer.toString(final_distance), initial);
		}
		
		return rlt;
	}
	
	public void run()
	{
		ArrayList<DataObject> list = null;
		
		while(true)
		{
			if(Monitor.normal_condition.isready)
			{
				while(true)
				{
					list = this.mainlist.get_list(this.obj_id);
					if(list != null)
						break;
					
					try {
						Thread.sleep(Monitor.list_get_interval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
				long[] lasttime = new long[1];
				FeatureObject result = compute( list, lasttime );
				
				if (Integer.parseInt(result.getValue()) > Monitor.similarity_minimum_threshold)
				{
					// System.out.println("f6 detedted");
					MainFrame_1080p.setSuspicious(this.obj_id);
					
					AnomalObject temp = new AnomalObject(result.getTimestamp(), lasttime[0], result.getValue(), AnomalDetector.Invalid_ID_Sequence_SIG);
					while (true) {
						if (Monitor.analyzer.f6_enqueue(temp))
							break;

						System.out.println("Process is delayed by Anomal Queue F6");
					}
					// System.out.println("Similarity Detect Time : " + result.getTimestamp() + " // Distance : " + result.getValue());
				}
				else
					MainFrame_1080p.releaseSuspicious(this.obj_id);
				
				Monitor.final_features.update_f6(result);
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
