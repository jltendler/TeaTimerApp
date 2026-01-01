package com.example.multibrewtimer;

import android.graphics.Color;

public class ThemeHelper {

    // 0: Original
    private static final int[] ORIGINAL_COLORS = {
            Color.parseColor("#4812a1"),
            Color.parseColor("#b70089"),
            Color.parseColor("#f12568"), // nice pink
            Color.parseColor("#ff754c"),
            Color.parseColor("#ffba47"),
            Color.parseColor("#039590")
    };

    // 1: Matcha (Greens/Earth)
    private static final int[] MATCHA_COLORS = {
            Color.parseColor("#3a5a40"), // Deep Forest
            Color.parseColor("#588157"), // Sage
            Color.parseColor("#a3b18a"), // Light Sage
            Color.parseColor("#6b705c"), // Olive Drab
            Color.parseColor("#344e41"), // Deep Jungle
            Color.parseColor("#283618")  // Dark Moss
    };

    // 2: Berry (Purples/Reds)
    private static final int[] BERRY_COLORS = {
            Color.parseColor("#4a0e4e"), // Deep Plum
            Color.parseColor("#811756"), // Magenta
            Color.parseColor("#c2185b"), // Dark Pink
            Color.parseColor("#e91e63"), // Pink
            Color.parseColor("#f06292"), // Light Pink
            Color.parseColor("#880e4f")  // Burgundy
    };

    // 3: Ocean (Blues)
    private static final int[] OCEAN_COLORS = {
            Color.parseColor("#0d47a1"), // Dark Blue
            Color.parseColor("#1565c0"), // Blue
            Color.parseColor("#1976d2"), // Light Blue
            Color.parseColor("#0097a7"), // Cyan
            Color.parseColor("#00bcd4"), // Teal
            Color.parseColor("#006064")  // Deep Teal
    };

    // 4: Sunset (Oranges/Yellows)
    private static final int[] SUNSET_COLORS = {
            Color.parseColor("#bf360c"), // Deep Orange
            Color.parseColor("#e64a19"), // Orange Red
            Color.parseColor("#ff5722"), // Orange
            Color.parseColor("#ff9800"), // Amber
            Color.parseColor("#ffc107"), // Yellow
            Color.parseColor("#ffeb3b")  // Lemon
    };

    // 5: Zen (Greyscale)
    private static final int[] ZEN_COLORS = {
            Color.parseColor("#000000"), // Black
            Color.parseColor("#616161"), // Grey
            Color.parseColor("#808080"), // Grey
            Color.parseColor("#9e9e9e"), // Light Grey
            Color.parseColor("#bdbdbd"), // Lighter Grey
            Color.parseColor("#e0e0e0")  // Very Light Grey
    };

    public static int getColor(int themeIndex, int timerIndex) {
        int[] selectedTheme;
        switch (themeIndex) {
            case 1: selectedTheme = MATCHA_COLORS; break;
            case 2: selectedTheme = BERRY_COLORS; break;
            case 3: selectedTheme = OCEAN_COLORS; break;
            case 4: selectedTheme = SUNSET_COLORS; break;
            case 5: selectedTheme = ZEN_COLORS; break;
            case 0:
            default: selectedTheme = ORIGINAL_COLORS; break;
        }
        
        // Safety check for index bounds
        if (timerIndex < 0 || timerIndex >= selectedTheme.length) {
            return selectedTheme[0]; // Fallback
        }
        return selectedTheme[timerIndex];
    }
}
