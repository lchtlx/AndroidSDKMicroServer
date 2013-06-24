package oing.android.SDKMicroServer.microserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import oing.android.SDKMicroServer.Main;

public class RunnableSDKRequestProcessor implements Runnable
{
	private Socket socketSDKRequest = null;
	
	public RunnableSDKRequestProcessor(Socket socketSDKRequest)
	{
		this.socketSDKRequest = socketSDKRequest;
	}
	
	@Override
	public void run()
	{
		BufferedReader bufferedReaderSocket = null;
		OutputStream outputStreamSocket = null;
		try
		{
			String requestedFileName = null;
			File fileRequested = null;
			bufferedReaderSocket = new BufferedReader(new InputStreamReader(socketSDKRequest.getInputStream()));
			outputStreamSocket = socketSDKRequest.getOutputStream();
			requestedFileName = bufferedReaderSocket.readLine().split(" ")[1];
			requestedFileName = new File(requestedFileName).getName();
			// 开始处理请求
			fileRequested = new File(Main.getApplicationStartPath() + File.separator + requestedFileName);
			if (fileRequested.exists() == true)
			{
				byte[] _byteBuff = new byte[1024 * 1024];
				int _buffedLength = -1;
				long elapsedTimeForTransferFile = System.currentTimeMillis();
				FileInputStream fis = new FileInputStream(fileRequested);
				// 输出状态到控制台
				System.out.println("RequestFile=" + fileRequested.getName() + ", Status=200, Transferring...");
				// 写出相应头
				outputStreamSocket.write("HTTP/1.1 200 OK\n".getBytes());
				outputStreamSocket.write("Accept-Ranges: bytes\n".getBytes());
				outputStreamSocket.write("Content-Type: application/zip\n".getBytes());
				outputStreamSocket.write(("Content-Length: " + fileRequested.length() + "\n\n").getBytes());
				// 传输文件字节流
				while ((_buffedLength = fis.read(_byteBuff)) != -1)
				{
					outputStreamSocket.write(_byteBuff, 0, _buffedLength);
				}
				fis.close();
				System.out.println("RequestFile=" + fileRequested.getName() + ", Status=200, Transferred in " + (System.currentTimeMillis() - elapsedTimeForTransferFile) + "ms.");
			}
			else
			{
				System.out.println("RequestFile=" + fileRequested.getName() + ", Status=404");
				outputStreamSocket.write("HTTP/1.1 404 NotFound".getBytes());
			}
			outputStreamSocket.flush();
			socketSDKRequest.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
