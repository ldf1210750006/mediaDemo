#extension GL_OES_EGL_image_external : require

//九宫格
// 精度
precision mediump float;
// 通过uniform传递过来的纹理
uniform samplerExternalOES vTexture;
// 纹理坐标
varying vec2 aCoord;

void main() {

    vec2 uv = aCoord.xy;

    if (uv.x <= 1.0 / 3.0) {
        uv.x = uv.x * 3.0;
    }else if (uv.x <= 2.0 / 3.0) {
        uv.x = (uv.x - 1.0 / 3.0) * 3.0;
    }else {
        uv.x = (uv.x - 2.0 / 3.0) * 3.0;
    }

    if (uv.y <= 1.0 / 3.0) {
        uv.y = uv.y * 3.0;
    }else if (uv.y <= 2.0 / 3.0) {
        uv.y = (uv.y - 1.0 / 3.0) * 3.0;
    }else {
        uv.y = (uv.y - 2.0 / 3.0) * 3.0;
    }

    gl_FragColor = texture2D(vTexture, uv);
}