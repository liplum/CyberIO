uniform float u_time;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

float zebra(vec2 xy, float time){
    return fract(xy.x * time + xy.y);
}

void main() {
    vec2 uv= v_texCoords.xy;
    vec4 origin = texture2D(u_texture, uv);
    if (origin.a<0.01){
        return;
    }
    float res = zebra(uv, u_time);
    gl_FragColor = vec4(res, res, res, 1.0);
}
