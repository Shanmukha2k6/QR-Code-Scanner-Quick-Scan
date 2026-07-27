@file:OptIn(ExperimentalLayoutApi::class)

package com.nexuzstudios.qrcodescanner_quickscan.ui.screens.create

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.QRType
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.WifiCredentials
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ContactInfo
import com.nexuzstudios.qrcodescanner_quickscan.ui.theme.*
import com.nexuzstudios.qrcodescanner_quickscan.utils.ShareUtil
import com.nexuzstudios.qrcodescanner_quickscan.viewmodel.CreateViewModel
import com.nexuzstudios.qrcodescanner_quickscan.viewmodel.GenerateState
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(viewModel: CreateViewModel = hiltViewModel()) {
    val selectedType by viewModel.selectedQRType.collectAsState()
    val generateState by viewModel.generateState.collectAsState()
    val savedMessage by viewModel.savedMessage.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create QR", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Type Selector ──────────────────────────────────────────────
            QRTypeSelectorTabs(selectedType = selectedType, onTypeSelected = viewModel::setQRType)

            Spacer(Modifier.height(16.dp))

            // ── Input Form ─────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    QRInputForm(
                        qrType = selectedType,
                        onGenerate = { content, title -> viewModel.generateQR(content, title) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Generated QR Preview ───────────────────────────────────────
            AnimatedVisibility(
                visible = generateState is GenerateState.Success || generateState is GenerateState.Loading,
                enter = expandVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (val state = generateState) {
                            is GenerateState.Loading -> {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                            is GenerateState.Success -> {
                                // Success badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle, null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Generated Successfully",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // QR Image
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .background(Color.White)
                                        .padding(20.dp)
                                ) {
                                    Image(
                                        bitmap = state.bitmap.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.size(200.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            saveBitmapToGallery(context, state.bitmap)
                                            viewModel.saveToHistory(state.content, selectedType.name)
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Save", fontWeight = FontWeight.SemiBold)
                                    }
                                    Button(
                                        onClick = { ShareUtil.shareText(context, state.content) },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Outlined.Share, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Share", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // ── Snackbar ───────────────────────────────────────────────────
            savedMessage?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            msg,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

private fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "QR_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRScanner")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        val stream: OutputStream? = context.contentResolver.openOutputStream(it)
        stream?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QRTypeSelectorTabs(selectedType: QRType, onTypeSelected: (QRType) -> Unit) {
    val qrTypes = listOf(
        QRType.TEXT to "Text",
        QRType.URL to "URL",
        QRType.WIFI to "WiFi",
        QRType.CONTACT to "Contact",
        QRType.EMAIL to "Email",
        QRType.PHONE to "Phone",
        QRType.SMS to "SMS"
    )
    val barcodeTypes = listOf(
        QRType.BARCODE_CODE128 to "Code128",
        QRType.BARCODE_EAN13 to "EAN-13",
        QRType.BARCODE_EAN8 to "EAN-8"
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "QR Codes",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            qrTypes.forEach { (type, label) ->
                TypeChip(label = label, selected = selectedType == type, onClick = { onTypeSelected(type) })
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Barcodes",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            barcodeTypes.forEach { (type, label) ->
                TypeChip(label = label, selected = selectedType == type, onClick = { onTypeSelected(type) })
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        selected = selected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = Color.Transparent,
            borderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRInputForm(qrType: QRType, onGenerate: (String, String) -> Unit) {
    var text by remember(qrType) { mutableStateOf("") }
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var smsNumber by remember { mutableStateOf("") }
    var smsMessage by remember { mutableStateOf("") }

    fun buildContent(): Pair<String, String> = when (qrType) {
        QRType.TEXT -> Pair(text, "Text")
        QRType.URL -> Pair(if (text.startsWith("http")) text else "https://$text", "URL")
        QRType.WIFI -> Pair(WifiCredentials(ssid, password).toWifiString(), ssid)
        QRType.CONTACT -> Pair(ContactInfo(firstName, lastName, phone, email).toVCard(), "$firstName $lastName")
        QRType.EMAIL -> Pair("mailto:$email?subject=${emailSubject}&body=${emailBody}", email)
        QRType.PHONE -> Pair("tel:$phone", phone)
        QRType.SMS -> Pair("smsto:$smsNumber:$smsMessage", smsNumber)
        QRType.BARCODE_CODE128 -> Pair(text, "Barcode")
        QRType.BARCODE_EAN13 -> Pair(text, "EAN-13")
        QRType.BARCODE_EAN8 -> Pair(text, "EAN-8")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (qrType) {
            QRType.TEXT, QRType.URL, QRType.BARCODE_CODE128, QRType.BARCODE_EAN13, QRType.BARCODE_EAN8 -> {
                val label = when (qrType) {
                    QRType.URL -> "Website URL"
                    QRType.BARCODE_EAN13 -> "13-digit number"
                    QRType.BARCODE_EAN8 -> "8-digit number"
                    QRType.BARCODE_CODE128 -> "Barcode content"
                    else -> "Enter text"
                }
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = if (qrType in listOf(QRType.BARCODE_EAN13, QRType.BARCODE_EAN8))
                        KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
                )
            }
            QRType.WIFI -> {
                OutlinedTextField(value = ssid, onValueChange = { ssid = it },
                    label = { Text("Network Name (SSID)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = password, onValueChange = { password = it },
                    label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp))
            }
            QRType.CONTACT -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it },
                        label = { Text("First Name") }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it },
                        label = { Text("Last Name") }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp))
                }
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            }
            QRType.EMAIL -> {
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = emailSubject, onValueChange = { emailSubject = it },
                    label = { Text("Subject (optional)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = emailBody, onValueChange = { emailBody = it },
                    label = { Text("Message (optional)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), maxLines = 3)
            }
            QRType.PHONE -> {
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            }
            QRType.SMS -> {
                OutlinedTextField(value = smsNumber, onValueChange = { smsNumber = it },
                    label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = smsMessage, onValueChange = { smsMessage = it },
                    label = { Text("Message (optional)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp))
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                val (content, title) = buildContent()
                onGenerate(content, title)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Filled.QrCode2, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Generate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
