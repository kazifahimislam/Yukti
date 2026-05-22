package com.projectyukti.yukti.Insights

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InsightBg = Color(0xFF0B1220)
private val InsightCard = Color(0xFF121A2A)
private val InsightCardSoft = Color(0xFF182235)
private val InsightAccent = Color(0xFF8B5CF6)
private val InsightBlue = Color(0xFF60A5FA)
private val InsightGreen = Color(0xFF34D399)
private val InsightText = Color(0xFFE5ECF6)
private val InsightMuted = Color(0xFF94A3B8)
private val InsightWarning = Color(0xFFF59E0B)

@Composable
fun Insights(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InsightBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InsightsHeroCard()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniInsightCard(
                modifier = Modifier.weight(1f),
                title = "Signals",
                value = "24",
                accent = InsightAccent
            )
            MiniInsightCard(
                modifier = Modifier.weight(1f),
                title = "At Risk",
                value = "05",
                accent = InsightWarning
            )
            MiniInsightCard(
                modifier = Modifier.weight(1f),
                title = "Healthy",
                value = "19",
                accent = InsightGreen
            )
        }

        DetailInsightCard(
            title = "Top Observations",
            content = listOf(
                "Fast-moving items are concentrated in a few categories.",
                "A small set of products is creating most restock pressure.",
                "Demand signals suggest selective replenishment is needed."
            )
        )

        DetailInsightCard(
            title = "Recommended Actions",
            content = listOf(
                "Restock the highest demand products first.",
                "Review slow-moving inventory before adding new stock.",
                "Use AI summaries to compare trends every day."
            )
        )
    }
}

@Composable
private fun InsightsHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            InsightAccent.copy(alpha = 0.20f),
                            InsightBlue.copy(alpha = 0.12f),
                            InsightCard
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "AI Insights",
                    color = InsightMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Understand what needs attention",
                    color = InsightText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Get high-level business patterns, stock pressure signals, and smart recommendations generated from your inventory activity.",
                    color = InsightText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun MiniInsightCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCardSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 4.dp)
                    .background(accent, RoundedCornerShape(50))
            )
            Text(
                text = title,
                color = InsightMuted,
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = InsightText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailInsightCard(
    title: String,
    content: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = InsightMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            content.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "•",
                        color = InsightAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item,
                        color = InsightText,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}