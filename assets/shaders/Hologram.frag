#define HIGHP

#define Density 1.3
#define OpacityScanline .3
uniform sampler2D u_texture;
uniform float u_time;
uniform vec4 u_holo_color;
uniform float u_alpha;//1f as default
uniform float u_opacityNoise;//0.2f as default
uniform float u_flickering;//0.03f as default
uniform float u_blendHoloColorOpacity;//0.8f as default
uniform float u_blendFormerColorOpacity;//0.6f as default
varying vec2 v_texCoords;
varying lowp vec4 v_color;// Draw.color()

float random(vec2 st) {
    return fract(sin(dot(st.xy,
    vec2(12.9898, 78.233)))*
    43758.5453123);
}

float blend(float x, float y) {
    return (x < 0.5) ? (2.0 * x * y) : (1.0 - 2.0 * (1.0 - x) * (1.0 - y));
}

vec3 blend(vec3 x, vec3 y, float opacity) {
    vec3 z = vec3(blend(x.r, y.r), blend(x.g, y.g), blend(x.b, y.b));
    return z * opacity + x * (1.0 - opacity);
}

void main(){
    vec2 uv = v_texCoords.xy;
    vec4 original = texture2D(u_texture, uv);
    if (original.a < 0.01){
        gl_FragColor = vec4(0.0);
        return;
    }
    vec3 col = original.rgb;
    if (u_blendFormerColorOpacity > 0.01){
        col = blend(col, v_color.rgb, u_blendFormerColorOpacity);
    }
    if (u_blendHoloColorOpacity > 0.01){
        col = blend(col, u_holo_color.rgb, u_blendHoloColorOpacity);
    }
    float count = Density;
    vec2 sl = vec2(sin(uv.y * count), cos(uv.y * count));
    vec3 scanlines = vec3(sl.x, sl.y, sl.x);

    col += col * scanlines * OpacityScanline;
    col += col * vec3(random(uv * u_time)) * u_opacityNoise;
    col += col * sin(110.0 * u_time) * u_flickering;

    gl_FragColor = vec4(col-0.25, u_alpha);
}