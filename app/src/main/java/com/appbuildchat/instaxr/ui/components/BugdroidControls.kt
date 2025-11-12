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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appbuildchat.instaxr.R
import com.appbuildchat.instaxr.viewmodel.SliderGroup

/**
 * Controls for changing the Gltf Model's Scale, Position, and Rotation
 */
@Composable
fun BugdroidControls(
    onSliderGroupSelected: (SliderGroup) -> Unit,
    onResetModel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                Modifier
                    .padding(25.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = { onSliderGroupSelected(SliderGroup.SCALE) },
                    modifier = modifier
                ) {
                    Text(text = stringResource(R.string.scale))
                }
                Button(
                    onClick = { onSliderGroupSelected(SliderGroup.ROTATION) },
                    modifier = modifier
                ) {
                    Text(text = stringResource(R.string.rotation))
                }
                Button(
                    onClick = { onSliderGroupSelected(SliderGroup.OFFSET) },
                    modifier = modifier
                ) {
                    Text(text = stringResource(R.string.offset))
                }
            }
            Column(
                Modifier
                    .padding(25.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = { onSliderGroupSelected(SliderGroup.MATERIAL_COLORS) },
                    modifier = modifier
                ) {
                    Text(text = stringResource(R.string.material_color))
                }
                Button(
                    onClick = { onSliderGroupSelected(SliderGroup.MATERIAL_PROPERTIES) },
                    modifier = modifier
                ) {
                    Text(text = stringResource(R.string.material_properties))
                }
                Button(
                    onClick = {
                        onResetModel()
                        onSliderGroupSelected(SliderGroup.NONE)
                    },
                    modifier = modifier
                ) {
                    Text(text = stringResource(R.string.reset))
                }
            }
        }
    }
}

@Preview
@Composable
fun BugdroidControlsPreview() {
    BugdroidControls(onSliderGroupSelected = {}, onResetModel = {})
}
