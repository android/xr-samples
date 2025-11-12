# Android XR Guidelines

## Overview
When you're writing your app for **Android XR**, it's important to understand the concepts of **Subspace** and **Spatialized Components**.

You can use subspace composables such as `Volume` and `SpatialPanel` for placing 3D models.  
Some XR components such as `Orbiter` or `SpatialDialog` are standard 2D composables that can be used anywhere in your 2D UI hierarchy, but **SubspaceComposables** must be invoked in your app's subspace.  
To do this, use either the `ApplicationSubspace` composable or the `Subspace` composable.

A `SubspaceModifier` lets you add attributes like **depth**, **offset**, and **positioning** to your subspace composables.

---

## Spatialized Components vs. Subspace Composables

Other spatialized components don't require being called inside a subspace. They consist of conventional 2D elements wrapped within a spatial container. These elements can be used within 2D or 3D layouts if defined for both.

- **Subspace Composables** (e.g., `SpatialPanel`, `SpatialRow`, `SpatialColumn`, `SpatialBox`, `SpatialLayoutSpacer`, `Volume`, `SpatialExternalSurface`) must be invoked inside a `Subspace` (or `ApplicationSubspace`) because they participate in the 3D layout and measurement system.
- **Spatialized Components** (e.g., `Orbiter`, `SpatialDialog`, or other 2D controls placed on a spatial surface) can often be used in either 2D or 3D contexts.

---

## SpatialPanel

A `SpatialPanel` is a subspace composable that lets you display app content — for example, video playback, still images, or any other content in a spatial panel.

```kotlin
Subspace {
    SpatialPanel(
        SubspaceModifier
            .height(824.dp)
            .width(1400.dp),
        dragPolicy = MovePolicy(),
        resizePolicy = ResizePolicy(),
    ) {
        SpatialPanelContent()
    }
}
```

```kotlin
@Composable
fun SpatialPanelContent() {
    Box(
        Modifier
            .background(color = Color.Black)
            .height(500.dp)
            .width(500.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Spatial Panel",
            color = Color.White,
            fontSize = 25.sp
        )
    }
}
```

**Notes:**
- The size of the `SpatialPanel` has been set using the `height` and `width` specifications on the `SubspaceModifier`.  
  Omitting these specifications lets the size be determined by its contents.
- Allow the user to move a panel by adding a `MovePolicy()`.
- Allow the user to resize a panel by adding a `ResizePolicy()`.

---

## Orbiter

An **Orbiter** typically contains navigation and contextual action items related to the entity it's anchored to.

Example:

```kotlin
Subspace {
    SpatialPanel(
        SubspaceModifier
            .height(824.dp)
            .width(1400.dp),
        dragPolicy = MovePolicy(),
        resizePolicy = ResizePolicy(),
    ) {
        SpatialPanelContent()
        OrbiterExample()
    }
}
```

```kotlin
@Composable
fun OrbiterExample() {
    Orbiter(
        position = ContentEdge.Bottom,
        offset = 96.dp,
        alignment = Alignment.CenterHorizontally
    ) {
        Surface(Modifier.clip(CircleShape)) {
            Row(
                Modifier
                    .background(color = Color.Black)
                    .height(100.dp)
                    .width(600.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orbiter",
                    color = Color.White,
                    fontSize = 50.sp
                )
            }
        }
    }
}
```

---

## Spatial Layouts

You can create multiple spatial panels and place them within a spatial layout using `SpatialRow`, `SpatialColumn`, `SpatialBox`, and `SpatialLayoutSpacer`.

```kotlin
Subspace {
    SpatialRow {
        SpatialColumn {
            SpatialPanel(SubspaceModifier.height(250.dp).width(400.dp)) {
                SpatialPanelContent("Top Left")
            }
            SpatialPanel(SubspaceModifier.height(200.dp).width(400.dp)) {
                SpatialPanelContent("Middle Left")
            }
            SpatialPanel(SubspaceModifier.height(250.dp).width(400.dp)) {
                SpatialPanelContent("Bottom Left")
            }
        }
        SpatialColumn {
            SpatialPanel(SubspaceModifier.height(250.dp).width(400.dp)) {
                SpatialPanelContent("Top Right")
            }
            SpatialPanel(SubspaceModifier.height(200.dp).width(400.dp)) {
                SpatialPanelContent("Middle Right")
            }
            SpatialPanel(SubspaceModifier.height(250.dp).width(400.dp)) {
                SpatialPanelContent("Bottom Right")
            }
        }
    }
}
```

**Notes:**
- All spatial layout composables must be placed within a `Subspace`.
- Use `SubspaceModifier` to customize layout.
- For immersive multi-panel setups, set a curve radius around **825.dp** so panels wrap around the user.

---

### Curved Rows (Example Signature)

```kotlin
@Composable
@SubspaceComposable
SpatialCurvedRow(
    modifier: SubspaceModifier,
    alignment: SpatialAlignment,
    horizontalArrangement: SpatialArrangement.Horizontal,
    curveRadius: Dp,
    content: @Composable @SubspaceComposable SpatialRowScope.() -> Unit
)
```

---

## Images & External Surfaces

A `SpatialExternalSurface` is a subspace composable that creates and manages a `Surface` into which your app can draw content, such as an image or video.  
`SpatialExternalSurface` supports either **stereoscopic** or **monoscopic** content.

---

## SubspaceModifier (Detailed)

A `SubspaceModifier` is similar to a Compose modifier for composables in a Subspace.  
It lets you manipulate composables in **3D space**, helping you position, rotate, and add behaviors to 3D layout nodes.

### Layout
By default, a `Subspace` is bounded by the recommended space for viewing an app — similar to bounds in 2D Compose layouts.

### Fill Bounds
Use:
- `fillMaxSize`
- `fillMaxWidth`
- `fillMaxHeight`
- `fillMaxDepth`

These make content fill (partially or fully) the parent bounds.

### Set Size
Use:
- `size`, `width`, `height`, `depth`
- Or `requiredSize`, `requiredWidth`, `requiredHeight`, `requiredDepth`

Units are in **dp**. Convert from meters using `Meter.toDp()`.

### Position Composables
- `offset`: Moves composables in 3D space (x, y, z axes).
- `rotate`: Rotates composables by:
  - **Pitch, Yaw, Roll**
  - **AxisAngle** (Vector3 + degrees)
  - **Quaternion**

### Change Appearance
- `alpha`: Controls element opacity (`0f` = transparent, `1.0f` = opaque).

---

## Summary Table

| Concept | Purpose | Usage Context |
|----------|----------|----------------|
| **Subspace** | 3D container for spatial composables | Root of 3D hierarchy |
| **SpatialPanel** | Displays 2D content in 3D | Inside Subspace |
| **Orbiter** | Anchored 2D controls | Inside SpatialPanel or attached to surface |
| **SubspaceModifier** | Controls 3D layout/positioning | On SubspaceComposables |
| **SpatialRow / SpatialColumn** | Arrange multiple panels | Inside Subspace |
| **SpatialExternalSurface** | Displays images/videos | Inside Subspace |

---

## Best Practices

- Always invoke `SubspaceComposables` inside a `Subspace`.
- Add interactive behaviors such as `MovePolicy()` and `ResizePolicy()` to enhance user control.
- Use **curved rows** (e.g., curve radius ≈ `825.dp`) for immersive layouts.
- Convert spatial measurements using `Meter.toDp()`.
- Keep UI within comfortable **field of view (FOV)** and follow XR **design guidance** for legibility and reach.

---

## Appendix: Example Components and Modifiers (Recap)

**Composables**
- `Subspace`, `ApplicationSubspace`
- `SpatialPanel`, `SpatialExternalSurface`, `Volume`
- Layouts: `SpatialRow`, `SpatialColumn`, `SpatialBox`, `SpatialLayoutSpacer`, `SpatialCurvedRow`
- Controls: `Orbiter`, `SpatialDialog`
- Policies: `MovePolicy`, `ResizePolicy`

**Modifiers**
- `height()`, `width()`, `size()`, `requiredSize()`
- `fillMaxSize()`, `fillMaxWidth()`, `fillMaxHeight()`, `fillMaxDepth()`
- `offset()`, `rotate()`, `alpha()`
