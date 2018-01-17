package top.neusoftware.server;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		Server ser=new Server();
		Boolean con=true;	
		String info;
		ser.firstAccess();
		while(con) {
			info=ser.reader.readLine();
			if(info.startsWith("USER")) {
				ser.userVerify(info);
			}
			else if(info.startsWith("PASS")) {
				ser.passVerify(info);
			}
			else if(info.equals("PASV")) {
				ser.pasvVerify();
			}
			else if(info.startsWith("STOR")) {
				ser.receiveFile();
			}
			else if(info.startsWith("RETR")) {
				ser.sendFile(info.substring(5));//这样会有异常隐患，但修改时间已经不够了
			}
			else if(info.startsWith("LIST")) {
				ser.listDirectory();
			}
			else if(info.startsWith("CWD")) {
				ser.changeWorkDirectory(info.substring(4));
			}
			else if(info.equals("QUIT")) {
				con=false;
			}
			else {
				ser.errorCommand();
			}
		}
	}
}