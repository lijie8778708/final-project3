import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;

public class Client extends JFrame implements Runnable {
	// Client_Name
	private String name;

	// Client's socket
	private Socket socket;

	// outputstream
	private PrintWriter out;

	// inputstream
	private BufferedReader in;

	// User close thread
	Thread receivethread;

	// label
	private JLabel label;

	// panel for button and label
	private JPanel buttomPanel, inputPanel, labelPanel, centenPanel;

	// display text here
	private JTextArea centerTextArea, inputTextArea, clientTextArea;

	// send and clear button
	private JButton send, clear;


	//Client
	public Client(String name, Socket socket) throws IOException {

		this.name = name;

		this.socket = socket;
		
		//log out ip and port number
		System.out.println(socket.getLocalPort());

		System.out.println(InetAddress.getLocalHost().getHostAddress());
		
		//get input stream
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		out = new PrintWriter(socket.getOutputStream());

		//send name to receiver
		out.println(name);
		out.flush();
		
		setTitle(name);

		//Client GUI
		create();

		//create a thread
		receivethread = new Thread(this);

		receivethread.start();

		//listening
		setActionLister();

	}

	//GUI
	private void create() {
		setSize(500, 500);

		// label
		label = new JLabel("Data and Computer Chatroom");

		label.setFont(new Font("Arial", 5, 15));

		labelPanel = new JPanel();

		labelPanel.setSize(150, 20);

		labelPanel.add(label, BorderLayout.CENTER);

		// window text
		centerTextArea = new JTextArea("    ---Chatroom connected---    " + "\r\n");

		centerTextArea.setFont(new Font("Arial", 5, 13));

		centerTextArea.setEditable(false); // Unchangeable

		centerTextArea.setBackground(Color.LIGHT_GRAY);

		// client window
		clientTextArea = new JTextArea("--Online Client--" + "\r\n");

		clientTextArea.setEditable(false); // unchangeable

		clientTextArea.setBorder(new LineBorder(null));

		// centerPanel 
		centenPanel = new JPanel(new BorderLayout());

		centenPanel.add(new JScrollPane(centerTextArea), BorderLayout.CENTER);

		centenPanel.add(clientTextArea, BorderLayout.EAST);

		// button
		send = new JButton("Send");

		clear = new JButton("Clear");

		buttomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		buttomPanel.add(clear);

		buttomPanel.add(send);

		// window input
		inputPanel = new JPanel(new BorderLayout());

		inputTextArea = new JTextArea();

		inputTextArea = new JTextArea(7, 20);

		inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

		inputPanel.add(buttomPanel, BorderLayout.SOUTH);

		add(labelPanel, BorderLayout.NORTH);

		add(centenPanel, BorderLayout.CENTER);

		add(inputPanel, BorderLayout.SOUTH);

		setVisible(true);

		setResizable(false); // fixed window size

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	//inpuTextArea
	private void setActionLister() {
		
		//close client 
		addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
				try {
					if(out!=null) {
						out.println("Good Bye");
						out.flush();
					}
				}
				catch (Exception e2) {
					System.out.println("Client Shut Down");
				}
			}
			
		});
		
		//Send message after click send button
		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				String aLine = inputTextArea.getText();
				
				inputTextArea.setText("");	//clear input text

				centerTextArea.append("You： " + aLine + "\r\n");

				try {
					
					out.println(aLine);

					out.flush();

					if (aLine.equals("Good Bye")) {
						
						
						receivethread.interrupt();

						socket.shutdownOutput();

						socket.shutdownInput();

						socket.close();
						
					}

				} catch (Exception e1) {

					System.out.println("Disconnect");

				}

			}
		});

		//keyboard listening
		inputTextArea.addKeyListener(new KeyAdapter() {
			
			public void keyReleased(KeyEvent e) {
				
				//CTRL+ENTER
				if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
					send.doClick();
				}

			}

		});

		//clear text
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				inputTextArea.setText("");
			}
		});

	}

	@Override
	public void run() {
		
		try {

			String aLine = "";

			while ((aLine = in.readLine()) != null) {
			
				//centerTextAre
				if (!aLine.split(":")[0].equals("update"))
					centerTextArea.append(aLine + "\r\n");
				
				//Update Client
				else {
					String[] strings = (aLine.split(":")[1]).split(" ");
					clientTextArea.setText("--Online Client--\r\n");
					for (String s : strings)
						clientTextArea.append(s + "\r\n");
				}
			}

		} catch (Exception e) {

			centerTextArea.append("Disconnected\r\n");
			
			System.out.println("Disconnected");

		}
	}

	public static void main(String[] args) throws IOException {

		new Client("Client", new Socket("localhost", 10086));
		
		// new clientGUI("Client2", new Socket("localhost", 10086));
	}

}
