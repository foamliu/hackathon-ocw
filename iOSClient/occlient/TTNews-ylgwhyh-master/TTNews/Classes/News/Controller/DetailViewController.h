//
//  DetailViewController.h
//  LearnWhat
//
//  Created by 曾经 on 16/6/6.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HomeModel.h"

@interface DetailViewController : UIViewController
@property (nonatomic, strong) HomeDetailModel *model;
@property (nonatomic, assign) BOOL isCashe;
@end
