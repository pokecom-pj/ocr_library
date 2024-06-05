#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');

module.exports = function (context) {
    const { projectRoot, plugin } = context.opts;
    const { ConfigParser } = context.requireCordovaModule('cordova-common');

    const platformPath = path.resolve(projectRoot, 'platforms/android');
    const config = new ConfigParser(path.resolve(platformPath, 'app/src/main/res/xml/config.xml'));
    const accessBackgroundLocation = config.getPreference('accessBackgroundLocation', 'android');

    const targetSdkVersion = getTargetSdkVersion(platformPath);
    if (!targetSdkVersion) {
        console.log(plugin.id + ': WARNING - unable to find Android SDK version');
    }

    const manifestPath = path.resolve(platformPath, 'app/src/main/AndroidManifest.xml');
    if (!fs.existsSync(manifestPath)) {
        throw "Can't find AndroidManifest.xml in platforms/Android";
    }

    let manifestChanged = false;
    let androidManifest = fs.readFileSync(manifestPath).toString();
    if (accessBackgroundLocation == 'true' && androidManifest.indexOf('ACCESS_BACKGROUND_LOCATION') == -1) {
        androidManifest = insertPermission(
            androidManifest,
            ' android:maxSdkVersion="30" android:name="android.permission.ACCESS_BACKGROUND_LOCATION"'
        );
        manifestChanged = true;
    }

    if (targetSdkVersion <= 30) {
        // Strip out Android 12+ changes
        androidManifest = stripPermission(androidManifest, 'BLUETOOTH_SCAN');
        androidManifest = stripPermission(androidManifest, 'BLUETOOTH_CONNECT');
        androidManifest = stripMaxSdkVersion(androidManifest, '30');
        manifestChanged = true;
    }

    if (targetSdkVersion <= 28) {
        // Strip out Android 10+ changes
        androidManifest = stripPermission(androidManifest, 'ACCESS_FINE_LOCATION');
        androidManifest = stripPermission(androidManifest, 'ACCESS_BACKGROUND_LOCATION');
        androidManifest = stripMaxSdkVersion(androidManifest, '28');
        manifestChanged = true;
    }

    if (manifestChanged) {
        fs.writeFileSync(manifestPath, androidManifest);
    }

    checkForDuplicatePermissions(plugin, androidManifest);

    setGradleProperties();
};

function setGradleProperties() {
    const PLUGIN_NAME = "cordova-plugin-androidx";
    const jvmargs = "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8";
    const enableAndroidX = "android.useAndroidX=true";
    const setJetifier = "android.enableJetifier=false";
    const style = "kotlin.code.style=official";
    const nonTransitiveRClass = "android.nonTransitiveRClass=true";
    const gradlePropertiesPath = "./platforms/android/gradle.properties";

    let gradleProperties = fs.readFileSync(gradlePropertiesPath);
    if (gradleProperties) {
        let updatedGradleProperties = false;
        gradleProperties = gradleProperties.toString();
        if (!gradleProperties.match(jvmargs)) {
            gradleProperties += "\n" + jvmargs;
            updatedGradleProperties = true;
        }
        if (!gradleProperties.match(enableAndroidX)) {
            gradleProperties += "\n" + enableAndroidX;
            updatedGradleProperties = true;
        }
        if (!gradleProperties.match(setJetifier)) {
            gradleProperties += "\n" + setJetifier;
            updatedGradleProperties = true;
        }
        if (!gradleProperties.match(style)) {
            gradleProperties += "\n" + style;
            updatedGradleProperties = true;
        }
        if (!gradleProperties.match(nonTransitiveRClass)) {
            gradleProperties += "\n" + nonTransitiveRClass;
            updatedGradleProperties = true;
        }
        if (updatedGradleProperties) {
            fs.writeFileSync(gradlePropertiesPath, gradleProperties, 'utf8');
        }
    } else {
//        log("gradle.properties file not found!")
    }

}

function checkForDuplicatePermissions(plugin, androidManifest) {
    const permissionsRegex = /<uses-permission.*?android:name="(?<permission>android\.permission\..*?)".*?\/>/gm;
    const permissions = {};
    let capture;
    while ((capture = permissionsRegex.exec(androidManifest)) !== null) {
        const permission = capture.groups && capture.groups.permission;
        if (permission && permissions[permission] && permissions[permission] != capture[0]) {
            console.log(plugin.id + ': !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!');
            console.log(plugin.id + ': WARNING - duplicate android permissions found: ' + permission);
            console.log(plugin.id + ': See https://github.com/don/cordova-plugin-ble-central/issues/925');
            console.log(plugin.id + ': !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!');
            break;
        }
        permissions[permission] = capture[0];
    }
}

function getTargetSdkVersion(platformPath) {
    let sdkVersion;
    const gradleConfigJson = path.resolve(platformPath, 'cdv-gradle-config.json');
    const gradleConfigProperties = path.resolve(platformPath, 'gradle.properties');

    if (fs.existsSync(gradleConfigJson)) {
        const gradleConfig = JSON.parse(fs.readFileSync(gradleConfigJson).toString());
        sdkVersion = gradleConfig.SDK_VERSION;
    } else if (fs.existsSync(gradleConfigProperties)) {
        const gradleConfig = fs.readFileSync(gradleConfigProperties).toString();
        sdkVersion = gradleConfig
            .split('\n')
            .map((l) => l.split('='))
            .filter(([key]) => key == 'cdvTargetSdkVersion')
            .map(([_, value]) => value);
    }

    return Number(sdkVersion || 0);
}

function insertPermission(androidManifest, text) {
    const permissionMatcher = /\n\s*?<uses-permission/;
    const template = permissionMatcher.exec(androidManifest);
    const toInsert = template + text + ' />\n';
    return (
        androidManifest.substring(0, template.index + 1) +
        toInsert +
        androidManifest.substring(template.index, androidManifest.length)
    );
}

function stripPermission(androidManifest, permission) {
    const replacer = new RegExp(
        '\\n\\s*?<uses-permission.*? android:name="android\\.permission\\.' + permission + '".*?\\/>\\n',
        'gm'
    );
    return androidManifest.replace(replacer, '\n');
}

function stripMaxSdkVersion(androidManifest, level) {
    const replacer = new RegExp('\\s*android:maxSdkVersion="' + level + '"\\s*', 'g');
    return androidManifest.replace(replacer, ' ');
}
