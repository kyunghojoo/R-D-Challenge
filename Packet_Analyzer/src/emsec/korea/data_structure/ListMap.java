package emsec.korea.data_structure;

import java.util.ArrayList;

public class ListMap {
	
	private ArrayList<DataObject> list_board;
	private int maxSize = 0;
	private volatile byte refreshed = 0;
	
	private volatile byte current_condition = 0;
	
	public ListMap() {
		// TODO Auto-generated constructor stub
	}
	
	public int get_list_size()
	{
		return list_board.size();
	}
	
	public int get_max_size()
	{
		return maxSize;
	}

	public synchronized void initialize(int listSize) {
		list_board = new ArrayList<DataObject>();
		maxSize = listSize;
	}

	public synchronized boolean isEmpty() {
		if (list_board.size() == 0)
		{
			return true;
		}
		return false;
	}

	public synchronized boolean isFull() {
		if (list_board.size() == maxSize)
		{
			return true;
		}
		return false;
	}

	public synchronized boolean enqueue(DataObject item)
	{
		if(current_condition != (byte) 0x00)
			return false;
		
		if (isFull()) {
			list_board.remove(0);
			list_board.add(list_board.size(), item);
		} else {
			list_board.add(list_board.size(), item);
		}
		
		refreshed = (byte) 0xff;
		
		return true;
	}
	
	public synchronized void releaseFlag(byte obj_id)
	{
		this.current_condition &= ~obj_id;
	}
	
	public synchronized ArrayList<DataObject> get_list(byte obj_id)
	{	
		if( (refreshed & obj_id) == obj_id )
		{
			this.current_condition |= obj_id;
			
			refreshed &= ~obj_id;
			
			return list_board;
		}
		else
			return null;
	}
	
}
