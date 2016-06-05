import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class BingoServer {
	static BufferedWriter myLogOut = null;
	static ServerSocket serverSocket;
	static int fileId = 0;
	static ArrayList<String> fileId2fileCode;
	static ArrayList<String> fileName = null;

	static String savePath = "/home/bingo/bingosave";

	public static void main(String args[]){
		BingoServer bingoServer = new BingoServer();
		bingoServer.init();
	}

	private void init(){
		try{
			myLogOut = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/bingo/BingoServerLog.txt",false)));

			fileName = new ArrayList<>();
			fileId2fileCode = new ArrayList<>();

			Log("init success");

			serverSocket = new ServerSocket(44180);

			while (true){
				Log("正在监听端口44180,准备接入下一个设备");
				Socket socket=serverSocket.accept();
				Log("新设备呼入");

			    int port = getPort();
			    Log("通信端口为"+port);

			    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			    dos.writeInt(port);
			    dos.flush();

			    socket.close();
			    dos.close();


			    TransmissionTheard theard= new TransmissionTheard(port);
			    theard.start();
			    Log("启动线程:"+theard.getId());

			}
		}catch (Exception e){
			e.printStackTrace();
			try {
				Log(e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private class TransmissionTheard extends Thread{
		private int localPort;

		public TransmissionTheard(int lport) {
			localPort = lport;
		}

		@Override
		public void run(){
			try {
				ServerSocket serverSocket = new ServerSocket(localPort);
				Socket socket = serverSocket.accept();

				String fileCode = generateFileCode();
			    Log("TheardId: "+getId()+": 文件标识符为"+fileCode);

			    int bufferSize = 1024;
                byte[] buf = new byte[bufferSize];

			    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			    dos.writeUTF(fileCode);

			    String fileName = dis.readUTF();
			    String Path =savePath + '/'+fileName;
			    Log("接收的文件名为:"+fileName);
			    DataOutputStream fileos = new DataOutputStream(
                        new BufferedOutputStream(new BufferedOutputStream(
                                new FileOutputStream(Path))));

			    long fileLength = dis.readLong();
			    Log("接收的文件长度为:"+fileLength);
			    while (true){
			    	int readLength = -1;
			    	if (dis!=null){
			    		readLength = dis.read(buf);
			    	}
			    	Log("读取的长度为"+readLength);
			    	if (readLength==-1){
			    		break;
			    	}

			    	fileos.write(buf,0,readLength);
			    }
			    Log("文件接受完毕");

			    serverSocket.close();
			    dos.close();
			    fileos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					Log(e.getMessage());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	//获取一个没有被占用的接口
	public static int getPort(){
		for (int i = 44181;;i++){
			if (checkPort(i))
				return i;
		}
	}

	public static boolean checkPort(int port){
		try
        {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
	}

	//生成随机的不重复的6位标识码
	public static String generateFileCode(){
		Random random = new Random();
		String code;
		while (true){
			code = String.valueOf(100000 + random.nextInt(899999));
			if (!fileId2fileCode.contains(code)) break;
		}
		return code;
	}

	//log
	public static void Log(String content) throws IOException{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

		myLogOut.write(df.format(new Date())+" "+content+'\n');
		myLogOut.flush();

		System.out.println(df.format(new Date())+" "+content+'\n');

	}
}
