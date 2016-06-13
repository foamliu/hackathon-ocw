//
//  DetailViewController.m
//  LearnWhat
//
//  Created by 曾经 on 16/6/6.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import "DetailViewController.h"
//#import "WMPlayer.h"
#import "Masonry.h"
#import <MediaPlayer/MediaPlayer.h>
@interface DetailViewController ()
@property (nonatomic, strong) MPMoviePlayerController *videoPlayer;
@property (nonatomic, strong) UISegmentedControl *segment;
@property (nonatomic, strong) UITextView *textView;
@property (nonatomic, strong) UILabel *label;
@end

@implementation DetailViewController
#pragma mark - life cycle
- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:self.videoPlayer.view];
//    _videoPlayer.state = WMPlayerStatePlaying;

    
    [self.view addSubview:self.segment];
    [self.view addSubview:self.textView];
    [self.view addSubview:self.label];

//    if (self.isCashe == YES) {
//        NSURL *url=[[NSURL alloc] initFileURLWithPath:self.model.coursePath];
//        self.videoPlayer.contentURL = url;
//        [self.videoPlayer play];
//    } else {
    [self setValue];
//    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}
- (void)dealloc {
    [self.videoPlayer stop];
}
#pragma mark - private methods
- (void)setValue {
    [self.videoPlayer play];
//     NSString *string = [[NSString alloc]initWithData:self.model.description encoding:NSUTF8StringEncoding];
    self.textView.text = self.model.description;
}
#pragma mark - setters and getters
- (MPMoviePlayerController *)videoPlayer {
    if (!_videoPlayer) {
        if (self.isCashe == YES) {
//            NSString *path=[[NSBundle mainBundle] pathForResource:@"VideoImageFolder/videoId_key_4042" ofType:@"mp4"];
            NSURL *url = [[NSURL alloc] initFileURLWithPath:[NSString stringWithFormat:@"%@", self.model.coursePath]];
            _videoPlayer = [[MPMoviePlayerController alloc] initWithContentURL:url];
            _videoPlayer.movieSourceType=MPMovieSourceTypeFile;
        } else {
        _videoPlayer = [[MPMoviePlayerController alloc] initWithContentURL:self.model.courselink];
        }
        [_videoPlayer.view setFrame:CGRectMake(0, 64,[UIScreen mainScreen].bounds.size.width, 260)];
        [_videoPlayer play];
    }
    return _videoPlayer;
}
- (UISegmentedControl *)segment {
    if (!_segment) {
        _segment = [[UISegmentedControl alloc] initWithItems:@[@"简介", @"评论"]];
        _segment.selectedSegmentIndex = 0;
        [_segment addTarget:self action:@selector(handleAction:) forControlEvents:(UIControlEventValueChanged)];
        [self.view addSubview:_segment];
        [_segment mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.view).offset(50);
            make.top.equalTo(self.videoPlayer.view.mas_bottom).offset(20);
            make.right.equalTo(self.view).offset(-50);
            make.height.equalTo(@(30));
        }];
    }
    return _segment;
}
- (UITextView *)textView {
    if (!_textView) {
        _textView = [UITextView new];
        _textView.backgroundColor = [UIColor whiteColor];
        [self.view addSubview:_textView];
        
        [_textView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.view).offset(20);
            make.top.equalTo(self.segment.mas_bottom).offset(20);
            make.right.equalTo(self.view).offset(-20);
            make.bottom.equalTo(self.view).offset(-20);
        }];
    }
    return _textView;
}
- (UILabel *)label {
    if (!_label) {
        _label = [UILabel new];
        _label.backgroundColor = [UIColor whiteColor];
        _label.hidden = YES;
        [self.view addSubview:self.label];
        
        [_label mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.view).offset(20);
            make.top.equalTo(self.segment.mas_bottom).offset(20);
            make.right.equalTo(self.view).offset(-20);
            make.bottom.equalTo(self.view).offset(-20);
        }];
    }
    return _label;
}
#pragma mark - enevt presents
- (void)handleAction:(UISegmentedControl *)seg {
    if (seg.selectedSegmentIndex == 0) {
        self.textView.hidden = NO;
        self.label.hidden = YES;
    } else {
        self.textView.hidden = YES;
        self.label.hidden = NO;
    }
}
@end
