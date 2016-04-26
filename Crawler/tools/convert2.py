# 转换 items.json
import json
import codecs

input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

for item in items:
    if item['source'] == '新浪公开课':
        item['enabled'] = False
    else:
        item['enabled'] = True

json.dump(items, output_file, indent=4, ensure_ascii=False, sort_keys=True)