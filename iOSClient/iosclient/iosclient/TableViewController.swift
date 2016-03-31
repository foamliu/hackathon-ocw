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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        jsonParsingFromUrl()
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
            nameLabel.text = courses[indexPath.row].valueForKey("title") as! String
        }
        
        if let descriptionLabel = cell.viewWithTag(101) as? UILabel {
            descriptionLabel.text = courses[indexPath.row].valueForKey("description") as! String
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