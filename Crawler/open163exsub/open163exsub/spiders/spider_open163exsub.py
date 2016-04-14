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
        links = getlinks()
        for link in links:
            self.driver.get(link)
            time.sleep(1)
            hxs = scrapy.Selector(text = self.driver.page_source)

            tlist = hxs.xpath('/html/head/title/text()').extract()
            dlist = hxs.xpath('p[@class="desc f-c9"]/text()').extract()

            item = Open163ExSubItem()
            item['link'] = link # for correlation
            item['title'] = encode(tlist[0]) if tlist else u''
            item['description'] = encode(dlist[0]) if dlist else u''
            item['piclink'] = info.xpath('a/img/@src').extract()[0]
            item['courselink'] = info.xpath('//div[@class="net-bd"]/video/@src').extract()[0]
            item['source'] = u'网易公开课'.encode('utf-8')
            item['duration'] = u''
            item['tags'] = u''
            item['language'] = u''
            item['instructor'] = u''
            yield item






