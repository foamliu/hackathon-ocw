# 用今日头条的方法推荐公开课
做了个APP，名字很土，叫《学啥》，用今日头条的方法推荐公开课。服务端是阿里云的CentOS-7 + Play! + Scala + Docker + Apache Mahout, 爬虫是Python写的，基于Scrapy框架；安卓客户端用Android Studio开发，iOS客户端基于swift。代码开源在：https://github.com/foamliu/hackathon-ocw, 可以在 http://jieko.cc 扫二维码体验。服务端提供简洁的 REST API，客户端基于此与服务端通信，为用户推荐公开课视频，后台定时任务负责爬取更新和训练模型。

平时我会用它来学点东西，还是有不少问题的。欢迎有兴趣的同学加入，愿它对上进的你有用！ :-)

## 开发环境
1. 安装 [Java SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
2. 安装 [GitHub desktop](https://desktop.github.com/).
3. Git Clone 代码库:
```git clone https://github.com/foamliu/hackathon-ocw.git```

5. 开发环境：
    a. Feed API: 打开命令行窗口, 进入FeedAPI目录, 输入：`activator ui` 回车, 即可开发 FeedAPI 项目.
也可执行 `activator eclipse`, 用 [ScalaIDE](http://scala-ide.org/) 导入, 体验更好.
    b. 安卓客户端：安装 [Android Studio](http://developer.android.com/sdk/index.html), 导入即可开发.
    c. iOS 客户端：配置OS X + xcode，若没有苹果电脑，可以在 Windows 上装一个虚拟机, 参见 [这里](https://xuanwo.org/2015/08/09/vmware-mac-os-x-intro/).
    d. 爬虫：基于 Python 和 Scrapy框架，目前在 CentOS 下用 gedit 开发， 简单够用.

可用 pull request 提交合并，若需写权限请发 GitHub 账号给 foamliu@yeah.net。

## 部署服务端
1. SSH 连接(e.g. PuTTY)到 Linux VM (如CentOS-7, "207.46.137.89", user = "root", pass = "#Bugsfor$").

2. 系统更新:
```
yum update
```

3. 安装 企业版 Linux 附加软件包 (EPEL - Extra Packages for Enterprise Linux):
```
sudo yum install epel-release
```

4. 安装 Git:
```
yum install git
```
在 /root 目录下 git clone https://github.com/foamliu/hackathon-ocw.git

5. 安装 docker:
```
curl -fsSL https://get.docker.com/ | sh
sudo service docker start
sudo docker run hello-world
```

6. 安装 mongodb:
```
sudo rpm --import https://www.mongodb.org/static/pgp/server-3.2.asc
vi /etc/yum.repos.d/mongodb-org-3.2.repo
```
把下边这段贴入保存并退出:
```
[mongodb-org-3.2]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/3.2/x86_64/
gpgcheck=1
enabled=1
```
然后执行:
```
sudo yum install -y mongodb-org
vi /etc/mongod.conf
```
注释掉: "bindIp: 127.0.0.1", 然后执行:
```
sudo setenforce 0
sudo service mongod start
```

7. 导入数据:
```
cd /root/hackathon-ocw/FeedAPI/mongodb
mongoimport --db jiekodb --collection ratings --file ratings.json
mongoimport --db jiekodb --collection users --file users.json
mongoimport --db jiekodb --collection counters --file counters.json
mongoimport --db jiekodb --collection comments --file comments.json
```

8. 启动docker：
```
docker run -i -t -v "/root/hackathon-ocw/FeedAPI:/root/Code" -p 80:9000 -p 9999:9999 -p 8888:8888 foamliu/play-framework
activator clean stage
target/universal/stage/bin/play-scala
```
浏览器访问 Linux VM 的 80 端口，看到二维码，即表示部署成功。
