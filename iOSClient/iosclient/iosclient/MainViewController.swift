//
//  MainViewController.swift
//  iosclient
//
//  Created by 杨典 on 16/6/19.
//  Copyright © 2016年 星群. All rights reserved.
//

import UIKit
import Alamofire
import SDWebImage

class MainViewController: UIViewController,UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet var tableView : UITableView!
  
    var courses: NSMutableArray = []
    
    var isInternetConnected = true
    var clearCourses : Bool = false

    override func viewDidLoad() {
        super.viewDidLoad()
        checkInternetConnection()
        
        if(isInternetConnected == true){
            getInitId()
        }

        self.title = "学啥"

        // Register custom cell
        let nib = UINib(nibName:"vwTblCell", bundle:nil)
        self.tableView.registerNib(nib, forCellReuseIdentifier: "CourseCell")
    }
    
    func checkInternetConnection(){
        if Reachability.isConnectedToNetwork() == true {
            print("Internet connection OK")
        } else {
            print("Internet connection FAILED")
        }
        //If the user is not connected to the internet, you may want to show them an alert dialog to notify them.
        if Reachability.isConnectedToNetwork() == true {
            isInternetConnected = true
            print("Internet connection OK")
        } else {
            self.navigationItem.leftBarButtonItem = nil;
            isInternetConnected = false
            print("Internet connection FAILED")
            let alert = UIAlertView(title: "没有网络连接", message: "请确认您已连接到互联网", delegate: nil, cancelButtonTitle: "OK")
            alert.show()
        }
    }
    
    func getInitId(){
        //使用deviceid换取userid
        User.sharedManager.deviceid = UIDevice.currentDevice().identifierForVendor!.UUIDString
        //检查本地userProfile文件
        let documentsDirectoryPathString = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true).first!
        let documentsDirectoryPath = NSURL(string: documentsDirectoryPathString)!
        let filePath = String(documentsDirectoryPath.URLByAppendingPathComponent("userProfile.json"))
        do{
            let fileContent = try NSString(contentsOfFile: filePath, encoding: NSUTF8StringEncoding)
            let json = try NSJSONSerialization.JSONObjectWithData(fileContent.dataUsingEncoding(NSUTF8StringEncoding)!, options: [])
            let userid = json["_id"] as! Int
            User.sharedManager.userid = userid
        }catch let error as NSError{
            print(error)
            getInitFromServer()
        }
    }
    
    func getInitFromServer(){
        var basicProfile = [String: AnyObject]()
        basicProfile["deviceid"] = User.sharedManager.deviceid
        let url = "http://jieko.cc/user"
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do{
            request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(basicProfile, options: [])
            let task = NSURLSession.sharedSession().dataTaskWithRequest(request, completionHandler: { data, response, error -> Void in
                guard error == nil && data != nil else {                                                          // check for fundamental networking error
                    print("error=\(error)")
                    return
                }
                
                if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {           // check for http errors
                    print("statusCode should be 200, but is \(httpStatus.statusCode)")
                    print("response = \(response)")
                }
                
                do {
                    let result = try NSJSONSerialization.JSONObjectWithData(data!, options: []) as? [String: Int]
                    User.sharedManager.userid = result!["userid"]
                    self.jsonParsingFromUrl()
                } catch let error as NSError {
                    print(error)
                }
                
            })
            task.resume()
        } catch _{
            print("Error json")
        }
    }
    
    func jsonParsingFromUrl(){
        Alamofire.request(.GET, "http://api.jieko.cc/user/" + String(User.sharedManager.userid!) + "/Candidates").responseJSON { response in
            if response.result.isSuccess {
                self.startParsingCourses(response.result.value!["courses"] as! NSArray)
            }
        }
    }
    
    func startParsingCourses(data: NSArray){
        if (clearCourses == true && data.count > 1){
            courses.removeAllObjects()
            clearCourses = false
        }
        if (data.count > 1){
            for i in 0...(data.count - 1){
                courses.addObject(data.objectAtIndex(i))
            }
        }
        dispatch_async(dispatch_get_main_queue(), {
            
            // DO SOMETHING ON THE MAINTHREAD
            self.tableView.reloadData()
        })
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return self.courses.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell: MainTableViewCell = self.tableView.dequeueReusableCellWithIdentifier("CourseCell") as! MainTableViewCell
        
        cell.lblTitle.text = courses[indexPath.row].valueForKey("title") as? String
        
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        print("You selected cell #\(indexPath.row)!")
    }


}

