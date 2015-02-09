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
        var rectHelperLine;
        var cirlceHelperLine;
        var scale = 0.5;

        this.director = null;

        this.onResize = function() {
            if(!this.director) return;
            this.director.touch.touchScale = 1/scale;
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
            this.director.touch.updateCanvasOffset();
        };

        this.setScale = function(value) {
            scale = value;
            wrapper.style.webkitTransform = 'scale(' + scale + ',' + scale + ')';
            this.onResize();
        };

        this.setSelectedGameObject = function(obj) {
            if(coordGameObject) {
                coordGameObject.active = !!obj;
            }
        };

        this.init = function(width, height) {
            var canvas = document.createElement('canvas');
            canvas.width = width;
            canvas.height = height;
            wrapper.appendChild(canvas);

            var director = new WOZLLA.Director(canvas);
            director.touch.hammer.get('pan').set({ threshold: 0 });
            if(bridgeInvoke("isExistInProject", "Editor/render_layers.json")) {
                var layers = JSON.parse(bridgeInvoke("readProjectFile", "Editor/render_layers.json"));
                var layerManager = director.renderer.layerManager;
                layers.forEach(function(layer, idx) {
                    layerManager.define(layer, (idx+1)*100);
                    console.log("define layer " + layer + "=" + ((idx+1)*100));
                });
            }
            window.director = director;
            director.touch.inSchedule = false;
            setTimeout(function() {
                director.touch.updateCanvasOffset();
            }, 10);

            var helperLayer = "__Editor_Helper_Layer";

            var oMask = new WOZLLA.GameObject();
            oMask.name = "VisualEditor_Mask";
            oMask.touchable = true;
            oMask.addComponent(new WOZLLA.component.MaskCollider());
            oMask.loadAssets(function() {
                oMask.init();
            });
            oMask.z = 999999999;
            director.stage.addChild(oMask, true);

            director.renderer.layerManager.define(helperLayer, 999999);
            coordGameObject = new WOZLLA.GameObject();
            coordGameObject.name = 'VisualEditor_CoordsXY';
            coordGameObject.z = oMask.z + 1;
            coordGameObject.touchable = true;
            var coordRenderer = new WOZLLA.component.SpriteRenderer();
            coordRenderer.imageSrc = '__internal_coords_image.png';
            coordRenderer.renderLayer = helperLayer;
            coordGameObject.addComponent(coordRenderer);
            var origin_visit = coordGameObject.visit;
            coordGameObject.visit = function(renderer, parentTransform, flags, render) {
                if(render) {
                    origin_visit.apply(this, arguments);
                }
            };

            var coordXYCollider = new WOZLLA.component.RectCollider();
            coordXYCollider.region = new WOZLLA.math.Rectangle(-12, -12, 24, 24);
            coordGameObject.addComponent(coordXYCollider);

            var coordXObj = new WOZLLA.GameObject();
            coordXObj.name = 'VisualEditor_Coord_X';
            coordXObj.transform.x = 30;
            coordXObj.touchable = true;
            var coordXCollider = new WOZLLA.component.RectCollider();
            coordXCollider.region = new WOZLLA.math.Rectangle(0, -12, 150, 24);
            coordXObj.addComponent(coordXCollider);

            var coordYObj = new WOZLLA.GameObject();
            coordYObj.name = 'VisualEditor_Coord_Y';
            coordYObj.transform.y = 30;
            coordYObj.touchable = true;
            var coordYCollider = new WOZLLA.component.RectCollider();
            coordYCollider.region = new WOZLLA.math.Rectangle(-12, 0, 24, 150);
            coordYObj.addComponent(coordYCollider);

            coordGameObject.addChild(coordXObj, true);
            coordGameObject.addChild(coordYObj, true);

            coordGameObject.loadAssets(function() {
                coordRenderer.spriteOffset = {
                    x: 6/coordRenderer.sprite.frame.width,
                    y: 6/coordRenderer.sprite.frame.height
                };
                coordGameObject.init();
            });

            var lastXY = {
                x: 0,
                y: 0
            };
            coordGameObject.addListener('panstart', function(e) {
                lastXY.x = e.x;
                lastXY.y = e.y;
            }, false);
            coordGameObject.addListener('panmove', function(e) {
                var deltaX = Math.ceil(e.x - lastXY.x);
                var deltaY = Math.ceil(e.y - lastXY.y);
                lastXY.x = e.x;
                lastXY.y = e.y;
                if(e.target === coordXObj) {
                    bridgeInvoke('moveX', deltaX);
                } else if(e.target === coordYObj) {
                    bridgeInvoke('moveY', deltaY);
                } else {
                    bridgeInvoke('moveXY', deltaX, deltaY);
                }
            }, false);

            director.stage.addChild(coordGameObject, true);


            rectHelperLine = new WOZLLA.GameObject();
            rectHelperLine.name = 'rectHelperLine' + Date.now();
            var rectRenderer = new WOZLLA.component.RectRenderer();
            rectHelperLine.addComponent(rectRenderer);
            rectRenderer.renderLayer = helperLayer;
            rectHelperLine.loadAssets(function() {
                rectHelperLine.init();
            });

            cirlceHelperLine = new WOZLLA.GameObject();
            cirlceHelperLine.name = 'cirlceHelperLine' + Date.now();
            var circleRenderer = new WOZLLA.component.CircleRenderer();
            cirlceHelperLine.addComponent(circleRenderer);
            circleRenderer.renderLayer = helperLayer;
            cirlceHelperLine.loadAssets(function() {
                cirlceHelperLine.init();
            });

            WOZLLA.GameObject.prototype.selected = false;
            WOZLLA.GameObject.prototype.visit = function(renderer, parentTransform, flags) {
                flags = super_visit.apply(this, arguments);
                if(this.selected) {
                    var scaleX = 1/this.transform.worldMatrix.values[0];
                    var scaleY = 1/this.transform.worldMatrix.values[4];
                    coordGameObject.transform.setScale(scaleX, scaleY);
                    coordGameObject.transform.dirty = true;
                    coordGameObject.visit(renderer, this.transform, flags, true);

                    if(this.rectTransform && this.rectTransform.width > 0 && this.rectTransform.height > 0) {
                        rectHelperLine.renderer.rect = new WOZLLA.math.Rectangle(
                            0, 0, this.rectTransform.width, this.rectTransform.height);
                        rectHelperLine.renderer.primitiveStyle.strokeColor = 'red';
                        rectHelperLine.renderer.primitiveStyle.alpha = 0.6;
                        rectHelperLine.transform.dirty = true;
                        rectHelperLine.visit(renderer, this.transform, flags);

                        rectHelperLine.renderer.rect = new WOZLLA.math.Rectangle(
                            -1, -1, this.rectTransform.width+2, this.rectTransform.height+2);
                        rectHelperLine.renderer.primitiveStyle.strokeColor = 'blue';
                        rectHelperLine.renderer.primitiveStyle.alpha = 0.6;
                        rectHelperLine.transform.dirty = true;
                        rectHelperLine.visit(renderer, this.transform, flags);
                    }

                    var collider = this.collider;
                    if(collider) {
                        if (collider instanceof WOZLLA.component.RectCollider) {
                            if (collider.region) {
                                rectHelperLine.renderer.rect = collider.region;
                                rectHelperLine.renderer.primitiveStyle.strokeColor = 'yellow';
                                rectHelperLine.renderer.primitiveStyle.alpha = 0.6;
                                rectHelperLine.transform.dirty = true;
                                rectHelperLine.visit(renderer, this.transform, flags);
                            }
                        } else if (collider instanceof  WOZLLA.component.CircleCollider) {
                            if (collider.region) {
                                cirlceHelperLine.renderer.circle = collider.region;
                                cirlceHelperLine.renderer.primitiveStyle.strokeColor = 'yellow';
                                cirlceHelperLine.renderer.primitiveStyle.alpha = 0.6;
                                cirlceHelperLine.transform.dirty = true;
                                cirlceHelperLine.visit(renderer, this.transform, flags);
                            }
                        }
                    }
                }
            };

            this.director = director;
        };

        this.destroy = function() {
            coordGameObject.destroy();
            rectHelperLine.destroy();
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
            editorInstance.setSelectedGameObject(selectedGameObject);
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
                case "reference":
                    reload2Stage(0);
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

        var JSONXBuilder__newComponent = WOZLLA.jsonx.JSONXBuilder.prototype._newComponent;
        WOZLLA.jsonx.JSONXBuilder.prototype._newComponent = function(compData, gameObj) {
            var config = WOZLLA.Component.getConfig(compData.name);
            var component;
            if(config.disableInEditor) {
                component = new WOZLLA.Component();
            } else {
                component = WOZLLA.Component.create(compData.name);
            }
            component._uuid = compData.uuid;
            this.uuidMap[compData.uuid] = component;
            component.gameObject = gameObj;
            if(!config.disableInEditor) {
                this._applyComponentProperties(component, config.properties, compData);
            }
            return component;
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