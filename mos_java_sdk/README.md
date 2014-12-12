MOS JAVA SDK
=============

Example

    import com.meituan.mos.sdk.v1.Client;

    client = new Client(key, secret, url, region, format, timeout, debug);
    JSONObject result = client.GetBalance();
    System.out.println(result.get("balance"));
