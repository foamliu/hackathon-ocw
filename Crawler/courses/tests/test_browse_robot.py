# -*- coding: utf-8 -*-
from courses.util import BrowserRobot

from courses.spiders import open163


def test_robot_load():
    robot = BrowserRobot(
        {"general.useragent.override": open163.MOBILE_USER_AGENT})
    try:
        urls = [
            "http://open.163.com/movie/2016/7/H/G/MBQP74779_MBQPAMTHG.html",
            "http://open.163.com/special/Khan/mindteasers.html"]
        resp1 = robot.load(urls[0])
        ret = resp1.xpath('//div[contains(@class,"video_info")]/@data-title').extract()
        assert len(ret) == 1
        assert ret[0].encode('utf-8') == '雪花中的宇宙'
    finally:
        robot.stop()


def test_robot_explore():
    def get_items(response):
        return response.xpath('//div[@class="cnt"]/a/@href').extract()

    with BrowserRobot() as robot:
        for item in robot.explore(open163.OPEN163_SEARCH_URL,
                                  get_items, open163.OPEN163_NEXT_XPATH, 2):
            if isinstance(item, list):
                for sub in item:
                    print(unicode(sub).encode('utf_8'))
            else:
                print(unicode(item).encode('utf_8'))
