import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public class Server extends JFrame {

	// server 
	private ServerSocket server;

	// Client's socket
	private Socket socket;

	// Client
	List<Client> list;

	// Window's label
	private JLabel label;

	
	private JPanel labelPanel, clientPanel;

	// Display
	private JTextArea centerTextArea, clientTextArea;

	public Server(int port) throws IOException {

		server = new ServerSocket(port);
		
		//log out ip address and port number
		System.out.println(server.getLocalPort());

		System.out.println(InetAddress.getLocalHost().getHostAddress());

		//Create a linkedlist to store client
		list = new ArrayList<Client>();

		setTitle("Server");

		//Graphic interface
		create();
		
		//Add client
		addClient();

	}

	private void create() {
		setSize(500, 500);

		// Display server
		label = new JLabel("Data and computer class Server");

		label.setFont(new Font("Arial", 5, 15));

		labelPanel = new JPanel();

		labelPanel.setSize(150, 20);

		labelPanel.add(label, BorderLayout.CENTER);

	
		centerTextArea = new JTextArea("    ---Chatroom connected---    " + "\r\n");

		centerTextArea.setFont(new Font("Arial", 5, 13));

		centerTextArea.setEditable(false); // Unchangeable

		centerTextArea.setBackground(Color.LIGHT_GRAY);

		// Display current Clients

		clientTextArea = new JTextArea("--Online Client--" + "\r\n");

		clientTextArea.setEditable(false); // Unchangeable

		//Container
		clientPanel = new JPanel(new FlowLayout());

		clientPanel.setBorder(new LineBorder(null));

		clientPanel.add(clientTextArea);

		//JFrame
		add(clientPanel, BorderLayout.EAST);

		add(labelPanel, BorderLayout.NORTH);

		add(new JScrollPane(centerTextArea), BorderLayout.CENTER);

		setVisible(true);

		setResizable(false); // fixed window size

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	// add clients
	private void addClient() throws IOException {

		//listening on clients
		while (true) {

			// accept client and save it
			socket = server.accept();

			// display how many clients are online
			String ip = socket.getInetAddress().getHostAddress();

			centerTextArea.append(ip + "Successfully connected，Client's number：" + (list.size() + 1) + "\r\n");

			Client client = new Client(socket);

			// add to client list
			list.add(client);

			// create a new thread for client
			new Thread(client).start();
			
			// update client
			client.update();

		}

	}

	class Client implements Runnable {

		String name;

		Socket socket;

		// outputstream
		private PrintWriter out;

		// inputstream
		private BufferedReader in;

		public Client(Socket socket) throws IOException {

			this.socket = socket;

			// get input stream
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// get output stream
			out = new PrintWriter(socket.getOutputStream());
			
			//clientTextArea
			name = in.readLine();
			
			clientTextArea.append(name+"\r\n");

		}

		//send message to others
		public void send(String str) {
			out.println(str);
			out.flush();
		}

		
		public void update() {
			
			StringBuffer sBuffer = new StringBuffer("update:");
			
			clientTextArea.setText("--Online Client--\r\n");
			
			for(int i = 0;i<list.size();i++) {
				clientTextArea.append(list.get(i).name+"\r\n");
				sBuffer.append(list.get(i).name+" ");
			}
			
			for(int i = 0;i<list.size();i++) {
				list.get(i).out.println(sBuffer);
				list.get(i).out.flush();
			}
			
		}

		@Override
		public void run() {

			try {

				String aLine;

				boolean flag = true;

				while (flag && (aLine = in.readLine()) != null) {

					//add clients name
					String str = this.name + " said：" + aLine;

					//display text
					centerTextArea.append(str+"\r\n");

				
					for (int i = 0; i < list.size(); i++) {

						Client client = list.get(i);

						if (client != this) {

							client.send(str);

							
							if (aLine.equals("Good Bye")) {

								client.send(this.name + "Disconnect");

								flag = false;

							}

						}
					}

				}


			} catch (Exception e1) {
				

			} finally {
				try {
					
					
					//1.remove client from current list
					if (list.contains(this))
						list.remove(this);

					
					update();

					System.out.println(this.name+"Left");
					
					//3.output stream
					socket.shutdownOutput();

					socket.shutdownInput();
					
					socket.close();
					
				} catch (Exception e) {
					
					System.out.println(this.name + "Somethings wrong, terminate the program now");
					
				}
			}
		}

	}

	public static void main(String[] args) throws IOException {
		Server Test = new Server(10086);
	}

}