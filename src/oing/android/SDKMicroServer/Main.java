package oing.android.SDKMicroServer;

import java.io.File;
import java.util.Scanner;
import oing.android.SDKMicroServer.microserver.MicroServer;
import oing.android.SDKMicroServer.xmldownloadandparse.XMLDownloadAndParse;

public class Main
{
	private static final String CURRENT_VERSION = "v1.0";
	private static Scanner scannerConsoleInput = null;
	private static String applicationStartPath = null;// ���������Ŀ¼
	
	public static void main(String[] args)
	{
		scannerConsoleInput = new Scanner(System.in);
		MicroServer microServer = new MicroServer();
		XMLDownloadAndParse xmlDownloadAndParse = new XMLDownloadAndParse();
		// ��ȡ��������·��
		applicationStartPath = microServer.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		applicationStartPath = new File(applicationStartPath).getParent();
		System.out.println("AndroidSDKMicroServer " + CURRENT_VERSION);
		System.out.println("ApplicationStartPath=" + getApplicationStartPath());
		System.out.println("Develop by oing9179, This project is available on GitHub.");
		System.out.println("https://github.com/oing9179/AndroidSDKMicroServer/");
		System.out.println("Tips: You can type '-1' anywhere to show current menu.");
		// ��ʼ���˵�ѭ��
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
				scannerConsoleInput.nextLine();// ��˵��������������Ļ���,��ֹ��ѭ��
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
	 * ȡ���������Ŀ¼
	 * 
	 * @return
	 */
	public static String getApplicationStartPath()
	{
		return applicationStartPath;
	}
}
