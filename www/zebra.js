module.exports = {
    print: function (toPrint, isBase64, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "Zebra", "print", [toPrint, isBase64]);
    }
};
