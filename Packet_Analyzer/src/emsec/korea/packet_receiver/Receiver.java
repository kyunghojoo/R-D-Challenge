package emsec.korea.packet_receiver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import emsec.korea.Monitor;
import emsec.korea.data_structure.DataObject;
import emsec.korea.data_structure.ListMap;
import emsec.korea.data_structure.Queue;
import emsec.korea.ui.MainFrame_1080p;

public class Receiver extends Thread {
	
	private ServerSocket server;
	private Socket connected;
	private int Current_SessionNum;
	
	private int ServerPort = 10000;
	private int MaxSessionNum = 1;
	
	private ListMap list = null;
	private Queue board = null;
	
	private boolean online_switch = false;
	
	private volatile boolean offline_read_process = false;
	private String file_loc = null;
	
	private String ClientIP;
	
	private Commander command = null;
	
	public Receiver(boolean online_switch, String loc, ListMap list, Queue board)
	{
		server = null;
		connected = null;
		Current_SessionNum = 0;
		
		this.online_switch = online_switch;
		this.file_loc = loc;
		this.list = list;
		this.board = board;
	}
	
	public Receiver(boolean online_switch, String loc, ListMap list, Queue board, int PortNum)
	{
		this(online_switch, loc, list, board);
		ServerPort = PortNum;
	}
	
	public boolean isConAlive()
	{
		if(online_switch && (connected != null))
		{
			if( connected.isConnected() && (connected.isClosed() == false) )
				return true;
			else
				return false;
		}
		else if(online_switch == false)
		{
			return true;
		}
		else
			return false;
	}
	
	private static final byte STOP_SIG = (byte)0x01;
	private static final byte RESUME_SIG = (byte)0x10;
	//private static final byte EXIT_SIG = (byte)0x20;
	
	public void doAction(int type) throws IOException
	{
		if( (command != null) && online_switch)
		{
			if(type == 1)
			{
				command.sendsig(STOP_SIG);
			}
			else if(type == 2)
			{
				command.sendsig(RESUME_SIG);
			}
		}
		else
		{
			if(type == 1)
			{
				printMessage("  - Paused");
				offline_read_process = false;
			}
			else if(type == 2)
			{
				printMessage("  - Resumed");
				offline_read_process = true;
			}
		}
	}
	
	public void run()
	{
		if(online_switch) // Online(TCP) mode
		{
			try {
				server = new ServerSocket(ServerPort);
				
				while(true)
				{
					if(Current_SessionNum < MaxSessionNum)
					{
						printMessage("### Waiting new connection... (Server Port : " + ServerPort + ") ###");
						connected = server.accept();
						printMessage(" - Connection established from : " + (ClientIP = connected.getInetAddress().toString().replaceAll("/", "")) + " (" + getTime() + ")");
						
						Thread worker_thread = new Thread(new Worker());
						command = new Commander();
						
						worker_thread.start();
						
						Current_SessionNum++;
						
						worker_thread.join();
						command.disconnect();
						
						if(connected.isClosed())
						{
							printMessage(" - Connection Closed : " + ClientIP + " (" + getTime() + ")");
							Current_SessionNum--;
							printMessage("");
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else // Offline(File-read) mode
		{
			String data;
			String ts = null, id = null, type = null, length = null, hexdata = null;
			BufferedWriter writer = null;
			
			boolean first = true;
			
			System.out.println(" > Data Reading Started...");
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file_loc));
				
				SimpleDateFormat f = new SimpleDateFormat("yyMMdd_HHmmss");
				Monitor.rawPacket_file_loc += (f.format(new Date()) + ".dat");
				writer = new BufferedWriter(new FileWriter(Monitor.rawPacket_file_loc));

				while(true)
				{
					if(offline_read_process)
					{
						data = reader.readLine();
						if(data != null)
						{
							String[] sep1 = data.split(":{1}| {2,}");
		
			            	ts = sep1[1].trim();
			            	id = sep1[3].trim();
			            	type = sep1[4].trim();
			            	length = sep1[6].trim();
			            	hexdata = sep1[7].replaceAll(" ", "").trim();      	
			            	
			            	DataObject obj = new DataObject(ts, id, type, length, hexdata);
			        		
			        		if(first)
			        		{
			        			Monitor.veryFirstTime = obj.ts;
			        			first = false;
			        		}
			            	
			            	while(true)
			            	{
			            		if(board.enqueue(obj) == false)
			            		{
			            			//System.out.println("Process is delayed by Main Queue");
			            			
			            			Thread.sleep(300);
			            		}
			            		else
			            		{
			            			while(true)
			            			{
			            				if(list.enqueue(obj))
			            					break;
			            		   	}
			            			
			            			writer.write(obj.toString() + "\n");
			            			writer.flush();
			            			break;
			            		}
			            	} 
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
			} finally {
				try {
					if (writer != null)
						writer.close();
					
					Thread.sleep(10000);
					
					MainFrame_1080p.uiControl(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}
	
	private static void printMessage(String msg){
		System.out.println(msg);
	}
	
	class Commander {
		
		private Socket socket = null;
		private DataOutputStream dos = null;
		
		public Commander() throws Exception
		{
			System.out.print(" * Connecting to " + ClientIP + ":" + (ServerPort + 1) + "...");
			
			socket = new Socket(ClientIP, ServerPort + 1);
			System.out.println("  Success");
			
			this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		}
		
		public void sendsig(byte type) throws IOException
		{
			this.dos.writeByte(type);
			this.dos.flush();
		}
		
		public void disconnect() throws IOException
		{
			System.out.println(" * Socket Close");
			this.dos.close();
			this.socket.close();
		}		
	}

	class Worker implements Runnable {
		
		private ObjectInputStream obj_dis = null;
		
		public Worker() throws IOException
		{
			this.obj_dis = new ObjectInputStream(connected.getInputStream());
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			//long start, end;
			BufferedWriter writer = null;
			
			try {
				SimpleDateFormat f = new SimpleDateFormat("yyMMdd_HHmmss");
				Monitor.rawPacket_file_loc += (f.format(new Date()) + ".dat");
				writer = new BufferedWriter(new FileWriter(Monitor.rawPacket_file_loc));
				
				DataObject received = (DataObject)this.obj_dis.readObject();
				received.socket_ts = System.nanoTime();
        		
        		Monitor.veryFirstTime = received.ts;
        		
        		board.enqueue(received);
        		list.enqueue(received);
       			
       			writer.write(received.toString() + "\n");
       			writer.flush();

				while(true)
				{
					received = (DataObject)this.obj_dis.readObject();
					received.socket_ts = System.nanoTime();	

	        		// 대기없이 그냥 쭉 읽어들일때
//					if(board.enqueue(received) == false)
//	        		{
//	        			board.dequeue();
//	        			board.enqueue(received);
//	        		}
//	       			list.enqueue(map);
	       			
	       			// dequeue 대기
	       			while(true)
	            	{            		
	            		if(board.enqueue(received) == false)
	            		{
	            			//System.out.println("Process is delayed by Main Queue");
	            			Thread.sleep(300);
	            		}
	            		else
	            		{
	            			while(true)
	            			{
	            				if(list.enqueue(received))
	            					break;
	            			}
	            			
	            			writer.write(received.toString() + "\n");
	            			writer.flush();
	            			break;
	            		}
	            	} 
	       			
					//System.out.println("Delay : " + (end - start));
					//start = end;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} finally {
				try {
					if(writer != null)
						writer.close();
					
					Thread.sleep(10000);
					
					connected.close();
					
					MainFrame_1080p.uiControl(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}