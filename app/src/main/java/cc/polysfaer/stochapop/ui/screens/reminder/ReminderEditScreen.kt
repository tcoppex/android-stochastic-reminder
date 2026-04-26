@file:OptIn(ExperimentalMaterial3Api::class)

package cc.polysfaer.stochapop.ui.screens.reminder


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.IntentCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.controller.NotificationChannels.hasPostNotificationPermission
import cc.polysfaer.stochapop.controller.sendNotification
import cc.polysfaer.stochapop.data.reminder.ReminderSettings
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

    val onNotificationSoundChange: (Uri?) ->Unit = {}
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

            onNotificationSoundChange = viewModel::setSoundUri,

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

    // TODO: use one custom scaffold per screen
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {  },
//                actions = {
//                    if (isEditMode) {
//                        IconButton(onClick = { showDeletionConfirmDialog = true }) {
//                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                }
//            )
//        },


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
            .padding(horizontal = 14.dp)
            .padding(top = 14.dp)
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitledEditSectionCard(
            title = "Notification",
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

            AlarmTypeRow(
                hasSound = reminder.hasSound,
                hasVibration = reminder.hasVibration,
                onHasSoundChange = actions.onToggleSound,
                onHasVibrationChange = actions.onToggleVibration,
            )

            SoundSelectRow(
                reminder.soundUri,
                actions.onNotificationSoundChange,
                enabled = reminder.hasSound
            )

            OutlinedButton(
                onClick = {
                    if (hasPostNotificationPermission(context)) {
                        sendNotification(context, reminder.toReminder(), false)
                    } else {
                        Toast.makeText(
                            context,
                            R.string.require_postnotification_toast,
                            Toast.LENGTH_SHORT
                        ).apply { show() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(8.dp)
            ) {
                Icon(Icons.Outlined.PlayArrow, null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.edit_test_btn))
            }
        }

        TitledEditSectionCard(
            title = "Schedule",
        ) {
            TimeModeSegmentedButton(
                reminder.useRandomRange,
                actions.onToggleRandomRange,
                modifier = Modifier.fillMaxWidth()
            )

            TimeInputRow(
                useRange = reminder.useRandomRange,
                startTime = reminder.startTime,
                endTime = reminder.endTime,
                onStartTimeChange = actions.onStartTimeChange,
                onEndTimeChange = actions.onEndTimeChange,
                is24Hour = is24Hour
            )

            NotificationCountSlider(
                labelId = R.string.edit_count_slider_label,
                value = reminder.notificationCount.toFloat(),
                onValueChange = {
                    actions.onNotificationCountChange(it.toInt())
                },
                enabled = reminder.useRandomRange,
                minvalue = 1f,
            )

            DaySelectionRow(
                context = context,
                selectedDays = reminder.selectedDays,
                onSelectedDaysChanged = actions.onSelectedDaysChanged,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

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
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
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

// ------------------------------------------------------------------------------------------------

@Composable
fun EditSectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 12.dp,
            bottomStart = 12.dp,
            bottomEnd = 12.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun TitledEditSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    content: @Composable ColumnScope.() -> Unit
) {
    val titleHeight = 24.dp
    val overlapOffset = titleHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = overlapOffset)
    ) {
        EditSectionCard(
            modifier = Modifier,
            containerColor = containerColor,
            content = content
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
//                .padding(start = 2.dp)
                .height(titleHeight)
                .offset(y = -overlapOffset),
            color = containerColor,
            contentColor = titleContentColor,
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 14.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = title.uppercase(),
//                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------

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
        supportingText = { SupportingText(value, ReminderSettings.MAX_TITLE_LENGTH) },
        singleLine = true,
        label = { Text(stringResource(R.string.edit_reminder_title), fontStyle = FontStyle.Italic) },
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
        supportingText = { SupportingText(value, ReminderSettings.MAX_MESSAGE_LENGTH) },
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

// ------------------------------------------------------------------------------------------------

@Composable
fun LabelText(
    labelId: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(labelId),
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}


@Composable
fun OptionChip(
    label: String,
    value: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconOn:  @Composable (() -> Unit)? = null,
    iconOff:  @Composable (() -> Unit)? = null
) {
    FilterChip(
        modifier = modifier,
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

//@Composable
//fun CheckRow(
//    labelId: Int,
//    checked: Boolean,
//    onCheckChanged: (Boolean) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        LabelText(labelId)
//
//        Switch(
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentWidth(Alignment.End),
//            checked = checked,
//            onCheckedChange = onCheckChanged,
//            thumbContent = if (checked) {
//                {
//                    Icon(
//                        imageVector = Icons.Filled.Check,
//                        contentDescription = null,
//                        modifier = Modifier.size(SwitchDefaults.IconSize),
//                    )
//                }
//            } else {
//                null
//            }
//        )
//    }
//}

// ------------------------------------------------------------------------------------------------

@Composable
fun TimeModeSegmentedButton(
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember {
        mutableIntStateOf(if (checked) 1 else 0)
    }
    val options = listOf(
        stringResource(R.string.time_mode_fixed_label),
        stringResource(R.string.time_mode_random_label)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LabelText(R.string.time_mode_label)

        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = {
                        selectedIndex = index
                        onCheckChanged(selectedIndex == 1)
                    },
                    selected = index == selectedIndex,
                    label = { Text(label, fontSize = 12.sp) },
                    icon = {},
                )
            }
        }
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
fun NotificationCountSlider(
    labelId: Int,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    minvalue: Float = 1f
) {
    Column(modifier = modifier.graphicsLayer(alpha = if (enabled) 1.0f else 0.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelText(labelId)
        }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            value = value,
            onValueChange = onValueChange,
            valueRange = minvalue..ReminderSettings.maxRandomNotificationCount.toFloat(),
            thumb = {
                Box(modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    colors = SliderColors(
                        activeTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeTickColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),

                        thumbColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledThumbColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledActiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledActiveTickColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledInactiveTickColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
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

// ------------------------------------------------------------------------------------------------


@Composable
fun SoundSelectRow(
    currentSoundUri: Uri?,
    onSoundSelected: (Uri?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {

    // XXX (TEST)
    // Can be incorrect if the user has defined SILENT as the defaut ringtone

//    if (currentSoundUri == null) {
//        val defaultUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        onSoundSelected(defaultUri)
//    }

    //---------

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.let { intent ->
                IntentCompat.getParcelableExtra(
                    intent,
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            }
            onSoundSelected(uri)
        }
    }

    val ringtoneSelectionTitle = stringResource(R.string.ringtone_selection_title)

    val intent = remember(currentSoundUri) {
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, ringtoneSelectionTitle)

            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)

            // XXX TODO?
            // the silent ringtone is considered to be at null, same value used
            // to detect the default ringtone.
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true) //

            val uriToSelect = currentSoundUri ?: Settings.System.DEFAULT_NOTIFICATION_URI
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uriToSelect)
        }
    }

    val ringtone = RingtoneManager.getRingtone(context, currentSoundUri)
    val ringtoneName = ringtone.getTitle(context)

    // Note: we only authorize system sound to be accessed, if we need user defined sound we'll need
    // the READ_EXTERNAL_STORAGE permission and be grant a persistable URI via takePersistableUriPermission
    // to use it after reboot.

    //---------

    Row(
        modifier = modifier.fillMaxWidth().graphicsLayer(alpha = if (enabled) 1.0f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelText(
            R.string.edit_alarm_custom_sound,
            modifier = Modifier
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            onClick = { launcher.launch(intent) },
            shape = MaterialTheme.shapes.extraSmall,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            enabled = enabled
        ) {
            Text(
                text = ringtoneName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}


// ------------------------------------------------------------------------------------------------

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
