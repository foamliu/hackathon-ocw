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
    var Comments:[Comment] = []
    
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
        let cell = commentsView.dequeueReusableCellWithIdentifier("CommentsCell") as! CommentsViewCell
        
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
    
    //Handler
    func getCourseItem(notif: NSNotification) {
        courseId = notif.userInfo!["courseId"] as! Int
    }
    
    func startCommentsParsing(data: NSData){
        let dict: NSArray!=(try! NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers)) as! NSArray
        for i in 0...(dict.count - 1){
            comments.addObject(dict.objectAtIndex(i))
            var newComment = Comment()
            var commentDic: NSDictionary = dict.objectAtIndex(i) as! NSDictionary
            newComment.item_id = commentDic["item_id"]?.integerValue
            newComment.author_id = commentDic["author_id"]?.integerValue
            newComment.author_name = commentDic["author_name"]?.string
            newComment.text = commentDic["text"]?.string
            newComment.timeline = commentDic["timeline"]?.string
            newComment.like = commentDic["like"]?.integerValue
            Comments.append(newComment)
        }
        commentsView.reloadData()
        loadHeadImg()
    }
    
    
    func loadHeadImg(){
        let headimgurl: String?

        for comment in comments {
            comments.removeObject(comment)
            
            //var author_id: Int! = comment["author_id"]
            //Get img url with GET request
            
            //更新到server端
            let url = "http://jieko.cc/user/" //+ String(author_id!)
            let request = NSMutableURLRequest(URL: NSURL(string: url)!)
            request.HTTPMethod = "GET"
           
            
            do{
                    let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                    guard error == nil && data != nil else {
                        print("error=\(error)")
                        return
                    }
                        
                    if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {           // check for http errors
                        print("statusCode should be 200, but is \(httpStatus.statusCode)")
                        print("response = \(response)")
                    }
                        
                    do {
                        let result = try NSJSONSerialization.JSONObjectWithData(data!, options: []) as? [String: Int]
                        var headimgurl = result!["headimgurl"]
                        //comment["headimgurl"] = headimgurl
                        self.comments.addObject(comment)
                    } catch let error as NSError {
                        print(error)
                    }
                }
                task.resume()
            } catch _{
                print("Error json")
            }
            
        }
    }
 
    
    
    
    
}