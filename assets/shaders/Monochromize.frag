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

void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    vec3 cRgb = c.rgb;
    vec3 mono = monochrome(cRgb);
    gl_FragColor = vec4(lerp(cRgb, mono, u_progress), c.a);
}
