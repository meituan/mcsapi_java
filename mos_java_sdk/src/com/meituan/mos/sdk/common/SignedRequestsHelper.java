package com.meituan.mos.sdk.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class SignedRequestsHelper {
	private static final String UTF8_CHARSET = "UTF-8";
	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
	
	private String request_uri;
	private String request_method; // must be uppercase
	private String endpoint; // must be lowercase
	private String awsSecretKey;

	private SecretKeySpec secretKeySpec = null;
	private Mac mac = null;

	private SignedRequestsHelper(String secret, String endpoint, String path, String method) {
		this.endpoint = endpoint.toLowerCase();
		this.awsSecretKey = secret;
		this.request_uri = path;
		this.request_method = method.toUpperCase();
		try {
			byte[] secretyKeyBytes = awsSecretKey.getBytes(UTF8_CHARSET);
			secretKeySpec = new SecretKeySpec(secretyKeyBytes,
					HMAC_SHA256_ALGORITHM);
			mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
			mac.init(secretKeySpec);
		}catch(Exception e) {
			
		}
	}

	private String sign(Map<String, String> params) {
		SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(
				params);
		String canonicalQS = canonicalize(sortedParamMap);
		String toSign = request_method + "\n" + endpoint + "\n" + request_uri
				+ "\n" + canonicalQS;
		//System.out.println("To sign: " + toSign);
		return  hmac(toSign);
	}

	private String hmac(String stringToSign) {
		String signature = null;
		byte[] data;
		byte[] rawHmac;
		try {
			data = stringToSign.getBytes(UTF8_CHARSET);
			rawHmac = mac.doFinal(data);
			Base64 encoder = new Base64();
			signature = new String(encoder.encode(rawHmac));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
		}
		return signature;
	}

	public static String canonicalize(Map<String, String> params) {
		if (params.isEmpty()) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();
		Iterator<Map.Entry<String, String>> iter = params.entrySet()
				.iterator();

		while (iter.hasNext()) {
			Map.Entry<String, String> kvpair = iter.next();
			buffer.append(percentEncodeRfc3986(kvpair.getKey()));
			buffer.append("=");
			buffer.append(percentEncodeRfc3986(kvpair.getValue()));
			if (iter.hasNext()) {
				buffer.append("&");
			}
		}
		String canonical = buffer.toString();
		return canonical;
	}

	public static String percentEncodeRfc3986(String s) {
		String out;
		try {
			out = URLEncoder.encode(s, UTF8_CHARSET).replace("+", "%20")
					.replace("*", "%2A").replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			out = s;
		}
		return out;
	}
	
	public static String signRequest(String secret, String endpoint, String path, String method, Map<String, String> params) {
		SignedRequestsHelper helper = new SignedRequestsHelper(secret, endpoint, path, method);
		return helper.sign(params);
	}

}
