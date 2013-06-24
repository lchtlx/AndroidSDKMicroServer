package oing.android.SDKMicroServer.microserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class RunnableMicroServer implements Runnable
{
	private ServerSocket serverSocketMicroServer = null;
	
	public RunnableMicroServer(ServerSocket serverSocket)
	{
		this.serverSocketMicroServer = serverSocket;
	}
	
	@Override
	public void run()
	{
		while (serverSocketMicroServer.isClosed() == false)
		{
			try
			{
				Socket socketSDKRequest = serverSocketMicroServer.accept();
				Thread threadSDKRequestProcessor = new Thread(new RunnableSDKRequestProcessor(socketSDKRequest));
				threadSDKRequestProcessor.start();
			}
			catch (SocketException e)
			{
				e.printStackTrace();
				System.out.flush();
				System.out.println("User try to close MicroServer maybe.");
			}
			catch (IOException e)
			{
				System.err.println("Failed to accept a Socket.");
				e.printStackTrace();
			}
		}
	}
}
