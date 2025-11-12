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

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import com.appbuildchat.instaxr.R
import com.appbuildchat.instaxr.environment.EnvironmentController
import com.appbuildchat.instaxr.ui.theme.InstaXRTheme

/**
 * Controls for changing the user's Environment, and toggling between Home Space and Full Space
 */
@Composable
fun EnvironmentControls(modifier: Modifier = Modifier) {
    // If we aren't able to access the session, these buttons wouldn't work and shouldn't be shown
    val activity = LocalActivity.current
    val session = LocalSession.current
    if (session != null && activity is ComponentActivity) {
        val uiIsSpatialized = LocalSpatialCapabilities.current.isSpatialUiEnabled
        val environmentController = remember(activity) {
            EnvironmentController(session, activity.lifecycleScope)
        }
        //load the model early so it's in memory for when we need it
        val environmentModelName = "green_hills_ktx2_mipmap.glb"
        environmentController.loadModelAsset(environmentModelName)

        Surface(modifier.clip(CircleShape)) {
            Row(Modifier.width(IntrinsicSize.Min)) {
                if (uiIsSpatialized) {
                    SetVirtualEnvironmentButton {
                        environmentController.requestCustomEnvironment(
                            environmentModelName
                        )
                    }
                    SetPassthroughButton { environmentController.requestPassthrough() }
                    VerticalDivider(
                        modifier = Modifier
                            .height(32.dp)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    RequestHomeSpaceButton { environmentController.requestHomeSpaceMode() }
                } else {
                    RequestFullSpaceButton { environmentController.requestFullSpaceMode() }
                }
            }
        }
    }
}

@Composable
private fun SetVirtualEnvironmentButton(
    modifier: Modifier = Modifier, onclick: () -> Unit
) {
    IconButton(
        onClick = onclick,
        modifier = modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
            .size(56.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.environment_24px),
            contentDescription = stringResource(id = R.string.set_virtual_environment),
        )
    }
}

@Composable
private fun SetPassthroughButton(
    modifier: Modifier = Modifier, onclick: () -> Unit
) {
    IconButton(
        onClick = onclick,
        modifier = modifier
            .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
            .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
            .size(56.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.passthrough_24px),
            contentDescription = stringResource(id = R.string.set_passthrough),
        )
    }
}

@Composable
private fun RequestHomeSpaceButton(onclick: () -> Unit) {
    IconButton(
        onClick = onclick,
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
            .size(56.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_request_home_space),
            contentDescription = stringResource(R.string.enter_home_space_mode)
        )
    }
}

@Composable
private fun RequestFullSpaceButton(onclick: () -> Unit) {
    IconButton(
        onClick = onclick, modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_request_full_space),
            contentDescription = stringResource(R.string.enter_full_space_mode)
        )
    }
}

@Preview
@Composable
private fun PreviewSetVirtualEnvironmentButton() {
    InstaXRTheme {
        SetVirtualEnvironmentButton {}
    }
}

@Preview
@Composable
private fun PreviewRequestHomeSpaceButton() {
    InstaXRTheme {
        RequestHomeSpaceButton {}
    }
}

@Preview
@Composable
private fun PreviewRequestFullSpaceButton() {
    InstaXRTheme {
        RequestFullSpaceButton {}
    }
}

@Preview
@Composable
private fun PreviewSetPassthroughButton() {
    InstaXRTheme {
        SetPassthroughButton {}
    }
}
