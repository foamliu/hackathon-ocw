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
    lines = inputfile.readlines()
    inputfile.close()
    links = []
    for line in lines:
        item = json.loads(line)
        links.append(item['link'])
    return links

def cleanse(alist):
    return alist[0].strip().encode('utf-8').replace('"', '“').replace('\n', '').replace('\t', '    ').replace('\\', '“') if alist else u''

class Open163ExSpider(scrapy.Spider):
    name = 'open163exsub'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://open.163.com"]
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

        item = Open163ExSubItem()
        item['title'] = cleanse(hxs.xpath('/html/head/title/text()').extract())
        item['title'] = item['title'].split('_')[1]
        item['description'] = cleanse(hxs.xpath('/html/body/div/div[1]/div[2]/text()').extract())
        item['piclink'] = cleanse(hxs.xpath('/html/body/div/div[1]/div[4]/a/img/@src').extract())
        item['courselink'] = cleanse(hxs.xpath('/html/body/div/div[1]/video/@src').extract())
        item['source'] = u'网易公开课'.encode('utf-8')

        label = cleanse(hxs.xpath('/html/body/div/div[1]/div[5]/p/span/text()').extract())

        school_pos = 5
        instructor_pos = 6
        language_pos = 8
        tags_pos = 9

        if label.decode('utf-8') == u'讲师：':
            school_pos = 5
            instructor_pos = 5
            language_pos = 6
            tags_pos = 7

        item['school'] = cleanse(hxs.xpath('/html/body/div/div[1]/div[{0}]/p/text()'.format(school_pos)).extract())
        item['instructor'] = cleanse(hxs.xpath('/html/body/div/div[1]/div[{0}]/p/text()'.format(instructor_pos)).extract())
        item['language'] = cleanse(hxs.xpath('/html/body/div/div[1]/div[{0}]/p/text()'.format(language_pos)).extract())
        item['tags'] = cleanse(hxs.xpath('/html/body/div/div[1]/div[{0}]/p/text()'.format(tags_pos)).extract())
        item['link'] = link
        return item

    def parse(self, response):
        links = getlinks()
        links.reverse()
        for link in links:
            print link
            isdownloaded = self.downloaded(link)
            print 'is downloaded: {0}'.format(isdownloaded)

            if not isdownloaded:
                try:
                    item = self.download(link)
                    yield item
                except Exception as err:
                    print(err)
                    #time.sleep(10)
                    #break






