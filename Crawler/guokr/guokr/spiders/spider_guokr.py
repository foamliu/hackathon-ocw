# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
import json
from selenium.webdriver.common.keys import Keys
from selenium import webdriver
from selenium.webdriver.common.action_chains import ActionChains
from guokr.items import GuokrItem

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

class GuokrSpider(scrapy.Spider):
    name = 'guokr'
    allowed_domains = ["www.guokr.com"]
    start_urls = ["http://www.guokr.com/scientific/"]

    def __init__(self):
        scrapy.Spider.__init__(self)

        profile = webdriver.FirefoxProfile()
        profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
        self.driver = webdriver.Firefox(profile)

    def __del__(self):
        self.driver.close()

    def parse(self, response):

        self.driver.get('http://www.guokr.com/scientific/')
        time.sleep(2)
        
        #lastElement = self.driver.find_elements_by_id("waterfallLoading")[-1]
        #ActionChains(self.driver).move_to_element(lastElement).click().perform()
        #lastElement.send_keys(Keys.NULL)
            
        for i in range(1, 600):
            self.driver.execute_script("window.scrollTo(0,document.body.scrollHeight);")
            time.sleep(5)

        hxs = scrapy.Selector(text = self.driver.page_source)
        for info in hxs.xpath('//*[@id="waterfall"]/div'):
            item = GuokrItem()
            item['title'] = cleanse(info.xpath('h3/a[@class="article-title"]/text()').extract())
            item['description'] = cleanse(info.xpath('p[@class="article-summary"]/text()').extract())
            item['piclink'] = cleanse(info.xpath('a/img/@src').extract())
            item['courselink'] = u''
            item['source'] = u'果壳网'
            item['school'] = u'果壳网'
            item['instructor'] = cleanse(info.xpath('div/a[1]/text()').extract())
            item['language'] = u'中文'
            item['tags'] = cleanse(info.xpath('a[@class="label label-common"]/text()').extract())
            item['link'] = cleanse(info.xpath('a[@data-gaevent="scientific_image:v1.1.1.1:scientific"]/@href').extract())
            yield item






