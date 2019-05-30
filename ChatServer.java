import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);//서버 소켓,클라이언트의 요청을 받기 위한 준비
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();//쓰레드 간의 정보를 공유할 hashmap 생성
			while(true){//클라이언트의 접속을 항상 받아들일 수 있도록
				Socket sock = server.accept();
				//클라이언트의 접속 요청을 받아들여 클라이언트와 통신하기 위한 소켓을 생성
				ChatThread chatthread = new ChatThread(sock, hm);
				// 유저마다 쓰레드를 생성해준다,소켓 객체와 해쉬 맵을 넘겨줌
				// 접속을 계속 유지하면서 데이터 송수신 하기 위해
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
			//아이디 출력 스트림 
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//소켓 통신을 할 BufferedReader 생성
			id = br.readLine(); //클라이언트가 보낸 데이터를 출력
			broadcast(id + " entered.");//다른 유저 모두에게 broadcast로 메시지를 보냄
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){ //쓰레드의 동기화
				hm.put(this.id, pw);//접속한 유저의 아이디와 PrintWriter을 hashmap에 넣음
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // constructor
	public void run(){ //run 메소드의 오버라이딩
		try{
			String line = null;
			while((line = br.readLine()) != null){//읽히면 무조건 수행
				if(line.equals("/quit"))
					break; //채팅방 나가기
				if(line.indexOf("/to ") == 0){
					sendmsg(line); //귓속말 sendmsg 메소드
				}else
					broadcast(id + " : " + line);//다른 유저 모두에게 메시지 보냄
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id); //채팅이 끝나면 hashmap에서 정보 삭제
			}
			broadcast(id + " exited.");//유저 모두에게 알림
			try{
				if(sock != null)
					sock.close();//소켓 닫기
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){ //귓속말 기능을 가진 sendmsg 메소드
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
