import java.io.Serializable;

import javax.swing.ImageIcon;

public class ChatMsg implements Serializable {
	// mode와 관련한 16진수 상수
	public final static int MODE_LOGIN  = 0x1;
	public final static int MODE_LOGOUT  = 0x2;
	public final static int MODE_TX_START  = 0x11;
	public final static int MODE_TX_FINISH  = 0x12;
	public final static int MODE_TX_WORD = 0x13;
	public final static int MODE_TX_TEXTFILE = 0x20;
	public final static int MODE_TX_SCORE = 0x30;
	public final static int MODE_TX_CORRECT = 0x60;
	public final static int MODE_TX_WINNER = 0x70;
	
	//같은 패키지안에서는 getter나 setter를 사용하지 않아도 접근이 가능
	String userID;
	int mode;
	String message;
	String fileName;
	int score;
	byte[] fileData;
	
	public ChatMsg(String userID, int mode, String message) {
		this.userID = userID;
		this.mode = mode;
		this.message = message;
	}
	
	public ChatMsg(String userID, int mode) {
		this.userID = userID;
		this.mode = mode;
	}
	
	public ChatMsg(int mode, String mesaage) {
		this.mode = mode;
		this.message = message;
	}
	
	public ChatMsg(String fileName, int code, byte[] fileData ) {
		this.fileName = fileName;
		this.mode = code;
		this.fileData = fileData;
	}
	
	public ChatMsg(String userID, int mode, int score) {
		this.userID = userID;
		this.mode = mode;
		this.score = score;
	}
}
