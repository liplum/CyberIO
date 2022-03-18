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
uint hash( uint x ) {
    x += ( x << 10u );
    x ^= ( x >>  6u );
    x += ( x <<  3u );
    x ^= ( x >> 11u );
    x += ( x << 15u );
    return x;
}

// Compound versions of the hashing algorithm I whipped together.
uint hash( uvec2 v ) { return hash( v.x ^ hash(v.y)                         ); }
uint hash( uvec3 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z)             ); }
uint hash( uvec4 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w) ); }



// Construct a float with half-open range [0:1] using low 23 bits.
// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
float floatConstruct( uint m ) {
    const uint ieeeMantissa = 0x007FFFFFu; // binary32 mantissa bitmask
    const uint ieeeOne      = 0x3F800000u; // 1.0 in IEEE binary32

    m &= ieeeMantissa;                     // Keep only mantissa bits (fractional part)
    m |= ieeeOne;                          // Add fractional part to 1.0

    float  f = uintBitsToFloat( m );       // Range [1:2]
    return f - 1.0;                        // Range [0:1]
}

// Pseudo-random value in half-open range [0:1].
float random( float x ) { return floatConstruct(hash(floatBitsToUint(x))); }
float random( vec2  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec3  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec4  v ) { return floatConstruct(hash(floatBitsToUint(v))); }

// [block] Random function usecase
uniform float time;
out vec4 fragment;

void main()
{
    vec3  inputs = vec3( gl_FragCoord.xy, time ); // Spatial and temporal inputs
    float rand   = random( inputs );              // Random per-pixel value
    vec3  luma   = vec3( rand );                  // Expand to RGB

    fragment = vec4( luma, 1.0 );
}
// [end] Random function usecase