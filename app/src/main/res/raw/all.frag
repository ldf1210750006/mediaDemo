#extension GL_OES_EGL_image_external : require

//四宫格
// 精度
precision mediump float;
// 通过uniform传递过来的纹理
uniform samplerExternalOES vTexture;
// 纹理坐标
varying vec2 aCoord;

const highp vec3 W = vec3(0.2125, 0.7154, 0.0721); // 借用GPUImage的值

// 纹理图片Size
const vec2 TexSize = vec2(400.0, 400.0);
// 马赛克Size
const vec2 mosaicSize = vec2(10.0, 10.0);


void main() {

    vec2 uv = aCoord.xy;

    if (uv.x <= 0.5) {
        uv.x = uv.x * 2.0;
    }else {
        uv.x = (uv.x - 0.5) * 2.0;
    }

    if (uv.y <= 0.5) {
        uv.y = uv.y * 2.0;
    }else {
        uv.y = (uv.y - 0.5) * 2.0;
    }


    vec2 intXY = vec2(uv.x * TexSize.x, uv.y * TexSize.y);
    vec2 XYMosaic = vec2(floor(intXY.x/mosaicSize.x) * mosaicSize.x, floor(intXY.y/mosaicSize.y) * mosaicSize.y);
    vec2 UVMosaic = vec2(XYMosaic.x/TexSize.x, XYMosaic.y/TexSize.y);
    vec4 mask = texture2D(vTexture, UVMosaic);


//    mask = texture2D(vTexture, uv);

    //灰色滤镜
//    vec4 mask = texture2D(vTexture, uv);
    //dot  返回x y的点积
    float temp = dot(mask.rgb, W);
    gl_FragColor = vec4(vec3(temp), 0.5);



//    gl_FragColor = color;

}