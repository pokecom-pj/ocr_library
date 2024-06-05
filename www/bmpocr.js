function BMOcrPlugin() {};

// Reader
BMOcrPlugin.prototype.startOCR = function (callback) {
	cordova.exec(function(result){ callback(result); }, function(err){}, "OcrPlugin", "startOCR", []);
};

BMOcrPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.bmpocr = new BMOcrPlugin();
  return window.plugins.bmpocr;
};
cordova.addConstructor(BMOcrPlugin.install);