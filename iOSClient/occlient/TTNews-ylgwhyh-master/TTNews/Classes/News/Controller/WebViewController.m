//
//  WebViewController.m
//  LearnWhat
//
//  Created by 曾经 on 16/6/6.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import "WebViewController.h"
#import "Masonry.h"
@interface WebViewController () <UIWebViewDelegate>
@property(nonatomic, strong) UIWebView *webView;
@end

@implementation WebViewController
#pragma mark - life cycle
- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    
    [self.view addSubview:self.webView];
//    self.title = self.model.title;
    NSURLRequest *request = [NSURLRequest requestWithURL:self.model.link];
    [self.webView loadRequest:request];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}
#pragma mark - setters and getters
- (UIWebView *)webView {
    if (!_webView) {
        _webView = [UIWebView new];
//        _webView.delegate = self;
        _webView.backgroundColor = [UIColor grayColor];
        [self.view addSubview:_webView];
        
        [_webView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.left.right.bottom.equalTo(self.view);
            
        }];
    }
    return _webView;
}
#pragma mark - UIWebViewDelegate

@end
