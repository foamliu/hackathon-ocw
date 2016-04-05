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
        
        labelTitle.text = selectedTitle
                
        //play online video
        if (videoUrl != nil){
            let url = NSURL.fileURLWithPath(videoUrl)
            let player = AVPlayer(URL: url)
            let playerViewController = AVPlayerViewController()
            playerViewController.player = player
            
            playerViewController.view.frame = CGRectMake(10, 110, 360, 300)
            self.view.addSubview(playerViewController.view)
            self.addChildViewController(playerViewController)
            
            player.play()
        }
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    
}