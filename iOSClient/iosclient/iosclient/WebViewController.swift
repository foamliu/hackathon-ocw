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
    var courseLink: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let trimCourseTitle = courseTitle.stringByReplacingCharactersInRange(courseTitle.startIndex.advancedBy(10)..<courseTitle.endIndex, withString: "...")
        self.title = trimCourseTitle
        showWebView();
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func showWebView(){
        webView.loadRequest(NSURLRequest(URL: NSURL(string: courseLink)!))
    }

}
