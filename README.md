# 用今日头条的方法推荐公开课
做了个APP，名字很土，叫《学啥》。用今日头条的方法推荐公开课。服务端是阿里云CentOS7+Play!+Scala+Docker+Appache Mahout, 爬虫是Scrapy，做了安卓客户端和简易的iOS客户端。代码开源在：https://github.com/foamliu/hackathon-ocw, 可以在 http://jieko.cc 搜二维码下载体验。服务端框架很简单，客户端通过REST API与服务端通信；服务端为用户推荐适合的公开课视频，后台定时任务负责训练模型。

现在有空时我可以用它来学点东西，但还是很多问题。欢迎有兴趣的同学加入一起开发，希望它对爱学习的同学们都有用！ :-)

