# 分析items.json
import json

input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

count = 0
for item in items:
    if item['enabled'] and 'posted' in item.keys() and item['posted'] == '':
        #item['enabled'] = False
        count += 1

print(count)


