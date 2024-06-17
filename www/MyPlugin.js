var exec = require('cordova/exec');

var MyPlugin = {
    coolMethod: function(success, error) {
        exec(success, error, "MyPlugin", "coolMethod", [arg0]);
    }
};

module.exports = MyPlugin;
