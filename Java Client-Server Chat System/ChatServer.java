
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer{  
	public static void main(String[] args ){  
		ArrayList<ChatHandler>handlers = new ArrayList<ChatHandler>();
		try{  
			ServerSocket s = new ServerSocket(3000);
			for(;;){
				Socket incoming = s.accept();
				new ChatHandler(incoming, handlers).start();
			}
		}catch (Exception e){  
			System.out.println(e);
		} 
	} 
}	  
	  
class ChatHandler extends Thread{
	private Socket incoming;
	ArrayList<ChatHandler>handlers;
	BufferedReader in;
	PrintWriter out;
	String userName;
	
	public ChatHandler(Socket i, ArrayList<ChatHandler>h){
		incoming = i;
		handlers = h;
		handlers.add(this);
	}
	public void run(){
		try{
			in = new BufferedReader(
				new InputStreamReader(incoming.getInputStream()));
         	out = new PrintWriter(
				incoming.getOutputStream(), true);

			boolean done = false;
			
			while (!done){  
				String str = in.readLine();
				if (str == null){
					done = true;
					
				}else{
					for(ChatHandler h : handlers){
						//synchronized(this) {//double check if this is helping
						if (str.matches("username_(.*)") == true){							
							userName = str.replace("username_", "");													
							setName(userName);
							h.out.println("Server: " + userName + " has entered the room.");						
						}else if(str.matches("update(.*)") == true) {
							for (int i = 0; i < handlers.size(); i++) {
								String hold = handlers.get(i).toString();
								hold = hold.replace("Thread[", "");
								hold = hold.replace(",5,main]", "");
								h.out.println("addUserToList: "+ hold);
							}
						}
						else if (str.matches("disconnect(.*)") == true){
							h.out.println("Server: " + userName + " has left the room.");
							h.out.println("removeUser: " + userName);
							done = true;
						}
						else {
							h.out.println(str);
							}
						//}	//sync end
					}
				}			
			}			
			incoming.close();
		}catch (Exception e){  
			System.out.println(e);			
		}finally{							
			handlers.remove(this);
		} 
	} 
}
