//
//  DetailViewController.swift
//  iosclient
//
//  Created by 杨典 on 16/3/30.
//  Copyright © 2016年 星群. All rights reserved.
//

import UIKit
import AVKit
import AVFoundation
import Foundation

class DetailViewController: UIViewController {
    
    //@IBOutlet weak var segmentedControl: UISegmentedControl!
    //@IBOutlet weak var descriptionView: UILabel!
    //@IBOutlet weak var commentView: UIView!
    //@IBOutlet weak var sendBtn: UIButton!
    //@IBOutlet weak var bottomConstraint: NSLayoutConstraint!
    //@IBOutlet weak var commentToolbar: UIToolbar!
    //@IBOutlet weak var commentTextfield: UITextField!
    
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
        
        //descriptionView.text = courseDescription
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
            //playerViewController.view.frame = CGRectMake(0, 110, screenSize.width, 260)
            playerViewController.view.frame = CGRectMake(0, 0, screenSize.width, screenSize.height)
            self.view.addSubview(playerViewController.view)
            self.addChildViewController(playerViewController)
            player.play()
            

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
    
    //@IBAction func indexChanged(sender: UISegmentedControl) {
    //    switch self.segmentedControl.selectedSegmentIndex{
    //        case 0:
    //            descriptionView.hidden = false
    //            commentView.hidden = true
    //        case 1:
    //            descriptionView.hidden = true
    //            commentView.hidden = false
    //        default:
    //            break
    //    }
    //}
    
//    func keyboardWillChange(notification: NSNotification) {
//        let dict = NSDictionary(dictionary: notification.userInfo!);
//        let keyboardFrame = dict[UIKeyboardFrameEndUserInfoKey]!.CGRectValue();
//        let ty = keyboardFrame.origin.y - view.frame.height;
//        let duration = dict[UIKeyboardAnimationDurationUserInfoKey] as! Double;
//        UIView.animateWithDuration(duration, animations: { () -> Void in
//            //self.commentToolbar.transform = CGAffineTransformMakeTranslation(0, ty);
//        });
//    }
    
    
    //@IBAction func sendBtnClicked(sender: UIButton) {
    //    commentTextfield.resignFirstResponder()
    //    if commentTextfield.text != "" {
    //       let dict = NSMutableDictionary()
    //        dict.setValue(courseId, forKey: "item_id")
    //        dict.setValue(commentTextfield.text, forKey: "text")
    //        dict.setValue(Int(self.player.currentTime().value), forKey: "timeline")
    //        dict.setValue(getCurrentTime(), forKey: "posted")
    //        commentTextfield.text = ""
            
            //Update the comment to commentField
    //        NSNotificationCenter.defaultCenter().postNotificationName("newCommentNotification", object: nil, userInfo: ["newComment": dict])
    //    }
    //
    //}
    
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