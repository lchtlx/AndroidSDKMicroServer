package oing.android.SDKMicroServer.xmldownloadandparse;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import oing.android.SDKMicroServer.Main;

public class XMLDownloadAndParse
{
	private XmlDownloadManager xmlDownloadManager = null;
	private boolean platformLinux = false;
	private boolean platformWindows = false;
	private boolean platformMac = false;
	
	public XMLDownloadAndParse()
	{
		xmlDownloadManager = new XmlDownloadManager();
	}
	
	public void menuLoop(Scanner scannerConsoleInput)
	{
		int userSelection = 0;
		do
		{
			System.out.println("---------- 2. XML download and parse ----------");
			System.out.println("0. Back to main menu");
			System.out.println("1. Download, and parse for SDKManager");
			System.out.println("2. Request abort download");
			System.out.println("3. Proxy settings");
			System.out.println("4. Parse only for zip urls");
			System.out.print("Type option: ");
			try
			{
				userSelection = scannerConsoleInput.nextInt();
			}
			catch (Exception e)
			{
				System.out.println("You have type a wrong number, try again.");
				userSelection = -1;
			}
			scannerConsoleInput.nextLine();// 刷新流缓存
			switch (userSelection)
			{
				case 1 :
					xmlDownloadManager.startDonwload();
					break;
				case 2 :
					xmlDownloadManager.abortDownload();
					break;
				case 3 :
				{
					System.out.println("The format of proxy address like this, http://127.0.0.1:80 or http://dl-ssl.google.com:80");
					System.out.println("Nothing for cancel proxy.");
					System.out.print("Input the address: ");
					String[] proxyAddress = scannerConsoleInput.nextLine().split(":");
					if (proxyAddress.length == 3)
					{
						try
						{
							InetSocketAddress inetSocketAddressProxy = new InetSocketAddress(proxyAddress[0] + proxyAddress[1], Integer.parseInt(proxyAddress[2]));
							Proxy proxy = new Proxy(Proxy.Type.SOCKS, inetSocketAddressProxy);
							xmlDownloadManager.setProxy(proxy);
						}
						catch (Exception e)
						{
							e.printStackTrace();
							System.out.println("You have input a wrong proxy address, try agian.");
						}
					}
					else
					{
						xmlDownloadManager.setProxy(null);
						System.out.println("Proxy canceled.");
					}
				}
					break;
				case 4 :
				{
					String baseURL = null;
					if (setUserSelectPlatforms(scannerConsoleInput) == false)
					{
						System.out.println("Operation canceled.");
					}
					else
					{
						System.out.println("Input baseURL: ");
						baseURL = scannerConsoleInput.nextLine();
						if (baseURL != null && baseURL.length() != 0)
						{
							parseOnlyForZipUrls(baseURL);
						}
						else
						{
							System.out.println("Parse canceled.");
						}
					}
				}
					break;
			}
		}
		while (userSelection != 0);
	}
	
	private void parseOnlyForZipUrls(String baseURL)
	{
		StringBuffer strBuffParseResult = new StringBuffer();
		System.out.println("Parse started.");
		File[] xmlFiles = new File(Main.getApplicationStartPath()).listFiles(new FileFilter_keepXml());
		System.out.println("Existed XML files=" + xmlFiles.length);
		for (File feitem_singleXmlFile : xmlFiles)
		{
			System.out.println("Parsing file=" + feitem_singleXmlFile.getName());
			StringBuffer strBuffFileContent = readFileAsText(feitem_singleXmlFile.getPath());
			if (strBuffFileContent != null)
			{
				ArrayList<String> arrListZipUrls = XmlParser.parseXML(strBuffFileContent, baseURL, platformLinux, platformWindows, platformMac);
				for (String feitem_singleZipUrl : arrListZipUrls)
				{
					strBuffParseResult.append(feitem_singleZipUrl + "\n");
				}
			}
		}
		System.out.println("Parse finished.");
		{
			FileOutputStream fos = null;
			try
			{
				String fileName_parseResult = Main.getApplicationStartPath() + File.separator + "parseResult" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_sss").format(new Date()) + ".txt";
				fos = new FileOutputStream(fileName_parseResult);
				fos.write(strBuffParseResult.toString().getBytes());
				fos.flush();
				System.out.println("File saved to " + fileName_parseResult);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (fos != null)
					{
						fos.close();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 设置用户要保留的zip的url的所属操作系统
	 * 
	 * @return 用户如果放弃
	 */
	private boolean setUserSelectPlatforms(Scanner scannerConsoleInput)
	{
		System.out.println("Select the platform you want to save, 0=discard or other");
		System.out.println("For example, 010 means save Windows Platform only.");
		System.out.println("[Linux][Windows][MacOSX]");
		System.out.println("Now: " + (platformLinux ? 1 : 0) + (platformWindows ? 1 : 0) + (platformMac ? 1 : 0));
		System.out.print("Input: ");
		String userSelectedPlatform = scannerConsoleInput.nextLine();
		if (userSelectedPlatform.length() == 3)
		{
			platformLinux = userSelectedPlatform.charAt(0) == '0' ? false : true;
			platformWindows = userSelectedPlatform.charAt(1) == '0' ? false : true;
			platformMac = userSelectedPlatform.charAt(2) == '0' ? false : true;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private StringBuffer readFileAsText(String fileName)
	{
		StringBuffer strBuffFileContent = new StringBuffer();
		FileInputStream fis = null;
		try
		{
			byte[] byteBuff = new byte[1024 * 1024];
			int bufferedLength = -1;
			fis = new FileInputStream(fileName);
			while ((bufferedLength = fis.read(byteBuff)) != -1)
			{
				strBuffFileContent.append(new String(byteBuff, 0, bufferedLength));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return strBuffFileContent;
	}
	
	class FileFilter_keepXml implements FileFilter
	{
		@Override
		public boolean accept(File pathname)
		{
			boolean isXmlFile = false;
			String fileName = pathname.getName();
			if ((fileName.lastIndexOf(".xml") != -1) && fileName.lastIndexOf(".xml") == fileName.length() - 4)// 如果找到了.xml并且.xml在末尾
			{
				isXmlFile = true;
			}
			return isXmlFile;
		}
	}
}
