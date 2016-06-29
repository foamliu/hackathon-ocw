# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class Open163Ex2Item(scrapy.Item):
    # define the fields for your item here like:
    coursetitle = scrapy.Field()        #名称
    coursedescription = scrapy.Field()  #简介
    coursepiclink = scrapy.Field()      #图片地址
    courseinstructor = scrapy.Field()   #讲师
    courselink = scrapy.Field()
    items = scrapy.Field()


