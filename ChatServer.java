import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);//클라이언트의 요청을 받기 위한 준비
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();//Hashmap 객체 hm 생성
			while(true){
				Socket sock = server.accept();//클라이언트의 요청을 받아 들인다
				ChatThread chatthread = new ChatThread(sock, hm);//소켓과 hm객체 넘겨
				// 유저마다 쓰레드를 생성해준다
				chatthread.start(); // 쓰레드의 시작, run()메소드 실행
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
	
}

class ChatThread extends Thread{ //Thread를 상속하는 ChatThread
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//소켓 통신을 할 PrintWriter 생성
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//소켓 통신을 할 BufferedReader 생성
			id = br.readLine(); 
			broadcast(id + " entered.");//
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
}
