# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
from open163ex.items import Open163ExItem

from selenium import webdriver

class Open163ExSpider(scrapy.Spider):
    name = 'open163'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://c.open.163.com/search/search.htm?query=#/search/video"]

    def __init__(self):
      scrapy.Spider.__init__(self)
      self.driver = webdriver.Firefox()

    def __del__(self):
      self.driver.close()

    def parse(self, response):
      self.driver.get("http://c.open.163.com/search/search.htm?query=#/search/video")
      #self.browser.get(response.url)
      time.sleep(5)

      while True:
      
        hxs = scrapy.Selector(text = self.driver.page_source)

        for info in hxs.xpath('//div[@class="cnt"]'):
          item = Open163Item()
          item['title'] = info.xpath('a/@title').extract()
          item['courselink'] = info.xpath('a/@href').extract()
          item['piclink'] = info.xpath('a/img/@src').extract()
          item['description'] = info.xpath('p[@class="desc f-c9"]/text()').extract()
          yield item

        next = self.driver.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')
        #print next.text
        time.sleep(2)

        try:
          next.click()

        except Exception as err:
          print(err)
          break
 



