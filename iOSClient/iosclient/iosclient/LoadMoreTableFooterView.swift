//
//  LoadMoreTableFooterView.swift
//  iosclient
//
//  Created by 典 杨 on 16/3/31.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import Foundation
import UIKit

let TEXT_COLOR: UIColor = UIColor(red: 87.0 / 255.0, green: 108.0 / 255.0, blue: 137.0 / 255.0, alpha: 1.0)

enum LoadMoreState{
    case LoadMorePulling
    case LoadMoreNormal
    case LoadMoreLoading
}

protocol LoadMoreTableFooterViewDelegate {
    func loadMoreTableFooterDidTriggerRefresh(view: LoadMoreTableFooterView)
    func loadMoreTableFooterDataSourceIsLoading(view: LoadMoreTableFooterView) -> Bool
}


class LoadMoreTableFooterView: UIView {
    var delegate: LoadMoreTableFooterViewDelegate?
    var state: LoadMoreState                        = LoadMoreState.LoadMoreNormal
    var statusLabel: UILabel                        = UILabel()
    var activityView: UIActivityIndicatorView       = UIActivityIndicatorView()
    
    required init(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)!
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        self.backgroundColor = UIColor.clearColor()
        
        let label: UILabel = UILabel(frame: CGRectMake(0, 10, self.frame.size.width, 20))
        label.autoresizingMask = UIViewAutoresizing.FlexibleWidth
        label.font = UIFont.boldSystemFontOfSize(13)
        label.textColor = TEXT_COLOR
        label.shadowColor = UIColor(white: 0.9, alpha: 1)
        label.shadowOffset = CGSizeMake(0, 1)
        label.backgroundColor = UIColor.clearColor()
        label.textAlignment = NSTextAlignment.Center
        self.addSubview(label)
        statusLabel = label
        
        let view: UIActivityIndicatorView = UIActivityIndicatorView(activityIndicatorStyle: UIActivityIndicatorViewStyle.Gray)
        view.frame = CGRectMake(55, 20, 20, 20)
        self.addSubview(view)
        activityView = view
        
        self.hidden = true
        
        setState(LoadMoreState.LoadMoreNormal)
    }
    
    func setState(aState: LoadMoreState) {
        switch aState {
        case LoadMoreState.LoadMorePulling:
            statusLabel.text = NSLocalizedString("松开加载更多...", comment: "")
            statusLabel.frame = CGRectMake(0, 20, self.frame.size.width, 20)
        case LoadMoreState.LoadMoreNormal:
            statusLabel.text = NSLocalizedString("上拉加载更多...", comment: "")
            statusLabel.frame = CGRectMake(0, 10, self.frame.size.width, 20)
            activityView.stopAnimating()
        case LoadMoreState.LoadMoreLoading:
            statusLabel.text = NSLocalizedString("加载中...", comment: "")
            statusLabel.frame = CGRectMake(0, 20, self.frame.size.width, 20)
            activityView.startAnimating()
        default:
            statusLabel.text = ""
        }
        state = aState
    }
    
    func loadMoreScrollViewDidScroll(loadScrollView: UIScrollView) {
        
        if state == LoadMoreState.LoadMoreLoading {
            loadScrollView.contentInset = UIEdgeInsetsMake(loadScrollView.contentInset.top, 0, 60, 0)
        } else if loadScrollView.dragging {
            var loading: Bool = false
            if delegate != nil {
                loading = delegate!.loadMoreTableFooterDataSourceIsLoading(self)
            }
            
            if (state == LoadMoreState.LoadMoreNormal && loadScrollView.contentOffset.y < (loadScrollView.contentSize.height - (loadScrollView.frame.size.height - 60)) && loadScrollView.contentOffset.y > (loadScrollView.contentSize.height - loadScrollView.frame.size.height) && !loading) {
                
                self.frame = CGRectMake(0, loadScrollView.contentSize.height, self.frame.size.width, self.frame.size.height)
                self.hidden = false
            } else if (state == LoadMoreState.LoadMoreNormal && loadScrollView.contentOffset.y > (loadScrollView.contentSize.height - (loadScrollView.frame.size.height - 60)) && !loading) {
                
                setState(LoadMoreState.LoadMorePulling)
            } else if (state == LoadMoreState.LoadMorePulling && loadScrollView.contentOffset.y < (loadScrollView.contentSize.height - (loadScrollView.frame.size.height - 60)) && loadScrollView.contentOffset.y > (loadScrollView.contentSize.height - loadScrollView.frame.size.height) && !loading) {
                
                setState(LoadMoreState.LoadMoreNormal)
            }
            
            if loadScrollView.contentInset.bottom != 40 {
                loadScrollView.contentInset = UIEdgeInsetsMake(loadScrollView.contentInset.top, 0, 40, 0)
            }
            
            let offset: CGFloat = loadScrollView.contentOffset.y - (loadScrollView.contentSize.height - loadScrollView.frame.size.height) - loadScrollView.contentInset.bottom
            if offset <= 20 && offset >= 0 {
                statusLabel.frame = CGRectMake(0, 10 + offset / 2, self.frame.size.width, 20)
            }
        }
    }
    
    func loadMoreScrollViewDidEndDragging(loadScrollView: UIScrollView) {
        var loading = false
        if delegate != nil {
            loading = delegate!.loadMoreTableFooterDataSourceIsLoading(self)
        }
        
        if (loadScrollView.contentOffset.y > (loadScrollView.contentSize.height - (loadScrollView.frame.size.height - 60)) && !loading) {
            if delegate != nil {
                delegate!.loadMoreTableFooterDidTriggerRefresh(self)
            }
            
            setState(LoadMoreState.LoadMoreLoading)
            
            UIView.beginAnimations(nil, context: nil)
            UIView.setAnimationDuration(0.2)
            loadScrollView.contentInset = UIEdgeInsetsMake(loadScrollView.contentInset.top, 0, 60, 0)
            UIView.commitAnimations()
        }
    }
    
    func loadMoreScrollViewDataSourceDidFinishedLoading(scrollView: UIScrollView) {
        
        setState(LoadMoreState.LoadMoreNormal)
        self.hidden = true  
    }  
}
