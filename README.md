<div align="center">

# Coze-Discord-Bridge

_免费的ChatGPT Turo 128k API_

_通过 `discord bot`调用 `coze 托管 discord bot`实现`免费使用GPT-4作为API`_

_觉得有点用的话 别忘了点个🌟_

</div>

## 截图
![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/24e4304b-a5f7-4baa-9559-8c01f9a935b3)

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/285d33e5-6898-4324-8f9c-8842f3a3912c)

## 功能

注:是最新源代码里支持的功能 不是Release里的 要看Release的往前翻Commit api文档 配置文件同样
- [X] 适配`NextChat`,`LobeChat`等可以修改OpenAPI URL的AI平台
- [X] HTTP/HTTPS API支持
- [X] 支持文生图(需`coze`配置`DALL·E3`/`DALL·E2`插件)返回图片url
- [X] 支持图生文(需`coze`配置`GPT4V`插件)(发送的文本消息中携带图片url/自己上传base64图片)
- [x] 支持对话隔离
- [X] 对话支持流式返回
- [X] 支持和`openai`对齐的对话接口(`v1/chat/completions`)
- [X] 支持和`openai`对齐的图像生成接口(`v1/images/generations`)
- [X] 突破Discord Bot 2k字消息长度上限
- [X] 定时活跃机器人 自定义活跃间隔 避免bot因为太久未互动而离线
- [ ] 导入此jar进行二次开发 [::80%]
- [ ] WebUI
- [ ] 多个Bot 负载均衡
- [ ] Token计数

大饼很甜,苦了的只是猫猫

## 部署准备材料

1.一个Windows/Linux/...机器 (只要能运行java,能联网就行)  需要安装java (推荐jdk17 已知jdk8及以下版本不兼容)

2.一个代理服务器/材料一的机器在国外

3.一个手机号/Google账号

4.一个Discord账号

## 部署

1.下载Release或者自行构建

2.运行一遍 `java -jar CozeDiscordBridge-xxxxxx.jar` 如果一切正常,你可以在运行目录看到  `Config.yml` 配置文件

3.打开配置文件,进行编辑

````
#Github: https://github.com/catx-feitu/coze-discord-bridge
Bots:
   - #访问密钥 留空或default 表示无需密钥 通过不同的密钥链接不同的bot
     Key: "default"
     #登录协议
     Protocol: "discord"
     #Discord user token
     #打开Discord(推荐注册小号 因为UserBot 本身Discord就禁止) 按下F12打开开发者模式
     #点进网络 随便选择一个 复制请求头中的 Authorization 粘贴在这里
     Token: ""
     #[仅Discord可用]创建频道时使用的父频道 (也可以理解成 分组) 打开开发者模式 右键就可以看到ID 为空关闭
     CreateChannel_Category: ""
     #Coze Bot所处的服务器ID 打开Discord开发者模式 右键服务器复制过来即可
     Server_id: ""
     #接入Coze的Bot id 邀请进服务器在用户列表右键 复制用户ID 过来即可
     CozeBot_id: ""

#配置是否启用代理  代理类型 HTTP 或 SOCKS 常用于中国大陆机器部署
UsingProxy: false
ProxyIP: 127.0.0.1
ProxyPort: 8080
ProxyType: HTTP

#API端口设置为0关闭 如果HTTP和HTTPS都监听失败则无法启动
#API端口 默认8092 curl http://127.0.0.1:8092/Ping
APIPort: 8092
#API HTTPS 端口 默认8093 curl https://127.0.0.1:8093/Ping
APISSLPort: 8093
......
````
首先你要在[Discord开发者平台](https://discord.com/developers/)创建一个Application

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/41310da4-5db7-46df-946d-de642b64f985)

点击Bot 然后获取Token 复制保存到其它地方

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/bf45bbd4-0039-4723-b781-38854b607bbc)

往下滑动 开启下面三个按钮 随后点击保存

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/dd4a278d-9bac-45b4-b871-f3c92a136172)

点击Oauth 勾选Bot 然后往下滑勾选Administrator (省事 如果注重安全性那么请确保 链接到Coze的bot能收发和编辑消息 链接到Coze-Discord-Birdge的bot能收发消息和创建删除子频道)

复制下方生成的URL

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/8bb919a9-0dd5-480e-8062-2733b5ef5084)
之后第二个Bot相同的操作 这样你就有了两个Token 两个URL

打开Discord App(网页版亦可) 创建一个服务器

然后依次打开两个URL 把两个bot都添加进服务器

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/1c72bf00-dfc7-48fe-adb8-fcfcbf05ad95)

点击左下角设置打开Discord设置

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/2310f3c6-1060-4b18-a026-0feaaf1a82e0)



之后登录[Coze AI Studio](https://www.coze.com/)创建一个Bot
![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/9de286bf-d6bd-43a4-a2a0-8566dd706d84)

之后可以配置Bot 添加插件(要能AI画图必须添加) 调整GPT设置之类的(Dialog round = 对话轮数  推荐拉满) 最后点击右上角Publish

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/cf33dfef-33f6-420c-8473-aecddb789432)

输入 Token 点击保存 然后Publish

ps:Changelog必填 随便写即可 如果你有强迫症的话那..不太建议..

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/f5b32f9e-8f9b-484a-afcd-7a935904dd45)

如果配置正确你应该能看到托管到Coze的机器人上线了

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/580a331d-713f-4686-961e-8c3169bcbee4)

下滑 找到高级设置 开启开发者模式

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/ca33a63f-6de7-44d5-b6bb-b13162712056)

点击左上角复制服务器ID

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/d84f4363-a0f6-4f05-abc8-798f5742794a)

点击右侧复制Bot 用户ID

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/9b560037-9d4d-4324-892d-dbb8bcc90bc3)

在Config中保存这两个ID

````
     ......
     #Coze Bot所处的服务器ID 打开Discord开发者模式 右键服务器复制过来即可
     Server_id: "xxxxxx"
     #接入Coze的Bot id 邀请进服务器在用户列表右键 复制用户ID 过来即可
     CozeBot_id: "xxxxxx"
     ......
````

回到Discord页面 按下F12打开浏览器开发者页面

点击网络(Network) 随便选择一个 复制请求头 Authorization

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/7d802fb5-4c62-458f-83da-c0b7770fd6d1)

保存到配置中
````
     ......
     #Discord user token
     #打开Discord(推荐注册小号 因为UserBot 本身Discord就禁止) 按下F12打开开发者模式
     #点进网络 随便选择一个 复制请求头中的 Authorization 粘贴在这里
     Token: "xxxxxxxxxxxxxxx"
     ......
````
4.再次运行 `java -jar CozeDiscordBridge-xxxxxx.jar` 如下显示则正常  如果您是使用的是Windows且控制台编码为GBK 请先执行`chcp 65001`

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/803bfe60-39d5-42d5-b1b3-7aaf932a2808)

ps:第一次启动报错 `读取 cache_names.json 失败` 正常 直接忽略即可

5.最后可通过curl或者其它工具测试 如果服务器内你的账号自动向机器人提问 随后机器人回答就是部署成功

![image](https://github.com/catx-feitu/Coze-Discord-Bridge/assets/108512490/a87d04ac-5c52-4929-bb7c-ff62bd2fde65)

## (可选)keepalive

因为Discord/Coze问题 当bot很长时间不互动会离线 遇到这种情况需要去Coze手动重新登录 很麻烦

因此 你可以通过编辑配置开启keepalive功能

它可以自动与Coze托管的bot对话 当累计到一段时间bot没有被互动过

````
......
# Keepalive 通过定时与Coze托管的bot互动防止因为太久未发言而被强制下限
# 内置定时器执行周期 单位分钟  设置 0 关闭  关闭后也可以通过api调用keepalive
Keepalive_timer: 0
# 只有大于指定分钟未发言Coze托管的bot才执行keepalive 单位分钟
Keepalive_maxIntervalMinutes: 720
# keepalive发送消息所在频道
Keepalive_sendChannel: "keepalive"
# keepalive发送消息内容
Keepalive_sendMessage: "keepalive"
````

当关闭内置定时器时 你也可以通过访问终结点`/api/keepalive`来执行keepalive任务

## API文档

[传送门](https://github.com/catx-feitu/Coze-Discord-Bridge/wiki)

````

## 免责申明

本项目中的任何代码/构建产品仅供学习使用

使用即代表您承担一切滥用所造成的后果

作者不保证软件绝对稳定 如果有能力请使用[Coze API](https://www.coze.com/open)

