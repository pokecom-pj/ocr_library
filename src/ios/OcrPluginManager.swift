//
//  OcrPluginManager.swift
//  IpcOcrSample
//
//  Created by 平山 裕也 on 2024/05/13.
//

import UIKit
import DriverCardOCRLibrary

// MARK: - Protocol
protocol OnOcrPluginManagerDelegate {
    // [errorCode]
    // 0: 成功
    // -1: 失敗
    // -2: 権限エラー
    func onResult(sender: OcrPluginManager, code: Int, result: OcrResultInfo?)
}

// MARK: - InnerClass
class OcrResultInfo {
    
    // MARK: - Member
    var mCardType: Int = 0
    var mName: String = ""
    var mAddress: String = ""
    var mBirthdate: String = ""
    var mGender: String = ""
    
}

class OcrPluginManager: DriverCardOCRDelegate {
    
    // MARK: - Define
    static let CODE_SUCCESS = 0
    static let CODE_FAILED = -1
    static let CODE_AUTHROIZE = -2

    static let CARDTYPE_MENKYOSYO = 1
    static let CARDTYPE_MYNUMBER = 2
    static let CARDTYPE_ZAIRYU = 3
    
    // MARK: - Member
    private var mDelegate: OnOcrPluginManagerDelegate? = nil
    
    // MARK: - DriverCardOCRDelegate
    func didScaned(result: [(String, String)], scanType: DriverCardOCRLibrary.SCAN_TYPE) {
        // 解析結果、result:項目名,項目内容のリスト　scanType:カードタイプ 0:運転免許証 1:マイナンバーカード 2:在留カード
        var info = OcrResultInfo()
        switch scanType {
            case SCAN_TYPE.DriverCard:
            info.mCardType = OcrPluginManager.CARDTYPE_MENKYOSYO
            result.forEach { set in
                if (set.0.compare("氏名") == .orderedSame) {
                    info.mName = set.1
                }
                if (set.0.compare("住所") == .orderedSame) {
                    info.mAddress = set.1
                }
                if (set.0.compare("生日") == .orderedSame) {
                    info.mBirthdate = set.1.replacingOccurrences(of: "生", with: "")
                }
            }
                break
            case SCAN_TYPE.ZairyuCard:
                info.mCardType = OcrPluginManager.CARDTYPE_ZAIRYU
                result.forEach { set in
                    if (set.0.compare("氏名") == .orderedSame) {
                        info.mName = set.1
                    }
                    if (set.0.compare("住所") == .orderedSame) {
                        info.mAddress = set.1
                    }
                    if (set.0.compare("生年月日") == .orderedSame) {
                        info.mBirthdate = set.1.replacingOccurrences(of: "生", with: "")
                    }
                    if (set.0.compare("性別") == .orderedSame) {
                        info.mGender = set.1
                    }
                }
                break
            case SCAN_TYPE.MyNumberCard:
                info.mCardType = OcrPluginManager.CARDTYPE_MYNUMBER
                var address1 = ""
                var address2 = ""
                result.forEach { set in
                    if (set.0.compare("氏名") == .orderedSame) {
                        info.mName = set.1
                    }
                    if (set.0.compare("住所①") == .orderedSame) {
                        address1 = set.1
                    }
                    if (set.0.compare("住所②") == .orderedSame) {
                        address2 = set.1
                    }
                    if (set.0.compare("生日") == .orderedSame) {
                        info.mBirthdate = set.1.replacingOccurrences(of: "生", with: "")
                    }
                    if (set.0.compare("性別") == .orderedSame) {
                        info.mGender = set.1
                    }
                }
                info.mAddress = address1 + address2
                break
            default:
                mDelegate?.onResult(sender: self, code: OcrPluginManager.CODE_FAILED, result: nil)
                return
        }
        mDelegate?.onResult(sender: self, code: OcrPluginManager.CODE_SUCCESS, result: info)
    }
    
    func canceled() {
        mDelegate?.onResult(sender: self, code: OcrPluginManager.CODE_FAILED, result: nil)
    }
    
    func staredProcessing() {
    }
    
    func endProcessing() {
    }
    
    // MARK: - Accesser
    func start(parent: UIViewController, delegate: OnOcrPluginManagerDelegate?) {
        UIApplication.shared.statusBarOrientation = .portrait

        mDelegate = delegate
        DriverCardOCR.shared.delegate = self
        DriverCardOCR.shared.parentController = parent
        DriverCardOCR.shared.doScanAutoCard()
    }
    
    func stop() {
        mDelegate = nil

        UIApplication.shared.statusBarOrientation = .landscapeLeft
    }
    
    
}
