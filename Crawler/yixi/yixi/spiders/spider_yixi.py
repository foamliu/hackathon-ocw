# -*- coding: utf-8 -*-
import scrapy
import time
import re
from yixi.items import YixiItem
from selenium import webdriver

class YixiSpider(scrapy.Spider):
    name = "yixi"
    allowed_domains = ["yixi.tv"]
    start_urls = ["http://yixi.tv/lecture/"]

    def __init__(self):
      scrapy.Spider.__init__(self)
      self.driver = webdriver.Firefox()

    def __del__(self):
      self.driver.close()

    def parse(self, response):
        base_url = "http://yixi.tv/lecture/"
        i = 1
        count = 351
        
        while (i <= count):
            url = base_url + str(i)
            self.driver.get(url)
          
            sel = scrapy.Selector(text = self.driver.page_source)
            
            tlist = sel.xpath('(//section[contains(@class,"cutline")])/h2/text()').extract()
            title = tlist[0] if tlist else u""
            snlist = sel.xpath('(//span[contains(@class,"speakerName")])/text()').extract()
            speakerName = snlist[0] if snlist else u""
            silist = sel.xpath('(//span[contains(@class,"speakerIntr")])/text()').extract()
            speakerIntr = silist[0] if silist else u""
            olist = sel.xpath('(//div[contains(@class,"lecturesOverView")])/text()').extract()
            lecturesOverView = olist[0] if olist else u""
            description = u"{0}\n{1}\n{2}\n".format(lecturesOverView,speakerName,speakerIntr)
            plist = sel.xpath('(//div[contains(@class,"detailHeadBox")])/img/@src').extract()
            piclink = plist[0] if plist else u""
            slist = sel.xpath('(//script[@src="http://player.youku.com/jsapi"])/text()').extract()
            if slist:
                script = slist[0]
                m = re.search("(?<=vid: \').*(?=\')", str(script))

                if m is not None:
                    video_id = m.group(0)

                    item = YixiItem()
                    item['title'] = title.encode('utf-8')
                    item['description'] = description.encode('utf-8')
                    item['piclink'] = piclink
                    item['courselink'] = url
                    item['duration'] = u""
                    item['source'] = u"一席"
                    item['video_id'] = video_id
                    yield item
            
            i = i + 1
            time.sleep(2)

