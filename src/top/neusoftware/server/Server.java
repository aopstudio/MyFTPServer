package top.neusoftware.server;

import java.io.*;
import java.math.RoundingMode;
import java.net.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Random;

public class Server {
	ServerSocket ss;
	Socket s;
	ServerSocket dataSocket=null;
	BufferedReader reader;
	PrintWriter writer;
	String filePath="D:/";//默认路径是D盘
	private Boolean user=false;
	private Boolean pass=false;
	private Boolean pasv=false;
	private static DecimalFormat df = null;
	static {  
        // 设置数字格式，保留一位有效小数  
        df = new DecimalFormat("#0.0");  
        df.setRoundingMode(RoundingMode.HALF_UP);  
        df.setMinimumFractionDigits(1);  
        df.setMaximumFractionDigits(1);  
    }  
	public Server() throws IOException {
		ss=new ServerSocket(3000);
		s=ss.accept();
		reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
		writer=new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
	}
	public void firstAccess() throws IOException {

		writer.println("220 FTP server ready");
		writer.flush();
	}
	public void userVerify(String info) {
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
	public void passVerify(String info) {
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
	public void pasvVerify() {
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
			pasv=true;
		}
		else {
			writer.println("530 Please login with USER and PASS");
			writer.flush();
		}
	}
	
	public void errorCommand() {
		writer.println("500 Invalid command: try being more creative");
		writer.flush();
	}
	
	public void receiveFile() throws IOException, InterruptedException {//收文件
		if(user&&pass&pasv) {
			writer.println("150 Opening data connection");
			writer.flush();
			byte[] inputByte = null;  
	        DataInputStream dis = null;  
	        FileOutputStream fos = null;  
	        Socket socket=dataSocket.accept();	 
	        try {  
	        	dis = new DataInputStream(socket.getInputStream());  
	        	  
                // 文件名和长度  
                String fileName = dis.readUTF();  
                long fileLength = dis.readLong();  
                File directory = new File(filePath);  
                if(!directory.exists()) {  
                    directory.mkdir();  
                }  
                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);  
                fos = new FileOutputStream(file);  
  
                // 开始接收文件  
                byte[] bytes = new byte[1024];  
                int length = 0;  
                while((length = dis.read(bytes, 0, bytes.length)) != -1) {  
                    fos.write(bytes, 0, length);  
                    fos.flush();  
                }  
                System.out.println("======== 文件接收成功 [File Name：" + fileName + "] [Size：" + getFormatFileSize(fileLength) + "] ========");  
            } catch (Exception e) {  
                e.printStackTrace();  
            } finally {  
                try {  
                    if(fos != null)  
                        fos.close();  
                    if(dis != null)  
                        dis.close();  
                    socket.close();  
                } catch (Exception e) {}  
            }  
        
        }  			
		else {
			writer.println("501 syntax error");
			writer.flush();
		}
    }  	
	private String getFormatFileSize(long length) {  
	    double size = ((double) length) / (1 << 30);  
	    if(size >= 1) {  
	        return df.format(size) + "GB";  
	    }  
	    size = ((double) length) / (1 << 20);  
	    if(size >= 1) {  
	        return df.format(size) + "MB";  
	    }  
	    size = ((double) length) / (1 << 10);  
	    if(size >= 1) {  
	        return df.format(size) + "KB";  
	    }  
	    return length + "B";  
	}  
	
	public void sendFile(String fileName) throws IOException {
		DataOutputStream dos=null;	
		FileInputStream fis=null;
		Socket socket=dataSocket.accept();
		if(user&&pass&pasv) {
			writer.println("150 Opening data connection");
			writer.flush();
			try {  
	            File file = new File(fileName);  
	            if(file.exists()) {  
	                fis = new FileInputStream(file);  
	                dos = new DataOutputStream(socket.getOutputStream());  
	  
	                // 文件名和长度  
	                dos.writeUTF(file.getName());  
	                dos.flush();  
	                dos.writeLong(file.length());  
	                dos.flush();  
	  
	                // 开始传输文件  
	                System.out.println("======== 开始传输文件 ========");  
	                byte[] bytes = new byte[1024];  
	                int length = 0;  
	                long progress = 0;  
	                while((length = fis.read(bytes, 0, bytes.length)) != -1) {  
	                    dos.write(bytes, 0, length);  
	                    dos.flush();  
	                    progress += length;  
	                    System.out.print("| " + (100*progress/file.length()) + "% |");  
	                }  
	                System.out.println();  
	                System.out.println("======== 文件传输成功 ========");  
	            }  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        } finally {  
	            if(fis != null)  
	                fis.close();  
	            if(dos != null)  
	                dos.close();  	          
	        }  
		}
		else {
			writer.println("501 syntax error");
			writer.flush();
		}
	}
	
	public void listDirectory() {
		File file=new File(filePath);
		  File[] tempList = file.listFiles();
		  System.out.println("该目录下对象个数："+tempList.length);
		  for (int i = 0; i < tempList.length; i++) {
			   if (tempList[i].isFile()) {
				   java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				   String dateTime=df.format(new Date(tempList[i].lastModified()));
				   writer.println("文   件："+tempList[i]+"     "+dateTime+"     "+tempList[i].length()+"kb");
				   writer.flush();
			   }
			   if (tempList[i].isDirectory()) {
				   java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				   String dateTime=df.format(new Date(tempList[i].lastModified()));
				   writer.println("文件夹："+tempList[i]+"    "+dateTime+"    "+tempList[i].length()+"kb");
				   writer.flush();
			   }
		  }
		  writer.println("EOF");
	}
}