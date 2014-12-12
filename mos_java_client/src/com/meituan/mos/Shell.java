package com.meituan.mos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import com.meituan.mos.sdk.common.BaseClient;
import com.meituan.mos.sdk.common.ServerErrorException;
import com.meituan.mos.shell.common.CommandHelp;
import com.meituan.mos.shell.common.CommandOption;
import com.meituan.mos.shell.common.CommandOptions;
import com.meituan.mos.shell.common.ShellInterface;
import com.meituan.mos.shell.common.Utils;


public class Shell {
	ArgumentParser parser;
	Map<String, Subparser> subcmd_tbl;
	
	private ArgumentParser init_cmd_parser() {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("climos").defaultHelp(true)
				.description("MOS java SDK CLI shell");
		
		parser.addArgument("--mos-access").setDefault(System.getenv("MOS_ACCESS")).help("MOS access key, defaults to env[MOS_ACCESS]");
		parser.addArgument("--mos-secret").setDefault(System.getenv("MOS_SECRET")).help("MOS secret, defaults to env[MOS_SECRET]");
		parser.addArgument("--mos-region").setDefault(System.getenv("MOS_REGION")).help("OS region name, defaults to env[REGION]");
		parser.addArgument("--mos-url").setDefault(System.getenv("MOS_URL")).help("MOS api URL, default to env[MOS_URL]");
		
		parser.addArgument("--timeout").type(Integer.class).help("Request timeout in seconds, default 60 seconds");
		parser.addArgument("--debug").action(Arguments.storeTrue()).help("Show debug information");
		parser.addArgument("--format").setDefault(System.getenv("MOS_FORMAT")).choices(BaseClient.FORMAT_JSON,BaseClient.FORMAT_XML).help("Required return content type");
		
		return parser;
	}
	
	private String get_conf_url(String[] args) {
		for(int i = 0; i < args.length - 1; i ++) {
			if (args[i].equals("--mos-url")) {
				return args[i+1];
			}
		}
		return System.getenv("MOS_URL");
	}
	
	private void init_subcmd_parser(Subparsers subparsers, Object obj, Method func) {
		String subcmd_name = func.getName().substring(3);
		// System.out.println("Init subcommand: " + subcmd_name);
		Subparser subparser = subparsers.addParser(subcmd_name)
				.setDefault("__invoke_func__", func)
				.setDefault("__invoke_object__", obj);
		subcmd_tbl.put(subcmd_name, subparser);
		CommandHelp helpanno = func.getAnnotation(CommandHelp.class);
		if (helpanno != null) {
			subparser.help(helpanno.help());
		}
		CommandOptions optsanno = func.getAnnotation(CommandOptions.class);
		if (optsanno != null) {
			CommandOption[] opt_list = optsanno.options();
			for(int i = 0; i < opt_list.length; i ++) {
				Argument arg = subparser.addArgument(opt_list[i].name());
				String action = opt_list[i].action();
				Class type = opt_list[i].type();
				String help = opt_list[i].help();
				String metavar = opt_list[i].metavar();
				if (help != null) {
					arg.help(help);
				}
				if (metavar != null && metavar.length() > 0) {
					arg.metavar(metavar);
				}
				if (action != null && action.length() > 0) {
					if (action.equalsIgnoreCase("store_true")) {
						arg.action(Arguments.storeTrue());
					}else if (action.equalsIgnoreCase("append")) {
						arg.action(Arguments.append());
					}
				}
				arg.type(type);
			}
		}
	}
	
	@CommandHelp(help="Show help")
	@CommandOptions(options={
			@CommandOption(name="command", metavar="<SUBCOMMAND>", help="subcommand")
	})
	public void do_help(Namespace cmd) {
		String subcmd = Utils.getArgString(cmd, "command", "");
		if (subcmd_tbl.containsKey(subcmd)) {
			subcmd_tbl.get(subcmd).printHelp();
		}else {
			parser.printHelp();
		}
	}
	
	private int exec(String[] args) {
		String key = null;
		String secret = null;
		String url = null;
		String region = null;
		String format = null;
		int timeout = 60;
		boolean debug = false;

		parser = init_cmd_parser();
		
		String conf_url = get_conf_url(args);
		if (conf_url == null) {
			System.err.println("Missing --mos-url or env[MOS_URL]");
			return 1;
		}
		url = conf_url;
		if (debug) {
			System.out.println("MOS_URL: " + url);
		}

		ShellInterface shell_obj = getShell(url);

		Subparsers subparsers = parser.addSubparsers().title("subcommands")
				.description("valid subcommands").help("additional help")
	            .metavar("COMMAND");

		subcmd_tbl = new HashMap<String, Subparser>();
		
		Method func;
		try {
			func = getClass().getMethod("do_help", Namespace.class);
			init_subcmd_parser(subparsers, this, func);
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		
		Method[] methods = shell_obj.getClass().getMethods();
		for(int i = 0; i < methods.length; i ++) {
			func = methods[i];
			if (func.getName().startsWith("do_")) {
				init_subcmd_parser(subparsers, shell_obj, func);
			}
		}

		Namespace cmds = null;
		Method invoke_func = null;
		Object invoke_obj = null;
		
		try {
			cmds = parser.parseArgs(args);
			key = cmds.getString("mos_access");
			secret = cmds.getString("mos_secret");
			region = cmds.getString("mos_region");
			invoke_func = cmds.get("__invoke_func__");
			invoke_obj = cmds.get("__invoke_object__");
			if (key == null) {
				throw new Exception("Missing --mos-access or env[MOS_ACCESS]");
			}
			if (secret == null) {
				throw new Exception("Missing --mos-secret or env[MOS_SECRET");
			}
			if (region == null) {
				throw new Exception("Missing --mos-region or env[MOS_REGION]");
			}
			if (invoke_func == null) {
				throw new Exception("Missing subcommand");
			}
		}catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
			for(String arg: args) {
				if (subcmd_tbl.containsKey(arg)) {
					subcmd_tbl.get(arg).printHelp();
					return 1;
				}
			}
			parser.printHelp();
			return 1;
		}
		
		format = Utils.getArgString(cmds, "format", null);
		timeout = Utils.getArgInt(cmds, "timeout", 60);
		debug = Utils.getArgBoolean(cmds, "debug");
		
		try {
			shell_obj.initClient(key, secret, url, region, format, timeout, debug);
			invoke_func.invoke(invoke_obj, cmds);
			return 0;
		}catch(InvocationTargetException ite) {
			Throwable fe = ite.getTargetException();
			if (fe instanceof ServerErrorException) {
				System.err.println(fe.getMessage());
			}else {
				fe.printStackTrace();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return 1;
	}
	
	private static ShellInterface getShell(String url) {
		try {
			String ver = url.substring(url.lastIndexOf('/') + 1);
			String cls_name = "com.meituan.mos.shell." + ver + ".Shell";
			Class shell_cls = Class.forName(cls_name);
			return (ShellInterface)shell_cls.newInstance();
		}catch(Exception e) {
			System.err.println("Failed: " + e.getMessage());
			System.exit(1);
		}
		return null;
	}
	
	public static void main(String[] args) {
		Shell shell = new Shell();
		System.exit(shell.exec(args));
	}

}
