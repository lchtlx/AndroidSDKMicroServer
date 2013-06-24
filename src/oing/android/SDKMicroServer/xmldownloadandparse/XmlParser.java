package oing.android.SDKMicroServer.xmldownloadandparse;

import java.io.File;
import java.util.ArrayList;

public class XmlParser
{
	private static int fileNameSequence = 0;// ��ֹ�ļ����ظ�����������
	
	/**
	 * �޸�xml��<sdk:url>�ڵ�,ȥ��Ŀ¼ֻ�����ļ���,���ĵ�ͬ�����ļ������ⷽ��������̫�����ˡ�����
	 * 
	 * @param xmlContent
	 *            Ҫ�޸ĵ�xml�ı�
	 * @return �޸ĺõ�xml�ı�
	 */
	public static StringBuffer parseXmlForSDKManager(StringBuffer xmlContent)
	{
		if (xmlContent == null)
		{
			return null;
		}
		StringBuffer strBuffResult = new StringBuffer();
		String[] strArrSDKArchives = xmlContent.toString().split("<sdk:url>");
		if (strArrSDKArchives.length >= 2 && strArrSDKArchives[1].indexOf(".zip") != -1)// �������.zip��˵����xml�Ǹ�sdk����������
		{
			return null;
		}
		{
			ArrayList<String> arrListSDKXmlFiles = new ArrayList<String>();
			for (int i = 1; i < strArrSDKArchives.length; i++)// ȡ������<sdk:url>�ڵ�����,����ӵ�arrListSDKXmlFiles
			{
				String _sdkUrl_single = strArrSDKArchives[i].split("</sdk:url>")[0];
				_sdkUrl_single = new File(_sdkUrl_single).getName();
				arrListSDKXmlFiles.add(_sdkUrl_single);
				strArrSDKArchives[i] = strArrSDKArchives[i].replace(_sdkUrl_single, "");// ��strArrSDKArchives�ѽ����õĵ�ַ���ɿյ�
			}
			// ��������ʼ����xml����������
			for (int i = 0; i < arrListSDKXmlFiles.size(); i++)
			{
				String _fileName = arrListSDKXmlFiles.get(i);
				String _fileName_main = _fileName.substring(0, _fileName.lastIndexOf('.')) + (++fileNameSequence);
				String _fileName_extensionWithDot = _fileName.substring(_fileName.lastIndexOf('.'));
				arrListSDKXmlFiles.set(i, _fileName_main + _fileName_extensionWithDot);
			}
			// �Ѵ���õĽ��������д��<sdk:url>�ڵ�
			strBuffResult.append(strArrSDKArchives[0]);
			for (int i = 1; i < strArrSDKArchives.length; i++)
			{
				String _sdkUrl_splited = strArrSDKArchives[i].split("</sdk:url>")[1];// ��ʵ���ǲ�����</sdk:url>����ǰ�߲��ֵ��ı�,����split��Ԫ��0�Ǹ����ַ���
				strBuffResult.append("<sdk:url>" + arrListSDKXmlFiles.get(i - 1) + "</sdk:url>" + _sdkUrl_splited);
				if (i < strArrSDKArchives.length - 1)// �ж��Ƿ�����һ��Ҫ������ı�
				{
					// �����,���滻_sdkUrl_splited��"",��֤�´�ƴ�ӵ�ʱ�򲻻���һ��_sdkUrl_splited
					strArrSDKArchives[i + 1] = strArrSDKArchives[i + 1].replace(_sdkUrl_splited, "");
				}
			}
		}
		return strBuffResult;
	}
	
	/**
	 * ����SDK��XML�ļ���ֻȡ��<sdk:url>����xml�ļ��ĵ�ַ
	 * 
	 * @param strBuffXmlContent
	 *            Ҫ�������ı�
	 * @param baseURL
	 *            ����ַ
	 * @return �������
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
			// ȡ��<sdk:url>������
			String _sdkUrl = strArrSDKUrls[i].substring(0, strArrSDKUrls[i].indexOf("</sdk:url>"));
			if (_sdkUrl.indexOf(".xml") != -1)
			{
				arrListSDKXmlUrls.add(changeHttpsToHttp(tryAddBaseURL(_sdkUrl, baseURL)));
			}
		}
		return arrListSDKXmlUrls;
	}
	
	/**
	 * ����SDK��XML�ļ���ֻȡ��<sdk:url>����zip�ļ��ĵ�ַ
	 * 
	 * @param strBuffXmlContent
	 *            Ҫ�������ı�
	 * @param baseURL
	 *            ����ַ
	 * @param platformLinux
	 *            ������linuxʹ�õİ�
	 * @param platformWindows
	 *            ������Windowsʹ�õİ�
	 * @param platformMac
	 *            ������MacOSXʹ�õİ�
	 * @return �������
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
			// �ָ������<sdk:archive>�ڵ�
			String[] _strArrSDKArchives_preview = strBuffXmlContent.toString().split("<sdk:archive");
			strArrSDKArchives = new String[_strArrSDKArchives_preview.length - 1];
			for (int i = 1; i < _strArrSDKArchives_preview.length; i++)
			{
				strArrSDKArchives[i - 1] = _strArrSDKArchives_preview[i].split("</sdk:archive>")[0];
			}
		}
		for (int i = 0; i < strArrSDKArchives.length; i++)
		{
			if (strArrSDKArchives[i].indexOf(".zip") == -1)// �������zip��������
			{
				continue;
			}
			String _sdkArchiveTextContent = strArrSDKArchives[i];
			boolean saveThisURL = false;
			// ������SDKArchive�������ĸ�����ϵͳ
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
			// ȡ��SDKArchive��URL
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
	 * ����URLȡ��BaseURL
	 * 
	 * @param url
	 *            Ҫ�����url
	 * @return ��ȡ�õĽ��
	 */
	public static String getBaseUrl(String url)
	{
		if (url.indexOf('/') < url.indexOf('.'))// url���ļ�����β
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
	 * ��URL��HTTPSЭ���ΪHTTP
	 * 
	 * @param url
	 *            Ҫ�޸ĵ�URL
	 * @return �޸ĺ��URL
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
	 * ������URLͷ�����baseURL,������ֻ���ļ���û��ͨ��Э���·���ļ���,���û������ӷ������
	 * 
	 * @param url
	 *            Ҫ�޸ĵ�URL
	 * @param baseURL
	 *            Ҫ��ӵ�ͷ����baseURL
	 * @return �޸ĺ��URL
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
