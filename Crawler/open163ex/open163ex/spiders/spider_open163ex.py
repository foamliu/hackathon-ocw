# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy
import time

from open163ex.items import Open163ExItem
from selenium import webdriver
from selenium.webdriver.common.action_chains import ActionChains

class Open163ExSpider(scrapy.Spider):
    name = 'open163ex'
    allowed_domains = ["open.163.com"]
    start_urls = ["http://c.open.163.com/search/search.htm?query=#/search/video"]

    def __init__(self):
        scrapy.Spider.__init__(self)

        profile = webdriver.FirefoxProfile()
        profile.set_preference("general.useragent.override","Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")

        self.main = webdriver.Firefox()
        self.detail = webdriver.Firefox(profile)
        # This will throw a TimeoutException whenever the page load takes more than 30 seconds.
        self.detail.set_page_load_timeout(30)

    def __del__(self):
        self.main.close()
        self.detail.close()

    def parse(self, response):
        self.main.get("http://c.open.163.com/search/search.htm?query=#/search/video")

        while True:

            hxs = scrapy.Selector(text = self.main.page_source)

            for info in hxs.xpath('//div[@class="cnt"]'):
                tlist = info.xpath('a/@title').extract()
                dlist = info.xpath('p[@class="desc f-c9"]/text()').extract()
                link = info.xpath('a/@href').extract()[0]
                item = Open163ExItem()
                item['title'] = tlist[0].encode('utf-8').replace('"', '“').replace('\n', '') if tlist else u''
                item['link'] = link
                item['piclink'] = info.xpath('a/img/@src').extract()[0]
                item['description'] = dlist[0].encode('utf-8').replace('"', '“').replace('\n', '') if dlist else u''
                item['source'] = u'网易公开课'.encode('utf-8')

                try:
                    self.detail.get(link)
                    time.sleep(2)
                    details = scrapy.Selector(text = self.detail.page_source)
                    item['courselink'] = details.xpath('//div[@class="net-bd"]/video/@src').extract()[0]
                    item['duration'] = u''
                    item['tags'] = details.xpath('//div[@class="net-bd"]/div[7]/p/text()').extract()[0].encode('utf-8')
                    item['language'] = details.xpath('//div[@class="net-bd"]/div[6]/p/text()').extract()[0].encode('utf-8')
                    item['instructor'] = details.xpath('//div[@class="net-bd"]/div[5]/p/text()').extract()[0].encode('utf-8')
                    yield item

                except Exception as err:
                    print(err)
            next = self.main.find_element_by_xpath('//div[@class="j-list"]/div[2]/div/a[11]')

            try:
                #next.click()
                ActionChains(self.main).move_to_element(next).click().perform()
                time.sleep(5)
            except KeyboardInterrupt:
                sys.exit(0)
            except Exception as err:
                print(err)




