//
//  ColumnCell.m
//  LearnWhat
//
//  Created by 曾经 on 16/6/4.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import "ColumnCell.h"
#import "UIImageView+WebCache.h"
#import "Masonry.h"
#import "DownLoadTableViewController.h"
#import "UIView+PSSizes.h"
//100
@interface ColumnCell ()
@property (nonatomic, strong) UIImageView *picture;
@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UILabel *sourceLabel;
@property (nonatomic, strong) UILabel *timeLabel;
@property (nonatomic, strong) UIButton *downBtn;

@property (nonatomic, strong) HomeDetailModel *model;
@end
@implementation ColumnCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self creatUI];
    }
    return self;
}
#pragma mark - private methods
- (void)creatUI {
    self.picture = [UIImageView new];
    _picture.backgroundColor = [UIColor lightGrayColor];
    [self.contentView addSubview:self.picture];
    
    self.titleLabel = [UILabel new];
//    _titleLabel.backgroundColor = [UIColor purpleColor];
    _titleLabel.numberOfLines = 2;
    _titleLabel.textAlignment = NSTextAlignmentLeft;
    [self.contentView addSubview:self.titleLabel];
    
    
    self.sourceLabel = [UILabel new];
//    _sourceLabel.backgroundColor = [UIColor greenColor];
    _sourceLabel.textAlignment = NSTextAlignmentLeft;
    [self.contentView addSubview:self.sourceLabel];
    
    self.timeLabel = [UILabel new];
//    _timeLabel.backgroundColor = [UIColor yellowColor];
    _timeLabel.textColor = [UIColor grayColor];
    _timeLabel.textAlignment = NSTextAlignmentCenter;
    [self.contentView addSubview:self.timeLabel];

    
    self.downBtn = [UIButton buttonWithType:(UIButtonTypeCustom)];
//    _downBtn.backgroundColor = [UIColor grayColor];
    [_downBtn addTarget:self action:@selector(handleDown:) forControlEvents:(UIControlEventTouchUpInside)];
    [_downBtn setImage:[UIImage imageNamed:@"cell_downlaod"] forState:(UIControlStateNormal)];
    [self.contentView addSubview:self.downBtn];
    
    
    [self.picture mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.left.equalTo(_picture.superview).offset(10);
        make.bottom.equalTo(_picture.superview).offset(-10);
        make.width.equalTo(@(80));
    }];
    
    [self.titleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(_picture.mas_right).offset(10);
        make.top.equalTo(_titleLabel.superview).offset(10);
        make.right.equalTo(_titleLabel.superview).offset(-10);
        make.height.equalTo(@(40));
    }];
    
    [self.sourceLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.picture.mas_right).offset(10);
        make.top.equalTo(self.titleLabel.mas_bottom).offset(10);
        make.bottom.equalTo(_sourceLabel.superview).offset(-10);
        make.width.equalTo(@(100));
    }];
    
    [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(_sourceLabel.mas_right);
        make.top.equalTo(_titleLabel.mas_bottom).offset(10);
        make.bottom.equalTo(_sourceLabel.superview).offset(-10);

        make.width.equalTo(_sourceLabel.mas_width);
//        make.height.equalTo(_sourceLabel.mas_height);
    }];
    
    [self.downBtn mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(_downBtn.superview).offset(-10);
        make.top.equalTo(_titleLabel.mas_bottom).offset(10);
        make.bottom.equalTo(_downBtn.superview).offset(-10);
        make.width.equalTo(@(30));
    }];
}
#pragma mark - event present
- (void)handleDown:(UIButton *)sender {
    DownLoadTableViewController *downVC = [[DownLoadTableViewController alloc] init];
    downVC.model = self.model;
    [[self firstAvailableUIViewController].navigationController pushViewController:downVC animated:YES];
}
#pragma mark - 赋值
- (void)configureDataWith:(HomeDetailModel *)model {
    self.model = model;
    [self.picture sd_setImageWithURL:model.piclink placeholderImage:nil];
    self.titleLabel.text = model.title;
    self.sourceLabel.text = model.source;
    self.timeLabel.text = model.duration;
    if (model.duration.length == 0) {
        self.downBtn.hidden = YES;
    } else {
       self.downBtn.hidden = NO;
    }
}
@end
