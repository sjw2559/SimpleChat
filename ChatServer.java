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
				chatthread.start(); // 쓰레드의 시작, run()메소드 실행
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
	
}

class ChatThread extends Thread{ //접속을 계속 유지하면서 데이터 송수신을 위해
	private Socket sock; //클라이언트와 통신하기 위한 소켓
	private String id; //클라이언트의 아이디 저장할 변수
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){//접속 요청한 소켓 객체와 해쉬맵이 전달
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//클라이언트로부터 데이터를 송신하기 위해
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//클라이언트로부터 데이터를 수신받기 위해
			id = br.readLine(); //클라이언트가 보낸 데이터를 출력
			broadcast(id + " entered.");//다른 유저 모두에게 broadcast로 메시지를 보냄
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){ //여러 스레드가 공유하는 해쉬 맵을 동기화
				hm.put(this.id, pw);
				//접속한 유저의 아이디를 key, 출력 스트림(송신)을 value로 저장
				//모든 클라이언트에 의해 공유되어 메시지를 브로드 캐스팅을 위해 출력 스트림을 해쉬 맵에 저장
				//스레드 간의 정보를 공유할 hashmap
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // constructor
	public void run(){ //클라이언트로부터 수신받은 데이터를 클라이언트에게 송신한다.
		try{
			String line = null;//클라이언트로부터 수신 받은 데이터를 저장하기 위한 변수
			while((line = br.readLine()) != null){
			//입력 스트림(수신)을 통해 클라이언트가 보낸 정보 읽어옴
				if(line.equals("/quit"))
					break; //finally 구문으로 가기
				if(line.indexOf("/to ") == 0){
					sendmsg(line); //특정 클라이언트에게 메시지를 보냈다면 귓속말 sendmsg 호출
				}else
					broadcast(id + " : " + line);//모든 클라이언트에게 broadcasting
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){//여러 쓰레드가 공유하는 해쉬 맵을 동기화함
				hm.remove(id); //hashmap에서 정보 삭제
			}
			broadcast(id + " exited.");//접속 종료를 모든 클라이언트에게 알림
			try{
				if(sock != null)
					sock.close();//나간 클라이언트 객체를 close
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){ //귓속말 기능을 가진 sendmsg 메소드
		//귓속말로 넘어온 메시지 중 아이디 부분에 해당되는 문자열을 찾기 위해
		int start = msg.indexOf(" ") +1; // 처음 공백 문자 다음부터
		int end = msg.indexOf(" ", start); //두번째 공백 문자 사이의 문자가 아이디
		if(end != -1){ 
			String to = msg.substring(start, end);// 아이디 부분만 얻어냄
			String msg2 = msg.substring(end+1);//아이디 이후는 대화내용
			Object obj = hm.get(to);//해쉬 맵에서 아이디로 출력 스트림을 얻어냄
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){//모든 클라이언트에게 받은 메세지를 브로드 캐스팅하기
		synchronized(hm){//여러 쓰레드가 공유하는 해쉬 맵을 동기화
			Collection collection = hm.values();//모든 출력 스트림 가져오기
			Iterator iter = collection.iterator();
			while(iter.hasNext()){//
				PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);//수신받은 메시지를 다시 송신한다
				pw.flush();
			}
		}
	} // broadcast
}
