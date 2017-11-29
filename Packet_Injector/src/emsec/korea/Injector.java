package emsec.korea;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import emsec.korea.data_structure.Queue;
import emsec.korea.operator.DataSender;
import emsec.korea.operator.FileReader_t;


public class Injector {
	
	private static Socket socket = null;
	
	private static Queue board = new Queue();
	
	private static int ServerPort = 0;

	public static void main(String[] args)
	{
		if(args.length != 2)
		{
			printMessage("Usage : java emsec.korea.Injector [Server IP] [Server Port] [Full File Path]");
			return ;
		}
		
		System.out.print("Connecting to " + args[0] + ":" + args[1] + "...");
		ServerPort = Integer.parseInt(args[1]) + 1;
		
		try {
			socket = new Socket(args[0], Integer.parseInt(args[1]));
			System.out.println("  Success");
			
			board.initialize(10000);
			
			//FileReader_t reader = new FileReader_t("D:\\CAN_Dataset\\CAN_Dataset.txt", board);
			FileReader_t reader = new FileReader_t(args[2], board);
			reader.start();
			
			DataSender sender = new DataSender(socket, board);
			
			ClientServer c_server = new ClientServer(ServerPort, sender);
			c_server.setDaemon(true);
			c_server.start();
			
			sender.start();
			
			reader.join();
			sender.join();
			
		} catch (Exception e) {
			System.out.println("  Fail");
			e.printStackTrace();
		}
		finally
		{
			try {
				Thread.sleep(500);
				System.out.println("Socket Close");
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void printMessage(String msg){
		System.out.println(msg);
	}
}

class ClientServer extends Thread {
	
	private ServerSocket c_server;
	private Socket connected;
	private int Current_SessionNum;
	
	private int ServerPort = 0;
	private int MaxSessionNum = 1;
	
	private DataSender operator = null;
	
	public ClientServer(int Port, DataSender oper)
	{
		operator = oper;
		c_server = null;
		connected = null;
		Current_SessionNum = 0;
		ServerPort = Port;
	}
	
	public void run()
	{
		//String ClientIP;
		
		try {
			c_server = new ServerSocket(ServerPort);
			
			while(true)
			{
				if(Current_SessionNum < MaxSessionNum)
				{
					//printMessage("### Waiting new connection... (Server Port : " + ServerPort + ") ###");
					connected = c_server.accept();
					//printMessage(" - Connection established from : " + (ClientIP = connected.getInetAddress().toString()) + " (" + getTime() + ")");
					
					Thread worker_thread = new Thread(new Worker());
					worker_thread.setDaemon(true);
					worker_thread.start();
					
					Current_SessionNum++;
					
					worker_thread.join();
					
					if(connected.isClosed())
					{
						//printMessage(" - Connection Closed : " + ClientIP + " (" + getTime() + ")");
						Current_SessionNum--;
						//printMessage("");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date());
	}
	
	private static void printMessage(String msg){
		System.out.println(msg);
	}

	class Worker implements Runnable {
		
		private static final byte STOP_SIG = (byte)0x01;
		private static final byte RESUME_SIG = (byte)0x10;
		private static final byte EXIT_SIG = (byte)0x20;
		
		private DataInputStream dis = null;
		
		public Worker() throws IOException
		{
			this.dis = new DataInputStream(new BufferedInputStream(connected.getInputStream()));
		}
		
		@Override
		public void run()
		{
			try {
				
				while(true)
				{
					byte receive = this.dis.readByte();
					
					if(receive == STOP_SIG)
					{
						printMessage(" - Worker Received STOP_SIG");
						
						operator.SetSwitch(false);
					}
					else if(receive == RESUME_SIG)
					{
						printMessage(" - Worker Received RESUME_SIG");
						
						operator.SetSwitch(true);
					}
					else if(receive == EXIT_SIG)
					{
						printMessage(" - Worker Received EXIT_SIG");
						
						break;
					}
					else
					{
						printMessage(" - Worker Received Wrong Signal : 0x" + String.format("%02X",  receive) + " (" + getTime() + ")");
					}
				}
				
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				try {
					connected.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}


