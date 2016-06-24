package com.weisen.xcxf.tool;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.http.cookie.Cookie;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.weisen.xcxf.app.MyApplication;

public class HttpTool {
	private static AsyncHttpClient client = new AsyncHttpClient(); // 实例话对象
	private static PersistentCookieStore myCookieStore;
	static {
		client.setTimeout(5000); // 设置链接超时，如果不设置，默认为10s
		myCookieStore = new PersistentCookieStore(MyApplication.instance);
		client.setCookieStore(myCookieStore);
	}

	public static void get(String urlString, AsyncHttpResponseHandler res) // 用一个完整url获取一个string对象
	{
		client.get(urlString, res);
	}

	public static void get(String urlString, RequestParams params,AsyncHttpResponseHandler res) // url里面带参数
	{
		client.get(urlString, params, res);
	}

	public static void post(String url, RequestParams params,AsyncHttpResponseHandler responseHandler) {

		client.post(url, params, responseHandler);
	}


	public static void postFile(String url, RequestParams params,AsyncHttpResponseHandler responseHandler) {
		client.addHeader("Content-Type",
				"multipart/form-data;boundary=276443266232757");
		client.post(null, url, null, params,
				"multipart/form-data;boundary=276443266232757", responseHandler);
		// client.post(url, params, responseHandler);
	}

	public static void get(String urlString, JsonHttpResponseHandler res) // 不带参数，获取json对象或者数组
	{
		client.get(urlString, res);
	}

	public static void get(String urlString, RequestParams params,JsonHttpResponseHandler res) // 带参数，获取json对象或者数组
	{
		client.get(urlString, params, res);
	}

	public static void get(String uString, BinaryHttpResponseHandler bHandler) // 下载数据使用，会返回byte数据
	{
		client.get(uString, bHandler);
	}

	public static AsyncHttpClient getClient() {
		return client;
	}

	public static void clearCookie() {
		client.setCookieStore(null);
	}

	// 声明称为静态变量有助于调用
	public static byte[] readImage(String imgUrl) throws Exception {
		URL url = new URL(imgUrl);
		// 记住使用的是HttpURLConnection类
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		// 如果运行超过5秒会自动失效 这是android规定
		conn.setConnectTimeout(5 * 1000);
		InputStream inStream = conn.getInputStream();
		// 调用readStream方法
		return readStream(inStream);
	}

	public static byte[] readStream(InputStream inStream) throws Exception {
		// 把数据读取存放到内存中去
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}

	// 声明称为静态变量有助于调用
	public static void writeImage(String imgUrl, String path) throws Exception {
		try {
			// 得到图片地址
			byte[] data = readImage(imgUrl);
			File file = new File(path);
			if (!file.exists())
				file.createNewFile();
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(data);
			// 关闭流的这个地方需要完善一下
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 声明称为静态变量有助于调用
	public static byte[] readFile(String path) {
		try {
			File file = new File(path);
			if (file.exists()) {
				FileInputStream stream = new FileInputStream(file);
				return readStream(stream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String post(String actionUrl, Map<String, String> params,
			Map<String, File> files, String name) throws IOException {
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 10 * 1024;// 10KB

		URL uri = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		String JSESSIONID  = "";
		List<Cookie> list = myCookieStore.getCookies();
		for (int i = 0; i < list.size(); i++) {
			if (i==0) {
				JSESSIONID  = list.get(i).getName()+"="+list.get(i).getValue();
			}else{
				JSESSIONID  += ";"+list.get(i).getName()+"="+list.get(i).getValue();
			}
		}
		conn.setReadTimeout(10 * 1000); // 缓存的最长时间
		conn.setRequestProperty("Cookie", JSESSIONID );
		conn.setReadTimeout(10 * 1000); // 缓存的最长时间
		conn.setConnectTimeout(10 * 1000);
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false); // 不允许使用缓存
		conn.setRequestMethod("POST");
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charsert", "UTF-8");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
				+ ";boundary=" + BOUNDARY);

		StringBuilder sb = new StringBuilder();
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINEND);
				sb.append("Content-Disposition: form-data; name=\""
						+ entry.getKey() + "\"" + LINEND);
				sb.append("Content-Type: text/plain; charset=" + CHARSET
						+ LINEND);
				sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
				sb.append(LINEND);
				sb.append(entry.getValue());
				sb.append(LINEND);
			}
		}
		try {
			conn.connect();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

		// 首先组拼文本类型的参数

		DataOutputStream outStream = new DataOutputStream(
				conn.getOutputStream());
		if (params != null) {
			outStream.write(sb.toString().getBytes());
		}
		// 发送文件数据

		if (files != null) {

			for (Map.Entry<String, File> file : files.entrySet()) {
				try {
					StringBuilder sb1 = new StringBuilder();
					sb1.append(PREFIX);
					sb1.append(BOUNDARY);
					sb1.append(LINEND);
					sb1.append("Content-Disposition: form-data; name=\"" + name
							+ "\"; filename=\"" + file.getValue().getName()
							+ "\"" + LINEND);
					sb1.append("Content-Type: application/octet-stream; charset="
							+ CHARSET + LINEND);
					sb1.append(LINEND);
					outStream.write(sb1.toString().getBytes());
					InputStream fileInputStream = new FileInputStream(
							file.getValue());
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);// 设置每次写入的大小
					buffer = new byte[bufferSize];
					// Read file
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

					while (bytesRead > 0) {
						outStream.write(buffer, 0, bufferSize);

						bytesAvailable = fileInputStream.available();
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);
					}
					fileInputStream.close();
					outStream.write(LINEND.getBytes());
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return null;
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}

			}

		}

		// 请求结束标志
		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
		outStream.write(end_data);
		outStream.flush();
		// 得到响应码
		StringBuilder sb2 = new StringBuilder();
		int res = 0;
		try {
			res = conn.getResponseCode();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		String result = null;
		Log.d("res",Integer.toString(res));
		if (res == 200) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getInputStream(), "UTF-8"));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb2.append(line);
			}
			result = sb2.toString();

		}
		Log.d("result",result);
		outStream.close();
		conn.disconnect();
		if (result != null) {
			result = result.replaceAll("null", "\"\"");
		}
		return result;
	}

	public static String get(String url) {
		if (url == null || "".equals(url)) {
			return null;
		}
		String result = null;
		StringBuilder sb2 = new StringBuilder();
		int res = 0;
		try {
			URL uri = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
			conn.setReadTimeout(10 * 1000); // 缓存的最长时间
			conn.setConnectTimeout(10 * 1000);
			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("GET");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Charsert", "UTF-8");
			try {
				res = conn.getResponseCode();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			if (res == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = "";
				while ((line = br.readLine()) != null) {
					sb2.append(line);
				}
				result = sb2.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static String post02(String actionUrl, Map<String, String> params,
								Map<String, File> files)
			throws IOException {

		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";

		URL uri = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		conn.setReadTimeout(5 * 1000); // 缓存的最长时间
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false); // 不允许使用缓存
		conn.setRequestMethod("POST");
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charsert", "UTF-8");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
				+ ";boundary=" + BOUNDARY);

		// 首先组拼文本类型的参数
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINEND);
			sb.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"" + LINEND);
			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
			sb.append(LINEND);
			sb.append(entry.getValue());
			sb.append(LINEND);
		}

		DataOutputStream outStream = new DataOutputStream(
				conn.getOutputStream());
		outStream.write(sb.toString().getBytes());
		InputStream in = null;
		StringBuilder sb3 = null;
		// 发送文件数据
		if (files != null) {
			DataOutputStream outStream2 = new DataOutputStream(
					conn.getOutputStream());
			for (Map.Entry<String, File> file : files.entrySet()) {
				StringBuilder sb1 = new StringBuilder();
				sb1.append(PREFIX);
				sb1.append(BOUNDARY);
				sb1.append(LINEND);
				sb1.append("Content-Disposition: form-data; name=\"icon\"; filename=\""
						+ file.getValue().getName() + "\"" + LINEND);// img
				sb1.append("Content-Type: application/octet-stream; charset="
						+ CHARSET + LINEND);
				sb1.append(LINEND);
				outStream2.write(sb1.toString().getBytes());

				InputStream is = new FileInputStream(file.getValue());
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					outStream2.write(buffer, 0, len);
				}

				is.close();
				outStream2.write(LINEND.getBytes());
			}

			// 请求结束标志
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
			outStream2.write(end_data);
			outStream2.flush();


		}


		int res = conn.getResponseCode();
		sb3 = new StringBuilder();
		if (res == 200) {
			in = conn.getInputStream();
			int ch;

			while ((ch = in.read()) != -1) {
				sb3.append((char) ch);
			}
		}
		outStream.close();
		conn.disconnect();

		String xmString = "";
		// String xmlUTF8="";
		try {

			xmString = new String(sb3.toString().getBytes("ISO-8859-1"),
					"UTF-8");
			// xmlUTF8 = URLEncoder.encode(xmString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			xmString = "";
			return xmString;
		}
		return xmString;
	}
}
