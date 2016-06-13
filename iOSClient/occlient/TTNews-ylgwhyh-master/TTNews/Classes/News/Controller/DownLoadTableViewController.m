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

#define kDownLaodCell @"kDownLaodCell"
@interface DownLoadTableViewController ()
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
    [self downLoadVedio];
    self.tableView.rowHeight = 100;
//    [self.tableView reloadData];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}
- (void)setModel:(HomeDetailModel *)model {
    _model = model;
    [self downLoadVedio];
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
    HomeDetailModel *model = self.data[indexPath.row];
    DownCell *cell = [tableView dequeueReusableCellWithIdentifier:kDownLaodCell forIndexPath:indexPath];
//    cell.progressView.dell
    [cell configureCellDataWith:model];
    return cell;
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    HomeDetailModel *model = self.data[indexPath.row];

    DetailViewController *detVC = [[DetailViewController alloc] init];
    detVC.isCashe = YES;
    detVC.model = model;
    [self.navigationController pushViewController:detVC animated:YES];
}
#pragma mark - 下载
- (void)downLoadVedio {
    NSString *videoModelsKey = @"Video_Model_Key";
    [self.data removeAllObjects];
    // 读取本地已下载的所有模型
    NSArray *videoArray = [[NSUserDefaults standardUserDefaults] objectForKey:videoModelsKey];
    if (videoArray != nil) {
        self.data = [HomeDetailModel arrayOfModelsFromDictionaries:videoArray error:nil];
    }
    
    if (self.model == nil) {
        [self.tableView reloadData];
        return;
    }
    
    // 查询是否已缓存过
    __block BOOL found = NO;
    [self.data enumerateObjectsUsingBlock:^(HomeDetailModel *_Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if ([obj.item_id isEqualToString:self.model.item_id]) {
            found = YES;
            *stop = YES;
        }
    }];
    
    if (found) {
        NSLog(@"读取本地model数据");
    } else {
        //正式版本
//        NSString *key = [NSString stringWithFormat:@"videoId_key_%@", self.model.item_id];
//        NSString *folder = [self getPathWith];
//        NSString *fileName = [NSString stringWithFormat:@"%@.mp4", key];
//        self.model.coursePath = [folder stringByAppendingPathComponent:fileName];
        NSString *key = [NSString stringWithFormat:@"videoId_key_%@", self.model.item_id];
        NSString *folder = [self getPathWith];
        NSString *fileName = [NSString stringWithFormat:@"%@.mp4", key];
        self.model.coursePath = [folder stringByAppendingPathComponent:fileName];
       
        
        [HYBNetworking downloadWithUrl:[NSString stringWithFormat:@"%@", self.model.courselink] saveToPath:self.model.coursePath progress:^(int64_t bytesRead, int64_t totalBytesRead) {
            NSLog(@"%lld %lld", bytesRead, totalBytesRead);
            long pre = bytesRead/totalBytesRead;
            self.present = [NSString stringWithFormat:@"%.2lld", bytesRead/totalBytesRead];
            
        } success:^(id response) {
            // 下载成功后，保存到本地
            __block BOOL found = NO;
            [self.data enumerateObjectsUsingBlock:^(HomeDetailModel *_Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                if ([obj.item_id isEqualToString:self.model.item_id]) {
                    found = YES;
                    *stop = YES;
                }
            }];
            
            if (!found) {
                [self.data addObject:self.model];
                NSArray *dictArray = [HomeDetailModel arrayOfDictionariesFromModels:self.data];
                [[NSUserDefaults standardUserDefaults] setObject:dictArray forKey:videoModelsKey];
                [[NSUserDefaults standardUserDefaults] synchronize];
                [self.tableView reloadData];
            }
//            // 下载成功后，保存到本地
//            [self.data addObject:self.model];
//            NSArray *dictArray = [HomeDetailModel arrayOfDictionariesFromModels:self.data];
//            [[NSUserDefaults standardUserDefaults] setObject:dictArray forKey:videoModelsKey];
//            [[NSUserDefaults standardUserDefaults] synchronize];
//            [self.tableView reloadData];
        } failure:^(NSError *error) {
    }];
    }
    [self.tableView reloadData];
}
#pragma mark - 路径
- (NSString *)getPathWith {
   NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    
    return [paths firstObject];
}
@end
