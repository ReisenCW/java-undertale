#version 330 core

in vec2 vTex;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec4 uColor;

void main()
{
    vec4 texColor = texture(uTexture, vTex);
    fragColor = texColor * uColor;
    fragColor.rgb = clamp(fragColor.rgb, 0.0f, 1.0f);
}