//
//  WebViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/21.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

class WebViewController: UIViewController {
    
    var webView: UIWebView!
    
    var courseId: Int!
    var courseTitle: String!
    var courseDescription: String!
    var courseImage: UIImage!
    var courseLink: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //self.title = "学啥"
        self.edgesForExtendedLayout = .All

        let newBackButton = UIBarButtonItem(title: "<返回", style: UIBarButtonItemStyle.Plain, target: self, action: #selector(WebViewController.back(_:)))
        self.navigationItem.leftBarButtonItem = newBackButton;
        
        showWebView();
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func back(sender: UIBarButtonItem) {
        if(self.webView.canGoBack) {
            self.webView.goBack()
        } else {
            self.dismissViewControllerAnimated(true, completion: nil)
        }
    }
    
    func showWebView(){
        if (webView == nil) {
            let screenSize : CGRect = UIScreen.mainScreen().bounds;
            webView = UIWebView(frame: CGRectMake(0, 0, screenSize.width, screenSize.height))
            self.view.addSubview(webView)
        }
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
