//
//  TableViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/3/30.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import Alamofire
import SDWebImage


class TableViewController: UITableViewController {
    
    //var courses:[Course] = coursesData
    var courses: NSMutableArray = []
    
    var customRefreshControl = UIRefreshControl()
    var dateFormatter = NSDateFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        jsonParsingFromUrl()
        
        self.dateFormatter.dateStyle = NSDateFormatterStyle.ShortStyle
        self.dateFormatter.timeStyle = NSDateFormatterStyle.ShortStyle
        
        self.customRefreshControl.attributedTitle = NSAttributedString(string:  "下拉刷新")
        self.customRefreshControl.addTarget(self, action: "refresh:", forControlEvents: UIControlEvents.ValueChanged)
        self.tableView?.addSubview(customRefreshControl)
    }
    
    func refresh(customRefreshControl: UIRefreshControl){
        jsonParsingFromUrl()
        
        let now = NSDate()
        let updateString = "更新于 " + self.dateFormatter.stringFromDate(now)
        self.customRefreshControl.attributedTitle = NSAttributedString(string: updateString)
        
        courses.removeAllObjects()
        self.tableView.reloadData()
        self.customRefreshControl.endRefreshing()
        
        
    }
    
    func jsonParsingFromUrl(){
        let url = NSURL(string: "http://api.jieko.cc/user/15/Candidates")
        let request = NSURLRequest(URL: url!)
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startParsing(data!)
        }
    }
    
    func startParsing(data: NSData){
        let dict: NSDictionary!=(try! NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
        for i in 0...((dict.valueForKey("courses") as! NSArray).count - 1){
            courses.addObject((dict.valueForKey("courses") as! NSArray) .objectAtIndex(i))
        }
        tableView.reloadData()
    }
    
    
    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return courses.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("CourseCell") as! TableViewCell
        
        if let nameLabel = cell.viewWithTag(100) as? UILabel {
            nameLabel.text = courses[indexPath.row].valueForKey("title") as? String
        }
        
        if let descriptionLabel = cell.viewWithTag(101) as? UILabel {
            descriptionLabel.text = courses[indexPath.row].valueForKey("description") as? String
        }
        
        if let courseImageView = cell.viewWithTag(102) as? UIImageView {
            let URLString:NSURL = NSURL(string: courses[indexPath.row].valueForKey("piclink") as! String)!
            courseImageView.sd_setImageWithURL(URLString, placeholderImage: UIImage(named: "default.jpg"))
        }
        
        return cell
    }
    
    /*
    func imageForRating(rating:Int) -> UIImage? {
        let imageName = "\(rating)Stars"
        return UIImage(named: imageName)
    }
     */
    
}