package oing.android.SDKMicroServer.xmldownloadandparse;

import java.io.File;
import java.util.ArrayList;

public class XmlParser
{
	private static int fileNameSequence = 0;// 防止文件名重复的数字序列
	
	/**
	 * 修改xml的<sdk:url>节点,去掉目录只留下文件名,并改掉同名的文件名。这方法的名字太难起了。。。
	 * 
	 * @param xmlContent
	 *            要修改的xml文本
	 * @return 修改好的xml文本
	 */
	public static StringBuffer parseXmlForSDKManager(StringBuffer xmlContent)
	{
		if (xmlContent == null)
		{
			return null;
		}
		StringBuffer strBuffResult = new StringBuffer();
		String[] strArrSDKArchives = xmlContent.toString().split("<sdk:url>");
		if (strArrSDKArchives.length >= 2 && strArrSDKArchives[1].indexOf(".zip") != -1)// 如果含有.zip则说明这xml是给sdk用来解析的
		{
			return null;
		}
		{
			ArrayList<String> arrListSDKXmlFiles = new ArrayList<String>();
			for (int i = 1; i < strArrSDKArchives.length; i++)// 取出所有<sdk:url>节点内容,并添加到arrListSDKXmlFiles
			{
				String _sdkUrl_single = strArrSDKArchives[i].split("</sdk:url>")[0];
				_sdkUrl_single = new File(_sdkUrl_single).getName();
				arrListSDKXmlFiles.add(_sdkUrl_single);
				strArrSDKArchives[i] = strArrSDKArchives[i].replace(_sdkUrl_single, "");// 从strArrSDKArchives把解析好的地址换成空的
			}
			// 接下来开始处理xml的重名问题
			for (int i = 0; i < arrListSDKXmlFiles.size(); i++)
			{
				String _fileName = arrListSDKXmlFiles.get(i);
				String _fileName_main = _fileName.substring(0, _fileName.lastIndexOf('.')) + (++fileNameSequence);
				String _fileName_extensionWithDot = _fileName.substring(_fileName.lastIndexOf('.'));
				arrListSDKXmlFiles.set(i, _fileName_main + _fileName_extensionWithDot);
			}
			// 把处理好的结果重重新写回<sdk:url>节点
			strBuffResult.append(strArrSDKArchives[0]);
			for (int i = 1; i < strArrSDKArchives.length; i++)
			{
				String _sdkUrl_splited = strArrSDKArchives[i].split("</sdk:url>")[1];// 其实这是不包含</sdk:url>及其前边部分的文本,而且split后元素0是个空字符串
				strBuffResult.append("<sdk:url>" + arrListSDKXmlFiles.get(i - 1) + "</sdk:url>" + _sdkUrl_splited);
				if (i < strArrSDKArchives.length - 1)// 判断是否有下一个要处理的文本
				{
					// 如果有,则替换_sdkUrl_splited到"",保证下次拼接的时候不会多出一段_sdkUrl_splited
					strArrSDKArchives[i + 1] = strArrSDKArchives[i + 1].replace(_sdkUrl_splited, "");
				}
			}
		}
		return strBuffResult;
	}
	
	/**
	 * 解析SDK的XML文件，只取出<sdk:url>含有xml文件的地址
	 * 
	 * @param strBuffXmlContent
	 *            要解析的文本
	 * @param baseURL
	 *            基地址
	 * @return 解析结果
	 */
	public static ArrayList<String> parseXML(StringBuffer strBuffXmlContent, String baseURL)
	{
		if (strBuffXmlContent == null)
		{
			return null;
		}
		ArrayList<String> arrListSDKXmlUrls = new ArrayList<String>();
		String[] strArrSDKUrls = strBuffXmlContent.toString().split("<sdk:url>");
		changeHttpsToHttp(baseURL);
		for (int i = 1; i < strArrSDKUrls.length; i++)
		{
			// 取出<sdk:url>的内容
			String _sdkUrl = strArrSDKUrls[i].substring(0, strArrSDKUrls[i].indexOf("</sdk:url>"));
			if (_sdkUrl.indexOf(".xml") != -1)
			{
				arrListSDKXmlUrls.add(changeHttpsToHttp(tryAddBaseURL(_sdkUrl, baseURL)));
			}
		}
		return arrListSDKXmlUrls;
	}
	
	/**
	 * 解析SDK的XML文件，只取出<sdk:url>含有zip文件的地址
	 * 
	 * @param strBuffXmlContent
	 *            要解析的文本
	 * @param baseURL
	 *            基地址
	 * @param platformLinux
	 *            包含给linux使用的包
	 * @param platformWindows
	 *            包含给Windows使用的包
	 * @param platformMac
	 *            包含给MacOSX使用的包
	 * @return 解析结果
	 */
	public static ArrayList<String> parseXML(StringBuffer strBuffXmlContent, String baseURL, boolean platformLinux, boolean platformWindows, boolean platformMac)
	{
		if (strBuffXmlContent == null)
		{
			return null;
		}
		ArrayList<String> arrListSDKArchiveUrls = new ArrayList<String>();
		String[] strArrSDKArchives = null;
		changeHttpsToHttp(baseURL);
		{
			// 分割出所有<sdk:archive>节点
			String[] _strArrSDKArchives_preview = strBuffXmlContent.toString().split("<sdk:archive");
			strArrSDKArchives = new String[_strArrSDKArchives_preview.length - 1];
			for (int i = 1; i < _strArrSDKArchives_preview.length; i++)
			{
				strArrSDKArchives[i - 1] = _strArrSDKArchives_preview[i].split("</sdk:archive>")[0];
			}
		}
		for (int i = 0; i < strArrSDKArchives.length; i++)
		{
			if (strArrSDKArchives[i].indexOf(".zip") == -1)// 如果不是zip包则跳过
			{
				continue;
			}
			String _sdkArchiveTextContent = strArrSDKArchives[i];
			boolean saveThisURL = false;
			// 解析该SDKArchive可用于哪个操作系统
			if (_sdkArchiveTextContent.contains("os=\"any\""))
			{
				saveThisURL = true;
			}
			else if (_sdkArchiveTextContent.contains("os=\"linux\"") && platformLinux == true)
			{
				saveThisURL = true;
			}
			else if (_sdkArchiveTextContent.contains("os=\"windows\"") && platformWindows == true)
			{
				saveThisURL = true;
			}
			else if (_sdkArchiveTextContent.contains("os=\"macosx\"") && platformMac == true)
			{
				saveThisURL = true;
			}
			// 取出SDKArchive的URL
			if (saveThisURL == true)
			{
				String _sdkArchive_url = _sdkArchiveTextContent.substring(_sdkArchiveTextContent.indexOf("<sdk:url>") + "<sdk:url>".length());
				_sdkArchive_url = _sdkArchive_url.substring(0, _sdkArchive_url.indexOf("</sdk:url>"));
				arrListSDKArchiveUrls.add(changeHttpsToHttp(tryAddBaseURL(_sdkArchive_url, baseURL)));
			}
		}
		return arrListSDKArchiveUrls;
	}
	
	/**
	 * 根据URL取出BaseURL
	 * 
	 * @param url
	 *            要处理的url
	 * @return 提取好的结果
	 */
	public static String getBaseUrl(String url)
	{
		if (url.indexOf('/') < url.indexOf('.'))// url以文件名结尾
		{
			url = url.substring(0, url.lastIndexOf('/') + 1);
		}
		if (url.lastIndexOf('/') != url.length() - 1)
		{
			url += "/";
		}
		return url;
	}
	
	/**
	 * 把URL的HTTPS协议改为HTTP
	 * 
	 * @param url
	 *            要修改的URL
	 * @return 修改后的URL
	 */
	private static String changeHttpsToHttp(String url)
	{
		if (url.indexOf("https://") == 0)
		{
			url = "http" + url.substring(5);
		}
		return url;
	}
	
	/**
	 * 尝试向URL头部添加baseURL,适用于只有文件名没有通信协议和路径文件名,如果没有则添加否则不添加
	 * 
	 * @param url
	 *            要修改的URL
	 * @param baseURL
	 *            要添加到头部的baseURL
	 * @return 修改后的URL
	 */
	private static String tryAddBaseURL(String url, String baseURL)
	{
		if (baseURL.lastIndexOf("/") != baseURL.length() - 1)
		{
			baseURL += "/";
		}
		if (url.indexOf("http") == -1)
		{
			url = baseURL + url;
		}
		return url;
	}
}
