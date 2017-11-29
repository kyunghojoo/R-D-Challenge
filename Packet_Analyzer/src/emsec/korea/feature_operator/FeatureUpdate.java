package emsec.korea.feature_operator;

import emsec.korea.data_structure.ListMap;
import emsec.korea.data_structure.Queue;
import emsec.korea.feature_operator.modules.F1_Num_of_Packets;
import emsec.korea.feature_operator.modules.F2_Bus_Load;
import emsec.korea.feature_operator.modules.F3_Num_of_IDs;
import emsec.korea.feature_operator.modules.F5_Frequence_per_ID;
import emsec.korea.feature_operator.modules.F6_Edit_Distance;

public class FeatureUpdate {
	
	private ListMap list = null;
	private Queue queue = null;
	
	private long f1_freq = 10;
	private long f2_freq = 10;
	private long f3_freq = 10;
	private long f5_freq = 10;
	private long f6_freq = 10;
	
	private F1_Num_of_Packets f1;
	private F2_Bus_Load f2;
	private F3_Num_of_IDs f3;
	private F5_Frequence_per_ID f5;
	private F6_Edit_Distance f6;
	
	private Worker_using_queue wuq; 
	
	public FeatureUpdate(ListMap list, Queue queue)
	{
		this.list = list;
		this.queue = queue;
		
		f1 = new F1_Num_of_Packets(this.list, 1000, this.f1_freq); // 1000millisec 동안 패킷 수
		f2 = new F2_Bus_Load(this.list, 1000, this.f2_freq); // 10000millisec 동안의 버스로
		f3 = new F3_Num_of_IDs(this.list, 1000, this.f3_freq);
		f5 = new F5_Frequence_per_ID(this.list, 2000, this.f5_freq);
		f6 = new F6_Edit_Distance(this.list, 1000, this.f6_freq);
		
		this.wuq = new Worker_using_queue(this.queue);
	}
	
	public void update(){
		f1.start();
		f2.start();
		f3.start();
		f5.start();
		f6.start();
		
		this.wuq.start();
	}
}
