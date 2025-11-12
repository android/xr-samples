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

package com.appbuildchat.instaxr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appbuildchat.instaxr.R
import com.appbuildchat.instaxr.ui.theme.HelloAndroidXRTheme
import com.appbuildchat.instaxr.viewmodel.MAX_MATERIAL_COLOR_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_MATERIAL_PROP_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_OFFSET_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_SCALE_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_W_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_X_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_Y_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MAX_Z_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_MATERIAL_COLOR_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_MATERIAL_PROP_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_OFFSET_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_SCALE_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_W_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_X_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_Y_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.MIN_Z_ROTATION_VALUE
import com.appbuildchat.instaxr.viewmodel.ModelMaterialColor
import com.appbuildchat.instaxr.viewmodel.ModelMaterialProperties
import com.appbuildchat.instaxr.viewmodel.ModelOffset
import com.appbuildchat.instaxr.viewmodel.ModelRotation
import com.appbuildchat.instaxr.viewmodel.ModelTransform
import com.appbuildchat.instaxr.viewmodel.SliderGroup

@Composable
fun BugdroidSliderControls(
    visibleSliderGroup: SliderGroup,
    modelTransform: ModelTransform,
    modifier: Modifier = Modifier,
    onScaleChange: (Float) -> Unit,
    onRotationChange: (ModelRotation) -> Unit,
    onOffsetChange: (ModelOffset) -> Unit,
    onMaterialColorChange: (ModelMaterialColor) -> Unit,
    onMaterialPropertiesChange: (ModelMaterialProperties) -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            when (visibleSliderGroup) {
                SliderGroup.SCALE -> {
                    ScaleSlider(
                        scale = modelTransform.scale,
                        onScaleChange = onScaleChange,
                        modifier = modifier
                    )
                }

                SliderGroup.ROTATION -> {
                    RotationSliders(
                        rotation = modelTransform.rotation,
                        onRotationChange = onRotationChange,
                        modifier = modifier
                    )
                }

                SliderGroup.OFFSET -> {
                    OffsetSliders(
                        offset = modelTransform.offset,
                        onOffsetChange = onOffsetChange,
                        modifier = modifier
                    )
                }

                SliderGroup.MATERIAL_COLORS -> {
                    MaterialColorSliders(
                        materialColor = modelTransform.materialColor,
                        onMaterialColorChange = onMaterialColorChange,
                        modifier = modifier
                    )
                }

                SliderGroup.MATERIAL_PROPERTIES -> {
                    MaterialPropertySliders(
                        materialProperties = modelTransform.materialProperties,
                        onMaterialPropertiesChange = onMaterialPropertiesChange,
                        modifier = modifier
                    )
                }

                else -> {
                    Text(text = stringResource(R.string.please_select))
                }
            }
        }
    }
}

@Composable
fun ScaleSlider(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier
) {
    Text(text = stringResource(R.string.change_scale))

    Spacer(modifier.padding(25.dp))

    Text(text = stringResource(R.string.scale))
    Slider(
        value = scale,
        onValueChange = onScaleChange,
        valueRange = MIN_SCALE_VALUE..MAX_SCALE_VALUE,
        modifier = Modifier.padding()
    )
    Text(text = "%.2f".format(scale))
}

@Composable
fun RotationSliders(
    rotation: ModelRotation,
    onRotationChange: (ModelRotation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = stringResource(R.string.change_rotation))
        Spacer(modifier = Modifier.padding(10.dp))

        Text(text = stringResource(R.string.x_rotation))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Slider(
                value = rotation.x,
                onValueChange = { newX -> onRotationChange(rotation.copy(x = newX)) },
                valueRange = MIN_X_ROTATION_VALUE..MAX_X_ROTATION_VALUE,
                modifier = Modifier.weight(.7f)
            )
            Text(
                text = "%.2f".format(rotation.x)
            )
        }
        Text(text = stringResource(R.string.y_rotation))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Slider(
                value = rotation.y,
                onValueChange = { newY -> onRotationChange(rotation.copy(y = newY)) },
                valueRange = MIN_Y_ROTATION_VALUE..MAX_Y_ROTATION_VALUE,
                modifier = Modifier.weight(.7f)
            )
            Text(text = "%.2f".format(rotation.y))
        }

        Text(text = stringResource(R.string.z_rotation))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Slider(
                value = rotation.z,
                onValueChange = { newZ -> onRotationChange(rotation.copy(z = newZ)) },
                valueRange = MIN_Z_ROTATION_VALUE..MAX_Z_ROTATION_VALUE,
                modifier = Modifier.weight(.7f)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(text = "%.2f".format(rotation.z))
        }

        Text(text = stringResource(R.string.w_rotation))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Slider(
                value = rotation.w,
                onValueChange = { newW -> onRotationChange(rotation.copy(w = newW)) },
                valueRange = MIN_W_ROTATION_VALUE..MAX_W_ROTATION_VALUE,
                modifier = Modifier.weight(.7f)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(text = "%.2f".format(rotation.w))
        }
    }
}

@Composable
fun OffsetSliders(
    offset: ModelOffset,
    onOffsetChange: (ModelOffset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = stringResource(R.string.change_offset))
        Spacer(modifier = Modifier.padding(10.dp))

        Text(text = stringResource(R.string.x_offset))
        Slider(
            value = offset.x,
            onValueChange = { newX -> onOffsetChange(offset.copy(x = newX)) },
            valueRange = MIN_OFFSET_VALUE..MAX_OFFSET_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(offset.x))

        Text(text = stringResource(R.string.y_offset))
        Slider(
            value = offset.y,
            onValueChange = { newY -> onOffsetChange(offset.copy(y = newY)) },
            valueRange = MIN_OFFSET_VALUE..MAX_OFFSET_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(offset.y))

        Text(text = stringResource(R.string.z_offset))
        Slider(
            value = offset.z,
            onValueChange = { newZ -> onOffsetChange(offset.copy(z = newZ)) },
            valueRange = MIN_OFFSET_VALUE..MAX_OFFSET_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(offset.z))
    }
}

@Composable
fun MaterialColorSliders(
    materialColor: ModelMaterialColor,
    onMaterialColorChange: (ModelMaterialColor) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = stringResource(R.string.change_material_color))
        Spacer(modifier = Modifier.padding(10.dp))

        Text(text = stringResource(R.string.r_material))
        Slider(
            value = materialColor.x,
            onValueChange = { newX -> onMaterialColorChange(materialColor.copy(x = newX)) },
            valueRange = MIN_MATERIAL_COLOR_VALUE..MAX_MATERIAL_COLOR_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(materialColor.x))

        Text(text = stringResource(R.string.g_material))
        Slider(
            value = materialColor.y,
            onValueChange = { newY -> onMaterialColorChange(materialColor.copy(y = newY)) },
            valueRange = MIN_MATERIAL_COLOR_VALUE..MAX_MATERIAL_COLOR_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(materialColor.y))

        Text(text = stringResource(R.string.b_material))
        Slider(
            value = materialColor.z,
            onValueChange = { newZ -> onMaterialColorChange(materialColor.copy(z = newZ)) },
            valueRange = MIN_MATERIAL_COLOR_VALUE..MAX_MATERIAL_COLOR_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(materialColor.z))
    }
}

@Composable
fun MaterialPropertySliders(
    materialProperties: ModelMaterialProperties,
    onMaterialPropertiesChange: (ModelMaterialProperties) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = stringResource(R.string.change_material_factors))
        Spacer(modifier = Modifier.padding(10.dp))

        Text(text = stringResource(R.string.ambient))
        Slider(
            value = materialProperties.ambientOcclusion,
            onValueChange = { newAmbientOcclusion ->
                onMaterialPropertiesChange(
                    materialProperties.copy(
                        ambientOcclusion = newAmbientOcclusion
                    )
                )
            },
            valueRange = MIN_MATERIAL_PROP_VALUE..MAX_MATERIAL_PROP_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(materialProperties.ambientOcclusion))

        Text(text = stringResource(R.string.metallic))
        Slider(
            value = materialProperties.metallic,
            onValueChange = { newMetallic ->
                onMaterialPropertiesChange(
                    materialProperties.copy(
                        metallic = newMetallic
                    )
                )
            },
            valueRange = MIN_MATERIAL_PROP_VALUE..MAX_MATERIAL_PROP_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(materialProperties.metallic))

        Text(text = stringResource(R.string.roughness))
        Slider(
            value = materialProperties.roughness,
            onValueChange = { newRoughness ->
                onMaterialPropertiesChange(
                    materialProperties.copy(
                        roughness = newRoughness
                    )
                )
            },
            valueRange = MIN_MATERIAL_PROP_VALUE..MAX_MATERIAL_PROP_VALUE,
            modifier = Modifier.padding()
        )
        Text(text = "%.2f".format(materialProperties.roughness))
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun ScaleSliderPreview() {
    HelloAndroidXRTheme {
        ScaleSlider(
            scale = 1f,
            onScaleChange = {},
            modifier = Modifier
        )
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun RotationSlidersPreview() {
    HelloAndroidXRTheme {
        RotationSliders(
            rotation = ModelRotation(0f, 0f, 0f, 1f),
            onRotationChange = {}
        )
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun OffsetSlidersPreview() {
    HelloAndroidXRTheme {
        OffsetSliders(
            offset = ModelOffset(0f, 0f, 0f),
            onOffsetChange = {}
        )
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun MaterialColorSlidersPreview() {
    HelloAndroidXRTheme {
        MaterialColorSliders(
            materialColor = ModelMaterialColor(0.5f, 0.5f, 0.5f),
            onMaterialColorChange = {},
            modifier = Modifier
        )
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun MaterialPropertySlidersPreview() {
    HelloAndroidXRTheme {
        MaterialPropertySliders(
            materialProperties = ModelMaterialProperties(0.5f, 0.5f, 0.5f),
            onMaterialPropertiesChange = {},
            modifier = Modifier
        )
    }
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun BugdroidSliderControlsPreview() {
    HelloAndroidXRTheme {
        BugdroidSliderControls(
            visibleSliderGroup = SliderGroup.SCALE,
            modelTransform = ModelTransform(
                scale = 1f,
                rotation = ModelRotation(0f, 0f, 0f, 1f),
                offset = ModelOffset(0f, 0f, 0f),
                materialColor = ModelMaterialColor(0.5f, 0.5f, 0.5f),
                materialProperties = ModelMaterialProperties(0.5f, 0.5f, 0.5f)
            ),
            onScaleChange = {},
            onRotationChange = {},
            onOffsetChange = {},
            onMaterialColorChange = {},
            onMaterialPropertiesChange = {}
        )
    }
}