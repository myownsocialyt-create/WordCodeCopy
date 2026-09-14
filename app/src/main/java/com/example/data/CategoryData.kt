package com.example.data

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val words: List<String>
)

object CategoryRepository {
    val CATEGORIES: List<Category> = listOf(
        Category(
            id = "animals",
            name = "Animals",
            emoji = "🐾",
            words = listOf(
                "CAT", "DOG", "LION", "TIGER", "ELEPHANT", "GIRAFFE", "ZEBRA", "MONKEY",
                "PENGUIN", "DOLPHIN", "EAGLE", "WOLF", "BEAR", "FOX", "RABBIT", "SNAKE",
                "TURTLE", "HORSE"
            )
        ),
        Category(
            id = "food",
            name = "Food",
            emoji = "🍕",
            words = listOf(
                "PIZZA", "BURGER", "SUSHI", "PASTA", "SALAD", "RICE", "BREAD", "SOUP",
                "TACO", "WAFFLE", "PANCAKE", "NOODLE", "STEAK", "CHEESE", "COOKIE"
            )
        ),
        Category(
            id = "sports",
            name = "Sports",
            emoji = "⚽",
            words = listOf(
                "SOCCER", "TENNIS", "BASKETBALL", "BASEBALL", "SWIMMING", "CYCLING",
                "BOXING", "GOLF", "RUGBY", "CRICKET", "HOCKEY", "RUNNING", "SKIING", "SURFING"
            )
        ),
        Category(
            id = "countries",
            name = "Countries",
            emoji = "🌍",
            words = listOf(
                "FRANCE", "JAPAN", "BRAZIL", "CANADA", "INDIA", "CHINA", "SPAIN", "ITALY",
                "EGYPT", "MEXICO", "RUSSIA", "AUSTRALIA", "GERMANY", "NIGERIA", "THAILAND"
            )
        ),
        Category(
            id = "colors",
            name = "Colors",
            emoji = "🎨",
            words = listOf(
                "RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE", "PINK", "BLACK",
                "WHITE", "BROWN", "GRAY", "CYAN", "GOLD", "SILVER", "VIOLET"
            )
        ),
        Category(
            id = "fruits",
            name = "Fruits",
            emoji = "🍎",
            words = listOf(
                "APPLE", "MANGO", "BANANA", "GRAPE", "LEMON", "PEACH", "PLUM", "ORANGE",
                "CHERRY", "MELON", "PAPAYA", "GUAVA", "KIWI", "PEAR", "BERRY"
            )
        ),
        Category(
            id = "space",
            name = "Space",
            emoji = "🚀",
            words = listOf(
                "MOON", "STAR", "SUN", "PLANET", "COMET", "GALAXY", "NEBULA", "ASTEROID",
                "ORBIT", "SATURN", "JUPITER", "MARS", "VENUS", "MERCURY", "COSMOS"
            )
        ),
        Category(
            id = "ocean_life",
            name = "Ocean Life",
            emoji = "🌊",
            words = listOf(
                "SHARK", "WHALE", "OCTOPUS", "CRAB", "LOBSTER", "SHRIMP", "CORAL", "JELLYFISH",
                "SEAHORSE", "CLAM", "OTTER", "SEAL", "TUNA", "SQUID"
            )
        ),
        Category(
            id = "music",
            name = "Music",
            emoji = "🎵",
            words = listOf(
                "GUITAR", "PIANO", "DRUMS", "VIOLIN", "TRUMPET", "FLUTE", "BASS", "RHYTHM",
                "MELODY", "CHORD", "TEMPO", "JAZZ", "ROCK", "BLUES", "OPERA"
            )
        ),
        Category(
            id = "movies",
            name = "Movies",
            emoji = "🎬",
            words = listOf(
                "ACTION", "DRAMA", "COMEDY", "HORROR", "ROMANCE", "THRILLER", "FANTASY",
                "MYSTERY", "WESTERN", "SEQUEL", "TRAILER", "CINEMA", "ACTOR", "SCENE"
            )
        ),
        Category(
            id = "nature",
            name = "Nature",
            emoji = "🌿",
            words = listOf(
                "FOREST", "RIVER", "MOUNTAIN", "OCEAN", "DESERT", "JUNGLE", "VALLEY", "ISLAND",
                "VOLCANO", "GLACIER", "PRAIRIE", "CANYON", "MARSH", "TUNDRA"
            )
        ),
        Category(
            id = "technology",
            name = "Technology",
            emoji = "💻",
            words = listOf(
                "COMPUTER", "INTERNET", "ROBOT", "PHONE", "TABLET", "KEYBOARD", "MONITOR",
                "BATTERY", "CIRCUIT", "CAMERA", "LASER", "SERVER", "NETWORK", "SOFTWARE"
            )
        ),
        Category(
            id = "jobs",
            name = "Jobs",
            emoji = "👔",
            words = listOf(
                "DOCTOR", "TEACHER", "PILOT", "CHEF", "LAWYER", "NURSE", "ENGINEER", "ARTIST",
                "FARMER", "WRITER", "POLICE", "FIREFIGHTER", "ARCHITECT", "SCIENTIST"
            )
        ),
        Category(
            id = "clothing",
            name = "Clothing",
            emoji = "👗",
            words = listOf(
                "SHIRT", "PANTS", "DRESS", "JACKET", "SHOES", "SOCKS", "HAT", "SCARF",
                "GLOVES", "BELT", "BOOTS", "COAT", "SKIRT", "SWEATER", "SHORTS"
            )
        ),
        Category(
            id = "vegetables",
            name = "Vegetables",
            emoji = "🥕",
            words = listOf(
                "CARROT", "POTATO", "TOMATO", "ONION", "GARLIC", "PEPPER", "CORN", "BROCCOLI",
                "SPINACH", "CABBAGE", "CELERY", "RADISH", "PUMPKIN", "ZUCCHINI"
            )
        )
    )

    fun getCategoryById(id: String): Category? {
        return CATEGORIES.find { it.id == id }
    }

    fun getNextCategory(currentId: String): Category? {
        val currentIndex = CATEGORIES.indexOfFirst { it.id == currentId }
        return if (currentIndex in 0 until CATEGORIES.size - 1) {
            CATEGORIES[currentIndex + 1]
        } else null
    }
}
