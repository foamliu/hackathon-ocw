# 转换 items.json
import json
import codecs

input_file = open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\Foam\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file, encoding='utf-8')

def getMonth(monthInChinese):
    if (monthInChinese == '一月'):
        return '01'
    if (monthInChinese == '二月'):
        return '02'
    if (monthInChinese == '三月'):
        return '03'
    if (monthInChinese == '四月'):
        return '04'
    if (monthInChinese == '五月'):
        return '05'
    if (monthInChinese == '六月'):
        return '06'
    if (monthInChinese == '七月'):
        return '07'
    if (monthInChinese == '八月'):
        return '08'
    if (monthInChinese == '九月'):
        return '09'
    if (monthInChinese == '十月'):
        return '10'
    if (monthInChinese == '十一月'):
        return '11'
    if (monthInChinese == '十二月'):
        return '12'
    return '00'

for item in items:
    if item['source'] == 'InfoQ':
        print (item['posted'])
        if '年' in item['posted']:
            item['posted'] = item['posted'].replace('年','-').replace('月','-').replace('日','')
        else:
            alist = item['posted'].split(" ")
            item['posted'] = alist[0] + '-' + getMonth(alist[1]) + '-' + alist[2]

json.dump(items, output_file, indent=4, ensure_ascii=False, sort_keys=True)