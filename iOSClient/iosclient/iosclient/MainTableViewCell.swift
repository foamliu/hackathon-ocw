//
//  TableViewCell.swift
//  iosclient
//
//  Created by 杨典 on 16/3/30.
//  Copyright © 2016年 星群. All rights reserved.
//

import UIKit

class MainTableViewCell: UITableViewCell {
    
    @IBOutlet var lblTitle: UILabel!
    @IBOutlet var imgThumbnail: UIImageView!
    //@IBOutlet var lblSource: UILabel!
    //@IBOutlet var lblDuration: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
    }
    
    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }

  
}
