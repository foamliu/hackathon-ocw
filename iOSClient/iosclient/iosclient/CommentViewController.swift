//
//  CommentViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/8.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import Alamofire

class CommentViewController: UITableViewController {
    
    var courseId: Int!
    var comments: NSMutableArray = []
    var Comments:[Comment] = []
    
    @IBOutlet var commentsView: UITableView!
    @IBOutlet weak var likeBtn: UIButton!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "getCourseItem:", name: "courseIdNotification", object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "newComment:", name: "newCommentNotification", object: nil)
        //load comments
        
        
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
        
        if let likeLabel = cell.viewWithTag(203) as? UILabel {
            likeLabel.text = String(comments[indexPath.row].valueForKey("like")!) as? String
        }
        
        if let userImageView = cell.viewWithTag(200) as? UIImageView {
            if(comments[indexPath.row].valueForKey("headimgurl") != nil){
                let URLString:NSURL = NSURL(string: comments[indexPath.row].valueForKey("headimgurl") as! String)!
                userImageView.sd_setImageWithURL(URLString, placeholderImage: UIImage(named: "default.jpg"))
            }
            else{
                userImageView.image = UIImage(named: "anonymous")
            }
            userImageView.layer.borderWidth = 2
            userImageView.layer.masksToBounds = false
            userImageView.layer.borderColor = UIColor.whiteColor().CGColor
            userImageView.layer.cornerRadius = userImageView.frame.height/2
            userImageView.clipsToBounds = true
        }
        
        return cell
    }
    
    func loadCommentsFromUrl(){
        let url = NSURL(string: "http://jieko.cc/item/" + String(courseId!) + "/Comments")
        let request = NSURLRequest(URL: url!)
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startCommentsParsing(data!)
        }
    }
    
    //Handler
    func getCourseItem(notif: NSNotification) {
        courseId = notif.userInfo!["courseId"] as! Int
        loadCommentsFromUrl()
    }
    
    func newComment(notif: NSNotification){
        let newComment: NSMutableDictionary = notif.userInfo!["newComment"] as! NSMutableDictionary
        newComment.setValue(0, forKey: "like")
        newComment.setValue(User.sharedManager.userid, forKey: "author_id")
        if(User.sharedManager.nickname != nil){
            newComment.setValue(User.sharedManager.nickname, forKey: "author_name")
            newComment.setValue(User.sharedManager.headimgurl, forKey: "headimgurl")
        }
        else{
            newComment.setValue("匿名用户", forKey: "author_name")
        }
        comments.insertObject(newComment, atIndex: 0)
        commentsView.reloadData()
        sendComment(newComment)
    }
    
    func startCommentsParsing(data: NSData){
        let dict: NSArray!=(try! NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers)) as! NSArray
        if (dict.count > 1){
            for i in 0...(dict.count - 1){
                comments.addObject(dict.objectAtIndex(i))
                var newComment = Comment()
                let commentDic: NSDictionary = dict.objectAtIndex(i) as! NSDictionary
                newComment.item_id = commentDic["item_id"]?.integerValue
                newComment.author_id = commentDic["author_id"]?.integerValue
                newComment.author_name = commentDic["author_name"] as? String
                newComment.text = commentDic["text"] as? String
                newComment.posted = commentDic["posted"] as? String
                newComment.timeline = commentDic["timeline"]?.integerValue
                newComment.like = commentDic["like"]?.integerValue
                Comments.append(newComment)
            }
            commentsView.reloadData()
            loadHeadImg()
        }
        
    }
    
    func loadHeadImg(){
        for comment in comments {
            
            let commentDic: NSDictionary = comment as! NSDictionary
            let author_id: Int! = commentDic["author_id"]?.integerValue
            
            Alamofire.request(.GET, "http://jieko.cc/user/" + String(author_id!)).responseJSON { response in
                print(response.result)   // result of response serialization
                print(response.result.value)
                
                if let JSON = response.result.value as? Array<Dictionary<String, AnyObject>> {
                    self.comments.removeObject(comment)
                    let headimgurl: String = (JSON[0]["headimgurl"] as? String)!
                    print(headimgurl)
                    commentDic.setValue(headimgurl, forKey: "headimgurl")
                    self.comments.addObject(commentDic)
                    self.commentsView.reloadData()
                }
            }
        }
    }
    
    func sendComment(newComment: NSMutableDictionary){
        let comment = NSKeyedUnarchiver.unarchiveObjectWithData(NSKeyedArchiver.archivedDataWithRootObject(newComment)) as! NSMutableDictionary
        comment.removeObjectForKey("headimgurl")
        
        let url = "http://jieko.cc/item/" + String(courseId) + "/Comments"
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do{
            request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(comment, options: [])
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
    
    @IBAction func likeBtnClicked(sender: UIButton) {
        //Update to local comment
        var indexPath: NSIndexPath
        var comment_id: String!
        if let button = sender as? UIButton {
            if let superview = button.superview {
                if let cell = superview.superview as? CommentsViewCell{
                    indexPath = commentsView.indexPathForCell(cell)!
                    let id = comments[indexPath.row].valueForKey("_id") as! NSMutableDictionary
                    comment_id = id.valueForKey("$oid") as! String
                    let likeLabel = cell.viewWithTag(203) as? UILabel
                    let like: Int = Int((likeLabel?.text)!)! + 1
                    likeLabel!.text = String(like)
                    comments[indexPath.row].setValue(like, forKey: "like")
                    commentsView.reloadData()
                }
            }
        }
        //Send to server
        Alamofire.request(.GET, "http://jieko.cc/item/Comments/" + String(comment_id) + "/like").responseJSON { response in
            print(response.result)   // result of response serialization
            print(response.result.value)
            }
        }

}