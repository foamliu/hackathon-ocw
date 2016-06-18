//
//  DownLoadTableViewController.m
//  TTNews
//
//  Created by 曾经 on 16/6/7.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import "DownLoadTableViewController.h"
#import "HYBNetworking.h"
#import "FMDBHelper.h"
#import "DownCell.h"
#import "DetailViewController.h"
#import "ZFDownloadManager.h"
#import "ZFSessionModel.h"

#define kDownLaodCell @"kDownLaodCell"
@interface DownLoadTableViewController ()<ZFDownloadDelegate>
@property (nonatomic, strong) NSMutableArray *data;
@property (nonatomic, copy) NSString *present;
@end

@implementation DownLoadTableViewController
- (NSMutableArray *)data {
    if (!_data) {
        _data = [NSMutableArray array];
    }
    return _data;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.tableView registerClass:[DownCell class] forCellReuseIdentifier:kDownLaodCell];
    //进来就要要获取以前缓存的,展示到当前页面
//    [self downLoadVedio];
    self.tableView.rowHeight = 100;
    [ZFDownloadManager sharedInstance].delegate = self;
    // 更新数据源
    [self initData];
    [self.tableView reloadData];
}
- (void)initData
{
    NSMutableArray *downladed = [ZFDownloadManager sharedInstance].downloadedArray;
    NSMutableArray *downloading = [ZFDownloadManager sharedInstance].downloadingArray;
    self.data = @[].mutableCopy;
    [self.data addObjectsFromArray:downladed];
    [self.data addObjectsFromArray:downloading];
    
    [self.tableView reloadData];
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}
- (void)setModel:(HomeDetailModel *)model {
//    _model = model;
//    [self downLoadVedio];
}
- (void)setPresent:(NSString *)present {
    _present = present;
    NSDictionary *dic = @{@"pregress" : self.present};
    NSNotification *notification = [NSNotification notificationWithName:@"FM-881" object:@"小韩讲故事" userInfo:dic];
    //发送消息
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}
#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.data.count;
//    return 10;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    __block ZFSessionModel *downloadObject = self.data[indexPath.row];

    HomeDetailModel *model = self.data[indexPath.row];
    DownCell *cell = [tableView dequeueReusableCellWithIdentifier:kDownLaodCell forIndexPath:indexPath];
    
    cell.sessionModel = downloadObject;
//    cell.progressView.dell
    
//    cell.downloadBlock = ^(UIButton *sender) {
//        [[ZFDownloadManager sharedInstance] download:[model.courselink absoluteString]progress:^(CGFloat progress, NSString *speed, NSString *remainingTime, NSString *writtenSize, NSString *totalSize) {} state:^(DownloadState state) {}];
//    };
//    [cell configureCellDataWith:model];
    return cell;
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    __block ZFSessionModel *downloadObject = self.data[indexPath.row];
    
    
    HomeDetailModel *model = [[ZFDownloadManager sharedInstance] getFileInfoModel:downloadObject.url];
    if (model) {
         NSString *str = [NSString stringWithFormat:@"%@/%@", ZFCachesDirectory, downloadObject.fileName];
        DetailViewController *detVC = [[DetailViewController alloc] init];
        detVC.isCashe = YES;
        model.coursePath = str;
        detVC.model = model;
        [self.navigationController pushViewController:detVC animated:YES];
    }
  
}
#pragma mark - 下载
- (void)downLoadVedio {
    NSString *url = [NSString stringWithFormat:@"%@", self.model.courselink];
    [[ZFDownloadManager sharedInstance] download:url progress:^(CGFloat progress, NSString *speed, NSString *remainingTime, NSString *writtenSize, NSString *totalSize) {
        
    } state:^(DownloadState state) {
        
    }];
}

#pragma mark - 路径
- (NSString *)getPathWith {
   NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    
    return [paths firstObject];
}


#pragma mark - ZFDownloadDelegate

- (void)downloadResponse:(ZFSessionModel *)sessionModel
{
    if (self.data) {
        // 取到对应的cell上的model
//        NSArray *downloadings = self.data[1];
//        if ([downloadings containsObject:sessionModel]) {
        
            NSInteger index = [self.data indexOfObject:sessionModel];
            NSIndexPath *indexPath = [NSIndexPath indexPathForRow:index inSection:0];
            __block DownCell *cell = [self.tableView cellForRowAtIndexPath:indexPath];
            __weak typeof(self) weakSelf = self;
            sessionModel.progressBlock = ^(CGFloat progress, NSString *speed, NSString *remainingTime, NSString *writtenSize, NSString *totalSize) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    
//                    cell.progressLabel.text   = [NSString stringWithFormat:@"%@/%@ (%.2f%%)",writtenSize,totalSize,progress*100];
//                    cell.speedLabel.text      = speed;
//                    cell.progress.progress    = progress;
//                    cell.downloadBtn.selected = YES;
//                    
                    cell.progressView.progress = progress;
                });
            };
        
        [self.tableView reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationNone];
            sessionModel.stateBlock = ^(DownloadState state){
                dispatch_async(dispatch_get_main_queue(), ^{
                    // 更新数据源
                    if (state == DownloadStateCompleted) {
                        [weakSelf initData];
//                        cell.downloadBtn.selected = NO;
                    }
                    // 暂停
                    if (state == DownloadStateSuspended) {
//                        cell.speedLabel.text = @"已暂停";
//                        cell.downloadBtn.selected = NO;
                    }
                });
            };
        }
//    }
}

@end
