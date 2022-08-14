// Only used on a full texture region
uniform float u_time;
uniform sampler2D u_texture;
uniform vec2 u_uv;
uniform vec2 u_uv2;
uniform vec4 u_scanline_color;
// The width precent relative to whole patch of scan line
uniform float u_scanline_width;// 0.1f as default
uniform float u_progress;// [0f,1f]
// 1 means true, the scan line moves from top to bottom.
// 0 means false, the scan line moves from bottom to top.
// bottom to top as default
uniform int u_topDown;
uniform vec2 u_size;
varying vec2 v_texCoords;

void main() {
    // This shader must know the whole texture
    // Get the coordinate on atlas
    vec2 tex_uv = v_texCoords.xy;
    // Get the width and height on altas
    vec2 altasXY = tex_uv - u_uv;
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
    if (height >= top){ // upon scanline
        // Scanline is invisible
        res.a = 0.0;
    } else if (height >= bottom){ // under scanline
        float width = top - bottom;
        float rest = height - bottom;
        float alpha = 0.8 * rest / width;
        if (isTopDown){
            alpha = 1.0 - alpha;
        }
        // Set color of scanline
        res.rgb = mix(res.rgb, u_scanline_color.rgb, alpha);
    }

    gl_FragColor = res;
}
