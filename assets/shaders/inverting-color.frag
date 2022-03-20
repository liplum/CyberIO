uniform sampler2D u_texture;
uniform float u_progress;
varying vec2 v_texCoords;

float blend(float x, float y) {
    return (x < 0.5) ? (2.0 * x * y) : (1.0 - 2.0 * (1.0 - x) * (1.0 - y));
}

vec3 blend(vec3 x, vec3 y, float opacity) {
    vec3 z = vec3(blend(x.r, y.r), blend(x.g, y.g), blend(x.b, y.b));
    return z * opacity + x * (1.0 - opacity);
}

//@param c:vec4 color
vec4 invert(vec4 c){
    return vec4(1.0-c.r, 1.0-c.g, 1.0-c.b, c.a);
}
void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    vec4 inverted = invert(c);
    vec3 res = blend(c, inverted.rgb, u_progress);
    gl_FragColor =vec4(res, c.a);
}
