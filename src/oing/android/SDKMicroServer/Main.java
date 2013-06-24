package oing.android.SDKMicroServer;

import java.io.File;
import java.util.Scanner;
import oing.android.SDKMicroServer.microserver.MicroServer;
import oing.android.SDKMicroServer.xmldownloadandparse.XMLDownloadAndParse;

public class Main
{
	private static final String CURRENT_VERSION = "v1.0";
	private static Scanner scannerConsoleInput = null;
	private static String applicationStartPath = null;// 程序的启动目录
	
	public static void main(String[] args)
	{
		scannerConsoleInput = new Scanner(System.in);
		MicroServer microServer = new MicroServer();
		XMLDownloadAndParse xmlDownloadAndParse = new XMLDownloadAndParse();
		// 读取程序启动路径
		applicationStartPath = microServer.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		applicationStartPath = new File(applicationStartPath).getParent();
		System.out.println("AndroidSDKMicroServer " + CURRENT_VERSION);
		System.out.println("ApplicationStartPath=" + getApplicationStartPath());
		System.out.println("Develop by oing9179, This project is available on GitHub.");
		System.out.println("https://github.com/oing9179/AndroidSDKMicroServer/");
		System.out.println("Tips: You can type '-1' anywhere to show current menu.");
		// 开始主菜单循环
		int userSelection = 0;
		do
		{
			System.out.println("----------Main menu----------");
			System.out.println("0. Stop MicroServer and exit");
			System.out.println("1. MicroServer settings");
			System.out.println("2. XML download and parse");
			System.out.print("Type option: ");
			try
			{
				userSelection = scannerConsoleInput.nextInt();
			}
			catch (Exception e)
			{
				System.out.println("You have type a wrong number, try again.");
				scannerConsoleInput.nextLine();// 据说可以清空输入流的缓存,防止死循环
				userSelection = -1;
				continue;
			}
			switch (userSelection)
			{
				case 1 :
					microServer.menuLoop(scannerConsoleInput);
					break;
				case 2 :
					xmlDownloadAndParse.menuLoop(scannerConsoleInput);
					break;
			}
		}
		while (userSelection != 0);
		microServer.stopMicroServer();
		System.out.println("\nSee you next time!");
	}
	
	/**
	 * 取程序的启动目录
	 * 
	 * @return
	 */
	public static String getApplicationStartPath()
	{
		return applicationStartPath;
	}
}
