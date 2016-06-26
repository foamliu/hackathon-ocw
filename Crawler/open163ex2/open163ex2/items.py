# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class Open163Ex2Item(scrapy.Item):
    # define the fields for your item here like:
    title = scrapy.Field()        #名称
    description = scrapy.Field()  #简介
    piclink = scrapy.Field()      #图片地址
    instructor = scrapy.Field()   #讲师
    link = scrapy.Field()
    items = scrapy.Field()


