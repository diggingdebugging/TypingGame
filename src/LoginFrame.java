import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {
   private JTextField t_userID;
   private JTextField t_portNum;
   private JTextField t_hostAddr;
   private String serverAddress = "localhost";
   private Object serverPort = 54321;
   private JTextArea t_display;

   public LoginFrame() {
      setTitle("로그인화면");
      setSize(400, 350);
      buildGUI();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(true); // 크기 변경 불가
      setVisible(true);
      getContentPane().setLayout(null);
   }

   public void buildGUI() {
      Container contentPane = getContentPane();
      contentPane.setBackground(Color.WHITE);
      contentPane.setLayout(new BorderLayout());
      
      JPanel subPanel = new JPanel();
      subPanel.setLayout(new GridLayout(3,0));
      subPanel.setBackground(Color.WHITE);
      subPanel.add(createDisplayPanel());
      subPanel.add(createInputPanel());
      subPanel.add(createControlPanel());
      contentPane.add(subPanel);
      
   }
   
   
      private JPanel createDisplayPanel() {
         JPanel displayPanel = new JPanel();
         displayPanel.setBackground(Color.WHITE);
         
         JLabel l = new JLabel("단어 맞히기 게임");
         l.setBounds(30,30,140,50);
         l.setHorizontalAlignment(JLabel.CENTER);
         l.setVerticalAlignment(JLabel.CENTER);         
         l.setFont(new Font("궁서", Font.BOLD, 30));
         displayPanel.add(l);
         
         
         return displayPanel;
      }

      
      

      private JPanel createInputPanel() {
          JPanel inputPanel = new JPanel();

          inputPanel.setBounds(30,90,330,230);
         inputPanel.setBackground(Color.WHITE);
         inputPanel.setLayout(null);

          JLabel label1 = new JLabel("아이디: ");
          label1.setFont(new Font("궁서", Font.BOLD, 13));
          label1.setBounds(10, 10, 50, 20);
          inputPanel.add(label1);

          t_userID = new JTextField(8);
          t_userID.setFont(new Font("궁서", Font.BOLD, 13));
          t_userID.setBounds(70, 10, 100, 20);
          inputPanel.add(t_userID);

          JLabel label2 = new JLabel("서버주소: ");
          label2.setFont(new Font("궁서", Font.BOLD, 13));
          label2.setBounds(10, 40, 70, 20);
          inputPanel.add(label2);

          t_hostAddr = new JTextField(10);
          t_hostAddr.setFont(new Font("궁서", Font.BOLD, 13));
          t_hostAddr.setBounds(70, 40, 100, 20);
          inputPanel.add(t_hostAddr);

          JLabel label3 = new JLabel("포트번호: ");
          label3.setFont(new Font("궁서", Font.BOLD, 13));
          label3.setBounds(10, 70, 70, 20);
          inputPanel.add(label3);

          t_portNum = new JTextField(5);
          t_portNum.setFont(new Font("궁서", Font.BOLD, 13));
          t_portNum.setBounds(70, 70, 100, 20);
          inputPanel.add(t_portNum);
          
          JLabel label4 = new JLabel("<html>*제한시간 60초*<br>상대방보다 빨리 단어를 입력하여<br>더 많은 점수를 얻으면 승리</html>");
          label4.setFont(new Font("궁서",Font.BOLD, 12));
          label4.setBounds(180,-30,200,150);
          inputPanel.add(label4);
          
          t_hostAddr.setText(serverAddress);
          t_portNum.setText(String.valueOf(serverPort));
          

          return inputPanel;
      }

      
      
   private JPanel createControlPanel() {
      JPanel controlPanel = new JPanel();
      controlPanel.setBackground(Color.WHITE);
      controlPanel.setLayout(null);
      
      JButton btn = new JButton("게임시작"); //게임시작 버튼
      btn.setFont(new Font("궁서", Font.BOLD, 13));
      
      btn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            String ipNum = t_hostAddr.getText(); // 아이피주소
            String idNum = t_userID.getText();
            try {
               int portNum = Integer.parseInt(t_portNum.getText()); // 문자열 정수 변환, 포트번호
                new TypingGameClient(ipNum, portNum, idNum); //클라이언트 실행
                setVisible(false); //화면 사라지게
            } catch (NumberFormatException e1) {
               System.out.println("숫자변환불가");   
            }
         }
      });
      
      btn.setBounds(200,20,160,30);
      controlPanel.setBounds(200,10,180,230);
      controlPanel.setLayout(null);
      controlPanel.add(btn);
      
      return controlPanel;
   }
   private void printDisplay(String s) {
      t_display.append(s+ "\n");
      t_display.setCaretPosition(t_display.getDocument().getLength());
      
   }


   public static void main(String [] args) {
      new LoginFrame(); 
   }
}