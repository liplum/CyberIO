uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;

float random(float seed)
{
    return fract(543.2543 * sin(dot(vec2(seed, seed), vec2(3525.46, -54.3415))));
}

void main()
{
    float shake_power = 0.03;
    float  shake_rate = 0.2;
    float shake_speed = 1.0;
    float shake_block_size = 1.5;
    float shake_color_rate = 0.01;
    float enable_shift = float(random(trunc(u_time * shake_speed)) < shake_rate);

    vec2 fixed_uv = v_texCoords;
    fixed_uv.x += (
    random(
    (trunc(v_texCoords.y * shake_block_size) / shake_block_size)
    +    u_time
    ) - 0.5
    ) * shake_power * enable_shift;

    vec4 pixel_color = texture2D(u_texture, fixed_uv, 0.0);
    pixel_color.r = mix(
    pixel_color.r
    , texture2D(u_texture, fixed_uv + vec2(shake_color_rate, 0.0), 0.0).r
    , enable_shift
    );
    pixel_color.b = mix(
    pixel_color.b
    , texture2D(u_texture, fixed_uv + vec2(-shake_color_rate, 0.0), 0.0).b
    , enable_shift
    );
    gl_FragColor = pixel_color;
}