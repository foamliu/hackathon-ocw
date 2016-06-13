//
//  TTLogHelper.h
//  TTNews
//
//  Created by 曾经 on 16/6/8.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "WXApi.h"
@interface TTLogHelper : NSObject <WXApiDelegate>

#pragma mark - 微信登陆
- (BOOL)isSupportWithWebchat;
+ (BOOL)sendAuthRequestWithWeixin;

#pragma mark- 第三登陆后回调登陆
+ (void)userThirdLogin:(NSString *)code openid:(NSString *)openid;
@end
