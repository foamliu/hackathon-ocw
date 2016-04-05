# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
from open163sub.items import Open163SubItem
from open163sub.PageDataLinklast import PageDataLinklast

from selenium import webdriver

class Open163SubSpider(scrapy.Spider):
    name = 'open163sub'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://open.163.com"]
    

    def __init__(self):
      scrapy.Spider.__init__(self)

      profile = webdriver.FirefoxProfile()
      profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
      self.driver = webdriver.Firefox(profile)

    def __del__(self):
      self.driver.close()

    def parse(self, response):
      for page in PageDataLinklast().page:
        self.driver.get(page)
        time.sleep(1)      
        hxs = scrapy.Selector(text = self.driver.page_source)

        for info in hxs.xpath('//div[@class="net-bd"]'):
          item = Open163SubItem()
          #item['courselink'] = info.xpath('div[@class="net-block"]/a[@target="_blank"]/@href').extract()
          item['courselink'] = page
          item['videolink'] = info.xpath('video/@src').extract()       
          yield item



