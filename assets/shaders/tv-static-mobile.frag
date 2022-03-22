#define PHI 1.61803398874989484820459
uniform float u_time;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

float random(vec2 xy, float seed){
    return fract(tan(distance(xy*PHI, xy)*seed)*xy.x);
}
void main() {
    vec2 uv= v_texCoords.xy;
    vec4 origin = texture2D(u_texture, uv);
    if (origin.a<0.01){
        return;
    }
    float res = random(uv * 1000.0, u_time);
    gl_FragColor = vec4(res, res, res, 1.0);
}
