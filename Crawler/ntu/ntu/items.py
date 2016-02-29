# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class NtuItem(scrapy.Item):
    debug = scrapy.Field()
    title = scrapy.Field()
    author = scrapy.Field()
    piclink = scrapy.Field()
    courselink = scrapy.Field()
    description = scrapy.Field()
