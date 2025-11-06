#version 330 core

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aTex;

out vec2 vTex;

uniform ivec2 uScreenSize;

void main()
{
    // 坐标范围为 -1 ~ 1, (0,0)为屏幕中心, 正方向为右和上
    float x = aPos.x / float(uScreenSize.x) * 2.0 - 1.0;
    float y = 1.0 - aPos.y / float(uScreenSize.y) * 2.0;
    gl_Position = vec4(x, y, 0.0, 1.0);
    vTex = aTex;
}