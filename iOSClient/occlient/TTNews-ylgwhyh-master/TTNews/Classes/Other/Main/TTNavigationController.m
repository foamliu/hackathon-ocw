//
//  TTNavigationController.m
//  TTNews
//
//  Created by 瑞文戴尔 on 16/3/25.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import "TTNavigationController.h"
#import "TTConst.h"
#import "UIButton+WebCache.h"

@interface TTNavigationController ()

@end

@implementation TTNavigationController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self updateSkinModel];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateSkinModel) name:SkinModelDidChangedNotification object:nil];
}

-(void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark 更新皮肤模式 接到模式切换的通知后会调用此方法
-(void)updateSkinModel {
    NSMutableDictionary *attributes = [NSMutableDictionary dictionary];
    NSString *currentSkinModel = [[NSUserDefaults standardUserDefaults] stringForKey:CurrentSkinModelKey];
    if ([currentSkinModel isEqualToString:NightSkinModelValue]) {//夜间模式
        self.navigationBar.barTintColor = [UIColor colorWithRed:34/255.0 green:30/255.0 blue:33/255.0 alpha:1.0];
        attributes[NSForegroundColorAttributeName] = [UIColor grayColor];
        
        self.toolbar.barTintColor = [UIColor blackColor];
    }else {//日间模式
        self.navigationBar.barTintColor = [UIColor colorWithRed:243/255.0 green:75/255.0 blue:80/255.0 alpha:1.0];
        attributes[NSForegroundColorAttributeName] = [UIColor whiteColor];
        self.toolbar.barTintColor = [UIColor whiteColor];
    }
    attributes[NSFontAttributeName] = [UIFont systemFontOfSize:20];
    self.navigationBar.titleTextAttributes = attributes;
}

-(void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated {
     [super pushViewController:viewController animated:animated];
//    if (self.childViewControllers.count  0) { // 如果push进来的不是第一个控制器
        UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button setImage:[UIImage imageNamed:@"navigationbar_pic_back_icon"] forState:UIControlStateNormal];
        [button setImage:[UIImage imageNamed:@"navigationbar_back_icon"] forState:UIControlStateHighlighted];
        button.frame = CGRectMake(0, 0, 30, 30);
        // 让按钮内部的所有内容左对齐
        button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        //        [button sizeToFit];
        // 让按钮的内容往左边偏移10
        [button addTarget:self action:@selector(back) forControlEvents:UIControlEventTouchUpInside];
        
        // 修改导航栏左边的item
        viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:button];
//        [viewController.navigationItem.leftBarButtonItem setImage:[UIImage imageNamed:@"default_round_head"]];
        
        // 隐藏tabbar
        viewController.hidesBottomBarWhenPushed = YES;
//    } else {
//        UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"home_edit_column@2x"] style:(UIBarButtonItemStylePlain) target:self action:@selector(handleLog:)];
//        NSString *imageurl = [[NSUserDefaults standardUserDefaults] objectForKey:@"imageUrl"];
////        [item sd_setImageWithURL:imageurl forState:(UIControlStateNormal)];
//        viewController.navigationItem.leftBarButtonItem = item;
//    }
    
    
    // 这句super的push要放在后面, 让viewController可以覆盖上面设置的leftBarButtonItem
   
    

}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

- (void)back
{
    [self popViewControllerAnimated:YES];
}


@end
