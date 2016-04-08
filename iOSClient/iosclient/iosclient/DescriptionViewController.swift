//
//  DescriptionViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/7.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

class DescriptionViewController: UIViewController {
    
    @IBOutlet weak var descriptionTextView: UITextView!
    
    var descriptionText: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "handler:", name: "descriptionNotification", object: nil)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //Handler
    func handler(notif: NSNotification) {
        descriptionTextView.text = notif.userInfo!["description"] as! String
    }

}