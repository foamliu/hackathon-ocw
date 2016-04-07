//
//  MenuController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/6.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import SVProgressHUD

class MenuController: UITableViewController {
    
    let kWXAPP_ID: String = "wx9b493c5b54472578"
    let kWXAPP_SECRET: String = "211b995337b10a7ef9c32d511e7c4576"
    var user = User()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "onRecviceWX_CODE_Notification:", name: "WX_CODE", object: nil)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    @IBAction func loginBtn(sender: AnyObject) {
        sendWXAuthRequest()
    }
    
    func sendWXAuthRequest(){
        let req : SendAuthReq = SendAuthReq()
        req.scope = "snsapi_userinfo,snsapi_base"
        req.state = "123"
        WXApi.sendReq(req)
    }
    
    //微信回调通知,获取code 第二步
    func onRecviceWX_CODE_Notification(notification:NSNotification){
        //SVProgressHUD.showSuccessWithStatus("获取到code", duration: 1)
        var userinfoDic : Dictionary = notification.userInfo!
        let code: String = userinfoDic["code"] as! String
        print("Recevice Code: \(code)")
        self.getAccess_token(code)
    }
    
    //获取token 第三步
    func getAccess_token(code :String){
        let requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=\(kWXAPP_ID)&secret=\(kWXAPP_SECRET)&code=\(code)&grant_type=authorization_code"
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            let requestURL: NSURL = NSURL(string: requestUrl)!
            let data = NSData(contentsOfURL: requestURL)
            
            dispatch_async(dispatch_get_main_queue(), {
                
                do{
                    let jsonResult: NSDictionary = try NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers) as! NSDictionary
                    
                    let token: String = jsonResult["access_token"] as! String
                    let openid: String = jsonResult["openid"] as! String
                    self.getUserInfo(token, openid: openid)
                }catch _ {
                    print("Error in Receive Token")
                }
            })
        })
    }
    
    //获取用户信息 第四步
    func getUserInfo(token :String,openid:String){
        let requestUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=\(token)&openid=\(openid)"
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), {
            let requestURL: NSURL = NSURL(string: requestUrl)!
            let data = NSData(contentsOfURL: requestURL)
            
            dispatch_async(dispatch_get_main_queue(), {
                do{
                    let jsonResult: NSDictionary = try NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers) as! NSDictionary
                    print("Recevice UserInfo: \(jsonResult)")
                    user.openid = jsonResult["openid"]
                    user.nickname = jsonResult["nickname"]
                    user.sex = jsonResult["sex"]
                    user.province = jsonResult["province"]
                    user.city = jsonResult["city"]
                    user.country = jsonResult["country"]
                    user.headimgurl = jsonResult["headimgurl"]
                }catch _ {
                    print("Error in json")
                }
                
                
                //SVProgressHUD.showSuccessWithStatus("获取到用户信息", duration: 1)
                
                //let headimgurl: String = jsonResult["headimgurl"] as! String
                //let nickname: String = jsonResult["nickname"] as! String
                
                //self.headerImg.sd_setImageWithURL(NSURL(string: headimgurl))
                //self.nicknameLbl.text = nickname
            })
        })
    }
    
    

    /*
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("reuseIdentifier", forIndexPath: indexPath) as UITableViewCell
        return cell
    }
 
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
 */


}
