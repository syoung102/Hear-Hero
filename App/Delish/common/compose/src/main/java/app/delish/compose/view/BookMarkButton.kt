/*
 * Copyright 2021 The Android Open Source Project
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

package app.delish.compose.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BookMarkButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    selected: Boolean = false,
    onBookMark: () -> Unit
) {
    val icon = if (selected) Icons.Outlined.Bookmark else Icons.Filled.BookmarkBorder
    Surface(
        color = backgroundColor,
        shape = CircleShape,
        modifier = modifier
            .requiredSize(36.dp, 36.dp)
            .clickable {
                onBookMark()
            }
    ) {
        Icon(
            imageVector = icon,
            tint = colorResource(id = android.R.color.white),
            contentDescription = null,
            modifier = Modifier
                .padding(6.dp)
        )
    }
}

@Composable
@Preview
fun previewBookMarkButtonClicked() {
    BookMarkButton(selected = false, backgroundColor = Color.Black) {}
}

@Composable
@Preview
fun previewBookMarkButtonUnClicked() {
    BookMarkButton(selected = true, backgroundColor = Color.Black) {}
}
