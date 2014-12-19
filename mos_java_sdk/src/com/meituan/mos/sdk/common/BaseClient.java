package com.meituan.mos.sdk.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;
import org.json.XML;


public class BaseClient {
	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_XML = "xml";
	public static final String USER_AGENT = "java-mosclient";
	private static final String REQUEST_METHOD = "POST";

	private String access_id;
	private String access_secret;
	private String region;
	private String format;
	private String request_url;
	private int timeout;
	private boolean debug;
	
	public BaseClient(String key, String secret, String url, String region,
			String format, int timeout, boolean debug) {
		this.access_id = key;
		this.access_secret = secret;
		this.region = region;
		this.format = format;
		this.timeout = timeout;
		this.request_url = url;
		this.debug = debug;
	}

	private static String getTimeStamp() {
		final Date date = new Date();
		final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'000Z'";
		final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
		final TimeZone utc = TimeZone.getTimeZone("UTC");
		sdf.setTimeZone(utc);
		return sdf.format(date);
	}

	private InputStream _request(String url, String action, JSONObject kwargs)
			throws Exception {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Action", action);
		params.put("AWSAccessKeyId", access_id);
		params.put("Timestamp", getTimeStamp());
		params.put("SignatureVersion", "2");
		params.put("SignatureMethod", "HmacSHA256");
		if (region != null && region.length() > 0) {
			params.put("Region", region);
		}
		if (kwargs != null) {
			Iterator keys = kwargs.keys();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				try {
					Object value = kwargs.get(key);
					//System.out.println(key + ": " + value.getClass().getName());
					if (value instanceof JSONArray) {
						JSONArray array = (JSONArray) value;
						try {
							for (int i = 1; i <= array.length(); i++) {
								params.put(key + "." + i, array.getString(i - 1));
							}
						} catch (Exception e) {
	
						}
					} else {
						params.put(key, value.toString());
					}
				} catch (Exception e) {
	
				}
			}
		}
		if (format != null && (format == FORMAT_JSON || format == FORMAT_XML)) {
			params.put("Format", format);
		}
		URL requrl = new URL(url);
		String endpoint = requrl.getHost();
		if (requrl.getPort() > 0
				&& !(requrl.getProtocol() == "http" && requrl.getPort() == 80)
				&& !(requrl.getProtocol() == "https" && requrl.getPort() == 443)) {
			endpoint += ":" + requrl.getPort();
		}
		String request_uri = requrl.getPath();
		String signature = SignedRequestsHelper.signRequest(access_secret,
				endpoint, request_uri, REQUEST_METHOD, params);
		
		params.put("Signature", signature);
		
		String data = SignedRequestsHelper.canonicalize(params);
		
		if (debug) {
			System.out.println("Request data: " + data);
		}
		
		if (requrl.getProtocol() == "https") {
			Utils.disableHttpsVerification();
		}
		
		HttpURLConnection req = (HttpURLConnection) requrl.openConnection();
		
		req.setRequestMethod(REQUEST_METHOD);
		req.setRequestProperty("User-Agent", USER_AGENT);
		req.setInstanceFollowRedirects(false);
		req.setConnectTimeout(timeout*1000);
		req.setReadTimeout(timeout*1000);
		
		req.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					
		req.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
		req.setRequestProperty("Content-Language", "en-US");  
					
		req.setUseCaches (false);
		req.setDoInput(true);
		req.setDoOutput(true);

		DataOutputStream wr = new DataOutputStream (req.getOutputStream ());
		wr.writeBytes (data);
		wr.flush ();
		wr.close ();

		int code = req.getResponseCode();
		if (code >= 200 && code < 300) {
			return req.getInputStream();
		} else if (code < 400) {
			String newloc = req.getHeaderField("Location");
			if (debug) {
				System.out.println("Redirect to " + newloc);
			}
			return _request(newloc, action, kwargs);
		} else {
			throw new ServerErrorException(req.getResponseMessage() + "(" + code
					+ ")");
		}
	}
	
	private String _inputStream2String(InputStream input) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		input.close();
		return response.toString();
	}

	protected InputStream raw_request(String action, JSONObject kwargs)
			throws Exception {
		return _request(request_url, action, kwargs);
	}

	protected JSONObject Request(String action, JSONObject kwargs)
			throws Exception {
		InputStream input = _request(request_url, action, kwargs);
		String response = _inputStream2String(input);
		JSONObject result;
		if (debug) {
			System.out.println("Response: " + response);
		}
		if (format == FORMAT_JSON) {
			result = JSONML.toJSONObject(response);
		}else {
			result = XML.toJSONObject(response);
		}
		return result.getJSONObject(action + "Response");
	}

	protected static void parse_list_params(int limit, int offset, Map<String, List<String>> filters, JSONObject kwargs) throws JSONException {
        if (limit > 0) {
        	kwargs.put("Limit", limit);
        }
        if (offset > 0) {
        	kwargs.put("Offset", offset);
        }
        if (filters != null) {
        	int fidx = 1;
        	for(String key : filters.keySet()) {
        		kwargs.put("Filter." + fidx + ".Name", key);
        		List<String> values = filters.get(key);
        		int vidx = 1;
        		for (String val : values) {
        			kwargs.put("Filter." + fidx + ".Value." + vidx, val);
        			vidx ++;
        		}
        		fidx ++;
        	}
        }
	}
	
}
