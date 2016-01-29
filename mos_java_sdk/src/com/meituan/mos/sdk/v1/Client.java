package com.meituan.mos.sdk.v1;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.meituan.mos.sdk.common.BaseClient;
import com.meituan.mos.sdk.common.Utils;

public class Client extends BaseClient {

	/**
	 * v1 API Client 构造函数
	 *
	 * @param key String MOS ACCESS KEY
	 * @param secret String MOS ACCESS Secret
	 * @param url String MOS API URL
	 * @param region String 访问的区域名称，目前只有Beijing一个区域
	 * @param format String 期望的数据格式，可以为null
	 * @param timeout int 每次请求的超时时间，单位为秒
	 * @param debug boolean 是否开启debug模式
	 */
	public Client(String key, String secret, String url, String region,
			String format, int timeout, boolean debug) {
		super(key, secret, url, region, format, timeout, debug);
	}

	/**
	 * 获取所有虚拟机类型
	 *
	 * @param limit int 最大返回数量，用于分页控制
	 * @param offset int 返回偏移量，用于分页控制
	 * @param filters Map<String, List<String>> 过滤条件，key/value分别指定过滤字段名称和值，支持的字段名称为：name, status
	 * @return JSONObject 包含系统支持的虚拟机类型列表
	 * @throws Exception
	 */
	public JSONObject DescribeInstanceTypes(int limit, int offset, Map<String, List<String>> filters) throws Exception {
        JSONObject kwargs = new JSONObject();
        parse_list_params(limit, offset, filters, kwargs);
        JSONObject result = Request("DescribeInstanceTypes", kwargs);
        return Utils.getJSONResult(result, "InstanceTypeSet");
	}

	/**
	 * 获得所有虚拟机模板
	 *
	 * @return 获得所有虚拟机模板
	 * @throws Exception
	 */
	public JSONObject DescribeTemplates() throws Exception {
		JSONObject result = Request("DescribeTemplates", null);
		return Utils.getJSONResult(result, "TemplateSet");
	}

	/**
	 * 获取帐户余额
	 *
	 * @return 帐户余额和最近更新时间
	 * @throws Exception
	 */
	public JSONObject GetBalance() throws Exception {
		JSONObject result = Request("GetBalance", null);
		return result;
	}

	/**
	 * 获得所有虚拟机
	 *
	 * @param ids String[] 期望获取的虚拟机ID列表
	 * @param names String[] 期望获取信息的虚拟机名称列表
	 * @param limit int 最多返回数量
	 * @param offset int 返回虚拟机的偏移量，用于分页显示
	 * @param filters Map<String, List<String>> 过滤器，一个dict，包含过滤字段名和值，可能过滤字段为：name, status
	 * @return JSONObject 包含虚拟机列表
	 * @throws Exception
	 */
	public JSONObject DescribeInstances(String[] ids, String[] names, int limit, int offset, Map<String, List<String>> filters) throws Exception {
		JSONObject kwargs = new JSONObject();
		if (ids != null && ids.length > 0) {
			kwargs.put("InstanceId", Utils.stringArray2JSONArray(ids));
		}
		if (names != null && names.length > 0) {
			kwargs.put("InstanceName", Utils.stringArray2JSONArray(names));
		}
		parse_list_params(limit, offset, filters, kwargs);
		JSONObject result = Request("DescribeInstances", kwargs);
		return Utils.getJSONResult(result, "InstanceSet");
	}

	/**
	 * 获取指定虚拟机的虚拟磁盘信息
	 *
	 * @param iid String 虚拟机ID
	 * @param limit int  最大返回数量，用于分页控制
	 * @param offset int 返回的偏移量，用于分页控制
	 * @param filters Map<String, List<String>> 返回结果过滤条件，由dict的key/value指定过滤字段名和值
	 * @return JSONObject InstanceVolumeSet，包含虚拟机磁盘列表
	 * @throws Exception
	 */
	public JSONObject DescribeInstanceVolumes(String iid, int limit, int offset, Map<String, List<String>> filters) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		parse_list_params(limit, offset, filters, kwargs);
		JSONObject result = Request("DescribeInstanceVolumes", kwargs);
		return Utils.getJSONResult(result, "InstanceVolumeSet");
	}

	/**
	 * 获取指定虚拟机的网络接口（虚拟网卡）信息
	 *
	 * @param iid String 虚拟机ID
	 * @param limit int 最大返回数量，用于分页控制
	 * @param offset int 返回的偏移量，用于分页控制
	 * @param filters Map<String, List<String>>  返回结果过滤条件，由dict的key/value指定过滤字段名和值
	 * @return JSONObject  InstanceNetworkInterfaceSet，包含虚拟机网络接口列表
	 * @throws Exception
	 */
	public JSONObject DescribeInstanceNetworkInterfaces(String iid, int limit, int offset, Map<String, List<String>> filters) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		parse_list_params(limit, offset, filters, kwargs);
		JSONObject result = Request("DescribeInstanceNetworkInterfaces", kwargs);
		return Utils.getJSONResult(result, "InstanceNetworkInterfaceSet");
	}

	/**
	 * 虚拟机租期续费
	 *
	 * @param iid String 虚拟机ID
	 * @param duration String 续费租期，单位为H(小时)或M(月), 缺省为'1M'，即一个月
	 * @throws Exception
	 */
	public void RenewInstance(String iid, String duration) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		if (duration != null) {
			if (Utils.matchDuration(duration)) {
				kwargs.put("Duration", duration);
			}else {
				throw new Exception("Illegal duration " + duration);
			}
		}
		Request("RenewInstance", kwargs);
	}

	/**
	 * 获取虚拟机的租期信息
	 *
	 * @param iid String 虚拟机ID
	 * @return 虚拟机租期信息，包含过期时间、自动删除时间
	 * @throws Exception
	 */
	public JSONObject GetInstanceContractInfo(String iid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		return Request("GetInstanceContractInfo", kwargs);
	}

	/**
	 * 创建虚拟机
	 *
	 * @param imageid String 系统模板ID
	 * @param itype String 虚拟机类型ID
	 * @param keypair String 虚拟机使用的SSH密钥ID
	 * @param datadisk_gb int  指定创建虚拟机使用的额外数据盘，单位为GB
	 * @param bandwidth_mbps int 指定创建虚拟机使用的额外带宽，单位为Mbps
	 * @param snapshotid String 创建虚拟机的虚拟机快照的ID
	 * @param duration String 虚拟机租期, 缺省为'1M'，即一个月
	 * @param name String 虚拟机名称(可选)
	 * @param zone String 虚拟机所在可用域(可选)
	 * @return 创建成功的虚拟机信息
	 * @throws Exception
	 */
	public JSONObject CreateInstance(String imageid, String itype,
			String keypair, int datadisk_gb, int bandwidth_mbps,
			String snapshotid,
			String duration, String name, String zone) throws Exception {
		JSONObject kwargs = new JSONObject();
		if (snapshotid != null) {
			kwargs.put("SnapshotId", snapshotid);
		}else if(imageid != null && itype != null) {
			kwargs.put("ImageId", imageid);
			kwargs.put("InstanceType", itype);
			if (keypair != null) {
				kwargs.put("KeyName", keypair);
			}
			if (datadisk_gb > 0) {
				kwargs.put("ExtraExtDisksize", datadisk_gb);
			}
			if (bandwidth_mbps > 0) {
				kwargs.put("ExtraExtBandwidth", bandwidth_mbps);
			}
		}else {
			throw new Exception("Not enough parameter to create instance");
		}
		if (duration != null) {
			if (Utils.matchDuration(duration)) {
				kwargs.put("Duration", duration);
			}else {
				throw new Exception("Illegal duration format " + duration);
			}
		}
		if (name != null) {
			kwargs.put("InstanceName", name);
		}
		if (zone != null) {
			kwargs.put("AvailabilityZoneId", zone);
		}
		JSONObject result = Request("CreateInstance", kwargs);
		return Utils.getJSONResult(result, "Instance");
	}

	/**
	 * 获取虚拟机的状态
	 *
	 * @param iid String 虚拟机ID
	 * @return 虚拟机状态字符串
	 * @throws Exception
	 */
	public JSONObject DescribeInstanceStatus(String iid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		JSONObject result = Request("DescribeInstanceStatus", kwargs);
		return Utils.getJSONResult(result, "InstanceStatus");
	}

	/**
	 * 获取虚拟机的Login帐户信息
	 *
	 * @param iid String 虚拟机ID
	 * @param key_file String 私钥文件路径，路过虚拟机使用了SSH密钥，需要指定私钥解密password
	 * @return 虚拟机Login信息，包含帐户名称、密码，如果使用SSH密钥，则还包含密钥ID和名称
	 * @throws Exception
	 */
	public JSONObject GetPasswordData(String iid, String key_file, String key_pass) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		JSONObject result = Request("GetPasswordData", kwargs);
		if (result.has("passwordData") && result.has("keypairName")) {
			if (key_file == null) {
				throw new Exception("Password is encrypted, please speecify " +
                        "private key of keypair " + result.getString("keypairName"));
			}else {
				String dec_pass = Utils.decrypt_password(key_file, key_pass, result.getString("passwordData"));
				result.put("passwordData", dec_pass);
			}
		}
		return result;
	}

	/**
	 * 启动虚拟机
	 *
	 * @param iid String 虚拟机ID
	 * @throws Exception
	 */
	public void StartInstance(String iid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		Request("StartInstance", kwargs);
	}

	/**
	 * 停止虚拟机
	 *
	 * @param iid String 虚拟机ID
	 * @param force boolean 是否强制停止虚拟机
	 * @throws Exception
	 */
	public void StopInstance(String iid, boolean force) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		if (force) {
			kwargs.put("Force", force);
		}
		Request("StopInstance", kwargs);
	}

	/**
	 * 重启虚拟机
	 *
	 * @param iid String 虚拟机ID
	 * @throws Exception
	 */
	public void RebootInstance(String iid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		Request("RebootInstance", kwargs);
	}

	/**
	 * 删除虚拟机
	 * @param iid String 虚拟机ID
	 * @throws Exception
	 */
	public void TerminateInstance(String iid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		Request("TerminateInstance", kwargs);
	}

	/**
	 * 重置虚拟机系统磁盘
	 *
	 * @param iid String 虚拟机ID
	 * @param image_id String 将虚拟机系统磁盘用指定镜像模板重置，如果无该参数，则使用原镜像模板重置
	 * @throws Exception
	 */
	public void RebuildInstanceRootImage(String iid, String image_id) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		if (image_id != null && image_id.length() > 0) {
			kwargs.put("ImageId", image_id);
		}
		Request("RebuildInstanceRootImage", kwargs);
	}

	/**
	 * 更改虚拟机类型
	 *
	 * @param iid String 虚拟机ID
	 * @param itype String 指定更改的虚拟机类型
	 * @param duration String 指定更改后的初始租期，缺省为'1M'，即一个月
	 * @param datadisk_gb int 指定创建虚拟机使用的额外数据盘，单位为GB
	 * @param bandwidth_mbps int 指定创建虚拟机使用的额外带宽，单位为Mbps
	 * @throws Exception
	 */
	public void ChangeInstanceType(String iid, String itype, String duration,
                            int datadisk_gb, int bandwidth_mbps) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		kwargs.put("InstanceType", itype);
		if (duration != null) {
			if (Utils.matchDuration(duration)) {
				kwargs.put("Duration", duration);
			}else {
				throw new Exception("IIlegal duration format " + duration);
			}
		}
		kwargs.put("ExtraExtDisksize", datadisk_gb);
		kwargs.put("ExtraExtBandwidth", bandwidth_mbps);
		Request("ChangeInstanceType", kwargs);
	}

	/**
	 * 获取虚拟机的metadata
	 *
	 * @param iid String 虚拟机ID
	 * @return JSONObject 一个dict包含虚拟机所有metadata的key/value
	 * @throws Exception
	 */
	public JSONObject GetInstanceMetadata(String iid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		JSONObject result = Request("GetInstanceMetadata", kwargs);
		return Utils.getJSONResult(result, "InstanceMetadata");
	}

	/**
	 * 修改虚拟机的metadata
	 *
	 * @param iid String 虚拟机ID
	 * @param data Map<String, String> 需要增加或修改的metadata信息
	 * @throws Exception
	 */
	public void PutInstanceMetadata(String iid, Map<String, String> data) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		JSONArray names = new JSONArray();
		JSONArray values = new JSONArray();
		for (String key: data.keySet()) {
			names.put(key);
			values.put(data.get(key));
		}
		kwargs.put("Name", names);
		kwargs.put("Value", values);
		Request("PutInstanceMetadata", kwargs);
	}

	/**
	 * 获取用户的SSH密钥对
	 *
	 * @param limit int 最大返回数量，用于分页控制
	 * @param offset int 返回偏移量，用于分页控制
	 * @param filters Map<String, List<String>> 过滤条件，key/value分别指定过滤字段名称和值，支持的字段名称为：name
	 * @return JSONObject KeyPairSet, 包含SSH密钥对列表
	 * @throws Exception
	 */
	public JSONObject DescribeKeyPairs(int limit, int offset, Map<String, List<String>> filters) throws Exception {
		JSONObject kwargs = new JSONObject();
		parse_list_params(limit, offset, filters, kwargs);
		JSONObject result = Request("DescribeKeyPairs", kwargs);
		return Utils.getJSONResult(result, "KeyPairSet");
	}

	/**
	 * 导入一个用户的SSH公钥，并创建一个SSH密钥对
	 *
	 * @param name String 密钥对名称
	 * @param pubkey String SSH公钥信息
	 * @return JSONObject  创建的SSH密钥对信息
	 * @throws Exception
	 */
	public JSONObject ImportKeyPair(String name, String pubkey) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("KeyName", name);
		kwargs.put("PublicKeyMaterial", pubkey);
		JSONObject result = Request("ImportKeyPair", kwargs);
		return Utils.getJSONResult(result, "KeyPair");
	}

	/**
	 * 删除一个SSH密钥对
	 *
	 * @param kid String 密钥对ID
	 * @throws Exception
	 */
	public void DeleteKeyPair(String kid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("KeyName", kid);
		Request("DeleteKeyPair", kwargs);
	}

	/**
	 * 保存虚拟机的模板
	 *
	 * @param iid String 虚拟机ID
	 * @param name String 模板名称
	 * @param notes String 保存模板的说明
	 * @throws Exception
	 */
    public void CreateTemplate(String iid, String name, String notes) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("InstanceId", iid);
    	kwargs.put("Name", name);
    	if (notes != null && notes.length() > 0) {
    		kwargs.put("Notes", notes);
    	}
    	Request("CreateTemplate", kwargs);
    }

    /**
     * 删除一个模板
     *
     * @param tid String 模板ID
     * @throws Exception
     */
    public void DeleteTemplate(String tid) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("TemplateId", tid);
    	Request("DeleteTemplate", kwargs);
    }


    /**
     * 列出所有的虚拟机快照
     *
     * @param ids String[] 列出指定ID范围内的快照
     * @param timestamps String[] 列出指定时间戳的快照
     * @param instanceIds String[] 列出指定ID范围内的虚拟机的快照
     * @param limit int 最多返回数量
     * @param offset int 返回虚拟机快照的偏移量，用于分页显示
     * @param filters Map<String, List<String>> 过滤器，一个dict，包含过滤字段名和值，可能过滤字段为：name, status
     * @return JSONObject 包含虚拟机快照的列表
     * @throws Exception
     */
    public JSONObject DescribeSnapshots(String[] ids, String[] timestamps, String[] instanceIds, int limit, int offset, Map<String, List<String>> filters) throws Exception {
		JSONObject kwargs = new JSONObject();
		if (ids != null && ids.length > 0) {
			kwargs.put("SnapshotId", Utils.stringArray2JSONArray(ids));
		}
		if (timestamps != null && timestamps.length > 0) {
			kwargs.put("SnapshotTimestamp", Utils.stringArray2JSONArray(timestamps));
		}
		if (instanceIds != null && instanceIds.length > 0) {
			kwargs.put("InstanceId", Utils.stringArray2JSONArray(instanceIds));
		}
		parse_list_params(limit, offset, filters, kwargs);
		JSONObject result = Request("DescribeSnapshots", kwargs);
		return Utils.getJSONResult(result, "SnapshotSet");
	}

    /**
     * 为指定虚拟机创建一个快照
     *
     * @param iid String 要创建快照的虚拟机ID
     * @param name String 快照名称（可选)
     * @throws Exception
     */
    public void CreateSnapshot(String iid, String name) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		if (name != null && name.length() > 0) {
			kwargs.put("SnapshotName", name);
		}
		Request("CreateSnapshot", kwargs);
	}

    /**
     * 删除指定虚拟机快照
     *
     * @param sid String 虚拟机快照的ID
     * @throws Exception
     */
    public void DeleteSnapshot(String sid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("SnapshotId", sid);
		Request("DeleteSnapshot", kwargs);
	}

    /**
     * 将一台虚拟机重置为指定虚拟机快照的内容
     *
     * @param iid String 虚拟机ID
     * @param sid String 虚拟机快照的ID
     * @throws Exception
     */
    public void RestoreSnapshot(String iid, String sid) throws Exception {
		JSONObject kwargs = new JSONObject();
		kwargs.put("InstanceId", iid);
		kwargs.put("SnapshotId", sid);
		Request("RestoreSnapshot", kwargs);
	}

    /**
     * 获得当前区域中所有的可用域
     *
     * @param limit int 最多返回数量
     * @param offset int 返回可用域的偏移量，用于分页显示
     * @return JSONObject 可用域列表
     * @throws Exception
     */
    public JSONObject DescribeAvailabilityZones(int limit, int offset) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	parse_list_params(limit, offset, null, kwargs);
		JSONObject result = Request("DescribeAvailabilityZones", kwargs);
		return result.getJSONObject("AvailabilityZoneSet");
    }


    /**
     *	浮动IP相关接口
     */

    /**
     *	创建浮动IP
     *
     *	@param name
     *  @param billing_model string 代表计费方式，有效值：bandwidth，flow，分别代表按带宽和按流量计费。默认为bandwidth
     *  @param availability_zone_id string 代表可用区ID, 通过DescribeAvailabilityZones接口获取
     *  @return Address结构
     */
    public JSONObject AllocateAddress(String name, String billing_model, String availability_zone_id) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("Name", name);
    	if (billing_model != null){
    		billing_model = "bandwidth";
    	}
    	kwargs.put("BillingModel", billing_model);
    	if (availability_zone_id != null){
    		kwargs.put("AvailabilityZoneId", availability_zone_id);
    	}

		JSONObject result = Request("AllocateAddress", kwargs);
		return result.getJSONObject("Address");
    }

    /**
     * 返回所有或者部分浮动IP列表信息列表
     *
     *	@param allocation_ids string[] 希望获取的Address ID列表
     *  @param limit int 返回的数量限制，用于分页控制
     *  @param offset int 返回的偏移量，用于分页控制
     *  @param filters Map<String, List<String>> 过滤器，一个dict，包含过滤字段名和值
     *  @return AddressSet, 包含Address列表
     */
    public JSONObject DescribeAddresses(String[] allocation_ids, int limit, int offset, Map<String, List<String>> filters) throws Exception {
    	JSONObject kwargs = new JSONObject();
		if (allocation_ids != null && allocation_ids.length > 0) {
			kwargs.put("AllocationId", Utils.stringArray2JSONArray(allocation_ids));
		}
		parse_list_params(limit, offset, filters, kwargs);
		JSONObject result = Request("DescribeAddresses", kwargs);
		return Utils.getJSONResult(result, "AddressSet");
    }

    /**
     *	配置浮动IP, 目前支持名称修改
     *
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *  @param name string 浮动IP的名称
     *  @return Address结构
     */
    public JSONObject ConfigAddress(String allocation_id, String name) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("AllocationId", allocation_id);
    	kwargs.put("Name", name);

		JSONObject result = Request("ConfigAddress", kwargs);
		return result.getJSONObject("Address");
    }

    /**
     *	配置浮动IP带宽
     *
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *  @param bandwidth int 浮动IP的带宽
     *  @return 请求是否成功
     */
    public JSONObject ConfigAddressBandwidth(String allocation_id, int bandwidth) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("AllocationId", allocation_id);
    	if (bandwidth != 0){
	    	kwargs.put("Bandwidth", bandwidth);
    	}
		JSONObject result = Request("ConfigAddressBandwidth", kwargs);
		return result;
    }

    /**
     *	释放浮动IP
     *
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *  @return 请求是否成功
     */
    public JSONObject ReleaseAddress(String allocation_id) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("AllocationId", allocation_id);
		JSONObject result = Request("ReleaseAddress", kwargs);
		return result;
    }

    /**
     *	绑定Fip到云产品上
     *
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *  @param association_type string 绑定云产品类型。有效值为server、elb，分别代表绑定到云服务器和ELB负载均衡器
     *  @param instance_id string 绑定的云产品ID
     *  @param bandwidth int 浮动IP的带宽
     *  @return 请求是否成功
     */
    public JSONObject AssociateAddress(String allocation_id, String association_type, String instance_id, int bandwidth) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("AllocationId", allocation_id);
    	kwargs.put("AssociationType", association_type);
    	kwargs.put("InstanceId", instance_id);
    	kwargs.put("Bandwidth", bandwidth);
		JSONObject result = Request("AssociateAddress", kwargs);
		return result;
    }

    /**
     *	将浮动IP解绑
     *
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *  @return 请求是否成功
     */
    public JSONObject DisassociateAddress(String allocation_id) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("AllocationId", allocation_id);
		JSONObject result = Request("DisassociateAddress", kwargs);
		return result;
    }

     /**
     *	将浮动IP换绑
     *
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *	@param allocation_id string 浮动IP的ID（或者IP）
     *  @return 请求是否成功
     */
    public JSONObject ReplaceAddress(String allocation_id, String newId) throws Exception {
    	JSONObject kwargs = new JSONObject();
    	kwargs.put("AllocationId", allocation_id);
    	kwargs.put("NewAllocationId", newId);
		JSONObject result = Request("ReplaceAddress", kwargs);
		return result;
    }

}
