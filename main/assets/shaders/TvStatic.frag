#define HIGHP

uniform float u_time;
uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uint hash(uint x) {
    x += (x << 10u);
    x ^= (x >>  6u);
    x += (x <<  3u);
    x ^= (x >> 11u);
    x += (x << 15u);
    return x;
}

uint hash(uvec3 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z)); }

float floatConstruct(uint m) {
    const uint ieeeMantissa = 0x007FFFFFu;// binary32 mantissa bitmask
    const uint ieeeOne      = 0x3F800000u;// 1.0 in IEEE binary32

    m &= ieeeMantissa;// Keep only mantissa bits (fractional part)
    m |= ieeeOne;// Add fractional part to 1.0

    float  f = uintBitsToFloat(m);// Range [1:2]
    return f - 1.0;// Range [0:1]
}

float random(vec3  v) { return floatConstruct(hash(floatBitsToUint(v))); }

void main() {
    vec2 uv= v_texCoords.xy;
    vec4 origin = texture2D(u_texture, uv);
    if (origin.a<0.01){
        return;
    }
    float res = random(vec3(uv, u_time));
    gl_FragColor = vec4(res, res, res, 1.0);
}
