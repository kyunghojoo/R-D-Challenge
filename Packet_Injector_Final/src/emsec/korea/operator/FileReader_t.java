package emsec.korea.operator;

import java.io.BufferedReader;
import java.io.FileReader;

import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.Queue;

public class FileReader_t extends Thread {
	
	private String filename = null;
	private Queue board = null;
	
	public FileReader_t(String fname, Queue board)
	{
		filename = fname;
		this.board = board;
	}
	
	public void run()
	{
		String data;
		String ts = null, id = null, type = null, length = null, hexdata = null;
		
		System.out.println(" > Data Reading Started...");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));             

            while ((data = reader.readLine()) != null)
            {
            	String[] sep1 = data.split(":{1}| {2,}");

            	ts = sep1[1].trim();
            	id = sep1[3].trim();
            	type = sep1[4].trim();
            	length = sep1[6].trim();
            	hexdata = sep1[7].replaceAll(" ", "").trim();      	
 
            	
            	DataObject obj = new DataObject(ts, id, type, length, hexdata);
            	
            	while(true)
            	{
            		if(board.enqueue(obj) == false)
            		{
            			Thread.sleep(300);
            		}
            		else
            			break;
            	}               
            }
 
            reader.close();
            
            System.out.println(" > Data Reading Completed...");
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
