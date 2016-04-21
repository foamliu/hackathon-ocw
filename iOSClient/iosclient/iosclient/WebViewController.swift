//
//  WebViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/21.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

class WebViewController: UIViewController {
    
    @IBOutlet weak var webView: UIWebView!
    
    var courseId: Int!
    var courseTitle: String!
    var courseDescription: String!
    var courseImage: UIImage!
    var courseLink: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if (courseTitle.characters.count > 10){
            let trimCourseTitle = courseTitle.stringByReplacingCharactersInRange(courseTitle.startIndex.advancedBy(10)..<courseTitle.endIndex, withString: "...")
            self.title = trimCourseTitle
        }
        else{
            self.title = courseTitle
        }
        showWebView();
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func showWebView(){
        webView.loadRequest(NSURLRequest(URL: NSURL(string: courseLink)!))
    }
    
    //分享网页
    func sendWebpage(title: String, description: String, image: UIImage, url: String, inScene: WXScene) {
        let message =  WXMediaMessage()
        message.title = title
        message.description = description
        message.setThumbImage(image)
        
        let ext =  WXWebpageObject()
        ext.webpageUrl = url
        message.mediaObject = ext
        
        let req =  SendMessageToWXReq()
        req.bText = false
        req.message = message
        req.scene = Int32(inScene.rawValue)
        WXApi.sendReq(req)
    }
    
    @IBAction func shareBtnClicked(sender: UIBarButtonItem) {
        sendWebpage(courseTitle, description: courseDescription, image: courseImage, url: courseLink, inScene: WXSceneSession)
    }
    

}
