#version 330 core

in vec2 vTex;
out vec4 fragColor;

uniform sampler2D uTexture;
uniform vec4 uColor;
uniform float uScale;

void main() {
  vec4 texColor = texture(uTexture, vTex);
  float whiteMix = 1.0 - uScale * uScale;
  vec3 highlightedColor = mix(texColor.rgb, vec3(1.0f), whiteMix);
  fragColor = vec4(highlightedColor, texColor.a * uColor.a);
  fragColor.rgb = clamp(fragColor.rgb, 0.0f, 1.0f);
}
