import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;

public class ClientWindow extends Panel implements ActionListener, ItemListener, Runnable{
	static gcChatFrame myFrame;
	public static void main(String[] args){
		myFrame = new gcChatFrame();
		
	}
	TextField tf,enterNameField;
	TextArea ta;
	Panel buttonPanel,mainPanel,cardPanel,enterNamePanel;
	CardLayout cl;
	List userNames_GUI;//gui element syncs to UserNames_DATA
	
	Button connect,disconnect;
	Label enterNameLabel;
	
	String userName, temp, sel_temp, selectedUsers;
	ArrayList<String>userNames_DATA;// data structure to contain userNames
  
	Socket s;
	BufferedReader in;
	PrintWriter out;
	Thread t;
	boolean connected = false;
	
	public ClientWindow(){		
		setLayout(new BorderLayout());
		
		tf = new TextField();
		tf.addActionListener(this);
		add(tf, BorderLayout.NORTH);
		
		ta = new TextArea();
		ta.setEditable(false);
		
		userNames_GUI = new List(4,true);	
		userNames_GUI.addItemListener(this);
		userNames_DATA = new ArrayList<String>();
		
		connect = new Button("Connect");
		connect.addActionListener(this);
		disconnect = new Button("Disconnect");
		disconnect.addActionListener(this);
		
		mainPanel = new Panel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(ta, BorderLayout.CENTER);		
		mainPanel.add(userNames_GUI, BorderLayout.EAST);
		
		buttonPanel = new Panel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(connect);
		buttonPanel.add(disconnect);
		
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		enterNameLabel = new Label("Enter Name");
		enterNamePanel = new Panel();
		
		enterNameField = new TextField();
		enterNameField.addActionListener(this);
		
		
		enterNamePanel.add(enterNameField);
		enterNamePanel.add(enterNameLabel);
				
		cardPanel = new Panel();
		cl = new CardLayout();
		cardPanel.setLayout(cl);
		cardPanel.add(enterNamePanel);		
		cardPanel.add(mainPanel, "Main Panel");
		add(cardPanel);
		
	}
	public void itemStateChanged(ItemEvent ie){
		List temp = (List)ie.getItemSelectable();	
		selectedUsers = "";
		
		if(ie.getStateChange() == ItemEvent.SELECTED){
			
			String[] items = temp.getSelectedItems();
			//sel_temp = sel;
			//System.out.println("New Selection Made: ");
			//sel_temp = "private_";
			
			for(int i = 0; i< items.length; i++){
				System.out.println(items[i]);
				if (selectedUsers.contains(items[i]) == false) {
					selectedUsers = selectedUsers.concat(items[i] + "_");
					//selectedUsers = selectedUsers + "*";
					//System.out.println(selectedUsers);
				}	
				//sel_temp = sel_temp + items[i] + "_";
			}
			//selectedUsers = sel_temp;
			tf.setText("private_" + selectedUsers + "from_" + userName + ":" );
		}
		//if(ie.getStateChange() == ItemEvent.DESELECTED){		}
		//System.out.println("Username Selected: " + sel);//console debug
	}
	public void actionPerformed(ActionEvent ae){		
		String temp = tf.getText();
		tf.setText("");
		
		if(ae.getSource() == enterNameField) {
			userName = enterNameField.getText();			
			System.out.println("Username:" + userName);//Shows which console this user is in
			myFrame.setTitle("Chat Client: " + userName);
			cl.show(cardPanel, "Main Panel");
		}
		else if(ae.getSource() == tf) {
			if (s != null) {
				if (temp.matches("private(.*)") == true) {
					out.println(temp);	//change userNames_GUI so that it sets tf with private
					
				}else {
				out.println(userName + ": " + temp);
				}			
			}else {
				ta.append("Not connected to server");//maybe update this to display in the frame?
			}
		}		
		else if (ae.getSource() == connect) {			
			try{
				if (connected == false) {
					s = new Socket("localhost", 3000);
					in = new BufferedReader(new InputStreamReader(s.getInputStream()));
					out = new PrintWriter(s.getOutputStream(), true);
					t = new Thread(this,"Whatever");
					t.start();
					connected = true;
					out.println("username_" + userName);//tells the server this client's username
					out.println("update");//asks the server to send a list of all users currently connected			
				}else {
					ta.append("Already Connected" + "\n");
				}
			}catch(UnknownHostException uhe){
				System.out.println(uhe.getMessage());
			}catch(IOException ioe){
				System.out.println(ioe.getMessage());
			}
		}else if(ae.getSource() == disconnect) {
			try {
				out.println("disconnect");
				connected = false;
				userNames_GUI.removeAll();
				userNames_DATA.clear();
				s.close();
			}catch(IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}
	public void run(){
		try{
			for(;;){			
					temp = in.readLine();
				
					
					if (temp.matches("addUserToList: (.*)") == true) {	
						temp = temp.replace("addUserToList: ", "");
						if (userNames_DATA.contains(temp)) {
						}else {
							userNames_DATA.add(temp);	
							userNames_GUI.removeAll();
							for (int i = 0; i < userNames_DATA.size(); i++) {//for each object in userNames_DATA add it to the gui's list								
								userNames_GUI.add(userNames_DATA.get(i));
							}
						}
					}else if (temp.matches("removeUser: (.*)") == true) {
						temp = temp.replace("removeUser: ", "");
						if (userNames_DATA.contains(temp)) {
							userNames_DATA.remove(temp);
							userNames_GUI.remove(temp);
						}											
					}else if (temp.matches("private_(.*)") == true) {//if the client's username is present show the message
						
						temp = temp.replace("private_", "");					//each additional user address is name_ name_ 
				System.out.println(temp + "after private filter"); //sender only came this far

						
						if (temp.contains("_" + userName + ":") == true) {//if client is the sender
				System.out.println("I am the sender");
							temp = temp.replace(selectedUsers, "");
							temp = temp.replace("_" + userName + ":", "to " + selectedUsers + ":");
							temp = temp.replace("from", "");
							temp = temp.replace("_", " ");
							ta.append(temp + "\n");
	
						}else if (temp.contains(userName + "_") == true) {//if client is among the receivers
							System.out.println("I am the recipient");
								temp = temp.replace("_", " ");
								temp = "to " + temp;
								ta.append(temp + "\n");
								}			
					}else {
						ta.append(temp + "\n");
					}
			
			}
		}catch(IOException ioe){
			System.out.println(ioe.getMessage());		
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
class gcChatFrame extends Frame{		
	/*the main method was removed from gcChatFrame(ChatFrame) so that this class could be inherited by gcFinalProject(ChatPanel)
	 * this achieved the changing of the title and the Frame sending an exit message when closed.
	 */
	public gcChatFrame(){		
		setTitle("Chat Client");
		setSize(500,500);
		
		ClientWindow myPanel = new ClientWindow();
		add(myPanel, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				if (myPanel.s == null) {// Socket s is null until the button "connect" has been pressed
					System.exit(0);
				}else {
					myPanel.out.println("disconnect");//sends exit message if the user hits the exit button on the frame
					System.exit(0);
				}
			}
		});	
		setVisible(true);
		}
}
