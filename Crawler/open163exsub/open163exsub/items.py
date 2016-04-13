# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class Open163ExSubItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    item_id = scrapy.Field()
    title = scrapy.Field()
    description = scrapy.Field()
    courselink = scrapy.Field()
    piclink = scrapy.Field()
    duration = scrapy.Field()
    source = scrapy.Field()
    tags = scrapy.Field()
    language = scrapy.Field()
    instructor = scrapy.Field()
