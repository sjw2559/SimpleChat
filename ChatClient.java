import java.net.*;
import java.io.*;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>"); //username, server-ip 입력하기 
			System.exit(1);
		}
		Socket sock = null; //  생성
		BufferedReader br = null; // BufferedReader 생성
		PrintWriter pw = null; //PrintWriter 생성
		boolean endflag = false; // endflag boolean 변수 선언
		try{
			sock = new Socket(args[1], 10001); // 서버 포트 할당해주기
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream())); // PrintWriter 스트림연결
			br = new BufferedReader(new InputStreamReader(sock.getInputStream())); // BufferedReader 스트림연결
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); //입력받는 keyboard 생성
			// send username.
			pw.println(args[0]);
			pw.flush(); // 
			InputThread it = new InputThread(sock, br); //InputThread 생성
			it.start(); // InputThread 시작, run함수 불러오기
			String line = null; // String 변수 생성
			while((line = keyboard.readLine()) != null){ //line을 다 읽을때까지 돌리기
				pw.println(line); //PrintWriter를 통해 line 출력
				pw.flush(); // 
				if(line.equals("/quit")){ // 만약 line이 "equit"이면 
					endflag = true; //endflag true로 바꿔주고
					break; // while문 빠져나오기
				}
			} 
			System.out.println("Connection closed."); // "연결끊어짐" 출력
		}catch(Exception ex){ // 만약 exception발생한다면
			if(!endflag) // endflag가 false라면
				System.out.println(ex); //에러메세지 출력
		}finally{
			try{ 
				if(pw != null) // PrintWriter이 null이라면
					pw.close(); // PrintWriter 닫기
			}catch(Exception ex){}
			try{ 
				if(br != null) // BufferedReader이 null이라면
					br.close(); // BufferedReader 닫기
			}catch(Exception ex){}
			try{
				if(sock != null) // socket이 null이라면
					sock.close(); // socket 닫기
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null; // socket 생성
	private BufferedReader br = null; // BufferedReader
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;  // 초기값 할당해주기
	}
	public void run(){ // run 메소드 정의하기
		try{
			String line = null; // String line 생성
			while((line = br.readLine()) != null){ //bufferedReader에 line이 읽힐때까지
				System.out.println(line); // line 출력하기
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)  // BufferedReader이 null이라면
					br.close(); // 닫기
			}catch(Exception ex){}
			try{
				if(sock != null) // socket이 null이라면
					sock.close(); // socket 닫기
			}catch(Exception ex){}
		}
	} // InputThread
}
