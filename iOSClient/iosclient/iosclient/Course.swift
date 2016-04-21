//
//  Course.swift
//  iosclient
//
//  Created by 典 杨 on 16/3/30.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

struct Course {
    var id: String?
    var title: String?
    var description: String?
    var rating: Int
    var link: String?
    
    init(id: String?, title: String?, description:String?, rating:Int, link:String?){
        self.id = id
        self.title = title
        self.description = description
        self.rating = rating
        self.link = link
    }
}
