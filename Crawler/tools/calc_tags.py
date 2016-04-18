#提取tags并计算出现次数

import json
from pprint import pprint
import operator
d = {}
input = open(r"C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json", "r", encoding="utf-8")
output = open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\tags.json', 'w', encoding='utf-8')
jsObj = json.load(input)
for item in jsObj:
    tags = item['tags'].split()
    for key in tags:
        if key in d:
            d[key] += 1
        else:
            d[key] = 1
print(str(d))
sorted_d = sorted(d.items(), key=operator.itemgetter(1), reverse=True)
output_d = {}
print(str(sorted_d))
for pair in sorted_d:
    output_d[pair[0]] = pair[1]
print(str(output_d))
#output.write(json.dumps(d,ensure_ascii=False))
output.write(json.dumps(sorted_d,ensure_ascii=False))