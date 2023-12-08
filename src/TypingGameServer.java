
//1871139 신유진
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TypingGameServer extends JFrame {
	private int port;
	private JTextArea t_display;
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private ServerSocket serverSocket;
	private Thread acceptThread = null;
	private Vector<ClientHandler> users = new Vector<ClientHandler>();

	public TypingGameServer(int port) {
		buildGUI();
		setTitle("TypingGameServer");
		setSize(400, 300);
		this.port = port;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void buildGUI() {
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(createDisplayPanel(), BorderLayout.CENTER);
		contentPane.add(createControlPanel(), BorderLayout.SOUTH);
	}

	private JPanel createDisplayPanel() {
		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BorderLayout()); // JPanel 의 디폴트 배치관리자는 FlowLayout, BorderLayout으로 교체
		t_display = new JTextArea(15, 15);
		displayPanel.add(t_display);
		return displayPanel;
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(1, 3, -10, -10));
		button1 = new JButton("서버시작");
		button2 = new JButton("서버종료");
		button3 = new JButton("종료");
		button2.setEnabled(false);

		// 서버시작버튼
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptThread = new Thread(new Runnable() {
					@Override
					public void run() {
						startServer();
					}	
				});
				acceptThread.start();
				button1.setEnabled(false);
				button2.setEnabled(true);
			}
		});

		// 서버종료버튼
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
				button1.setEnabled(true);
				button2.setEnabled(false);
			}
		});

		// 프로그램 종료버튼
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
			}
		});

		controlPanel.add(button1);
		controlPanel.add(button2);
		controlPanel.add(button3);
		return controlPanel;
	}

	private void startServer() {
		Socket clientSocket;
		try {
			serverSocket = new ServerSocket(port);
			printDisplay("서버가 시작되었습니다.");
			while (acceptThread == Thread.currentThread()) {
				try {
					clientSocket = serverSocket.accept();
					printDisplay("클라이언트가 연결되었습니다.");
					// receiveMessages(clientSocket);
					ClientHandler cHandler = new ClientHandler(clientSocket);
					users.add(cHandler);
					cHandler.start();
					if(users.size() == 2) {
						Timer timer = new Timer();
						broadcasting(new ChatMsg("", ChatMsg.MODE_TX_STRING, "게임시작!"));
						
						
						TimerTask tt = new TimerTask() {
							@Override
							public void run() {
								broadcasting(new ChatMsg("", ChatMsg.MODE_TX_STRING, "게임종료!"));
							}
						};
						timer.schedule(tt, 60000); //60초동안게임
						
					}
				} catch(SocketException e){
					printDisplay("서버 소켓 종료!");
				}
				catch (IOException e) {
					printDisplay("입출력오류!");
					System.exit(-1);
				}
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void broadcasting(ChatMsg msg) {
		for (ClientHandler c : users) {
			c.send(msg);
		}
	}

	private void printDisplay(String s) {
		t_display.append(s + "\n");
		t_display.setCaretPosition(t_display.getDocument().getLength());
	}
	
	private void disconnect() {
		try {
			acceptThread = null;
			serverSocket.close();
		} catch (IOException e1) {
			System.err.println("서버 종료 오류> " + e1.getMessage());
		}
	}
	
	private class SendMsg extends Thread{
		
	}

	private class ClientHandler extends Thread {
		private Socket clientSocket;
		private ObjectOutputStream out;
		private String uid;
		
		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
			try {
				out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void receiveMessages(Socket cs) {
			try {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				
				String message; 
				ChatMsg msg;
				try {
					while ((msg = (ChatMsg)in.readObject()) != null) {
						if(msg.mode == ChatMsg.MODE_LOGIN) {
							uid = msg.userID;
							printDisplay("새 참가자:" + uid);
							printDisplay("현재 참가자 수 : " + users.size());
							continue;
						}
						else if (msg.mode == ChatMsg.MODE_LOGOUT) {
							break;
						}
						else if (msg.mode == ChatMsg.MODE_TX_STRING) {
							message = uid + ": " + msg.message;
							printDisplay(message);
							broadcasting(msg);
						}
						else if (msg.mode == ChatMsg.MODE_TX_IMAGE) {
							printDisplay(uid + ": " + msg.message);
							broadcasting(msg);
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				users.removeElement(this);
				printDisplay(uid + " 퇴장. 현재 참가자 수: " + users.size());
			} catch (IOException e) {
				users.removeElement(this);
				printDisplay(uid + " 연결 끊김. 현재 참가자 수: " + users.size());
			} finally {
				try {
					cs.close();
				} catch (IOException e) {
					System.err.println("서버 닫기 오류> " + e.getMessage());
					System.exit(-1);
				}
			}

		}
		
		private void sendMessage(String msg) {		
			send(new ChatMsg(uid, ChatMsg.MODE_TX_STRING, msg));	
		}
		
		public void broadcasting(ChatMsg msg) {
			for (ClientHandler c : users) {
				c.send(msg);
			}
		}
		
		private void send(ChatMsg msg) {
			try {
				out.writeObject(msg);
				out.flush();
			} catch (IOException e) {
				System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
			}
		
		}

		@Override
		public void run() {
		
			receiveMessages(clientSocket);
		}
	}

	public static void main(String[] args) {
		int port = 54321;
		new TypingGameServer(port);
	}
}
