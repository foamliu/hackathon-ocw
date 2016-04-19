# 把爬下内容融合到items.json中
import json
import codecs
import io
from pprint import pprint

def get_duration(olist, url):
    for item in olist:
        if (item['courselink']) == url: return item['duration']
    return ''

#input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\Crawler\opensina\items.json', "r")
#output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\Crawler\opensina\output.json', "w", encoding="utf-8")

input_file_1 = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\Crawler\open163exsub\out.json', "r", encoding="utf-8")
input_file_2 = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\Crawler\open163exsub\output.json', "w", encoding="utf-8")

olist = json.load(input_file_2, encoding='utf-8')
output = []
lines = input_file_1.readlines()
i = 1
for line in lines:
    item = json.loads(line)
    item['item_id'] = i
    item['duration'] = get_duration(olist, item['courselink'])
    output.append(item)
    i += 1


json.dump(output ,output_file, indent=4,ensure_ascii=False)

