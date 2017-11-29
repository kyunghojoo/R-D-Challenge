package emsec.korea.data_structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import emsec.korea.feature_operator.AnomalDetector;

public class AnomalObject {
	
	private long start = 0;
	private long end = 0;
	private String value = "";
	private int anomal_type = 0;
	
	private String invalid_IDs = "";
	private String LowFrequency_IDs = "";
	private String HighFrequency_IDs = "";
	
	private String attack_category = "Unknown";
	
	private Map<String, Integer> detailed_anomal = new HashMap<String, Integer>();
	
	private ArrayList<String> IDs_in_order = new ArrayList<String>();
	
	
	public AnomalObject()
	{
		
	}
	
	public AnomalObject(AnomalObject input)
	{
		start = input.start;
		end = input.end;
		value = input.value;
		anomal_type = input.anomal_type;
		
		attack_category = input.attack_category;
	}
	
	public AnomalObject(long start, long end, String value, int anomal_type)
	{
		this.start = start;
		this.end = end;
		this.value = value;
		this.anomal_type = anomal_type;
	}
	
	public String gen_detailed_anomal_string()
	{
		String rlt = "";
		switch(anomal_type)
		{
			case AnomalDetector.Less_Num_of_Packets_SIG:
				rlt = "Less number of packets";
				break;
			case AnomalDetector.More_Num_of_Packets_SIG:
				rlt = "More number of packets";
				break;
			case AnomalDetector.Low_Busload_SIG:
				rlt = "Low bus load";
				break;
			case AnomalDetector.High_Busload_SIG:
				rlt = "High bus load";
				break;
			case AnomalDetector.Less_Num_of_IDs_SIG:
				rlt = "Less number of IDs";
				break;
			case AnomalDetector.More_Num_of_IDs_SIG:
				rlt = "More number of IDs";
				break;
			case AnomalDetector.Low_Frequence_SIG:
				rlt = "Low frequence (" + this.value + ")";
				break;
			case AnomalDetector.High_Frequence_SIG:
				rlt = "High frequence (" + this.value + ")";
				break;
			case AnomalDetector.Invalid_ID_SIG:
				rlt = "Invalid ID (" + this.value + ")";
				break;
			case AnomalDetector.Invalid_ID_Sequence_SIG:
	            rlt = "Invalid ID Sequence";
	            break;  
		}
		
		return rlt;
	}
	
	public void set(AnomalObject input)
	{
		this.start = input.start;
		this.end = input.end;
		this.value = input.value;
		this.anomal_type = input.anomal_type;
		this.detailed_anomal.put( gen_detailed_anomal_string(), 1 );
	}
	
	public synchronized void update(AnomalObject input)
	{
		this.updateTime(input);
		this.updateAnomalType( input );
	}
	
	public synchronized void updateTime(AnomalObject input)
	{
		if(this.start > input.start)
			this.start = input.start;
		
		if(this.end < input.end)
			this.end = input.end;
	}
	
	public synchronized void updateAnomalType(AnomalObject input)
	{
		this.anomal_type = this.anomal_type | input.anomal_type;
		
		if( input.anomal_type == AnomalDetector.Invalid_ID_SIG )
		{
			if( !this.invalid_IDs.contains(input.getValue()) )
				this.invalid_IDs += ( input.getValue() + ", " );
		}
		else if( input.anomal_type == AnomalDetector.Low_Frequence_SIG )
		{
			if( !this.LowFrequency_IDs.contains(input.getValue()) )
				this.LowFrequency_IDs += ( input.getValue() + ", " );
		}
		else if( input.anomal_type == AnomalDetector.High_Frequence_SIG )
		{
			if( !this.HighFrequency_IDs.contains(input.getValue()) )
				this.HighFrequency_IDs += ( input.getValue() + ", " );
		}
		
		
		// IDs-in-order
		// parsing type
		if ( input.anomal_type == AnomalDetector.High_Frequence_SIG )
		{
			if (this.IDs_in_order.size() == 0) {
				this.IDs_in_order.add("HF-(" + input.getValue() + ")-1");
			}
			else
			{
				String[] wordss = this.IDs_in_order.get(this.IDs_in_order.size() - 1).split("-{1}");

				if ( wordss[1].equals("(" + input.getValue() + ")") )
				{
					String str = wordss[0] + "-" + wordss[1] + "-" + (Integer.parseInt(wordss[2]) + 1);
					this.IDs_in_order.set(IDs_in_order.size() - 1, str);
				}
				else
				{
					this.IDs_in_order.add("HF-(" + input.getValue() + ")-1");
				}

			}
		}
		else if ( input.anomal_type == AnomalDetector.Low_Frequence_SIG )
		{
			if (this.IDs_in_order.size() == 0) {
				this.IDs_in_order.add("LF-(" + input.getValue() + ")-1");
			} else {
				String[] wordss = this.IDs_in_order.get(this.IDs_in_order.size() - 1).split("-{1}");

				if ( wordss[1].equals("(" + input.getValue() + ")") )
				{
					String str = wordss[0] + "-" + wordss[1] + "-" + (Integer.parseInt(wordss[2]) + 1);
					this.IDs_in_order.set(IDs_in_order.size() - 1, str);
				}
				else
				{
					this.IDs_in_order.add("LF-(" + input.getValue() + ")-1");
				}

			}
		}
		else if ( input.anomal_type == AnomalDetector.Invalid_ID_SIG )
		{
			if (this.IDs_in_order.size() == 0) {
				this.IDs_in_order.add("InvID-(" + input.getValue() + ")-1");
			} else {
				String[] wordss = this.IDs_in_order.get(this.IDs_in_order.size() - 1).split("-{1}");

				if ( wordss[1].equals("(" + input.getValue() + ")") )
				{
					String str = wordss[0] + "-" + wordss[1] + "-" + (Integer.parseInt(wordss[2]) + 1);
					this.IDs_in_order.set(IDs_in_order.size() - 1, str);
				} else {
					this.IDs_in_order.add("InvID-(" + input.getValue() + ")-1");
				}
			}
		}
		
		String key = input.gen_detailed_anomal_string();
		if( !detailed_anomal.containsKey(key) )
		{
			detailed_anomal.put(key, 1);
		}
		else
		{
			detailed_anomal.put(key, detailed_anomal.get(key) + 1);
		}
		
		attackClassification();
	}
	
	public void updateAttackCategory(String input)
	{
		this.attack_category = input;
	}
	
	public void attackClassification()
	{
		int sum = 0;
		
		for( Map.Entry<String, Integer> elem : detailed_anomal.entrySet() )
		{
			sum = sum + elem.getValue();
		}
		
		if( (this.anomal_type & AnomalDetector.Invalid_ID_SIG) == AnomalDetector.Invalid_ID_SIG )
		{
			if(this.invalid_IDs.contains("0000"))
			{
				if(sum > 5)
					this.attack_category = "DoS Attack";
			}
		}
		else if( (this.anomal_type & AnomalDetector.High_Frequence_SIG) == AnomalDetector.High_Frequence_SIG )
		{
			if(sum > 5)
				this.attack_category = "Fuzzy Attack";
		}
	}
	
	public long getStartTime()
	{
		return start;
	}
	
	public long getEndTime()
	{
		return end;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public int getType()
	{
		
		return anomal_type;
	}
	
	public String getAttackCategory()
	{
		
		return attack_category;
	}
	
	public synchronized  Map<String, Integer> getCount()
	{
		Map<String, Integer> tmp = new HashMap<String, Integer>(this.detailed_anomal);
		return tmp;
	}
	
	private String genString_of_all_anomal_type()
	{
		String rlt = "";
		
		if( (this.anomal_type & AnomalDetector.Less_Num_of_Packets_SIG) == AnomalDetector.Less_Num_of_Packets_SIG )
		{
			rlt += "Less number of packets, ";
		}
		
		if( (this.anomal_type & AnomalDetector.More_Num_of_Packets_SIG) == AnomalDetector.More_Num_of_Packets_SIG )
		{
			rlt += "More number of packets, ";
		}
		
		if( (this.anomal_type & AnomalDetector.Low_Busload_SIG) == AnomalDetector.Low_Busload_SIG )
		{
			rlt += "Low bus load, ";
		}
		
		if( (this.anomal_type & AnomalDetector.High_Busload_SIG) == AnomalDetector.High_Busload_SIG )
		{
			rlt += "High bus load, ";
		}
		
		if( (this.anomal_type & AnomalDetector.Less_Num_of_IDs_SIG) == AnomalDetector.Less_Num_of_IDs_SIG )
		{
			rlt += "Less number of IDs, ";
		}
		
		if( (this.anomal_type & AnomalDetector.More_Num_of_IDs_SIG) == AnomalDetector.More_Num_of_IDs_SIG )
		{
			rlt += "More number of IDs, ";
		}
		
		if( (this.anomal_type & AnomalDetector.Low_Frequence_SIG) == AnomalDetector.Low_Frequence_SIG )
		{
			rlt += "Low frequence, ";
		}
		
		if( (this.anomal_type & AnomalDetector.High_Frequence_SIG) == AnomalDetector.High_Frequence_SIG )
		{
			rlt += "High frequence, ";
		}
		
		if( (this.anomal_type & AnomalDetector.Invalid_ID_SIG) == AnomalDetector.Invalid_ID_SIG )
		{
			rlt += "Invalid ID, ";
		}
		
		if( (this.anomal_type & AnomalDetector.Invalid_ID_Sequence_SIG) == AnomalDetector.Invalid_ID_Sequence_SIG )
	      {
	         rlt += "Invalid ID Sequence, ";
	      }
		
		return rlt.substring(0, rlt.length()-2);
	}
	
	public synchronized  Map<String, Integer> getDetailed_anomal()
   {
      Map<String, Integer> tmp = new HashMap<String, Integer>(this.detailed_anomal);
      return tmp;
   }
	
	public synchronized ArrayList<String> getIDs()
	   {
	      ArrayList<String> tmp = new ArrayList<String>(this.IDs_in_order);
	      return tmp;
	   }
	
	public void print()
	{
		System.out.println("==============================================");
		System.out.println("Start : " + this.start);
		System.out.println("End   : " + this.end);
		System.out.println("Duration : " + (float)(this.end - this.start)/1000000);
		System.out.println("Value : " + this.value);
		System.out.println("Anomal Type : " + genString_of_all_anomal_type());
		
		if( (this.anomal_type & AnomalDetector.Invalid_ID_SIG) == AnomalDetector.Invalid_ID_SIG )
		{
			System.out.println("\t - Invalid IDs {" + invalid_IDs.substring(0, invalid_IDs.length()-2) + "}");
		}
		
		if( (this.anomal_type & AnomalDetector.Low_Frequence_SIG) == AnomalDetector.Low_Frequence_SIG )
		{
			System.out.println("\t - Low frequence IDs {" + LowFrequency_IDs.substring(0, LowFrequency_IDs.length()-2) + "}");
		}
		
		if( (this.anomal_type & AnomalDetector.High_Frequence_SIG) == AnomalDetector.High_Frequence_SIG )
		{
			System.out.println("\t - High frequence IDs {" + HighFrequency_IDs.substring(0, HighFrequency_IDs.length()-2) + "}");
		}
		
		System.out.println("Count : "); // +this.getCount());
		
		if( (this.anomal_type & AnomalDetector.Invalid_ID_SIG) == AnomalDetector.Invalid_ID_SIG )
		{
			String str = "";
			
			TreeMap<String, Integer> tm = new TreeMap<String, Integer>(getDetailed_anomal());
				 
			Iterator<String> iteratorKey = tm.keySet( ).iterator( );

			while (iteratorKey.hasNext()) {
				String key = iteratorKey.next();
				if(key.startsWith("Invalid ID ("))
				{
					int value = tm.get(key);
					str += ("[" + key.substring(12, 16) + "] = " + value + ", ");
				}
			}
			
			System.out.println("\t - Invalid ID Lists {" + str.substring(0, str.length()-2) + "}");
		}
		
		if( (this.anomal_type & AnomalDetector.High_Frequence_SIG) == AnomalDetector.High_Frequence_SIG )
		{
			String str = "";
			
			TreeMap<String, Integer> tm = new TreeMap<String, Integer>(getDetailed_anomal());
				 
			Iterator<String> iteratorKey = tm.keySet( ).iterator( );

			while (iteratorKey.hasNext()) {
				String key = iteratorKey.next();
				if(key.startsWith("High fre"))
				{
					int value = tm.get(key);
					str += ("[" + key.substring(16, 20) + "] = " + value + ", ");
				}
			}
			
			System.out.println("\t - High frequency ID Lists {" + str.substring(0, str.length()-2) + "}");
		}
		
		if( (this.anomal_type & AnomalDetector.Invalid_ID_Sequence_SIG) == AnomalDetector.Invalid_ID_Sequence_SIG )
		{			
			System.out.println("\t - Invalid ID Sequence Count : " + getDetailed_anomal().get("Invalid ID Sequence") );
		}
		
		System.out.println("IDs shown in order : " + this.getIDs());
		System.out.println("Attack Class : " + this.attack_category);
		System.out.println("==============================================\n\n");
	}
}
