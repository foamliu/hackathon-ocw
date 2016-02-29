# -*- coding: utf-8 -*-

import json
import sys

def readocwJson():
    inFile = open("items.json",'r',0)
    text = inFile.read()
    ocw_dict = json.loads(text)
    
    reload(sys) 
    sys.setdefaultencoding("utf-8") 
    output = sys.stdout
    outputfile = open("result.txt", 'w')
    sys.stdout = outputfile


    for ocw in ocw_dict:
        title = ocw["title"][0]
        author = ocw["author"][0]
        piclink = ocw["piclink"][0]
        courselink = ocw["courselink"][0]
        description = ocw["description"][0]
               
        print "title ".decode("utf-8") + title
        print "author ".decode("utf-8") + author
        print "piclink ".decode("utf-8") + piclink
        print "courselink ".decode("utf-8") + courselink
        print "description ".decode("utf-8") + description
        print
    
    outputfile.close()
    sys.stdout = output

readocwJson()
