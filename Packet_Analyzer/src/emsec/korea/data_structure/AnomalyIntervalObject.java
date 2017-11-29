package emsec.korea.data_structure;

import java.util.ArrayList;
import java.util.Map;

public class AnomalyIntervalObject 
{
	public ArrayList<AnomalObject> list = new ArrayList<AnomalObject>();
	public ArrayList<AnomalObject> list_error = new ArrayList<AnomalObject>();

	public void update(AnomalObject input)
	{
		if (getNumofInterval() == 0)
		{
			list.add(input);
			list.get(list.size() - 1).updateAnomalType(input);
		}
		else
		{
			if( (list.get(list.size() - 1).getEndTime() > input.getStartTime()) || (input.getStartTime() - list.get(list.size() - 1).getEndTime() < 500000) )
			{
				list.get(list.size() - 1).update(input);
			} 
			else if( (list.get(list.size() - 1).getType() == input.getType()) && (input.getStartTime() - list.get(list.size() - 1).getEndTime() < 1000000) )
			{
				list.get(list.size() - 1).update(input);
			}
			else
			{
				list.add(input);
				list.get(list.size() - 1).updateAnomalType(input);
				
				// * check if interval is created by error
	            int count = 0;
	            for( Map.Entry<String, Integer> elem : list.get(list.size()-2).getCount().entrySet() )
	            {
	               count = count + elem.getValue();
	            }
	            long duration = list.get(list.size()-2).getEndTime()-list.get(list.size()-2).getStartTime();
	            
	            if( (float)count / duration < 0.0001 )
	            {
	               list.get(list.size()-2).updateAttackCategory("Error");
	               list_error.add(list.get( list.size()-2));
	               list.remove(list.size()-2);
	            }
			}
		}
	}

	public int getNumofInterval()
	{
		return list.size();
	}

	public synchronized ArrayList<AnomalObject> getList()
	{
		ArrayList<AnomalObject> temp = new ArrayList<AnomalObject>();
		for(int i=0;i<list.size();i++)
		{
			temp.add(new AnomalObject(list.get(i)));
		}

		return temp;
	}
	
	public int getNumofInterval2()
	{
		return list_error.size();
	}

	public synchronized ArrayList<AnomalObject> getList2()
	{
		ArrayList<AnomalObject> temp = new ArrayList<AnomalObject>();
		for(int i=0;i<list_error.size();i++)
		{
			temp.add(new AnomalObject(list_error.get(i)));
		}

		return temp;
	}
	
	private static int a = 0;
	private static int b = 0;
	private static int c = 0;
	private static int d = 0;

	public void print()
	{
		b = list.size();

		//마지막꺼 안찍히니 확인하려면 수정해야함
		if( (b > a) && (b > 1) )
		{
			System.out.println( b - 2 );
			list.get(b-2).print();
			
			a = b;
		}
		
		
		d = list_error.size();

		//마지막꺼 안찍히니 확인하려면 수정해야함
		if( (d > c) && (d > 1) )
		{
			System.out.println("\n\n Errors");
			System.out.println( d - 2 );
			list_error.get(d-2).print();
			
			c = d;
		}
	}
}