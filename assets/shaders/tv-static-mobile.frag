uniform float u_time;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

float random(vec2 xy, float time){
    return fract(dot(xy, vec2(12.9898, 78.233)) * time);
}

void main() {
    vec2 uv= v_texCoords.xy;
    vec4 origin = texture2D(u_texture, uv);
    if (origin.a<0.01){
        return;
    }
    float res = random(uv, u_time);
    gl_FragColor = vec4(res, res, res, 1.0);
}
