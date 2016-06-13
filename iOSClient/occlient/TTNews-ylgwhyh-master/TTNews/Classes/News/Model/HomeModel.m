//
//  HomeModel.m
//  LearnWhat
//
//  Created by 曾经 on 16/6/5.
//  Copyright © 2016年 sandy. All rights reserved.
//

#import "HomeModel.h"

#define VerifyValue(value)\
({id tmp;\
if ([value isKindOfClass:[NSNull class]])\
tmp = nil;\
else\
tmp = value;\
tmp;\
})\

@implementation HomeModel
+(BOOL)propertyIsOptional:(NSString*)propertyName
{
    return YES;
}
@end
@implementation HomeDetailModel

+(BOOL)propertyIsOptional:(NSString*)propertyName
{
//    if ([propertyName isEqualToString:@"des"]) {
//        propertyName = @"description";
//    }
    return YES;
}

@end