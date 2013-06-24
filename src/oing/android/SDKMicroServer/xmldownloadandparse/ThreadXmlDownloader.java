package oing.android.SDKMicroServer.xmldownloadandparse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import oing.android.SDKMicroServer.Main;

public class ThreadXmlDownloader extends Thread
{
	private static final String XMLURL_ADDONS_LIST_2 = "http://dl-ssl.google.com/android/repository/addons_list-2.xml";
	private static final String XMLURL_REPOSITORY_8 = "http://dl-ssl.google.com/android/repository/repository-8.xml";
	
	private Proxy proxy = null;
	private boolean requestAbortDownload = false;// 由启动线程的对象通过requestAbortDownload()请求终止下载
	private LinkedHashMap<String, String> lnkHashMapSDKXmlUrls = null;
	
	public ThreadXmlDownloader()
	{
	}
	
	public ThreadXmlDownloader(Proxy proxy)
	{
		this.proxy = proxy;
	}
	
	public void requestAbortDownload()
	{
		this.requestAbortDownload = true;
	}
	
	@Override
	public void run()
	{
		super.run();
		processDownloadQueue();
		if (requestAbortDownload == true)
		{
			System.out.println("Download and parse aborted.");
		}
		else
		{
			System.out.println("Download and parse finished.");
		}
		requestAbortDownload = false;// 重置这个状态
	}
	
	private LinkedHashMap<String, String> processDownloadQueue()
	{
		// 这个时间字符串将被用作解析完成的zip的url的txt的文件名
		// 在Iterator迭代过程中,一旦Iterator指向的原始数据发生变化,Iterator就会迷失自己的方向然后抛出异常java.util.ConcurrentModificationException
		// 因此只能暂时使用两个ArrayList来代替一个LinkedHashMap
		// 在最后的时候把ArrayList里的东西都put到LinkedHashMap里
		ArrayList<String> arrListSDKXmlUrls_original = new ArrayList<String>();
		ArrayList<String> arrListSDKXmlUrls_reanmed = new ArrayList<String>();
		arrListSDKXmlUrls_original.add(XMLURL_ADDONS_LIST_2);
		arrListSDKXmlUrls_reanmed.add(null);
		arrListSDKXmlUrls_original.add(XMLURL_REPOSITORY_8);
		arrListSDKXmlUrls_reanmed.add(null);
		
		lnkHashMapSDKXmlUrls = new LinkedHashMap<String, String>();
		for (int i = 0; i < arrListSDKXmlUrls_original.size() && this.requestAbortDownload == false; i++)
		{
			String _sdkXmlUrl_single = arrListSDKXmlUrls_original.get(i);
			StringBuffer _strBuffXmlContent = null;
			System.out.println("Downloading " + _sdkXmlUrl_single);
			_strBuffXmlContent = downloadXml(_sdkXmlUrl_single);
			if (_strBuffXmlContent == null)
			{
				continue;
			}
			System.out.println("Parsing...");
			StringBuffer strBuffXmlForSDKManager = XmlParser.parseXmlForSDKManager(_strBuffXmlContent);// 这个对象还要给下边保存成xml文件的用上
			{
				// 解析还需要下载的xml文件,并添加到arrListDownloadQueue下载队列里
				ArrayList<String> arrListXmlFiles = XmlParser.parseXML(_strBuffXmlContent, XmlParser.getBaseUrl(_sdkXmlUrl_single));
				ArrayList<String> arrListXmlFiles_renamed = XmlParser.parseXML(strBuffXmlForSDKManager, XmlParser.getBaseUrl(_sdkXmlUrl_single));
				for (int j = 0; j < arrListXmlFiles.size(); j++)
				{
					arrListSDKXmlUrls_original.add(arrListXmlFiles.get(j));
					arrListSDKXmlUrls_reanmed.add(arrListXmlFiles_renamed.get(j));
				}
			}
			// 保存下载好的文件为新的文件名
			{
				FileOutputStream fos = null;
				String fileName = null;
				fileName = arrListSDKXmlUrls_reanmed.get(i);
				if (fileName == null)// 当无法在改名后的列表里找到URL时候就用改名前的
				{
					fileName = arrListSDKXmlUrls_original.get(i);
				}
				fileName = Main.getApplicationStartPath() + File.separator + fileName.substring(fileName.lastIndexOf('/') + 1);
				System.out.println("Saving parsed XML: " + fileName);
				try
				{
					if (strBuffXmlForSDKManager == null)
					{
						strBuffXmlForSDKManager = _strBuffXmlContent;
					}
					fos = new FileOutputStream(fileName);
					fos.write(strBuffXmlForSDKManager.toString().getBytes());
					fos.flush();
					System.out.println("Saved.");
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
					catch (IOException e)
					{
						System.out.println("Failed to close " + fos.toString());
						e.printStackTrace();
					}
				}
			}
		}
		for (int i = 0; i < arrListSDKXmlUrls_original.size(); i++)
		{
			lnkHashMapSDKXmlUrls.put(arrListSDKXmlUrls_original.get(i), arrListSDKXmlUrls_reanmed.get(i));
		}
		return lnkHashMapSDKXmlUrls;
	}
	
	private StringBuffer downloadXml(String url)
	{
		StringBuffer strBuffDownloadedXmlContent = null;
		InputStream inputStreamDownloader = null;
		try
		{
			int length_buffered = -1;
			URLConnection urlConnFileDownloader = null;
			if (this.proxy == null)
			{
				urlConnFileDownloader = new URL(url).openConnection();
			}
			else
			{
				urlConnFileDownloader = new URL(url).openConnection(this.proxy);
			}
			inputStreamDownloader = urlConnFileDownloader.getInputStream();
			strBuffDownloadedXmlContent = new StringBuffer();
			byte[] buff_byte = new byte[1024 * 1024];// 1MB
			while ((length_buffered = inputStreamDownloader.read(buff_byte)) != -1)
			{
				strBuffDownloadedXmlContent.append(new String(buff_byte, 0, length_buffered));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			strBuffDownloadedXmlContent = null;
		}
		finally
		{
			try
			{
				if (inputStreamDownloader != null)
				{
					inputStreamDownloader.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return strBuffDownloadedXmlContent;
	}
}
