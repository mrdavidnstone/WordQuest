package com.spellarade.core.domain.model

/**
 * Shared vocabulary / spelling entry.
 *
 * Per product requirements:
 * - Word
 * - Required definition
 *
 * Do not add category, example sentence, or difficulty fields
 * unless explicitly approved.
 */
data class Word(
    val id: String,
    val text: String,
    val definition: String,
)

/**
 * A named collection of words that can be selected for any game.
 */
data class WordList(
    val id: String,
    val name: String,
    val wordIds: List<String>,
)
