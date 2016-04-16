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


class DetailViewController: UIViewController {
    
    @IBOutlet weak var labelTitle: UILabel!
    @IBOutlet weak var segmentedControl: UISegmentedControl!
    @IBOutlet weak var descriptionView: UIView!
    @IBOutlet weak var commentView: UIView!
    @IBOutlet weak var sendBtn: UIButton!
    @IBOutlet weak var bottomConstraint: NSLayoutConstraint!
    @IBOutlet weak var commentToolbar: UIToolbar!
    @IBOutlet weak var commentTextfield: UITextField!
    
    var courseId: Int!
    var courseTitle: String!
    var courseDescription: String!
    var courseImage: UIImage!
    var courseVideoUrl: String!
    var player: AVPlayer!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        labelTitle.text = courseTitle
        playVideo()
        
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
    
    func dismissKeyboard(){
        view.endEditing(true)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
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
        
        /*
        let activityViewController : UIActivityViewController = UIActivityViewController(
        activityItems: [firstActivityItem], applicationActivities: nil)
         
         // This lines is for the popover you need to show in iPad
         activityViewController.popoverPresentationController?.barButtonItem = (sender as! UIBarButtonItem)
         
         // This line remove the arrow of the popover to show in iPad
         activityViewController.popoverPresentationController?.permittedArrowDirections = UIPopoverArrowDirection()
         activityViewController.popoverPresentationController?.sourceRect = CGRect(x: 150, y: 150, width: 0, height: 0)
         
         // Anything you want to exclude
         activityViewController.excludedActivityTypes = [
         UIActivityTypePostToWeibo,
         UIActivityTypePrint,
         UIActivityTypeAssignToContact,
         UIActivityTypeSaveToCameraRoll,
         UIActivityTypeAddToReadingList,
         UIActivityTypePostToFlickr,
         UIActivityTypePostToVimeo,
         ]
         
         self.presentViewController(activityViewController, animated: true, completion: nil)
         */
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