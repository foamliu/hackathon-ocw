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
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

def getlinks():
    inputfile = open('open163ex2/links.json','r')
    lines = inputfile.readlines()
    links = []
    for line in lines:
        item = json.loads(line)
        links.append(item['link'])
    inputfile.close()
    return links

def cleanse(alist):
    return alist[0].strip().encode('utf-8').replace('"', '“').replace('\n', '').replace('\t', '    ').replace('\\', '“') if alist else u''

class Open163Ex2Spider(scrapy.Spider):
    name = 'open163ex2'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://c.open.163.com/search/search.htm?query=#/search/video"]
    out = []

    def __init__(self):
        scrapy.Spider.__init__(self)

        self.main = webdriver.Firefox()
        self.detail = webdriver.Firefox()
        self.detail.set_page_load_timeout(2)

    def __del__(self):
        self.main.close()
        self.detail.close()
    
    def getout(self):
        if len(self.out) == 0:
            inputfile = open('out.json','r')
            lines = inputfile.readlines()
            inputfile.close()
            for line in lines:
                self.out.append(json.loads(line))
        return self.out

    def downloaded(self, link):
        for js in self.getout():
            if js['link'] == link:
                return True
        return False
        
    def downloadOne(self, link):
        print 'one: '+link
        isdownloaded = self.downloaded(link)
        print 'is downloaded: {0}'.format(isdownloaded)

        if not isdownloaded:
            item = Open163Ex2Item()
            item['link'] = link
            return item 
    
    def downloadList(self, link):
        try:
            print("downloading: " + link)
            self.detail.get(link)
            time.sleep(2)
        except Exception as err:
            return
            
        try:
            more = self.detail.find_element_by_xpath('/html/body/div[8]/div[1]/div[1]/div[2]')
            ActionChains(self.detail).move_to_element(more).click().perform()
            time.sleep(5)
        except Exception as err:
            pass
        
        hxs = scrapy.Selector(text = self.detail.page_source)
        
        title = cleanse(hxs.xpath('/html/body/div[6]/div/span[2]/text()').extract())
        if title and title != '':
            item = Open163Ex2Item()
            item['courselink'] = link
            item['coursetitle'] = title
            item['coursedescription'] = cleanse(hxs.xpath('/html/body/div[7]/div/div/p[3]/text()').extract())
            item['coursepiclink'] = cleanse(hxs.xpath('/html/body/div[7]/div/img/@src').extract())
            item['courseinstructor'] = cleanse(hxs.xpath('/html/body/div[8]/div[2]/div[1]/div/div/h6[1]/span/text()').extract())
            
            items = []

            for info in hxs.xpath('//td[@class="u-ctitle"]'):
                t1 = cleanse(info.xpath('text()').extract())
                t2 = cleanse(info.xpath('a/text()').extract())
                alist = info.xpath('a/@href').extract()
                alink = alist[0]
                course = {}
                course['link'] = alink
                course['title'] = t1 + t2
                if course not in items:
                    items.append(course)

            item['items'] = items
            yield item

    def download(self, link):
        self.main.get(link)
        time.sleep(2)
        
        while True:

            hxs = scrapy.Selector(text = self.main.page_source)
            # 第一种常见格式
            for info in hxs.xpath('//div[@class="cnt"]'):
                llist = info.xpath('a[@class="img"]/@href').extract()
                if llist:
                    link = llist[0]
                    #print(link)
                    #if link.startswith('http://open.163.com/movie/'):
                    #    item = self.downloadOne(link)
                    #    yield item
                        
                    if link.startswith('http://open.163.com/special/'):
                        try:
                            alist = self.downloadList(link)
                            for item in alist:
                                yield item
                        except Exception as err:
                            print(err)
            # 第二种常见格式
            # TODO
            
            next = self.main.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')
            aclass = next.get_attribute('class')
            if ('js-disabled' in aclass):
                break

            try:
                ActionChains(self.main).move_to_element(next).click().perform()
                time.sleep(5)
            except KeyboardInterrupt:
                sys.exit(0)
            except Exception as err:
                pass
        
    def parse(self, response):
        links = getlinks()

        for link in links:
            items = self.download(link)
            for item in items:
                yield item

