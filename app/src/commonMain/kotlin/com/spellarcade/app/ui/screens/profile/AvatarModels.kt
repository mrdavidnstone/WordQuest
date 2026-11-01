package com.spellarcade.app.ui.screens.profile

data class Avatar(
    val gender: String = "Boy", // Boy, Girl
    val skinColor: String = "#FFD180",
    val faceShape: String = "Round", // Round, Oval, Square, Narrow, Heart
    val eyeStyle: String = "Round", // Round, Large, Almond, Narrow, Happy
    val eyeColor: String = "#6D4C41", // Brown, Blue, Green, Hazel, Gray, Dark Brown
    val eyebrowsStyle: String = "Natural", // Natural, Thick, Thin, Curved, Straight
    val noseStyle: String = "Small", // Small, Button, Defined, Triangular
    val mouthStyle: String = "Smile", // Smile, Big Smile, Small Smile, Neutral, Surprised
    val hairStyle: String = "Messy",
    val hairColor: String = "#6D4C41",
    val outfit: String = "Everyday", // Everyday, Explorer, Knight, Wizard, Archer, School, Sports, Scientist
    val outfitColor: String = "#1E88E5",
    val bottomStyle: String = "Jeans", // Jeans, Shorts, Trousers, Skirt, Cargo, RobeBottom
    val bottomColor: String = "#1A237E",
    val hatStyle: String = "None", // None, Red Cap, Blue Cap, Wizard Hat, Crown, Straw Hat, Cowboy Hat, Beret, Headband
    val shoeStyle: String = "Sneakers", // Sneakers, Boots, Athletic
    val shoeColor: String = "#3E2723",
    val accessory: String = "None" // Glasses, Sunglasses, None
)

object AvatarOptions {
    val genders = listOf("Boy", "Girl")
    val skinColors = listOf("#FFD180", "#FFE0B2", "#D7B594", "#E0AC69", "#8D6E63", "#5D4037", "#3C2E2A", "#2E1D13")
    val faceShapes = listOf("Round", "Oval", "Square", "Narrow", "Heart")
    val eyeStyles = listOf("Round", "Large", "Almond", "Narrow", "Happy")
    val eyeColors = listOf("#6D4C41", "#3E2723", "#42A5F5", "#66BB6A", "#8D6E63", "#94A3B8")
    val eyebrowsStyles = listOf("Natural", "Thick", "Thin", "Curved", "Straight")
    val noseStyles = listOf("Small", "Button", "Defined", "Triangular")
    val mouthStyles = listOf("Smile", "Big Smile", "Small Smile", "Neutral", "Surprised")
    
    val hairStylesBoy = listOf("Bald", "Messy", "Spiky", "Short", "Side Part", "Buzz Cut", "Curly Fade", "Undercut", "Afro", "Top Knot")
    val hairStylesGirl = listOf("Bob", "Long Straight", "Long Wavy", "Ponytail", "Braids", "Bun", "Pixie", "Afro Puffs", "Pigtails", "Space Buns")
    
    val hairColors = listOf("#000000", "#3E2723", "#6D4C41", "#A1887F", "#F4A460", "#FBC02D", "#E53935", "#D84315", "#42A5F5", "#7E57C2")
    
    val outfits = listOf("Everyday", "Explorer", "Knight", "Wizard", "Archer", "School", "Sports", "Scientist")
    val outfitColors = listOf("#1E88E5", "#E53935", "#43A047", "#FB8C00", "#7E57C2", "#3949AB", "#546E7A", "#212121")
    
    val bottomStyles = listOf("Jeans", "Shorts", "Trousers", "Skirt", "Cargo Pants", "Robe Bottom")
    val bottomColors = listOf("#1A237E", "#37474F", "#2E7D32", "#C62828", "#EF6C00", "#4527A0", "#3E2723", "#111111")
    
    val hatStyles = listOf("None", "Baseball Cap", "Wizard Hat", "Noble Crown", "Straw Hat", "Cowboy Hat", "Beret", "Explorer Helmet", "Headband")
    
    val shoeStyles = listOf("Sneakers", "Boots", "Athletic", "Casual")
    val accessories = listOf("None", "Glasses", "Sunglasses")
}
