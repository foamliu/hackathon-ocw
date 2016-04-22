#批量调整标签
import json
import codecs

input_file = open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
#output_file = codecs.open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

count = 0
for item in items:
    #item['tags'] = item['tags'].replace('计算机', '互联网')
    #item['tags'] = item['tags'].replace('国际名校公开课', '').strip()
    #item['tags'] = item['tags'].replace('中国大学视频公开课 国内', '').strip()
    #item['tags'] = item['tags'].replace('可汗学院', '').strip()
    #item['tags'] = item['tags'].replace('大气', '地球科学').strip()
    #item['tags'] = item['tags'].replace('InfoQ', '').strip()
    #item['tags'] = item['tags'].replace('赏课', '').strip()
    #item['tags'] = item['tags'].replace('  ', ' ').strip()
    if '国内' in item['tags']:
        print(item['item_id'])
        print(item['title'])
        count += 1

print (count)
#技能 地球科学 建筑 医学 社会 生物 物理 教育 艺术 建筑 历史 机器人学 教育 经济 天文 法律 演讲 教育
#json.dump(items ,output_file, indent=4,ensure_ascii=False,sort_keys=True)
#http://imgsize.ph.126.net/?enlarge=true&amp;imgurl=http://img4.cache.netease.com/video/2013/9/4/20130904155017c21a3.jpg_280x158x1x95.jpg