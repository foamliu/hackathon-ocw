# 转换 items.json
import json
import codecs

input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

for item in items:
    if item['source'] == '网易公开课' and item['courselink'] != '' and item['courselink'] != 'http://mov.bn.netease.com/movie/nofile/list.mp4':
        print (item['courselink'])
        alist = item['courselink'].split("/")
        if (item['courselink'].startswith('http://mov.bn.netease.com/movie/')):
            item['posted'] = alist[4] + '-' + alist[5]
        else:
            item['posted'] = alist[6]+'-'+alist[7]+'-'+alist[8]


json.dump(items, output_file, indent=4, ensure_ascii=False, sort_keys=True)