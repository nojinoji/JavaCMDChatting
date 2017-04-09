
import java.net.*;            
import java.io.*;            
import java.util.*;            

public class ChatServer {				
	static HashMap hm;                                    
	static HashMap room; 
	static ServerSocket server;
	
	public static void main(String[] args) throws SocketException {			
		try{		
			server = new ServerSocket(10001);	
			System.out.println("접속을 기다립니다.");	
			hm = new HashMap();	
			room = new HashMap();
			String roomName = "waitroom";
				
			while(true){	
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, roomName);
				chatthread.showlist();
				chatthread.help();
				chatthread.start();
			} // while	
		}catch(Exception e){	
			System.out.println(e);
		}	
	} 	
}	
