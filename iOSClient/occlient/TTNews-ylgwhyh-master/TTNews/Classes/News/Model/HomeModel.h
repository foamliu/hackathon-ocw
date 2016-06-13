//
//  HomeModel.h
//  LearnWhat
//
//  Created by 曾经 on 16/6/5.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import <JSONModel/JSONModel.h>

@interface HomeDetailModel : JSONModel
@property (nonatomic, strong) NSString <Optional> *item_id;
@property (nonatomic, strong) NSString <Optional> *title;
@property (nonatomic, strong) NSString <Optional> *des;
@property (nonatomic, strong) NSURL <Optional> *piclink;
@property (nonatomic, strong) NSURL <Optional> *courselink;
@property (nonatomic, strong) NSString <Optional> *duration;
@property (nonatomic, strong) NSString <Optional> *source;
@property (nonatomic, strong) NSString <Optional> *school;
@property (nonatomic, strong) NSString <Optional> *instructor;
@property (nonatomic, strong) NSString <Optional> *language;
@property (nonatomic, strong) NSString <Optional> *tags;
@property (nonatomic, strong) NSURL <Optional> *link;
@property (nonatomic, strong) NSString <Optional> *enabled;

@property (nonatomic, strong) NSString <Optional> *picPath;
@property (nonatomic, strong) NSString <Optional> *coursePath;
//- (void)setNilValueForKey:(NSString *)key
@end
@protocol HomeDetailModel  <NSObject>
@end

//@interface HomeModelList : JSONModel
//@property (nonatomic, strong) NSMutableArray <> *courses;
//@end
@interface HomeModel : JSONModel
@property (nonatomic, strong) NSMutableArray <ConvertOnDemand, HomeDetailModel, Optional> *courses;
@end

