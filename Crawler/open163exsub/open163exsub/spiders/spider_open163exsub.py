# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
import json
from selenium import webdriver
from open163exsub.items import Open163ExSubItem

def getlinks():
    inputfile = open('open163exsub/links.json','r')
    jsonObj = json.load(inputfile)
    return jsonObj['links']

def encode(field):
    return field.encode('utf-8').replace('"', '“').replace('\n', '').replace('\t', '    ')

class Open163ExSpider(scrapy.Spider):
    name = 'open163exsub'
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

        for link in getlinks():
            self.driver.get(link)
            time.sleep(2)

            hxs = scrapy.Selector(text = self.driver.page_source)

            item = Open163ExSubItem()
            item['title'] = encode(hxs.xpath('/html/head/title/text()'))
            item['description'] = encode(hxs.xpath('/html/body/div/div[1]/div[2]/text()').extract())
            item['piclink'] = info.xpath('/html/body/div/div[1]/div[4]/a/img/@src').extract()
            item['courselink'] = info.xpath('/html/body/div/div[1]/video/@src').extract()
            item['source'] = u'网易公开课'.encode('utf-8')
            item['school'] = encode(hxs.xpath('/html/body/div/div[1]/div[5]/p/text()').extract())
            item['instructor'] = encode(hxs.xpath('/html/body/div/div[1]/div[6]/p/text()').extract())
            item['language'] = encode(hxs.xpath('/html/body/div/div[1]/div[8]/p/text()').extract())
            item['tags'] = encode(hxs.xpath('/html/body/div/div[1]/div[9]/p/text()').extract())
            item['link'] = link
            yield item






