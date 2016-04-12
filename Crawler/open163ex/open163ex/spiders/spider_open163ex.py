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
    name = 'open163ex'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://c.open.163.com/search/search.htm?query=#/search/video"]

    def __init__(self):
        scrapy.Spider.__init__(self)
        self.driver = webdriver.Firefox()

    def __del__(self):
        self.driver.close()

    def parse(self, response):
        self.driver.get("http://c.open.163.com/search/search.htm?query=#/search/video")

        while True:
      
            hxs = scrapy.Selector(text = self.driver.page_source)

            for info in hxs.xpath('//div[@class="cnt"]'):
                item = Open163ExItem()
                item['title'] = info.xpath('a/@title').extract()[0].encode('utf-8').replace('"', '“')
                item['courselink'] = info.xpath('a/@href').extract()[0]
                item['piclink'] = info.xpath('a/img/@src').extract()[0]
                item['description'] = info.xpath('p[@class="desc f-c9"]/text()').extract()[0].encode('utf-8').replace('"', '“')
                item['source'] = u'网易公开课'.encode('utf-8')
                yield item

            next = self.driver.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')
            time.sleep(2)

            try:
                next.click()

            except Exception as err:
                print(err)
                break



