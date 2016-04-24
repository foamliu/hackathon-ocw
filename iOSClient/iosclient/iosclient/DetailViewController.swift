//
//  DetailViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/3/30.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import AVKit
import AVFoundation
import Cosmos
import Foundation

class DetailViewController: UIViewController {
    
    @IBOutlet weak var segmentedControl: UISegmentedControl!
    @IBOutlet weak var descriptionView: UIView!
    @IBOutlet weak var commentView: UIView!
    @IBOutlet weak var sendBtn: UIButton!
    @IBOutlet weak var bottomConstraint: NSLayoutConstraint!
    @IBOutlet weak var commentToolbar: UIToolbar!
    @IBOutlet weak var commentTextfield: UITextField!
    @IBOutlet weak var ratingBar: CosmosView!
    
    var courseId: Int!
    var courseTitle: String!
    var courseDescription: String!
    var courseImage: UIImage!
    var courseVideoUrl: String!
    var courseLink: String?
    var player: AVPlayer!
    var alert: UIAlertController!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if (courseTitle.characters.count > 10){
            let trimCourseTitle = courseTitle.stringByReplacingCharactersInRange(courseTitle.startIndex.advancedBy(10)..<courseTitle.endIndex, withString: "...")
            self.title = trimCourseTitle
        }
        else{
            self.title = courseTitle
        }
        playVideo()
        ratingBar.didTouchCosmos = didTouchCosmos
        
        //segmentControl
        descriptionView.hidden = false
        commentView.hidden = true
        
        //Pass values
        if(courseDescription != nil){
            NSNotificationCenter.defaultCenter().postNotificationName("descriptionNotification", object: nil, userInfo: ["description" : courseDescription])
        }
        if(courseId != nil){
            NSNotificationCenter.defaultCenter().postNotificationName("courseIdNotification", object: nil, userInfo: ["courseId" : courseId])
        }
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillChange:", name: UIKeyboardWillChangeFrameNotification, object: nil)
        
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        view.addGestureRecognizer(tap)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(true)
        
        let name = "Screen~\(self.courseTitle)"
        
        // [START screen_view_hit_swift]
        let tracker = GAI.sharedInstance().defaultTracker
        tracker.set(kGAIScreenName, value: name)
        
        let builder = GAIDictionaryBuilder.createScreenView()
        tracker.send(builder.build() as [NSObject : AnyObject])
        // [END screen_view_hit_swift]
    }
    
    override func viewWillDisappear(animated: Bool) {
        player.pause()
    }
    
    func dismissKeyboard(){
        view.endEditing(true)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func didTouchCosmos(rating: Double){
        let rate: Int = Int(rating)
        sendSelectedCourse(courseId, rating: rate)
        notifyUser("谢谢您的评分", message: "", timeToDissapear: 2)
        
    }
    
    func sendSelectedCourse(courseId: Int, rating: Int){
        var courseSelected = [String: AnyObject]()
        courseSelected["user_id"] = User.sharedManager.userid
        courseSelected["item_id"] = courseId
        courseSelected["pref"] = rating
        
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
    
    func notifyUser(title: String, message: String, timeToDissapear: Int) -> Void
    {
        alert = UIAlertController(title: title,message: message,preferredStyle: UIAlertControllerStyle.Alert)
        
        let cancelAction = UIAlertAction(title: "OK",style: .Cancel, handler: nil)
        
        alert.addAction(cancelAction)
        UIApplication.sharedApplication().keyWindow?.rootViewController!.presentViewController(alert, animated: true,completion: nil)
        
        // Delay the dismissal by timeToDissapear seconds
        let delay = Double(timeToDissapear) * Double(NSEC_PER_SEC)
        let time = dispatch_time(DISPATCH_TIME_NOW, Int64(delay))
        dispatch_after(time, dispatch_get_main_queue()) { [weak self] in
            self!.alert.dismissViewControllerAnimated(true, completion: nil)
        }
    }
    
    func playVideo(){
        let screenSize : CGRect = UIScreen.mainScreen().bounds;
        //play online video
        if (courseVideoUrl != nil){
            let videoURL = NSURL(string: courseVideoUrl)
            player = AVPlayer(URL: videoURL!)
            let playerViewController = AVPlayerViewController()
            playerViewController.player = player
            playerViewController.view.frame = CGRectMake(0, 110, screenSize.width, 260)
            self.addChildViewController(playerViewController)
            self.view.addSubview(playerViewController.view)
            playerViewController.player!.play()
        }
    }
    
    //分享文本
    func sendText(text:String, inScene: WXScene)->Bool{
        let req=SendMessageToWXReq()
        req.text=text
        req.bText=true
        req.scene=Int32(inScene.rawValue)
        return WXApi.sendReq(req)
    }
    
    //分享视频
    func sendVideo(title: String, description: String, image: UIImage, url: String, inScene: WXScene) {
        let message =  WXMediaMessage()
        message.title = title
        message.description = description
        message.setThumbImage(image)
        
        let ext =  WXVideoObject()
        ext.videoUrl = url
        message.mediaObject = ext
        
        let req =  SendMessageToWXReq()
        req.bText = false
        req.message = message
        req.scene = Int32(inScene.rawValue)
        WXApi.sendReq(req)
    }
    
    @IBAction func shareBtn(sender: UIBarButtonItem) {
        //sendText("这是来自学啥iOS端的分享", inScene: WXSceneSession) //分享文本到朋友圈
        sendVideo(courseTitle, description: courseDescription, image: courseImage, url: courseVideoUrl, inScene: WXSceneSession)
    }
    
    @IBAction func indexChanged(sender: UISegmentedControl) {
        switch self.segmentedControl.selectedSegmentIndex{
            case 0:
                descriptionView.hidden = false
                commentView.hidden = true
            case 1:
                descriptionView.hidden = true
                commentView.hidden = false
            default:
                break
        }
    }
    
    func keyboardWillChange(notification: NSNotification) {
        let dict = NSDictionary(dictionary: notification.userInfo!);
        let keyboardFrame = dict[UIKeyboardFrameEndUserInfoKey]!.CGRectValue();
        let ty = keyboardFrame.origin.y - view.frame.height;
        let duration = dict[UIKeyboardAnimationDurationUserInfoKey] as! Double;
        UIView.animateWithDuration(duration, animations: { () -> Void in
            self.commentToolbar.transform = CGAffineTransformMakeTranslation(0, ty);
        });
    }
    
    
    @IBAction func sendBtnClicked(sender: UIButton) {
        commentTextfield.resignFirstResponder()
        if commentTextfield.text != "" {
            let dict = NSMutableDictionary()
            dict.setValue(courseId, forKey: "item_id")
            dict.setValue(commentTextfield.text, forKey: "text")
            dict.setValue(Int(self.player.currentTime().value), forKey: "timeline")
            dict.setValue(getCurrentTime(), forKey: "posted")
            commentTextfield.text = ""
            
            //Update the comment to commentField
            NSNotificationCenter.defaultCenter().postNotificationName("newCommentNotification", object: nil, userInfo: ["newComment": dict])
        }
        
    }
    
    func getCurrentTime() -> String{
        let currentTime = NSDate()
        let formatter = NSDateFormatter();
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ";
        formatter.timeZone = NSTimeZone(abbreviation: "UTC");
        var utcTimeZoneStr = formatter.stringFromDate(currentTime);
        utcTimeZoneStr = utcTimeZoneStr.stringByReplacingOccurrencesOfString("GMT", withString: "Z")
        return utcTimeZoneStr
    }
    
}