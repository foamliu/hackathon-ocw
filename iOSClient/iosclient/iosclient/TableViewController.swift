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

class TableViewController: UITableViewController, UISearchBarDelegate, TableViewCellDelegate, UIPopoverPresentationControllerDelegate, NSURLConnectionDataDelegate {
    
    var courses: NSMutableArray = []
    var data = NSMutableData()
    var loadMoreEnable = true
    var isInternetConnected = true
    
    var selectedCourseId: Int!
    var selectedTitle: String!
    var selectedVideoUrl: String?
    var selectedDescription: String!
    var selectedImage: UIImage!
    var selectedLink: String?
    var searchActive : Bool = false
    var clearCourses : Bool = false
    
    var customRefreshControl = UIRefreshControl()
    var infiniteScrollingView:UIView?
    var dateFormatter = NSDateFormatter()
    
    var deleteCourseIndexPath: NSIndexPath? = nil
    
    @IBOutlet weak var menuButton: UIBarButtonItem!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        checkInternetConnection()
        
        if(isInternetConnected == true){
            //self.tableView.registerClass(MGSwipeTableCell.self, forCellReuseIdentifier: "CourseCell")
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
            
            self.setupInfiniteScrollingView()
            
            NSNotificationCenter.defaultCenter().addObserver(self, selector: "search:", name: "newSearchNotification", object: nil)
            NSNotificationCenter.defaultCenter().addObserver(self, selector: "searchByTags:", name: "newSearchByTagNotification", object: nil)
            NSNotificationCenter.defaultCenter().addObserver(self, selector: "deleteRow:", name: "deleteRowNotification", object: nil)
        }
    }
    
    func checkInternetConnection(){
        if Reachability.isConnectedToNetwork() == true {
            print("Internet connection OK")
        } else {
            print("Internet connection FAILED")
        }
        //If the user is not connected to the internet, you may want to show them an alert dialog to notify them.
        if Reachability.isConnectedToNetwork() == true {
            print("Internet connection OK")
        } else {
            isInternetConnected = false
            print("Internet connection FAILED")
            let alert = UIAlertView(title: "没有网络连接", message: "请确认您已连接到互联网", delegate: nil, cancelButtonTitle: "OK")
            alert.show()
        }
    }
    
    func getInitId(){
        User.sharedManager.deviceid = UIDevice.currentDevice().identifierForVendor!.UUIDString
        //检查本地userProfile文件
        let documentsDirectoryPathString = NSSearchPathForDirectoriesInDomains(.DocumentDirectory, .UserDomainMask, true).first!
        let documentsDirectoryPath = NSURL(string: documentsDirectoryPathString)!
        let filePath = String(documentsDirectoryPath.URLByAppendingPathComponent("userProfile.json"))
        do{
            let fileContent = try NSString(contentsOfFile: filePath, encoding: NSUTF8StringEncoding)
            let json = try NSJSONSerialization.JSONObjectWithData(fileContent.dataUsingEncoding(NSUTF8StringEncoding)!, options: [])
            let userid = json["_id"] as! Int
            User.sharedManager.userid = userid
        }catch let error as NSError{
            print(error)
            getInitFromServer()
        }
    }
    
    func getInitFromServer(){
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
        let activityViewIndicator = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.White)
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
        let dict: NSDictionary!=(try! NSJSONSerialization.JSONObjectWithData(data, options: NSJSONReadingOptions.MutableContainers)) as! NSDictionary
        if (clearCourses == true && (dict.valueForKey("courses") as! NSArray).count > 1){
            courses.removeAllObjects()
            clearCourses = false
        }
        if ((dict.valueForKey("courses") as! NSArray).count > 1){
            for i in 0...((dict.valueForKey("courses") as! NSArray).count - 1){
                courses.addObject((dict.valueForKey("courses") as! NSArray) .objectAtIndex(i))
            }
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
        
        cell.nameLabel.text = courses[indexPath.row].valueForKey("title") as? String
        cell.descriptionLabel.text = courses[indexPath.row].valueForKey("description") as? String
        cell.sourceLabel.text = courses[indexPath.row].valueForKey("source") as? String
        if(courses[indexPath.row].valueForKey("duration") as? String == ""){
            cell.durationLabel.text = "--:--"
        }
        else{
            cell.durationLabel.text = courses[indexPath.row].valueForKey("duration") as? String
        }
        
        let URLString:NSURL = NSURL(string: courses[indexPath.row].valueForKey("piclink") as! String)!
        cell.courseImageView.sd_setImageWithURL(URLString, placeholderImage: UIImage(named: "default.jpg"))
        
        if (indexPath.row == self.courses.count - 1){
            self.tableView.tableFooterView = self.infiniteScrollingView
            loadMore()
        }
        
        if cell.buttonDelegate == nil {
            cell.buttonDelegate = self
        }
        
        return cell
    }
    
    // MARK: UITableViewDelegate Methods
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            deleteCourseIndexPath = indexPath
            let courseToDelete = courses[indexPath.row].valueForKey("title") as! String
            confirmDelete(courseToDelete)
        }
    }
    
    // Delete Confirmation and Handling
    func confirmDelete(course: String) {
        let alert = UIAlertController(title: "Delete Course", message: "Are you sure you want to permanently delete \(course)?", preferredStyle: .ActionSheet)
        
        let DeleteAction = UIAlertAction(title: "Delete", style: .Destructive, handler: handleDeleteCourse)
        let CancelAction = UIAlertAction(title: "Cancel", style: .Cancel, handler: cancelDeleteCourse)
        
        alert.addAction(DeleteAction)
        alert.addAction(CancelAction)
        
        self.presentViewController(alert, animated: true, completion: nil)
    }
    
    func handleDeleteCourse(alertAction: UIAlertAction!) -> Void {
        if let indexPath = deleteCourseIndexPath {
            tableView.beginUpdates()
            
            courses.removeObjectAtIndex(indexPath.row)
            
            // Note that indexPath is wrapped in an array:  [indexPath]
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
            
            deleteCourseIndexPath = nil
            tableView.endUpdates()
        }
    }
    
    func cancelDeleteCourse(alertAction: UIAlertAction!) {
        deleteCourseIndexPath = nil
    }
    
    func deleteRow(notif: NSNotification) {
        let indexPath = notif.userInfo!["indexPath"] as! NSIndexPath
        tableView.beginUpdates()
        courses.removeObjectAtIndex(indexPath.row)
        tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Automatic)
        tableView.endUpdates()
    }
    
    func cellTapped(cell: TableViewCell) {
        //self.showAlertForRow(tableView.indexPathForCell(cell)!.row)
        //print(tableView.indexPathForCell(cell)!.row)
        
        //self.performSegueWithIdentifier("showDislike", sender: self)
        
        /*
        //debug now
        tableView.beginUpdates()
        courses.removeObjectAtIndex(tableView.indexPathForCell(cell)!.row)
        tableView.deleteRowsAtIndexPaths([tableView.indexPathForCell(cell)!], withRowAnimation: .Automatic)
        tableView.endUpdates()
         */
        //let fromRect:CGRect()
        
        let popVC = (storyboard?.instantiateViewControllerWithIdentifier("Popover"))! as! PopoverViewController
        popVC.modalPresentationStyle = UIModalPresentationStyle.Popover
        popVC.popoverPresentationController?.delegate = self
        popVC.popoverPresentationController?.sourceView = cell.dislikeButton
        popVC.popoverPresentationController?.sourceRect = cell.dislikeButton.bounds
        popVC.popoverPresentationController?.permittedArrowDirections = .Up
        popVC.preferredContentSize = CGSizeMake(400, 150)
        self.presentViewController(popVC, animated: true, completion: nil)
        
        //send related info
        let dict = NSMutableDictionary()
        dict.setValue(tableView.indexPathForCell(cell)!, forKey: "indexPath")
        dict.setValue(courses[tableView.indexPathForCell(cell)!.row].valueForKey("item_id")!, forKey: "item_id")
        dict.setValue(courses[tableView.indexPathForCell(cell)!.row].valueForKey("tags")!, forKey: "tags")
        dict.setValue(courses[tableView.indexPathForCell(cell)!.row].valueForKey("source")!, forKey: "source")
        
        //Update the comment to commentField
        NSNotificationCenter.defaultCenter().postNotificationName("newDislikeNotification", object: nil, userInfo: ["newDislike": dict])
        
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        let currentCell = tableView.cellForRowAtIndexPath(indexPath)! as! TableViewCell
        let nameLabel = currentCell.viewWithTag(100) as? UILabel
        let courseImageView = currentCell.viewWithTag(102) as? UIImageView
        selectedTitle = nameLabel?.text
        selectedDescription = courses[indexPath.row].valueForKey("description") as? String
        selectedImage = courseImageView?.image
        selectedVideoUrl = courses[indexPath.row].valueForKey("courselink") as? String
        selectedCourseId = courses[indexPath.row].valueForKey("item_id") as? Int
        selectedLink = courses[indexPath.row].valueForKey("link") as? String
        
        //Send request to server
        sendSelectedCourse(selectedCourseId)
        
        if((selectedLink?.containsString("yixi")) == true){
            parseYixiCourse(selectedLink!);
        }
        
        if(selectedVideoUrl == "" || selectedVideoUrl == nil){
            //Pass values
            performSegueWithIdentifier("showWebDetail", sender: self)
        }
        else{
            performSegueWithIdentifier("showDetail", sender: self)
        }
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
    
    func parseYixiCourse(videoUrl: String){
        Alamofire.request(.GET, videoUrl).responseString { response in
            print(response.result.value)
            let str: String = response.result.value!
            do {
                //vid: \'XMTQ4NTYxMDc4MA==\'
                let pattern = ".*vid: \'.*"
                let regex = try NSRegularExpression(pattern: pattern, options: [])
                let nsString = str as NSString
                let result = regex.matchesInString(str, options: NSMatchingOptions(rawValue: 0), range: NSMakeRange(0, nsString.length)) as Array<NSTextCheckingResult>
                for match in result {
                    print(nsString.substringWithRange(match.range))
                }
            }catch let error as NSError{
                print(error)
            }
        }
    }
    
    func search(notif: NSNotification){
        let keywords: String = notif.userInfo!["newSearch"] as! String
        let keywordsUTF8: String = keywords.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
        //Send to server
        let url = NSURL(string: "http://jieko.cc/items/search/" + keywordsUTF8)
        let request = NSURLRequest(URL: url!)
        clearCourses = true
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startParsing(data!)
        }
    }
    
    func searchByTags(notif: NSNotification){
        let keywords: String = notif.userInfo!["newSearchByTag"] as! String
        let keywordsUTF8: String = keywords.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
        //Send to server
        let url = NSURL(string: "http://jieko.cc/user/" + String(User.sharedManager.userid!) + "/Candidates/tag/" + keywordsUTF8)
        let request = NSURLRequest(URL: url!)
        clearCourses = true
        NSURLConnection.sendAsynchronousRequest(request, queue: NSOperationQueue.mainQueue()){(response, data, error) in self.startParsing(data!)
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == "showDetail"){
            let viewController = segue.destinationViewController as! DetailViewController
            viewController.courseId = selectedCourseId
            viewController.courseTitle = selectedTitle
            viewController.courseDescription = selectedDescription
            viewController.courseImage = selectedImage
            viewController.courseVideoUrl = selectedVideoUrl
            viewController.courseLink = selectedLink
        }
        else if(segue.identifier == "showWebDetail"){
            let viewController = segue.destinationViewController as! WebViewController
            viewController.courseId = selectedCourseId
            viewController.courseTitle = selectedTitle
            viewController.courseDescription = selectedDescription
            viewController.courseImage = selectedImage
            viewController.courseLink = selectedLink
        }
    }
    
    
    func adaptivePresentationStyleForPresentationController(controller: UIPresentationController) -> UIModalPresentationStyle {
        return .None
    }
    
}
