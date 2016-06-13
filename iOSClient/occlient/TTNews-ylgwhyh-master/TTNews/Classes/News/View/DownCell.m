//
//  DownCell.m
//  TTNews
//
//  Created by 曾经 on 16/6/10.
//  Copyright © 2016年 瑞文戴尔. All rights reserved.
//

#import "DownCell.h"
#import "UIImageView+WebCache.h"
#import "Masonry.h"
#import "DownLoadTableViewController.h"
#import "UIView+PSSizes.h"

@interface DownCell () 
@property (nonatomic, strong) UIImageView *picture;
@property (nonatomic, strong) UILabel *titleLabel;
@end
@implementation DownCell
- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self creatUI];
        NSNotificationCenter *notiCenter = [NSNotificationCenter defaultCenter];
        
        //Observer: 监听对象, 让谁成为监听者,那么监听听到信息的方法就写在谁的.m中
        //selector: 监听到消息后触发的方法.
        //name:也就是我们电台中的频道, 该值是自己定的,我们以什么频道发送消息
        //object: 该频道指定的消息的唯一标识 nil:表示所有的频道 或者所有的消息都接受
        //
        [notiCenter addObserver:self selector:@selector(getNotifi:) name:@"FM-881" object:@"小韩讲故事"];
    }
    return self;
}
- (void)getNotifi:(NSNotificationCenter *)center {
    NSLog(@"--------%@", center);
}
#pragma mark - private methods
- (void)creatUI {
    self.picture = [UIImageView new];
    _picture.backgroundColor = [UIColor redColor];
    [self.contentView addSubview:self.picture];
    
    self.titleLabel = [UILabel new];
    //    _titleLabel.backgroundColor = [UIColor purpleColor];
    _titleLabel.numberOfLines = 2;
    _titleLabel.textAlignment = NSTextAlignmentLeft;
    [self.contentView addSubview:self.titleLabel];
    
    
    self.progressView = [UIProgressView new];
    _progressView.backgroundColor = [UIColor redColor];
    [self.contentView addSubview:_progressView];
    
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
    
    [self.progressView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(self.titleLabel.mas_bottom).offset(10);
        make.left.equalTo(self.picture.mas_right).offset(10);
        make.right.equalTo(_progressView.superview).offset(-10);
        make.height.equalTo(@(20));
    }];

  
}

#pragma mark - 赋值
- (void)configureCellDataWith:(HomeDetailModel *)model {
    [self.picture sd_setImageWithURL:model.piclink];
    self.titleLabel.text = model.title;
}
@end
