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

package com.example.helloandroidxr.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.math.Vector4
import androidx.xr.scenecore.AlphaMode
import androidx.xr.scenecore.ExperimentalGltfAnimationApi
import androidx.xr.scenecore.GltfAnimationStartOptions
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.KhronosPbrMaterial
import androidx.xr.scenecore.scene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_SCALE = 0.25f
private const val DEFAULT_X_ROTATION = 0.0f
private const val DEFAULT_Y_ROTATION = 0.0f
private const val DEFAULT_Z_ROTATION = 0.0f
private const val DEFAULT_W_ROTATION = 1.0f
private const val DEFAULT_X_OFFSET = 0.0f
private const val DEFAULT_Y_OFFSET = 0.0f
private const val DEFAULT_Z_OFFSET = 0.25f // Start closer to the viewer
private const val DEFAULT_X_MATERIAL_COLOR = 0.0f
private const val DEFAULT_Y_MATERIAL_COLOR = 1.0f
private const val DEFAULT_Z_MATERIAL_COLOR = 0.0f
private const val DEFAULT_W_MATERIAL_COLOR = 1.0f
private const val DEFAULT_AMBIENT_OCCLUSION = 1.0f
private const val DEFAULT_METALLIC = 0.0f
private const val DEFAULT_ROUGHNESS = 0.0f
const val MIN_SCALE_VALUE = 0.1f
const val MAX_SCALE_VALUE = 5.0f
const val MIN_X_ROTATION_VALUE = -15.0f
const val MAX_X_ROTATION_VALUE = 15.0f
const val MIN_Y_ROTATION_VALUE = -15.0f
const val MAX_Y_ROTATION_VALUE = 15.0f
const val MIN_Z_ROTATION_VALUE = -50.0f
const val MAX_Z_ROTATION_VALUE = 50.0f
const val MIN_W_ROTATION_VALUE = -5.0f
const val MAX_W_ROTATION_VALUE = 5.0f
const val MIN_OFFSET_VALUE = -1.5f
const val MAX_OFFSET_VALUE = 1.5f
const val MIN_MATERIAL_COLOR_VALUE = 0.0f
const val MAX_MATERIAL_COLOR_VALUE = 1.0f
const val MIN_MATERIAL_PROP_VALUE = 0.0f
const val MAX_MATERIAL_PROP_VALUE = 1.0f

// Represents the rotation values for the 3D model
data class ModelRotation(
    val x: Float = DEFAULT_X_ROTATION,
    val y: Float = DEFAULT_Y_ROTATION,
    val z: Float = DEFAULT_Z_ROTATION,
    val w: Float = DEFAULT_W_ROTATION,
)

// Represents the offset values for the 3D model
data class ModelOffset(
    val x: Float = DEFAULT_X_OFFSET,
    val y: Float = DEFAULT_Y_OFFSET,
    val z: Float = DEFAULT_Z_OFFSET,
)

// Represents the material color values for the 3D model
data class ModelMaterialColor(
    val x: Float = DEFAULT_X_MATERIAL_COLOR,
    val y: Float = DEFAULT_Y_MATERIAL_COLOR,
    val z: Float = DEFAULT_Z_MATERIAL_COLOR,
    val w: Float = DEFAULT_W_MATERIAL_COLOR,
)

// Represents the material properties for the 3D model
data class ModelMaterialProperties(
    val ambientOcclusion: Float = DEFAULT_AMBIENT_OCCLUSION,
    val metallic: Float = DEFAULT_METALLIC,
    val roughness: Float = DEFAULT_ROUGHNESS,
)

// Represents the transform values for the 3D model
data class ModelTransform(
    val scale: Float = DEFAULT_SCALE,
    val rotation: ModelRotation = ModelRotation(),
    val offset: ModelOffset = ModelOffset(),
    val materialColor: ModelMaterialColor = ModelMaterialColor(),
    val materialProperties: ModelMaterialProperties = ModelMaterialProperties(),
)

// Enum to represent which slider group is visible.
// This prevents impossible states, like two groups showing at once.
enum class SliderGroup {
    NONE, SCALE, ROTATION, OFFSET, MATERIAL_COLORS, MATERIAL_PROPERTIES
}

// The single state object for the entire screen
data class BugdroidUiState(
    val showBugdroid: Boolean = false,
    val animateBugdroid: Boolean = false,
    val visibleSliderGroup: SliderGroup = SliderGroup.NONE,
    val modelTransform: ModelTransform = ModelTransform(),
    val capturedCommand: String = "",
)

class BugdroidViewModel : ViewModel() {
    // Private mutable state
    private val _uiState = MutableStateFlow(BugdroidUiState())

    // Public immutable state flow for the UI to observe
    val uiState: StateFlow<BugdroidUiState> = _uiState.asStateFlow()
    private var xrSession: Session? = null
    private var gltfModel: GltfModel? = null
    private var gltfEntity: GltfModelEntity? = null
    private var pbrMaterial: KhronosPbrMaterial? = null

    fun updateCapturedCommand(command: String) {
        _uiState.update { it.copy(capturedCommand = command) }
    }

    fun initSession(session: Session) {
        if (this.xrSession == session) return
        this.xrSession = session

        /**
         * This demonstrates working with a 3D model directly with XR SceneCore APIs. We generally
         * recommend working with Jetpack Compose for XR when possible.
         * In this case, for example, we could use a SceneCoreEntity to leverage Compose for layout
         */
        viewModelScope.launch {
            try {
                // Create the model
                val model = GltfModel.create(
                    session,
                    "models/bugdroid_animated_wave.glb".toUri()
                )
                gltfModel = model

                // Create the entity from the model
                val entity = GltfModelEntity.create(
                    session = session,
                    model = model,
                    parent = session.scene.activitySpace
                )
                gltfEntity = entity

                // Create the material
                val material = KhronosPbrMaterial.create(
                    session = session,
                    alphaMode = AlphaMode.OPAQUE
                ).also { pbrMaterial = it }

                val currentTransform = _uiState.value.modelTransform
                material.setBaseColorFactor(
                    Vector4(
                        x = currentTransform.materialColor.x,
                        y = currentTransform.materialColor.y,
                        z = currentTransform.materialColor.z,
                        w = currentTransform.materialColor.w
                    )
                )
                material.setMetallicFactor(currentTransform.materialProperties.metallic)
                material.setRoughnessFactor(currentTransform.materialProperties.roughness)

                // Apply the material to the correct node in the entity
                val bugdroidNode = entity.nodes.find { it.name == "Droid_Solo:Bugdroid" }
                bugdroidNode?.setMaterialOverride(material)

                updateEntityTransform(currentTransform)
                entity.setEnabled(_uiState.value.showBugdroid)

                updateAnimationState(_uiState.value.animateBugdroid)
            } catch (e: Exception) {
                Log.e("BugdroidViewModel", "Failed to load Bugdroid model entity: $e")
            }
        }
    }

    private fun updateEntityTransform(transform: ModelTransform) {
        val entity = gltfEntity ?: return
        entity.setScale(transform.scale)
        entity.setPose(
            Pose(
                translation = Vector3(
                    x = transform.offset.x,
                    y = transform.offset.y,
                    z = transform.offset.z
                ),
                rotation = Quaternion(
                    x = transform.rotation.x,
                    y = transform.rotation.y,
                    z = transform.rotation.z,
                    w = transform.rotation.w
                )
            )
        )
    }

    @OptIn(ExperimentalGltfAnimationApi::class)
    @SuppressLint("NewApi")
    private fun updateAnimationState(animate: Boolean) {
        val entity = gltfEntity ?: return
        val animation = entity.getAnimations().find {
            it.name == "Armature|Take 001|BaseLayer"
        } ?: return

        if (animate) {
            animation.start(GltfAnimationStartOptions(shouldLoop = true))
        } else {
            animation.stop()
        }
    }

    fun updateShownSliderGroup(group: SliderGroup) {
        _uiState.update { currentState ->
            currentState.copy(visibleSliderGroup = group)
        }
    }

    fun updateShowBugdroid() {
        _uiState.update { currentState ->
            val nextState = !currentState.showBugdroid
            gltfEntity?.setEnabled(nextState)
            currentState.copy(showBugdroid = nextState)
        }
    }

    fun updateAnimateBugdroid() {
        _uiState.update { currentState ->
            val nextState = !currentState.animateBugdroid
            updateAnimationState(nextState)
            currentState.copy(animateBugdroid = nextState)
        }
    }

    fun updateScale(newScale: Float) {
        _uiState.update { currentState ->
            val updatedTransform = currentState.modelTransform.copy(
                scale = newScale.coerceIn(MIN_SCALE_VALUE, MAX_SCALE_VALUE)
            )
            updateEntityTransform(updatedTransform)
            currentState.copy(modelTransform = updatedTransform)
        }
    }

    fun updateRotation(newRotation: ModelRotation) {
        _uiState.update { currentState ->
            val updatedRotation = newRotation.copy(
                x = newRotation.x.coerceIn(MIN_X_ROTATION_VALUE, MAX_X_ROTATION_VALUE),
                y = newRotation.y.coerceIn(MIN_Y_ROTATION_VALUE, MAX_Y_ROTATION_VALUE),
                z = newRotation.z.coerceIn(MIN_Z_ROTATION_VALUE, MAX_Z_ROTATION_VALUE),
                w = newRotation.w.coerceIn(MIN_X_ROTATION_VALUE, MAX_W_ROTATION_VALUE)
            )
            val updatedTransform = currentState.modelTransform.copy(rotation = updatedRotation)
            updateEntityTransform(updatedTransform)
            currentState.copy(modelTransform = updatedTransform)
        }
    }

    fun updateOffset(newOffset: ModelOffset) {
        _uiState.update { currentState ->
            val updatedOffset = currentState.modelTransform.offset.copy(
                x = newOffset.x.coerceIn(MIN_OFFSET_VALUE, MAX_OFFSET_VALUE),
                y = newOffset.y.coerceIn(MIN_OFFSET_VALUE, MAX_OFFSET_VALUE),
                z = newOffset.z.coerceIn(MIN_OFFSET_VALUE, MAX_OFFSET_VALUE),
            )
            val updatedTransform = currentState.modelTransform.copy(offset = updatedOffset)
            updateEntityTransform(updatedTransform)
            currentState.copy(modelTransform = updatedTransform)
        }
    }

    fun updateMaterialColor(newMaterialColor: ModelMaterialColor) {
        _uiState.update { currentState ->
            val updatedColor = currentState.modelTransform.materialColor.copy(
                x = newMaterialColor.x.coerceIn(
                    MIN_MATERIAL_COLOR_VALUE,
                    MAX_MATERIAL_COLOR_VALUE
                ),
                y = newMaterialColor.y.coerceIn(
                    MIN_MATERIAL_COLOR_VALUE,
                    MAX_MATERIAL_COLOR_VALUE
                ),
                z = newMaterialColor.z.coerceIn(
                    MIN_MATERIAL_COLOR_VALUE,
                    MAX_MATERIAL_COLOR_VALUE
                ),
                w = newMaterialColor.w.coerceIn(
                    MIN_MATERIAL_COLOR_VALUE,
                    MAX_MATERIAL_COLOR_VALUE
                ),
            )
            val updatedTransform = currentState.modelTransform.copy(materialColor = updatedColor)
            pbrMaterial?.setBaseColorFactor(
                Vector4(
                    x = updatedColor.x,
                    y = updatedColor.y,
                    z = updatedColor.z,
                    w = updatedColor.w
                )
            )
            currentState.copy(modelTransform = updatedTransform)
        }
    }

    fun updateMaterialProperties(newMaterialProperties: ModelMaterialProperties) {
        _uiState.update { currentState ->
            val updatedProperties = currentState.modelTransform.materialProperties.copy(
                ambientOcclusion = newMaterialProperties.ambientOcclusion.coerceIn(
                    MIN_MATERIAL_PROP_VALUE,
                    MAX_MATERIAL_PROP_VALUE
                ),
                metallic = newMaterialProperties.metallic.coerceIn(
                    MIN_MATERIAL_PROP_VALUE,
                    MAX_MATERIAL_PROP_VALUE
                ),
                roughness = newMaterialProperties.roughness.coerceIn(
                    MIN_MATERIAL_PROP_VALUE,
                    MAX_MATERIAL_PROP_VALUE
                ),
            )
            val updatedTransform =
                currentState.modelTransform.copy(materialProperties = updatedProperties)
            pbrMaterial?.setMetallicFactor(updatedProperties.metallic)
            pbrMaterial?.setRoughnessFactor(updatedProperties.roughness)
            currentState.copy(modelTransform = updatedTransform)
        }
    }

    fun resetModel() {
        _uiState.update { currentState ->
            val resetTransform = ModelTransform()
            updateEntityTransform(resetTransform)
            pbrMaterial?.let { material ->
                material.setBaseColorFactor(
                    Vector4(
                        x = resetTransform.materialColor.x,
                        y = resetTransform.materialColor.y,
                        z = resetTransform.materialColor.z,
                        w = resetTransform.materialColor.w
                    )
                )
                material.setMetallicFactor(resetTransform.materialProperties.metallic)
                material.setRoughnessFactor(resetTransform.materialProperties.roughness)
            }
            updateAnimationState(false)
            currentState.copy(
                modelTransform = resetTransform,
                animateBugdroid = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        gltfEntity?.parent = null
        gltfEntity = null
        gltfModel?.close()
        gltfModel = null
        pbrMaterial?.close()
        pbrMaterial = null
        xrSession = null
    }
}