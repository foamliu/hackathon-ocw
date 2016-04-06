//
//  MenuController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/6.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

class MenuController: UITableViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
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
    
    func onResp(resp: BaseResp!) {
        
        /*
         ErrCode  ERR_OK = 0(用户同意)
         ERR_AUTH_DENIED = -4（用户拒绝授权）
         ERR_USER_CANCEL = -2（用户取消）
         code 用户换取access_token的code，仅在ErrCode为0时有效
         state    第三方程序发送时用来标识其请求的唯一性的标志，由第三方程序调用sendReq时传入，由微信终端回传，state字符串长度不能超过1K
         lang 微信客户端当前语言
         country  微信用户当前国家信息
         */
        let aresp = resp as! SendAuthResp
        if (aresp.errCode == 0)
        {
            print(aresp.code)
            var dic:Dictionary<String,String>=["code":aresp.code];
            let value = dic["code"]
            print("code:\(value)")
            NSNotificationCenter.defaultCenter().postNotificationName("WX_CODE", object: nil, userInfo: dic)
        }  
    }
    
    //微信回调通知,获取code 第二步
    
    func onRecviceWX_CODE_Notification(notification:NSNotification)
    {
        SVProgressHUD.showSuccessWithStatus("获取到code", duration: 1)
        var userinfoDic : Dictionary = notification.userInfo!
        let code: String = userinfoDic["code"] as! String
        print("Recevice Code: \(code)")
        self.getAccess_token(code)
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
