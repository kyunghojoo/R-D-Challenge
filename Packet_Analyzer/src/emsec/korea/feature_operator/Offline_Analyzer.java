package emsec.korea.feature_operator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import emsec.korea.Monitor;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.FeatureObject;
import emsec.korea.data_structure.ShowObject;
import emsec.korea.data_structure.TotalFeaturesObject;

public class Offline_Analyzer extends Thread {

	private ArrayList<DataObject> window;
	
	private long start, target, end;
	private long interval = Monitor.timeInterval_in_millisec * 1000;
	private int CAN_Speed = 500;
	
	private HashMap<String, ShowObject> perID_feature = null;
	private ArrayList<TotalFeaturesObject> total_list = null;
	
	private HashMap<String, ArrayList<DataObject>> data_perID_during_interval = null;
	
	public volatile boolean isEnd = false;
	public volatile int progress_count = 0;
	
	public Offline_Analyzer(long start, long end)
	{
		this.target = start;
		this.start = start - interval;
		this.end = end;
		
		window = new ArrayList<DataObject>();
		
		perID_feature = new HashMap<String, ShowObject>();
		
		total_list = new ArrayList<TotalFeaturesObject>();
	}
	
	private void update_per_ID_features(DataObject item)
	{
		String id = item.getid();
		
		perID_feature.put(id, perID_feature.containsKey(id) ? perID_feature.get(id).update(item) : new ShowObject(item) );
	}
	
	private FeatureObject compute_F1()
	{
		return new FeatureObject(Integer.toString(window.size()), target);
	}
	
	public FeatureObject compute_F2()
	{
		int i, size = 0;
		double busload;
		
		for (i = 0; i < window.size(); i++)
		{
			size = size + 44 + (Integer.parseInt(window.get(i).getdata_length()) * 8);
		}
		
		busload = ((((double)size / (CAN_Speed * 1024 * ((double)Monitor.timeInterval_in_millisec/1000))) * 100));
		
		return new FeatureObject(Double.toString(busload), target);
	}
	
	public FeatureObject[] compute_F3F4()
	{
		int i;
		HashSet<String> set = new HashSet<String>();
		
		for (i = 0; i < window.size(); i++)
		{
			if (!set.contains( window.get(i).getid() )) {
				set.add( window.get(i).getid() );
			}
		}
		
		FeatureObject[] temp = new FeatureObject[2];
		
		temp[0] = new FeatureObject(Integer.toString(set.size()), target); 
		
		String listID = set.toString();
		temp[1] = new FeatureObject(listID, target); 
		
		return temp;
	}
	
	public void computeFF()
	{
		int i;
		data_perID_during_interval = new HashMap<String, ArrayList<DataObject>>();
		
		for (i = 0; i < window.size(); i++)
		{
			DataObject target = window.get(i);
			
			if( data_perID_during_interval.containsKey(target.getid()) )
			{
				data_perID_during_interval.get(target.getid()).add(target);
			}
			else
			{
				ArrayList<DataObject> temp = new ArrayList<DataObject>();
				temp.add(target);
				
				data_perID_during_interval.put(target.getid(), temp);
			}
		}
	}
	
	
	private void calculate_all_list_feature()
	{	
		FeatureObject f1 = compute_F1();
		FeatureObject f2 = compute_F2();
		FeatureObject[] f3f4 = compute_F3F4();
		
		computeFF();	
		
		
		HashMap<String, ShowObject> new_perID_feature = new HashMap<String, ShowObject>();
		for (Map.Entry<String, ShowObject> entry : perID_feature.entrySet()) {
			ShowObject temp = new ShowObject( ((ShowObject)entry.getValue()) );
			new_perID_feature.put( (String)entry.getKey(), temp );
		}
		
		HashMap<String, ArrayList<DataObject>> new_data_perID_during_interval = new HashMap<String, ArrayList<DataObject>>();
		for (Map.Entry<String, ArrayList<DataObject>> entry : data_perID_during_interval.entrySet())
		{
			ArrayList<DataObject> temp = new ArrayList<DataObject>();
			for(int i=0;i<entry.getValue().size();i++)
			{
				temp.add( entry.getValue().get(i) );
			}
			
			new_data_perID_during_interval.put( (String)entry.getKey(), temp);
		}		
		
		//TotalFeaturesObject new_one = new TotalFeaturesObject( f1, f2, f3f4[0], f3f4[1], (HashMap<String, ShowObject>) new_perID_feature );
		TotalFeaturesObject new_one = new TotalFeaturesObject( f1, f2, f3f4[0], f3f4[1], (HashMap<String, ShowObject>) new_perID_feature, (HashMap<String, ArrayList<DataObject>>) new_data_perID_during_interval );
		
		total_list.add(new_one);
	}
	
	public void run()
	{
		String data;
		String ts = null, id = null, type = null, length = null, hexdata = null;
		BufferedReader reader = null;
		
		//System.out.println(" > Log Data Reading Started...");
		
		try {
			reader = new BufferedReader(new FileReader(Monitor.rawPacket_file_loc));

			while(true)
			{
				data= reader.readLine();
				if(data != null)
				{
					String[] sep1 = data.split(" : {1}| // {1}");
					
					ts = sep1[1].trim();
					id = sep1[3].trim();
					type = sep1[5].trim();
					length = sep1[7].trim();
					hexdata = sep1[9].trim();
					
					DataObject obj = new DataObject(ts, id, type, length, hexdata);
					obj.ts = Long.parseLong(ts);
					
					update_per_ID_features(obj);
					
					if( (Long.compare(obj.ts, start) >= 0) && (Long.compare(obj.ts, target) < 0) )
					{
						window.add(obj);
					}
					else if( Long.compare(obj.ts, target) > 0 )
					{
						calculate_all_list_feature();
						
						window.clear();
						
						if( (Long.compare(obj.ts, end) > 0) )
							break;
						
						start = start + interval;
						target = target + interval;
						window.add(obj);
					}
					
					progress_count = ((int)((obj.ts - Monitor.veryFirstTime)/1000000) + 2);
				}
				else
					break;
			}
			
			reader.close();
			
			//System.out.println(" > Log Data Processing Completed...");
			
			isEnd = true;
            
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<TotalFeaturesObject> get_total_list()
	{
		return total_list;
	}
}
