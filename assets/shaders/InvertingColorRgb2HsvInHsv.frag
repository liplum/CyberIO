uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform float u_time;
uniform sampler2D u_texture;
uniform float u_progress;
#define R2H vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0)
#define H2R vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0)
// All components are in the range [0…1], including hue.
vec3 rgb2hsv(vec3 c){
    vec4 p = mix(vec4(c.bg, R2H.wz), vec4(c.gb, R2H.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}
// All components are in the range [0…1], including hue.
vec3 hsv2rgb(vec3 c){
    vec3 p = abs(fract(c.xxx + H2R.xyz) * 6.0 - H2R.www);
    return c.z * mix(H2R.xxx, clamp(p - H2R.xxx, 0.0, 1.0), c.y);
}
// 0 <= stepNumber <= lastStepNumber
float lerpX(float start, float end, float t) {
    return (1.0 - t) * start + t * end;
}

vec3 lerpHSV(
vec3 start, vec3 end, float progress
){
    return vec3(
    lerpX(start.r, end.r, progress),
    lerpX(start.g, end.g, progress),
    lerpX(start.b, end.b, progress)
    );
}

vec3 invert(vec3 c){
    return vec3(1.0-c.r, 1.0-c.g, 1.0-c.b);
}

void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    vec3 cHSV = rgb2hsv(c.rgb);
    vec3 invertedHSV = invert(cHSV);
    vec3 resHSV = lerpHSV(cHSV, invertedHSV, u_progress);
    gl_FragColor =vec4(hsv2rgb(resHSV), c.a);
}
