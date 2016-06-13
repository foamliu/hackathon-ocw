//
//  ColumnCell.h
//  LearnWhat
//
//  Created by 曾经 on 16/6/4.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeModel.h"

@interface ColumnCell : UITableViewCell
- (void)configureDataWith:(HomeDetailModel *)model;
@end
