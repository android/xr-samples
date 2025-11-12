/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appbuildchat.instaxr.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.platform.LocalSpatialConfiguration
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.MovePolicy
import androidx.xr.compose.subspace.ResizePolicy
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.compose.subspace.layout.fillMaxSize
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.size
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Quaternion
import com.appbuildchat.instaxr.R
import com.appbuildchat.instaxr.ui.components.BugdroidControls
import com.appbuildchat.instaxr.ui.components.BugdroidModel
import com.appbuildchat.instaxr.ui.components.BugdroidSliderControls
import com.appbuildchat.instaxr.ui.components.EnvironmentControls
import com.appbuildchat.instaxr.ui.components.SearchBar
import com.appbuildchat.instaxr.ui.components.TextPane
import com.appbuildchat.instaxr.ui.theme.HelloAndroidXRTheme
import com.appbuildchat.instaxr.viewmodel.BugdroidUiState
import com.appbuildchat.instaxr.viewmodel.BugdroidViewModel
import com.appbuildchat.instaxr.viewmodel.ModelMaterialColor
import com.appbuildchat.instaxr.viewmodel.ModelMaterialProperties
import com.appbuildchat.instaxr.viewmodel.ModelOffset
import com.appbuildchat.instaxr.viewmodel.ModelRotation
import com.appbuildchat.instaxr.viewmodel.SliderGroup
import kotlinx.coroutines.launch

@Composable
fun HelloAndroidXRApp() {
    val viewModel = BugdroidViewModel()
    val uiState by viewModel.uiState.collectAsState()
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        SpatialLayout(
            primaryContent = {
                PrimaryContent(
                    uiState = uiState,
                    onShowBugdroidToggle = viewModel::updateShowBugdroid,
                    onAnimateBugdroidToggle = viewModel::updateAnimateBugdroid
                )
            },
            firstSupportingContent = {
                BlockOfContentOne(
                    showBugdroid = uiState.showBugdroid,
                    onSliderGroupSelected = viewModel::updateShownSliderGroup,
                    onResetModel = viewModel::resetModel
                )
            },
            secondSupportingContent = {
                BlockOfContentTwo(
                    uiState = uiState,
                    showBugdroid = uiState.showBugdroid,
                    onScaleChange = viewModel::updateScale,
                    onRotationChange = viewModel::updateRotation,
                    onOffsetChange = viewModel::updateOffset,
                    onMaterialColorChange = viewModel::updateMaterialColor,
                    onMaterialPropertiesChange = viewModel::updateMaterialProperties
                )
            }
        )
    } else {
        NonSpatialTwoPaneLayout(
            secondaryPane = {
                BlockOfContentOne(
                    modifier = Modifier.height(240.dp),
                    showBugdroid = uiState.showBugdroid,
                    onSliderGroupSelected = viewModel::updateShownSliderGroup,
                    onResetModel = viewModel::resetModel
                )
                BlockOfContentTwo(
                    uiState = uiState,
                    showBugdroid = uiState.showBugdroid,
                    onScaleChange = viewModel::updateScale,
                    onRotationChange = viewModel::updateRotation,
                    onOffsetChange = viewModel::updateOffset,
                    onMaterialColorChange = viewModel::updateMaterialColor,
                    onMaterialPropertiesChange = viewModel::updateMaterialProperties
                )
            },
            primaryPane = {
                PrimaryContent(
                    uiState = uiState,
                    onShowBugdroidToggle = viewModel::updateShowBugdroid,
                    onAnimateBugdroidToggle = viewModel::updateAnimateBugdroid
                )
            }
        )
    }
}

/**
 * Layout that displays content in [SpatialPanel]s, should be used when spatial UI is enabled.
 */
@Composable
private fun SpatialLayout(
    primaryContent: @Composable () -> Unit,
    firstSupportingContent: @Composable () -> Unit,
    secondSupportingContent: @Composable () -> Unit
) {
    val animatedAlpha = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        launch {
            animatedAlpha.animateTo(
                1.0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
    }
    Subspace {
        SpatialRow(modifier = SubspaceModifier.height(816.dp).fillMaxWidth()) {
            SpatialColumn(modifier = SubspaceModifier.width(400.dp)) {
                SpatialPanel(
                    SubspaceModifier
                        .alpha(animatedAlpha.value)
                        .size(400.dp)
                        .padding(bottom = 16.dp),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    firstSupportingContent()
                }
                SpatialPanel(
                    SubspaceModifier
                        .alpha(animatedAlpha.value)
                        .weight(1f),
                    dragPolicy = MovePolicy(isEnabled = true),
                    resizePolicy = ResizePolicy(isEnabled = true)
                ) {
                    secondSupportingContent()
                }
            }
            SpatialPanel(
                modifier = SubspaceModifier
                    .alpha(animatedAlpha.value)
                    .fillMaxSize()
                    .padding(left = 16.dp),
                dragPolicy = MovePolicy(isEnabled = true),
                resizePolicy = ResizePolicy(isEnabled = true)
            ) {
                Column {
                    TopAppBar()
                    primaryContent()
                }
            }
        }
    }
}

/**
 * Layout that displays content in a 2-pane layout, should be used when spatial UI is not enabled.
 */
@Composable
private fun NonSpatialTwoPaneLayout(
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val animatedAlpha = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        launch {
            animatedAlpha.animateTo(
                1.0f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        }
    }
    Column(
        modifier = modifier
            .alpha(animatedAlpha.value)
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        TopAppBar()
        Spacer(Modifier.height(16.dp))
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            TopAndBottomPaneLayout(primaryPane, secondaryPane)
        } else {
            SideBySidePaneLayout(primaryPane, secondaryPane)
        }
    }
}

/**
 * Positions the panes in a horizontal orientation
 */
@Composable
private fun SideBySidePaneLayout(
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Surface(
            Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column {
                secondaryPane()
            }
        }
        Spacer(Modifier.width(16.dp))
        Surface(modifier.clip(RoundedCornerShape(16.dp))) {
            primaryPane()
        }
    }
}

/**
 * Positions the panes in a scrollable vertical orientation
 */
@Composable
private fun TopAndBottomPaneLayout(
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        Surface(Modifier.requiredHeight(500.dp)) {
            primaryPane()
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            Modifier
                .requiredHeight(500.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column {
                secondaryPane()
            }
        }
    }
}

/**
 * Contains controls that decompose into Orbiters when spatial UI is enabled
 */
@Composable
private fun TopAppBar() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        Spacer(Modifier.weight(1f))
        Orbiter(
            position = ContentEdge.Top,
            offset = dimensionResource(R.dimen.top_ornament_padding),
            alignment = Alignment.Start
        ) {
            SearchBar()
        }
        Spacer(Modifier.weight(1f))
        Orbiter(
            position = ContentEdge.Top,
            offset = dimensionResource(R.dimen.top_ornament_padding),
            alignment = Alignment.End
        ) {
            EnvironmentControls()
        }
    }
}

@Composable
private fun PrimaryContent(
    uiState: BugdroidUiState,
    onShowBugdroidToggle: () -> Unit,
    onAnimateBugdroidToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        val showStringResId =
            if (uiState.showBugdroid) R.string.hide_bugdroid else R.string.show_bugdroid
        val animateStringResId =
            if (uiState.animateBugdroid) R.string.stop_animation_bugdroid else R.string.animate_bugdroid
        val modelTransform = uiState.modelTransform
        Surface(modifier.fillMaxSize()) {
            Column(modifier.padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier.padding(48.dp), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = onShowBugdroidToggle,
                        modifier = modifier
                    ) {
                        Text(
                            text = stringResource(id = showStringResId),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Box(modifier.padding(48.dp), contentAlignment = Alignment.Center) {
                    if (uiState.showBugdroid) {
                        Button(
                            onClick = onAnimateBugdroidToggle,
                            modifier = modifier
                        ) {
                            Text(
                                text = stringResource(id = animateStringResId),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                BugdroidModel(
                    modelTransform = modelTransform,
                    showBugdroid = uiState.showBugdroid,
                    animateBugdroid = uiState.animateBugdroid,
                    modifier = SubspaceModifier
                        .fillMaxSize()
                        .rotate(
                            Quaternion(
                                x = modelTransform.rotation.x,
                                y = modelTransform.rotation.y,
                                z = modelTransform.rotation.z,
                                w = modelTransform.rotation.w
                            )
                        )
                        .offset(
                            x = modelTransform.offset.x.dp,
                            y = modelTransform.offset.y.dp,
                            z = modelTransform.offset.z.dp // Relative position from the panel
                        )
                )
            }
        }
    } else {
        TextPane(
            text = stringResource(R.string.primary_content),
            modifier = modifier.clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun BlockOfContentOne(
    modifier: Modifier = Modifier,
    showBugdroid: Boolean,
    onSliderGroupSelected: (SliderGroup) -> Unit,
    onResetModel: () -> Unit
) {
    if (LocalSpatialConfiguration.current.hasXrSpatialFeature && showBugdroid) {
        BugdroidControls(
            onSliderGroupSelected = onSliderGroupSelected,
            onResetModel = {
                onResetModel()
                onSliderGroupSelected(SliderGroup.NONE)
            },
            modifier = modifier
        )
    } else {
        TextPane(stringResource(R.string.block_of_content_1), modifier = modifier.fillMaxHeight())
    }
}

@Composable
private fun BlockOfContentTwo(
    modifier: Modifier = Modifier,
    uiState: BugdroidUiState,
    showBugdroid: Boolean,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (ModelRotation) -> Unit,
    onOffsetChange: (ModelOffset) -> Unit,
    onMaterialColorChange: (ModelMaterialColor) -> Unit,
    onMaterialPropertiesChange: (ModelMaterialProperties) -> Unit,
) {
    if (LocalSpatialConfiguration.current.hasXrSpatialFeature && showBugdroid) {
        BugdroidSliderControls(
            visibleSliderGroup = uiState.visibleSliderGroup,
            modelTransform = uiState.modelTransform,
            onScaleChange = onScaleChange,
            onRotationChange = onRotationChange,
            onOffsetChange = onOffsetChange,
            onMaterialColorChange = onMaterialColorChange,
            onMaterialPropertiesChange = onMaterialPropertiesChange,
            modifier = modifier
        )
    } else {
        TextPane(stringResource(R.string.block_of_content_2), modifier = modifier.fillMaxHeight())
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun AppLayoutPreview() {
    HelloAndroidXRTheme {
        HelloAndroidXRApp()
    }
}