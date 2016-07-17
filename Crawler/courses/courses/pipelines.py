# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html
import pymongo


class MongoDBPipeline(object):
    def __init__(self, mongo_uri, mongo_port, mongo_db, mongo_collection):
        self.mongo_uri = mongo_uri
        self.mongo_port = mongo_port
        self.mongo_db = mongo_db
        self.collection_name = mongo_collection

    @classmethod
    def from_crawler(cls, crawler):
        return cls(
            crawler.settings.get('MONGODB_URI'),
            crawler.settings.get('MONGODB_PORT', 27017),
            crawler.settings.get('MONGODB_DB'),
            crawler.settings.get('MONGODB_COLLECTION'),
        )

    def open_spider(self, spider):
        self.client = pymongo.MongoClient(self.mongo_uri, self.mongo_port)
        self.db = self.client[self.mongo_db]

    def close_spider(self, spider):
        self.client.close()
        pass

    def process_item(self, item, spider):
        # self.db[self.collection_name].find_and_modify()
        item_dic = dict(item)
        self.db[self.collection_name].update(
            {"url": item_dic['url']},
            {'$set': item_dic},
            upsert=True
        )
        # self.db[self.collection_name].insert(item_dic)
        return item
