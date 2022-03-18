#define baseColor vec4(0.3058, 0.835, 0.960, .05)
//#define baseColor vec4(66f/255f, 165f/255f, 245f/255f, 0.2)
#define linesColor vec4(0.6, 0.7647, 0.4118, 0.4)
#define linesColorIntensity 10
#define hologramTextureTiling vec2(8., 40.)


uniform float u_time;
uniform float u_alpha;
uniform float u_speed;

uniform sampler2D u_texture;
uniform sampler2D u_hologram;

varying vec2 v_texCoords;
varying lowp vec4 v_color;

vec2 tilingAndOffset(vec2 uv, vec2 tiling, vec2 offset) {
    return mod(uv * tiling + offset, 1);
}
vec4 blend(vec4 bk, vec4 fg, float fgA){
    vec4 res;
    res.r = (fg.r * fgA) + (bk.r * (1.0 - fgA));
    res.g = (fg.g * fgA) + (bk.g * (1.0 - fgA));
    res.b = (fg.b * fgA) + (bk.b * (1.0 - fgA));
    res.a = bk.a;
    return res;
}
void main() {
    vec2 uv = v_texCoords.xy;
    vec4 origin = texture2D(u_texture, uv);
    if (origin.a < 0.01){
        return;
    }

    origin = blend(origin, v_color, 0.35);

    vec2 offset = vec2(u_time * u_speed / 100.0);
    vec2 tiling = tilingAndOffset(uv, hologramTextureTiling, offset);

    vec4 noise = texture2D(u_hologram, tiling);

    float fresnel = 0.71;
    vec4 colorLines = linesColor * vec4(vec3(linesColorIntensity), 1.0);
    vec4 emission = colorLines * fresnel * noise;

    vec4 albedo = baseColor;
    float alpha = dot(noise.rgb, vec3(1.0));
    vec4 hologram;
    hologram.rgb = emission.rgb + (1.0 - emission.rgb) * albedo.rgb * albedo.a;
    hologram.a = emission.a + (1.0 - emission.a) * alpha;
    hologram.a = hologram.a + (1.0 - hologram.a) * albedo.a;

    vec4 res;
    res.rgb = origin.rgb + (1.0 - origin.rgb) * hologram.rgb;
    res.a = u_alpha;
    gl_FragColor = res;
}
