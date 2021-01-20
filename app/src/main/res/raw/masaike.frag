#extension GL_OES_EGL_image_external : require

precision highp float;
// 纹理坐标
varying vec2 aCoord;
// 纹理采样器
uniform samplerExternalOES vTexture;
// 纹理图片Size
const vec2 TexSize = vec2(400.0, 400.0);
// 马赛克Size
const vec2 mosaicSize = vec2(10.0, 10.0);

void main () {
    vec2 intXY = vec2(aCoord.x * TexSize.x, aCoord.y * TexSize.y);
    vec2 XYMosaic = vec2(floor(intXY.x/mosaicSize.x) * mosaicSize.x, floor(intXY.y/mosaicSize.y) * mosaicSize.y);
    vec2 UVMosaic = vec2(XYMosaic.x/TexSize.x, XYMosaic.y/TexSize.y);
    vec4 color = texture2D(vTexture, UVMosaic);
    gl_FragColor = color;
}
