# -*- coding: utf-8 -*-
import scrapy
import time
import json
from yixi.items import YixiItem
from selenium import webdriver

def getlinks():
    links = []
    base_url = "http://yixi.tv/lecture/"
    count = 354
    i = count
    while (i >= 1):
        url = base_url + str(i)
        links.append(url)
        i = i - 1
    return links

def getout():
    out = []
    inputfile = open('out.json','r')
    lines = inputfile.readlines()
    inputfile.close()
    for line in lines:
        out.append(json.loads(line))
    return out

def cleanse(alist):
    return alist[0].strip().encode('utf-8').replace('"', '“').replace('\n', '').replace('\t', '    ') if alist else u''

def downloaded(link):
    out = getout()
    for js in out:
        if js['link'] == link:
            return True
    return False

class YixiSpider(scrapy.Spider):
    name = "yixi"
    allowed_domains = ["yixi.tv"]
    start_urls = ["http://yixi.tv/lecture/"]

    def __init__(self):
      scrapy.Spider.__init__(self)
      self.driver = webdriver.Firefox()

    def __del__(self):
      self.driver.close()

    def download(self, link):
        self.driver.get(link)
        time.sleep(2)

        hxs = scrapy.Selector(text = self.driver.page_source)

        speakerIntr = cleanse(hxs.xpath('(//span[contains(@class,"speakerIntr")])/text()').extract())

        item = YixiItem()
        item['title'] = cleanse(hxs.xpath('/html/body/div[1]/div[2]/section[2]/h2/text()').extract())
        item['description'] = cleanse(hxs.xpath('(//div[contains(@class,"lecturesOverView")])/text()').extract())
        item['piclink'] = cleanse(hxs.xpath('/html/body/div[1]/div[2]/section[1]/div/div/img/@src').extract())
        item['courselink'] = u""
        item['source'] = u"一席"
        item['school'] = cleanse(hxs.xpath('/html/body/div[1]/div[2]/section[2]/span[2]/text()').extract())
        item['instructor'] = cleanse(hxs.xpath('(//span[contains(@class,"speakerName")])/text()').extract())
        item['language'] = u"中文"
        item['tags'] = u"一席"
        item['link'] = link
        #item['posted'] = cleanse(hxs.xpath('/html/body/div[1]/div[2]/section[2]/span[1]/text()').extract())
        #item['crawled'] = time.strftime("%Y-%m-%d")
        return item
      
    def parse(self, response):
        
        for link in getlinks():
            print link
            isdownloaded = downloaded(link)
            print 'is downloaded: {0}'.format(isdownloaded)

            if not isdownloaded:
                
                #max_retry = 5
                #for i in range(max_retry):
                try:
                    item = self.download(link)
                    yield item
                except Exception as err:
                    print(err)
                    #time.sleep(100)
                    break

