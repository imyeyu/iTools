package net.imyeyu.itools;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.imyeyu.itools.bean.HTTPInfo;

public class NetworkUtils {

	public static String doGet(String url, Map<String, String> params, boolean isSSL) throws ConnectException, UnknownHostException, Exception {
		String sendUrl = EncodeUtils.enURL(url, params);
		URL uri = new URL(sendUrl);

		HttpURLConnection connect = (HttpURLConnection) uri.openConnection();
		if (isSSL) {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[] { myX509TrustManager }, null);
			if (connect instanceof HttpsURLConnection) {
				((HttpsURLConnection) connect).setSSLSocketFactory(sslcontext.getSocketFactory());
			}
		}
		connect.setRequestMethod("GET");
		setRequestHeader(connect);

		String line = null;
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));
		while ((line = br.readLine()) != null) {
			sb.append(line + "\r\n");
		}
		br.close();
		connect.disconnect();

		return sb.toString();
	}

	private static TrustManager myX509TrustManager = new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	};

	public static String doGet(HTTPInfo http) throws UnknownHostException, ConnectException, Exception {
		String urlString = http.getUrl() + "?" + http.getParam();
		URL realUrl = new URL(urlString);

		HttpURLConnection connect = (HttpURLConnection) realUrl.openConnection();
		connect.setRequestProperty("accept", "*/*");
		connect.setRequestProperty("connection", "Keep-Alive");
		connect.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3314.0 Safari/537.36 SE 2.X MetaSr 1.0");
		connect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connect.setRequestProperty("host", http.getHost());
		connect.setRequestProperty("Accept-Charset", http.getCharset());
		connect.setRequestProperty("Cookie", http.getCookie());
		connect.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream(), http.getCharset()));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line + "\r\n");
		}
		br.close();
		connect.disconnect();
		return sb.toString();
	}

	public static String doGet(String url) throws UnknownHostException, ConnectException, Exception {
		return doGet(url, "");
	}

	public static String doGet(String url, String params) throws UnknownHostException, ConnectException, Exception {
		HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
		connect.setRequestMethod("GET");
		setRequestHeader(connect);

		BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line + "\r\n");
		}
		br.close();
		return sb.toString();
	}

	public static String doPost(String url, String params) throws UnknownHostException, ConnectException, Exception {
		PrintWriter out = null;

		HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
		connect.setDoOutput(true);
		connect.setDoInput(true);
		connect.setRequestMethod("POST");
		setRequestHeader(connect);
		connect.connect();

		out = new PrintWriter(connect.getOutputStream());
		out.write(params);
		out.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line + "\r\n");
		}
		out.close();
		br.close();
		return sb.toString();
	}

	public static String doPost(String url, Map<String, String> params) throws UnknownHostException, ConnectException, Exception {
		return doPost(EncodeUtils.enURL(url, params));
	}

	public static String doPost(String url) throws UnknownHostException, ConnectException, Exception {
		return doPost(url, "");
	}

	public static void openURIInBrowser(URI uri) {
		try {
			Desktop dp = Desktop.getDesktop();
			if (dp.isSupported(Desktop.Action.BROWSE)) {
				dp.browse(uri);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int pingHostByCMD(String ip) {
		Runtime rt = Runtime.getRuntime();
		int ping = -1;
		try {
			Process process = rt.exec("ping " + ip + " -n 1");
			process.isAlive();
			StringBuffer sb = new StringBuffer();
			InputStreamReader isr = new InputStreamReader(process.getInputStream(), "GB2312");
			BufferedReader br = new BufferedReader(isr);
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			line = sb.toString();
			if (!(line.indexOf("请求超时") != -1 || line.indexOf("timed out") != -1)) {
				int start = 0, end = 0;
				if (line.indexOf("平均") != -1) {
					start = line.indexOf("平均") + 5;
					end = line.indexOf("ms", start);
				} else {
					start = line.indexOf("Average") + 10;
					end = line.indexOf("ms", start);
				}
				ping = Integer.parseInt(line.substring(start, end));
			}
			br.close();
			isr.close();
			return ping;
		} catch (Exception e) {
			return ping;
		}
	}

	public static void downloadFile(String url, String path, String fileName)
			throws MalformedURLException, FileNotFoundException, IOException, Exception {
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();

		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setConnectTimeout(8000);
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		InputStream is = conn.getInputStream();
		byte[] buffer = new byte[1024];
		int l = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((l = is.read(buffer)) != -1) {
			bos.write(buffer, 0, l);
		}
		byte[] getData = bos.toByteArray();
		File file = new File(dir + File.separator + fileName);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(getData);
		fos.close();
		bos.close();
		is.close();
	}

	public static String getNetworkIp() throws Exception {
		String response = doPost("http://ip.chinaz.com", "");
		Matcher m = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>").matcher(response);
		return m.find() ? m.group(1) : "";
	}

	public static boolean isBusyPort(int port) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress("localhost", port), 500);
			socket.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 设置请求头
	 * 
	 * @param conn 连接对象
	 */
	private static void setRequestHeader(URLConnection connect) {
		connect.setRequestProperty("accept", "*/*");
		connect.setRequestProperty("connection", "Keep-Alive");
		connect.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		connect.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connect.setRequestProperty("Accept-Charset", "UTF-8");
		connect.setConnectTimeout(8000);
	}
}