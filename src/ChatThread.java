
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
			broadcast(id + "���� �����Ͽ����ϴ�.");
			System.out.println("������ ������� ���̵�� " + id + "�Դϴ�.");
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
	} // ������

	public void run() {
		try {
			String line = null;

			while ((line = br.readLine()) != null) {
				String sub[] = line.split("\\|");
				String bro = sub[1];
				System.out.println("log::"+sub[0]+"����");
				
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
			broadcast(id + " ���� ���� �����Ͽ����ϴ�.");
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
				broadcast("1." + sub[0] + " " + "2." + sub[1] + "��ǥ�� ����̵Ǿ����ϴ�.");
				
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
					} else if(str.equals("2")){// str.equals�� �ٲٱ�
						second++;
						count++;
						
					}else{
						pw.println("�߸��Է��ϼ̽��ϴ�.");
						pw.flush();
					}
					broadcast("1��: " + first + "ǥ" + " || " + "2��: " + second + "ǥ �Դϴ�.");
					
				}
				if(count == (ChatServer.hm.size())){
					endflag = true;
				}
				if(endflag == true){
					broadcast("����� 1��: " + first + "ǥ" + " || " + "2��: " + second + "ǥ �Դϴ�.");
				}

			} else {
				pw.println("�߸��Է��ϼ̽��ϴ�.");
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
			roomcast(id + "�̹� �����Դϴ�.", ChatServer.room.get(id).toString());
		} else {
			if (obj != null) {
				ChatServer.room.put(id, "waitroom");

				broadcast(id + "���� ���� �������ϴ�.");
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
					broadcast(ChatServer.room.get(id).toString() + ">>" + id + " ���� �����Ͽ����ϴ�.");

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
					pw.println(id + " ���� ������ �ӼӸ��� �����̽��ϴ�. :" + msg2);
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
			
				pw.println("/quit(������), /to(�ӼӸ�), /room(�����),"+"\n"
						+ "/bro(��ε��ɽ���), /out(�泪����), /vote(��ǥ)");
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
			System.out.println("log::" + protocol+"\\����");
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
