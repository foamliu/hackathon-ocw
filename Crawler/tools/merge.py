#把duration融入items.json
import json
import codecs

def getDuration(durations, link):
    for item in durations:
        if item['link'] == link:
            return item['duration']
    return ''


input_file_1 = open(r'C:\Users\foamliu.FAREAST\Desktop\video_info_1.0_free\NReco.VideoInfo\examples\NReco.VideoInfo.Examples.GetFileInfo\bin\Debug\result.json', "r", encoding="utf-8")
input_file_2 = open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\items.json', "r", encoding="utf-8")
output_file = codecs.open(r'C:\Users\foamliu.FAREAST\Documents\GitHub\hackathon-ocw\FeedAPI\app\assets\jsons\output.json', "w", encoding="utf-8")

items = json.load(input_file_2, encoding='utf-8')
durations = json.load(input_file_1, encoding='utf-8')

count = 0
for item in items:
    if item['source'] == '网易公开课' and item['duration'] == '':
        #item['duration'] = getDuration(durations, item['link'])
        count += 1

print (count)

json.dump(items ,output_file, indent=4,ensure_ascii=False,sort_keys=True)
