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
    inputfile = open('open163ex2/links.json','r')
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
    
class Open163Ex2Spider(scrapy.Spider):
    name = 'open163ex2'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://c.open.163.com/search/search.htm?query=#/search/video"]

    def __init__(self):
        scrapy.Spider.__init__(self)

        self.main = webdriver.Firefox()
        self.detail = webdriver.Firefox()

    def __del__(self):
        self.main.close()
        self.detail.close()
    
    def downloadOne(self, link):
        print 'one: '+link
        isdownloaded = downloaded(link)
        print 'is downloaded: {0}'.format(isdownloaded)

        if not isdownloaded:
            item = Open163Ex2Item()
            item['link'] = link
            return item 
    
    def downloadList(self, link):
        self.detail.get(link)
        time.sleep(2)
        
        try:
            more = self.detail.find_element_by_xpath('/html/body/div[8]/div[1]/div[1]/div[2]')
            ActionChains(self.detail).move_to_element(more).click().perform()
            time.sleep(5)
        except Exception as err:
            print(err)
        
        hxs = scrapy.Selector(text = self.detail.page_source)
        
        for info in hxs.xpath('//td[@class="u-ctitle"]'):
            alist = info.xpath('a/@href').extract()
            if alist:
                alink = alist[0]
                
                if alink.startswith('http://open.163.com/movie/'):
                    print('list: ' + alink)
                    item = self.downloadOne(alink)
                    yield item

    def download(self, link):
        self.main.get(link)
        time.sleep(2)
        
        while True:

            hxs = scrapy.Selector(text = self.main.page_source)
            # 第一种常见格式
            #//*[@id="j-resultbox"]/div/div/div/div[1]/div[1]/div[2]/
            for info in hxs.xpath('//div[@class="cnt"]'):
                llist = info.xpath('a[@class="img"]/@href').extract()
                if llist:
                    link = llist[0]
                    #print(link)
                    if link.startswith('http://open.163.com/movie/'):
                        item = self.downloadOne(link)
                        yield item
                        
                    if link.startswith('http://open.163.com/special/'):
                        alist = self.downloadList(link)
                        for item in alist:
                            yield item
            # 第二种常见格式
            # TODO
            
            next = self.main.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')
            aclass = next.get_attribute('class')
            if ('js-disabled' in aclass):
                break

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
                
            try:
                items = self.download(link)
                for item in items:
                    yield item
            except Exception as err:
                print(err)
                break
