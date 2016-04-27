//
//  SearchViewController.swift
//  iosclient
//
//  Created by 典 杨 on 16/4/21.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit
import Alamofire

class SearchViewController: UIViewController, UISearchBarDelegate, TagListViewDelegate {
    
    var searchBar: UISearchBar!
    
    @IBOutlet weak var tagListView: TagListView!
    
    var searchTags = [String]()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupSearchBar();
        searchTagsInit();
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    private func setupSearchBar(){
        if let navigationBarFrame = navigationController?.navigationBar.bounds{
            let searchBar: UISearchBar = UISearchBar(frame: navigationBarFrame)
            searchBar.delegate = self
            searchBar.placeholder = "Search"
            //searchBar.showsCancelButton = true
            searchBar.autocapitalizationType = UITextAutocapitalizationType.None
            searchBar.keyboardType = UIKeyboardType.Default
            navigationItem.titleView = searchBar
            navigationItem.titleView?.frame = searchBar.frame
            self.searchBar = searchBar
            searchBar.becomeFirstResponder()
        }
    }
    
    func searchTagsInit(){
        tagListView.delegate = self
        tagListView.textFont = UIFont.systemFontOfSize(12)
        tagListView.textColor = UIColor.darkGrayColor()
        tagListView.tagBackgroundColor = UIColor.whiteColor()
        tagListView.cornerRadius = 15
        tagListView.marginX = 15
        tagListView.marginY = 10
        tagListView.paddingX = 15
        tagListView.paddingY = 8
        tagListView.borderColor = UIColor.lightGrayColor()
        tagListView.borderWidth = 1
        
        //Get tags from server
        Alamofire.request(.GET, "http://jieko.cc/user/" + String(User.sharedManager.userid!) + "/tags").responseJSON { response in
            let json = response.result.value!["tags"] as! NSArray
            for element in json{
                let ele = element as! NSArray
                self.tagListView.addTag(ele[0] as! String)
            }
        }
    }
    
    func searchBarShouldBeginEditing(searchBar: UISearchBar) -> Bool {
        return true
    }
    
    func searchBarShouldEndEditing(searchBar: UISearchBar) -> Bool {
        return true
    }
    
    func searchBarSearchButtonClicked(searchBar: UISearchBar) {
        if(searchBar.text != nil){
            //Update the searchText to Table
            NSNotificationCenter.defaultCenter().postNotificationName("newSearchNotification", object: nil, userInfo: ["newSearch": searchBar.text!])
            navigationController!.popViewControllerAnimated(true)
        }
    }
    
    func tagPressed(title: String, tagView: TagView, sender: TagListView) {
        //Search by tags
        //Update the searchText to Table
        NSNotificationCenter.defaultCenter().postNotificationName("newSearchByTagNotification", object: nil, userInfo: ["newSearchByTag": title])
        navigationController!.popViewControllerAnimated(true)
        
    }
    

}
