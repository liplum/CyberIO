uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;

float nrand(in float x, in float y)
{
    return fract(sin(dot(vec2(x, y), vec2(12.9898, 78.233))) * 43758.5453);
}

void main()
{
    float cx = v_texCoords.x;
    float cy = v_texCoords.y;

    float range = nrand(0, cy) * 2.0 - 1;

    float willOffset = step(0.5, abs(range));
    float uBurrParam = 0.1f;
    float offset = range * willOffset * 0.13 * uBurrParam;

    gl_FragColor = texture2D(u_texture, fract(vec2(cx + offset, cy)));
}