//
//  PopoverViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/23.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import Foundation
import TagListView

class PopoverViewController: UIViewController, TagListViewDelegate {
    
    var tags: String!
    var source: String!
    var indexPath: NSIndexPath!
    
    @IBOutlet weak var disliketagListView: TagListView!
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
        disliketagListView.tagSelectedBackgroundColor = UIColor(red: 1, green: 0, blue: 0, alpha: 0.3)
    
        disliketagListView.addTag("重复")
        disliketagListView.addTag("内容质量差")
    }
    
    //Handler
    func getDislikeCourse(notif: NSNotification) {
        let newDislike: NSMutableDictionary = notif.userInfo!["newDislike"] as! NSMutableDictionary
        indexPath = newDislike.valueForKey("indexPath") as? NSIndexPath
        tags = newDislike.valueForKey("tags") as? String
        source = newDislike.valueForKey("source") as? String
        disliketagListView.addTag("来源:" + source)
        disliketagListView.addTag(tags)
    }
    
    
    @IBAction func dislikeBtnClicked(sender: AnyObject) {
        //send remove signal to tableview
        NSNotificationCenter.defaultCenter().postNotificationName("deleteRowNotification", object: nil, userInfo: ["indexPath" : indexPath])
        //send to server
        
        //close
        self.presentingViewController?.dismissViewControllerAnimated(true, completion: nil)
    }
    
}