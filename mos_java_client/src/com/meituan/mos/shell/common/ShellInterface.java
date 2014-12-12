package com.meituan.mos.shell.common;


public interface ShellInterface {
	public void initClient(String key, String secret, String url, String region,
			String format, int timeout, boolean debug);
}
