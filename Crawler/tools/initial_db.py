# initial cources collection
import json
import codecs
import io
from pymongo import MongoClient

root_path = 'C:\\Users\\Foam\\Documents\\GitHub\\hackathon-ocw\\'
items_file = open(root_path + 'FeedAPI\\app\\assets\\jsons\\items.json', "r", encoding="utf-8")
items = json.load(items_file, encoding='utf-8')

client = MongoClient('192.168.56.101', 27017);
db = client['jiekodb'];
courses = db['course']
courses.drop()
courses.insert_many(items)
courses.create_index("link");
client.close()