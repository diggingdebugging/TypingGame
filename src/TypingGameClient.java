//1871139 신유진
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class TypingGameClient extends JFrame {
	private JTextPane t_display;
	private DefaultStyledDocument document;
	private JTextField t_input;
	private JButton b_connect;
	private JButton b_disconnect;
	private JButton b_send;
	private JButton b_select;
	private JButton b_exit;
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Thread receiveThread = null;
	private JTextField t_userID;
	private JTextField t_hostAddr;
	private JTextField t_portNum;
	private String uid;
	private JPanel displayPanel;
	private String[] strArr;
	private JLabel [] wordLabels; 

	public TypingGameClient(String serverAddress, int serverPort) {// 생성
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		setTitle("TypingGameClient");
		setSize(600, 400);
		buildGUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false); //크기 변경 불가
		setVisible(true); 	
	}

	private void buildGUI() {// contentPane에 3가지의 Panel을 부착한다.
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JPanel subPanel = new JPanel(); // 서브패널
		subPanel.setLayout(new GridLayout(3, 0));
		subPanel.add(createInputPanel());
		subPanel.add(createInfoPanel());
		subPanel.add(createControlPanel());

		contentPane.add(createDisplayPanel(), BorderLayout.CENTER);
		contentPane.add(createScorePanel(), BorderLayout.EAST);
		contentPane.add(subPanel, BorderLayout.SOUTH);
	}

	private JPanel createDisplayPanel() {
		displayPanel = new JPanel();
		// displayPanel.setLayout(new BorderLayout());
		displayPanel.setLayout(new GridLayout(3, 3));
		displayPanel.setBackground(Color.WHITE);
		// displayPanel.setSize();
		// document = new DefaultStyledDocument();
		// t_display = new JTextPane(document);
		// t_display.setEditable(false);
		// displayPanel.add(new JScrollPane(t_display), BorderLayout.CENTER);
		return displayPanel;
	}

	private JPanel createInputPanel() {
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());//

		// 입력창 텍스트필드
		t_input = new JTextField(30);
		t_input.addActionListener(new ActionListener() { // 이너클래스, 익명함수
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(); // 엔터키를 입력받으면 sendMessage()
			}
		});

		// 보내기 버튼
		b_send = new JButton("보내기");
		b_send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(); // 버튼을 클릭하면 sendMessage()
			}
		});

		b_select = new JButton("선택하기");
		b_select.addActionListener(new ActionListener() {

			JFileChooser chooser = new JFileChooser();

			@Override
			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF & PNG Images", "jpg", "gif",
						"png");
				chooser.setFileFilter(filter);

				int ret = chooser.showOpenDialog(TypingGameClient.this);
				if (ret != JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(TypingGameClient.this, "파일을 선택하지 않았습니다");
				}

				t_input.setText(chooser.getSelectedFile().getAbsolutePath());
				sendImage();
			}
		});

		inputPanel.add(t_input, BorderLayout.CENTER);
		JPanel p_button = new JPanel(new GridLayout(1, 0));
		p_button.add(b_select);
		p_button.add(b_send);
		inputPanel.add(p_button, BorderLayout.EAST);

		t_input.setEditable(false);
		b_select.setEnabled(false);
		b_send.setEnabled(false);

		return inputPanel;
	}
	
	private JPanel createScorePanel() {
		JPanel scorePanel = new JPanel();
		scorePanel.setLayout(new BorderLayout());
		JLabel scoreLabel = new JLabel("점수");
		scorePanel.add(scoreLabel, BorderLayout.NORTH);
		return scorePanel;
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(0, 3));

		// 접속하기 버튼
		b_connect = new JButton("접속하기");
		b_connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TypingGameClient.this.serverAddress = t_hostAddr.getText();
				TypingGameClient.this.serverPort = Integer.parseInt(t_portNum.getText());

				try {
					connectToServer();
					sendUserID();
				} catch (UnknownHostException e1) {
					printDisplay("서버 주소와 포트번호를 확인하세요: " + e1.getMessage());
					return;
				} catch (IOException e1) {
					printDisplay("서버와의 연결 오류: " + e1.getMessage());
					return;
				} // 버튼을 클릭하면 connectToServer()
				// JFrame f = new JFrame(); 

				t_input.setEditable(true);
				b_send.setEnabled(true);
				b_select.setEnabled(true);

				b_connect.setEnabled(false);
				b_disconnect.setEnabled(true);
				b_exit.setEnabled(false);

				t_userID.setEditable(false);
				t_hostAddr.setEditable(false);
				t_portNum.setEditable(false);
			}
		});

		// 접속끊기 버튼
		b_disconnect = new JButton("접속끊기");
		b_disconnect.setEnabled(false);
		b_disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect(); // 버튼을 클릭하면 disconnect()
				t_input.setEditable(false);// 입력창 비활성화
				b_send.setEnabled(false);// 보내기버튼 끄기
				b_select.setEnabled(false);

				b_connect.setEnabled(true);// 접속하기 버튼 켜기
				b_disconnect.setEnabled(false);
				b_exit.setEnabled(true);

				t_userID.setEditable(true);
				t_hostAddr.setEditable(true);
				t_portNum.setEditable(true);
			}
		});

		// 종료하기 버튼
		b_exit = new JButton("종료하기");
		b_exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0); // 버튼을 클릭하면 프로그램종료
			}
		});

		controlPanel.add(b_connect, BorderLayout.WEST);
		controlPanel.add(b_disconnect, BorderLayout.CENTER);
		controlPanel.add(b_exit, BorderLayout.EAST);
		return controlPanel;
	}

	private JPanel createInfoPanel() {
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		t_userID = new JTextField(7);
		t_hostAddr = new JTextField(12);
		t_portNum = new JTextField(5);

		JLabel label1 = new JLabel("아이	디: ");
		JLabel label2 = new JLabel("서버주소: ");
		JLabel label3 = new JLabel("포트번호: ");

		t_userID.setText("guest" + getLocalAddr().split("\\.")[3]);
		t_hostAddr.setText(serverAddress);
		t_portNum.setText(String.valueOf(serverPort));
		t_portNum.setHorizontalAlignment(JTextField.CENTER);

		infoPanel.add(label1);
		infoPanel.add(t_userID);
		infoPanel.add(label2);
		infoPanel.add(t_hostAddr);
		infoPanel.add(label3);
		infoPanel.add(t_portNum);

		return infoPanel;
	}

	private void connectToServer() throws UnknownHostException, IOException { // connetToServer에서 예외를 직접처리하지 않고 메소드를 호출한
																				// 호출측으로 전
		socket = new Socket(); // 빈 소켓 객체 생성
		SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
		socket.connect(sa, 3000); // 3000ms, 이 서버에 3초안에 연결을 요청, 3초가 넘어가면 타임아웃! 연결 시도를 중단

		out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream())); // not block

		receiveThread = new Thread(new Runnable() {
			private void receiveMessage() {
				try {
					ChatMsg inMsg = (ChatMsg) in.readObject();
					if (inMsg == null) {
						disconnect();
						printDisplay("서버 연결 끊김");
						return;
					}

					switch (inMsg.mode) {
					case ChatMsg.MODE_TX_STRING:
						printDisplay(inMsg.userID + ": " + inMsg.message);
						break;
					case ChatMsg.MODE_TX_START:
						System.out.println(inMsg.message);
						/*
						 * SwingUtilities.invokeLater(() -> { JLabel startLabel = new
						 * JLabel(inMsg.message); startLabel.setFont(new Font("휴먼엑스포", Font.BOLD, 18));
						 * startLabel.setForeground(Color.WHITE); displayPanel.add(startLabel);
						 * displayPanel.revalidate(); displayPanel.repaint(); });
						 */
					case ChatMsg.MODE_TX_FINISH:
						break;
					case ChatMsg.MODE_TX_CORRECT: //맞으면 안보이게
						search(inMsg.message);
						break;
					case ChatMsg.MODE_TX_IMAGE:
						printDisplay(inMsg.userID + ": " + inMsg.message);
						printDisplay(inMsg.image);
						break;
					case ChatMsg.MODE_TX_TEXTFILE:
						String data = new String(inMsg.fileData);
						strArr = data.split(" "); //클래스의 멤버변수

						for (int i = 0; i < strArr.length; i++) {
							/*int index = i;
							SwingUtilities.invokeLater(() -> {
								JLabel wordLabel = new JLabel(strArr[index]);
								wordLabel.setFont(new Font("휴먼엑스포", Font.BOLD, 18));
								wordLabel.setForeground(Color.BLACK);
								displayPanel.add(wordLabel);
								displayPanel.revalidate();
								displayPanel.repaint();
							});*/
							wordLabels = new JLabel[strArr.length]; //클래스의 멤버변수
							
							int index = i;
							SwingUtilities.invokeLater(() -> {
								wordLabels[index] = new JLabel(strArr[index]);
								wordLabels[index].setFont(new Font("휴먼엑스포", Font.BOLD, 18));
								wordLabels[index].setForeground(Color.BLACK);
								displayPanel.add(wordLabels[index]);
								displayPanel.revalidate();
								displayPanel.repaint();
							});
						}

					}

				} catch (IOException e) {
					System.err.println("클라이언트 일반 수신 오류> " + e.getMessage());
				} catch (ClassNotFoundException e) {
					printDisplay("잘못된 객체가 전달되었습니다.");
				}
			}

			@Override
			public void run() {
				try {
					in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				} catch (IOException e) {
					printDisplay("입력 스트림이 열리지 않음");
				}
				while (receiveThread == Thread.currentThread()) {
					receiveMessage();
				}
			}
		});
		receiveThread.start();
	}

	public void disconnect() {
		send(new ChatMsg(uid, ChatMsg.MODE_LOGOUT));

		try {
			receiveThread = null;
			socket.close();
		} catch (IOException e) {
			System.err.println("클라이언트 닫기 오류> " + e.getMessage());
			System.exit(-1);
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

	private void sendMessage() {
		String message = t_input.getText();
		if (message.isEmpty())
			return;
		send(new ChatMsg(uid, ChatMsg.MODE_TX_STRING, message));
		t_input.setText(""); // t_input창 비우기
	}
	
	private void search(String s) {
		for(int i = 0; i < strArr.length; i++) {
			if(s.equals(strArr[i])) {
				int index = i;
				SwingUtilities.invokeLater(() -> {
				    displayPanel.remove(wordLabels[index]);
				    displayPanel.revalidate();
				    displayPanel.repaint();
				});
				break;
			}
		}
	}

	private void sendUserID() {
		uid = t_userID.getText();
		send(new ChatMsg(uid, ChatMsg.MODE_LOGIN));
	}

	private String getLocalAddr() {
		InetAddress local = null;
		String addr = "";
		try {
			local = InetAddress.getLocalHost();
			addr = local.getHostAddress();
			System.out.println(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return addr;
	}

	private void sendImage() {
		String filename = t_input.getText().strip();
		if (filename.isEmpty())
			return;

		File file = new File(filename);
		if (!file.exists()) {
			printDisplay(">> 파일이 존재하지 않습니다: " + filename);
			return;
		}

		ImageIcon icon = new ImageIcon(filename);
		send(new ChatMsg(uid, ChatMsg.MODE_TX_IMAGE, file.getName(), icon));

		t_input.setText("");
	}

	private void printDisplay(String msg) {
		int len = t_display.getDocument().getLength();
		try {
			document.insertString(len, msg + "\n", null);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		t_display.setCaretPosition(len);
	}

	private void printDisplay(ImageIcon icon) {
		t_display.setCaretPosition(t_display.getDocument().getLength());

		if (icon.getIconWidth() > 400) {
			Image img = icon.getImage();
			Image changeImg = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
			icon = new ImageIcon(changeImg);
		}

		t_display.insertIcon(icon);

		printDisplay("");
		t_input.setText("");
	}

	public static void main(String[] args) {
		String serverAddress = "localhost";
		int serverPort = 54321;
		new TypingGameClient(serverAddress, serverPort);
	}
}
