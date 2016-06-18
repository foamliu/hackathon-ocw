//
//  DownCell.h
//  TTNews
//
//  Created by 曾经 on 16/6/10.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeModel.h"
#import "ZFSessionModel.h"
#import "ZFDownloadManager.h"

@interface DownCell : UITableViewCell
@property (nonatomic, strong) UIProgressView *progressView;
@property (nonatomic, copy) void(^downloadCallBack)();
@property (nonatomic, strong) ZFSessionModel  *sessionModel;

- (void)configureCellDataWith:(HomeDetailModel *)model;
@end
