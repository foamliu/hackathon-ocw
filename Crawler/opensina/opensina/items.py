# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class OpensinaItem(scrapy.Item):
    # define the fields for your item here like:
    title = scrapy.Field()        #名称
    description = scrapy.Field()  #简介
    piclink = scrapy.Field()      #图片地址
    courselink = scrapy.Field()   #课程地址
    source = scrapy.Field()       #来源
    school = scrapy.Field()       #学校
    instructor = scrapy.Field()   #讲师
    language = scrapy.Field()     #授课语言
    tags = scrapy.Field()         #类型
    link = scrapy.Field()         #链接，用于关联。
