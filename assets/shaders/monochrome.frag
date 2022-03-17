uniform sampler2D u_texture;
varying vec2 v_texCoords;

vec4 monochrome(vec4 color){
    float mono =  (0.2125f * color.r) + (0.7154f * color.g) + (0.0721f * color.b);
    return vec4(mono, mono, mono, color.a);
}

void main() {
    vec4 c = texture2D(u_texture, v_texCoords.xy);
    gl_FragColor = monochrome(c);
}
