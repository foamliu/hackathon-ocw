//
//  User.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/7.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

class User {
    var userid: Int? = 0
    var openid: String?
    var deviceid: String?
    var nickname: String?
    var sex: Int?
    var city: String?
    var province: String?
    var country: String?
    var headimgurl: String?
    
    class var sharedManager: User {
        struct Static {
            static let instance = User()
        }
        return Static.instance
    }
    
    init(){}
       
    init(userid: Int?, openid:String?, deviceid:String?, nickname:String?, sex:Int?, city:String?, province:String?, country:String?, headimgurl:String?){
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
