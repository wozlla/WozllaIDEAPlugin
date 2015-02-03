(function() {
    var bridgeInvoke = window['bridgeInvoke'];
    var bridge = window.bridge = {};

    function createThrowFunc() {
        return function(msg) {
            bridgeInvoke('throw', msg);
        };
    }

    function loadScript(src, callback, error) {
        var script = document.createElement('script');
        script.src = src + '?__vest=' + Date.now() + '-' + Math.random();
        script.onload = function () {
            document.head.removeChild(script);
            callback && callback();
        };
        script.onerror = function() {
            error && error();
        };
        document.head.appendChild(script);
    }

    function loadExternals(callback) {
        loadScript('Editor/externals.js', callback, createThrowFunc());
    }

    window.onload = function() {
        var director = new WOZLLA.Director(document.getElementById("main"));

        var oMask = new WOZLLA.GameObject();
        oMask.addComponent(new WOZLLA.component.MaskCollider());
        oMask.loadAssets(function() {
            oMask.init();
        });
        oMask.z = 999999999;
        director.stage.addChild(oMask, true);

        director.renderer.layerManager.define("__internal_coordsGameObject", 999999);
        var coordGameObject = new WOZLLA.GameObject();
        var coordRenderer = new WOZLLA.component.SpriteRenderer();
        coordRenderer.imageSrc = '__internal_coords_image.png';
        coordRenderer.renderLayer = '__internal_coordsGameObject';
        coordGameObject.addComponent(coordRenderer);
        coordGameObject.loadAssets(function() {
            coordGameObject.init();
            coordGameObject.transform.setPosition(-6, -6);
            coordGameObject.transform.setScale(1, 1);
        });

        var super_visit = WOZLLA.GameObject.prototype.visit;
        WOZLLA.GameObject.prototype.selected = false;
        WOZLLA.GameObject.prototype.visit = function(renderer, parentTransform, flags) {
            flags = super_visit.apply(this, arguments);
            if(this.selected) {
                coordGameObject.transform.setScale(1, 1);
                coordGameObject.transform.dirty = true;
                coordGameObject.visit(renderer, this.transform, flags);
            }
        };

        var dirty = true;

        var builder;
        var rootGameObject;
        var selectedGameObject;

        requestAnimationFrame(function update() {
            requestAnimationFrame(update);
            if(dirty) {
                dirty = false;
                director.runStep(1);
            }
        });

        function load(callback) {
            var openJsonXData = bridgeInvoke('getOpenedJSONXData');
            builder = WOZLLA.jsonx.JSONXBuilder.create();
            builder.instantiateWithJSON({ root: JSON.parse(openJsonXData) });
            builder.load().init().build(function(error, root) {
                callback && callback(root);
            });
        }

        var reloadTimer;
        function reload2Stage(time, callback) {
            reloadTimer && clearTimeout(reloadTimer);
            reloadTimer = setTimeout(function() {
                reloadTimer = null;
                load(function(root) {
                    if(rootGameObject) {
                        rootGameObject.destroy();
                        rootGameObject.removeMe();
                    }
                    director.stage.addChild(root, true);
                    rootGameObject = root;
                    if(selectedGameObject) {
                        selectedGameObject = builder.getByUUID(selectedGameObject._uuid);
                        selectedGameObject.selected = true;
                    }
                    dirty = true;
                    callback && callback();
                });
            }, time || 500);
        }

        bridge.onGameObjectSelectionChange = function(uuid) {
            if(selectedGameObject) {
                selectedGameObject.selected = false;
            }
            if(uuid) {
                var gameObj = builder.getByUUID(uuid);
                gameObj.selected = true;
                selectedGameObject = gameObj;
            } else {
                selectedGameObject = null;
            }
            dirty = true;
        };

        bridge.onGameObjectPropertyChange = function(uuid, name, newValue, oldValue) {
            var gameObject = builder.getByUUID(uuid);
            switch(name) {
                case "id":
                case "name":
                    gameObject[name] = newValue;
                    break;
                case "active":
                case "visible":
                case "touchable":
                    gameObject[name] = newValue === 'true';
                    break;
                case "z":
                    gameObject.z = parseInt(newValue);
                    break;
            }
            dirty = true;
        };

        bridge.onTransformPropertyChange = function(uuid, name, newValue, oldValue) {
            var gameObject = builder.getByUUID(uuid);
            gameObject.transform[name] = name === 'relative' ? newValue === 'true' : parseFloat(newValue);
            if(gameObject.transform.dirty) {
                dirty = true;
            }
        };

        bridge.onComponentPropertyChange = function(uuid, name, newValue, oldValue) {
            reload2Stage(20);
        };

        bridge.onHierarchyChange = function() {
            reload2Stage(20);
        };

        loadExternals(function() {
            reload2Stage(0, function() {
                bridgeInvoke('updateComponentConfig', JSON.stringify(WOZLLA.Component.configMap));
                bridgeInvoke('editorReady');
            });
        });

    };

    // overrides
    (function() {
        WOZLLA.Component.register = function (ctor, config) {
            WOZLLA.Component.ctorMap[config.name] = ctor;
            WOZLLA.Component.configMap[config.name] = config;
            ctor.componentName = config.name;
        };

        WOZLLA.jsonx.JSONXBuilder.prototype._loadJSONData = function(callback) {
            var me = this;
            if(me.src && !me.data) {
                if(bridgeInvoke('isExistInProject', me.src)) {
                    me.data = JSON.parse(bridgeInvoke('readProjectFile', me.src));
                    callback && callback();
                } else {
                    me.err = me.src + 'not found';
                    callback && callback();
                }
            } else if(me.data) {
                callback && callback();
            } else {
                me.err = "fail to load data";
                callback && callback();
            }
        };

        WOZLLA.assets.SpriteAtlas.prototype._loadImage = function(callback) {
            var me = this;
            if(bridgeInvoke('isExistInProject', me.src)) {
                var image = new Image();
                image.src = 'data:image/png;base64,' + bridgeInvoke('readProjectFileAsBase64', me.src);
                image.onload = function() {
                    callback && callback(null, image);
                };
                image.onerror = function() {
                    callback('Fail to parse base64 image: ' + me.src);
                };
            } else {
                me.err = me.src + 'not found';
                callback && callback();
            }
        };
    })();
})();