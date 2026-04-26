package cc.polysfaer.stochapop.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.polysfaer.stochapop.R
import cc.polysfaer.stochapop.controller.NotificationChannels.hasPostNotificationPermission
import cc.polysfaer.stochapop.controller.openAppSettings
import cc.polysfaer.stochapop.data.DataSource
import cc.polysfaer.stochapop.data.reminder.Reminder
import cc.polysfaer.stochapop.ui.AppViewModelProvider
import cc.polysfaer.stochapop.ui.navigation.NavigationDestination
import cc.polysfaer.stochapop.ui.screens.ElasticScrollWrapper
import cc.polysfaer.stochapop.ui.screens.ElasticSlideItem
import cc.polysfaer.stochapop.ui.screens.SwipeBox
import cc.polysfaer.stochapop.ui.theme.StochaPopTheme
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@Composable
fun HomeScreen(
    navigateToNewReminder: () -> Unit,
    navigateToEditReminder: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val homeUiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        reminderList = homeUiState.reminderList,
        onAddReminder = { navigateToNewReminder() },
        onEditReminder = { navigateToEditReminder(it.id) },
        onToggleReminder = { viewModel.toggleReminder(it) },
        onDeleteReminder = { viewModel.deleteReminder(it) }
    )
}

@Composable
fun HomeScreenContent(
    reminderList: List<Reminder>,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        ElasticScrollWrapper { childModifier ->
            ReminderList(
                reminderList = reminderList,
                onEditReminder = onEditReminder,
                onToggleReminder = onToggleReminder,
                onDeleteReminder = onDeleteReminder,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            AddReminderWithPermissionButton(
                onClick = onAddReminder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------

@Composable
fun ReminderList(
    reminderList: List<Reminder>, //
    modifier: Modifier = Modifier,
    onEditReminder: (Reminder) -> Unit = {},
    onToggleReminder: (Reminder) -> Unit = {},
    onDeleteReminder: (Reminder) -> Unit = {},
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.getDefault()) }
    val getDayFirstLetter = { day : DayOfWeek ->
        formatter.format(day)
            .first()
            .uppercase()
    }

    LazyColumn(
        modifier = modifier.padding(bottom = 12.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        userScrollEnabled = true
    ) {
        items(items = reminderList, key = { it.id }) { reminder ->
            SwipeBox(
                onSwipLeft = { onDeleteReminder(reminder) },
            ) {
                ElasticSlideItem {
                    ReminderCard(
                        reminder = reminder,
                        onClick = { onEditReminder(reminder) },
                        onDoubleClick = { onToggleReminder(reminder) },
                        getDayFirstLetter = getDayFirstLetter,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.graphicsLayer(alpha=0.25f),
            )
        }
    }
}

@Composable
fun CardInfoText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontStyle = FontStyle.Italic,
        modifier = modifier
    )
}

@Composable
fun AlarmIcon(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id),
        contentDescription = contentDescription,
        modifier = modifier.size(14.dp)
    )
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    getDayFirstLetter: (DayOfWeek) -> String = { "⦁" },
) {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleClick,
                onLongClick = onLongClick,
            )
        ,
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .graphicsLayer(alpha = if (reminder.enabled) 1f else 0.33f),
        ) {
            if (!reminder.title.isEmpty()) {
                Text(
                    text = reminder.title,
                    modifier = Modifier.padding(bottom = 3.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                )
            }

            // -----------------------

            Row(
                modifier = Modifier.graphicsLayer(alpha = 0.4f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!reminder.selectedDays.isEmpty()) {
                    Row(Modifier
                        .border(
                            width = 0.25.dp,
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            shape = CircleShape,
                        )
                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                    ) {
                        DayOfWeek.entries.forEachIndexed { index, day ->
                            val alpha = if (reminder.selectedDays.contains(day)) 1.0f else 0.25f
                            CardInfoText(
                                text = getDayFirstLetter(day),
                                modifier = Modifier.graphicsLayer(alpha = alpha)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                }

                CardInfoText(reminder.startTime.format(formatter))
                if (reminder.useRandomRange) {
                    CardInfoText(" - ${reminder.endTime.format(formatter)}")
                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                    CardInfoText("x${reminder.notificationCount}")
                    Icon(
                        painter = painterResource(id = R.drawable.casino_24px),
                        contentDescription = "dice icon",
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                if (!reminder.hasSound && !reminder.hasVibration) {
                    AlarmIcon(R.drawable.volume_off_24px, "no alarm")
                } else {
                    if (reminder.hasSound) {
                        AlarmIcon(R.drawable.volume_up_24px, "sound alarm")
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    }
                    if (reminder.hasVibration) {
                        AlarmIcon(R.drawable.mobile_vibrate_24px, "vibration alarm")
                    }
                }

            }

            // -----------------------

            Text(
                text = reminder.message,
                modifier = Modifier
                    .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------

@Composable
fun NotificationRationaleDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    AlertDialog(
        icon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
        title = { Text(text = stringResource(R.string.permission_required_title)) },
        text = { Text(text = stringResource(R.string.permission_required_message)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmRequest) {
                Text(stringResource(R.string.dialog_confirm_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_dismiss_label))
            }
        }
    )
}

@Composable
fun AddReminderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(22.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(stringResource(R.string.home_btn_add))
    }
}


fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}


@Composable
fun AddReminderWithPermissionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    // Should we show a dialog when the permission has not been granted
    var showRationaleDialog by remember { mutableStateOf(false) }
    var haveClicked by remember { mutableStateOf(false) }

    // Check if we have the POST_NOTIFICATION permission.
    var hasNotificationPermission by remember {
        mutableStateOf(hasPostNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (haveClicked) {
            if (isGranted) {
                onClick()
            } else {
                showRationaleDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Wrap the original onClick to handle the permission request.
    val onClickWrapper = {
        val isTiramisuPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        haveClicked = true

        when {
            hasNotificationPermission || !isTiramisuPlus -> {
                onClick()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                showRationaleDialog = true
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Box(modifier = modifier) {
        AddReminderButton(
            onClick = onClickWrapper,
            modifier = Modifier.fillMaxWidth()
        )
        if (showRationaleDialog) {
            NotificationRationaleDialog(
                {
                    showRationaleDialog = false
                    haveClicked = false
                },
                {
                    openAppSettings(context)
                    showRationaleDialog = false
                    haveClicked = false
                }
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    StochaPopTheme {
        HomeScreenContent(
            reminderList = DataSource.getReminders(context),
            onAddReminder = {},
            onEditReminder = {},
            onToggleReminder = {},
            onDeleteReminder = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
