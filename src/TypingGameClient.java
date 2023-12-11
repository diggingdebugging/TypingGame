
//1871139 신유진
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TypingGameClient extends JFrame {
	private Thread receiveThread = null;
	private JTextField t_input;
	private JButton b_connect;
	private JButton b_disconnect;
	private JButton b_exit;
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String uid = "defaultUser";
	private JPanel displayPanel;
	private String[] strArr;
	private JLabel[] wordLabels;
	private JLabel scoreLabel;
	private int score = 0; // 점수
	private JTextArea textArea;

	public TypingGameClient(String serverAddress, int serverPort, String uid) {// 생성
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.uid = uid;

		setTitle("타자게임");
		setSize(600, 400);
		buildGUI();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false); // 크기 변경 불가
		setVisible(true);
	}

	private void buildGUI() {// contentPane에 3가지의 Panel을 부착한다.
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		contentPane.add(createDisplayPanel()); // 단어가 출력되는 Panel
		contentPane.add(createInputPanel()); // 단어를 입력하는 Panel
		contentPane.add(createSubPanel()); // 오른쪽에 기능을 담당하는 Panel
	}

	private JPanel createDisplayPanel() {
		displayPanel = new JPanel();
		displayPanel.setBounds(10, 10, 450, 320);
		displayPanel.setLayout(new GridLayout(4, 4));
		displayPanel.setBackground(Color.WHITE);

		return displayPanel;
	}

	private JPanel createInputPanel() {
		JPanel inputPanel = new JPanel();
		inputPanel.setBounds(150, 330, 150, 50);

		// 입력창 텍스트필드
		t_input = new JTextField(10);
		t_input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMessage(); // 엔터키를 입력받으면 sendMessage()
			}
		});

		inputPanel.add(t_input);
		t_input.setEditable(false); // 게임이 매칭되기전까지는 입력불가능

		return inputPanel;
	}

	private JPanel createSubPanel() { // controlPanel과 InfoPanel의 정보를 담고있음
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(2, 0));
		subPanel.setBackground(Color.WHITE);
		subPanel.setBounds(470, 10, 120, 320);
		// subPanel.add(createLabelPanel());
		subPanel.add(createControlPanel());
		subPanel.add(createInfoPanel());
		return subPanel;
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel(new GridLayout(5, 0));

		JLabel label = new JLabel("점수");
		controlPanel.add(label);

		scoreLabel = new JLabel(Integer.toString(score));
		controlPanel.add(scoreLabel);

		JLabel userIdLabel = new JLabel("<html><br> id : " + uid + "</html>");
		controlPanel.add(userIdLabel);

		// 접속 button
		b_connect = new JButton("게임매칭");
		b_connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					connectToServer();
					sendUserID();
				} catch (UnknownHostException e1) {
					return;
				} catch (IOException e1) {
					
					return;
				} // 버튼을 클릭하면 connectToServer()

				t_input.setEditable(true);

				b_connect.setEnabled(false);
				b_disconnect.setEnabled(true);
				b_exit.setEnabled(false);
			}
		});
		controlPanel.add(b_connect);

		// 종료하기 button
		b_exit = new JButton("종료하기");
		b_exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0); // 버튼을 클릭하면 프로그램종료
			}
		});

		controlPanel.add(b_exit);

		return controlPanel;
	}

	private JPanel createInfoPanel() {
		JPanel infoPanel = new JPanel();
		textArea = new JTextArea(7, 10);
		infoPanel.add(textArea);
		return infoPanel;
	}

	private void connectToServer() throws UnknownHostException, IOException { // connetToServer에서 예외를 직접처리하지 않고 메소드를 호출한다															
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
						// printDisplay("서버 연결 끊김");
						return;
					}

					switch (inMsg.mode) {
					case ChatMsg.MODE_TX_START: // 시작 메세지
						textArea.append(inMsg.message + "\n");
						break;
					case ChatMsg.MODE_TX_FINISH:// 종료 메세지
						textArea.append(inMsg.message + "\n");
						t_input.setEditable(false);
						b_disconnect.setEnabled(false);
						b_exit.setEnabled(true);
						sendScore();
						break;
					case ChatMsg.MODE_TX_WINNER:
						textArea.append(inMsg.message + "\n");
						break;
					case ChatMsg.MODE_TX_CORRECT:// 정답 메세지
						search(inMsg);
						break;
					case ChatMsg.MODE_TX_TEXTFILE:// 단어 파일
						String data = new String(inMsg.fileData);
						strArr = data.split(" "); // 클래스의 멤버변수
						for (int i = 0; i < strArr.length; i++) {
							wordLabels = new JLabel[strArr.length]; // 클래스의 멤버변수

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
					
				}
			}

			@Override
			public void run() {
				try {
					in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				} catch (IOException e) {
					
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

	private void sendMessage() { // 정답 서버로 보내기
		String message = t_input.getText();
		if (message.isEmpty())
			return;
		send(new ChatMsg(uid, ChatMsg.MODE_TX_WORD, message));
		t_input.setText(""); // t_input창 비우기
	}

	private void sendScore() { // 점수 서버로 보내기
		send(new ChatMsg(uid, ChatMsg.MODE_TX_SCORE, score));
	}

	private void search(ChatMsg inMsg) {
		String s = inMsg.message;
		for (int i = 0; i < strArr.length; i++) {
			if (s.equals(strArr[i])) {
				int index = i;
				SwingUtilities.invokeLater(() -> {
					displayPanel.remove(wordLabels[index]);
					displayPanel.revalidate();
					displayPanel.repaint();
				});

				if (uid.equals(inMsg.userID)) {
					score++;
					scoreLabel.setText(Integer.toString(score));
				}
				break;
			}
		}
	}

	private void sendUserID() {
		send(new ChatMsg(uid, ChatMsg.MODE_LOGIN));
	}
}
