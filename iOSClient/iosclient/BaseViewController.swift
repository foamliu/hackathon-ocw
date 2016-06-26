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
            .ScrollMenuBackgroundColor (UIColor(red:0.867, green:0.867, blue:0.867, alpha:1)),
            .SelectedMenuItemLabelColor (UIColor.blueColor()),
            .UnselectedMenuItemLabelColor (UIColor.darkGrayColor()),
            .SelectionIndicatorHeight (0.0),
            .MenuMargin(10.0)
        ]
        
        pageMenu = CAPSPageMenu(viewControllers: controllerArray, frame: CGRectMake(0.0, 0.0, self.view.frame.width, self.view.frame.height), pageMenuOptions: parameters)
        
        self.view.addSubview(pageMenu!.view)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}
