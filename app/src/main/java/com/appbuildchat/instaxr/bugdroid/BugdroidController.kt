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

package com.appbuildchat.instaxr.bugdroid

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.xr.runtime.Session
import androidx.xr.scenecore.GltfModel
import com.appbuildchat.instaxr.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStream

class BugdroidController(
    private val xrSession: Session?,
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    var gltfModel by mutableStateOf<GltfModel?>(null)

    init {
        loadBugdroidModel()
    }

    private fun loadBugdroidModel() {
        coroutineScope.launch {
            gltfModel = BugdroidGltfModelCache.getOrLoadModel(xrSession, context)
        }
    }
}

private object BugdroidGltfModelCache {
    private var cachedModel: GltfModel? = null
    @SuppressLint("RestrictedApi")
    suspend fun getOrLoadModel(
        xrCoreSession: Session?, context: Context
    ): GltfModel? {
        xrCoreSession ?: run {
            Log.w(TAG, "Cannot load model, session is null.")
            return null
        }
        return if (cachedModel == null) {
            try {
                val inputStream: InputStream =
                    context.resources.openRawResource(R.raw.bugdroid_animated_wave)
                cachedModel = GltfModel.create(
                    xrCoreSession, inputStream.readBytes(), "BUGDROID"
                )
                cachedModel
            } catch (e: Exception) {
                Log.e(TAG, "Error loading GLTF model", e)
                null
            }
        } else {
            cachedModel
        }
    }

    fun clearCache() {
        cachedModel = null
    }

    const val TAG = "BugdroidGltfModelCache"
}