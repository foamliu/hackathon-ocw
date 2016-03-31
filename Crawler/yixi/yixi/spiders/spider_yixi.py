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

            for info in hxs.xpath('//div[@class="mainBody"]/section/ul/li'):
              item = YixiItem()
              item['title'] = info.xpath('div/span[@class="videoTitle"]/text()').extract()
              yield item

            page = page + 1        
		
