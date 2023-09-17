package com.crow.module_home.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.crow.base.R.color.base_grey_500_75
import com.crow.module_home.model.resp.homepage.Banner
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.absoluteValue
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Banner(
    duration: Long = 3000,
    modifier: Modifier = Modifier,
    banners: List<Banner>,
    click: (Banner) -> Unit,
) {
    val pageCount = banners.size
    val pageStart = Int.MAX_VALUE shr 1
    val pageStateBanner = rememberPagerState(initialPage = pageStart) { Int.MAX_VALUE }
    var pageIsDragging by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pageStateBanner,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) { page ->
            val index = pageMapper(pageStart, pageCount, page)
            BannerItem(
                index = index,
                pageCount = pageCount,
                pageStart = pageStart,
                pagerState = pageStateBanner,
                banner = banners[index],
                bannerClick = click
            )
        }

        BanneIndicator(
            pagerState = pageStateBanner,
            dotWidth = 12.dp,
            dotHeight = 4.dp,
            dotSpacing = 8.dp,
            pageCount = pageCount,
            pageStart = pageStart,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter)
        )
    }

    LaunchedEffect(key1 = Unit) {
        pageStateBanner.interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> pageIsDragging = true
                is PressInteraction.Release -> pageIsDragging = false
                is PressInteraction.Cancel -> pageIsDragging = false
                is DragInteraction.Start -> pageIsDragging = true
                is DragInteraction.Stop -> pageIsDragging = false
                is DragInteraction.Cancel -> pageIsDragging = false
            }
        }
    }

    if (pageIsDragging.not()) {
        LaunchedEffect(key1 = pageIsDragging) {
            try {
                while (true) {
                    delay(duration)
                    val current = pageStateBanner.currentPage
                    val nextPage = current + 1
                    if (pageIsDragging.not()) {
                        val toPage = nextPage.takeIf { nextPage < pageStateBanner.pageCount }
                            ?: (current + pageStart + 1)
                        if (toPage > current) {
                            pageStateBanner.animateScrollToPage(toPage)
                        } else {
                            pageStateBanner.scrollToPage(toPage)
                        }
                    }
                }
            } catch (e: CancellationException) {
                e.printStackTrace()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerItem(
    banner: Banner,
    bannerClick: (Banner) -> Unit,
    index: Int,
    pagerState: PagerState,
    pageCount: Int,
    pageStart: Int,
) {
    val colors = listOf(
        MaterialTheme.colorScheme.surface,
        Color.Transparent,
        Color.Transparent,
        Color.Transparent,
        MaterialTheme.colorScheme.surface
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer {
                // Calculate the absolute offset for the current page from the
                // scroll position. We use the absolute value which allows us to mirror
                // any effects for both directions
                val pageOffset = ((pageMapper(
                    pageStart,
                    pageCount,
                    pagerState.currentPage
                ) - index) + pagerState.currentPageOffsetFraction).absoluteValue

                // We animate the alpha, between 50% and 100%
                alpha = lerp(
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                )

                lerp(
                    start = 0.7f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }
            }
            .combinedClickable(
                onClick = { bannerClick(banner) },
                onLongClick = null,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(true, color = Color(ContextCompat.getColor(LocalContext.current, base_grey_500_75)))
            )
    ) {
        AsyncImage(
            model = banner.mImgUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    drawRect(brush = Brush.verticalGradient(colors))
                }
        )

        DrawOutlineText(
            text = banner.mBrief,
            textMaxLine = 3,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp, start = 16.dp, end = 16.dp),
            outlineStokeTextStyle = getOutlineStokeTextStyle().copy(
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            ),
            outlineFillTextStyle = getOutlineFillTextStyle()
                .copy(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                ),
        )
    }
}

@Composable
private fun DrawOutlineText(
    text: String,
    textMaxLine: Int = Int.MAX_VALUE,
    textMinLine: Int = 1,
    textOverFlow: TextOverflow = TextOverflow.Ellipsis,
    modifier: Modifier,
    outlineFillTextStyle: TextStyle = getOutlineFillTextStyle(),
    outlineStokeTextStyle: TextStyle = getOutlineStokeTextStyle(),
) {
    val textMeasureStoke = rememberTextMeasurer()
    Text(
        text = text,
        maxLines = textMaxLine,
        minLines = textMinLine,
        overflow = textOverFlow,
        modifier = modifier.drawBehind {
            drawText(
                textMeasurer = textMeasureStoke,
                text = text,
                overflow = textOverFlow,
                style = outlineStokeTextStyle
            )
        },
        style = outlineFillTextStyle,
    )
}

private fun getOutlineStokeTextStyle(): TextStyle {
    return TextStyle(
        fontSize = 20.sp,
        color = Color.Black,
        drawStyle = Stroke(
            width = 12f,
            miter = 5f,
            join = StrokeJoin.Round
        )
    )
}

private fun getOutlineFillTextStyle(): TextStyle {
    return TextStyle(
        fontSize = 20.sp,
        color = Color.White,
    )
}

private infix fun Int.floorMod(other: Int): Int = when (other) {
    0 -> this
    else -> this - floorDiv(other) * other
}

private fun pageMapper(startPage: Int, pageCount: Int, index: Int): Int {
    return (index - startPage) floorMod pageCount
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BanneIndicator(
    pagerState: PagerState,
    dotWidth: Dp,
    dotHeight: Dp,
    dotSpacing: Dp,
    pageCount: Int,
    pageStart: Int,
    modifier: Modifier = Modifier
) {
    val inactiveColor = Color.Gray.copy(alpha = 0.5F)
    val activeColor = Color.Black
    val dotSpacingPx = (LocalDensity.current.run { dotSpacing.roundToPx() })
    val dotWidthPx = (LocalDensity.current.run { dotWidth.roundToPx() })
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dotSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pageCount) {
                Box(
                    modifier = Modifier
                        .size(width = dotWidth, height = dotHeight)
                        .background(
                            color = inactiveColor,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Box(
            Modifier
                .offset {
                    val position = pageMapper(pageStart, pageCount, pagerState.currentPage)
                    val offset = pagerState.currentPageOffsetFraction
                    val next = pageMapper(
                        pageStart,
                        pageCount,
                        pagerState.currentPage + offset.sign.toInt()
                    )
                    val scrollPosition = ((next - position) * offset.absoluteValue + position)
                        .coerceIn(
                            0f,
                            (pageCount - 1)
                                .coerceAtLeast(0)
                                .toFloat()
                        )
                    IntOffset(
                        x = ((dotWidthPx + dotSpacingPx) * scrollPosition).toInt(),
                        y = 0
                    )
                }
                .size(width = dotWidth, height = dotHeight)
                .background(
                    color = activeColor,
                    shape = RoundedCornerShape(3.dp),
                )
        )
    }
}