# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
import json
from selenium import webdriver
from infoqsub.items import InfoqsubItem

def getlinks():
    links = []
    inputfile = open('infoqsub/links.json','r')
    lines = inputfile.readlines()
    inputfile.close()
    for line in lines:
        jsonObj = json.loads(line)
        links.append(jsonObj['link'])
    return links

def cleanse(alist):
    return alist[0].strip().encode('utf-8').replace('"', '“').replace('\n', '').replace('\t', '    ').replace('\\', '“') if alist else u''


class InfoqsubSpider(scrapy.Spider):
    name = 'infoqsub'
    allowed_domains = ["www.infoq.com"]
    start_urls = ["http://www.infoq.com/cn/presentations"]
    out = []

    def __init__(self):
        scrapy.Spider.__init__(self)

        profile = webdriver.FirefoxProfile()
        profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
        self.driver = webdriver.Firefox(profile)

    def __del__(self):
        self.driver.close()

    def getout(self):
        if len(self.out) == 0:
            inputfile = open('out.json','r')
            lines = inputfile.readlines()
            inputfile.close()
            for line in lines:
                self.out.append(json.loads(line))
        return self.out

    def downloaded(self, link):
        out = self.getout()
        for js in out:
            if js['link'] == link:
                return True
        return False
        
    def download(self, link):
        self.driver.get(link)
        time.sleep(2)

        hxs = scrapy.Selector(text = self.driver.page_source)

        item = InfoqsubItem()
        item['title'] = cleanse(hxs.xpath('/html/body/div[1]/div/section/h1/text()').extract())
        item['description'] = cleanse(hxs.xpath('/html/body/div[1]/div/div[3]/p[1]/text()').extract())
        item['piclink'] = cleanse(hxs.xpath('/html/body/div[1]/div/div[1]/div[1]/div[1]/div[3]/img/@src').extract())
        item['courselink'] = u''
        item['source'] = u'InfoQ'.encode('utf-8')
        item['school'] = u'InfoQ'.encode('utf-8')
        item['instructor'] = cleanse(hxs.xpath('/html/body/div[1]/div/section/p/a/text()').extract())
        item['language'] = u'中文'.encode('utf-8')
        item['tags'] = u'计算机 InfoQ'.encode('utf-8')
        item['link'] = link
        return item

    def parse(self, response):

        for link in getlinks():
            print link
            isdownloaded = self.downloaded(link)
            print 'is downloaded: {0}'.format(isdownloaded)

            if not isdownloaded:
                
                #max_retry = 5
                #for i in range(max_retry):
                try:
                    item = self.download(link)
                    yield item
                except Exception as err:
                    print(err)
                    time.sleep(100)
                    #break






