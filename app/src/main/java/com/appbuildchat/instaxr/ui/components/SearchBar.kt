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

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appbuildchat.instaxr.R
import com.appbuildchat.instaxr.ui.theme.InstaXRTheme

/**
 * A search textbox that expands its surface when spatial UI is enabled
 */
@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface(modifier = modifier.clip(CircleShape)) {
        SearchTextBox(
            onSearch = { query ->
                showNotImplementedToast(
                    query = query,
                    context = context
                )
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun SearchTextBox(
    onSearch: (query: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = {
            Text(
                stringResource(R.string.search_product_name),
            )
        },
        leadingIcon = { Icon(Icons.Filled.Search, null) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        // keyboardOptions change the newline key to a search key on the soft keyboard
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // keyboardActions submits the search query when the search key is pressed
        keyboardActions = KeyboardActions(onSearch = {
            onSearch(text)
            text = ""
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
        }),
        modifier = modifier
            .width(640.dp)
            .height(56.dp)
            .clip(CircleShape)
    )
}

private fun showNotImplementedToast(query: String, context: Context) {
    Toast.makeText(
        context, context.getString(R.string.search_is_not_implemented, query), Toast.LENGTH_SHORT
    ).show()
}

@Composable
@Preview(device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(device = "spec:width=411dp,height=891dp")
fun SearchTextBoxPreview() {
    InstaXRTheme {
        SearchTextBox(onSearch = {})
    }
}