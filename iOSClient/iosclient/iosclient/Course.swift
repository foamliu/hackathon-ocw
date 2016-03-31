//
//  Course.swift
//  iosclient
//
//  Created by 典 杨 on 16/3/30.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

struct Course {
    var title: String?
    var description: String?
    var rating: Int
    
    init(title: String?, description:String?, rating:Int){
        self.title = title
        self.description = description
        self.rating = rating
    }
}
