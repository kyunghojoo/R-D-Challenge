package emsec.korea.data_structure;

public class FeatureObject {

	private String value;
	private long time;	
	
	public FeatureObject()
	{
		value = "";
		time = 0;
	}
	
	public FeatureObject(String value, long time)
	{
		this.value = value;
		this.time = time;
	}
	
	public FeatureObject(FeatureObject obj)
	{
		this.value = obj.getValue();
		this.time = obj.getTimestamp();
	}
	
	public void set(String value, long time)
	{
		this.value = value;
		this.time = time;
	}
	
	public synchronized void update(FeatureObject obj)
	{
		this.value = obj.getValue();
		this.time = obj.getTimestamp();
	}
	
	public synchronized String getValue(){
		return this.value;
	}
	public synchronized long getTimestamp(){
		return this.time;
	}
	
	public synchronized void print(String name){
		System.out.println("=======================================");
		System.out.println("name : " + name);
		System.out.println("value : " + this.value);
		System.out.println("timestamp : " + this.time);
		System.out.println("=======================================");
	}
	
}
