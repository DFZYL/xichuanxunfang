package com.weisen.xcxf.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import com.weisen.xcxf.bean.CellInfo;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

public class UtilTool {

	public static boolean isGpsEnabled(LocationManager locationManager) {
		boolean isOpenGPS = locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		boolean isOpenNetwork = locationManager
				.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
		if (isOpenGPS || isOpenNetwork) {
			return true;
		}
		return false;
	}

	/**
	 * 根据基站信息获取经纬度
	 * 
	 * 原理向http://www.google.com/loc/json发送http的post请求，根据google返回的结果获取经纬度
	 * 
	 * @param cellIds
	 * @return
	 * @throws Exception
	 */
	public static Location callGear(Context ctx) throws Exception {
		String result = "";
		JSONObject data = null;
		ArrayList<CellInfo> cellIds = init(ctx);
		if (cellIds == null || cellIds.size() == 0) {
			return null;
		}
		try {
			String url = "http://api.cellid.cn/cellid.php";
			String params = getRequestParams(cellIds);
			result = sendGetRequest(url + "?" + params);
			if (result.length() <= 1)
				return null;
			data = new JSONObject(result);

			Location loc = new Location(LocationManager.NETWORK_PROVIDER);
			loc.setLatitude((Double) data.get("lat"));
			loc.setLongitude((Double) data.get("lon"));
			return loc;
		} catch (JSONException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 接收Google返回的数据格式
	 * 
	 * 出参：{"location":{"latitude":26.0673834,"longitude":119.3119936,
	 * "address":{
	 * "country":"ä¸­å½","country_code":"CN","region":"ç¦å»ºç","city"
	 * :"ç¦å·å¸",
	 * "street":"äºä¸ä¸­è·¯","street_number":"128å·"},"accuracy":935.0},
	 * "access_token":"2:xiU8YrSifFHUAvRJ:aj9k70VJMRWo_9_G"}
	 * 请求路径：http://maps.google.cn/maps/geo?key=abcdefg&q=26.0673834,119.3119936
	 * 
	 * @param cellIds
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws Exception
	 */
	public static String getResponseResult(Context ctx, String path,
			ArrayList<CellInfo> cellInfos) throws UnsupportedEncodingException,
			MalformedURLException, IOException, ProtocolException, Exception {
		String result = "";
		Log.i(ctx.getApplicationContext().getClass().getSimpleName(),
				"in param: " + getRequestParams(cellInfos));
		result = HttpTool.get(path + "?" + getRequestParams(cellInfos));
		System.out.println(result);
		return result;
	}

	/**
	 * 拼装json请求参数，拼装基站信息
	 * 
	 * 入参：{'version': '1.1.0','host':
	 * 'maps.google.com','home_mobile_country_code': 460,
	 * 'home_mobile_network_code': 14136,'radio_type': 'cdma','request_address':
	 * true, 'address_language': 'zh_CN','cell_towers':[{'cell_id':
	 * '12835','location_area_code': 6, 'mobile_country_code':
	 * 460,'mobile_network_code': 14136,'age': 0}]}
	 * 
	 * @param cellInfos
	 * @return
	 */
	public static String getRequestParams(List<CellInfo> cellInfos) {
		StringBuffer sb = new StringBuffer("");
		if (cellInfos != null && cellInfos.size() > 0) {
			CellInfo cellInfo = cellInfos.get(0);
			sb.append("&cell_id=" + cellInfo.getCellId());
			sb.append("&lac=" + cellInfo.getLocationAreaCode());
			sb.append("&token=0e7e3049ab8c578bb360aed914bacd3e");
		}
		return sb.toString();
	}

	/**
	 * 获取UTC时间
	 * 
	 * UTC + 时区差 ＝ 本地时间(北京为东八区)
	 * 
	 * @return
	 */
	public static long getUTCTime() {
		// 取得本地时间
		Calendar cal = Calendar.getInstance(Locale.CHINA);
		// 取得时间偏移量
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 取得夏令时差
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 从本地时间里扣除这些差量，即可以取得UTC时间
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		return cal.getTimeInMillis();
	}

	/**
	 * 初始化，记得放在onCreate()方法里初始化，获取基站信息
	 * 
	 * @return
	 */
	public static ArrayList<CellInfo> init(Context ctx) {
		ArrayList<CellInfo> cellInfos = new ArrayList<CellInfo>();

		TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		// 网络制式
		int type = tm.getNetworkType();
		/**
		 * 获取SIM卡的IMSI码 SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile
		 * Subscriber Identification Number）是区别移动用户的标志，
		 * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
		 * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
		 * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
		 * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
		 */
		String imsi = tm.getSubscriberId();
		// 为了区分移动、联通还是电信，推荐使用imsi来判断(万不得己的情况下用getNetworkType()判断，比如imsi为空时)
		if (imsi != null && !"".equals(imsi)) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
				// 中国移动
				mobile(cellInfos, tm);
			} else if (imsi.startsWith("46001")) {
				// 中国联通
				union(cellInfos, tm);
			} else if (imsi.startsWith("46003")) {
				// 中国电信
				cdma(cellInfos, tm);
			}
		} else {
			// 在中国，联通的3G为UMTS或HSDPA，电信的3G为EVDO
			// 在中国，移动的2G是EGDE，联通的2G为GPRS，电信的2G为CDMA
			// String OperatorName = tm.getNetworkOperatorName();

			// 中国电信
			if (type == TelephonyManager.NETWORK_TYPE_EVDO_A
					|| type == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| type == TelephonyManager.NETWORK_TYPE_CDMA
					|| type == TelephonyManager.NETWORK_TYPE_1xRTT) {
				cdma(cellInfos, tm);
			}
			// 移动(EDGE（2.75G）是GPRS（2.5G）的升级版，速度比GPRS要快。目前移动基本在国内升级普及EDGE，联通则在大城市部署EDGE。)
			else if (type == TelephonyManager.NETWORK_TYPE_EDGE
					|| type == TelephonyManager.NETWORK_TYPE_GPRS) {
				mobile(cellInfos, tm);
			}
			// 联通(EDGE（2.75G）是GPRS（2.5G）的升级版，速度比GPRS要快。目前移动基本在国内升级普及EDGE，联通则在大城市部署EDGE。)
			else if (type == TelephonyManager.NETWORK_TYPE_GPRS
					|| type == TelephonyManager.NETWORK_TYPE_EDGE
					|| type == TelephonyManager.NETWORK_TYPE_UMTS
					|| type == TelephonyManager.NETWORK_TYPE_HSDPA) {
				union(cellInfos, tm);
			}
		}

		return cellInfos;
	}

	/**
	 * 电信
	 * 
	 * @param cellInfos
	 * @param tm
	 */
	private static void cdma(ArrayList<CellInfo> cellInfos, TelephonyManager tm) {
		CdmaCellLocation location = (CdmaCellLocation) tm.getCellLocation();
		CellInfo info = new CellInfo();
		info.setCellId(location.getBaseStationId());
		info.setLocationAreaCode(location.getNetworkId());
		info.setMobileNetworkCode(String.valueOf(location.getSystemId()));
		info.setMobileCountryCode(tm.getNetworkOperator().substring(0, 3));
		info.setRadioType("cdma");
		cellInfos.add(info);

		// 前面获取到的都是单个基站的信息，接下来再获取周围邻近基站信息以辅助通过基站定位的精准性
		// 获得邻近基站信息
		List<NeighboringCellInfo> list = tm.getNeighboringCellInfo();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			CellInfo cell = new CellInfo();
			cell.setCellId(list.get(i).getCid());
			cell.setLocationAreaCode(location.getNetworkId());
			cell.setMobileNetworkCode(String.valueOf(location.getSystemId()));
			cell.setMobileCountryCode(tm.getNetworkOperator().substring(0, 3));
			cell.setRadioType("cdma");
			cellInfos.add(cell);
		}
	}

	/**
	 * 移动
	 * 
	 * @param cellInfos
	 * @param tm
	 */
	private static void mobile(ArrayList<CellInfo> cellInfos,
			TelephonyManager tm) {
		GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
		CellInfo info = new CellInfo();
		info.setCellId(location.getCid());
		info.setLocationAreaCode(location.getLac());
		info.setMobileNetworkCode(tm.getNetworkOperator().substring(3, 5));
		info.setMobileCountryCode(tm.getNetworkOperator().substring(0, 3));
		info.setRadioType("gsm");
		cellInfos.add(info);

		// 前面获取到的都是单个基站的信息，接下来再获取周围邻近基站信息以辅助通过基站定位的精准性
		// 获得邻近基站信息
		List<NeighboringCellInfo> list = tm.getNeighboringCellInfo();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			CellInfo cell = new CellInfo();
			cell.setCellId(list.get(i).getCid());
			cell.setLocationAreaCode(location.getLac());
			cell.setMobileNetworkCode(tm.getNetworkOperator().substring(3, 5));
			cell.setMobileCountryCode(tm.getNetworkOperator().substring(0, 3));
			cell.setRadioType("gsm");
			cellInfos.add(cell);
		}
	}

	/**
	 * 联通
	 * 
	 * @param cellInfos
	 * @param tm
	 */
	private static void union(ArrayList<CellInfo> cellInfos, TelephonyManager tm) {
		GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
		CellInfo info = new CellInfo();
		// 经过测试，获取联通数据以下两行必须去掉，否则会出现错误，错误类型为JSON Parsing Error
		// info.setMobileNetworkCode(tm.getNetworkOperator().substring(3, 5));
		// info.setMobileCountryCode(tm.getNetworkOperator().substring(0, 3));
		info.setCellId(location.getCid());
		info.setLocationAreaCode(location.getLac());
		info.setMobileNetworkCode("");
		info.setMobileCountryCode("");
		info.setRadioType("gsm");
		cellInfos.add(info);

		// 前面获取到的都是单个基站的信息，接下来再获取周围邻近基站信息以辅助通过基站定位的精准性
		// 获得邻近基站信息
		List<NeighboringCellInfo> list = tm.getNeighboringCellInfo();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			CellInfo cell = new CellInfo();
			cell.setCellId(list.get(i).getCid());
			cell.setLocationAreaCode(location.getLac());
			cell.setMobileNetworkCode("");
			cell.setMobileCountryCode("");
			cell.setRadioType("gsm");
			cellInfos.add(cell);
		}
	}

	/**
	 * 提示
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void alert(Context ctx, String msg) {
		Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
	}

	/**
	 * 发送get请求
	 * 
	 * @param path
	 *            请求路径
	 * @return
	 * @throws Exception
	 */
	public static String sendGetRequest(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		InputStream inStream = conn.getInputStream();
		byte[] data = readInputStream(inStream);
		String result = new String(data, "UTF-8");
		System.out.println(result);
		return result;
	}

	/**
	 * 从输入流中读取数据
	 * 
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();// 网页的二进制数据
		outStream.close();
		inStream.close();
		return data;
	}

}