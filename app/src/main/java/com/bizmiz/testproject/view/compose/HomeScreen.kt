package com.bizmiz.testproject.view.compose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import com.bizmiz.testproject.R
import com.bizmiz.testproject.data.db.LocationModel
import com.bizmiz.testproject.intent.MainIntent
import com.bizmiz.testproject.model.service.LocationService
import com.bizmiz.testproject.state.MainState
import com.bizmiz.testproject.util.Constants.CAMERA_ZOOM
import com.bizmiz.testproject.view.compose.components.CircularProgressBar
import com.bizmiz.testproject.view.compose.components.StyledText
import com.bizmiz.testproject.view.theme.BottomSheetArrowDarkColor
import com.bizmiz.testproject.view.theme.BottomSheetArrowLightColor
import com.bizmiz.testproject.view.theme.BottomSheetInsideContainerDarkColor
import com.bizmiz.testproject.view.theme.BottomSheetInsideContainerLightColor
import com.bizmiz.testproject.view.theme.BottomSheetLineDarkColor
import com.bizmiz.testproject.view.theme.BottomSheetLineLightColor
import com.bizmiz.testproject.view.theme.BottomSheetTextAndIconDarkColor
import com.bizmiz.testproject.view.theme.BottomSheetTextAndIconLightColor
import com.bizmiz.testproject.view.theme.DefaultBackgroundDarkColor
import com.bizmiz.testproject.view.theme.DefaultBackgroundLightColor
import com.bizmiz.testproject.view.theme.DefaultLineIconDarkColor
import com.bizmiz.testproject.view.theme.DefaultLineIconLightColor
import com.bizmiz.testproject.view.theme.DefaultLocationIconDarkColor
import com.bizmiz.testproject.view.theme.DefaultLocationIconLightColor
import com.bizmiz.testproject.view.theme.FirstStaticContainerColor
import com.bizmiz.testproject.view.theme.SecondStaticContainerColor
import com.bizmiz.testproject.view.theme.StaticTextColor
import com.bizmiz.testproject.viewmodel.HomeViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.attribution.generated.AttributionSettings
import com.mapbox.maps.plugin.compass.generated.CompassSettings
import com.mapbox.maps.plugin.gestures.generated.GesturesSettings
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

object HomeScreen : Screen {
    private fun readResolve(): Any = HomeScreen


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val viewModel = koinViewModel<HomeViewModel>()
        val isDark = isSystemInDarkTheme()
        var selectedButton by remember { mutableStateOf(true) }

        var componentWidth by remember { mutableStateOf(0.dp) }

        val density = LocalDensity.current

        val offset by animateDpAsState(
            targetValue = if (selectedButton) componentWidth / 2 else 0.dp,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            label = ""
        )
        val busyTextColor by animateColorAsState(
            targetValue = if (isDark) Color.White else if (selectedButton) Color.Black else Color.White,
            label = "Busy text animation"
        )
        val activeTextColor by animateColorAsState(
            targetValue = if (isDark) if (selectedButton) Color.Black else Color.White else Color.Black,
            label = "Active text animation"
        )
        var bottomSheetExpand by remember { mutableStateOf(false) }
        var currentLocation by remember { mutableStateOf<LocationModel?>(null) }
        var mapZoom by remember { mutableDoubleStateOf(CAMERA_ZOOM) }
        var pointCenter by remember { mutableStateOf(true) }
        var progressShow by remember { mutableStateOf(false) }
        val locationPermissionState = rememberMultiplePermissionsState(
            listOfNotNull(
                Manifest.permission.ACCESS_FINE_LOCATION,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    null
                }
            )
        )
        LaunchedEffect(locationPermissionState.allPermissionsGranted) {
            if (locationPermissionState.allPermissionsGranted) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, LocationService::class.java)
                )
            } else {
                locationPermissionState.launchMultiplePermissionRequest()
            }
        }
        LaunchedEffect(Unit) {
            viewModel.locationIntent.send(MainIntent.FetchLocation)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(state) {
            when (state) {
                MainState.Idle -> {
                    progressShow = true
                }

                is MainState.GetLocation -> {
                    val data = (state as MainState.GetLocation).location
                    currentLocation = data
                    progressShow = false
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()

        ) {
            DisplayMapWithAnimatedPointer(
                context = context,
                isDark = isDark,
                currentLocation = currentLocation,
                zoom = mapZoom,
                pointCenter = pointCenter
            )
            Row(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.onSurface)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                        .onGloballyPositioned {
                            componentWidth = with(density) {
                                it.size.width.toDp()
                            }
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.onSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = offset)
                            .width(componentWidth / 2)
                            .padding(4.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedButton) FirstStaticContainerColor else SecondStaticContainerColor)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val interactionSource = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedButton = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            StyledText(
                                modifier = Modifier,
                                text = "Band",
                                color = busyTextColor,
                                fontSize = 18.sp,
                                isBold = !selectedButton
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedButton = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            StyledText(
                                modifier = Modifier,
                                text = "Faol",
                                color = activeTextColor,
                                fontSize = 18.sp,
                                isBold = selectedButton
                            )
                        }

                    }
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            4.dp,
                            MaterialTheme.colorScheme.onSurface,
                            RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(15.dp))
                        .background(FirstStaticContainerColor)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    StyledText(
                        text = "95",
                        color = StaticTextColor,
                        fontSize = 20.sp,
                        isBold = true
                    )
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, bottom = 140.dp),
                visible = !bottomSheetExpand,
                enter = slideInHorizontally(
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    initialOffsetX = { it - 60 })
                        + fadeIn(tween(500)),
                exit = slideOutHorizontally(animationSpec = tween(500), targetOffsetX = { it - 60 })
                        + fadeOut(tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            4.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_up_icon),
                        contentDescription = "Up",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                visible = !bottomSheetExpand,
                enter = slideInHorizontally(
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    initialOffsetX = { it - 240 })
                        + fadeIn(tween(500)),
                exit = slideOutHorizontally(animationSpec = tween(500),
                    targetOffsetX = { it - 240 })
                        + fadeOut(tween(500))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.8f))
                            .clickable {
                                mapZoom += 1.0
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus),
                            contentDescription = "Plus",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            .clickable {
                                mapZoom -= 1.0
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_minus),
                            contentDescription = "Minus",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            .clickable {
                                pointCenter = !pointCenter
                                mapZoom = CAMERA_ZOOM
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_location),
                            contentDescription = "Location",
                            tint = if (isDark) DefaultLocationIconDarkColor else DefaultLocationIconLightColor
                        )
                    }
                }
            }
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
            var dragBool by remember { mutableStateOf(false) }
            val dragLineIconAnimation by animateDpAsState(
                targetValue = if (dragBool) 0.dp else 5.dp,
                animationSpec = tween(durationMillis = 800, easing = FastOutLinearInEasing),
                label = "Drag line animation",
            )
            LaunchedEffect(Unit) {
                while (isActive) {
                    dragBool = !dragBool
                    delay(800)
                }
            }
            LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
                when (bottomSheetScaffoldState.bottomSheetState.currentValue) {
                    SheetValue.Hidden -> {
                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                    }

                    SheetValue.Expanded -> {
                        bottomSheetExpand = true
                    }

                    SheetValue.PartiallyExpanded -> {
                        bottomSheetExpand = false
                    }
                }
            }

            BottomSheetScaffold(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                scaffoldState = bottomSheetScaffoldState,
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                sheetContainerColor = Color.Transparent,
                sheetContentColor = Color.Transparent,
                sheetShadowElevation = 0.dp,
                sheetDragHandle = {
                    Icon(
                        modifier = Modifier
                            .offset(y = dragLineIconAnimation)
                            .width(40.dp)
                            .height(10.dp),
                        painter = painterResource(R.drawable.ic_bottom_line),
                        contentDescription = "Line",
                        tint = if (isDark) DefaultLineIconDarkColor else DefaultLineIconLightColor
                    )
                },
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            .background(MaterialTheme.colorScheme.onSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 25.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) BottomSheetInsideContainerDarkColor else BottomSheetInsideContainerLightColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp)
                                    .height(56.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.ic_tariff_icon),
                                    contentDescription = "Tarif",
                                    colorFilter = ColorFilter.tint(if (isDark) BottomSheetTextAndIconDarkColor else BottomSheetTextAndIconLightColor)
                                )
                                StyledText(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                    text = "Tarif",
                                    color = if (isDark) DefaultBackgroundLightColor else DefaultBackgroundDarkColor,
                                    fontSize = 18.sp,
                                    isBold = true,
                                    textAlign = TextAlign.Start
                                )
                                StyledText(
                                    modifier = Modifier.padding(end = 5.dp),
                                    text = "6 / 8",
                                    color = if (isDark) BottomSheetTextAndIconDarkColor else BottomSheetTextAndIconLightColor,
                                    fontSize = 18.sp,
                                    isBold = true
                                )
                                Image(
                                    modifier = Modifier.padding(end = 5.dp),
                                    painter = painterResource(R.drawable.ic_right_arrow),
                                    contentDescription = "Right arrow",
                                    colorFilter = ColorFilter.tint(if (isDark) BottomSheetArrowDarkColor else BottomSheetArrowLightColor)
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp)
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .background(if (isDark) BottomSheetLineDarkColor else BottomSheetLineLightColor)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp)
                                    .height(56.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.ic_order_icon),
                                    contentDescription = "Buyurtmalar",
                                    colorFilter = ColorFilter.tint(if (isDark) BottomSheetTextAndIconDarkColor else BottomSheetTextAndIconLightColor)
                                )
                                StyledText(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                    text = "Buyurtmalar",
                                    color = if (isDark) DefaultBackgroundLightColor else DefaultBackgroundDarkColor,
                                    fontSize = 18.sp,
                                    isBold = true,
                                    textAlign = TextAlign.Start
                                )
                                StyledText(
                                    modifier = Modifier.padding(end = 5.dp),
                                    text = "0",
                                    color = if (isDark) BottomSheetTextAndIconDarkColor else BottomSheetTextAndIconLightColor,
                                    fontSize = 18.sp,
                                    isBold = true
                                )
                                Image(
                                    modifier = Modifier.padding(end = 5.dp),
                                    painter = painterResource(R.drawable.ic_right_arrow),
                                    contentDescription = "Right arrow",
                                    colorFilter = ColorFilter.tint(if (isDark) BottomSheetArrowDarkColor else BottomSheetArrowLightColor)
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp)
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .background(if (isDark) BottomSheetLineDarkColor else BottomSheetLineLightColor)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp)
                                    .height(56.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(R.drawable.ic_rocket_icon),
                                    contentDescription = "Bordur",
                                    colorFilter = ColorFilter.tint(if (isDark) BottomSheetTextAndIconDarkColor else BottomSheetTextAndIconLightColor)
                                )
                                StyledText(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                    text = "Bordur",
                                    color = if (isDark) DefaultBackgroundLightColor else DefaultBackgroundDarkColor,
                                    fontSize = 18.sp,
                                    isBold = true,
                                    textAlign = TextAlign.Start
                                )
                                Image(
                                    modifier = Modifier.padding(end = 5.dp),
                                    painter = painterResource(R.drawable.ic_right_arrow),
                                    contentDescription = "Right arrow",
                                    colorFilter = ColorFilter.tint(if (isDark) BottomSheetArrowDarkColor else BottomSheetArrowLightColor)
                                )
                            }
                        }
                    }
                },
                sheetPeekHeight = 125.dp,
            ) {}
            CircularProgressBar(isDisplayed = progressShow)
        }
    }

}

@OptIn(MapboxExperimental::class)
@Composable
fun DisplayMapWithAnimatedPointer(
    context: Context,
    isDark: Boolean,
    currentLocation: LocationModel?,
    zoom: Double = CAMERA_ZOOM,
    pointCenter: Boolean = true
) {
    var previousLocation by remember { mutableStateOf(currentLocation) }
    currentLocation?.let { location ->
        val mapViewportState = rememberMapViewportState {
            setCameraOptions {
                center(Point.fromLngLat(location.longitude, location.latitude))
                zoom(zoom)
            }
        }

        SetupMapViewport(
            mapViewportState = mapViewportState,
            zoom = zoom,
            location = location,
            pointCenter = pointCenter
        )

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapInitOptionsFactory = { context ->
                MapInitOptions(
                    context = context,
                    styleUri = if (isDark) Style.DARK else Style.LIGHT,
                )
            },
            gesturesSettings = defaultGestureSettings(),
            mapViewportState = mapViewportState,
            compassSettings = defaultCompassSettings(),
            scaleBarSettings = defaultScaleBarSettings(),
            attributionSettings = AttributionSettings { enabled = false }
        ) {
            AnimatedMapPointer(
                context = context,
                point = Point.fromLngLat(location.longitude, location.latitude),
                previousPoint = previousLocation?.let {
                    Point.fromLngLat(it.longitude, it.latitude)
                }
            )
            previousLocation = currentLocation
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun SetupMapViewport(
    mapViewportState: MapViewportState,
    zoom: Double,
    location: LocationModel,
    pointCenter: Boolean
) {
    LaunchedEffect(key1 = zoom, key2 = pointCenter) {
        mapViewportState.flyTo(
            cameraOptions = cameraOptions {
                center(Point.fromLngLat(location.longitude, location.latitude))
                zoom(zoom)
            },
            animationOptions = MapAnimationOptions.mapAnimationOptions { duration(1000) },
        )
    }
}

@Composable
fun defaultGestureSettings(): GesturesSettings {
    return remember {
        mutableStateOf(GesturesSettings {
            rotateEnabled = true
            pinchToZoomEnabled = true
            pitchEnabled = true
        })
    }.value
}

@Composable
fun defaultCompassSettings(): CompassSettings {
    return remember {
        mutableStateOf(CompassSettings { enabled = false })
    }.value
}

@Composable
fun defaultScaleBarSettings(): ScaleBarSettings {
    return remember {
        mutableStateOf(ScaleBarSettings { enabled = false })
    }.value
}

@OptIn(MapboxExperimental::class)
@Composable
fun AnimatedMapPointer(context: Context, point: Point, previousPoint: Point?) {
    var animatedPoint by remember { mutableStateOf(previousPoint ?: point) }
    val threshold = 0.0001
    val shouldAnimate = previousPoint == null ||
            kotlin.math.abs(point.latitude() - previousPoint.latitude()) > threshold ||
            kotlin.math.abs(point.longitude() - previousPoint.longitude()) > threshold

    val animatedLatitude = animateFloatAsState(
        targetValue = if (shouldAnimate) point.latitude().toFloat() else previousPoint?.latitude()
            ?.toFloat() ?: point.latitude().toFloat(),
        animationSpec = tween(durationMillis = 1000), label = ""
    ).value

    val animatedLongitude = animateFloatAsState(
        targetValue = if (shouldAnimate) point.longitude().toFloat() else previousPoint?.longitude()
            ?.toFloat() ?: point.longitude().toFloat(),
        animationSpec = tween(durationMillis = 1000), label = ""
    ).value

    animatedPoint = Point.fromLngLat(animatedLongitude.toDouble(), animatedLatitude.toDouble())

    val drawable = remember {
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_car_icon, null)
    }

    drawable?.let {
        val bitmap = remember {
            it.toBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        PointAnnotation(iconImageBitmap = bitmap, point = animatedPoint)
    }
}