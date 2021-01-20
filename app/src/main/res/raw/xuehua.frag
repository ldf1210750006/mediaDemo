// Created by inigo quilez - iq/2014
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.

#extension GL_OES_EGL_image_external : require

precision mediump float;

// 纹理坐标
varying vec2 aCoord;

uniform vec3      iResolution;           // viewport resolution (in pixels)
uniform float     iTime;                 // shader playback time (in seconds)
uniform samplerExternalOES vTexture;          // input channel. XX = 2D/Cube

void main()
{
    vec2 p = aCoord/iResolution.xy;

    vec3  col = vec3( 0.0 );

    for( int i=0; i<150; i++ )
    {
        float an = 6.2831*float(i)/150.0;
        vec2  of = vec2( cos(an), sin(an) ) * (1.0+0.6*cos(7.0*an+iTime)) + vec2( 0.0, iTime );
        col = max( col, texture2D( vTexture, p + 20.0*of/iResolution.xy ).xyz );
        col = max( col, texture2D( vTexture, p +  5.0*of/iResolution.xy ).xyz );
    }

    col = pow( col, vec3(1.0,2.0,3.0) ) * pow( 4.0*p.y*(1.0-p.y), 0.25 );

    gl_FragColor = vec4( col, 1.0 );
}