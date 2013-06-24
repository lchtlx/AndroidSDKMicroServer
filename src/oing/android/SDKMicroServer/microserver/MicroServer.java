package oing.android.SDKMicroServer.microserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class MicroServer
{
	private ServerSocket serverSocketMicroServer = null;
	private int microServerPort = 0;
	
	public void menuLoop(Scanner scannerConsoleInput)
	{
		int userSelection = 0;
		do
		{
			System.out.println("---------- 1. MicroServer settings ----------");
			if (serverSocketMicroServer != null && serverSocketMicroServer.isClosed() == false)
			{
				System.out.println("MicroServer is running on port " + serverSocketMicroServer.getLocalPort());
			}
			System.out.println("0. Back to main menu");
			System.out.println("1. Start MicroServer");
			System.out.println("2. Stop MicroServer");
			System.out.println("3. Edit MicroServer port");
			System.out.print("Type option: ");
			try
			{
				userSelection = scannerConsoleInput.nextInt();
			}
			catch (Exception e)
			{
				System.out.println("You have type a wrong number, try again.");
				scannerConsoleInput.nextLine();
				userSelection = -1;
				continue;
			}
			switch (userSelection)
			{
				case 1 :
					this.startMicroServer();
					break;
				case 2 :
					this.stopMicroServer();
					break;
				case 3 :
				{
					int proxyHostPort = 0;
					System.out.println("0=auto, -1=dont change, or other number will set.");
					System.out.print("Input port number: ");
					try
					{
						proxyHostPort = scannerConsoleInput.nextInt();
						if (proxyHostPort == -1)
						{
							break;
						}
						else
						{
							this.setMicroServer(proxyHostPort);
						}
					}
					catch (Exception e)
					{
						System.out.println("You have input a wrong number, try again.");
						scannerConsoleInput.nextLine();
					}
				}
					
					break;
			}
		}
		while (userSelection != 0);
	}
	
	private void startMicroServer()
	{
		if (serverSocketMicroServer == null || (serverSocketMicroServer != null && serverSocketMicroServer.isClosed() == true))
		{
			System.out.println("Starting MicroServer...");
			try
			{
				serverSocketMicroServer = new ServerSocket(microServerPort);
				Thread threadRunnableMicroServer = new Thread(new RunnableMicroServer(serverSocketMicroServer));
				threadRunnableMicroServer.start();
			}
			catch (IOException e)
			{
				System.out.println("Failed to start MicroServer.");
				e.printStackTrace();
				serverSocketMicroServer = null;
			}
			System.out.println("MicroServer successful start.");
		}
		else
		{
			System.out.println("MicroServer has been running!");
		}
	}
	
	public void stopMicroServer()
	{
		if (serverSocketMicroServer == null)
		{
			System.out.println("MicroServer is not running.");
		}
		else if (serverSocketMicroServer != null)
		{
			if (serverSocketMicroServer.isClosed() == false)
			{
				System.out.println("Closing MicroServer...");
				try
				{
					serverSocketMicroServer.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				System.out.println("MicroServer closed.");
			}
			else
			{
				System.out.println("MicroServer has been closed.");
			}
		}
	}
	
	private void setMicroServer(int port)
	{
		if (serverSocketMicroServer != null && serverSocketMicroServer.isClosed() == false)
		{
			System.out.println("MicroServer is running, Stop and try again.");
		}
		else
		{
			microServerPort = port;
			System.out.println("MicroServer port changed.");
		}
	}
}
