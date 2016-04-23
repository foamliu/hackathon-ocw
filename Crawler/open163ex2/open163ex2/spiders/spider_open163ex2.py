# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
import json

from open163ex2.items import Open163Ex2Item
from selenium import webdriver
from selenium.webdriver.common.action_chains import ActionChains

def getlinks():
    inputfile = open('open163exsub/links.json','r')
    lines = inputfile.readlines()
    links = []
    for line in lines:
        item = json.loads(line)
        links.append(item['link'])
    inputfile.close()
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
    
class Open163ExSpider(scrapy.Spider):
    name = 'open163ex'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://c.open.163.com/search/search.htm?query=#/search/video"]

    def __init__(self):
        scrapy.Spider.__init__(self)

        self.main = webdriver.Firefox()

    def __del__(self):
        self.main.close()
    
    def downloadOne(self, link):
        
        print link
        isdownloaded = downloaded(link)
        print 'is downloaded: {0}'.format(isdownloaded)

        if not isdownloaded:
            item = Open163ExItem()
            item['link'] = link
            return item 
    
    def downloadList(self, link):
    
        for info in hxs.xpath('//*[@id="list1"]/tbody/tr'):
            link = info.xpath('td[@class="u-ctitle"]/a/@href').extract()[0]
            item = downloadOne(self,link)
            yield item

    def download(self, link):
        self.driver.get(link)
        time.sleep(2)
        
        while True:

            hxs = scrapy.Selector(text = self.main.page_source)
            # 第一种常见格式
            for info in hxs.xpath('//*[@id="j-resultbox"]/div/div/div/div[1]/div[1]/div[2]/div[@class="cnt"]'):
                link = info.xpath('/a[@class="img"]').extract()[0]
                if link.startswith('http://open.163.com/movie/'):
                    item = downloadOne(self,link)
                    yield item
                    
                if link.startswith('http://open.163.com/special/'):
                    list = downloadList(self, link)
                    for item in items:
                        yield item    
            # 第二种常见格式
            # TODO
            
            next = self.main.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')

            try:
                #next.click()
                ActionChains(self.main).move_to_element(next).click().perform()
                time.sleep(5)
            except KeyboardInterrupt:
                sys.exit(0)
            except Exception as err:
                break
        
    def parse(self, response):
        links = getlinks()

        for link in links:
                
            #max_retry = 5
            #for i in range(max_retry):
            try:
                items = self.download(link)
                for item in items:
                    yield item
            except Exception as err:
                print(err)
                time.sleep(100)
                #break
