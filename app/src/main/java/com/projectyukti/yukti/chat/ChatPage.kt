package com.projectyukti.yukti.chat

import ChatViewModel
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdfScanner
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.projectyukti.yukti.MainActivity
import com.projectyukti.yukti.chat.components.ChatHeader
import com.projectyukti.yukti.chat.components.menu.NavDrawerItems
import com.projectyukti.yukti.navigation.Routes
import com.projectyukti.yukti.navigation.isKeyboardOpen
import com.projectyukti.yukti.permission.CameraPermission
import com.projectyukti.yukti.permission.MicrophonePermission
import com.projectyukti.yukti.permission.RequestNotificationPermission
import com.projectyukti.yukti.sign_in.GoogleAuthUiClient
import com.projectyukti.yukti.subscription.SubscriptionCache
import com.projectyukti.yukti.subscription.SubscriptionCache.clearSubscriptionDetails
import com.projectyukti.yukti.subscription.SubscriptionCache.getSubscriptionDetails
import com.projectyukti.yukti.subscription.SubscriptionChecker
import com.projectyukti.yukti.subscription.SubscriptionViewModel
import com.projectyukti.yukti.texttospeach.TTSHelper
import com.projectyukti.yukti.ui.theme.ColorModelMessage
import com.projectyukti.yukti.ui.theme.ColorUserMessage
import com.google.firebase.auth.FirebaseAuth
import com.projectyukti.yukti.createbusiness.ExportChatData
import geminiImagePrompt
import getChatDateLabel
import getChatTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ── Drawer color tokens ──────────────────────────────────
private val DrawerBg          = Color(0xFF0D1117)
private val DrawerSurface     = Color(0xFF161B22)
private val DrawerCard        = Color(0xFF1C2433)
private val DrawerBorder      = Color(0xFF30363D)
private val DrawerTeal        = Color(0xFF2DD4BF)
private val DrawerTealDim     = Color(0xFF14B8A6)
private val DrawerTealGlow    = Color(0x1A2DD4BF)
private val DrawerBlueSky     = Color(0xFF0EA5E9)
private val DrawerTextPrimary = Color(0xFFE6EDF3)
private val DrawerTextMuted   = Color(0xFF8B949E)
private val DrawerTextFaint   = Color(0xFF484F58)
private val DrawerScrim       = Color(0xCC000000)
// ─────────────────────────────────────────────────────────

@Composable
fun ChatPage(
    chatViewModel: ChatViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    var isKeyboardVisible by remember { mutableStateOf(false) }
    isKeyboardVisible = isKeyboardOpen()

    val defaultPadding = PaddingValues(bottom = 0.dp)
    val innerKeyboardPadding = if (isKeyboardVisible) defaultPadding else innerPadding

    val subscriptionViewModel = SubscriptionViewModel()
    RequestNotificationPermission()
    val context = LocalContext.current

    val isSubscribed by subscriptionViewModel.isSubscribed.collectAsState()
    var businessId by remember { mutableStateOf("") }
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val subscriptionChecker = SubscriptionChecker(context)

    LaunchedEffect(Unit) {
        val (isSubscribed, businessName, businessId) = subscriptionChecker.checkSubscription()
        SubscriptionCache.isSubscribed = isSubscribed
        subscriptionViewModel.setSubscriptionStatus(isSubscribed)
        SubscriptionCache.businessName = businessName
        subscriptionViewModel.setBusinessName(businessName.toString())
        SubscriptionCache.businessId = businessId
        subscriptionViewModel.setBusinessId(businessId.toString())
    }

    val sharedViewModel: SharedViewModel = viewModel()

    // ── Custom drawer state (replaces ModalNavigationDrawer) ──
    var drawerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val errorState by chatViewModel.errorState.collectAsState()
    val chatId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_chat"
    val onSignOut = rememberCoroutineScope()
    val ttsHelper = remember { TTSHelper(context) }

    val signOutAction = {
        onSignOut.launch {
            try {
                googleAuthUiClient.signOut()
                Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
                clearSubscriptionDetails(context)
            } catch (e: Exception) {
                Toast.makeText(context, "Sign out failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(chatId) {
        chatViewModel.onChatScreenOpened(
            chatId,
            getSubscriptionDetails(context).third,
            getSubscriptionDetails(context).second.toString()
        )
    }

    LaunchedEffect(errorState) {
        errorState?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            chatViewModel._errorState.value = null
        }
    }

    DisposableEffect(context) {
        onDispose { ttsHelper.shutdown() }
    }

    val navItems = remember(getSubscriptionDetails(context)) {
        if (getSubscriptionDetails(context).first) {
            listOf(
                NavDrawerItems(
                    getSubscriptionDetails(context).second.toString(),
                    getSubscriptionDetails(context).second.toString(),
                    "Go to Manage business page",
                    icon = Icons.Default.Business
                ),
                NavDrawerItems("Business Members", "Business Members", "View Member List", icon = Icons.Default.AccountCircle),
                NavDrawerItems("Generate a bill", "Generate a bill", "Generate a bill", icon = Icons.Default.AdfScanner),
            )
        } else if (!getSubscriptionDetails(context).first && getSubscriptionDetails(context).second != null) {
            listOf(
                NavDrawerItems(
                    getSubscriptionDetails(context).second.toString(),
                    getSubscriptionDetails(context).second.toString(),
                    "Go to Manage business page",
                    icon = Icons.Default.Business
                ),
                NavDrawerItems("Business Members", "Business Members", "View Member List", icon = Icons.Default.AccountCircle),
                NavDrawerItems("Export Chat data", "Export Chat data", "Export Chat data", icon = Icons.Default.AdfScanner),
            )
        } else {
            listOf(
                NavDrawerItems("Create a Business", "Create a Business", "Go to Create a Business page", icon = Icons.Default.Create),
                NavDrawerItems("Join a Business", "Join a Business", "Go to Join a Business page", icon = Icons.Default.AddCircle)
            )
        }
    }

    // ── Root Box: stacks drawer ON TOP of content ──────────
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Main content ────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            ChatHeader(
                onSignOut = signOutAction,
                navItems = navItems,
                onNavigationIconClick = { drawerOpen = !drawerOpen }
            )
            Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                Column(modifier = Modifier.padding(innerKeyboardPadding)) {
                    MessageList(
                        modifier = Modifier.weight(1f),
                        messageList = chatViewModel.messageList
                    )
                    MessageInput(
                        onMessageSend = {
                            chatViewModel.sendMessage(
                                chatId, it,
                                getSubscriptionDetails(context).third,
                                getSubscriptionDetails(context).second.toString(),
                                context
                            )
                        },
                        context,
                        getSubscriptionDetails(context).third.toString(),
                        getSubscriptionDetails(context).second.toString(),
                        currentUserUid, chatViewModel, chatId
                    )
                }
            }
        }

        // ── Premium animated drawer overlay ─────────────────
        PremiumDrawer(
            isOpen = drawerOpen,
            navItems = navItems,
            currentUser = FirebaseAuth.getInstance().currentUser,
            businessName = getSubscriptionDetails(context).second?.toString() ?: "",
            isSubscribed = getSubscriptionDetails(context).first,
            onClose = { drawerOpen = false },
            onSignOut = {
                drawerOpen = false
                signOutAction()
            },
            onItemClick = { item ->
                drawerOpen = false
                when (item.title) {
                    "Generate a bill" -> ExportChatData().exportChatData(context, chatViewModel.messageList.toString())
                    "Business Members" -> navController.navigate(Routes.businessMembers) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                    "Create a Business" -> navController.navigate(Routes.subscriptionPage) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                    "Join a Business" -> navController.navigate(Routes.joinBusiness)
                    else -> Toast.makeText(context, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────
//  PREMIUM DRAWER COMPOSABLE
// ─────────────────────────────────────────────────────────

@Composable
fun PremiumDrawer(
    isOpen: Boolean,
    navItems: List<NavDrawerItems>,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    businessName: String,
    isSubscribed: Boolean,
    onClose: () -> Unit,
    onSignOut: () -> Unit,
    onItemClick: (NavDrawerItems) -> Unit
) {
    // Scrim alpha animation
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(320, easing = EaseInOut),
        label = "scrimAlpha"
    )

    // Drawer slide offset animation
    val drawerOffset by animateDpAsState(
        targetValue = if (isOpen) 0.dp else (-320).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "drawerOffset"
    )

    if (scrimAlpha > 0f || isOpen) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Scrim ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(scrimAlpha)
                    .background(DrawerScrim)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )

            // ── Drawer panel ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .offset(x = drawerOffset)
                    .background(DrawerBg)
            ) {
                // Subtle ambient glow behind panel
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-40).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(DrawerTealGlow, Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                        .blur(48.dp)
                )

                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Header ─────────────────────────────
                    DrawerPremiumHeader(
                        currentUser = currentUser,
                        businessName = businessName,
                        isSubscribed = isSubscribed,
                        onClose = onClose
                    )

                    HorizontalDivider(color = DrawerBorder, thickness = 1.dp)

                    // ── Nav Items ──────────────────────────
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    ) {
                        navItems.forEachIndexed { index, item ->
                            // Staggered entrance per item
                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(isOpen) {
                                if (isOpen) {
                                    delay((60 + index * 60).toLong())
                                    itemVisible = true
                                } else {
                                    itemVisible = false
                                }
                            }

                            AnimatedVisibility(
                                visible = itemVisible,
                                enter = fadeIn(tween(240)) + slideInHorizontally(
                                    initialOffsetX = { -40 },
                                    animationSpec = tween(280, easing = EaseOut)
                                ),
                                exit = fadeOut(tween(120))
                            ) {
                                DrawerNavItem(
                                    item = item,
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = DrawerBorder, thickness = 1.dp)

                    // ── Sign Out ───────────────────────────
                    DrawerSignOutButton(onSignOut = onSignOut)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  DRAWER HEADER
// ─────────────────────────────────────────────────────────

@Composable
fun DrawerPremiumHeader(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    businessName: String,
    isSubscribed: Boolean,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()

            .background(
                Brush.verticalGradient(
                    colors = listOf(DrawerSurface, DrawerBg)
                )
            )
            .padding(20.dp)
    ) {
        // Close button top-right
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(0.dp,20.dp,0.dp,0.dp)
                .align(Alignment.TopEnd)
                .size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close drawer",
                tint = DrawerTextMuted,
                modifier = Modifier.size(18.dp)

            )
        }

        Column(modifier = Modifier.padding(top = 4.dp)) {
            // Avatar circle with gradient + initials
            Box(
                modifier = Modifier
                    .padding(0.dp,20.dp,0.dp,0.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DrawerTealDim, DrawerBlueSky),
                            start = Offset(0f, 0f),
                            end = Offset(56f, 56f)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.displayName?.firstOrNull()?.uppercase() ?: "Y",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = currentUser?.displayName ?: "Yukti User",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DrawerTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = currentUser?.email ?: "",
                fontSize = 12.sp,
                color = DrawerTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (businessName.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DrawerCard)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSubscribed) DrawerTeal else DrawerTextMuted)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = businessName,
                        fontSize = 11.sp,
                        color = if (isSubscribed) DrawerTeal else DrawerTextMuted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
//  DRAWER NAV ITEM
// ─────────────────────────────────────────────────────────

@Composable
fun DrawerNavItem(
    item: NavDrawerItems,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(120),
        label = "itemBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "itemScale"
    )



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(DrawerCard.copy(alpha = bgAlpha))
            .clickable(
                indication = ripple(color = DrawerTeal),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container with teal tint
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DrawerTealGlow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = DrawerTeal,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DrawerTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.contentDescription.isNotBlank()) {
                Text(
                    text = item.contentDescription,
                    fontSize = 11.sp,
                    color = DrawerTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = DrawerTextFaint,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────
//  SIGN OUT BUTTON
// ─────────────────────────────────────────────────────────

@Composable
fun DrawerSignOutButton(onSignOut: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = ripple(color = Color(0xFFFF6B6B)),
                interactionSource = remember { MutableInteractionSource() }
            ) { onSignOut() }
            .padding(horizontal = 26.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "Sign out",
            tint = Color(0xFFFF6B6B),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Sign out",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFF6B6B)
        )
    }
}


// ─────────────────────────────────────────────────────────
//  REST OF YOUR ORIGINAL COMPOSABLES — UNCHANGED
// ─────────────────────────────────────────────────────────

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true
    ) {
        var lastDateLabel: String? = null
        items(messageList.reversed()) { message ->
            val currentDateLabel = getChatDateLabel(message.timestamp)
            MessaageRow(messageModel = message)
            if (currentDateLabel != lastDateLabel) {
                DateHeader(date = currentDateLabel)
                lastDateLabel = currentDateLabel
            }
        }
    }
}

@Composable
fun MessaageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                    )
                    .clip(RoundedCornerShape(38f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(10.dp, 5.dp)
            ) {
                Text(text = messageModel.message, color = Color.White)
                Text(
                    text = getChatTime(messageModel.timestamp),
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = date, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun MessageInput(
    onMessageSend: (String) -> Unit,
    context: Context,
    businessId: String,
    businessName: String,
    currentUserUid: String,
    chatViewModel: ChatViewModel,
    chatId: String
) {
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showGeminiPrompt by remember { mutableStateOf(false) }

    if (showGeminiPrompt && photoBitmap != null) {
        geminiImagePrompt(photoBitmap!!, chatViewModel, businessId, businessName, chatId)
        showGeminiPrompt = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoBitmap = result.data?.extras?.get("data") as? Bitmap
            showGeminiPrompt = true
            Toast.makeText(context, "Photo captured successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera action canceled", Toast.LENGTH_SHORT).show()
        }
    }

    var message by remember { mutableStateOf("") }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                message = spokenText
                onMessageSend(message)
                message = ""
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .imePadding()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            label = { Text("Type a message") },
            shape = RoundedCornerShape(20.dp),
            leadingIcon = {
                IconButton(onClick = {
                    if (CameraPermission().checkAndRequestPermission(context as Activity)) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        try {
                            cameraLauncher.launch(cameraIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error starting camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
                    }
                }) { }
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera")
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (MicrophonePermission().checkAndRequestPermission(context as Activity)) {
                        if (SpeechRecognizer.isRecognitionAvailable(context)) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, "Speech Recognition not available", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Microphone")
                }
            }
        )
        IconButton(onClick = {
            if (message.isNotBlank()) {
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message")
        }
    }
}
