#合并历次爬取结果并去重

import json
from pprint import pprint
from PageDataLinklast import PageDataLinklast
from PageDataLink import PageDataLink

file = open(r"C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\Crawler\open163ex\out.json", "r", encoding="utf-8")
output = open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\Crawler\open163ex\links.json', 'w', encoding='utf-8')
links = []
'''lines = file.readlines()

output.close()'''

items = json.load(file)

for item in items:
    links.append(item["link"])

for link in PageDataLinklast().page:
    links.append(link)

for link in PageDataLink().page:
    links.append(link)
print(len(links))
links=list(set(links))
print(len(links))
jsonList = {'links':[]}
for link in links:
    jsonList['links'].append(link)

output.write(json.dumps(jsonList))


