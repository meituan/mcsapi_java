package com.meituan.mos.shell.common;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.argparse4j.inf.Namespace;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bethecoder.ascii_table.ASCIITable;


public class Utils {
	public static int getArgInt(Namespace cmd, String name, int defval) {
		Integer val = cmd.getInt(name);
		if (val == null) {
			return defval;
		}else {
			return val.intValue();
		}
	}
	
	public static boolean getArgBoolean(Namespace cmd, String name) {
		Boolean val = cmd.getBoolean(name);
		if (val == null) {
			return false;
		}else {
			return val.booleanValue();
		}
	}
	
	public static String getArgString(Namespace cmd, String name, String defval) {
		String val = cmd.getString(name);
		if (val == null) {
			return defval;
		}else {
			return val;
		}
	}
	
	public static Map<String, List<String>> getArgFilters(Namespace cmd) {
		String name = "filter";
		Map<String, List<String>> filters = new HashMap<String,List<String>>();
		List<String> args = cmd.getList(name);
		if (args != null) {
			for (String arg: args) {
				String[] parts = arg.split(":");
				if (parts.length >= 2) {
					List<String> vals;
					String key = parts[0];
					if (filters.containsKey(key)) {
						vals = filters.get(key);
					}else {
						vals = new ArrayList<String>();
					}
					for(int i = 1; i < parts.length; i ++) {
						vals.add(parts[i]);
					}
					if (!filters.containsKey(key)) {
						filters.put(key, vals);
					}
				}
			}
		}
		return filters;
	}
	
	public static String[] getArgStringArray(Namespace cmd, String name) {
		List<String> args = cmd.getList(name);
		if (args != null) {
			String[] rets = new String[args.size()];
			for(int i = 0; i < args.size(); i ++) {
				rets[i] = args.get(i);
			}
			return rets;
		}else {
			return null;
		}
	}
	
	public static void printDict(JSONObject dict) {
		if (dict.length() == 0) {
			return;
		}
		String[] colnames = {"Property", "Value"};
		String[][] data = new String[dict.length()][2];
		Iterator keys = dict.keys();
		int i = 0;
		while(keys.hasNext()) {
			String key = (String)keys.next();
			data[i][0] = key;
			data[i][1] = dict.get(key).toString();
			i++;
		}
		ASCIITable.getInstance().printTable(colnames, data);
	}
	
	public static void printList(JSONObject data, String field, String[] colnames) {
		if (data.length() == 0 || !data.has(field) || data.isNull(field)) {
			return;
		}
		JSONArray items = null;
		Object tmp = data.get(field);
		if (tmp instanceof JSONObject) {
			items = new JSONArray();
			items.put(tmp);
		}else if (tmp instanceof JSONArray) {
			items = (JSONArray)tmp;
		}else {
			return;
		}
		int limit = 0;
		if (data.has("limit")) {
			limit = data.getInt("limit");
		}
		int offset = 0;
		if (data.has("offset")) {
			offset = data.getInt("offset");
		}
		int total = 0;
		if (data.has("total")) {
			total = data.getInt("total");
		}
		if (colnames == null) {
			Set<String> colset = new HashSet<String>();
			for(int i = 0; i < items.length(); i ++) {
				colset.addAll(items.getJSONObject(i).keySet());
			}
			colnames = new String[colset.size()];
			colset.toArray(colnames);
		}
		String[][] cells = new String[items.length()][colnames.length];
		for(int i = 0; i < items.length(); i ++) {
			for(int j = 0; j < colnames.length; j ++) {
				if (items.getJSONObject(i).has(colnames[j])) {
					cells[i][j] = "" + items.getJSONObject(i).get(colnames[j]);
				}else {
					cells[i][j] = "";
				}
			}
		}
		ASCIITable.getInstance().printTable(colnames, cells);
		if (limit > 0) {
			int pages = total/limit;
			if (pages*limit < total) {
				pages ++;
			}
			int page = (offset/limit) + 1;
			System.out.println("Total: " + total + " Pages: " + pages + " Limit: " + limit + " Offset: " + offset + " Page: " + page);
		}else {
			System.out.println("Total: " + items.length());
		}
	}
	
	public static String fileGetContent(String filename) throws IOException
	{
		FileReader reader = new FileReader(filename);
	    StringBuilder builder = new StringBuilder();
	    int rd = 0;
	    char[] charbuf = new char[4096];
	    // For every line in the file, append it to the string builder
	    while((rd = reader.read(charbuf)) > 0)
	    {
	        builder.append(new String(charbuf, 0, rd));
	    }

	    reader.close();
	    return builder.toString();
	}

	
}
