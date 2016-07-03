# 把爬下内容融合到items.json中
import json
import codecs
import io
from pprint import pprint

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
    for i in range(0, len(items)-1):
        if items[i]['link'] == link:
            return i
    return -1

input_file_1 = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\Crawler\infoq\out.json', "r", encoding="utf-8")
input_file_2 = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file_2, encoding='utf-8')

lines = input_file_1.readlines()
i = 36901
for line in lines:
    line = line.replace('\\','\\\\')
    #print(line)
    item = json.loads(line)
    #print(item)
    pos = getPos(items, item['link'])
    if pos == -1:
        item['item_id'] = i
        item['duration'] = ''
        item['enabled'] = True
        items.append(item)
        i += 1
    else:
        item['item_id'] = items[pos]['item_id']
        item['duration'] = items[pos]['duration']
        item['enabled'] = items[pos]['enabled']
        item['tags'] = items[pos]['tags']
        items[pos] = item


json.dump(items ,output_file, indent=4,ensure_ascii=False,sort_keys=True)