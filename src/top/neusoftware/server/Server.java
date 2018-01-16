package top.neusoftware.server;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Server {
	public static void main(String[] args) throws IOException {
		ServerSocket ss=new ServerSocket(21);
		Socket s=ss.accept();
		ServerSocket dataSocket=null;
		String info;
		BufferedReader reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
		PrintWriter writer=new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
		Boolean con=true;
		Boolean user=false;
		Boolean pass=false;
		info=reader.readLine();
		writer.println("220 FTP server ready");
		writer.flush();
		while(con) {
			info=reader.readLine();
			if(info.startsWith("USER")) {
				if(user) {
					writer.println("230 Already logged-in");
					writer.flush();
				}
				else if(info.length()<=5){
					writer.println("501 syntax error");
					writer.flush();
				}
				else if(info.substring(5).equals("root")) {
					user=true;
					writer.println("331 User name okay,need password");
					writer.flush();
				}
				else if(!info.substring(5).equals("root")) {
					writer.println("530 Invalid user name");
					writer.flush();
				}
				else {
					writer.println("501 syntax error");
					writer.flush();
				}
			}
			else if(info.startsWith("PASS")) {
				if(!user) {
					writer.println("503 Login with USER first");
					writer.flush();
				}
				else if(pass) {
					writer.println("202 Already logged-in");
					writer.flush();
				}
				else if(info.length()<=5){
					writer.println("501 syntax error");
					writer.flush();
				}
				else if(info.substring(5).equals("root")) {
					pass=true;
					writer.println("230 User logged-in, proceed");
					writer.flush();
				}
				else if(!info.substring(5).equals("root")){
					writer.println("530 Authentication failed");
					writer.flush();
				}
				else {
					writer.println("501 syntax error");
					writer.flush();
				}
			}
			else if(info.equals("PASV")) {
				if(user&&pass) {
					Random generator=new Random();
					int portHigh,portLow;
					while(true) {
						portHigh=1+generator.nextInt(20);
						portLow=100+generator.nextInt(1000);
						try {
							dataSocket=new ServerSocket(portHigh*256+portLow);
							break;
						}
						catch(IOException e) {
							continue;
						}
					}
					InetAddress i=null;
					try {
						i=InetAddress.getLocalHost();
					}
					catch(UnknownHostException e1) {
						e1.printStackTrace();
					}
					writer.println("227Entering passive mode("+i.getHostAddress().replace(".", ",")+","+portHigh+","+portLow+")");
					writer.flush();
				}
				else {
					writer.println("530 Please login with USER and PASS");
					writer.flush();
				}
			}
			else if(info.equals("QUIT")) {
				con=false;
			}
			else {
				writer.println("500 Invalid command: try being more creative");
				writer.flush();
			}
		}
	}
}