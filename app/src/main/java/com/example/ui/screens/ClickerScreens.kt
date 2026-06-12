package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BusinessState
import com.example.data.LeaderboardEntry
import com.example.data.StockState
import com.example.ui.GameViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// EARNINGS SCREEN (SCREENSHOT 2: CLICKER)
// ==========================================
@Composable
fun EarningsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val player by viewModel.playerState.collectAsState()
    val liveBalance by viewModel.liveBalance.collectAsState()
    val isNoAds by viewModel.isNoAdsPurchased.collectAsState()
    val showShop by viewModel.showShopModal.collectAsState()

    val currentBusinesses by viewModel.businesses.collectAsState()
    val currentStocks by viewModel.stocks.collectAsState()

    // Calculate hourly income for top display
    val businessHourly = currentBusinesses.sumOf { it.currentHourlyIncome }
    val stockHourly = currentStocks.sumOf { (it.currentPrice * it.ownedCount) * (it.dividendYield / 24.0) }
    val totalHourlyIncomeRaw = businessHourly + stockHourly
    val totalHourlyIncome = if (isNoAds) totalHourlyIncomeRaw * 1.25 else totalHourlyIncomeRaw

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Upper Bar: Profit Per Hour
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Прибыль в час",
                        color = GameGrayText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${viewModel.formatDouble(totalHourlyIncome)}",
                        color = GameWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.showShopModal(true) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(GameSurface, CircleShape)
                        .border(1.dp, GameGrayText.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Бонусы",
                        tint = GameAccentPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bank Credit Card
            CreditCard(balance = liveBalance)

            Spacer(modifier = Modifier.height(16.dp))

            // Click Info Row
            player?.let { p ->
                val upgradePriceStr = viewModel.formatDoubleNoCeil(p.upgradeCost)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$${viewModel.formatDouble(if (isNoAds) p.clickIncome * 1.3 else p.clickIncome)} за нажатие",
                            color = GameWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${p.clickLevel} уровень",
                            color = GameGrayText,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.upgradeClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GameSurface,
                            contentColor = GameWhite
                        ),
                        border = BorderStroke(1.dp, GameGrayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        enabled = liveBalance >= p.upgradeCost
                    ) {
                        Text(
                            text = "$$upgradePriceStr",
                            color = if (liveBalance >= p.upgradeCost) GameWhite else GameGrayText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Promo Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // No ADS button
                Button(
                    onClick = { viewModel.toggleNoAds() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNoAds) GameAccentPurple else GameWhite,
                        contentColor = if (isNoAds) GameWhite else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (isNoAds) GameWhite.copy(alpha = 0.2f) else Color.Black.copy(
                                        alpha = 0.1f
                                    ), CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "AD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNoAds) GameWhite else Color.Black
                            )
                            // Draw a slash across AD
                            Canvas(modifier = Modifier.size(14.dp)) {
                                drawLine(
                                    color = if (isNoAds) GameWhite else Color.Black,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 2f
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isNoAds) "Без Рекламы ✔" else "No ADS",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Shop / Магазин button
                Button(
                    onClick = { viewModel.showShopModal(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameAccentBlue,
                        contentColor = GameWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Магазин",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Магазин",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Circular Cyber Fingerprint clicker graphic
            val clickAmountFormatted = player?.let { p ->
                if (isNoAds) p.clickIncome * 1.3 else p.clickIncome
            } ?: 1.0
            val clickLabelText = "+$${viewModel.formatDouble(clickAmountFormatted)}"

            FingerprintClicker(
                onClick = { viewModel.click() },
                clickLabel = clickLabelText
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Нажмите на область, чтобы заработать деньги",
                color = GameGrayText,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // Shop modal
        if (showShop) {
            ShopModalDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showShopModal(false) }
            )
        }
    }
}

@Composable
fun CreditCard(balance: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GameCardOrange,
                            GameCardRed,
                            Color(0xFFE91E63)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(20.dp)
        ) {
            // Mastercard Logo (Overlapping circles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Double Circles logo
                Box(modifier = Modifier.size(44.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFE53935).copy(alpha = 0.9f),
                            radius = 16.dp.toPx(),
                            center = Offset(14.dp.toPx(), 22.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFFB300).copy(alpha = 0.85f),
                            radius = 16.dp.toPx(),
                            center = Offset(28.dp.toPx(), 22.dp.toPx())
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "5282 3456 7890 1289",
                        color = GameWhite.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "09/28",
                        color = GameWhite.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Balance Details at bottom of card
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = "Баланс",
                    color = GameWhite.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$${String.format("%,.2f", balance).replace(",", " ")}",
                    color = GameWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

data class ClickParticle(
    val id: Long,
    val offsetX: Float,
    val offsetY: Float,
    val extraText: String = ""
)

@Composable
fun AnimatedParticle(
    particle: ClickParticle,
    onAnimationEnd: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(1.0f) }
    val yAnim = remember { Animatable(0f) }
    val xAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val duration = 550
        launch {
            scaleAnim.animateTo(
                targetValue = if (particle.extraText.isNotEmpty()) 1.3f else 0.7f,
                animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = duration, easing = FastOutLinearInEasing)
            )
        }
        launch {
            yAnim.animateTo(
                targetValue = particle.offsetY - 110f,
                animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            xAnim.animateTo(
                targetValue = particle.offsetX,
                animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
            )
        }
        delay(duration.toLong())
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .offset(x = xAnim.value.dp, y = yAnim.value.dp)
            .scale(scaleAnim.value)
            .alpha(alphaAnim.value),
        contentAlignment = Alignment.Center
    ) {
        if (particle.extraText.isNotEmpty()) {
            Text(
                text = particle.extraText,
                color = GameAccentPurple,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(GameGoldBadge, CircleShape)
                    .border(1.dp, GameWhite, CircleShape)
            )
        }
    }
}

@Composable
fun FingerprintClicker(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    clickLabel: String = "+$1.00"
) {
    var isPressed by remember { mutableStateOf(false) }
    var particleList by remember { mutableStateOf(listOf<ClickParticle>()) }
    var particleIdCounter by remember { mutableStateOf(0L) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "clickScale"
    )

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Render current animated click particles
        particleList.forEach { p ->
            key(p.id) {
                AnimatedParticle(
                    particle = p,
                    onAnimationEnd = {
                        particleList = particleList.filter { item -> item.id != p.id }
                    }
                )
            }
        }

        // Outer premium core wrapper (translates the bg-gradient-to-br from-[#49454F] to-[#1C1B1F] border-8 border-[#313033] shadow-2xl)
        Box(
            modifier = Modifier
                .size(210.dp)
                .scale(scale)
                .shadow(28.dp, CircleShape, clip = false)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF49454F),
                            Color(0xFF1C1B1F)
                        )
                    ),
                    shape = CircleShape
                )
                .border(7.dp, Color(0xFF313033), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // Custom visual feedback via scale is enough and more elegant!
                ) {
                    isPressed = true
                    onClick()

                    // Spawn particles
                    val random = java.util.Random()
                    val newParticles = mutableListOf<ClickParticle>()
                    // 1. Text flying particle
                    newParticles.add(
                        ClickParticle(
                            id = ++particleIdCounter,
                            offsetX = (random.nextFloat() - 0.5f) * 40f,
                            offsetY = -20f,
                            extraText = clickLabel
                        )
                    )
                    // 2. Flying sparks
                    for (i in 0 until 5) {
                        val angle = random.nextDouble() * 2 * Math.PI
                        val distance = 60f + random.nextFloat() * 110f
                        newParticles.add(
                            ClickParticle(
                                id = ++particleIdCounter,
                                offsetX = (Math.cos(angle) * distance).toFloat(),
                                offsetY = (Math.sin(angle) * distance).toFloat() - 20f,
                                extraText = ""
                            )
                        )
                    }
                    particleList = particleList + newParticles
                },
            contentAlignment = Alignment.Center
        ) {
            // Blur neon backing glow simulator
            Box(
                modifier = Modifier
                    .fillMaxSize(0.85f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GameAccentPurple.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Inner core circle: w-48 h-48 rounded-full bg-[#2B2930] border-4 border-[#D0BCFF] shadow-[inset_0_0_20px_rgba(208,188,255,0.2)]
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color(0xFF2B2930), CircleShape)
                    .border(3.dp, GameAccentPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Tap Core lightning indicator",
                        tint = GameAccentPurple,
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape, clip = false)
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "TAP CORE",
                        color = GameAccentPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Capture click feedback pulse and reset immediately
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(90)
            isPressed = false
        }
    }
}

// ==========================================
// BUSINESS SCREEN (SCREENSHOT 4)
// ==========================================
@Composable
fun BusinessScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val liveBalance by viewModel.liveBalance.collectAsState()
    val businesses by viewModel.businesses.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var selectedBusinessForUpgrade by remember { mutableStateOf<BusinessState?>(null) }

    val businessHourly = businesses.sumOf { it.currentHourlyIncome }
    val isNoAds by viewModel.isNoAdsPurchased.collectAsState()
    val totalHourlyIncomeRaw = businessHourly
    val totalHourlyIncome = if (isNoAds) totalHourlyIncomeRaw * 1.25 else totalHourlyIncomeRaw

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(20.dp))

            // Upper Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Бизнес",
                    color = GameWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Баланс: $${viewModel.formatDouble(liveBalance)}",
                    color = GameGrayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Stats Block
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "$${viewModel.formatDouble(totalHourlyIncome)}",
                color = GameWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Общий доход в час",
                color = GameGreenPositive,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Base and Bonus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Базовый: $${viewModel.formatDouble(businessHourly)}",
                    color = GameGrayText,
                    fontSize = 12.sp
                )
                Text(
                    text = "Бонус: $${viewModel.formatDouble(if (isNoAds) businessHourly * 0.25 else 0.0)}",
                    color = GameAccentPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two main buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* Simulated action */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GameAccentPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Слияние бизнесов", color = GameWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(
                    onClick = { /* Open selection */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GameWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Начать бизнес", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Список компаний",
                    color = GameWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Available slots
                Box(
                    modifier = Modifier
                        .border(1.dp, GameGrayText.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Свободные слоты 4/9",
                        color = GameWhite,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ad promo slot
            Card(
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable {},
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Слот за рекламу",
                    color = GameWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Business items list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(businesses) { item ->
                    BusinessRow(
                        business = item,
                        viewModel = viewModel,
                        onUpgradeClick = { selectedBusinessForUpgrade = item },
                        onAlertClick = { viewModel.collectBusinessAlert(item.id) }
                    )
                }
            }
        }

        // Upgrade overlay
        selectedBusinessForUpgrade?.let { bus ->
            BusinessUpgradeDialog(
                business = bus,
                userBalance = liveBalance,
                onDismiss = { selectedBusinessForUpgrade = null },
                onUpgrade = {
                    viewModel.buyOrUpgradeBusiness(bus.id)
                    selectedBusinessForUpgrade = null
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun BusinessRow(
    business: BusinessState,
    viewModel: GameViewModel,
    onUpgradeClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    val isLocked = business.level == 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUpgradeClick() }
            .border(
                width = if (isLocked) 1.dp else 1.5.dp,
                color = if (isLocked) Color.Transparent else GameAccentPurple.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) GameSurfaceDark.copy(alpha = 0.65f) else GameSurfaceDark
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        color = if (isLocked) GameSurface.copy(alpha = 0.5f) else GameSurface, 
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (business.iconName) {
                        "factory" -> Icons.Default.Factory
                        "crane" -> Icons.Default.PrecisionManufacturing
                        "bank" -> Icons.Default.AccountBalance
                        "delivery" -> Icons.Default.LocalShipping
                        else -> Icons.Default.Business
                    },
                    contentDescription = business.name,
                    tint = if (isLocked) GameWhite.copy(alpha = 0.4f) else GameWhite,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = business.subtitle,
                        color = GameGrayText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Level capsule badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isLocked) Color(0xFF37363F) else GameAccentPurple.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLocked) "0 ур" else "${business.level} ур",
                            color = if (isLocked) GameGrayText else GameAccentPurple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Text(
                    text = business.name,
                    color = if (isLocked) GameWhite.copy(alpha = 0.6f) else GameWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (!isLocked) "+$${viewModel.formatDouble(business.currentHourlyIncome)} / час" else "Начать за $${viewModel.formatDouble(business.baseUpgradeCost)}",
                    color = if (!isLocked) GameGreenPositive else GameAccentBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exclamation Warning Icon
            if (business.isWarningActive && business.level > 0) {
                IconButton(
                    onClick = onAlertClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error, // warning exclamation in circle
                        contentDescription = "Предупреждение",
                        tint = GameGoldBadge,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Lock or Action Icon representation
            IconButton(
                onClick = onUpgradeClick,
                modifier = Modifier
                    .size(34.dp)
                    .background(if (isLocked) Color(0xFF2B2A33) else GameAccentPurple, CircleShape),
                colors = IconButtonDefaults.iconButtonColors(contentColor = GameWhite)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.ArrowForward,
                    contentDescription = if (isLocked) "Заблокировано" else "Прокачать",
                    tint = if (isLocked) GameGrayText else GameWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==========================================
// INVESTMENTS MAIN SCREEN (SCREENSHOT 1)
// ==========================================
@Composable
fun InvestmentsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val liveBalance by viewModel.liveBalance.collectAsState()
    val currentInvestTab by viewModel.currentInvestTab.collectAsState()
    val showStocksMarket by viewModel.showStockMarket.collectAsState()
    val stocks by viewModel.stocks.collectAsState()

    // Calculate details
    val ownedStocksValue = stocks.sumOf { it.currentPrice * it.ownedCount }

    if (showStocksMarket) {
        StockMarketScreen(viewModel = viewModel)
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(GameBackground)
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(20.dp))

                // Upper Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Инвестиции",
                        color = GameWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Баланс: $${viewModel.formatDouble(liveBalance)}",
                        color = GameGrayText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontal Tab selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InvestmentPill(
                        label = "Акции",
                        isSelected = currentInvestTab == 0,
                        onClick = { viewModel.setCurrentInvestTab(0) }
                    )
                    InvestmentPill(
                        label = "Крипта",
                        isSelected = currentInvestTab == 1,
                        onClick = { viewModel.setCurrentInvestTab(1) }
                    )
                    InvestmentPill(
                        label = "Недвижимость",
                        isSelected = currentInvestTab == 2,
                        onClick = { viewModel.setCurrentInvestTab(2) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentInvestTab == 0) {
                    // Stocks Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        colors = CardDefaults.cardColors(containerColor = GameSurfaceDark),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Мой портфель акций",
                                color = GameWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Стоимость портфеля акций",
                                color = GameGrayText,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "$${viewModel.formatDoubleNoCeil(ownedStocksValue)}",
                                color = GameWhite,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )

                            // Profit tag
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "2,95M $ (16,03) % ",
                                    color = GameGreenPositive,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "за всё время",
                                    color = GameGrayText,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Расчетный доход в час",
                                color = GameGrayText,
                                fontSize = 11.sp
                            )
                            val dividendHourly = stocks.sumOf { (it.currentPrice * it.ownedCount) * (it.dividendYield / 24.0) }
                            Text(
                                text = "$${viewModel.formatDouble(dividendHourly)}",
                                color = GameWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Inner Category Cards Grid (Мои акции, Рынок акций)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Pill Card: My Stocks
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(125.dp)
                                .clickable { viewModel.showMyStocksDetails(true) },
                            colors = CardDefaults.cardColors(containerColor = GameWhite),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Мои акции",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Мои акции",
                                        color = Color.Black,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Изучите свои акции",
                                        color = Color.Black.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Right Pill Card: Stock market
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(125.dp)
                                .clickable { viewModel.showStockMarket(true) },
                            colors = CardDefaults.cardColors(containerColor = GameAccentPurple),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = "Рынок акций",
                                    tint = GameWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Рынок акций",
                                        color = GameWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Изучите фондовый рынок",
                                        color = GameWhite.copy(alpha = 0.75f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (currentInvestTab == 1) {
                    PlaceholderInvestContent(title = "Криптовалюта", desc = "Торговля биткоином и альткоинами в разработке. Будет добавлено в будущих обновлениях.")
                } else {
                    PlaceholderInvestContent(title = "Недвижимость", desc = "Покупка доходных коммерческих зданий будет открыта на следующем уровне.")
                }
            }

            val showDetails by viewModel.showMyStocksDetails.collectAsState()
            if (showDetails && currentInvestTab == 0) {
                MyStocksDetailsDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.showMyStocksDetails(false) }
                )
            }
        }
    }
}

@Composable
fun InvestmentPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) GameWhite else Color.Transparent)
            .border(1.dp, if (isSelected) GameWhite else GameGrayText.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else GameGrayText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PlaceholderInvestContent(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Заблокировано",
            tint = GameGrayText,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, color = GameWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = desc,
            color = GameGrayText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ==========================================
// STOCK MARKET SUB-SCREEN (SCREENSHOT 3)
// ==========================================
@Composable
fun StockMarketScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val liveBalance by viewModel.liveBalance.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val sortOption by viewModel.stockSortOption.collectAsState()
    var selectedStockForTrade by remember { mutableStateOf<StockState?>(null) }

    val sortedStocks = when (sortOption) {
        1 -> stocks.sortedByDescending { it.dividendYield }
        2 -> stocks.sortedBy { it.dividendYield }
        else -> stocks
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(20.dp))

            // Upper Header-Back Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.showStockMarket(false) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = GameWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Рынок акций",
                    color = GameWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Баланс: $${viewModel.formatDouble(liveBalance)}",
                    color = GameGrayText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sort Pills Row (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = "Все акции",
                    selected = sortOption == 0,
                    onClick = { viewModel.setStockSortOption(0) }
                )
                FilterChip(
                    label = "Наибольшие дивиденды",
                    selected = sortOption == 1,
                    onClick = { viewModel.setStockSortOption(1) }
                )
                FilterChip(
                    label = "Наименьшие дивиденды",
                    selected = sortOption == 2,
                    onClick = { viewModel.setStockSortOption(2) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stocks List view
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedStocks) { stock ->
                    StockMarketItem(
                        stock = stock,
                        viewModel = viewModel,
                        onClick = { selectedStockForTrade = stock }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Trade Modal
        selectedStockForTrade?.let { stk ->
            StockTradeDialog(
                stock = stk,
                userBalance = liveBalance,
                onDismiss = { selectedStockForTrade = null },
                onBuy = { qty -> viewModel.buyStock(stk.symbol, qty) },
                onSell = { qty -> viewModel.sellStock(stk.symbol, qty) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) GameAccentPurple.copy(alpha = 0.3f) else GameSurfaceDark)
            .border(1.dp, if (selected) GameAccentPurple else GameGrayText.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) GameWhite else GameGrayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StockMarketItem(
    stock: StockState,
    viewModel: GameViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = GameSurfaceDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Logo
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GameSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.symbol.take(2).uppercase(),
                    color = if (stock.isUp) GameGreenPositive else GameRedNegative,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.name,
                    color = GameWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Доступно • ${stock.ownedCount} шт",
                    color = GameGrayText,
                    fontSize = 12.sp
                )
            }

            // Real Sparkline trend chart drawn using Canvas!
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 28.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val points = stock.priceHistory
                    if (points.size >= 2) {
                        val min = points.minOrNull() ?: 0f
                        val max = points.maxOrNull() ?: 100f
                        val range = max - min
                        val dx = size.width / (points.size - 1)
                        val path = Path()

                        points.forEachIndexed { idx, price ->
                            val cy = size.height - ((price - min) / (if (range == 0f) 1f else range)) * size.height
                            if (idx == 0) {
                                path.moveTo(0f, cy)
                            } else {
                                path.lineTo(idx * dx, cy)
                            }
                        }

                        drawPath(
                            path = path,
                            color = if (stock.isUp) GameGreenPositive else GameRedNegative,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Price tag
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$ ${stock.currentPrice}",
                    color = GameWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (stock.isUp) "+" else ""}${stock.percentageChange}% ${if (stock.isUp) "▲" else "▼"}",
                    color = if (stock.isUp) GameGreenPositive else GameRedNegative,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// LEADERBOARD SCREEN (EXCLUSIVE TAB)
// ==========================================
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val leaderboard by viewModel.leaderboard.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Рейтинг олигархов",
            color = GameWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Лучшие инвесторы и кликеры в реальном времени",
            color = GameGrayText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Leaderboard Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Игрок", color = GameGrayText, fontSize = 12.sp)
            Text("Капитал", color = GameGrayText, fontSize = 12.sp)
        }

        Divider(color = GameGrayText.copy(alpha = 0.2f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(leaderboard) { entry ->
                LeaderboardRow(entry = entry, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    viewModel: GameViewModel
) {
    val backgroundColor = if (entry.isPlayer) GameAccentPurple.copy(alpha = 0.2f) else GameSurfaceDark
    val borderColor = if (entry.isPlayer) GameAccentPurple else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        when (entry.rank) {
                            1 -> GameGoldBadge
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> GameSurface
                        }, CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entry.rank}",
                    color = if (entry.rank in 1..3) Color.Black else GameWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(android.graphics.Color.parseColor(entry.avatarColorHex)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name.first().toString().uppercase(),
                    color = GameWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name
            Text(
                text = entry.name,
                color = GameWhite,
                fontSize = 15.sp,
                fontWeight = if (entry.isPlayer) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Balance Value
            Text(
                text = "$ ${viewModel.formatDoubleNoCeil(entry.balance)}",
                color = if (entry.isPlayer) GameWhite else GameGrayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// ACCOUNT / PROFILE SCREEN
// ==========================================
@Composable
fun AccountScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val player by viewModel.playerState.collectAsState()
    val liveBalance by viewModel.liveBalance.collectAsState()
    val isNoAds by viewModel.isNoAdsPurchased.collectAsState()

    val currentBusinesses by viewModel.businesses.collectAsState()
    val currentStocks by viewModel.stocks.collectAsState()

    val businessHourly = currentBusinesses.sumOf { it.currentHourlyIncome }
    val stockHourly = currentStocks.sumOf { (it.currentPrice * it.ownedCount) * (it.dividendYield / 24.0) }
    val totalHourlyIncomeRaw = businessHourly + stockHourly
    val totalHourlyIncome = if (isNoAds) totalHourlyIncomeRaw * 1.25 else totalHourlyIncomeRaw

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GameBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Профиль инвестора",
            color = GameWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GameSurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(GameAccentPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Я",
                        color = GameWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Магнат Финансов",
                    color = GameWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Капитал: $${viewModel.formatDouble(liveBalance)}",
                    color = GameGrayText,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Statistics List
        Text(
            text = "Игровая Статистика",
            color = GameWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        StatRow(label = "Баланс лицевого счета", value = "$${viewModel.formatDouble(liveBalance)}")
        StatRow(label = "Доход в час с акций", value = "$${viewModel.formatDouble(stockHourly)}")
        StatRow(label = "Доход в час с бизнеса", value = "$${viewModel.formatDouble(businessHourly)}")
        StatRow(label = "Итоговый доход в час", value = "$${viewModel.formatDouble(totalHourlyIncome)}")
        StatRow(label = "Уровень клика", value = "${player?.clickLevel ?: 1}")
        StatRow(label = "Сила клика", value = "$${viewModel.formatDouble(player?.clickIncome ?: 1.0)}")
        StatRow(label = "Всего произведено кликов", value = "${player?.totalClicks ?: 0}")

        Spacer(modifier = Modifier.height(24.dp))

        // Reset / Restart Button for prestige
        Button(
            onClick = { /* Simulated prestige resets */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GameRedNegative)
        ) {
            Text(
                "ПОВЫСИТЬ ПРЕСТИЖ (В РАЗРАБОТКЕ)",
                fontWeight = FontWeight.Bold,
                color = GameWhite
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = GameGrayText, fontSize = 14.sp)
        Text(text = value, color = GameWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
    Divider(color = GameGrayText.copy(alpha = 0.1f))
}

// ==========================================
// POPUPS / MODALS
// ==========================================

@Composable
fun MyStocksDetailsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()
    val ownedStocks = stocks.filter { it.ownedCount > 0 }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GameSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Мой Капитал в Акциях",
                    color = GameWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (ownedStocks.isEmpty()) {
                    Text(
                        text = "Вы пока не владеете акциями. Посетите раздел 'Рынок акций', чтобы купить ценные бумаги.",
                        color = GameGrayText,
                        fontSize = 14.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ownedStocks) { stock ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stock.name, color = GameWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${stock.ownedCount} шт. по $${stock.currentPrice}", color = GameGrayText, fontSize = 12.sp)
                                }
                                Text(
                                    text = "$${viewModel.formatDouble(stock.ownedCount * stock.currentPrice)}",
                                    color = GameGreenPositive,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Divider(color = GameGrayText.copy(alpha = 0.1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GameAccentPurple)
                ) {
                    Text("Закрыть", color = GameWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StockTradeDialog(
    stock: StockState,
    userBalance: Double,
    onDismiss: () -> Unit,
    onBuy: (Int) -> Unit,
    onSell: (Int) -> Unit,
    viewModel: GameViewModel
) {
    var quantity by remember { mutableStateOf(1) }
    val totalCost = stock.currentPrice * quantity

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GameSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Сделка: ${stock.name}",
                    color = GameWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Текущая цена: $${stock.currentPrice}",
                    color = GameGrayText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.background(GameSurfaceDark, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Минус", tint = GameWhite)
                    }

                    Text(
                        text = "$quantity шт.",
                        color = GameWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.background(GameSurfaceDark, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Плюс", tint = GameWhite)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Итоговая цена:", color = GameGrayText, fontSize = 14.sp)
                    Text("$${viewModel.formatDouble(totalCost)}", color = GameWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Trade action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (stock.ownedCount >= quantity) {
                                onSell(quantity)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GameRedNegative),
                        enabled = stock.ownedCount >= quantity,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Продать ${stock.ownedCount}", color = GameWhite, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (userBalance >= totalCost) {
                                onBuy(quantity)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GameGreenPositive),
                        enabled = userBalance >= totalCost,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Купить", color = GameWhite, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, GameGrayText),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Отмена", color = GameWhite)
                }
            }
        }
    }
}

@Composable
fun BusinessUpgradeDialog(
    business: BusinessState,
    userBalance: Double,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    viewModel: GameViewModel
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GameSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (business.level > 0) "Прокачать ${business.name}" else "Инвестировать в ${business.name}",
                    color = GameWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Текущий уровень: ${business.level}",
                    color = GameGrayText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Увеличение дохода: + $${viewModel.formatDouble(business.baseIncome)} в час",
                    color = GameGreenPositive,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Стоимость:", color = GameGrayText, fontSize = 14.sp)
                    Text("$${viewModel.formatDouble(business.currentUpgradeCost)}", color = GameWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GameAccentPurple),
                    enabled = userBalance >= business.currentUpgradeCost,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (business.level > 0) "Улучшить" else "Начать бизнес",
                        color = GameWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, GameGrayText),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Отмена", color = GameWhite)
                }
            }
        }
    }
}

@Composable
fun ShopModalDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val balance by viewModel.liveBalance.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = GameSurface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Магазин Усилений",
                    color = GameWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                ShopItemRow(
                    title = "Удвоить силу клика",
                    price = 500000.0,
                    description = "Навсегда умножает силу клика на x2",
                    isPurchased = false,
                    onBuy = {
                        coroutineScope.launch {
                            // Deduct balance and apply
                            onDismiss()
                        }
                    },
                    userBalance = balance,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(10.dp))

                ShopItemRow(
                    title = "Супер Менеджеры",
                    price = 1200000.0,
                    description = "+35% к пассивному доходу бизнеса",
                    isPurchased = false,
                    onBuy = {
                        onDismiss()
                    },
                    userBalance = balance,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GameAccentBlue)
                ) {
                    Text("ОК", color = GameWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShopItemRow(
    title: String,
    price: Double,
    description: String,
    isPurchased: Boolean,
    onBuy: () -> Unit,
    userBalance: Double,
    viewModel: GameViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GameSurfaceDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = GameWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, color = GameGrayText, fontSize = 11.sp)
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPurchased) Color.Gray else GameAccentPurple,
                    contentColor = GameWhite
                ),
                enabled = !isPurchased && userBalance >= price,
                modifier = Modifier.height(34.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (isPurchased) "Куплено" else "$${viewModel.formatDoubleNoCeil(price)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
