//
//  CommentViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/8.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

class CommentViewController: UITableViewController {
    
    var courseId: Int!
    var comments: NSMutableArray = []
    
    @IBOutlet var commentsView: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "getCourseItem:", name: "courseIdNotification", object: nil)
        //load comments
        loadCommentsFromUrl()
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(commentsView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return comments.count
    }
    
    override func tableView(commentsView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = commentsView.dequeueReusableCellWithIdentifier("CommentsCell") as! TableViewCell
        
        if let nameLabel = cell.viewWithTag(201) as? UILabel {
            nameLabel.text = comments[indexPath.row].valueForKey("author_name") as? String
        }
        
        if let commentLabel = cell.viewWithTag(202) as? UILabel {
            commentLabel.text = comments[indexPath.row].valueForKey("text") as? String
        }
        
        /*
        if let userImageView = cell.viewWithTag(200) as? UIImageView {
            let URLString:NSURL = NSURL(string: comments[indexPath.row].valueForKey("piclink") as! String)!
            courseImageView.sd_setImageWithURL(URLString, placeholderImage: UIImage(named: "default.jpg"))
        }
         */
        
        return cell
    }
    
    func loadCommentsFromUrl(){
        //let url = NSURL(string: "http://jieko.cc/item/" + String(courseId!) + "/Comments")
        let url = NSURL(string: "http://jieko.cc/item/" + String("1") + "/Comments")
        let request = NSURLRequest(URL: url!)
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startCommentsParsing(data!)
        }
    }
    
    func startCommentsParsing(data: NSData){
        let dict: NSArray!=(try! NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers)) as! NSArray
        for i in 0...(dict.count - 1){
            comments.addObject(dict.objectAtIndex(i))
        }
        commentsView.reloadData()
    }
    
    //Handler
    func getCourseItem(notif: NSNotification) {
        courseId = notif.userInfo!["courseId"] as! Int
    }
    
    
}