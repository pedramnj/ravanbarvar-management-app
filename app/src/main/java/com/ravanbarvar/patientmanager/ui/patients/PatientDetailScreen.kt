package com.ravanbarvar.patientmanager.ui.patients

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentStatus
import com.ravanbarvar.patientmanager.data.local.entity.DocumentEntity
import com.ravanbarvar.patientmanager.data.local.entity.Gender
import com.ravanbarvar.patientmanager.ui.currentApp
import com.ravanbarvar.patientmanager.ui.theme.LavenderSecondary
import com.ravanbarvar.patientmanager.ui.theme.SagePrimary
import com.ravanbarvar.patientmanager.ui.theme.SuccessGreen
import com.ravanbarvar.patientmanager.ui.theme.WarningAmber
import com.ravanbarvar.patientmanager.ui.theme.statusCanceled
import com.ravanbarvar.patientmanager.ui.theme.statusDone
import com.ravanbarvar.patientmanager.ui.theme.statusScheduled
import com.ravanbarvar.patientmanager.util.DocumentUtils
import com.ravanbarvar.patientmanager.util.JalaliDate
import com.ravanbarvar.patientmanager.util.formatToman
import com.ravanbarvar.patientmanager.util.minutesToPersianClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(patientId: Long, onBack: () -> Unit, onDeleted: () -> Unit) {
    val app = currentApp()
    val context = LocalContext.current
    val viewModel: PatientDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer { PatientDetailViewModel(app.patientRepository, app.appointmentRepository, patientId) }
        }
    )

    val documents by viewModel.documents.collectAsState()
    val upcoming by viewModel.upcomingAppointment.collectAsState()
    val sessionHistory by viewModel.sessionHistory.collectAsState()

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddDocMenu by remember { mutableStateOf(false) }
    var showAvatarMenu by remember { mutableStateOf(false) }
    var docPendingDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAvatarCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val avatarKey = remember { viewModel.patientId?.toString() ?: java.util.UUID.randomUUID().toString() }

    val requiredFieldError = stringResource(R.string.required_field_error)

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some providers do not support persistable permissions; the URI is still usable for this session.
            }
            val name = DocumentUtils.queryDisplayName(context, uri)
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            viewModel.addDocument(uri.toString(), name, mime)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCaptureUri
        if (success && uri != null) {
            viewModel.addDocument(uri.toString(), "عکس ${JalaliDate.today().formattedShort()}", "image/jpeg")
        }
        pendingCaptureUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val id = viewModel.patientId
        if (granted && id != null) {
            val uri = DocumentUtils.createCaptureUri(context, id)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    // Documents attach to a saved patient record. If the admin hasn't saved yet
    // (still filling in a brand-new patient), save it first — silently, using
    // whatever is already filled in — then continue with the requested action.
    fun addDocumentAction(action: () -> Unit) {
        if (viewModel.patientId != null) {
            action()
        } else {
            viewModel.save(requiredFieldError) { action() }
        }
    }

    val pickAvatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
            }
            viewModel.photoUri = uri.toString()
        }
    }

    val takeAvatarPictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingAvatarCaptureUri
        if (success && uri != null) {
            viewModel.photoUri = uri.toString()
        }
        pendingAvatarCaptureUri = null
    }

    val avatarCameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = DocumentUtils.createAvatarCaptureUri(context, avatarKey)
            pendingAvatarCaptureUri = uri
            takeAvatarPictureLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(if (viewModel.isNew) R.string.patient_detail_new_title else R.string.patient_detail_edit_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (!viewModel.isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!viewModel.isLoaded) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SagePrimary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(LavenderSecondary.copy(alpha = 0.18f))
                            .clickable { showAvatarMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.photoUri != null) {
                            AsyncImage(
                                model = viewModel.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Filled.PersonOutline,
                                contentDescription = null,
                                tint = LavenderSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SagePrimary)
                            .clickable { showAvatarMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(R.string.change_photo),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(expanded = showAvatarMenu, onDismissRequest = { showAvatarMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_from_files)) },
                            leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                            onClick = {
                                showAvatarMenu = false
                                pickAvatarLauncher.launch(arrayOf("image/*"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.take_photo)) },
                            leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                            onClick = {
                                showAvatarMenu = false
                                avatarCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!viewModel.isNew) {
                UpcomingAppointmentCard(upcoming)
                Spacer(Modifier.height(16.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = viewModel.firstName,
                    onValueChange = { viewModel.firstName = it; viewModel.onFieldsChanged() },
                    label = { Text(stringResource(R.string.patient_first_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = viewModel.lastName,
                    onValueChange = { viewModel.lastName = it; viewModel.onFieldsChanged() },
                    label = { Text(stringResource(R.string.patient_last_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = { Text(stringResource(R.string.patient_phone)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.nationalId,
                onValueChange = { viewModel.nationalId = it },
                label = { Text(stringResource(R.string.patient_national_id)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.patient_gender), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    Gender.MALE to stringResource(R.string.gender_male),
                    Gender.FEMALE to stringResource(R.string.gender_female),
                    Gender.OTHER to stringResource(R.string.gender_other)
                )
                options.forEachIndexed { index, (value, label) ->
                    SegmentedButton(
                        selected = viewModel.gender == value,
                        onClick = { viewModel.gender = value },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        colors = SegmentedButtonDefaults.colors(activeContainerColor = SagePrimary.copy(alpha = 0.16f))
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.patient_birth_date), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.birthDate?.formatted() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(stringResource(R.string.select_date)) },
                    trailingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                // A transparent overlay, rather than .clickable on the field itself: a
                // readOnly OutlinedTextField still runs its own internal touch handling
                // for focus/cursor, which competes with an outer clickable and made this
                // tap register inconsistently. The overlay intercepts the tap first.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showBirthDatePicker = true }
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text(stringResource(R.string.patient_notes)) },
                placeholder = { Text(stringResource(R.string.patient_notes_hint)) },
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(22.dp))

            if (!viewModel.isNew) {
                Text(stringResource(R.string.session_history), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                if (sessionHistory.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_session_history),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sessionHistory.forEach { appt ->
                            SessionHistoryRow(appointment = appt)
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.patient_documents), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Box {
                    TextButton(onClick = { showAddDocMenu = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_document))
                    }
                    DropdownMenu(expanded = showAddDocMenu, onDismissRequest = { showAddDocMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_from_files)) },
                            leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                            onClick = {
                                showAddDocMenu = false
                                addDocumentAction { pickFileLauncher.launch(arrayOf("image/*", "application/pdf")) }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.take_photo)) },
                            leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                            onClick = {
                                showAddDocMenu = false
                                addDocumentAction { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (documents.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_documents),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    documents.forEach { doc ->
                        DocumentRow(
                            document = doc,
                            onClick = { openDocument(context, doc) },
                            onDelete = { docPendingDelete = doc }
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            if (viewModel.saveError != null) {
                Text(
                    text = viewModel.saveError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            Button(
                onClick = { viewModel.save(requiredFieldError) {} },
                enabled = !viewModel.isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showBirthDatePicker) {
        BirthDatePickerDialog(
            initialDate = viewModel.birthDate,
            onDismiss = { showBirthDatePicker = false },
            onConfirm = {
                viewModel.birthDate = it
                showBirthDatePicker = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete_patient_title)) },
            text = { Text(stringResource(R.string.confirm_delete_patient_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deletePatient(onDeleted)
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    docPendingDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docPendingDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_document_title)) },
            text = { Text(stringResource(R.string.confirm_delete_document_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDocument(doc)
                    docPendingDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { docPendingDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun UpcomingAppointmentCard(upcoming: AppointmentWithPatientName?) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Event, contentDescription = null, tint = SagePrimary)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = stringResource(R.string.upcoming_appointment),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (upcoming == null) {
                        stringResource(R.string.no_upcoming_appointment)
                    } else {
                        "${JalaliDate.fromEpochDay(upcoming.dateEpochDay).formatted()} · ${minutesToPersianClock(upcoming.startMinutes)}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(appointment: AppointmentWithPatientName) {
    val statusColor = when (appointment.status) {
        AppointmentStatus.DONE -> statusDone
        AppointmentStatus.CANCELED -> statusCanceled
        else -> statusScheduled
    }
    val statusLabel = when (appointment.status) {
        AppointmentStatus.DONE -> stringResource(R.string.appointment_status_done)
        AppointmentStatus.CANCELED -> stringResource(R.string.appointment_status_canceled)
        else -> stringResource(R.string.appointment_status_scheduled)
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${JalaliDate.fromEpochDay(appointment.dateEpochDay).formatted()} · ${minutesToPersianClock(appointment.startMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val fee = appointment.feeAmount
            if (fee != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Paid,
                        contentDescription = null,
                        tint = if (appointment.isPaid) SuccessGreen else WarningAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatToman(fee),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (appointment.isPaid) SuccessGreen else WarningAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(document: DocumentEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LavenderSecondary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        document.mimeType.startsWith("image/") -> Icons.Filled.Image
                        document.mimeType == "application/pdf" -> Icons.Filled.PictureAsPdf
                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = LavenderSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = document.displayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun openDocument(context: android.content.Context, doc: DocumentEntity) {
    try {
        val uri = Uri.parse(doc.uri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, doc.mimeType.ifBlank { "*/*" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    } catch (_: SecurityException) {
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SagePrimary,
    focusedLabelColor = SagePrimary,
    cursorColor = SagePrimary
)
