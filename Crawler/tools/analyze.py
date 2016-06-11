# 分析items.json
import json
import codecs

input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

count = 0
for item in items:
    if item['enabled'] and ('posted' not in item.keys() or item['posted'] == ''):
        print(item)
        count += 1

print(count)
#json.dump(items ,output_file, indent=4,ensure_ascii=False,sort_keys=True)


