package io.github.rosyidputrar.kembar.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme

/** Section title inside a settings-style list. Marked as a heading for TalkBack navigation. */
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val spacing = KembarTheme.spacing
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(start = spacing.lg, end = spacing.lg, top = spacing.xl, bottom = spacing.sm)
            .semantics { heading() },
    )
}

/** A row that toggles a setting. The whole row is the touch target, not only the switch. */
@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
        modifier = modifier
            .heightIn(min = KembarTheme.spacing.minTouchTarget)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

/** A row that opens another screen. */
@Composable
fun SettingsNavigationItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        trailingContent = {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        modifier = modifier
            .heightIn(min = KembarTheme.spacing.minTouchTarget)
            .clickable(role = Role.Button, onClick = onClick),
    )
}

/**
 * One option of a single-choice group. Put the rows in a parent with
 * `Modifier.selectableGroup()` so TalkBack announces them as a group.
 */
@Composable
fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = summary?.let { { Text(it) } },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = modifier
            .heightIn(min = KembarTheme.spacing.minTouchTarget)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

@PreviewLightDark
@Composable
private fun SettingsItemsPreview() {
    KembarTheme(dynamicColor = false) {
        Surface {
            Column {
                SettingsSectionHeader(title = "Tampilan")
                SettingsRadioItem(title = "Ikuti sistem", selected = true, onClick = {})
                SettingsRadioItem(title = "Gelap", selected = false, onClick = {})
                SettingsSwitchItem(
                    title = "Warna dinamis",
                    summary = "Ikut warna wallpaper kamu",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Outlined.Palette,
                )
                SettingsNavigationItem(title = "Tentang Kembar", onClick = {})
            }
        }
    }
}
