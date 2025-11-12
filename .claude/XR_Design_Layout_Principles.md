# XR Design / Layout Principles

## Overview

An **Android XR differentiated app** has a user experience explicitly designed for XR and implements features that are only offered on XR.  
You can take full advantage of Android XR capabilities and differentiate your app's experiences by adding XR features such as **spatial panels**, **orbiters**, and **environments**.

Because Android XR-differentiated apps are highly customized, some of the listed capabilities are applicable only to specific types of apps.  
Choose the capabilities that best fit your use case.

---

## Basic XR

Your app should implement at least one **XR-specific feature** or piece of XR-specific content to enhance the user experience.  
This can include:

- An **Orbiter**
- One or more **Spatial Panels**
- **Environments** or **3D Objects**

---

## Spatial Panels

When multitasking (i.e., completing two or more tasks at once) with panels, create **separate spatial panels**.  
For example, you should create separate spatial panels for:
- Chat windows
- Lists or task panes

Place menus, assets, and controls in a **dedicated panel or orbiter** — do not include them in the main editing panel.

---

## Environments

When showing a virtual environment, brightness can be distracting and fatiguing. Follow these guidelines:

- Present a **safe tonal range** with no spikes in brightness that conflict with UI or cause fatigue.  
- Ensure **UI is legible in all directions**, especially within the **middle horizontal band** of the user's gaze.  
- Avoid high contrast flashes or sudden luminance changes.

*(Detailed guidelines will be linked in the future.)*

---

## Transitioning Between Home Space (HSM) and Full Space (FSM)

When taking users to **Full Space**, your app must provide a clear entry point for users to transition between spaces.

- Use an **icon or label** to indicate transitions.
- Place the button in an **easy-to-access location**.

---

## Menu / List Scrolling

Enhance scroll interactions (e.g., carousels or vertical lists) with **physics or momentum**.

For example:
- Scrolling should have inertia — content continues moving briefly after user interaction.
- Gradually slow to a stop instead of stopping instantly.

This provides a more natural, tactile experience in XR environments.

---

## Interaction Design Principles

### 1. Familiarity
- Use **common UI elements** like buttons, menus, and text fields that users already recognize.
- Maintain **consistent interaction patterns** to support intuitive navigation.
- Add **visual cues** to indicate interactable objects or regions.

### 2. Multi-modal Interaction
- Allow interaction via **hands**, **eyes**, and **voice**.
- Ensure users can interact from different **body positions** (standing, seated, reclining).

### 3. Ergonomics
- **Center interactable elements** within the user’s field of view (FOV) to minimize head and eye strain.
- **Reserve large-scale body movements** only for actions that enhance immersion.

### 4. Color & Lighting
- Ensure **sufficient color contrast** to aid users with color vision differences.
- Maintain **readable contrast ratios**, especially with transparent or semi-transparent backgrounds.
- Use **dimming** to separate your app’s visuals from the real-world environment.

### 5. Accessibility & Comfort
- Use **larger UI and pointer targets** to simplify object selection and manipulation.
- **Reduce cognitive load** by limiting simultaneous choices.
- Offer **visual or audio feedback** after actions to confirm completion.

---

## Spatial Layout and Field of View

For optimal comfort:
- Place content in the **center 41°** of the user's field of view.  
- Avoid forcing users to look at extreme angles for essential interactions.

You can break your app into **spatial panels**, **orbiters**, and add **spatial elevation** for depth and hierarchy.

---

## Orbiters

Use **Orbiters** sparingly and deliberately.

- Excessive spatialized UI elements can lead to **visual fatigue** and **cognitive overload**.  
- Adapt only a few key navigation components (e.g., **navigation rail**, **navigation bar**) into orbiters.

### Orbiter Placement & Sizing

- **Offset:** 20dp is the recommended visual distance.
- **Elevation:** Default elevation is **15dp in Z-depth**; adjust if needed using spatial elevation levels.
- **Avoid spatializing large planes** (e.g., bottom sheets, side sheets).

---

## Typography & Targets

- Use **font size ≥ 14dp** with **normal or higher weight** for legibility.
- **Pointer targets:** 56dp is optimal for touch and gaze selection.
- Include **hover** and **focus states** to improve accessibility.

---

## References

- [Android XR Design Guidelines](https://developer.android.com/design/ui/xr/guides/get-started)

---

*This document summarizes key design and layout recommendations for creating comfortable, accessible, and performant XR experiences in Android.*
