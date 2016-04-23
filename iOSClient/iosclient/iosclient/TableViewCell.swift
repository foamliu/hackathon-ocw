//
//  TableViewCell.swift
//  iosclient
//
//  Created by 典 杨 on 16/3/30.
//  Copyright © 2016年 典 杨. All rights reserved.
//

import UIKit

protocol TableViewCellDelegate {
    func cellTapped(cell: TableViewCell)
}

class TableViewCell: UITableViewCell {
    
    var buttonDelegate: TableViewCellDelegate?
    
    @IBOutlet weak var dislikeButton: UIButton!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var descriptionLabel: UILabel!
    @IBOutlet weak var sourceLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    @IBOutlet weak var courseImageView: UIImageView!
    
    @IBAction func dislikeBtnClicked(sender: AnyObject) {
        if let delegate = buttonDelegate {
            delegate.cellTapped(self)
        }
    }
    
}
