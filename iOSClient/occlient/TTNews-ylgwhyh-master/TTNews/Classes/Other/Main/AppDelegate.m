//
//  AppDelegate.m
//  TTNews
//
//  Created by 瑞文戴尔 on 16/3/24.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import "AppDelegate.h"
#import "TTTabBarController.h"
#import "TTConst.h"
#import "NewsViewController.h"
#import "WXApi.h"
#import "TTLogHelper.h"
#import "TTNavigationController.h"
@interface AppDelegate () <WXApiDelegate>

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
//    [self setupUserDefaults];
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
//    self.window.rootViewController = [[TTTabBarController alloc] init];
    
    NewsViewController *new = [[NewsViewController alloc] init];
    UINavigationController *nav = [[UINavigationController alloc] initWithRootViewController:new];
    new.title = @"学啥";
    self.window.rootViewController = nav;
    [self.window makeKeyAndVisible];
    NSMutableDictionary *attributes = [NSMutableDictionary dictionary];

    nav.navigationBar.barTintColor = [UIColor colorWithRed:243/255.0 green:75/255.0 blue:80/255.0 alpha:1.0];
    attributes[NSForegroundColorAttributeName] = [UIColor whiteColor];
    nav.toolbar.barTintColor = [UIColor whiteColor];

    
     [WXApi registerApp:@"wx9b493c5b54472578" withDescription:@"news"];
    return YES;
    
   

}

-(void)setupUserDefaults {
    NSString *currentModel = [[NSUserDefaults standardUserDefaults] objectForKey:CurrentSkinModelKey];
    if (currentModel==nil) {
        [[NSUserDefaults standardUserDefaults] setObject:DaySkinModelValue forKey:CurrentSkinModelKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
    BOOL isShakeCanChangeSkin = [[NSUserDefaults standardUserDefaults] boolForKey:IsShakeCanChangeSkinKey];
    if (!isShakeCanChangeSkin) {
        [[NSUserDefaults standardUserDefaults] setObject:@(NO) forKey:IsShakeCanChangeSkinKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
    
    BOOL isDownLoadNoImageIn3G = [[NSUserDefaults standardUserDefaults] boolForKey:IsDownLoadNoImageIn3GKey];
    if (!isDownLoadNoImageIn3G) {
        [[NSUserDefaults standardUserDefaults] setObject:@(NO) forKey:IsDownLoadNoImageIn3GKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
    
    NSString *userName = [[NSUserDefaults standardUserDefaults] stringForKey:UserNameKey];
    if (userName==nil) {
        [[NSUserDefaults standardUserDefaults] setObject:@"TTNews" forKey:UserNameKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
    
    NSString *userSignature = [[NSUserDefaults standardUserDefaults] stringForKey:UserSignatureKey];
    if (userSignature==nil) {
        [[NSUserDefaults standardUserDefaults] setObject:@"这个家伙很懒,什么也没有留下" forKey:UserSignatureKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}
// 这个方法是用于从微信返回第三方App
- (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url {
    
    return [WXApi handleOpenURL:url delegate:self];
}
/*
 如果第三方程序向微信发送了sendReq的请求，那么onResp会被回调。sendReq请求调用后，会切到微信终端程序界面。
 */

#pragma mark - WXApiDelegate
- (void)onResp:(BaseResp *)resp {
//    if ([resp isKindOfClass:[SendMessageToWXResp class]]){
//        SendMessageToWXResp *resqonese=(SendMessageToWXResp *)resp;
//        if (resqonese.errCode ==0) {
//            [Message show:@"分享成功"];
//        }else{
//            [Message error:@"分享失败"];
//        }
//    }
    if ([resp isKindOfClass:[SendAuthResp class]]) {
        
        if (resp.errCode == 0){
            NSLog(@"用户同意");
            SendAuthResp *aresp = (SendAuthResp *)resp;
            if (aresp.code != nil) {
                NSLog(@"++++++++++code:%@",aresp.code);
                [TTLogHelper userThirdLogin:aresp.code openid:@""];


                
            }
            
        }
        else if (resp.errCode == -4){
            NSLog(@"用户拒绝");
            
        }else if (resp.errCode == -2){
            NSLog(@"用户取消");
        }
        
    }
}
//weixin
- (void)onReq:(BaseReq *)req {
    
}

@end
