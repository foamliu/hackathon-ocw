//
//  User.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/7.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

struct User {
    var userid: String?
    var openid: String?
    var deviceid: String?
    var nickname: String?
    var sex: String?
    var city: String?
    var province: String?
    var country: String?
    var headimgurl: String?
    
    init(){}
       
    init(userid: String?, openid:String?, deviceid:String?, nickname:String?, sex:String?, city:String?, province:String?, country:String?, headimgurl:String?){
        self.userid = userid
        self.openid = openid
        self.deviceid = deviceid
        self.nickname = nickname
        self.sex = sex
        self.city = city
        self.province = province
        self.country = country
        self.headimgurl = headimgurl
    }
    
}
