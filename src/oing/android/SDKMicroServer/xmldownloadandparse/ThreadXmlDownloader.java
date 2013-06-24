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
	private boolean requestAbortDownload = false;// �������̵߳Ķ���ͨ��requestAbortDownload()������ֹ����
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
		requestAbortDownload = false;// �������״̬
	}
	
	private LinkedHashMap<String, String> processDownloadQueue()
	{
		// ���ʱ���ַ�����������������ɵ�zip��url��txt���ļ���
		// ��Iterator����������,һ��Iteratorָ���ԭʼ���ݷ����仯,Iterator�ͻ���ʧ�Լ��ķ���Ȼ���׳��쳣java.util.ConcurrentModificationException
		// ���ֻ����ʱʹ������ArrayList������һ��LinkedHashMap
		// ������ʱ���ArrayList��Ķ�����put��LinkedHashMap��
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
			StringBuffer strBuffXmlForSDKManager = XmlParser.parseXmlForSDKManager(_strBuffXmlContent);// �������Ҫ���±߱����xml�ļ�������
			{
				// ��������Ҫ���ص�xml�ļ�,����ӵ�arrListDownloadQueue���ض�����
				ArrayList<String> arrListXmlFiles = XmlParser.parseXML(_strBuffXmlContent, XmlParser.getBaseUrl(_sdkXmlUrl_single));
				ArrayList<String> arrListXmlFiles_renamed = XmlParser.parseXML(strBuffXmlForSDKManager, XmlParser.getBaseUrl(_sdkXmlUrl_single));
				for (int j = 0; j < arrListXmlFiles.size(); j++)
				{
					arrListSDKXmlUrls_original.add(arrListXmlFiles.get(j));
					arrListSDKXmlUrls_reanmed.add(arrListXmlFiles_renamed.get(j));
				}
			}
			// �������غõ��ļ�Ϊ�µ��ļ���
			{
				FileOutputStream fos = null;
				String fileName = null;
				fileName = arrListSDKXmlUrls_reanmed.get(i);
				if (fileName == null)// ���޷��ڸ�������б����ҵ�URLʱ����ø���ǰ��
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
