(function() {
    var bridgeInvoke = window['bridgeInvoke'];
    var bridge = window.bridge = {};
    var editorInstance;

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

    function EditorInstance() {

        var super_visit = WOZLLA.GameObject.prototype.visit;
        var wrapper = document.getElementById("main-wrapper");
        var coordGameObject;
        var scale = 0.5;

        this.director = null;

        this.onResize = function() {
            if(!this.director) return;
            var width = parseInt(this.director.view.width+20) * scale;
            var height = parseInt(this.director.view.height+20) * scale;
            wrapper.style.width = width + 'px';
            wrapper.style.height = height + 'px';
            if(width < window.innerWidth) {
                wrapper.style.left = Math.abs(window.innerWidth-width) * scale + 'px';
            } else {
                wrapper.style.left = 0;
            }
            if(height < window.innerHeight) {
                wrapper.style.top = Math.abs(window.innerHeight-height) * scale + 'px';
            } else {
                wrapper.style.top = 0;
            }
        };

        this.setScale = function(value) {
            scale = value;
            wrapper.style.webkitTransform = 'scale(' + scale + ',' + scale + ')';
            this.onResize();
        };

        this.init = function(width, height) {
            var canvas = document.createElement('canvas');
            canvas.width = width;
            canvas.height = height;
            wrapper.appendChild(canvas);

            var director = new WOZLLA.Director(canvas);

            var oMask = new WOZLLA.GameObject();
            oMask.addComponent(new WOZLLA.component.MaskCollider());
            oMask.loadAssets(function() {
                oMask.init();
            });
            oMask.z = 999999999;
            director.stage.addChild(oMask, true);

            director.renderer.layerManager.define("__internal_coordsGameObject", 999999);
            coordGameObject = new WOZLLA.GameObject();
            coordGameObject.name = Date.now();
            var coordRenderer = new WOZLLA.component.SpriteRenderer();
            coordRenderer.imageSrc = '__internal_coords_image.png';
            coordRenderer.renderLayer = '__internal_coordsGameObject';
            coordGameObject.addComponent(coordRenderer);
            coordGameObject.loadAssets(function() {
                coordGameObject.init();
                coordGameObject.transform.setPosition(-6, -6);
                coordGameObject.transform.setScale(1, 1);
            });

            WOZLLA.GameObject.prototype.selected = false;
            WOZLLA.GameObject.prototype.visit = function(renderer, parentTransform, flags) {
                flags = super_visit.apply(this, arguments);
                if(this.selected) {
                    coordGameObject.transform.setScale(1, 1);
                    coordGameObject.transform.dirty = true;
                    coordGameObject.visit(renderer, this.transform, flags);
                }
            };

            this.director = director;
        };

        this.destroy = function() {
            console.log("destroy", coordGameObject.name);
            coordGameObject.destroy();
            this.director.view.parentNode.removeChild(this.director.view);
            this.director.stage.destroy();
            WOZLLA.GameObject.prototype.visit = super_visit;
        };
    }

    window.addEventListener('resize', function() {
        editorInstance && editorInstance.onResize();
    });

    window.onload = function() {

        editorInstance = new EditorInstance();
        editorInstance.init(960, 640);

        var dirty = true;

        var builder;
        var rootGameObject;
        var selectedGameObject;

        requestAnimationFrame(function update() {
            requestAnimationFrame(update);
            if(dirty) {
                dirty = false;
                editorInstance.director.runStep(1);
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
                    editorInstance.director.stage.addChild(root, true);
                    rootGameObject = root;
                    if(selectedGameObject) {
                        selectedGameObject = builder.getByUUID(selectedGameObject._uuid);
                        selectedGameObject.selected = true;
                    }
                    dirty = true;
                    callback && callback();
                });
            }, time == void 0 ? 500 : time);
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
            if(name === 'anchorMode') {
                gameObject.transform.anchorMode = WOZLLA.RectTransform.getMode(newValue);
                console.log(newValue + " " + WOZLLA.RectTransform.getMode(newValue))
            } else {
                gameObject.transform[name] = name === 'relative' ? newValue === 'true' : parseFloat(newValue);
            }
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

        bridge.onResize = function(width, height) {
            console.log("resize", width, height);
            editorInstance && editorInstance.destroy();
            editorInstance = new EditorInstance();
            editorInstance.init(width, height);
            editorInstance.onResize();
            reload2Stage(0);
        };

        bridge.onZoomChange = function(value) {
            editorInstance.setScale(value/100);
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

        var SpriteAtlas__LoadImage = WOZLLA.assets.SpriteAtlas.prototype._loadImage;
        WOZLLA.assets.SpriteAtlas.prototype._loadImage = function(callback) {
            var me = this;
            if(me._imageSrc === '__internal_coords_image.png') {
                var image = new Image();
                image.src = coordsImageBase64;
                image.onload = function() {
                    callback && callback(null, image);
                };
                image.onerror = function() {
                    callback('Fail to parse base64 image: ' + me.src);
                };
            } else {
                SpriteAtlas__LoadImage.apply(me, arguments);
            }
        };
    })();

    var coordsImageBase64 = "data:image/png;base64," + "iVBORw0KGgoAAAANSUhEUgAAAKoAAACqCAMAAAAKqCSwAAAAgVBMVEUAAACtaGgAAAAAA" +
        "AAAAAClY2MAAAADAAAEAAC4bm4AAAACAAADAAACAAADAAB/MQADAAADAAAKCwkDAAABenYCVlQGAQBaIgACQkBSHwCnY2MA//b/ZgC" +
        "aXFyRi4nHbVgA1s7oXAAAtrAA7ubOUQCMVFQAmZSaPQDHjIy5ZVC2SAAdWcZyAAAAG3RSTlMA5CQxC9cExUDUGXxIaYz4s1baoPPa+" +
        "sfz+MQ1to2JAAACyklEQVR42u3cyXLbMAyAYZSkKZeLVi9xyTSSrNiJ3/8BC9mOl5l2egwwg/9iH7+BRFInws/nFkApq4I2cI021cV" +
        "VGZzlQFV+XNVRGQZUF9dpbMugzI3649ZiSakQizHtV7VHrIWL9PWr94JU7eYlpdSvirJxF+rrx8fva4lk+8M68KO+vVF+AfaPLwBSS" +
        "S4r3/Yz9LKs7lQWmxUpqnVaK6e0dnY+Ag5F1AglSXWxq33j6y6688HaKAtEqars00v3kvpSgdFBGwtUqc6vU7/p09o7AGMM3FrMvX9" +
        "+vi/OfT/V6m5MfRo7bWHun9Rf8O252O7Tvo0O/tKyyLlYApFMKMY01sGQp1rlVymltVeWOtU0BW4B+75uDHGq1eUhHYpVOpTa0qaaJ" +
        "e5Vta9xt1oa2lSr54Nq2XS1pz5VMPPxb5zWivq7+hD5zUqo/02owCGhYkIFDgkVEypwSKiYUIFDQsWEChwSKiZU4JBQMaECh4SKCRU" +
        "4JFRMqMAhoWJCBQ4JFRMqcEiomFCBQ0LFhAocEiomVOCQUDGhAoeEigkVOCRUTKjAIaFiQgUOCRUTKnBIqJhQgUNCxYQKHBIqJlTgk" +
        "FAxoQKHhIoJFTgkVEyowCGhYkIFDgkVEypwSKiYUIFDQsWEChwSKiZU4NCV+nxJI80u1OvVl42+Xn1Jsi+qCds8dbGb8nZJdKz3qXa" +
        "nXBVVPpbUpwquKYZhMwxFQ3Sod6pVvspDrrwiOtQ7FUyopzzVgepQH6igyipXpQKqPVCdR6p3QDWm1IjUSJNqnXJhpgb8Y8Fo33lNc" +
        "1Xp6KNvc27xJyq0KrIfAKE67dpNzpt2d9rS3aTmdHnM0y7n3UT4OL1dJD/lcyfCO/85q2Kb54aW6MK/Z3VZZawqyR78t0woBhxqQfz" +
        "xn1O+wqF6ugf/81h5DBVAlccj4a+px0yz3ZL97n/OuhAc+eX/B3tnjfTmc1XlAAAAAElFTkSuQmCC";
})();