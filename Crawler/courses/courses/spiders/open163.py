# -*- coding: utf-8 -*-
import datetime
import re

import scrapy
from scrapy.loader.processors import MapCompose, TakeFirst
from scrapy.loader import ItemLoader
from scrapy.http import Request

from courses.items import CourseItem
from courses.util import BrowserRobot, uniq

OPEN163_SEARCH_URL = "http://c.open.163.com/search/search.htm?enc=#/search/course"
OPEN163_NEXT_XPATH = '//div[@class="j-list"]/div[2]/div/a[text()=""]'
COURSE_SOURCE = u'网易公开课'.encode('utf-8')
MOBILE_USER_AGENT = 'Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/BuildID) \
AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36'


class Open163Spider(scrapy.Spider):
    name = "open163"
    allowed_domains = ["open.163.com"]
    start_urls = []

    abs_info_key = {
        u'学校：': 'school',
        u'制片国家/地区：': 'school',
        u'讲师：': 'instructor',
        u'授课语言：': 'language',
        u'类型：': 'tags',
        u'课程简介：': 'description'
    }
    abs_re = re.compile(r'<p><span class="f-c9">(?P<key>[^<]+)</span>(?P<value>[^<]+)</p>')

    def __init__(self):
        super(Open163Spider, self).__init__()

        self.start_urls = self._explore163()
        self.mobile_browser = BrowserRobot({"general.useragent.override": MOBILE_USER_AGENT})

    def closed(self, reason):
        print("Spider {} exiting with reason:{}".format(self.name, reason))
        self.mobile_browser.stop()
        pass

    @staticmethod
    def _explore163():
        def grab_course_urls(response):
            return response.xpath('//div[@class="cnt"]/a/@href').extract()

        with BrowserRobot() as robot:
            pages = list(robot.explore(
                OPEN163_SEARCH_URL,
                grab_course_urls,
                OPEN163_NEXT_XPATH))
            # the returned are actually list of list of links on each page
            all_links = []
            for page in pages:
                all_links.extend(page)
            return uniq(all_links)

    def parse(self, response):
        # Click to get the next batch
        content_type = response.url.split('/')[3]
        if content_type == "movie":
            yield self.parse_item(response)
        elif content_type == "special":
            sub_links = response.xpath('//*[@id="list1"]//a/@href').extract()
            for sub in uniq(sub_links):
                yield Request(sub)
        else:
            raise NotImplemented("Could not parse content:{}. url:{}"
                                 .format(content_type, response.url))

    def parse_item(self, response):
        """ This function parses a course page. To get mobile video link,
        leverage selenium to reload page
        """
        # get abstract information from original normal response
        abs_info_raw = response.xpath('//div[@class="g-sd"]//p').extract()
        kvs = {}
        for m in [self.abs_re.search(item) for item in abs_info_raw]:
            if not m:
                continue
            kvs[m.group('key')] = m.group('value')

        # Create the loader using the response
        il = ItemLoader(item=CourseItem(),
                        selector=self.mobile_browser.load_until(
                            response.url,
                            '//div[contains(@class,"video_info")]'))
        il.default_output_processor = TakeFirst()
        # just get the first link which is for hd mp4
        il.add_xpath('title', '//div[contains(@class,"video_info")]/@data-title',
                     MapCompose(unicode.strip))
        # il.add_xpath('description', '//div[contains(@class,"video_info")]/@data-desc')
        il.add_xpath('piclink', '//div[contains(@class,"video_info")]/@data-img')
        il.add_xpath('courselink', '//div[@class="video-wrapper"]/video/source/@src')

        # Housekeeping fields
        il.add_value('source', COURSE_SOURCE)
        il.add_value('crawled', datetime.datetime.now())
        il.add_value('url', response.url)

        item = il.load_item()
        # print item
        # post-processing
        if item['courselink'] == u'http://mov.bn.netease.com/movie/nofile/list.mp4':
            item['courselink'] = u''
        item['title'] = item['title'].split('_')[-1]
        item['posted'] = self._parse_posted_date_from_link(item['courselink'] or item['url'])
        for k in kvs:
            if k in self.abs_info_key:
                item[self.abs_info_key[k]] = cleanse(kvs[k])
        return item

    @staticmethod
    def _parse_posted_date_from_link(link):
        """
        link could be one of the following format:
        1. http://mov.bn.netease.com/open-movie/nos/mp4/2016/07/15/SBR31IJCE_sd.mp4
        2. http://open.163.com/movie/2016/6/4/3/MBPB3H8DC_MBPB3M243.html
        :param link:
        :return:
        """
        ret = re.search(r'\d{4}/\d{1,2}/\d{0,2}', link)
        if not ret:
            return '1990-01-01'
        else:
            return ret.group(0).strip('/').replace('/', '-')


def cleanse(words):
    return words.strip().encode('utf-8').replace('"', '“') \
        .replace('\n', '').replace('\t', '    ') \
        .replace('\\', '“') if words else u''
