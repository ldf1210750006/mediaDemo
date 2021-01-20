#extension GL_OES_EGL_image_external : require


precision highp float;

uniform samplerExternalOES vTexture;
varying highp vec2 aCoord;


const highp vec3 W = vec3(0.2125, 0.7154, 0.0721); // 借用GPUImage的值

void main() {
    vec4 mask = texture2D(vTexture, aCoord);
    float temp = dot(mask.rgb, W);
    gl_FragColor = vec4(vec3(temp), 1.0);
}
