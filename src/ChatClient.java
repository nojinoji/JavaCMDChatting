
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;			

public class ChatClient {			

	private static final int REQ_LOGON = 1000;
	private static final int REQ_BRO = 1001;
	private static final int REQ_EXIT = 1002;
	private static final int REQ_TO = 1003;
	private static final int REQ_ROOM = 1004;
	private static final int REQ_VOTE = 1005;

	public static void main(String[] args) {		
		System.out.println("IP를 입력해주세요.");
		Scanner sc = new Scanner(System.in);
		String hostIP = sc.nextLine();

		System.out.println("ID를 입력해주세요.");
		String id = sc.nextLine();


		Socket sock = null;	
		
		BufferedReader br = null;	
		PrintWriter pw = null;	
	
		try{	
			sock = new Socket(hostIP, 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));		
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));		
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));		
	
			pw.println(REQ_LOGON+"\\|"+id);		
			pw.flush();		
			InputThread it = new InputThread(sock, br);		
			it.start();		
			String line = null;		
			while((line = keyboard.readLine()) != null){
				
				if(line.equals("/quit")){
					pw.println(REQ_EXIT);		
					pw.flush();

					break;
				}
				else if(line.indexOf("/to ") == 0 ){
					pw.println(REQ_TO+"\\|"+line);
					pw.flush();
				}
				else if(line.indexOf("/room ") == 0){
					pw.println(REQ_ROOM+"\\|"+line);	
					pw.flush();
				}
				else if(line.indexOf("/vote ") == 0){
					pw.println(REQ_VOTE+"\\|"+line);	
					pw.flush();
				}
				else{
					pw.println(REQ_BRO+"\\|"+line);	
					pw.flush();
				}
			}			

			System.out.println("클라이언트의 접속을 종료합니다.");		
		}catch(Exception ex){					
				System.out.println(ex);	
		}finally{			
			try{		
				if(pw != null)	
					pw.close();
			}catch(Exception ex){}		
			try{		
				if(br != null)	
					br.close();
			}catch(Exception ex){}		
			try{		
				if(sock != null)	
					sock.close();
			}catch(Exception ex){}		
		}		
	} 			
} 				

class InputThread extends Thread{					
	private Socket sock = null;				
	private BufferedReader br = null;			
	private static final int YES_LOGON =2000;
	private static final int NO_LOGON= 3000;
	private static final int YES_BRO =2001;
	private static final int NO_BRO= 3001;
	private static final int YES_EXIT =2002;
	private static final int YES_TO =2003;
	private static final int NO_TO= 3003;
	private static final int YES_ROOM =2004;
	private static final int NO_ROOM= 3004;
	private static final int YES_VOTE =2005;
	private static final int NO_VOTE= 3005;


	public InputThread(Socket sock, BufferedReader br){				
		this.sock = sock;			
		this.br = br;			
	}				
	public void run(){				
		try{		
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals(String.valueOf(YES_LOGON))){
				}
				else if(line.equals(String.valueOf(NO_LOGON))){
					System.out.println("로그인에 실패했습니다.");
				}
				else if(line.equals(String.valueOf(YES_BRO))){
				}
				else if(line.equals(String.valueOf(NO_BRO))){
				}
				else if(line.equals(String.valueOf(YES_EXIT))){
				}
				else if(line.equals(String.valueOf(YES_TO))){
				}
				else if(line.equals(String.valueOf(NO_TO))){
					System.out.println("귓속말에 실패했습니다.");
				}
				else if(line.equals(String.valueOf(YES_ROOM))){
				}
				else if(line.equals(String.valueOf(NO_ROOM))){
				}
				else if(line.equals(String.valueOf(YES_VOTE))){
				}
				else if(line.equals(String.valueOf(NO_VOTE))){
					System.out.println("투표를 못만들었습니다.");
				}
				else{
					System.out.println(line);
				}
			}
		}catch(Exception ex){			
		}finally{			
			try{		
				if(br != null)	
					br.close();
			}catch(Exception ex){}		
			try{		
				if(sock != null)	
					sock.close();
			}catch(Exception ex){}		
		}			
	} // InputThread				
}					
