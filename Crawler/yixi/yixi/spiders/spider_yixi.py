import scrapy
import time
from yixi.items import YixiItem

from selenium import webdriver

class YixiSpider(scrapy.Spider):
    name = "yixi"
    allowed_domains = ["yixi.tv"]
    start_urls = ["http://yixi.tv/lectures/all"]

    def __init__(self):
      scrapy.Spider.__init__(self)
      self.driver = webdriver.Firefox()

    def __del__(self):
      self.driver.close()        

    def parse(self, response):
        base_url = "http://yixi.tv/lectures/all?page="
        page = 1
        
        while (page <= 27):
            url = base_url + str(page)
            self.driver.get(url)
          
            hxs = scrapy.Selector(text = self.driver.page_source)
            
            for sel in hxs.xpath('//div[@class="mainBody"]/section/ul/li'):
				item = YixiItem()
 				item['title'] = sel.xpath('div/span[@class="videoTitle"]/text()').extract()
	            item['description'] = sel.xpath('//div/span[@class="speakerDescription"]/text()').extract()
 				item['piclink'] = sel.xpath('//div[@class="videoContent"]/@style').extract()
	            item['courselink'] = sel.xpath('//div[@class="videoCover"]/a/@href').extract()
	            item['duration'] = sel.xpath('li[@class="playTime"]').extract()
				item['source'] = "yixi"
                yield item
            
            page = page + 1        
		
