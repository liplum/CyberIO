uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform float u_time;
uniform sampler2D u_texture;
//@param c:vec4 color
vec4 invert(vec4 c){
    return vec4(1.0-c.r, 1.0-c.g, 1.0-c.b, c.a);
}
void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    gl_FragColor = invert(c);
}
