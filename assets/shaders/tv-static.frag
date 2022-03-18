uniform float u_time;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

float rand(vec2 xy){
    return fract(sin(dot(xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv= v_texCoords.xy;
    vec4 origin = texture2D(u_texture, uv);
    if (origin.a<0.01){
        return;
    }
    vec2 inputs = vec2(uv.x+u_time, uv.y-u_time);
    float res = rand(uv);
    gl_FragColor = vec4(res, res, res, 1.0);
}
