# -*- coding: utf-8 -*-
import scrapy
import time
import re
from opensina.items import OpensinaItem
from selenium import webdriver

class OpensinaSpider(scrapy.Spider):
    name = "opensina"
    allowed_domains = ["open.sina.com.cn"]
    start_urls = ["http://open.sina.com.cn/courses/"]

    def __init__(self):
      scrapy.Spider.__init__(self)
      profile = webdriver.FirefoxProfile()
      profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
      self.driver = webdriver.Firefox(profile)

    def __del__(self):
      self.driver.close()

    def parse(self, response):
        base_url = "http://open.sina.com.cn/course/id_"
        i = 1
        #count = 1270
        count = 5
        
        while (i <= count):
            url = base_url + str(i)
            self.driver.get(url)
          
            sel = scrapy.Selector(text = self.driver.page_source)
            
            tlist = sel.xpath('(//h2[contains(@class,"fblue")])/text()').extract()
            title = tlist[0].strip(u' \t\n') if tlist else u""
            olist = sel.xpath('(//p[contains(@class,"txt")])/text()').extract()
            lecturesOverView = olist[0].strip(u' \t\n') if olist else u""
            description = u"{0}".format(lecturesOverView)
            plist = sel.xpath('(//div[contains(@class,"pic")])/img/@src').extract()
            piclink = plist[0] if plist else u""
            vlist = sel.xpath('(//video[@id="myMovie"])/@src').extract()
            if vlist:
                link = vlist[0]

                item = OpensinaItem()
                item['title'] = title.encode('utf-8')
                item['description'] = description.encode('utf-8')
                item['piclink'] = piclink
                item['courselink'] = link
                item['duration'] = u""
                item['source'] = u"opensina"
                yield item
            
            i = i + 1
            time.sleep(2)

