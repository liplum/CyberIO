//@param c:vec4 color
vec4 invert(vec4 c){
    return vec4(1.0-c.r, 1.0-c.g, 1.0-c.b, c.a);
}

vec4 blend(vec4 bk, vec4 fg, float fgA){
    vec4 res;
    res.r = (fg.r * fgA) + (bk.r * (1.0 - fgA));
    res.g = (fg.g * fgA) + (bk.g * (1.0 - fgA));
    res.b = (fg.b * fgA) + (bk.b * (1.0 - fgA));
    res.a = bk.a;
    return res;
}
vec4 blend(vec4 bk, vec4 fg){
    vec4 res;
    res.r = (fg.r * fg.a) + (bk.r * (1.0 - fg.a));
    res.g = (fg.g * fg.a) + (bk.g * (1.0 - fg.a));
    res.b = (fg.b * fg.a) + (bk.b * (1.0 - fg.a));
    res.a = bk.a;
    return res;
}


// grabbed from https://stackoverflow.com/questions/4200224/random-noise-functions-for-glsl
// A single iteration of Bob Jenkins' One-At-A-Time hashing algorithm.
uint hash(uint x) {
    x += (x << 10u);
    x ^= (x >>  6u);
    x += (x <<  3u);
    x ^= (x >> 11u);
    x += (x << 15u);
    return x;
}

// Compound versions of the hashing algorithm I whipped together.
uint hash(uvec2 v) { return hash(v.x ^ hash(v.y)); }
uint hash(uvec3 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z)); }
uint hash(uvec4 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w)); }


// Construct a float with half-open range [0:1] using low 23 bits.
// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
float floatConstruct(uint m) {
    const uint ieeeMantissa = 0x007FFFFFu;// binary32 mantissa bitmask
    const uint ieeeOne      = 0x3F800000u;// 1.0 in IEEE binary32

    m &= ieeeMantissa;// Keep only mantissa bits (fractional part)
    m |= ieeeOne;// Add fractional part to 1.0

    float  f = uintBitsToFloat(m);// Range [1:2]
    return f - 1.0;// Range [0:1]
}

// Pseudo-random value in half-open range [0:1].
float random(float x) { return floatConstruct(hash(floatBitsToUint(x))); }
float random(vec2  v) { return floatConstruct(hash(floatBitsToUint(v))); }
float random(vec3  v) { return floatConstruct(hash(floatBitsToUint(v))); }
float random(vec4  v) { return floatConstruct(hash(floatBitsToUint(v))); }

// [block] Random function usecase
uniform float time;
out vec4 fragment;

void main()
{
    vec3  inputs = vec3(gl_FragCoord.xy, time);// Spatial and temporal inputs
    float rand   = random(inputs);// Random per-pixel value
    vec3  luma   = vec3(rand);// Expand to RGB

    fragment = vec4(luma, 1.0);
}
// [end] Random function usecase

// Mod function doesn't exist on Android.
float mod(float a, float b){
    return a - (b * floor(a/b));
}
vec2 modVec2(vec2 a, float b){
    return vec2(
    a[0] - (b * floor(a[0]/b)),
    a[1] - (b * floor(a[1]/b))
    );
}
/**
@param xy[0] x
@param xy[1] y
*/
float rand(vec2 xy){
    return fract(sin(dot(xy, vec2(12.9898, 78.233))) * 43758.5453);
}

float rand(vec2 xy, float time){
    return fract(dot(xy, vec2(12.9898, 78.233)) * time);
}

float blend(float x, float y) {
    return (x < 0.5) ? (2.0 * x * y) : (1.0 - 2.0 * (1.0 - x) * (1.0 - y));
}

vec3 blend(vec3 x, vec3 y, float opacity) {
    vec3 z = vec3(blend(x.r, y.r), blend(x.g, y.g), blend(x.b, y.b));
    return z * opacity + x * (1.0 - opacity);
}

    // Gold Noise ©2015 dcerisano@standard3d.com
    // - based on the Golden Ratio
    // - uniform normalized distribution
    // - fastest static noise generator function (also runs at low precision)
    // - use with indicated seeding method.

    // Φ = Golden Ratio
    #define PHI 1.61803398874989484820459
// you have to multiple xy by 200 - 1000
float gold_noise(vec2 xy, float seed){
    return fract(tan(distance(xy*PHI, xy)*seed)*xy.x);
}