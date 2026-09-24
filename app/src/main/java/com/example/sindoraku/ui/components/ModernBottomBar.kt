package com.example.sindoraku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.sindoraku.R
import com.example.sindoraku.ui.theme.AccentLime
import com.example.sindoraku.ui.theme.Primary
import com.example.sindoraku.ui.theme.Stone
import com.example.sindoraku.ui.theme.Surface

private val BottomBarHeight = 44.dp
private val BottomBarHorizontalPadding = 0.dp
private val BottomBarBottomPadding = 5.dp
private val BottomBarCornerRadius = 0.dp

private val DockRadius = 42.dp

private val FabSize = 68.dp
private val FabOuterSize = 78.dp
private val FabTopOffset = (-24).dp

private val SideIconSize = 30.dp
private val CenterIconSize = 32.dp

@Composable
fun ModernBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    if (currentRoute?.startsWith("word_detail") == true) return

    val navShape = BottomNavShape(
        cornerRadius = BottomBarCornerRadius,
        dockRadius = DockRadius
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = BottomBarHorizontalPadding,
                end = BottomBarHorizontalPadding
            )
            .height(BottomBarHeight + BottomBarBottomPadding)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BottomBarBottomPadding)
                .background(Surface)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(BottomBarHeight)
                .background(
                    color = Surface,
                    shape = navShape
                )
        ) {
            RowContent(
                currentRoute = currentRoute,
                navController = navController
            )
        }

        CenterCameraButton(
            icon = ImageVector.vectorResource(R.drawable.ic_solar_camera),
            selected = currentRoute == "detection",
            onClick = {
                navigateTo(
                    navController = navController,
                    currentRoute = currentRoute,
                    route = "detection"
                )
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = FabTopOffset)
                .zIndex(2f)
        )
    }
}

@Composable
private fun RowContent(
    currentRoute: String?,
    navController: NavController
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BottomBarHeight)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SideNavItem(
            icon = ImageVector.vectorResource(R.drawable.ic_solar_home),
            label = "Home",
            contentDescription = "Beranda",
            selected = currentRoute == "home",
            onClick = {
                navigateTo(
                    navController = navController,
                    currentRoute = currentRoute,
                    route = "home"
                )
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(FabOuterSize + 28.dp))

        SideNavItem(
            icon = ImageVector.vectorResource(R.drawable.ic_solar_book),
            label = "Kamus",
            contentDescription = "Kamus",
            selected = currentRoute == "word_list",
            onClick = {
                navigateTo(
                    navController = navController,
                    currentRoute = currentRoute,
                    route = "word_list"
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}

private fun navigateTo(
    navController: NavController,
    currentRoute: String?,
    route: String
) {
    if (currentRoute == route) return

    navController.navigate(route) {
        popUpTo("home") {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun SideNavItem(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColor = if (selected) Primary else Stone

    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = itemColor,
            modifier = Modifier.size(SideIconSize)
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            text = label,
            color = itemColor,
            fontSize = 8.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            lineHeight = 8.sp
        )
    }
}

@Composable
private fun CenterCameraButton(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraBackgroundBrush = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF6F5CFF),
                Primary,
                Color(0xFF3F2ED8)
            ),
            start = Offset(0f, 0f),
            end = Offset(180f, 220f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFDFFF7A),
                AccentLime,
                Color(0xFFF4FFD1)
            ),
            start = Offset(40f, 0f),
            end = Offset(180f, 220f)
        )
    }

    val cameraIconColor = if (selected) Surface else Primary

    Box(
        modifier = modifier
            .requiredSize(FabSize)
            .clip(CircleShape)
            .background(cameraBackgroundBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Terjemah",
            tint = cameraIconColor,
            modifier = Modifier.size(CenterIconSize)
        )
    }
}

private class BottomNavShape(
    private val cornerRadius: Dp,
    private val dockRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val corner = with(density) { cornerRadius.toPx() }
        val dock = with(density) { dockRadius.toPx() }

        val baseRect = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset.Zero,
                        size = size
                    ),
                    topLeft = CornerRadius(corner, corner),
                    topRight = CornerRadius(corner, corner)
                )
            )
        }

        val circle = Path().apply {
            addOval(
                Rect(
                    left = size.width / 2f - dock,
                    top = -dock,
                    right = size.width / 2f + dock,
                    bottom = dock
                )
            )
        }

        val leftBlock = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        left = 0f,
                        top = 0f,
                        right = size.width / 2f - dock + 4f,
                        bottom = size.height
                    ),
                    topLeft = CornerRadius(corner, corner)
                )
            )
        }

        val leftSmooth = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        left = 0f,
                        top = 0f,
                        right = size.width / 2f - dock + 4f,
                        bottom = size.height
                    ),
                    topLeft = CornerRadius(corner, corner),
                    topRight = CornerRadius(32f, 32f)
                )
            )
        }

        val leftCut = Path.combine(
            operation = PathOperation.Difference,
            path1 = leftBlock,
            path2 = leftSmooth
        )

        val rightBlock = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        left = size.width / 2f + dock - 4f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height
                    ),
                    topRight = CornerRadius(corner, corner)
                )
            )
        }

        val rightSmooth = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        left = size.width / 2f + dock - 4f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height
                    ),
                    topLeft = CornerRadius(32f, 32f),
                    topRight = CornerRadius(corner, corner)
                )
            )
        }

        val rightCut = Path.combine(
            operation = PathOperation.Difference,
            path1 = rightBlock,
            path2 = rightSmooth
        )

        val withCircleCut = Path.combine(
            operation = PathOperation.Difference,
            path1 = baseRect,
            path2 = circle
        )

        val withLeftCut = Path.combine(
            operation = PathOperation.Difference,
            path1 = withCircleCut,
            path2 = leftCut
        )

        val finalPath = Path.combine(
            operation = PathOperation.Difference,
            path1 = withLeftCut,
            path2 = rightCut
        )

        return Outline.Generic(finalPath)
    }
}