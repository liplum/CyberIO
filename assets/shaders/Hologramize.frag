// Only used on a full texture region
#define Density 1.3
#define OpacityScanline .3
#define Holo vec3(0.2588, 0.6471, 0.9608)

uniform float u_time;
uniform sampler2D u_texture;
uniform vec2 u_uv;
uniform vec2 u_uv2;
// The width precent relative to whole patch of scan line
uniform float u_scanline_width;// [0f,1f]
uniform float u_progress;
// 1 means true, the scan line moves from top to bottom.
// 0 means false, the scan line moves from bottom to top.
// bottom to top as default
uniform int u_topDown;
uniform vec2 u_size;
varying lowp vec4 v_color;// Draw.color()
varying vec2 v_texCoords;
uniform float u_opacityNoise;//0.2f as default
uniform float u_flickering;//0.03f as default
uniform float u_blendHoloColorOpacity;//0.8f as default
uniform float u_blendFormerColorOpacity;//0.6f as default
uniform vec4 u_holo_color;

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
void main() {
    // This shader must know the whole texture
    // Get the coordinate on atlas
    vec2 tex_uv = v_texCoords.xy;
    vec2 altasXY = v_texCoords.xy - u_uv;
    vec2 coords = altasXY / (u_uv2 - u_uv);// belongs to [0f,1f]
    // scanline progress
    float p = u_progress * (1.0 + u_scanline_width);
    bool isTopDown = u_topDown != 0;// true: topDown; false: bottomUp
    if (isTopDown){
        p = 1.0 - p;
    }
    float top = clamp(p, 0.0, 1.0);
    float bottom = clamp(p - u_scanline_width, 0.0, 1.0);
    // height progress
    float height = 1.0 - coords.y;
    // The color on original texture by patch coordinate
    vec4 res = texture2D(u_texture, tex_uv);
    if (height >= top || height >= bottom){ // upon scanline
        // Scanline is invisible
        //res.a = 0.0;
        vec3 col = res.rgb;
        if (u_blendFormerColorOpacity > 0.01){
            col = blend(col, v_color.rgb, u_blendFormerColorOpacity);// Opacity * 0.5 looks like golden
        }
        if (u_blendHoloColorOpacity > 0.01){
            col = blend(col, u_holo_color.rgb, u_blendHoloColorOpacity);
        }
        float count = Density;
        vec2 sl = vec2(sin(tex_uv.y * count), cos(tex_uv.y * count));
        vec3 scanlines = vec3(sl.x, sl.y, sl.x);

        col += col * scanlines * OpacityScanline;
        col += col * vec3(random(tex_uv * u_time)) * u_opacityNoise;
        col += col * sin(110.0 * u_time) * u_flickering;
        if (height>= top){
            res.rgb = col;
        } else if (height >= bottom){
            // Set color of scanline
            float width = top - bottom;
            float rest = height - bottom;
            float alpha = 0.8 * rest / width;
            if (isTopDown){
                alpha = 1.0 - alpha;
                res.rgb = blend(res.rgb, col.rgb, alpha);
            } else {
                res.rgb = blend(col.rgb, res.rgb, alpha);
            }
        }
    }

    gl_FragColor = res;
}
