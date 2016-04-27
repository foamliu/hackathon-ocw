//
//  PopoverViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/23.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import Foundation

class PopoverViewController: UIViewController, TagListViewDelegate {
    
    var tags: String!
    var source: String!
    var courseId: Int!
    var indexPath: NSIndexPath!
    var tagsArray: NSMutableArray = []
    
    @IBOutlet weak var disliketagListView: TagListView!
    @IBOutlet weak var dislikeLabel: UILabel!
    @IBOutlet weak var dislikeButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "getDislikeCourse:", name: "newDislikeNotification", object: nil)
        dislikeButtonInit();
        dislikeTagsInit();
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func dislikeButtonInit(){
        dislikeButton.layer.cornerRadius = 15
        
    }
    
    func dislikeTagsInit(){
        disliketagListView.delegate = self
        disliketagListView.textFont = UIFont.systemFontOfSize(12)
        disliketagListView.textColor = UIColor.darkGrayColor()
        disliketagListView.tagBackgroundColor = UIColor.whiteColor()
        disliketagListView.cornerRadius = 5
        disliketagListView.marginX = 8
        disliketagListView.marginY = 8
        disliketagListView.paddingX = 15
        disliketagListView.paddingY = 8
        disliketagListView.borderColor = UIColor.lightGrayColor()
        disliketagListView.borderWidth = 1
        disliketagListView.selectedTextColor = UIColor.redColor()
        
        disliketagListView.selectedTextColor = UIColor.redColor()
        disliketagListView.tagSelectedBackgroundColor = UIColor.whiteColor()
        disliketagListView.selectedBorderColor = UIColor.redColor()
        
        disliketagListView.addTag("重复")
        disliketagListView.addTag("内容质量差")
    }
    
    func getDislikeCourse(notif: NSNotification) {
        let newDislike: NSMutableDictionary = notif.userInfo!["newDislike"] as! NSMutableDictionary
        indexPath = newDislike.valueForKey("indexPath") as? NSIndexPath
        tags = newDislike.valueForKey("tags") as? String
        courseId = newDislike.valueForKey("item_id") as? Int
        source = newDislike.valueForKey("source") as? String
        disliketagListView.addTag("来源:" + source)
        disliketagListView.addTag(tags)
    }
    
    func tagPressed(title: String, tagView: TagView, sender: TagListView) {
        //Search by tags
        //Update the searchText to Table
        tagView.selected = true
        if(tagsArray.containsObject(title)){
            tagsArray.removeObject(title)
        }
        else{
            tagsArray.addObject(title)
        }
        if(tagsArray.count != 0){
            dislikeButton.setTitle("确定", forState: UIControlState.Normal)
            dislikeLabel.text = "已选择" + String(tagsArray.count) + "个理由"
        }
        else{
            dislikeButton.setTitle("不感兴趣", forState: UIControlState.Normal)
            dislikeLabel.text = "可选理由，精准屏蔽"
        }
    }
    
    func sendSelectedCourse(courseId: Int){
        var courseSelected = [String: AnyObject]()
        courseSelected["user_id"] = User.sharedManager.userid
        courseSelected["item_id"] = courseId
        courseSelected["pref"] = 0
        
        let url = "http://jieko.cc/user/" + String(User.sharedManager.userid!) + "/Preferences"
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do{
            request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(courseSelected, options: [])
            let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                guard error == nil && data != nil else {
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
            print("Error json")
        }
    }
    
    
    @IBAction func dislikeBtnClicked(sender: AnyObject) {
        //send remove signal to tableview
        NSNotificationCenter.defaultCenter().postNotificationName("deleteRowNotification", object: nil, userInfo: ["indexPath" : indexPath])
        //send to server
        if(tagsArray.count != 0){
            //TODO: send sever with dislike tags
            sendSelectedCourse(courseId)
        }
        else{
            print(courseId)
            sendSelectedCourse(courseId)
        }
        
        //close
        tagsArray.removeAllObjects()
        self.presentingViewController?.dismissViewControllerAnimated(true, completion: nil)
    }
    
}