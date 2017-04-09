
import java.net.*;
import java.io.*;
import java.util.*;

class ChatThread extends Thread {
	private static final int YES_LOGON = 2000;
	private static final int NO_LOGON = 3000;
	private static final int YES_BRO = 2001;
	private static final int NO_BRO = 3001;
	private static final int YES_EXIT = 2002;
	private static final int YES_TO = 2003;
	private static final int NO_TO = 3003;
	private static final int YES_ROOM = 2004;
	private static final int NO_ROOM = 3004;
	private static final int YES_VOTE = 2005;
	private static final int NO_VOTE = 3005;

	private Socket sock;
	private String id;
	private BufferedReader br;
	private PrintWriter pw;
	private boolean initFlag = false;
	private String roomName;
	private static int first = 0;
	private static int second = 0;
	private static int count = 0;
	private boolean endflag = false;

	public ChatThread(Socket sock, String roomName) {
		this.sock = sock;
		this.roomName = roomName;
		try {
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String idpro = br.readLine();
			String sub[] = idpro.split("\\|");
			id = sub[1];
			broadcast(id + "님이 접속하였습니다.");
			System.out.println("접속한 사용자의 아이디는 " + id + "입니다.");
			protocol(YES_LOGON);
			synchronized (ChatServer.hm) {
				ChatServer.hm.put(this.id, pw);
			}
			synchronized (ChatServer.room) {
				ChatServer.room.put(this.id, roomName);
			}
			initFlag = true;
		} catch (Exception ex) {
			protocol(NO_LOGON);
			System.out.println(ex);
		}
	} // 생성자

	public void run() {
		try {
			String line = null;

			while ((line = br.readLine()) != null) {
				String sub[] = line.split("\\|");
				String bro = sub[1];
				System.out.println("log::"+sub[0]+"수신");
				
				if (bro.equals("/quit")) {
					protocol(YES_EXIT);
					break;
				} else if (bro.indexOf("/to ") == 0) {
					sendmsg(bro);
				} else if (bro.indexOf("/room ") == 0) {
					room(bro);
				} else if (bro.indexOf("/bro ") == 0) {
					int start = bro.indexOf(" ") + 1;
					String to = bro.substring(start);
					broadcast(to);
				} else if (bro.indexOf("/vote ") == 0) {
					vote(bro);
					protocol(YES_VOTE);
				} else if (bro.equals("/out")) {
					roomout();
					protocol(YES_BRO);
				} else {
					roomcast(" " + id + " : " + bro, ChatServer.room.get(id).toString());
					protocol(YES_BRO);
				}
			}
		} catch (Exception ex) {
			protocol(NO_BRO);
			System.out.println(ex);
		} finally {
			synchronized (ChatServer.hm) {
				ChatServer.hm.remove(id);
			}
			broadcast(id + " 님이 접속 종료하였습니다.");
			try {
				if (sock != null)
					sock.close();
			} catch (Exception ex) {
			}
		}
	} // run

	private void vote(String vote) {
		try{
			
			int receive = vote.indexOf(" ") + 1;
			String str = vote.substring(receive);

			if (str.contains(",")) {
				String sub[] = str.split(",");
				broadcast("1." + sub[0] + " " + "2." + sub[1] + "투표가 등록이되었습니다.");
				
				endflag = false;
				count = 0;
				first = 0;
				second = 0;
			}

			else if (str.equals("") == false) {
				if (count < ChatServer.hm.size()) {
					if (str.equals("1")) {
						first++;
						count++;
					} else if(str.equals("2")){// str.equals로 바꾸기
						second++;
						count++;
						
					}else{
						pw.println("잘못입력하셨습니다.");
						pw.flush();
					}
					broadcast("1번: " + first + "표" + " || " + "2번: " + second + "표 입니다.");
					
				}
				if(count == (ChatServer.hm.size())){
					endflag = true;
				}
				if(endflag == true){
					broadcast("결과는 1번: " + first + "표" + " || " + "2번: " + second + "표 입니다.");
				}

			} else {
				pw.println("잘못입력하셨습니다.");
				pw.flush();
			}
		}catch(Exception e){
			protocol(NO_VOTE);
			System.err.println(e);
		}
	}

	private void roomout() {
		Object obj = ChatServer.hm.get(id);

		if (ChatServer.room.get(id).toString() == "waitroom") {
			roomcast(id + "이미 대기실입니다.", ChatServer.room.get(id).toString());
		} else {
			if (obj != null) {
				ChatServer.room.put(id, "waitroom");

				broadcast(id + "님이 방을 나갔습니다.");
			}
		}

	}

	public void room(String room) {
		try {
			int start = room.indexOf(" ") + 1;
			if (start != -1) {
				String to = room.substring(start);
				Object obj = ChatServer.hm.get(id);
				System.err.println("ID: " + id);

				if (obj != null) {
					ChatServer.room.put(id, to);
					System.err.println(to);
					broadcast(ChatServer.room.get(id).toString() + ">>" + id + " 님이 입장하였습니다.");

				}
				protocol(YES_ROOM);

			}
		} catch (Exception ex) {
			protocol(NO_ROOM);
		}
	}

	public void sendmsg(String msg) {
		try {
			int start = msg.indexOf(" ") + 1;
			int end = msg.indexOf(" ", start);
			if (end != -1) {
				String to = msg.substring(start, end);
				String msg2 = msg.substring(end + 1);
				Object obj = ChatServer.hm.get(to);
				if (obj != null) {
					PrintWriter pw = (PrintWriter) obj;
					pw.println(id + " 님이 다음의 귓속말을 보내셨습니다. :" + msg2);
					pw.flush();
				} 
				pw.println(YES_TO);
				pw.flush();
			}
		} catch (Exception ex) {
			pw.println(NO_TO);
			pw.flush();
		}
	}

	public void showlist() {
		synchronized (ChatServer.hm) {
			
			Collection collection = ChatServer.hm.values();
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {
				PrintWriter pw = (PrintWriter) iter.next();
				pw.println(ChatServer.hm.keySet());
				pw.flush();
			}
		}
	}
	public void help() {
		synchronized (ChatServer.hm) {
			
				pw.println("/quit(나가기), /to(귓속말), /room(방들어가기),"+"\n"
						+ "/bro(브로드케스팅), /out(방나가기), /vote(투표)");
				pw.flush();
			
		}
	}

	public void broadcast(String msg) {
		
		synchronized (ChatServer.hm) {
			Collection collection = ChatServer.hm.values();
			Iterator iter = collection.iterator();

			while (iter.hasNext()) {

				PrintWriter pw = (PrintWriter) iter.next();
				pw.println(msg);
				pw.flush();
			}
		}
	}

	public void roomcast(String msg, String roomName) {
		
		synchronized (ChatServer.hm) {
			Collection collection = ChatServer.hm.values();
			Iterator iter = collection.iterator();

			Object arr[] = ChatServer.room.keySet().toArray();

			int i = 0;
			while (iter.hasNext()) {
				if (ChatServer.room.get(arr[i].toString()).equals(roomName)) {
					PrintWriter pw = (PrintWriter) iter.next();
					pw.println("[" + roomName + "]" + msg);
					pw.flush();
				} else {
					iter.next();
				}
				i++;
			}
		}
	}

	public void protocol(int protocol) {

		synchronized (ChatServer.hm) {
			System.out.println("log::" + protocol+"\\전송");
			Collection collection = ChatServer.hm.values();
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {

				PrintWriter pw = (PrintWriter) iter.next();
				pw.println(String.valueOf(protocol));
				pw.flush();

			}
		}
	}
}
