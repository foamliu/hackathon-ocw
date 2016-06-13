//
//  TTLogHelper.m
//  TTNews
//
//  Created by 曾经 on 16/6/8.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import "TTLogHelper.h"
#import "HYBNetworking.h"
@implementation TTLogHelper
- (BOOL)isSupportWithWebchat {
    if ([WXApi isWXAppSupportApi] || [WXApi isWXAppInstalled]) {
        return YES;
    } else {
        return NO;
    }
}
+ (BOOL)sendAuthRequestWithWeixin {
    if ([WXApi isWXAppInstalled]||[WXApi isWXAppSupportApi]) {
        
        //构造SendAuthReq结构体
        SendAuthReq* req =[[SendAuthReq alloc ] init ];
        req.scope = @"snsapi_userinfo, snsapi_base" ;
        req.state = @"0744" ;
        //第三方向微信终端发送一个SendAuthReq消息结构
        return [WXApi sendReq:req];
        
    }else{
        //        [Message show:@"你的手机上没有安装微信"];
            
        return NO;
    }
}

+ (void)userThirdLogin:(NSString *)code openid:(NSString *)openid {
    if (!code||code==nil) {
        return;
    }
    if (!openid) {
        return;
    }
    
        NSString *url =[NSString stringWithFormat:@"https://api.weixin.qq.com/sns/oauth2/access_token?appid=%@&secret=%@&code=%@&grant_type=authorization_code",@"wx9b493c5b54472578",@"211b995337b10a7ef9c32d511e7c4576",code];
    [HYBNetworking postWithUrl:url refreshCache:NO params:nil success:^(id response) {
        NSLog(@"------------%@", response);
        [TTLogHelper getUserInfoWith:response[@"access_token"] openID:@"openid"];
    } fail:^(NSError *error) {
        NSLog(@"%@", error);
    }];
    

}
+ (void)getUserInfoWith:(NSString *)token openID:(NSString *)openid {
    // https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
    
    NSString *url =[NSString stringWithFormat:@"https://api.weixin.qq.com/sns/userinfo?access_token=%@&openid=%@",token, openid];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        [HYBNetworking getWithUrl:url refreshCache:NO success:^(id response) {
            NSLog(@"`````````````%@", response);
            [[NSUserDefaults standardUserDefaults] setObject:response[@"headimgurl"] forKey:@"imageUrl"];
            [[NSUserDefaults standardUserDefaults] setObject:response[@"nickname"] forKey:@"userName"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            
            NSNotification * notice = [NSNotification notificationWithName:@"123" object:nil userInfo:@{@"1":@"123"}];
            //发送消息
            [[NSNotificationCenter defaultCenter]postNotification:notice];
        } fail:^(NSError *error) {
            NSLog(@"%@", error);
        }];
    });
}
@end
