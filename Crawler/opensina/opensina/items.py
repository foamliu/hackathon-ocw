# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class OpensinaItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    title = scrapy.Field()
    description = scrapy.Field()
    piclink = scrapy.Field()
    courselink = scrapy.Field()
    duration = scrapy.Field()
    source = scrapy.Field()
    templink = scrapy.Field()
