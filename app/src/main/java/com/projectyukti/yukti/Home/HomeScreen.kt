package com.projectyukti.yukti.Home

import ChatViewModel
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.projectyukti.yukti.subscription.SubscriptionCache
import com.projectyukti.yukti.subscription.SubscriptionCache.getSubscriptionDetails
import com.projectyukti.yukti.subscription.SubscriptionChecker
import com.projectyukti.yukti.subscription.SubscriptionViewModel

private val HomeBg = Color(0xFF0B1220)
private val CardBg = Color(0xFF121A2A)
private val CardBgSecondary = Color(0xFF182235)
private val BorderColor = Color(0xFF263042)
private val Accent = Color(0xFF2DD4BF)
private val AccentBlue = Color(0xFF60A5FA)
private val TextPrimary = Color(0xFFE5ECF6)
private val TextSecondary = Color(0xFF94A3B8)
private val Danger = Color(0xFFFB7185)
private val Warning = Color(0xFFF59E0B)

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current

    val inventoryResult = chatViewModel.inventoryResult.value
    val isLoading by chatViewModel.isLoading.collectAsState()

    val subscriptionViewModel = SubscriptionViewModel()
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

    LaunchedEffect(Unit) {
        chatViewModel.loadMessagesAndFetchInventory(
            getSubscriptionDetails(context).third,
            getSubscriptionDetails(context).second.toString(),
            context
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp,30.dp)
            ,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InventoryHeroCard(
            isLoading = isLoading,
            inventoryResult = inventoryResult
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Tracked",
                value = "248",
                subtitle = "items"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Low Stock",
                value = "12",
                subtitle = "needs refill",
                accentColor = Warning
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Alerts",
                value = "3",
                subtitle = "priority",
                accentColor = Danger
            )
        }

        InsightCard(
            title = "AI Insight",
            content = inventoryResult?.takeIf { it.isNotBlank() }
                ?: "No inventory insight available right now."
        )

        InsightCard(
            title = "Business Status",
            content = buildString {
                append("Business: ")
                append(getSubscriptionDetails(context).second ?: "Not connected")
                append("\n")
                append("Subscription: ")
                append(if (getSubscriptionDetails(context).first) "Active" else "Inactive")
            }
        )
    }
}

@Composable
private fun InventoryHeroCard(
    isLoading: Boolean,
    inventoryResult: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Accent.copy(alpha = 0.18f),
                            AccentBlue.copy(alpha = 0.10f),
                            CardBg
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Inventory Health",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Overview",
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Accent
                        )
                        Text(
                            text = "Analyzing inventory with AI...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = inventoryResult ?: "Your inventory is currently empty.",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color = Accent
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 4.dp)
                    .background(accentColor, RoundedCornerShape(100))
            )
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}