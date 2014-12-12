package com.meituan.mos.sdk.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {
	
	private static Pattern duration_pattern = Pattern.compile("^\\d+[HhMm]$");
	public static boolean matchDuration(String dura) {
		Matcher matcher = duration_pattern.matcher(dura);
		return matcher.find();
	}
	
	public static void disableHttpsVerification() {
		X509TrustManager tm = new X509TrustManager() {
			@Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

		TrustManager[] trustAllCerts = new TrustManager[] {tm};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	private static final JSONObject empty = new JSONObject();
	public static JSONObject getJSONResult(JSONObject data, String field) {
		if (data.length() == 0 || !data.has(field) || !(data.get(field) instanceof JSONObject)) {
			return empty;
		}else {
			return data.getJSONObject(field);
		}
	}
	
	public static JSONArray stringArray2JSONArray(String[] array) {
		JSONArray ret = new JSONArray();
		for(String str: array) {
			ret.put(str);
		}
		return ret;
	}
	
	private static String decrypt(Key decryptionKey, byte[] buffer) {
	    try {
	        Cipher rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
	        byte[] utf8 = rsa.doFinal(buffer);
	        return new String(utf8, "UTF8");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	private static KeyPair readKeyPair(String keyPath, String keyPassword) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(keyPath));
		Security.addProvider(new BouncyCastleProvider());
		PEMParser pp = new PEMParser(br);
		PEMKeyPair pemKeyPair = (PEMKeyPair) pp.readObject();
		KeyPair kp = new JcaPEMKeyConverter().getKeyPair(pemKeyPair);
		pp.close();
		return kp;
	}
	
	public static String decrypt_password(String keyfile, String keypass, String data) throws IOException {
		KeyPair kp = readKeyPair(keyfile, keypass);
		byte[] secret = data.getBytes();
		return decrypt(kp.getPrivate(), secret);
 	}

}
