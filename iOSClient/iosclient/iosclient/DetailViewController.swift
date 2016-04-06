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
    
    
    var selectedTitle: String!
    var videoUrl: String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let screenSize : CGRect = UIScreen.mainScreen().bounds;
        labelTitle.text = selectedTitle

        //play online video
        if (videoUrl != nil){
            let videoURL = NSURL(string: videoUrl)
            let player = AVPlayer(URL: videoURL!)
            let playerViewController = AVPlayerViewController()
            playerViewController.player = player
            playerViewController.view.frame = CGRectMake(0, 110, screenSize.width, 300)
            self.addChildViewController(playerViewController)
            self.view.addSubview(playerViewController.view)
            playerViewController.player!.play()
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func shareBtn(sender: AnyObject) {
        let firstActivityItem = "Text you want"
        
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
    }
    
    
    
    
}