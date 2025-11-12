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

package com.appbuildchat.instaxr.environment

import android.net.Uri
import android.util.Log
import androidx.xr.runtime.Session
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.SpatialEnvironment
import androidx.xr.scenecore.scene
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EnvironmentController(private val xrSession: Session, private val coroutineScope: CoroutineScope) {
    private val assetCache: HashMap<String, Any> = HashMap()
    private var activeEnvironmentModelName: String? = null

    fun requestHomeSpaceMode() = xrSession.scene.requestHomeSpaceMode()

    fun requestFullSpaceMode() = xrSession.scene.requestFullSpaceMode()

    fun requestPassthrough() {
        xrSession.scene.spatialEnvironment.preferredPassthroughOpacity = 1f
    }

    /**
     * Request the system load a custom Environment
     */
    fun requestCustomEnvironment(environmentModelName: String) {
        coroutineScope.launch {
            try {
                if (activeEnvironmentModelName == null ||
                    activeEnvironmentModelName != environmentModelName
                ) {

                    val environmentModel = assetCache[environmentModelName] as GltfModel

                    SpatialEnvironment.SpatialEnvironmentPreference(
                        skybox = null,
                        geometry = environmentModel
                    ).let {
                        xrSession.scene.spatialEnvironment.preferredSpatialEnvironment = it
                    }
                    activeEnvironmentModelName = environmentModelName
                }
                xrSession.scene.spatialEnvironment.preferredPassthroughOpacity = 0f

            } catch (e: Exception) {
                Log.e(
                    "Hello Android XR",
                    "Failed to update Environment Preference for $environmentModelName: $e"
                )
            }
        }
    }

    fun loadModelAsset(modelName: String) {
        coroutineScope.launch {
            //load the asset if it hasn't been loaded previously
            if (!assetCache.containsKey(modelName)) {
                try {
                    val gltfModel =
                        GltfModel.create(xrSession, Uri.parse(modelName))
                    assetCache[modelName] = gltfModel

                } catch (e: Exception) {
                    Log.e(
                        "Hello Android XR",
                        "Failed to load model for $modelName: $e"
                    )
                }
            }
        }
    }
}
