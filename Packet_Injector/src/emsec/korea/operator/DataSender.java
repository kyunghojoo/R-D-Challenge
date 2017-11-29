package emsec.korea.operator;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.Queue;

public class DataSender extends Thread {
	
	private Socket sock = null;	
	private ObjectOutputStream dos = null;
	
	private Queue board = null;
	
	AtomicBoolean bool = new AtomicBoolean(false);
	//private long delay = 0;
	private DataObject curitem = null;
	
	AtomicBoolean oper_switch = new AtomicBoolean(false);
	
	private long count = 0;
		
	public DataSender(Socket socket, Queue board)
	{
		this.sock = socket;
		
		this.board = board;
	
		try {
			this.dos = new ObjectOutputStream(this.sock.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void SetSwitch(boolean on)
	{
		oper_switch.set(on);
	}
	
	public void run()
	{
		int i;
		
		System.out.print(" > Count to send : ");
		
		for(i=0;i<3;i++)
		{
			System.out.print(3-i + " ");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		System.out.println("Go.");
		
		small_queue_maker inner = new small_queue_maker();
		inner.setDaemon(true);
		inner.start();

		try {
			while(true)
			{
				if( (bool.get() == true) && (oper_switch.get() == true) )
				{
					System.out.println("Sending [" + (count++) + "] : " + curitem.toString());
					this.dos.writeObject(curitem);
					this.dos.flush();
					curitem = null;
					bool.set(false);
					//TimeUnit.MICROSECONDS.sleep(delay/2);
					
					if( (inner.isAlive() == false) && (curitem == null) )
					{
						System.out.println(" > Data Sending End...");
						break;
					}
				}			
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class small_queue_maker extends Thread 
	{
		private DataObject next = null;
		
		public small_queue_maker()
		{
			curitem = board.dequeue();
			next = board.dequeue();
			//delay = next.ts - curitem.ts;
			//System.out.println("First Delay : " + delay);
			bool.set(true);
		}
		
		public void run()
		{
			while(true)
			{
				if(bool.get() == false)
				{
					curitem = next;
					next = board.dequeue();
					if(next == null)
					{
						//delay = 0;
						bool.set(true);
						break;
					}
					//delay = next.ts - curitem.ts;
					bool.set(true);
					//System.out.println("Delay : " + delay);
				}
			}
		}
	}
}
