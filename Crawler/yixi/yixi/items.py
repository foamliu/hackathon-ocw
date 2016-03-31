# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class YixiItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    title = scrapy.Field()
    description = scrapy.Field()
    pic_link = scrapy.Field()
    item_link = scrapy.Field()
    speaker_name = scrapy.Field()
    speaker_description = scrapy.Field()
    source = scrapy.Field()
