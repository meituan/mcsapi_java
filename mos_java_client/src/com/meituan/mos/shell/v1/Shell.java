package com.meituan.mos.shell.v1;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.argparse4j.inf.Namespace;

import org.json.JSONObject;

import com.meituan.mos.shell.common.CommandHelp;
import com.meituan.mos.shell.common.CommandOptions;
import com.meituan.mos.shell.common.CommandOption;
import com.meituan.mos.shell.common.ShellInterface;
import com.meituan.mos.shell.common.Utils;
import com.meituan.mos.sdk.v1.Client;

public class Shell implements ShellInterface {
	Client client = null;
	
	public void initClient(String key, String secret, String url, String region,
			String format, int timeout, boolean debug) {
		client = new Client(key, secret, url, region, format, timeout, debug);
	}
	
	@CommandHelp(help="List instance types")
	@CommandOptions(options = {
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Page limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Page offset"),
			@CommandOption(name="--filter", metavar="<FILTER>", action="append", help="List filters, in the form of name:value")
	})
	public void do_DescribeInstanceTypes(Namespace cmd) throws Exception {
		int limit = Utils.getArgInt(cmd, "limit", 0);
		int offset = Utils.getArgInt(cmd, "offset", 0);
		Map<String, List<String>> filters = Utils.getArgFilters(cmd);
		JSONObject result = client.DescribeInstanceTypes(limit, offset, filters);
		Utils.printList(result, "InstanceType", null);
	}
	
	@CommandHelp(help="List all image templates")
	public void do_DescribeTemplates(Namespace cmd) throws Exception {
		JSONObject result = client.DescribeTemplates();
		Utils.printList(result, "Template", null);
	}
	
	@CommandHelp(help="Get balance")
	public void do_GetBalance(Namespace args) throws Exception {
		JSONObject result = client.GetBalance();
		Utils.printDict(result);
	}

	@CommandHelp(help="Renew an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--duration", metavar="<DURATION>", help="Renew instance duration, in H or M, eg 72H, 1M")
	})
	public void do_RenewInstance(Namespace args) throws Exception {
		String iid = Utils.getArgString(args, "id", null);
		String duration = Utils.getArgString(args, "duration", null);
		client.RenewInstance(iid, duration);
	}
	
	@CommandHelp(help="Query instance contract information")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance")
	})
	public void do_GetInstanceContractInfo(Namespace args) throws Exception {
		String iid = Utils.getArgString(args, "id", null);
		JSONObject result = client.GetInstanceContractInfo(iid);
		Utils.printDict(result);
	}
	
	@CommandHelp(help="Create servers")
	@CommandOptions(options={
			@CommandOption(name="image", metavar="<IMAGE>", help="ID of image template"),
			@CommandOption(name="instance_type", metavar="<INSTANCE_TYPE>", help="Instance type"),
			@CommandOption(name="--duration", metavar="<DURATION>", help="Reserved instance duration, in H or M, e.g. 72H, 1M"),
			@CommandOption(name="--name", metavar="<NAME>", help="Optional instance name"),
			@CommandOption(name="--keypair", metavar="<KEYPAIR>", help="SSH key pair name"),
			@CommandOption(name="--datadisk", metavar="<DISKSIZE>", type=Integer.class, help="Extra disksize in GB!!"),
			@CommandOption(name="--bandwidth", metavar="<BANDWIDTH>", type=Integer.class, help="Extra external bandwidth in Mbps"),
			@CommandOption(name="--zone", metavar="<ZONE>", help="Optional availability zone"),
	})
	public void do_CreateInstance(Namespace args) throws Exception {
		JSONObject val = client.CreateInstance(Utils.getArgString(args, "image", null),
				Utils.getArgString(args, "instance_type", null),
				Utils.getArgString(args, "keypair", null),
				Utils.getArgInt(args, "datadisk", 0),
				Utils.getArgInt(args, "bandwidth", 0),
				null,
				Utils.getArgString(args, "duration", null),
				Utils.getArgString(args, "name", null),
				Utils.getArgString(args, "zone", null)
				);
		Utils.printDict(val);
	}
	
	@CommandHelp(help="Get status of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance")
	})
	public void do_DescribeInstanceStatus(Namespace args) throws Exception {
		JSONObject val = client.DescribeInstanceStatus(Utils.getArgString(args, "id", null));
		Utils.printDict(val);
	}

	@CommandHelp(help="Get initial password of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--key-file", metavar="<PRIVATE_KEY>", help="Private key file to decrypt password"),
			@CommandOption(name="--key-pass", metavar="<KEY_FILE_PASSPHRASE>", help="Passphrase to open private key file")
	})
	public void do_GetPasswordData(Namespace args) throws Exception {
		JSONObject val = client.GetPasswordData(Utils.getArgString(args, "id", null),
												Utils.getArgString(args, "key_file", null),
												Utils.getArgString(args, "key_pass", null));
		Utils.printDict(val);
	}

	@CommandHelp(help="Get details of all or specified instances")
	@CommandOptions(options={
			@CommandOption(name="--id", metavar="<ID>", action="append", help="ID of instance"),
			@CommandOption(name="--name", metavar="<NAME>", action="append", help="Name of instance"),
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Offset"),
			@CommandOption(name="--filter", metavar="<FILTER>", action="append", help="Filter")
	})
	public void do_DescribeInstances(Namespace args) throws Exception {
		JSONObject val = client.DescribeInstances(
				Utils.getArgStringArray(args, "id"),
				Utils.getArgStringArray(args, "name"),
				Utils.getArgInt(args, "limit", 0), 
				Utils.getArgInt(args, "offset", 0),
				Utils.getArgFilters(args));
		Utils.printList(val, "Instance", null);
	}
	
	@CommandHelp(help="List all disks of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Offset"),
			@CommandOption(name="--filter", metavar="<FILTER>", action="append", help="Filter")
	})
	public void do_DescribeInstanceVolumes(Namespace args) throws Exception {
		JSONObject val = client.DescribeInstanceVolumes(
				Utils.getArgString(args, "id", null),
				Utils.getArgInt(args, "limit", 0),
				Utils.getArgInt(args, "offset", 0),
				Utils.getArgFilters(args));
		Utils.printList(val, "InstanceVolume", null);	    
	}

	@CommandHelp(help="List all network interfaces of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Offset"),
			@CommandOption(name="--filter", metavar="<FILTER>", action="append", help="Filter")
	})
	public void do_DescribeInstanceNetworkInterfaces(Namespace args) throws Exception {
		JSONObject val = client.DescribeInstanceNetworkInterfaces(
				Utils.getArgString(args, "id", null),
				Utils.getArgInt(args, "limit", 0),
				Utils.getArgInt(args, "offset", 0),
				Utils.getArgFilters(args));
		Utils.printList(val, "InstanceNetworkInterface", null);
	}

	@CommandHelp(help="Start an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance")
	})
	public void do_StartInstance(Namespace args) throws Exception {
		client.StartInstance(Utils.getArgString(args, "id", null));
	}

	@CommandHelp(help="Stop an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--force", action="store_true", help="Force stop running isntance")
	})
	public void do_StopInstance(Namespace args) throws Exception {
		client.StopInstance(Utils.getArgString(args, "id", null),
				Utils.getArgBoolean(args, "force"));
	}

	@CommandHelp(help="Reboot an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance")
	})
	public void do_RebootInstance(Namespace args) throws Exception {
		client.RebootInstance(Utils.getArgString(args, "id", null));
	}
	
	@CommandHelp(help="Terminate an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance")
	})
	public void do_TerminateInstance(Namespace args) throws Exception {
		client.TerminateInstance(Utils.getArgString(args, "id", null));
	}
	
	@CommandHelp(help="Rebuild root image of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--image", metavar="<IMAGE>", help="ID of root image template")
	})
	public void do_RebuildInstanceRootImage(Namespace args) throws Exception {
		client.RebuildInstanceRootImage(Utils.getArgString(args, "id", null),
				Utils.getArgString(args, "image", null));
	}

	@CommandHelp(help="Change instance type")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="instance_type", metavar="<INSTANCE_TYPE>", help="Instance type"),
			@CommandOption(name="--duration", metavar="<DURATION>", help="Reserved instance duration, in H or M, e.g. 72H, 1M"),
			@CommandOption(name="datadisk", metavar="<DISKSIZE>", type=Integer.class, help="Extra disksize in GB!!"),
			@CommandOption(name="bandwidth", metavar="<BANDWIDTH>", type=Integer.class, help="Extra external bandwidth in Mbps")
	})
	public void do_ChangeInstanceType(Namespace args) throws Exception {
		client.ChangeInstanceType(Utils.getArgString(args, "id", null),
				Utils.getArgString(args, "instance_type", null),
				Utils.getArgString(args, "duration", null),
				Utils.getArgInt(args, "datadisk", 0),
				Utils.getArgInt(args, "bandwidth", 0));
	}

	@CommandHelp(help="Get metadata of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance")
	})
	public void do_GetInstanceMetadata(Namespace args) throws Exception {
		JSONObject val = client.GetInstanceMetadata(Utils.getArgString(args, "id", null));
		Utils.printDict(val);
	}

	@CommandHelp(help="Put metadata of an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--data", metavar="<KEY:VALUE>", action="append", help="Key and value pair, separated by coma:")
	})
	public void do_PutInstanceMetadata(Namespace args) throws Exception {
		String[] data = Utils.getArgStringArray(args, "data");
		Map<String, String> dataval = null;
		if (data != null) {
			dataval = new HashMap<String, String>();
			for(String d: data) {
				int colon_pos = d.indexOf(':');
				if (colon_pos > 0) {
					dataval.put(d.substring(0, colon_pos), d.substring(colon_pos+1));
				}else {
					dataval.put(d, "");
				}
			}
			if (dataval.size() == 0) {
				throw new Exception("No data to put");
			}
		}
		client.PutInstanceMetadata(Utils.getArgString(args, "id", null), dataval);
	}

	@CommandHelp(help="List all keypairs")
	@CommandOptions(options={
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Offset"),
			@CommandOption(name="--filter", metavar="<FILTER>", action="append", help="Filter")
	})
	public void do_DescribeKeyPairs(Namespace args) throws Exception {
		JSONObject val = client.DescribeKeyPairs(Utils.getArgInt(args, "limit", 0),
				Utils.getArgInt(args, "offset", 0),
				Utils.getArgFilters(args));
		Utils.printList(val, "KeyPair", null);
	}

	@CommandHelp(help="Import SSH keypairs")
	@CommandOptions(options={
			@CommandOption(name="name", metavar="<NAME>", help="Name of keypair"),
			@CommandOption(name="key-file", metavar="<PUBLIC_KEY_FILE>", help="Public key file path")
	})
	public void do_ImportKeyPair(Namespace args) throws Exception {
		String keyfile = Utils.getArgString(args, "key_file", null);
		String pubkey = Utils.fileGetContent(keyfile);
		JSONObject val = client.ImportKeyPair(Utils.getArgString(args, "name", null), pubkey);
		Utils.printDict(val);
	}  

	@CommandHelp(help="Delete a keypair")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of keypair")
	})
	public void do_DeleteKeyPair(Namespace args) throws Exception {
		client.DeleteKeyPair(Utils.getArgString(args, "id", null));
	}  

	@CommandHelp(help="Save root disk to new image and upload to glance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<INSTANCE_ID>", help="ID of instance"),
			@CommandOption(name="name", metavar="<TEMPLATE_NAME>", help="Name of template"),
			@CommandOption(name="--notes", metavar="<NOTES>", help="Template Notes")
	})
	public void do_CreateTemplate(Namespace args) throws Exception {
		client.CreateTemplate(Utils.getArgString(args, "id", null),
				Utils.getArgString(args, "name", null),
				Utils.getArgString(args, "notes", null));
	}

	@CommandHelp(help="Delete a template")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of template")
	})
	public void do_DeleteTemplate(Namespace args) throws Exception {
		client.DeleteTemplate(Utils.getArgString(args, "id", null));
	}
	
	@CommandHelp(help="Get details of all or specified instance snapshots")
	@CommandOptions(options={
			@CommandOption(name="--id", metavar="<ID>", action="append", help="ID of snapshots"),
			@CommandOption(name="--timestamp", metavar="<TIMESTAMP>", action="append", help="Timestamp of snapshots"),
			@CommandOption(name="--instance-id", metavar="<INSTANCEID>", action="append", help="ID of intances of snapshots"),
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Offset"),
			@CommandOption(name="--filter", metavar="<FILTER>", action="append", help="Filter")
	})
	public void do_DescribeSnapshots(Namespace args) throws Exception {
		JSONObject val = client.DescribeSnapshots(
				Utils.getArgStringArray(args, "id"),
				Utils.getArgStringArray(args, "timestamp"),
				Utils.getArgStringArray(args, "instance_id"),
				Utils.getArgInt(args, "limit", 0), 
				Utils.getArgInt(args, "offset", 0),
				Utils.getArgFilters(args));
		Utils.printList(val, "Snapshot", null);
	}
	
	@CommandHelp(help="Create a snapshot for an instance")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="--name", metavar="<NAME>", help="Name of snapshot, optional")
	})
	public void do_CreateSnapshot(Namespace args) throws Exception {
		client.CreateSnapshot(Utils.getArgString(args, "id", null),
				Utils.getArgString(args, "name", null));
	}
	
	
	@CommandHelp(help="Delete a snapshot")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
	})
	public void do_DeleteSnapshot(Namespace args) throws Exception {
		client.DeleteSnapshot(Utils.getArgString(args, "id", null));
	}
	
	
	@CommandHelp(help="Restore an instance to a snapshot")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of instance"),
			@CommandOption(name="snapshotid", metavar="<SNAPSHOTID>", help="ID of snapshot"),
	})
	public void do_RestoreSnapshot(Namespace args) throws Exception {
		client.RestoreSnapshot(Utils.getArgString(args, "id", null),
				Utils.getArgString(args, "snapshotid", null));
	}
	
	
	@CommandHelp(help="Create an instance from a snapshot")
	@CommandOptions(options={
			@CommandOption(name="id", metavar="<ID>", help="ID of snapshot"),
			@CommandOption(name="--duration", metavar="<DURATION>", help="Reserved instance duration, in H or M, e.g. 72H, 1M"),
			@CommandOption(name="--name", metavar="<NAME>", help="Optional instance name"),
			@CommandOption(name="--zone", metavar="<ZONE>", help="Optional availability zone"),
	})
	public void do_CreateInstanceFromSnapshot(Namespace args) throws Exception {
		JSONObject val = client.CreateInstance(null, null,
				null, 0, 0,
				Utils.getArgString(args, "id", null),
				Utils.getArgString(args, "duration", null),
				Utils.getArgString(args, "name", null),
				Utils.getArgString(args, "zone", null));
		Utils.printDict(val);
	}
	
	
	@CommandHelp(help="Get details of all zones")
	@CommandOptions(options={
			@CommandOption(name="--limit", metavar="<LIMIT>", type=Integer.class, help="Limit"),
			@CommandOption(name="--offset", metavar="<OFFSET>", type=Integer.class, help="Offset"),
	})
	public void do_DescribeAvailabilityZones(Namespace args) throws Exception {
		JSONObject val = client.DescribeAvailabilityZones(
				Utils.getArgInt(args, "limit", 0), 
				Utils.getArgInt(args, "offset", 0));
		Utils.printList(val, "AvailabilityZone", null);
	}
}
