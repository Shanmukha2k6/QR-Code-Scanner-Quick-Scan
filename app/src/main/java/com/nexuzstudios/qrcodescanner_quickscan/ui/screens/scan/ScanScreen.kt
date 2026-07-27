package com.nexuzstudios.qrcodescanner_quickscan.ui.screens.scan

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ContentType
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanResult
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanState
import com.nexuzstudios.qrcodescanner_quickscan.ui.theme.*
import com.nexuzstudios.qrcodescanner_quickscan.utils.ClipboardUtil
import com.nexuzstudios.qrcodescanner_quickscan.utils.ShareUtil
import com.nexuzstudios.qrcodescanner_quickscan.viewmodel.ScanViewModel
import java.util.concurrent.Executors

// ─────────────────────────────────────────────────────────────────────────────
// Entry — Permission Gate
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    onScanCompleted: () -> Unit = {}
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    when {
        cameraPermission.status.isGranted ->
            ScanScreenContent(viewModel = viewModel, onScanCompleted = onScanCompleted)
        cameraPermission.status.shouldShowRationale ->
            PermissionScreen(
                icon = Icons.Outlined.CameraAlt,
                title = "Camera Access Needed",
                subtitle = "QuickScan needs your camera to scan QR codes and barcodes.",
                buttonText = "Grant Permission",
                onAction = { cameraPermission.launchPermissionRequest() }
            )
        else ->
            PermissionScreen(
                icon = Icons.Outlined.NoPhotography,
                title = "Camera Permission Denied",
                subtitle = "Enable camera access in your device Settings to use the scanner.",
                buttonText = "Request Again",
                onAction = { cameraPermission.launchPermissionRequest() }
            )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Camera Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScanScreenContent(
    viewModel: ScanViewModel,
    onScanCompleted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanState by viewModel.scanState.collectAsState()
    val isFlashOn by viewModel.isFlashOn.collectAsState()

    // Camera state
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var useBackCamera by remember { mutableStateOf(true) }

    // Gallery state
    var isGalleryProcessing by remember { mutableStateOf(false) }
    var galleryError by remember { mutableStateOf<String?>(null) }

    val hasResult = scanState is ScanState.Success

    // ── Bind/rebind camera whenever lens choice changes ───────────────────────
    // Key on both cameraProvider and useBackCamera — only run when both are ready
    LaunchedEffect(cameraProvider, useBackCamera) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val pv = previewView ?: return@LaunchedEffect
        bindCamera(
            provider = provider,
            previewView = pv,
            lifecycleOwner = lifecycleOwner,
            useBack = useBackCamera,
            onCameraReady = { cam -> camera = cam },
            onBarcode = { value, format -> viewModel.onBarcodeScanned(value, format) }
        )
        if (!useBackCamera) {
            viewModel.setFlash(false)
            camera?.cameraControl?.enableTorch(false)
        }
    }

    // ── Animations ────────────────────────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "scan")
    val scanLineY by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "line"
    )
    val cornerAlpha by inf.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "corners"
    )
    val ringScale by inf.animateFloat(
        1f, 1.042f,
        infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring"
    )
    val successGlow by animateFloatAsState(
        if (hasResult) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "glow"
    )
    val activeScale by animateFloatAsState(
        if (hasResult) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "snap"
    )
    val finalScale = if (hasResult) activeScale else activeScale * ringScale
    var flipAngle by remember { mutableStateOf(0f) }
    val animatedFlip by animateFloatAsState(
        flipAngle, tween(380, easing = FastOutSlowInEasing), label = "flip"
    )

    LaunchedEffect(scanState) {
        if (scanState is ScanState.Success) onScanCompleted()
    }
    LaunchedEffect(galleryError) {
        if (galleryError != null) {
            kotlinx.coroutines.delay(3000)
            galleryError = null
        }
    }

    // ── Gallery launcher ──────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        isGalleryProcessing = true
        galleryError = null
        try {
            val bmp = context.contentResolver.openInputStream(uri)
                ?.let { BitmapFactory.decodeStream(it) }
            if (bmp != null) {
                val img = InputImage.fromBitmap(bmp, 0)
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
                )
                scanner.process(img)
                    .addOnSuccessListener { barcodes ->
                        isGalleryProcessing = false
                        val first = barcodes.firstOrNull()
                        if (first?.rawValue != null) viewModel.onBarcodeScanned(first.rawValue!!, first.format)
                        else galleryError = "No QR code found in image"
                    }
                    .addOnFailureListener { isGalleryProcessing = false; galleryError = "Could not read image" }
            } else { isGalleryProcessing = false; galleryError = "Could not open image" }
        } catch (e: Exception) { isGalleryProcessing = false; galleryError = "Error: ${e.message}" }
    }

    Box(Modifier.fillMaxSize()) {

        // ── Camera Preview ────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                    ProcessCameraProvider.getInstance(ctx).addListener({
                        cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier
                .fillMaxSize()
                // Mirror horizontally for front camera — feels like a natural selfie
                .scale(scaleX = if (!useBackCamera) -1f else 1f, scaleY = 1f)
        )

        // ── Top subtle vignette only ────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x99000000),
                        0.18f to Color(0x00000000),
                        1f to Color(0x00000000)
                    )
                )
        )

        // ── Animated Scrim on Result ──────────────────────────────────────────
        AnimatedVisibility(
            visible = hasResult,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250))
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        }

        // ── Viewfinder ────────────────────────────────────────────────────────
        PremiumViewfinder(
            scanLineY = scanLineY,
            cornerAlpha = cornerAlpha,
            scale = finalScale,
            successGlow = successGlow,
            hasResult = hasResult
        )

        // ── Top Bar ───────────────────────────────────────────────────────────
        TopScanBar(
            isFlashOn = isFlashOn,
            showFlash = useBackCamera,
            onToggleFlash = {
                viewModel.toggleFlash()
                camera?.cameraControl?.enableTorch(!isFlashOn)
            }
        )

        // ── Bottom Controls ───────────────────────────────────────────────────
        BottomControlRail(
            isGalleryProcessing = isGalleryProcessing,
            useBackCamera = useBackCamera,
            flipAngle = animatedFlip,
            onGallery = { galleryLauncher.launch("image/*") },
            onFlipCamera = {
                viewModel.setFlash(false)
                camera?.cameraControl?.enableTorch(false)
                flipAngle += 180f
                useBackCamera = !useBackCamera
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )

        // ── Gallery processing spinner ────────────────────────────────────────
        AnimatedVisibility(
            visible = isGalleryProcessing,
            enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(52.dp))
                Text("Scanning image…", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }

        // ── Gallery error snackbar ────────────────────────────────────────────
        AnimatedVisibility(
            visible = galleryError != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 72.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ErrorRed.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(galleryError ?: "", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── Result card ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = hasResult,
            enter = slideInVertically(
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow),
                initialOffsetY = { it }
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            (scanState as? ScanState.Success)?.result?.let { result ->
                ScanResultCard(
                    result = result,
                    onCopy  = { ClipboardUtil.copyToClipboard(context, result.rawValue) },
                    onShare = { ShareUtil.shareText(context, result.rawValue) },
                    onOpen  = { if (result.contentType == ContentType.URL) ShareUtil.openUrl(context, result.rawValue) },
                    onDismiss = { viewModel.resetScan() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Camera bind helper — called on initial load AND on lens flip
// ─────────────────────────────────────────────────────────────────────────────

private fun bindCamera(
    provider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    useBack: Boolean,
    onCameraReady: (Camera) -> Unit,
    onBarcode: (String, Int) -> Unit
) {
    try {
        val selector = if (useBack) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val executor = Executors.newSingleThreadExecutor()
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        )
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also { ia ->
                ia.setAnalyzer(executor) { proxy -> processImageProxy(proxy, barcodeScanner, onBarcode) }
            }
        provider.unbindAll()
        val cam = provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
        onCameraReady(cam)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopScanBar(isFlashOn: Boolean, showFlash: Boolean, onToggleFlash: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(22.dp))
            Column {
                Text("QuickScan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
                Text("Point at a code", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
            }
        }

        // Flash — hidden on front camera
        if (showFlash) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isFlashOn) Color.White else Color.Black.copy(alpha = 0.4f))
                    .clickable { onToggleFlash() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    "Flash",
                    tint = if (isFlashOn) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Spacer(Modifier.size(44.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Control Rail — Gallery  ·  Flip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomControlRail(
    isGalleryProcessing: Boolean,
    useBackCamera: Boolean,
    flipAngle: Float,
    onGallery: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraControlButton(
                icon = Icons.Filled.FlipCameraAndroid,
                label = if (useBackCamera) "Front" else "Back",
                tint = Color.White,
                rotationDegrees = flipAngle,
                size = 54.dp,
                iconSize = 25.dp,
                onClick = onFlipCamera
            )
            CameraControlButton(
                icon = Icons.Outlined.PhotoLibrary,
                label = "Gallery",
                isLoading = isGalleryProcessing,
                tint = Color.White,
                onClick = onGallery
            )
        }
    }
}

@Composable
private fun CameraControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    rotationDegrees: Float = 0f,
    tint: Color = Color.White,
    size: androidx.compose.ui.unit.Dp = 52.dp,
    iconSize: androidx.compose.ui.unit.Dp = 23.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = tint, strokeWidth = 2.dp, modifier = Modifier.size(iconSize))
            } else {
                Icon(
                    icon, null, tint = tint,
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer { rotationZ = rotationDegrees }
                )
            }
        }
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Premium Viewfinder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumViewfinder(
    scanLineY: Float,
    cornerAlpha: Float,
    scale: Float,
    successGlow: Float,
    hasResult: Boolean
) {
    val accentColor = Color.White
    val successColor = Color(0xFF4CAF50)
    val scanLineColor = Color(0xFFFF1744)

    androidx.compose.foundation.Canvas(Modifier.fillMaxSize().scale(scale)) {
        val topSpace = 76.dp.toPx()
        val bottomSpace = 84.dp.toPx()
        val availableHeight = (size.height - topSpace - bottomSpace).coerceAtLeast(100f)
        val boxSize = minOf(size.width * 0.70f, availableHeight * 0.70f)
        val left = (size.width - boxSize) / 2
        val top  = topSpace + (availableHeight - boxSize) / 2
        val cornerLen = boxSize * 0.13f
        val strokeW = 4.dp.toPx()
        val thinStroke = 1.dp.toPx()
        val activeColor = if (hasResult) successColor else accentColor.copy(alpha = cornerAlpha)

        // ── Subtle border rectangle ──────────────────────────────────────
        drawRect(
            color = (if (hasResult) successColor else accentColor).copy(alpha = 0.08f),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
            style = androidx.compose.ui.graphics.drawscope.Stroke(thinStroke)
        )

        // ── Success glow fill ────────────────────────────────────────────
        if (successGlow > 0f) {
            drawRect(
                color = successColor.copy(alpha = 0.1f * successGlow),
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxSize, boxSize)
            )
        }

        if (!hasResult) {
            // ── Scan line with glow ──────────────────────────────────────
            val lineY = top + boxSize * scanLineY
            // Wide glow
            drawLine(
                scanLineColor.copy(0.12f),
                androidx.compose.ui.geometry.Offset(left + strokeW, lineY),
                androidx.compose.ui.geometry.Offset(left + boxSize - strokeW, lineY),
                16.dp.toPx()
            )
            // Medium glow
            drawLine(
                scanLineColor.copy(0.25f),
                androidx.compose.ui.geometry.Offset(left + strokeW, lineY),
                androidx.compose.ui.geometry.Offset(left + boxSize - strokeW, lineY),
                6.dp.toPx()
            )
            // Sharp core line
            drawLine(
                scanLineColor.copy(0.95f),
                androidx.compose.ui.geometry.Offset(left + strokeW, lineY),
                androidx.compose.ui.geometry.Offset(left + boxSize - strokeW, lineY),
                2.dp.toPx()
            )

            // ── Grid of targeting dots ───────────────────────────────────
            val rows = 8
            val cols = 8
            val spaceX = boxSize / cols
            val spaceY = boxSize / rows
            for (r in 1 until rows) {
                for (c in 1 until cols) {
                    val dotY = top + r * spaceY
                    // Dots near the scan line glow brighter
                    val distFromLine = kotlin.math.abs(dotY - lineY) / boxSize
                    val dotAlpha = if (distFromLine < 0.08f) 0.55f else 0.18f
                    drawCircle(
                        color = scanLineColor.copy(alpha = dotAlpha),
                        radius = 1.2.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(left + c * spaceX, dotY)
                    )
                }
            }
        }

        // ── Corner brackets ──────────────────────────────────────────────
        fun corner(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(activeColor, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Offset(x + dx * cornerLen, y), strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(activeColor, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Offset(x, y + dy * cornerLen), strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
        corner(left,           top,           1f,  1f )
        corner(left + boxSize, top,          -1f,  1f )
        corner(left,           top + boxSize, 1f, -1f )
        corner(left + boxSize, top + boxSize,-1f, -1f )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Result Card — consistent with History/Settings dark card style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ScanResultCard(
    result: ScanResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    val ctd = remember(result.contentType) { getContentTypeData(result.contentType) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF1E1E1E))

            .padding(bottom = 20.dp)
    ) {
        // Handle
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp, bottom = 6.dp)
                .width(38.dp).height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        )

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(ctd.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(ctd.icon, null, tint = ctd.color, modifier = Modifier.size(21.dp)) }
                Column {
                    Text(ctd.label, color = ctd.color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(result.displayFormat.ifBlank { "QR Code" }, color = Color.Gray, fontSize = 11.sp)
                }
            }
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(15.dp)) }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 20.dp))

        // Value
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                result.rawValue,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp, lineHeight = 21.sp,
                maxLines = 4, overflow = TextOverflow.Ellipsis
            )
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResultActionButton(Icons.Outlined.ContentCopy, "Copy",  AccentBlue, Modifier.weight(1f), onCopy,  filled = false)
            ResultActionButton(Icons.Outlined.Share,        "Share", AccentBlue,  Modifier.weight(1f), onShare, filled = false)
            if (result.contentType == ContentType.URL)
                ResultActionButton(Icons.Filled.OpenInBrowser, "Open", AccentBlue, Modifier.weight(1f), onOpen, filled = true)
        }

        Spacer(Modifier.height(4.dp))

        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Outlined.QrCodeScanner, null, tint = Color.Gray, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text("Scan again", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ResultActionButton(
    icon: ImageVector, label: String, color: Color,
    modifier: Modifier, onClick: () -> Unit, filled: Boolean
) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier.height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(icon, null, tint = Color.Black, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
            border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Permission Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionScreen(
    icon: ImageVector, title: String, subtitle: String,
    buttonText: String, onAction: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                Modifier.size(80.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = AccentBlue, modifier = Modifier.size(38.dp)) }
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(subtitle, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 21.sp)
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Content-type helpers
// ─────────────────────────────────────────────────────────────────────────────

private data class ContentTypeDisplay(val label: String, val icon: ImageVector, val color: Color)
private fun getContentTypeData(type: ContentType): ContentTypeDisplay = when (type) {
    ContentType.URL      -> ContentTypeDisplay("Web URL",  Icons.Filled.Language,     Color(0xFF1E88E5))
    ContentType.EMAIL    -> ContentTypeDisplay("Email",    Icons.Filled.Email,         Color(0xFFAB47BC))
    ContentType.PHONE    -> ContentTypeDisplay("Phone",    Icons.Filled.Phone,         AccentBlue)
    ContentType.SMS      -> ContentTypeDisplay("SMS",      Icons.Filled.Sms,           Color(0xFF00BCD4))
    ContentType.WIFI     -> ContentTypeDisplay("Wi-Fi",    Icons.Filled.Wifi,          Color(0xFF29B6F6))
    ContentType.CONTACT  -> ContentTypeDisplay("Contact",  Icons.Filled.Person,        Color(0xFFFF7043))
    ContentType.GEO      -> ContentTypeDisplay("Location", Icons.Filled.LocationOn,    Color(0xFFFFCA28))
    ContentType.CALENDAR -> ContentTypeDisplay("Calendar", Icons.Filled.CalendarMonth, Color(0xFFEC407A))
    else                 -> ContentTypeDisplay("QR Code",  Icons.Filled.QrCode,        AccentBlue)
}

// ─────────────────────────────────────────────────────────────────────────────
// ML Kit
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onResult: (String, Int) -> Unit
) {
    val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees).let { img ->
        barcodeScanner.process(img)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { onResult(it, barcodes.first().format) }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
