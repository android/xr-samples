# Android XR Guidelines

## Transition from Material Components to XR-Differentiated

---

### App Bar

An **app bar** can appear in an **orbiter** for a more immersive experience. Currently, this spatial capability is only available in **full space**. In **home space**, use a regular app bar on the same plane as the body content to mimic a 2D experience.

- The app bar orbiter should be **centered and anchored to the top of the app**.
- In most cases, apps should only have **one app bar orbiter**, placed in a **global context**.
- Include a **20dp margin** to visually separate the app bar orbiter from the spatial panel and prevent content obstruction.
- Always **align the app bar orbiter** within the bounds of nearby spatial panels.
- Adjust the **width of the app bar orbiter** to fit within a person’s field of view.

---

### Dialogs

For effective **visual hierarchy**, a dialog should be the **most prominent element** in your scene.

- Add a **scrim** behind a dialog to improve visibility and prevent other content from being selected until the dialog action is complete.
- The dialog should have the **highest elevation** in the product.
  - Example: If a dialog uses `surfaceContainerHigh`, do not use `surfaceContainerHighest` for other elements.
- **Basic dialogs** are recommended when designing for XR’s expanded window sizes to keep the required action within the person’s field of view.
- **Full-screen dialogs** should be limited to compact window sizes (e.g., mobile devices).
- **Spatial dialogs** should be **centered in the user’s field of view (FOV)**.
- If the dialog cannot track head movements, position it **in the center of the app’s content**.

---

### Navigation Bar

In **full space**, a **navigation bar** can appear in an **orbiter** for a more immersive experience. Currently, spatial capabilities such as orbiters are only available in full space. In **home space**, use a regular navigation bar on the same plane as body content to mimic a 2D experience.

- When placed in global context, the **navigation bar orbiter** is centered at the **bottom of the app it controls**.
- It stays **anchored** to the app during layout or content changes, ensuring navigation elements remain easy to find and use.
- **Do not obstruct content.** To maintain balance and avoid clutter:
  - The navigation bar orbiter should **overlap spatial panels by 12dp** and **no more than half their height**.
  - Its **width should not exceed** that of adjacent spatial panels.
- A navigation bar orbiter should always be placed **at the bottom of a spatial panel** and **within the user’s immediate field of view**.

---

### Toolbar

There is **one toolbar orbiter**, closely aligned with the floating toolbar. It can be configured as **horizontal** or **vertical**.

- A toolbar can appear in an **orbiter** for a more immersive experience (currently available only in **full space**).
- When placed in global context, the **toolbar orbiter** is centered **at the bottom of the app**.
- Toolbars with **more than five items** can **expand and collapse** to reveal or hide additional content.
- When a toolbar orbiter expands, it stays **within the bounds of the adjacent spatial panel**.
- More complex toolbars can be **split into multiple toolbars**.
- Always **align the toolbar orbiter** within the horizontal bounds of nearby spatial panels.
- **Avoid placing vertical toolbar orbiters** between spatial panels.

---

*End of document.*
