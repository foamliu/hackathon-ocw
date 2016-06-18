//
//  ContentTableViewController.m
//  TTNews
//
//  Created by 瑞文戴尔 on 16/3/26.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import "ContentTableViewController.h"
#import <MJRefresh.h>
#import <SVProgressHUD.h>
#import "SinglePictureNewsTableViewCell.h"
#import "MultiPictureTableViewCell.h"
#import "NoPictureNewsTableViewCell.h"
#import "TTNormalNews.h"
#import "TTHeaderNews.h"
//#import "DetailViewController.h"
#import "ShowMultiPictureViewController.h"
#import "TTNormalNewsFetchDataParameter.h"
#import "TTDataTool.h"
#import "TTConst.h"
#import "UIImageView+Extension.h"
#import "TTImageCyclePlayView.h"

#import "HYBNetworking.h"
#import "HomeModel.h"
#import "ColumnCell.h"
#import "WebViewController.h"
#import "DetailViewController.h"

#define NSLog(...) {}

@interface ContentTableViewController ()<TTImageCyclePlayViewDelegate>

@property (nonatomic, strong) NSMutableArray *headerNewsArray;
@property (nonatomic, assign) NSInteger currentPage;
@property (nonatomic, strong) NSMutableArray *normalNewsArray;
@property (nonatomic, weak) TTImageCyclePlayView *headerView;
@property (nonatomic, copy) NSString *currentSkinModel;

@end

static NSString * const kNewCell = @"newCell";
static NSString * const singlePictureCell = @"SinglePictureCell";
static NSString * const multiPictureCell = @"MultiPictureCell";
static NSString * const noPictureCell = @"NoPictureCell";

@implementation ContentTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupBasic];
    [self setupRefresh];
    [self.tableView registerClass:[ColumnCell class] forCellReuseIdentifier:kNewCell];
    self.tableView.rowHeight = 100;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
//    self.navigationController.navigationBar.hidden = YES
//    [self setupHeader];
}

-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateSkinModel) name:SkinModelDidChangedNotification object:nil];
    [self updateSkinModel];
}

-(void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.headerView removeTimer];
    [SVProgressHUD dismiss];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark --private Method--设置tableView
-(void)setupBasic {
    self.automaticallyAdjustsScrollViewInsets = NO;
    self.tableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.tableView.scrollIndicatorInsets = UIEdgeInsetsMake(104, 0, 0, 0);
}

#pragma mark --private Method--初始化轮播图View
-(void)setupHeader {
    TTImageCyclePlayView *headerView = [[TTImageCyclePlayView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.width*9/16)];
    headerView.delegate = self;
    self.headerView = headerView;
    self.tableView.tableHeaderView = headerView;
}

#pragma mark --private Method--初始化刷新控件
-(void)setupRefresh {
    self.tableView.mj_header = [MJRefreshNormalHeader headerWithRefreshingTarget:self refreshingAction:@selector(loadNewData)];
    self.tableView.mj_header.automaticallyChangeAlpha = YES;
    [self.tableView.mj_header beginRefreshing];
    self.tableView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingTarget:self refreshingAction:@selector(loadMoreData)];
    self.currentPage = 1;
}

#pragma mark --private Method--下拉刷新数据
- (void)loadNewData {
    [SVProgressHUD show];
    [self fetchNewHeaderNews];
    [self fetchNewNormalNews];
}

#pragma mark --private Method--获取最新的轮播图新闻数据
-(void)fetchNewHeaderNews {
    [self.headerView removeTimer];
    [TTDataTool TTHeaderNewsFromServerOrCacheWithMaxTTHeaderNews:self.headerNewsArray.lastObject success:^(NSMutableArray *array) {
        [SVProgressHUD dismiss];
        self.headerNewsArray = array;
        NSMutableArray *imageUrls = [NSMutableArray array];
        NSMutableArray *titles = [NSMutableArray array];
        for (TTHeaderNews *news in self.headerNewsArray){
            [imageUrls addObject:news.image_url];
            [titles addObject:news.title];
        }
        self.headerView.imageUrls = [imageUrls copy];
        self.headerView.titles = [titles copy];
        self.headerView.currentMiddleImageViewIndex = 0;
        [self.headerView updateImageViewsAndTitleLabel];
        [self.headerView addTimer];
        [self.tableView reloadData];
        } failure:^(NSError *error) {
            [SVProgressHUD dismiss];
            [SVProgressHUD showErrorWithStatus:@"加载失败！"];
            [self.tableView.mj_header endRefreshing];
            NSLog(@"%@fetchHeaderNews%@",self, error);
    }];
}

#pragma mark --private Method--获取最新的普通新闻数据（也就是非轮播图数据）
-(void)fetchNewNormalNews {

//    NSLog(@"---------------------&@", self.keyString);
    
    NSString *url ;
    if ([self.keyString isEqualToString:@"推荐"]) {
        url = @"http://jieko.cc/user/5/Candidates";
    } else {
        url= [[NSString stringWithFormat:@"http://jieko.cc/user/5/Candidates/tag/%@", self.keyString] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    }
    [HYBNetworking getWithUrl:url
                 refreshCache:YES
                       params:nil
                      success:^(id response) {
                          NSLog(@"%@", response);
                          HomeModel *model = [[HomeModel alloc] initWithDictionary:response error:nil];
                          
                          self.normalNewsArray = model.courses;
                          [SVProgressHUD dismiss];
                          [self.tableView reloadData];
                          [self.tableView.mj_header endRefreshing];
                      } fail:^(NSError *error) {
                          NSLog(@"%@", error);
                          [SVProgressHUD dismiss];
                          [SVProgressHUD showErrorWithStatus:@"加载失败！"];
                          [self.tableView.mj_footer endRefreshing];
                          [self.tableView reloadData];
                      }];

}

#pragma mark --private Method--上拉刷新数据
-(void)loadMoreData {
    [SVProgressHUD show];
    
    [HYBNetworking getWithUrl:[[NSString stringWithFormat:@"http://jieko.cc/user/5/Candidates/tag/%@", self.keyString] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]
                 refreshCache:YES
                       params:nil
                      success:^(id response) {
                          NSLog(@"%@", response);
                          HomeModel *model = [[HomeModel alloc] initWithDictionary:response error:nil];
                          //                          self.data = model.courses;
                          //                          [self reloadData];
                          for (HomeDetailModel *mod in model.courses) {
                              [self.normalNewsArray addObject:mod];
                          }
                          [SVProgressHUD dismiss];
                          [self.tableView reloadData];
                         [self.tableView.mj_footer endRefreshing];
                      } fail:^(NSError *error) {
                          [SVProgressHUD dismiss];
                          [SVProgressHUD showErrorWithStatus:@"加载失败！"];
                          [self.tableView.mj_footer endRefreshing];
                          [self.tableView reloadData];
                          NSLog(@"%@", error);
                      }];
    
}


#pragma mark -UITableViewDataSource 返回tableView每一组有多少行
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.normalNewsArray.count;
//    return 10;
}

#pragma mark -UITableViewDataSource 返回indexPath对应的cell
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    HomeDetailModel *model = self.normalNewsArray[indexPath.row];

    ColumnCell *cell = [tableView dequeueReusableCellWithIdentifier:kNewCell];
     [cell configureDataWith:model];
        return cell;
}
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    HomeDetailModel *model = self.normalNewsArray[indexPath.row];
    if (model.duration.length != 0) {
        DetailViewController *detVC = [[DetailViewController alloc] init];
        detVC.model = model;
        [self.navigationController pushViewController:detVC animated:YES];
    } else {
        WebViewController *webVC = [[WebViewController alloc] init];
        webVC.model = model;
        [self.navigationController pushViewController:webVC animated:YES];
    }
}



#pragma mark -UIScrollViewDelegate scrollView将要开始滑动
- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    [self.headerView removeTimer];
}

#pragma mark -UIScrollViewDelegate scrollView已经停止拖动（手指离开屏幕时调用）
-(void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    //判断headerview是否在视野内
    if (self.tableView.contentOffset.y <= self.headerView.frame.size.height) {
        [self.headerView addTimer];
    }
}

#pragma mark -TTImageCyclePlayViewDelegate 点击了轮播图当前播放的imageView
- (void)clickCurrentImageViewInImageCyclePlay {
    TTHeaderNews *news = self.headerNewsArray[self.headerView.currentMiddleImageViewIndex];
    [self pushToDetailViewControllerWithUrl:news.url];
}

#pragma mark --private Method--点击了某一条新闻，调转到新闻对应的网页去
-(void)pushToDetailViewControllerWithUrl:(NSString *)url {
    DetailViewController *viewController = [[DetailViewController alloc] init];
//    viewController.url = url;
    [self.navigationController pushViewController:viewController animated:YES];
}

#pragma mark --private Method--更新皮肤模式 接到模式切换的通知后会调用此方法
-(void)updateSkinModel {
    self.currentSkinModel = [[NSUserDefaults standardUserDefaults] stringForKey:CurrentSkinModelKey];
    if ([self.currentSkinModel isEqualToString:NightSkinModelValue]) {//夜间模式
        self.tableView.backgroundColor = [UIColor blackColor];
        [self.headerView updateToNightSkinMode];
    } else {//日间模式
        self.tableView.backgroundColor = [UIColor colorWithRed:250.0/255.0 green:250.0/255.0 blue:250.0/255.0 alpha:1.0];
        [self.headerView updateToDaySkinMode];
    }
    [self.tableView reloadData];
}

#pragma mark --懒加载--normalNewsArray
-(NSMutableArray *)normalNewsArray {
    if (!_normalNewsArray) {
        _normalNewsArray = [NSMutableArray array];
    }
    return _normalNewsArray;
}

#pragma mark --懒加载--headerNewsArray
-(NSMutableArray *)headerNewsArray {
    if (!_headerNewsArray) {
        _headerNewsArray = [NSMutableArray array];
    }
    return _headerNewsArray;
}

@end
