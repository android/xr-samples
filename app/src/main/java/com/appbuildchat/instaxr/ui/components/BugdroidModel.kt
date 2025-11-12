/*
 * Copyright 2025 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SceneCoreEntity
import androidx.xr.compose.subspace.SceneCoreEntitySizeAdapter
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.scale
import androidx.xr.compose.unit.Meter
import androidx.xr.runtime.math.Vector4
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.KhronosPbrMaterial
import androidx.xr.scenecore.KhronosPbrMaterialSpec
import com.appbuildchat.instaxr.bugdroid.BugdroidController
import com.appbuildchat.instaxr.viewmodel.ModelTransform

// Bugdroid glb height in meters
private const val bugdroidHeight = 2.08f

// The desired amount of the available layout height to use for the bugdroid
private const val fillRatio = 0.5f

const val TAG = "BugdroidModel"

@SuppressLint("RestrictedApi")
@Composable
fun BugdroidModel(
    modelTransform: ModelTransform,
    showBugdroid: Boolean,
    animateBugdroid: Boolean,
    modifier: SubspaceModifier = SubspaceModifier,
) {
    val xrSession = LocalSession.current
    if (xrSession != null && showBugdroid) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val bugdroidController = remember(xrSession, context, coroutineScope) {
            BugdroidController(xrSession, context, coroutineScope)
        }
        val gltfModel = bugdroidController.gltfModel
        gltfModel?.let { model ->
            Subspace {
                val density = LocalDensity.current
                var scaleFromLayout by remember { mutableFloatStateOf(1f) }
                var pbrMaterial by remember { mutableStateOf<KhronosPbrMaterial?>(null) }
                LaunchedEffect(xrSession) {
                    try {
                        val spec =
                            KhronosPbrMaterialSpec.create(
                                lightingModel = KhronosPbrMaterialSpec.LightingModel.LIT,
                                blendMode = KhronosPbrMaterialSpec.BlendMode.OPAQUE,
                                doubleSidedMode = KhronosPbrMaterialSpec.DoubleSidedMode.SINGLE_SIDED,
                            )
                        pbrMaterial = KhronosPbrMaterial.create(xrSession, spec)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating material", e)
                    }
                }
                LaunchedEffect(
                    pbrMaterial,
                    modelTransform.materialColor.x,
                    modelTransform.materialColor.y,
                    modelTransform.materialColor.z,
                    modelTransform.materialColor.w,
                    modelTransform.materialProperties.ambientOcclusion,
                    modelTransform.materialProperties.metallic,
                    modelTransform.materialProperties.roughness,
                ) {
                    pbrMaterial?.setBaseColorFactors(
                        Vector4(
                            x = modelTransform.materialColor.x,
                            y = modelTransform.materialColor.y,
                            z = modelTransform.materialColor.z,
                        )
                    )
                    pbrMaterial?.setAmbientOcclusionFactor(modelTransform.materialProperties.ambientOcclusion)
                    pbrMaterial?.setMetallicFactor(modelTransform.materialProperties.metallic)
                    pbrMaterial?.setRoughnessFactor(modelTransform.materialProperties.roughness)
                }
                SceneCoreEntity(
                    factory = {
                        GltfModelEntity.create(xrSession, model)
                    },
                    update = { entity: GltfModelEntity ->
                        pbrMaterial?.let { newMaterial ->
                            entity.setMaterialOverride(
                                material = newMaterial,
                                "Droid_Solo:Bugdroid"
                            )
                            if (animateBugdroid) {
                                entity.startAnimation(
                                    loop = true, animationName = "Armature|Take 001|BaseLayer"
                                )
                            } else {
                                entity.stopAnimation()
                            }
                        }
                    },
                    sizeAdapter = SceneCoreEntitySizeAdapter(onLayoutSizeChanged = { size ->
                        // Calculate the scale we should use for the entity based on the size the
                        // layout is setting on the SceneCoreEntity
                        val scaleToFillLayoutHeight = Meter
                            .fromPixel(size.height.toFloat(), density).toM() / bugdroidHeight
                        //Limit the scale to a ratio of the available space
                        scaleFromLayout = scaleToFillLayoutHeight * fillRatio
                    }),
                    modifier = modifier.scale(scaleFromLayout * modelTransform.scale)
                )
            }
        }
    }
}