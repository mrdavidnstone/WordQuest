package com.spellarcade.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spellarcade.app.ui.components.avatar.SpriteOption
import com.spellarcade.app.ui.components.avatar.SpriteRepository
import com.spellarcade.app.ui.screens.profile.character.AvatarSpriteRenderer
import com.spellarcade.app.ui.screens.profile.character.CharacterRenderer
import com.spellarcade.app.ui.screens.profile.character.CharacterState
import com.spellarcade.app.ui.screens.profile.character.CharacterViewModel
import com.spellarcade.app.util.MusicManager

enum class CustomizerCategory(val title: String, val emoji: String) {
    BODY("Body", "👤"),
    HAIR("Hair", "💇"),
    HATS("Hats", "🧢"),
    TOPS("Tops", "🧥"),
    BOTTOMS("Bottoms", "👖"),
    SHOES("Shoes", "👟")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarBuilderScreen(
    onBack: () -> Unit,
    viewModel: CharacterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val characterState by viewModel.characterState.collectAsState()
    val layerVisibility by viewModel.layerVisibility.collectAsState()
    var selectedCategory by remember { mutableStateOf(CustomizerCategory.BODY) }

    LaunchedEffect(Unit) {
        MusicManager.playMusic(context, "music/hero_build.wav")
    }

    Scaffold(
        containerColor = Color(0xFF031024),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF031024))) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text("BUILD YOUR HERO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Spacer(Modifier.size(48.dp))
                }
                // Blue star accent line under title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(60.dp).height(1.dp).background(Color(0xFF00B0FF).copy(alpha = 0.5f)))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF00B0FF), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.width(60.dp).height(1.dp).background(Color(0xFF00B0FF).copy(alpha = 0.5f)))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. HERO PREVIEW AREA (Stationary fixed height — NEVER shifts or resizes)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Blue Frame
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF00B0FF), CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1565C0),
                                    Color(0xFF0B2B50),
                                    Color(0xFF031024)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CharacterRenderer(
                        character = characterState,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        debugMode = false,
                        layerVisibility = layerVisibility
                    )
                }
            }

            // 2. CATEGORY TABS (Single Row with 6 Equal-Width Buttons — No Scrolling Needed)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0C2040)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomizerCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF0080FF) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(category.emoji, fontSize = 18.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    category.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. STATIONARY COLOR SWATCH BAR
            Surface(
                modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0C2040)
            ) {
                StationaryColorBar(
                    category = selectedCategory,
                    characterState = characterState,
                    onUpdate = { viewModel.updateCharacter(it) }
                )
            }

            // 4. 3-COLUMN SELECTION GRID
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                ThreeColumnSpriteGrid(
                    category = selectedCategory,
                    characterState = characterState,
                    onUpdate = { viewModel.updateCharacter(it) }
                )
            }
        }
    }
}

@Composable
private fun StationaryColorBar(
    category: CustomizerCategory,
    characterState: CharacterState,
    onUpdate: ((CharacterState) -> CharacterState) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
        when (category) {
            CustomizerCategory.BODY -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GENDER", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00B0FF), letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val isBoy = characterState.gender.equals("Boy", ignoreCase = true)
                        FilterChip(
                            selected = isBoy,
                            onClick = { onUpdate { it.copy(gender = "Boy") } },
                            label = { Text("👦 Boy", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E88E5),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0D234A),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        FilterChip(
                            selected = !isBoy,
                            onClick = { onUpdate { it.copy(gender = "Girl") } },
                            label = { Text("👧 Girl", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E88E5),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0D234A),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
            CustomizerCategory.HAIR -> {
                Text("HAIR COLOR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00B0FF), letterSpacing = 1.sp)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                val hairColors = listOf(
                    "brown" to "#6D4C41",
                    "black" to "#111111",
                    "blonde" to "#FBC02D",
                    "red" to "#E53935",
                    "purple" to "#7E57C2",
                    "gray" to "#546E7A"
                )
                ColorSwatchRow(colors = hairColors, selectedId = characterState.hairColor) { color ->
                    onUpdate { it.copy(hairColor = color) }
                }
            }
            CustomizerCategory.TOPS -> {
                Text("TOP COLOR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00B0FF), letterSpacing = 1.sp)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                val topColors = listOf("gray" to "#546E7A", "blue" to "#1E88E5", "red" to "#E53935", "green" to "#43A047", "purple" to "#7E57C2")
                ColorSwatchRow(colors = topColors, selectedId = characterState.topColor) { color ->
                    val cleanStyle = when {
                        characterState.top.contains("hoodie") -> "hoodie"
                        characterState.top.contains("shirt") -> "tshirt"
                        characterState.top.contains("jacket") -> "jacket"
                        characterState.top.contains("long") -> "longsleeve"
                        characterState.top.contains("sweater") -> "sweater"
                        else -> characterState.top
                    }
                    onUpdate { it.copy(topColor = color, top = cleanStyle) }
                }
            }
            CustomizerCategory.BOTTOMS -> {
                Text("BOTTOM COLOR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00B0FF), letterSpacing = 1.sp)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                val bottomColors = listOf("khaki" to "#8D6E63", "gray" to "#546E7A", "blue" to "#1A237E", "green" to "#2E7D32", "purple" to "#4527A0")
                ColorSwatchRow(colors = bottomColors, selectedId = characterState.bottomColor) { color ->
                    val cleanStyle = when {
                        characterState.bottom.contains("cargo") -> "cargo_pants"
                        characterState.bottom.contains("jeans") -> "jeans"
                        characterState.bottom.contains("shorts") -> "shorts"
                        characterState.bottom.contains("joggers") -> "joggers"
                        else -> characterState.bottom
                    }
                    onUpdate { it.copy(bottomColor = color, bottom = cleanStyle) }
                }
            }
            else -> {
                Text("EQUIP ITEM", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF00B0FF), letterSpacing = 1.sp)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                Text("Select an item below to equip", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun ColorSwatchRow(
    colors: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(colors, key = { it.first }) { (id, hex) ->
            val isSelected = id == selectedId
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(hex)))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(id) }
                    .then(
                        if (isSelected) Modifier.border(2.5.dp, Color(0xFF00B0FF), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreeColumnSpriteGrid(
    category: CustomizerCategory,
    characterState: CharacterState,
    onUpdate: ((CharacterState) -> CharacterState) -> Unit
) {
    val context = LocalContext.current

    when (category) {
        CustomizerCategory.BODY -> {
            val scanned = remember(context, characterState.gender) {
                SpriteRepository.scanDirectory(context, "sprites/base_body", characterState.gender)
            }
            val options = scanned.ifEmpty {
                listOf(
                    SpriteOption("body_01.png", "Body 01", "sprites/base_body/body_01.png"),
                    SpriteOption("body_02.png", "Body 02", "sprites/base_body/body_02.png")
                )
            }
            Mockup3ColumnGrid(items = options, selectedId = characterState.skin) { option ->
                onUpdate { it.copy(skin = option.id.removeSuffix(".png")) }
            }
        }
        CustomizerCategory.HAIR -> {
            val scanned = remember(context, characterState.gender) {
                SpriteRepository.scanDirectory(context, "sprites/hair", characterState.gender)
            }
            val options = scanned.ifEmpty {
                listOf(
                    SpriteOption("spiky_brown.png", "Spiky", "sprites/hair/spiky_brown.png"),
                    SpriteOption("spiky.png", "Messy", "sprites/hair/spiky.png"),
                    SpriteOption("side_part.png", "Side Swept", "sprites/hair/side_part.png"),
                    SpriteOption("curly.png", "Curly", "sprites/hair/curly.png"),
                    SpriteOption("undercut.png", "Straight", "sprites/hair/undercut.png")
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(options, key = { it.fullAssetPath }) { option ->
                    val hairClean = option.id.removeSuffix(".png")
                    val isSelected = hairClean == characterState.hair || option.id == characterState.hair

                    HairOptionPortraitCard(
                        option = option,
                        characterState = characterState,
                        isSelected = isSelected,
                        onSelect = {
                            onUpdate { it.copy(hair = hairClean, hat = "none") }
                        }
                    )
                }
            }
        }
        CustomizerCategory.HATS -> {
            val scanned = remember(context, characterState.gender) {
                SpriteRepository.scanDirectory(context, "sprites/hats", characterState.gender)
            }
            val baseOptions = scanned.ifEmpty {
                listOf(
                    SpriteOption("cap.png", "Cap", "sprites/hats/cap.png"),
                    SpriteOption("cowboy.png", "Cowboy", "sprites/hats/cowboy.png"),
                    SpriteOption("crown.png", "Crown", "sprites/hats/crown.png"),
                    SpriteOption("bucket.png", "Bucket", "sprites/hats/bucket.png")
                )
            }
            val options = listOf(SpriteOption("none", "None", "sprites/hats/none")) + baseOptions.filter { it.id != "none" }

            Mockup3ColumnGrid(items = options, selectedId = characterState.hat) { option ->
                val hatClean = option.id.removeSuffix(".png")
                onUpdate { it.copy(hat = hatClean) }
            }
        }
        CustomizerCategory.TOPS -> {
            val scanned = remember(context, characterState.topColor, characterState.gender) {
                SpriteRepository.scanDirectory(context, "sprites/top/${characterState.topColor}", characterState.gender)
            }
            val options = scanned.ifEmpty {
                listOf(
                    SpriteOption("top_hoodie_gray.png", "Hoodie Gray", "sprites/top/${characterState.topColor}/top_hoodie_gray.png"),
                    SpriteOption("hoodie.png", "Hoodie", "sprites/top/${characterState.topColor}/hoodie.png")
                )
            }
            Mockup3ColumnGrid(items = options, selectedId = characterState.top) { option ->
                val rawClean = option.id.removeSuffix(".png")
                val topClean = if (rawClean.contains("shirt", ignoreCase = true)) "tshirt" else rawClean
                onUpdate { it.copy(top = topClean) }
            }
        }
        CustomizerCategory.BOTTOMS -> {
            val scanned = remember(context, characterState.bottomColor, characterState.gender) {
                SpriteRepository.scanDirectory(context, "sprites/bottom/${characterState.bottomColor}", characterState.gender)
            }
            val options = scanned.ifEmpty {
                listOf(
                    SpriteOption("cargo_pants.png", "Cargo Pants", "sprites/bottom/${characterState.bottomColor}/cargo_pants.png"),
                    SpriteOption("bottom_cargo_khaki.png", "Cargo Khaki", "sprites/bottom/${characterState.bottomColor}/bottom_cargo_khaki.png")
                )
            }
            Mockup3ColumnGrid(items = options, selectedId = characterState.bottom) { option ->
                val bottomClean = option.id.removeSuffix(".png")
                onUpdate { it.copy(bottom = bottomClean) }
            }
        }
        CustomizerCategory.SHOES -> {
            val scanned = remember(context, characterState.gender) {
                SpriteRepository.scanDirectory(context, "sprites/shoes", characterState.gender)
            }
            val options = scanned.ifEmpty {
                listOf(
                    SpriteOption("shoes_blue.png", "Blue Shoes", "sprites/shoes/shoes_blue.png"),
                    SpriteOption("sneakers.png", "Sneakers", "sprites/shoes/sneakers.png"),
                    SpriteOption("shoes_01.png", "Shoes 01", "sprites/shoes/shoes_01.png")
                )
            }
            Mockup3ColumnGrid(items = options, selectedId = characterState.shoes) { option ->
                val shoesClean = option.id.removeSuffix(".png")
                onUpdate { it.copy(shoes = shoesClean) }
            }
        }
    }
}

@Composable
private fun HairOptionPortraitCard(
    option: SpriteOption,
    characterState: CharacterState,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val hairClean = option.id.removeSuffix(".png")
    val previewCharacter = remember(characterState, hairClean) {
        characterState.copy(hair = hairClean, hat = "none")
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F264A))
            .border(BorderStroke(2.dp, if (isSelected) Color(0xFF00B0FF) else Color.Transparent), RoundedCornerShape(16.dp))
            .pointerInput(option.fullAssetPath) {
                detectTapGestures(onTap = { onSelect() })
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Head & Shoulders Zoomed Portrait Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds(),
                contentAlignment = Alignment.TopCenter
            ) {
                AvatarSpriteRenderer(
                    character = previewCharacter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = 2.4f
                            scaleY = 2.4f
                            translationY = 68.dp.toPx()
                        }
                )
            }

            // Label Bar at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF081830)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hair (${option.displayName})",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(18.dp)
                    .background(Color(0xFF00B0FF), CircleShape)
            )
        }
    }
}

@Composable
private fun Mockup3ColumnGrid(
    items: List<SpriteOption>,
    selectedId: String,
    onSelect: (SpriteOption) -> Unit
) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.fullAssetPath }) { option ->
            val cleanId = option.id.removeSuffix(".png")
            val isSelected = cleanId == selectedId || option.id == selectedId
            val thumbnailBitmap = remember(option.fullAssetPath) {
                SpriteRepository.loadCroppedItemBitmap(context, option.fullAssetPath)
            }

            Box(
                modifier = Modifier
                    .aspectRatio(0.85f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F264A))
                    .border(BorderStroke(2.dp, if (isSelected) Color(0xFF00B0FF) else Color.Transparent), RoundedCornerShape(16.dp))
                    .pointerInput(option.fullAssetPath) {
                        detectTapGestures(
                            onTap = { onSelect(option) }
                        )
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = thumbnailBitmap,
                            contentDescription = option.displayName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFF081830)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.displayName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(18.dp)
                            .background(Color(0xFF00B0FF), CircleShape)
                    )
                }
            }
        }
    }
}
