import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

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
   private String serverAddress;
   private Object serverPort;
   private JTextArea t_display;

   public LoginFrame() {
      setTitle("로그인화면");
      setSize(550, 300);
      buildGUI();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false); // 크기 변경 불가
      setVisible(true);
   }

   public void buildGUI() {
      Container contentPane = getContentPane();
      contentPane.setLayout(null);
      
      
      JPanel subPanel = new JPanel();
      subPanel.setLayout(new GridLayout(3,0));
//      subPanel.add(createInfoPanel());
      subPanel.add(createInputPanel());
      subPanel.add(createControlPanel());
      
      contentPane.add(createDisplayPanel(), BorderLayout.CENTER);
      contentPane.add(subPanel, BorderLayout.SOUTH);
   }
   
//      private JPanel createInfoPanel() {
//         JPanel InfoPanel = new JPanel();
//         InfoPanel.setLayout(new BorderLayout());
//         
//            createDisplayPanel();
//            printDisplay("타일 뒤집기 게임");
//         
//         JLabel l = new JLabel("타일 뒤집기 게임");
//         l.setHorizontalAlignment(JLabel.CENTER);
//         l.setVerticalAlignment(JLabel.CENTER);
//
//         
//         l.setFont(new Font("SansSerif", Font.BOLD, 24));
//         InfoPanel.add(l, BorderLayout.NORTH);
//         
//         
//         return InfoPanel;
//         
//      }
//   
   
   
      private JPanel createDisplayPanel() {
         JPanel displayPanel = new JPanel();
         displayPanel.setLayout(new BorderLayout());
         
         JLabel l = new JLabel("영어 타자 게임");
         l.setHorizontalAlignment(JLabel.CENTER);
         l.setVerticalAlignment(JLabel.CENTER);

         
         l.setFont(new Font("SansSerif", Font.BOLD, 24));
         displayPanel.add(l, BorderLayout.NORTH);

         return displayPanel;
      }

      private JPanel createInputPanel() {
      JPanel InputPanel = new JPanel();
      InputPanel.setLayout(new GridLayout(3, 2, 5, 5));      
      t_userID = new JTextField(8);
      t_hostAddr = new JTextField(10);
      t_portNum = new JTextField(5);
      
      JLabel label1 = new JLabel("아이디: ");
      JLabel label2 = new JLabel("서버주소: ");
      JLabel label3 = new JLabel("포트번호: ");
      
      t_portNum.setHorizontalAlignment(JTextField.CENTER);
      
      InputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
      
      InputPanel.add(label1);
      InputPanel.add(t_userID);
      InputPanel.add(label2);
      InputPanel.add(t_hostAddr);
      InputPanel.add(label3);
      InputPanel.add(t_portNum);
      
      return InputPanel;

}
      
   private JPanel createControlPanel() {
      JPanel controlPanel = new JPanel();
      controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      
      JButton btn = new JButton("게임시작"); //게임시작 버튼

      btn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
//            String t_portNum = t_portNum.getText(); // 아이피주소  
            try {
//               int portNum = Integer.parseInt(portTf.getText()); // 문자열 정수 변환, 포트번호
//                new TypingGameClient(ipNum, portNum); //클라이언트 실행
                setVisible(false); //화면 사라지게
            } catch (NumberFormatException e1) {
               System.out.println("숫자변환불가");   
            }
         }
      });
      
      btn.setPreferredSize(new java.awt.Dimension(100, 30));
      
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