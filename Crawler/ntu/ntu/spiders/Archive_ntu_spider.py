# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
from ntu.items import NtuItem
from ntu.PageData import PageData

class NtuSpider(scrapy.Spider):
    name = 'ntu'
    allowed_domains = ["ntu.edu.tw"]
    start_urls = [("http://ocw.aca.ntu.edu.tw/ntu-ocw/index.php/ocw/coupage/" + word) for word in PageData().page]

    def parse(self, response):
      for info in response.xpath('//div[@class="coursebox"]'):
        item = NtuItem()
        item['title'] = info.xpath('div[@class="coursetext"]/div[@class="coursetitle"]/a/text()').extract()
        item['author'] = info.xpath('div[@class="coursetext"]/div[@class="teacher"]/text()').extract()
        item['piclink'] = info.xpath('div[@class="coursepic"]/a/img/@src').extract()
        item['courselink'] = info.xpath('div[@class="coursepic"]/a/@href').extract()
        item['description'] = info.xpath('div[@class="coursetext"]/div[@class="introtext"]/text()').extract()
        yield item
      
      '''
      #next page
      next_page = response.xpath('//div[@id="pagenavi"]/ul[@id="pagecount"]/li/a/@href')
      if next_page:
        url = response.urljoin(next_page[0].extract())
        yield scrapy.Request(url, self.parse) 
      '''
