# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time
import json

from open163ex.items import Open163ExItem
from selenium import webdriver
from selenium.webdriver.common.action_chains import ActionChains

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

        # profile = webdriver.FirefoxProfile()
        # profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")

        self.main = webdriver.Firefox()
        # self.detail = webdriver.Firefox(profile)
        # This will throw a TimeoutException whenever the page load takes more than 30 seconds.
        # self.detail.set_page_load_timeout(30)

    def __del__(self):
        self.main.close()
        # self.detail.close()

    def parse(self, response):

        self.main.get("http://c.open.163.com/search/search.htm?query=#/search/video")

        while True:

            hxs = scrapy.Selector(text = self.main.page_source)

            for info in hxs.xpath('//div[@class="cnt"]'):
                link = info.xpath('a/@href').extract()[0]
                print link
                isdownloaded = downloaded(link)
                print 'is downloaded: {0}'.format(isdownloaded)

                if not isdownloaded:
                    item = Open163ExItem()
                    item['link'] = link
                    yield item

            next = self.main.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')

            try:
                #next.click()
                ActionChains(self.main).move_to_element(next).click().perform()
                time.sleep(5)
            except KeyboardInterrupt:
                sys.exit(0)
            except Exception as err:
                print(err)


