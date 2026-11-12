#!/usr/bin/env python3
"""
MovieTix WCAG 2.1 Contrast Ratio Calculator
Calculates relative luminance and contrast ratios for design system color tokens
against backgrounds as required by W3C WCAG 2.1 standards.
"""

def srgb_to_linear(val: float) -> float:
    c = val / 255.0
    if c <= 0.04045:
        return c / 12.92
    else:
        return ((c + 0.055) / 1.055) ** 2.4

def relative_luminance(hex_str: str) -> float:
    hex_str = hex_str.lstrip('#')
    if len(hex_str) == 8:
        # ARGB or RGBA, extract RGB
        hex_str = hex_str[2:] if hex_str.startswith('FF') else hex_str[:6]
    r = int(hex_str[0:2], 16)
    g = int(hex_str[2:4], 16)
    b = int(hex_str[4:6], 16)
    r_lin = srgb_to_linear(r)
    g_lin = srgb_to_linear(g)
    b_lin = srgb_to_linear(b)
    return 0.2126 * r_lin + 0.7152 * g_lin + 0.0722 * b_lin

def contrast_ratio(hex1: str, hex2: str) -> float:
    l1 = relative_luminance(hex1)
    l2 = relative_luminance(hex2)
    lighter = max(l1, l2)
    darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)

def wcag_status(ratio: float, is_large_text: bool = False, is_ui_component: bool = False) -> str:
    if is_ui_component:
        return "PASS (UI Component >= 3.0:1)" if ratio >= 3.0 else "FAIL (< 3.0:1)"
    if is_large_text:
        return "PASS (WCAG AA Large >= 3.0:1)" if ratio >= 3.0 else "FAIL (< 3.0:1)"
    if ratio >= 7.0:
        return "PASS (WCAG AAA >= 7.0:1)"
    elif ratio >= 4.5:
        return "PASS (WCAG AA >= 4.5:1)"
    else:
        return "FAIL (< 4.5:1)"

# Dark Theme Tokens
DARK_TOKENS = {
    "CinemaBackground": "#0B0E14",
    "CinemaSurfaceSubtle": "#131822",
    "CinemaSurfaceCard": "#1A202E",
    "CinemaSurfaceBorder": "#2A3448",
    "CinemaPrimary": "#E5A93C",
    "CinemaPrimaryContainer": "#362103",
    "CinemaSecondary": "#F08C4A",
    "CinemaOnPrimary": "#120C02",
    "CinemaTextPrimary": "#F2F5FA",
    "CinemaTextSecondary": "#9AA7BC",
    "CinemaTextTertiary": "#63738A",
    "CinemaTextHint": "#7E8EAA",
    "CinemaSeatAvailable": "#222B3D",
    "CinemaSeatBorder": "#506488",
    "CinemaSeatSelected": "#E5A93C",
    "CinemaSeatOccupied": "#161B26",
    "StateSuccess": "#2EC492",
    "StateSuccessContainer": "#1E3A2B",
    "StateErrorDark": "#FF6B6B",
    "StateErrorContainerDark": "#381E1E"
}

# Light Theme Tokens
LIGHT_TOKENS = {
    "LightBackground": "#F8F9FC",
    "LightSurface": "#FFFFFF",
    "LightSurfaceSubtle": "#EDF1F7",
    "LightPrimary": "#825000",
    "LightOnPrimary": "#FFFFFF",
    "LightSecondary": "#A34400",
    "LightTextPrimary": "#10141D",
    "LightTextSecondary": "#526075",
    "LightTextHint": "#5D6D82",
    "LightBorder": "#D6DEEB",
    "LightSeatBorder": "#B0BDD0",
    "StateErrorLight": "#C62828"
}

PAIRS_TO_AUDIT = [
    # Dark Theme Pairs
    ("Dark", "CinemaTextPrimary", DARK_TOKENS["CinemaTextPrimary"], "CinemaBackground", DARK_TOKENS["CinemaBackground"], False, False, "Headings / Body on canvas"),
    ("Dark", "CinemaTextPrimary", DARK_TOKENS["CinemaTextPrimary"], "CinemaSurfaceCard", DARK_TOKENS["CinemaSurfaceCard"], False, False, "Headings / Body on cards"),
    ("Dark", "CinemaTextSecondary", DARK_TOKENS["CinemaTextSecondary"], "CinemaBackground", DARK_TOKENS["CinemaBackground"], False, False, "Secondary metadata on canvas"),
    ("Dark", "CinemaTextSecondary", DARK_TOKENS["CinemaTextSecondary"], "CinemaSurfaceCard", DARK_TOKENS["CinemaSurfaceCard"], False, False, "Secondary metadata on cards"),
    ("Dark", "CinemaTextHint", DARK_TOKENS["CinemaTextHint"], "CinemaSurfaceCard", DARK_TOKENS["CinemaSurfaceCard"], False, False, "Hint / placeholder on cards"),
    ("Dark", "CinemaTextTertiary", DARK_TOKENS["CinemaTextTertiary"], "CinemaSurfaceCard", DARK_TOKENS["CinemaSurfaceCard"], False, True, "Inactive tags / decorative text"),
    ("Dark", "CinemaOnPrimary", DARK_TOKENS["CinemaOnPrimary"], "CinemaPrimary", DARK_TOKENS["CinemaPrimary"], False, False, "Text/Icon on Primary CTA button"),
    ("Dark", "CinemaPrimary", DARK_TOKENS["CinemaPrimary"], "CinemaBackground", DARK_TOKENS["CinemaBackground"], True, False, "Primary Gold titles / large numbers"),
    ("Dark", "CinemaPrimary", DARK_TOKENS["CinemaPrimary"], "CinemaPrimaryContainer", DARK_TOKENS["CinemaPrimaryContainer"], False, False, "Gold icon inside Deep Amber container"),
    ("Dark", "CinemaTextPrimary", DARK_TOKENS["CinemaTextPrimary"], "CinemaPrimaryContainer", DARK_TOKENS["CinemaPrimaryContainer"], False, False, "Text inside Deep Amber container"),
    ("Dark", "StateSuccess", DARK_TOKENS["StateSuccess"], "CinemaBackground", DARK_TOKENS["CinemaBackground"], False, False, "Success state label on canvas"),
    ("Dark", "StateErrorDark", DARK_TOKENS["StateErrorDark"], "CinemaBackground", DARK_TOKENS["CinemaBackground"], False, False, "Error message on canvas"),
    ("Dark", "StateErrorDark", DARK_TOKENS["StateErrorDark"], "CinemaSurfaceCard", DARK_TOKENS["CinemaSurfaceCard"], False, False, "Error message on cards"),
    ("Dark", "CinemaSeatBorder", DARK_TOKENS["CinemaSeatBorder"], "CinemaBackground", DARK_TOKENS["CinemaBackground"], False, True, "Available seat outline contrast"),

    # Light Theme Pairs
    ("Light", "LightTextPrimary", LIGHT_TOKENS["LightTextPrimary"], "LightBackground", LIGHT_TOKENS["LightBackground"], False, False, "Headings / Body on light canvas"),
    ("Light", "LightTextPrimary", LIGHT_TOKENS["LightTextPrimary"], "LightSurface", LIGHT_TOKENS["LightSurface"], False, False, "Headings / Body on white card"),
    ("Light", "LightTextSecondary", LIGHT_TOKENS["LightTextSecondary"], "LightSurface", LIGHT_TOKENS["LightSurface"], False, False, "Secondary text on white card"),
    ("Light", "LightTextHint", LIGHT_TOKENS["LightTextHint"], "LightSurface", LIGHT_TOKENS["LightSurface"], False, False, "Placeholder on light card"),
    ("Light", "LightOnPrimary", LIGHT_TOKENS["LightOnPrimary"], "LightPrimary", LIGHT_TOKENS["LightPrimary"], False, False, "White text on Light Primary button"),
    ("Light", "LightPrimary", LIGHT_TOKENS["LightPrimary"], "LightBackground", LIGHT_TOKENS["LightBackground"], False, False, "Primary ochre accents on light canvas"),
    ("Light", "LightSecondary", LIGHT_TOKENS["LightSecondary"], "LightSurface", LIGHT_TOKENS["LightSurface"], False, False, "Secondary terracotta on white card"),
    ("Light", "StateErrorLight", LIGHT_TOKENS["StateErrorLight"], "LightSurface", LIGHT_TOKENS["LightSurface"], False, False, "Error crimson on white card"),
]

def main():
    print(f"| Theme | Foreground Token | Hex | Background Token | Hex | Contrast Ratio | WCAG 2.1 AA | Purpose |")
    print(f"| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |")
    for theme, fg_name, fg_hex, bg_name, bg_hex, is_large, is_ui, purpose in PAIRS_TO_AUDIT:
        ratio = contrast_ratio(fg_hex, bg_hex)
        status = wcag_status(ratio, is_large_text=is_large, is_ui_component=is_ui)
        print(f"| {theme} | `{fg_name}` | `{fg_hex}` | `{bg_name}` | `{bg_hex}` | **{ratio:.2f}:1** | {status} | {purpose} |")

if __name__ == "__main__":
    main()
