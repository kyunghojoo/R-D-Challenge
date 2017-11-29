package emsec.korea;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import emsec.korea.data_structure.AnomalyIntervalObject;
import emsec.korea.data_structure.ListMap;
import emsec.korea.data_structure.NormalConditionObject;
import emsec.korea.data_structure.Queue;
import emsec.korea.data_structure.TotalFeaturesObject;
import emsec.korea.feature_operator.AnomalDetector;
import emsec.korea.feature_operator.FeatureUpdate;
import emsec.korea.feature_operator.LearningNormalCondition;
import emsec.korea.packet_receiver.Receiver;

public class Monitor {
	
	//---------------------------------------------------------
	// Config Parameters
	//---------------------------------------------------------
	private int maxSize_of_Container = 10000;
	private static boolean switch_to_Online = false;
	
	public static final long timeInterval_in_millisec = 1000; // millisecond
	public static final long from_index_for_learning = 10000;
	public static final long to_index_for_learning = 20000;

	private static String offline_can_data_file_loc = "CAN_Dataset.txt"; // Full path
	public static String rawPacket_file_loc = "RawPacket_";
	
	public static final long list_get_interval = 10;
	public static final int size_of_base_similarity = 5000; // should be less than (to_index_for_learning - from_index_for_learning)
	public static final int size_of_target_similarity = 100;
	public static final int similarity_minimum_threshold = 80;
	//---------------------------------------------------------
	
	private final String config_filename = "config.def";
	private BufferedReader conf_reader = null;
	
	public ListMap list_board = new ListMap();
	public Queue queue_board = new Queue();
		
	public static TotalFeaturesObject final_features;
	public static NormalConditionObject normal_condition;
	public static AnomalyIntervalObject anomaly_interval;
	
	public static long veryFirstTime = 0;
	
	public static AnomalDetector analyzer = new AnomalDetector();
	
	private Receiver rcv = null;	
	
	public static void setOnlineMode(boolean on)
	{
		switch_to_Online = on;
	}
	
	public static void setDataSetFile(String loc)
	{
		offline_can_data_file_loc = loc;
	}
	
	public Monitor()
	{
		String pwd = System.getProperty("user.dir");
		String config_file_loc = pwd + "\\" + config_filename;
		
		// Load conf from Configuration file
		try
		{
			conf_reader = new BufferedReader(new FileReader(config_file_loc));
			
			// Do something....
			
			
		} 
		catch (FileNotFoundException e)
		{
			System.out.println("Config file not exist... Setting up with default values...\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				if(conf_reader != null)
					conf_reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		list_board.initialize(maxSize_of_Container);
		queue_board.initialize(maxSize_of_Container);
		
		final_features = new TotalFeaturesObject();
		normal_condition = new NormalConditionObject();
		anomaly_interval = new AnomalyIntervalObject();

		rcv = new Receiver(switch_to_Online, offline_can_data_file_loc, list_board, queue_board);
		rcv.start();
		
		LearningNormalCondition learning = new LearningNormalCondition(from_index_for_learning, to_index_for_learning);
		FeatureUpdate feature = new FeatureUpdate(list_board, queue_board);
		
		learning.start();
		feature.update();
		analyzer.start();
	}
	
	public void Packet_receive_start() throws IOException
	{
		if(rcv != null)
		{
			rcv.doAction(2);
		}
	}
	
	public void Packet_receive_Pause() throws IOException
	{
		if(rcv != null)
		{
			rcv.doAction(1);
		}
	}
	
	public boolean isReceiverAlive()
	{
		if(rcv != null)
			return rcv.isConAlive();
		else
			return false;
	}
	
	public void show_all_features()
	{
		final_features.print();	
		anomaly_interval.print();
	}
}
