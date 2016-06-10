# 用今日头条的方法推荐公开课
做了个APP，名字很土，叫《学啥》。《学啥》用今日头条的方法推荐公开课。服务端是阿里云CentOS7 + Play! + Scala + Docker + Apache Mahout, 爬虫是Python写的，基于Scrapy框架。有安卓和iOS客户端。代码开源在：https://github.com/foamliu/hackathon-ocw, 可以在 http://jieko.cc 扫二维码下载体验。服务端框架很简单，客户端通过REST API与服务端通信；服务端为用户推荐适合的公开课视频，后台定时任务负责爬取课程更新和训练模型。

平时我会用它来学点东西，还是不少问题，欢迎有兴趣的同学加入，希望它对有情怀的你们也有用！ :-)

