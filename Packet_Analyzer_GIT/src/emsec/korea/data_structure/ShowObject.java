package emsec.korea.data_structure;

public class ShowObject {
	
	public double freq;
	public long elapsed_time;
	public long count;
	public int data_length;
	public String data;
	public long timestamp;
	
	public ShowObject()
	{
		freq = 0.0;
		elapsed_time = 0;
		count = 1;
	}
	
	public ShowObject(DataObject obj)
	{
		freq = 0.0;
		elapsed_time = 0;
		count = 1;
		data_length = Integer.parseInt(obj.getdata_length());
		data = obj.getdata();
		timestamp = obj.ts;
	}
	
	public ShowObject(ShowObject obj)
	{
		this.freq = obj.freq;
		this.elapsed_time = obj.elapsed_time;
		this.count = obj.count;
		this.data_length = obj.data_length;
		this.data = obj.data;
		this.timestamp = obj.timestamp;
	}
	
	public ShowObject update(DataObject updated)
	{
		double freq_total;
		
		elapsed_time = updated.ts - timestamp;
		
		if(count == 1)
		{
			freq_total = elapsed_time;
		}
		else
		{
			freq_total = freq * (count - 1);
			freq_total += elapsed_time;
		}
		freq = freq_total / count;
		
		count++;
		
		data_length = Integer.parseInt(updated.getdata_length());
		
		data = updated.getdata();
		
		timestamp = updated.ts;
		
		return this;
	}
	
	public ShowObject updateElapTime(long time)
	{
		if( (time - this.timestamp) > this.elapsed_time )
		{
			this.elapsed_time = (time - this.timestamp);
		}
		
		return this;
	}
}
