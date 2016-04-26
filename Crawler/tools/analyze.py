# 分析items.json
import json

input_file = open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

count = 0
for item in items:
    if item['source'] == '网易公开课' and item['courselink'] == '':
        #item['duration'] = getDuration(durations, item['link'])
        count += 1

print(count)


