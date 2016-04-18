#测试是否有重复，去重

import json

file = open(r"C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json", "r", encoding="utf-8")
titles=[]
items = json.load(file)
for item in items:
    titles.append(item["title"])

print(len(titles))
titles=list(set(titles))
print(len(titles))


'''seen = set()
for x in titles:
    if x not in seen:
        seen.add(x)
    else:
        print(x)'''