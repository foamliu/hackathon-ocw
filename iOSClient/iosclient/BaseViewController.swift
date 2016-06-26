//
//  BaseViewController.swift
//  iosclient
//
//  Created by Yang Liu on 16/6/23.
//  Copyright © 2016年 星群. All rights reserved.
//

import UIKit
import PageMenu


class BaseViewController: UIViewController {

    var pageMenu : CAPSPageMenu?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = "学啥"
        self.edgesForExtendedLayout = .None

        var controllerArray : [UIViewController] = []
        
        for c in Constants.catalogs {
            let controller = MainViewController(nibName: "MainWindow", bundle: nil)
            controller.title = c
            controllerArray.append(controller)
        }
        
        let parameters: [CAPSPageMenuOption] = [
            .AddBottomMenuHairline(true),
            .ScrollMenuBackgroundColor (UIColor(red: 0.750, green: 0.055, blue: 0.082, alpha: 1.0)),
            .SelectedMenuItemLabelColor (UIColor.whiteColor()),
            .UnselectedMenuItemLabelColor (UIColor.lightGrayColor()),
            .SelectionIndicatorHeight (0.0),
            .MenuMargin(15.0),
            .MenuItemWidthBasedOnTitleTextWidth(true)
        ]
        
        pageMenu = CAPSPageMenu(viewControllers: controllerArray, frame: CGRectMake(0.0, 0.0, self.view.frame.width, self.view.frame.height), pageMenuOptions: parameters)
        
        self.view.addSubview(pageMenu!.view)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func prefersStatusBarHidden() -> Bool {
        return true
    }
}
