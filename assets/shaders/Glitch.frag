uniform vec2 u_resolution;
uniform vec2 u_offset;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_time;
/*
float red_displacement : hint_range(-1.0,1.0) = 0.0;
float green_displacement : hint_range(-1.0,1.0) = 0.0;
float blue_displacement : hint_range(-1.0,1.0) = 0.0;
float ghost : hint_range(0.0, 1.0) = 0.0;
float intensity : hint_range(0.0,1.0) = 0.0;
float scan_effect : hint_range(0.0,1.0) = 0.0;
float distortion_effect : hint_range(0.0,1.0) = 0.0;
float negative_effect : hint_range(0.0,1.0) = 0.0;
*/
void main()
{
    float red_displacement = 0.3;
    float green_displacement = 0.3;
    float blue_displacement  = 0.3;
    float ghost= 0.1;
    float intensity  = 0.1;
    float scan_effect  = 0.1;
    float distortion_effect  = 0.1;
    float negative_effect = 0.1;

    vec4 baseTexture = texture2D(u_texture, v_texCoords);
    vec4 color1 = texture2D(u_texture, v_texCoords+vec2(sin(u_time*0.2*intensity), tan(v_texCoords.y)));
    vec4 COLOR = (1.0-scan_effect)*baseTexture*0.75 + scan_effect*color1;

    vec4 color2 = texture2D(u_texture, v_texCoords+vec2(fract(u_time*0.01*intensity), cos(fract(u_time*intensity)*10.0)));
    COLOR = COLOR + ((1.0-distortion_effect)*baseTexture*0.75 + distortion_effect*color2);

    vec4 color3 = texture2D(u_texture, v_texCoords + vec2(fract(u_time*0.1*intensity), tan(u_time*0.02*intensity)));
    COLOR = COLOR - ((1.0-negative_effect)*baseTexture*0.5 + negative_effect*color3);

    COLOR.r = (1.0-red_displacement)*baseTexture.r + red_displacement*texture2D(u_texture, v_texCoords-vec2(sin(u_time*0.1*intensity) + 0.2, 0.1)).r;
    COLOR.g = (1.0-green_displacement)*baseTexture.g +  green_displacement*texture2D(u_texture, v_texCoords+vec2(- 0.2, sin(u_time*0.1*intensity))).g;
    COLOR.b = (1.0-blue_displacement)*baseTexture.b + blue_displacement*texture2D(u_texture, v_texCoords+vec2(sin(u_time*0.1*intensity) + 0.2, 0.1)).b;
    COLOR = COLOR + texture2D(u_texture, v_texCoords + v_texCoords*ghost)*ghost;
    gl_FragColor = COLOR;
}