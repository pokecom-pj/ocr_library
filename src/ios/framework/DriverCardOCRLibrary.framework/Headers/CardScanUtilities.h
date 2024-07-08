//
//  CashCardScanUtilities.h
//
//  Created by IP-Consulitng on 2024/01/01.
//  https://www.ip-consulitng.co.jp
//  Copyright © 2024 IP-Consulitng. All rights reserved.
//
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

NS_ASSUME_NONNULL_BEGIN
typedef struct {
    float x;
    float y;
} point_s;

typedef struct {
    point_s top_left;
    point_s bottom_left;
    point_s top_right;
    point_s bottom_right;
} corner_points_s;

@interface corner_line : NSObject
    @property point_s start;
    @property point_s end;
@end
typedef struct {
    corner_points_s corner_points;
    bool foundCard;
} result;

typedef void (^CompletionHandler)(bool foundCard);

@interface CardScanUtilities : NSObject
+ (instancetype)shared;
@property int ocrType;
@property int cardType;
@property NSMutableArray *resultList;
-(result) doScan:(UIImage *)oriImage completionHandler:(CompletionHandler)completionHandler;
-(UIImage *)imageFromSampleBufferYUV:(CMSampleBufferRef)sampleBuffer;
-(void)doOcr;
-(int)licenseCheck;
@end

NS_ASSUME_NONNULL_END
