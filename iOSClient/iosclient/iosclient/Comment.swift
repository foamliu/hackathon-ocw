//
//  Comment.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/8.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

struct Comment {
    var id: String?
    var item_id: Int?
    var author_id: Int?
    var author_name: String?
    var posted: String?
    var text: String?
    var timeline: String?
    var like: Int? = 0
    
    init(){}
    
    init(id: String?, item_id: Int?, author_id: Int?, author_name: String?, posted: String?, text: String?, timeline: String?, like: Int?){
        self.id = id
        self.item_id = item_id
        self.author_id = author_id
        self.author_name = author_name
        self.posted = posted
        self.text = text
        self.timeline = timeline
        self.like = like
    }
}
