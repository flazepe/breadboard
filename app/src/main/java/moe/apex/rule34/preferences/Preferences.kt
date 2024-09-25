package moe.apex.rule34.preferences

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import moe.apex.rule34.prefs
import moe.apex.rule34.ui.theme.ProcrasturbatingTheme
import moe.apex.rule34.util.SaveDirectorySelection
import moe.apex.rule34.util.TitleBar


@Composable
private fun Heading(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.padding(horizontal = 16.dp),
        text = text,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.sp,
        fontWeight = FontWeight.Medium
    )
}


@Composable
private fun TitleSummary(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null
) {
    Column(
        modifier = modifier.heightIn(min = 64.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = (if (summary == null) 14.dp else 0.dp))
        )

        if (summary != null) {
            Text(
                summary,
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 14.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}


@Composable
private fun SwitchPref(
    checked: Boolean,
    title: String,
    summary: String? = null,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TitleSummary(Modifier.weight(1f), title, summary)
        Switch(checked, onToggle, Modifier.padding(end = 16.dp))
    }
}


@Composable
private fun EnumPref(
    title: String,
    summary: String?,
    enumItems: Array<PrefEnum<*>>,
    selectedItem: PrefEnum<*>,
    onSelection: (PrefEnum<*>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { },
            title = { Text(title) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    for (setting in enumItems) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(100))
                                .selectable(
                                    selected = selectedItem == setting,
                                    onClick = {
                                        onSelection(setting)
                                        showDialog = false
                                    },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                modifier = Modifier.padding(start = 12.dp, end = 16.dp),
                                selected = selectedItem == setting,
                                onClick = null
                            )
                            Text(text = setting.description)
                        }
                    }
                }
            }
        )
    }

    TitleSummary(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        title = title,
        summary = summary
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(navController: NavController) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val scope = rememberCoroutineScope()
    val storageLocationPromptLaunched = remember { mutableStateOf(false) }

    val prefs = LocalContext.current.prefs
    val currentSettings by prefs.getPreferences.collectAsState(Prefs.DEFAULT)
    val storageLocation = currentSettings.storageLocation
    val excludeAi = currentSettings.excludeAi

    ProcrasturbatingTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TitleBar(
                    title = "Settings",
                    scrollBehavior = scrollBehavior,
                    navController = navController
                )
            }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(it)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(12.dp))

                Heading(text = "Data saver")
                EnumPref(
                    title = "Data saver",
                    summary = currentSettings.dataSaver.description,
                    enumItems = DataSaver.entries.toTypedArray(),
                    selectedItem = currentSettings.dataSaver,
                    onSelection = { scope.launch { prefs.updateDataSaver(it as DataSaver) } }
                )

                Spacer(Modifier.height(24.dp))

                Heading(text = "Downloads")
                TitleSummary(
                    modifier = Modifier
                        .clickable { storageLocationPromptLaunched.value = true }
                        .fillMaxWidth(),
                    title = "Save downloads to",
                    summary = if (storageLocation == Uri.EMPTY) "Tap to set"
                    else storageLocation.toString()
                )
                if (storageLocationPromptLaunched.value) {
                    SaveDirectorySelection(storageLocationPromptLaunched)
                }

                Spacer(Modifier.height(24.dp))

                Heading(text = "Searching")
                EnumPref(
                    title = "Image source",
                    summary = currentSettings.imageSource.description,
                    enumItems = ImageSource.entries.toTypedArray(),
                    selectedItem = currentSettings.imageSource,
                    onSelection = { scope.launch { prefs.updateImageSource(it as ImageSource) } }
                )
                SwitchPref(
                    checked = excludeAi,
                    title = "Hide AI-generated images",
                    summary = "Attempt to remove AI-generated images by excluding the " +
                              "'ai_generated' tag in search queries by default."
                ) {
                    scope.launch { prefs.updateExcludeAi(it) }
                }

                HorizontalDivider(Modifier.padding(vertical = 48.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = "When data saver is enabled, images will load in a lower resolution by default. " +
                                "Downloads will always be in the maximum resolution.",
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}