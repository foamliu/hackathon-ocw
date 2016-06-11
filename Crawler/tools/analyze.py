# 分析items.json
import json
import codecs

input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

count = 0
for item in items:
    #if item['enabled'] and 'posted' in item.keys() and item['posted'] == '':
    if item['enabled'] and item['courselink'] == 'http://mov.bn.netease.com/movie/nofile/list.mp4':
        item['courselink'] = ''
        alist = item['link'].split("/")
        item['posted'] = alist[4] + '-' + alist[5]
        #print(item)
        count += 1

print(count)
json.dump(items ,output_file, indent=4,ensure_ascii=False,sort_keys=True)


