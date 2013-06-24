package oing.android.SDKMicroServer.xmldownloadandparse;

import java.net.Proxy;

public class XmlDownloadManager
{
	private Proxy proxy = null;
	private ThreadXmlDownloader threadXmlDownloadAndParse = null;
	
	public void startDonwload()
	{
		if (threadXmlDownloadAndParse != null && threadXmlDownloadAndParse.getState() != Thread.State.TERMINATED)
		{
			System.out.println("Thread is running now, you can abort it.");
			return;
		}
		if (proxy == null)
		{
			threadXmlDownloadAndParse = new ThreadXmlDownloader();
		}
		else
		{
			threadXmlDownloadAndParse = new ThreadXmlDownloader(proxy);
		}
		threadXmlDownloadAndParse.start();
	}
	
	public void abortDownload()
	{
		if (threadXmlDownloadAndParse != null && threadXmlDownloadAndParse.getState() != Thread.State.TERMINATED)
		{
			threadXmlDownloadAndParse.requestAbortDownload();
			System.out.println("Abort requested.");
		}
		else
		{
			System.out.println("Thread is not running.");
		}
	}
	
	public Proxy getProxy()
	{
		return this.proxy;
	}
	
	public void setProxy(Proxy proxy)
	{
		this.proxy = proxy;
	}
}
