package emsec.korea.data_structure;

import java.io.Serializable;

import emsec.korea.utils.BytesUtils;

public class DataObject implements Serializable {
	
	private static final long serialVersionUID = 7648426095832338295L;
	
	public long socket_ts = 0;
	public long ts;
	private int id;
	private int type;
	private int data_length;
	private byte[] data;
	
	public DataObject(String ts, String id, String type, String data_length, String data)
	{
		this.ts = (long)(Double.parseDouble(ts) * 1000000);
		this.id = Integer.parseInt(id, 16);
		this.type = Integer.parseInt(type, 16);
		this.data_length = Integer.parseInt(data_length, 16);
		
		this.data = BytesUtils.HexstringToHex(data);
	}
	
	public String getid()
	{
		return String.format("%04X", id);
	}
	
	public String gettype()
	{
		return String.format("%03X", type);
	}
	
	public String getdata_length()
	{
		return Integer.toString(data_length);
	}
	
	public String getdata()
	{
		return BytesUtils.HextoString(data);
	}
	
	public String toString()
	{
		//return "TS : " + String.format("%.6f", ts) + " // ID : " + String.format("%04X", this.id) + " // Type : " + String.format("%03X", this.type) + " // DLC : " + this.data_length + " // Data : " + BytesUtils.HextoStringwithcol(this.data, 0, this.data.length);
		return "TS : " + ts + " // ID : " + String.format("%04X", this.id) + " // Type : " + String.format("%03X", this.type) + " // DLC : " + this.data_length + " // Data : " + BytesUtils.HextoStringwithcol(this.data, 0, this.data.length);
	}
	
	public void print()
	{
		System.out.println("Socket TS : " + socket_ts);
		System.out.println("TimeStamp : " + ts);
		System.out.println("ID        : " + String.format("%04X", this.id) + " (" + this.id + ")");
		System.out.println("Type      : " + String.format("%03X", this.type) + " (" + this.type + ")");
		System.out.println("Data_len  : " + this.data_length);
		System.out.println("Data      : " + BytesUtils.HextoStringwithcol(this.data, 0, this.data.length));
	}
}
