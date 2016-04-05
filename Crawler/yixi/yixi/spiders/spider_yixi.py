import scrapy
import time
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
        
        while (i <= 323):
            url = base_url + str(i)
            self.driver.get(url)
          
            sel = scrapy.Selector(text = self.driver.page_source)

            speakerName = sel.xpath('(//span[contains(@class,"speakerName")])[1]/text()').extract()
            speakerIntr = sel.xpath('(//span[contains(@class,"speakerIntr")])[1]/text()').extract()
            lecturesOverView = sel.xpath('(//div[contains(@class,"lecturesOverView")])[1]/text()').extract()
            video_id = 

            item = YixiItem()
            item['title'] = sel.xpath('(//section[contains(@class,"cutline")])[1]/h2/text()').extract()
            item['description'] = lecturesOverView + '\n' + speakerName + '\n' + speakerIntr
            item['piclink'] = sel.xpath('(//div[contains(@class,"detailHeadBox")])[1]/img/@src').extract()
            item['courselink'] = ""
            item['duration'] = "01:00"
            item['source'] = "yixi"
            yield item
            
            i = i + 1

