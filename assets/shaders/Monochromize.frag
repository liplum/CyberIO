uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform float u_time;
uniform sampler2D u_texture;

uniform float u_progress;

vec3 monochrome(vec3 color){
    float mono =  (0.2125 * color.r) + (0.7154 * color.g) + (0.0721 * color.b);
    return vec3(mono, mono, mono);
}
float lerp(float start, float end, float t) {
    return (1.0 - t) * start + t * end;
}

vec3 lerp(
vec3 start, vec3 end, float progress
){
    return vec3(
    lerp(start.r, end.r, progress),
    lerp(start.g, end.g, progress),
    lerp(start.b, end.b, progress)
    );
}

void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    vec3 cRgb = c.rgb;
    vec3 mono = monochrome(cRgb);
    vec3 res = lerp(cRgb, mono, u_progress);
    gl_FragColor = vec4(res.r, res.g, res.b, c.a);
}
