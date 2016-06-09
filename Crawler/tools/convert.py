# 把爬下内容融合到items.json中
import json
import codecs
import copy
import io
from pprint import pprint
from pymongo import MongoClient

def get_duration(olist, url):
    for item in olist:
        if (item['courselink']) == url: return item['duration']
    return ''

#input_file = open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r")
#output_file = codecs.open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

def downloaded(items, link):
    for item in items:
        if item['link'] == link:
            return True
    return False

def getPos(items, link):
    for i in range(0, len(items)):
        if items[i]['link'] == link:
            return i
    return -1

root_path = 'C:\\Users\\Foam\\Documents\\GitHub\\hackathon-ocw\\'
sources = ['guokr', 'infoq_article', 'open163exsub', 'yixi']
items_file = open(root_path + 'FeedAPI\\app\\assets\\jsons\\items.json', "r", encoding="utf-8")
items = json.load(items_file, encoding='utf-8')
i = 1;
if (len(items) > 0) : i = items[len(items) - 1]['item_id'] + 1

client = MongoClient('192.168.0.1', 27017);
db = client['jiekodb'];
courses = db['course']

for source in sources:
	source_file_name = 'out.json'
	source_file = open(root_path + 'Crawler\\' + source + '\\' + source_file_name, "r", encoding="utf-8")
	lines = source_file.readlines()
	for line in lines:
		line = line.replace('\\','\\\\')
		#print(line)
		try:
			item = json.loads(line)
		except ValueError:
			print("Decode json error in source : " + source)
		else:
			#print(item)
			pos = getPos(items, item['link'])
			if pos == -1:
				item['item_id'] = i
				item['duration'] = ''
				item['enabled'] = True
				items.append(item)
				document = copy.deepcopy(item)
				courses.insert_one(document)
				i += 1
			else:
				item['item_id'] = items[pos]['item_id']
				item['duration'] = items[pos]['duration']
				item['enabled'] = items[pos]['enabled']
				item['tags'] = items[pos]['tags']
				items[pos] = item
				courses.find_one_and_replace({'link' : item['link']}, item)

client.close()
output_file = codecs.open(root_path + "FeedAPI\\app\\assets\\jsons\\output.json", "w", encoding="utf-8")
json.dump(items ,output_file, indent=4,ensure_ascii=False,sort_keys=True)