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
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

def cleanse(alist):
    return alist[0].strip().encode('utf-8').replace('"', '“').replace('\n', '').replace('\t', '    ').replace('\\', '“') if alist else u''

class GuokrSpider(scrapy.Spider):
    name = 'guokr'
    allowed_domains = ["www.guokr.com"]
    start_urls = ["http://www.guokr.com/scientific/"]
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
        
    def parse(self, response):

        self.driver.get('http://www.guokr.com/scientific/')
        time.sleep(2)
        
        #lastElement = self.driver.find_elements_by_id("waterfallLoading")[-1]
        #ActionChains(self.driver).move_to_element(lastElement).click().perform()
        #lastElement.send_keys(Keys.NULL)
            
        for i in range(1, 650):
            self.driver.execute_script("window.scrollTo(0,document.body.scrollHeight);")
            time.sleep(5)

        hxs = scrapy.Selector(text = self.driver.page_source)
        for info in hxs.xpath('//*[@id="waterfall"]/div'):
            link = cleanse(info.xpath('a[@data-gaevent="scientific_image:v1.1.1.1:scientific"]/@href').extract())
            if not self.downloaded(link):
                title = cleanse(info.xpath('h3/a[@class="article-title"]/text()').extract())
                if (title != '' and link != ''):
                    try:
                        item = GuokrItem()
                        item['title'] = title
                        item['description'] = cleanse(info.xpath('p[@class="article-summary"]/text()').extract())
                        item['piclink'] = cleanse(info.xpath('a/img/@src').extract())
                        item['courselink'] = u''
                        item['source'] = u'果壳网'
                        item['school'] = u'果壳网'
                        item['instructor'] = cleanse(info.xpath('div/a[1]/text()').extract())
                        item['language'] = u'中文'
                        item['tags'] = cleanse(info.xpath('a[@class="label label-common"]/text()').extract())
                        item['link'] = link
                        item['posted'] = cleanse(info.xpath('normalize-space(div[@class="article-info"])').extract())
                        if '|' in item['posted']:
                            item['posted'] = item['posted'].split('|')[1]
                        item['crawled'] = time.strftime('%Y-%m-%d %H:%M', time.localtime())
                        yield item
                    except Exception as err:
                        print(err)
                        #break






