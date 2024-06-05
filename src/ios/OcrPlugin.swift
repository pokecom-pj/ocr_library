//
//  OcrPlugin.swift
//  CardReaderSample
//
//  Created by 平山 裕也 on 2023/12/04.
//

import Foundation
import UIKit

@objc(OcrPlugin)
class OcrPlugin : CDVPlugin, OnOcrPluginManagerDelegate {
    
    // MARK: - Member
    private var mOcrPluginManager: OcrPluginManager? = nil
    private var mOcrCallbackId: String? = nil
    
    // MARK: - OnOcrPluginManagerDelegate
    func onResult(sender: OcrPluginManager, code: Int, result: OcrResultInfo?) {
        stopOcr()
        if (mOcrCallbackId != nil) {
            if (code == OcrPluginManager.CODE_SUCCESS) {
                let inType = result?.mCardType ?? 0
                let inName = result?.mName ?? ""
                let inGender = result?.mGender ?? ""
                let inAddress = result?.mAddress ?? ""
                let inBirthdate = result?.mBirthdate ?? ""
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: ["errorCode": 0, "type": inType, "name": inName, "gender": inGender, "address": inAddress, "birthdate": inBirthdate ])
                pluginResult?.keepCallback = true
                commandDelegate.send(pluginResult, callbackId: mOcrCallbackId)
            } else if (code == OcrPluginManager.CODE_AUTHROIZE) {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: ["errorCode": -2] )
                pluginResult?.keepCallback = true
                commandDelegate.send(pluginResult, callbackId: mOcrCallbackId)
            } else {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: ["errorCode": -1] )
                pluginResult?.keepCallback = true
                commandDelegate.send(pluginResult, callbackId: mOcrCallbackId)
            }
        }
    }
        
    // MARK: - Accesser
    @objc(startOCR:)
    func startOCR(command: CDVInvokedUrlCommand) {
        mOcrCallbackId = command.callbackId
        startOcr()
    }
    
    // MARK: - Function
    private func getOcrPluginManager() -> OcrPluginManager {
        if (mOcrPluginManager == nil) {
            mOcrPluginManager = OcrPluginManager()
        }
        return mOcrPluginManager!
    }
    
    private func startOcr() {
        if (viewController == nil) {
            if (mOcrCallbackId != nil) {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: ["errorCode": -1] )
                pluginResult?.keepCallback = true
                commandDelegate.send(pluginResult, callbackId: mOcrCallbackId)
            }
            return
        }
        let parent = OcrViewController()
        getOcrPluginManager().start(parent: parent, delegate: self)
        viewController.present(parent, animated: true)
        
//        getOcrPluginManager().start(parent: viewController!, delegate: self)
    }
    
    private func stopOcr() {
        getOcrPluginManager().stop()
        mOcrPluginManager = nil
    }

}
