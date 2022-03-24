uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;

vec4 monochrome(vec4 color){
    float mono =  (0.2125 * color.r) + (0.7154 * color.g) + (0.0721 * color.b);
    return vec4(mono, mono, mono, color.a);
}

void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    gl_FragColor = monochrome(c);
}
