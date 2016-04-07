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
    
    @IBOutlet weak var userImg: UIImageView!
    @IBOutlet weak var userNickname: UILabel!
    
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
                    self.user.openid = jsonResult["openid"] as? String
                    self.user.nickname = jsonResult["nickname"] as? String
                    self.user.sex = jsonResult["sex"] as? String
                    self.user.province = jsonResult["province"] as? String
                    self.user.city = jsonResult["city"] as? String
                    self.user.country = jsonResult["country"] as? String
                    self.user.headimgurl = jsonResult["headimgurl"] as? String
                }catch _ {
                    print("Error in json")
                }
                
                //Update user profile to Controller
                if(self.user.headimgurl != nil){
                    self.userNickname.text = self.user.nickname
                    let url = NSURL(string: self.user.headimgurl!)
                    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) {
                        let data = NSData(contentsOfURL: url!) //make sure your image in this url does exist, otherwise unwrap in a if let check
                        dispatch_async(dispatch_get_main_queue(), {
                            self.userImg.image = UIImage(data: data!)
                        });
                    }
                    self.userImg.layer.borderWidth = 2
                    self.userImg.layer.masksToBounds = false
                    self.userImg.layer.borderColor = UIColor.whiteColor().CGColor
                    self.userImg.layer.cornerRadius = self.userImg.frame.height/2
                    self.userImg.clipsToBounds = true
                    
                }
                //Build json file
                var userProfile = [String: AnyObject]()
                userProfile["openid"] = self.user.openid
                userProfile["nickname"] = self.user.nickname
                userProfile["sex"] = Int(self.user.sex!)
                userProfile["province"] = self.user.province
                userProfile["city"] = self.user.city
                userProfile["country"] = self.user.country
                userProfile["headimgurl"] = self.user.headimgurl
                
                //userProfile["_id"] = Int(self.user.userid!)
                //userProfile["deviceid"] = self.user.deviceid

                //Send user profile to server
                let url = "http://jieko.cc/user" + self.user.userid!
                let request = NSMutableURLRequest(URL: NSURL(string: url)!)
                request.HTTPMethod = "POST"
                request.addValue("application/json", forHTTPHeaderField: "Content-Type")
                
                do{
                    request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(userProfile, options: [])
                    let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                        guard error == nil && data != nil else {                                                          // check for fundamental networking error
                            print("error=\(error)")
                            return
                        }
                        
                        if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {           // check for http errors
                            print("statusCode should be 200, but is \(httpStatus.statusCode)")
                            print("response = \(response)")
                        }
                        
                        let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
                        print("responseString = \(responseString)")
                    }
                    task.resume()
                } catch _{
                    print("Eror json")
                }
                
                //Write to local file
                let documentsDirectoryPathString = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true).first!
                let documentsDirectoryPath = NSURL(string: documentsDirectoryPathString)!
                let jsonFilePath = documentsDirectoryPath.URLByAppendingPathComponent("userProfile.json")
                
                do {
                    let file = try NSFileHandle(forWritingToURL: jsonFilePath)
                    do {
                        let jsonData = try NSJSONSerialization.dataWithJSONObject(userProfile, options: NSJSONWritingOptions.PrettyPrinted)
                        file.writeData(jsonData)
                    } catch let error as NSError {
                        print(error)
                    }
                    print("JSON data was written to teh file successfully!")
                } catch let error as NSError {
                    print("Couldn't write to file: \(error.localizedDescription)")
                }
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
