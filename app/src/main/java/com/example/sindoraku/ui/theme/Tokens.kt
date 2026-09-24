package com.example.sindoraku.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens from DESIGN.md, scaled for mobile touch targets.
 */
object AppTokens {
    object Radius {
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 10.dp
        val xl = 12.dp
        val xxl = 18.dp
        val full = 9999.dp
    }

    object Space {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 48.dp
        val section = 40.dp
        val sectionLg = 56.dp
    }

    object Component {
        val buttonHeight = 48.dp
        val iconButtonSize = 44.dp
        val topBarHeight = 56.dp
        val inputHeight = 48.dp
        val listRowMinHeight = 56.dp
        val bottomBarHeight = 64.dp
        val bottomBarCornerRadius = 28.dp
        val bottomBarCutoutRadius = 34.dp
        val bottomBarFabSize = 56.dp
        val bottomBarFloatingHorizontal = 20.dp
        val bottomBarIconSide = 26.dp
        val bottomBarIconCenter = 28.dp
        val bottomBarActiveIndicatorWidth = 20.dp
        val bottomBarActiveIndicatorHeight = 3.dp
        val heroMinHeight = 200.dp
        val actionCardHeight = 100.dp
        val actionCardCompactHeight = 112.dp
    }

    object Elevation {
        val card: Dp = 0.dp
        val elevated: Dp = 4.dp
        val heroCard: Dp = 8.dp
    }
}
