package emsec.korea.feature_operator;

import emsec.korea.Monitor;
import emsec.korea.data_structure.AnomalObject;
import emsec.korea.data_structure.AnomalQueue;

public class AnomalDetector extends Thread {

	private AnomalQueue f1 = null;
	private AnomalQueue f2 = null;
	private AnomalQueue f3 = null;
	private AnomalQueue f4 = null;
	private AnomalQueue f5 = null;
	private AnomalQueue f6 = null;
	
	private boolean flag_Less_Num_of_Packets = false;
	private boolean flag_More_Num_of_Packets = false;
	private boolean flag_Low_Busload = false;
	private boolean flag_High_Busload = false;
	private boolean flag_Less_Num_of_IDs = false;
	private boolean flag_More_Num_of_IDs = false;
	private boolean flag_Low_Frequence = false;
	private boolean flag_High_Frequence = true;
	private boolean flag_Invalid_ID = true;
	private boolean flag_ID_Sequence = false;
	
	
	public static final int Less_Num_of_Packets_SIG = 0x00000001;
	public static final int More_Num_of_Packets_SIG = 0x00000002;
	
	public static final int Low_Busload_SIG = 0x00000004;
	public static final int High_Busload_SIG = 0x00000008;
	
	public static final int Less_Num_of_IDs_SIG = 0x00000010;
	public static final int More_Num_of_IDs_SIG = 0x00000020;
	
	public static final int Low_Frequence_SIG = 0x00000040;
	public static final int High_Frequence_SIG = 0x00000080;

	public static final int Invalid_ID_SIG = 0x00000100;
	
	public static final int Invalid_ID_Sequence_SIG = 0x00000200;
	
	
	private int interval_gen_SIG = 0;
	
	
	private void build_Interval_gen_rule()
	{
		if(flag_Less_Num_of_Packets)
			interval_gen_SIG |= Less_Num_of_Packets_SIG;
		
		if(flag_More_Num_of_Packets)
			interval_gen_SIG |= More_Num_of_Packets_SIG;
		
		if(flag_Low_Busload)
			interval_gen_SIG |= Low_Busload_SIG;
		
		if(flag_High_Busload)
			interval_gen_SIG |= High_Busload_SIG;
		
		if(flag_Less_Num_of_IDs)
			interval_gen_SIG |= Less_Num_of_IDs_SIG;
		
		if(flag_More_Num_of_IDs)
			interval_gen_SIG |= More_Num_of_IDs_SIG;
		
		if(flag_Low_Frequence)
			interval_gen_SIG |= Low_Frequence_SIG;
		
		if(flag_High_Frequence)
			interval_gen_SIG |= High_Frequence_SIG;
		
		if(flag_Invalid_ID)
			interval_gen_SIG |= Invalid_ID_SIG;
		
		if(flag_ID_Sequence)
	         interval_gen_SIG |= Invalid_ID_Sequence_SIG;
	}
	
	private boolean check_Interval_gen_rule(int value)
	{
		if( (interval_gen_SIG & value) == value )
			return true;
		else
			return false;
	}

	
	public AnomalDetector() 
	{
		f1 = new AnomalQueue();
		f2 = new AnomalQueue();
		f3 = new AnomalQueue();
		f4 = new AnomalQueue();
		f5 = new AnomalQueue();
		f6 = new AnomalQueue();
		
		f1.initialize(10000);
		f2.initialize(10000);
		f3.initialize(10000);
		f4.initialize(10000);
		f5.initialize(10000);
		f6.initialize(10000);
	}
	
	public boolean f1_enqueue(AnomalObject input)
	{
		return f1.enqueue(input);
	}
	
	public boolean f2_enqueue(AnomalObject input)
	{
		return f2.enqueue(input);
	}
	
	public boolean f3_enqueue(AnomalObject input)
	{
		return f3.enqueue(input);
	}
	
	public boolean f4_enqueue(AnomalObject input)
	{
		return f4.enqueue(input);
	}
	
	public boolean f5_enqueue(AnomalObject input)
	{
		return f5.enqueue(input);
	}
	
	public boolean f6_enqueue(AnomalObject input)
	{
		return f6.enqueue(input);
	}
	
	private static int minIndex(long ... numbers) {
		long min = 0;
	    int index = -1;
	    
	    for (int i=0 ; i<numbers.length ; i++)
	    {
	    	if(min == 0 && numbers[i] != 0)
	    	{
	    		min = numbers[i];
	    		index = i;
	    		continue;
	    	}
	    	
	    	if( (numbers[i] < min) && (numbers[i] != 0) )
	    	{
	    		min = numbers[i];
	    		index = i;
	    	}
	    }
	    
	    return index;
	}
	
	public void run()
	{
		build_Interval_gen_rule();
		
		while(true)
		{
//			System.out.println("=========================================");
//			System.out.println("  " + f.format(new Date()) );
//			System.out.println("=========================================");
//			System.out.println("Anomal F1 : " + f1.get_element_count());
//			System.out.println("Anomal F2 : " + f2.get_element_count());
//			System.out.println("Anomal F3 : " + f3.get_element_count());
//			System.out.println("Anomal F4 : " + f4.get_element_count());
//			System.out.println("Anomal F5 : " + f5.get_element_count());
//			System.out.println("=========================================\n");
			
			// fiding early timestamp
			AnomalObject temp = null;
			int index = 0;
			boolean isempty_all_queues = false;
			
			
			index = minIndex( f1.getFirstElement().getStartTime(), f2.getFirstElement().getStartTime(), f3.getFirstElement().getStartTime(), f4.getFirstElement().getStartTime(), f5.getFirstElement().getStartTime(), f6.getFirstElement().getStartTime() );
			switch(index)
			{
				case 0:
					temp = f1.dequeue();
					break;
				case 1:
					temp = f2.dequeue();
					break;
				case 2:
					temp = f3.dequeue();
					break;
				case 3:
					temp = f4.dequeue();
					break;
				case 4:
					temp = f5.dequeue();
					break;
				 case 5:
		            temp = f6.dequeue();
		            break;
				default:
					isempty_all_queues = true;
					break;
			}
			
			if( !isempty_all_queues )
			{
				if( check_Interval_gen_rule(temp.getType()) )
				{
					Monitor.anomaly_interval.update(temp);
				}
			}
		}
			
	}
}
