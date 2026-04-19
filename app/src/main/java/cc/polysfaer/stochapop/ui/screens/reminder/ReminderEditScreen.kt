@file:OptIn(ExperimentalMaterial3Api::class)

package cc.polysfaer.stochapop.ui.screens.reminder

import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.controller.sendNotification
import cc.polysfaer.stochapop.ui.AppViewModelProvider
import cc.polysfaer.stochapop.ui.navigation.NavigationDestination
import cc.polysfaer.stochapop.ui.theme.StochaPopTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object NewReminderDestination : NavigationDestination {
    override val route = "new_reminder"
    override val titleRes = R.string.new_screen_name
}

object EditReminderDestination : NavigationDestination {
    override val route = "edit_reminder"
    override val titleRes = R.string.edit_screen_name

    const val REMINDER_ID_ARG = "reminderId"
    val routeWithArgs = "$route/{$REMINDER_ID_ARG}"

    val arguments = listOf(
        navArgument(REMINDER_ID_ARG) { type = NavType.IntType }
    )
}

data class EditActions(
    val onTitleChange: (String) -> Unit = {},
    val onMessageChange: (String) -> Unit = {},
    val onToggleEnable: (Boolean) -> Unit = {},
    val onToggleRandomRange: (Boolean) -> Unit = {},
    val onToggleSound: (Boolean) -> Unit = {},
    val onToggleVibration: (Boolean) -> Unit = {},
    val onStartTimeChange: (LocalTime) -> Unit = {},
    val onEndTimeChange: (LocalTime) -> Unit = {},
    val onNotificationCountChange: (Int) -> Unit = {},
    val onSelectedDaysChanged: (Set<DayOfWeek>) -> Unit = {},
    val onSaveButtonClick: () -> Unit = {},
    val onDeleteButtonClick: () -> Unit = {},
    val onCancelButtonClick: () -> Unit = {},
)

@Composable
fun ReminderEditScreen(
    viewModel: ReminderEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBack: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val actions = remember(viewModel, navigateBack) {
        EditActions(
            onTitleChange = viewModel::setTitle,
            onMessageChange = viewModel::setMessage,
            onToggleEnable = viewModel::toggleEnable,
            onToggleRandomRange = viewModel::toggleRandomRange,
            onToggleSound = viewModel::toggleSound,
            onToggleVibration = viewModel::toggleVibration,
            onStartTimeChange = viewModel::setStartTime,
            onEndTimeChange = viewModel::setTimeRangeEnd,
            onNotificationCountChange = viewModel::setRangeNotificationCount,
            onSelectedDaysChanged = viewModel::setSelectedDays,
            onSaveButtonClick = {
                coroutineScope.launch {
                    viewModel.saveReminder()
                    navigateBack()
                }
            },
            onDeleteButtonClick = {
                coroutineScope.launch {
                    viewModel.deleteReminder()
                    navigateBack()
                }
            },
            onCancelButtonClick = {
                navigateBack()
            }
        )
    }

    BackHandler(enabled = true) {
        navigateBack()
    }

    if (viewModel.uiState.initialLoadDone) {
        EditScreenContent(
            isEditMode = viewModel.isEditMode,
            uiState = viewModel.uiState,
            actions = actions
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreenContent(
    modifier: Modifier = Modifier,
    isEditMode: Boolean = true,
    uiState: ReminderEditUIState = ReminderEditUIState(),
    actions: EditActions = EditActions(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val reminder = uiState.reminderDetails

    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animatedHeight by animateDpAsState(
        targetValue = if (isFocused) 250.dp else 125.dp,
    )


    // -------------

    var showDeletionConfirmDialog by remember { mutableStateOf(false) }

    // Be sure the mutable is set to false once the screen is disposed.
    DisposableEffect(Unit) {
        onDispose {
            showDeletionConfirmDialog = false
        }
    }

    // -------------

    Column(
        modifier = modifier
            .padding(horizontal = 12.dp)
            // .statusBarsPadding()
            // .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
    ) {
        ReminderTitleField(
            reminder.title,
            actions.onTitleChange,
            focusManager = focusManager,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 3.dp)
        )
        
        ReminderTextField(
            reminder.message,
            actions.onMessageChange,
            focusManager = focusManager,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp)
                .height(animatedHeight),
            interactionSource = interactionSource
        )

        // TEST Button
        CustomEditButton(
            labelId = R.string.edit_test_btn,
            onClick = {
                sendNotification(
                    context = context,
                    reminder = reminder.toReminder(), //
                    backToActivityOnClick = false
                )
            },
            icon = { Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = null
            ) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        )

        CustomHorizontalDivider()

        CheckRow(
            R.string.edit_enable_label,
            reminder.enabled,
            actions.onToggleEnable,
            modifier = Modifier.fillMaxWidth()
        )

        CheckRow(
            R.string.use_random_range_label,
            reminder.useRandomRange,
            actions.onToggleRandomRange,
            modifier = Modifier.fillMaxWidth()
        )

        AlarmTypeRow(
            hasSound = reminder.hasSound,
            hasVibration = reminder.hasVibration,
            onHasSoundChange = actions.onToggleSound,
            onHasVibrationChange = actions.onToggleVibration,
        )

        CustomHorizontalDivider()

        DaySelectionRow(
            context = context,
            selectedDays = reminder.selectedDays,
            onSelectedDaysChanged = actions.onSelectedDaysChanged,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )

        CustomHorizontalDivider()

        Column {
            TimeInputRow(
                useRange = reminder.useRandomRange,
                startTime = reminder.startTime,
                endTime = reminder.endTime,
                onStartTimeChange = actions.onStartTimeChange,
                onEndTimeChange = actions.onEndTimeChange,
                is24Hour = is24Hour
            )

            if (reminder.useRandomRange) {
                CountColumn(
                    labelId = R.string.edit_count_slider_label,
                    value = reminder.notificationCount.toFloat(),
                    onValueChange = {
                        actions.onNotificationCountChange(it.toInt())
                    },
                    minvalue = 1f
                )
            }
        }

        CustomHorizontalDivider()

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
        ) {
            // SAVE Button
            CustomEditButton(
                labelId = R.string.edit_save_btn,
                onClick = {
                    actions.onSaveButtonClick()
                },
                icon = { Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = null
                ) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            if (isEditMode) {
                // DELETE Button
                CustomEditButton(
                    labelId = R.string.edit_delete_btn,
                    onClick = { showDeletionConfirmDialog = true },
                    icon = { Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    ) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                )
            } else {
                // CANCEL Button
                CustomEditButton(
                    labelId = R.string.edit_cancel_btn,
                    onClick = actions.onCancelButtonClick,
                    icon = { Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null
                    ) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }

        if (showDeletionConfirmDialog) {
            ConfirmDeletionDialog(
                onDismissRequest = {
                    showDeletionConfirmDialog = false
                },
                onConfirmation = {
                    showDeletionConfirmDialog = false
                    actions.onDeleteButtonClick()
                }
            )
        }
    }
}

@Composable
fun OptionChip(
    label: String,
    value: Boolean,
    onClick: () -> Unit,
    iconOn:  @Composable (() -> Unit)? = null,
    iconOff:  @Composable (() -> Unit)? = null
) {
    FilterChip(
        onClick = onClick,
        label = { Text(label) },
        selected = value,
        trailingIcon = if (value) iconOn else iconOff,
    )
}

@Composable
fun AlarmTypeRow(
    hasSound: Boolean,
    hasVibration: Boolean,
    onHasSoundChange: (Boolean) -> Unit,
    onHasVibrationChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LabelText(R.string.edit_label_alarm_type)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OptionChip(
                label = stringResource(R.string.edit_alarm_type_sound),
                value = hasSound,
                onClick = { onHasSoundChange(!hasSound) },
                iconOn = {
                    Icon(
                        painter = painterResource(id = R.drawable.volume_up_24px),
                        contentDescription = "sound on"
                    )
                },
                iconOff = {
                    Icon(
                        painter = painterResource(id = R.drawable.volume_mute_24px),
                        contentDescription = "sound off"
                    )
                },
            )

            OptionChip(
                label = stringResource(R.string.edit_alarm_type_vibration),
                value = hasVibration,
                onClick = { onHasVibrationChange(!hasVibration) },
                iconOn = {
                    Icon(
                        painter = painterResource(id = R.drawable.mobile_vibrate_24px),
                        contentDescription = "vibration on"
                    )
                },
                iconOff = {
                    Icon(
                        painter = painterResource(id = R.drawable.mobile_24px),
                        contentDescription = "vibration off"
                    )
                },
            )
        }
    }

}


@Composable
fun CustomHorizontalDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .padding(vertical = 6.dp)
            .graphicsLayer(alpha = 0.4f),

    )
}

@Composable
private fun ClearTrailingIcon(
    setTextAreaContent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier,
        onClick = { setTextAreaContent("") }
    ) {
        Icon(
            imageVector = Icons.Outlined.Clear,
            contentDescription = "Clear text"
        )
    }
}

@Composable
private fun SupportingText(
    value: String,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    if (value.length > 0.75f * maxLength) {
        Text(
            modifier = modifier.fillMaxWidth(),
            text = "${value.length}/${maxLength}",
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ReminderTitleField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusManager: FocusManager
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        supportingText = { SupportingText(value, ReminderEditSetting.MAX_TITLE_LENGTH) },
        singleLine = true,
        label = { Text(stringResource(R.string.edit_reminder_title), fontStyle = FontStyle.Italic) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done //ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        trailingIcon = { if (value != "") ClearTrailingIcon(onValueChange) },
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
    )
}

@Composable
fun ReminderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    focusManager: FocusManager
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        supportingText = { SupportingText(value, ReminderEditSetting.MAX_MESSAGE_LENGTH) },
        singleLine = false,
        label = { Text(stringResource(R.string.edit_reminder_text), fontStyle = FontStyle.Italic) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        trailingIcon = { if (value != "") ClearTrailingIcon(onValueChange) },
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
        interactionSource = interactionSource,
    )
}

@Composable
fun LabelText(
    labelId: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(labelId),
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun CheckRow(
    labelId: Int,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelText(labelId)

        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = checked,
            onCheckedChange = onCheckChanged,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )
    }
}

@Composable
fun TimeInputRow(
    useRange: Boolean,
    startTime: LocalTime,
    endTime: LocalTime,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    is24Hour: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelText(R.string.edit_time_label)

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (useRange) {
                false -> {
                    TimeSelectorCard(startTime, onStartTimeChange, is24Hour, Modifier)
                }
                true -> {
                    TimeSelectorCard(startTime, onStartTimeChange, is24Hour, Modifier)
                    Text(text = " - ")
                    TimeSelectorCard(endTime, onEndTimeChange, is24Hour, Modifier)
                }
            }
        }
    }
}

@Composable
fun TimeSelectorCard(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    is24Hour: Boolean,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    var showTimePicker by remember { mutableStateOf(false) }
    val dismissDialog = { showTimePicker = false }

    val state = TimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = is24Hour,
    )

    OutlinedCard(
        modifier = modifier,
        onClick = { showTimePicker = true },
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = time.format(formatter),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            title = { TimePickerDialogDefaults.Title(displayMode = TimePickerDisplayMode.Input) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(LocalTime.of( state.hour, state.minute))
                    dismissDialog()
                }) {
                    Text(stringResource(R.string.dialog_confirm_label))
                }
            },
            onDismissRequest = dismissDialog,
            dismissButton = {
                TextButton(onClick = dismissDialog) {
                    Text(stringResource(R.string.dialog_dismiss_label))
                }
            },
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    modifier = Modifier,
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,

                        periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun CountColumn(
    labelId: Int,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minvalue: Float = 1f
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(labelId)
        }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = minvalue..ReminderEditSetting.maxRandomNotificationCount.toFloat(),
            thumb = {
                Box(modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    thumbTrackGapSize = 2.dp,
                    modifier = Modifier.height(4.dp)
                )
            }
        )
    }
}

@Composable
fun DaySelectionRow(
    context: Context,
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChanged: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentToast by remember { mutableStateOf<Toast?>(null) }

    val formatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.getDefault()) }
    val getDayLabel = { day : DayOfWeek ->
        formatter.format(day)
            .replace(".", "")
            .replaceFirstChar { it.uppercase() }
    }
    val days = DayOfWeek.entries

    Column(modifier) {
        LabelText(
            R.string.edit_days_selection_label,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            MultiChoiceSegmentedButtonRow {
                days.forEachIndexed { index, day ->
                    val isSelected = selectedDays.contains(day)
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = days.size),
                        checked = isSelected,
                        onCheckedChange = {
                            val newSelection = if (isSelected) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                            onSelectedDaysChanged(newSelection)

                            if (newSelection.isEmpty()) {
                                currentToast?.cancel()
                                currentToast = Toast.makeText(
                                    context,
                                    R.string.day_selection_toast,
                                    Toast.LENGTH_SHORT
                                ).apply { show() }
                            }
                        },
                        icon = {},
                    ) {
                        Text(getDayLabel(day), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomEditButton(
    labelId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        colors = colors,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        Text(stringResource(labelId))
    }
}

@Composable
fun ConfirmDeletionDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        icon = { Icon(Icons.Outlined.Warning, contentDescription = null)},
        title = { Text(text = stringResource(R.string.delete_reminder_confirmation_title)) },
        text = { Text(text = stringResource(R.string.delete_reminder_confirmation_message)) },
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = onConfirmation) { Text(stringResource(R.string.dialog_confirm_label)) } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dialog_dismiss_label)) } }
    )
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreview() {
    StochaPopTheme {
        EditScreenContent(
            modifier = Modifier.fillMaxSize(),
            uiState = ReminderEditUIState(reminderDetails = ReminderDetails(useRandomRange = true))
        )
    }
}
