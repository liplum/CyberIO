uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform float u_time;
uniform sampler2D u_texture;

uniform float u_progress;

vec3 invert(vec3 c){
    return vec3(1.0-c.r, 1.0-c.g, 1.0-c.b);
}

void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    vec3 cRgb = c.rgb;
    vec3 inverted = invert(cRgb);
    gl_FragColor = vec4(lerp(cRgb, inverted, u_progress), c.a);
}
