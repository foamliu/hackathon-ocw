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


class TableViewController: UITableViewController, UISearchBarDelegate {
    
    //var courses:[Course] = coursesData
    var courses: NSMutableArray = []
    var loadMoreEnable = true
    
    var selectedCourseId: Int!
    var selectedTitle: String!
    var selectedVideoUrl: String!
    var selectedDescription: String!
    var selectedImage: UIImage!
    var searchActive : Bool = false
    var clearCourses : Bool = false
    
    var customRefreshControl = UIRefreshControl()
    var infiniteScrollingView:UIView?
    var dateFormatter = NSDateFormatter()
    
    
    @IBOutlet weak var menuButton: UIBarButtonItem!
    @IBOutlet weak var searchBar: UISearchBar!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        getInitId()
        jsonParsingFromUrl()
        
        if self.revealViewController() != nil {
            menuButton.target = self.revealViewController()
            menuButton.action = "revealToggle:"
            self.view.addGestureRecognizer(self.revealViewController().panGestureRecognizer())
        }
        
        self.dateFormatter.dateStyle = NSDateFormatterStyle.ShortStyle
        self.dateFormatter.timeStyle = NSDateFormatterStyle.ShortStyle
        
        self.customRefreshControl.attributedTitle = NSAttributedString(string:  "下拉刷新")
        self.customRefreshControl.addTarget(self, action: "refresh:", forControlEvents: UIControlEvents.ValueChanged)
        self.tableView?.addSubview(customRefreshControl)
        
        self.tableView.contentOffset = CGPointMake(0, 44);
        self.searchBar.showsCancelButton = true
        self.searchBar.delegate = self
        
        self.setupInfiniteScrollingView()
    }
    
    func getInitId(){
        User.sharedManager.deviceid = NSUUID().UUIDString
        //使用deviceid换取userid
        var basicProfile = [String: AnyObject]()
        basicProfile["deviceid"] = User.sharedManager.deviceid
        let url = "http://jieko.cc/user"
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do{
            request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(basicProfile, options: [])
            let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                guard error == nil && data != nil else {                                                          // check for fundamental networking error
                    print("error=\(error)")
                    return
                }
                
                if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {           // check for http errors
                    print("statusCode should be 200, but is \(httpStatus.statusCode)")
                    print("response = \(response)")
                }
                
                do {
                    let result = try NSJSONSerialization.JSONObjectWithData(data!, options: []) as? [String: Int]
                    User.sharedManager.userid = result!["userid"]
                } catch let error as NSError {
                    print(error)
                }
                
            }
            task.resume()
        } catch _{
            print("Error json")
        }
    }
    
    func setupInfiniteScrollingView(){
        self.infiniteScrollingView = UIView(frame: CGRectMake(0, self.tableView.contentSize.height, self.tableView.bounds.size.width, 0))
        self.infiniteScrollingView!.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        self.infiniteScrollingView!.backgroundColor = UIColor.whiteColor()
        var activityViewIndicator = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.White)
        activityViewIndicator.color = UIColor.darkGrayColor()
        activityViewIndicator.frame = CGRectMake(self.infiniteScrollingView!.frame.size.width/2-activityViewIndicator.frame.width/2, self.infiniteScrollingView!.frame.size.height/4-activityViewIndicator.frame.height, activityViewIndicator.frame.width, 0)
        activityViewIndicator.startAnimating()
        self.infiniteScrollingView!.addSubview(activityViewIndicator)
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
    
    func loadMore(){
        jsonParsingFromUrl()
        self.tableView.reloadData()
    }
    
    func jsonParsingFromUrl(){
        let url = NSURL(string: "http://api.jieko.cc/user/" + String(User.sharedManager.userid!) + "/Candidates")
        let request = NSURLRequest(URL: url!)
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startParsing(data!)
        }
    }
    
    func startParsing(data: NSData){
        if (clearCourses == true){
            courses.removeAllObjects()
            clearCourses = false
        }
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
        
        if let sourceLabel = cell.viewWithTag(103) as? UILabel {
            sourceLabel.text = courses[indexPath.row].valueForKey("source") as? String
        }
        
        if let durationLabel = cell.viewWithTag(104) as? UILabel {
            durationLabel.text = courses[indexPath.row].valueForKey("duration") as? String
        }
        
        if (indexPath.row == self.courses.count - 1){
            self.tableView.tableFooterView = self.infiniteScrollingView
            loadMore()
        }
        
        return cell
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let currentCell = tableView.cellForRowAtIndexPath(indexPath)! as UITableViewCell
        let nameLabel = currentCell.viewWithTag(100) as? UILabel
        let courseImageView = currentCell.viewWithTag(102) as? UIImageView
        selectedTitle = nameLabel?.text
        selectedDescription = courses[indexPath.row].valueForKey("description") as? String
        selectedImage = courseImageView?.image
        selectedVideoUrl = courses[indexPath.row].valueForKey("courselink") as? String
        selectedCourseId = courses[indexPath.row].valueForKey("item_id") as? Int
        
        //Send request to server
        sendSelectedCourse(selectedCourseId)
        
        //Pass values
        performSegueWithIdentifier("showDetail", sender: self)
    }
    
    func sendSelectedCourse(courseId: Int){
        var courseSelected = [String: AnyObject]()
        courseSelected["user_id"] = User.sharedManager.userid
        courseSelected["item_id"] = courseId
        courseSelected["pref"] = 3
        
        let url = "http://jieko.cc/user/" + String(User.sharedManager.userid!) + "/Preferences"
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do{
            request.HTTPBody = try NSJSONSerialization.dataWithJSONObject(courseSelected, options: [])
            let task = NSURLSession.sharedSession().dataTaskWithRequest(request) { data, response, error in
                guard error == nil && data != nil else {
                    print("error=\(error)")
                    return
                }
                
                if let httpStatus = response as? NSHTTPURLResponse where httpStatus.statusCode != 200 {           // check for http errors
                    print("statusCode should be 200, but is \(httpStatus.statusCode)")
                    print("response = \(response)")
                }
                
                let responseString = NSString(data: data!, encoding: NSUTF8StringEncoding)
                print("responseString = \(responseString)")
            }
            task.resume()
        } catch _{
            print("Error json")
        }
    }
    
    @IBAction func searchBtnClicked(sender: UIBarButtonItem) {
        self.tableView.setContentOffset(CGPointMake(0, -64), animated: true)
    }

    
    func searchBarShouldBeginEditing(searchBar: UISearchBar) -> Bool {
        return true
    }
    
    func searchBarShouldEndEditing(searchBar: UISearchBar) -> Bool {
        return true
    }
    
    func searchBarSearchButtonClicked(searchBar: UISearchBar) {
        if(searchBar.text != nil){
            let searchKeywords: String = searchBar.text!.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
            
            //Send to server
            let url = NSURL(string: "http://jieko.cc/items/search/" + searchKeywords)
            let request = NSURLRequest(URL: url!)
            clearCourses = true
            NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startParsing(data!)
            }
            searchBarCancelButtonClicked(searchBar)
            self.view.endEditing(true)
        }
    }
    
    func searchBarCancelButtonClicked(searchBar: UISearchBar) {
        searchBar.text = ""
        self.tableView.setContentOffset(CGPointMake(0, -20), animated: true)
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == "showDetail"){
            let viewController = segue.destinationViewController as! DetailViewController
            viewController.courseTitle = selectedTitle
            viewController.courseDescription = selectedDescription
            viewController.courseImage = selectedImage
            viewController.courseVideoUrl = selectedVideoUrl
            viewController.courseId = selectedCourseId
        }
    }
}
