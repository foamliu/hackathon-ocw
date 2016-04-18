# -*- coding: utf-8 -*-
import scrapy
import time
import json
from opensina.items import OpensinaItem
from selenium import webdriver

def getlinks():
    links = []
    base_url = "http://open.sina.com.cn/course/id_"
    count = 1277
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

class OpensinaSpider(scrapy.Spider):
    name = "opensina"
    allowed_domains = ["open.sina.com.cn"]
    start_urls = ["http://open.sina.com.cn/courses/"]

    def __init__(self):
      scrapy.Spider.__init__(self)
      profile = webdriver.FirefoxProfile()
      profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
      self.driver = webdriver.Firefox(profile)

    def __del__(self):
      self.driver.close()

    def download(self, link):
        self.driver.get(link)
        time.sleep(2)

        hxs = scrapy.Selector(text = self.driver.page_source)

        item = OpensinaItem()
        item['title'] = cleanse(hxs.xpath('/html/body/div[11]/div[2]/div[1]/div/h2/text()').extract())
        item['description'] = cleanse(hxs.xpath('/html/body/div[11]/div[2]/div[2]/div[3]/p[1]/text()').extract())
        item['piclink'] = cleanse(hxs.xpath('(//div[contains(@class,"pic")])/img/@src').extract())
        item['courselink'] = u''
        item['source'] = u"新浪公开课"
        item['link'] = link
        item['school'] = cleanse(hxs.xpath('//*[@id="scr_cont3"]/div/div[1]/div[1]/div/div[2]/a/text()').extract())
        item['instructor'] = cleanse(hxs.xpath('//*[@id="scr_cont3"]/div/div[1]/div[1]/div/div[2]/text()[1]').extract())
        item['language'] = u'中文'
        item['tags'] = u"新浪公开课"
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
