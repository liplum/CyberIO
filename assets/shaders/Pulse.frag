uniform sampler2D u_texture;
varying vec2 v_texCoords;
//时间戳
uniform float u_time;
//PI常量
const float PI = 3.1415926;
//随机数
float rand(float n){
    //fract(x)返回x的小数部分
    //返回 sin(n) * 43758.564236482638
    //sin(n) * 极大值，带小数点，想要随机数算的比较低，乘的数就必须较大，噪声随机
    //如果想得到[0，1]范围的小数值，可以将sin * 1
    //如果只保留小数部分，乘以一个极大值
    return fract(sin(n) * 43758.5453123);
}

void main(){
    //最大抖动上限
    float maxJitter = 0.06;
    //一次毛刺效果的时长
    float duration = 0.3;
    //红色颜色偏移
    float colorROffset = 0.01;
    //绿色颜色偏移
    float colorBOffset = -0.025;

    //表示将传入的事件转换到一个周期内，范围是 0 ~ 0.6，抖动时长变成0.6
    float time = mod(u_time, duration * 2.0);
    //振幅，随着时间变化，范围是[0, 1]                                                                             
    float amplitude = max(sin(time * (PI / duration)), 0.0);

    //像素随机偏移范围 -1 ~ 1，* 2.0 - 1.0是为了得到[-1，1]范围内的随机值
    float jitter = rand(v_texCoords.y) * 2.0 - 1.0;
    //判断是否需要偏移，如果jitter范围 < 最大范围*振幅
    // abs(jitter) 范围【0，1】
    // maxJitter * amplitude 范围【0， 0.06】
    bool needOffset = abs(jitter) < maxJitter * amplitude;

    //获取纹理x坐标，根据needOffset来计算它的x撕裂
    //needOffset = YES，则撕裂大
    //needOffset = NO，则撕裂小，需要降低撕裂 = *振幅*非常细微的数
    float textureX = v_texCoords.x + (needOffset ? jitter : (jitter * amplitude * 0.006));
    //获取纹理撕裂后的x、y坐标
    vec2 textureCoords = vec2(textureX, v_texCoords.y);

    //颜色偏移：获取3组颜色
    //撕裂后的原图颜色
    vec4 mask = texture2D(u_texture, textureCoords);
    //根据撕裂计算后的纹理坐标，获取纹素
    vec4 maskR = texture2D(u_texture, textureCoords + vec2(colorROffset * amplitude, 0.0));
    //根据撕裂计算后的纹理坐标，获取纹素
    vec4 maskB = texture2D(u_texture, textureCoords + vec2(colorBOffset * amplitude, 0.0));

    //颜色主要撕裂，红色和蓝色部分，所以只调整红色
    gl_FragColor = vec4(maskR.r, mask.g, maskB.b, mask.a);
}