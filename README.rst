美团云 API JAVA SDK和客户端
============================

美团云提供了方便调用美团云API的Java SDK以及基于该SDK实现的客户端。


获取二进制
-----------

从"`这里 <http://mtmos.com:80/v1/mss_a9564ab5d57a4fc183232162004ade09/javasdk/>`_"获取二进制并解压，进入mos-java-bin目录，执行下述命令运行java客户端::

    ./climos


获取源码
--------

执行下列命令从github获取源码::

    git clone https://github.com/meituan/mcsapi_java

进入mos_java_sdk，执行命令编译生成SDK java class::

    mvn compile

然后，进行mos_java_client，执行命令编译生成client java classes::

    mvn compile

在mos_java_client目录中，执行下述命令执行::

    ./bin/climos


获取MOS API访问密钥
-------------------

访问MOS管理界面的"`API <https://mos.meituan.com/console/#api>`_"页面获得API入口URL、ACCESS Key和Secret。


使用SDK
--------

API详细说明参见"`javadoc <http://mtmos.com:80/v1/mss_a9564ab5d57a4fc183232162004ade09/javaapidoc/>`_"。

示例代码如下:

::

    import com.meituan.mos.sdk.v1.Client;

    client = new Client(key, secret, url, region, format, timeout, debug);
    JSONObject result = client.GetBalance();
    System.out.println(result.get("balance"));


使用客户端
----------

通过上述步骤获得客户端软件后，执行以下步骤开始使用美团云 API java客户端。

1. 在MOS帐户页面获取个人的MOS ACCESS Key和Secret，以及API入口URL，设置如下环境变量：

::

    export MOS_ACCESS=4ba303cc17454cc7904e044db2a3c912
    export MOS_SECRET=2952f821201341a38978ac4a4a292ce8
    export MOS_URL=https://mosapi.meituan.com/mcs/v1
    export MOS_REGION=Beijing

2. 执行climos客户端

::

    ./climos help  # 显示所有支持的命令
    ./climos help DescribeTemplates # 查看某个子命令的参数
    ./climos DescribeTemplates # 执行获取模板列表的命令
