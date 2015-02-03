(function() {

    var bridgeInvoke = window['bridgeInvoke'];
    if(bridgeInvoke('isExistInProject', 'Editor/runtime.json')) {
        var runtimeJsonText = bridgeInvoke('readProjectFile', 'Editor/runtime.json');
        var data = JSON.parse(runtimeJsonText);
        data.forEach(function (src) {
            console.log('runtime load: ' + src);
            document.write('<script type="text/javascript" src="' + src + '"><\/script>')
        });
    } else {
        bridgeInvoke('throw', 'Editor/runtime.json not exists.');
    }

})();