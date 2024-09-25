package moe.apex.rule34.image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import moe.apex.rule34.preferences.ImageSource

@Serializable
data class Image(
    val fileName: String,
    val fileFormat: String,
    val previewUrl: String,
    val fileUrl: String,
    val sampleUrl: String,
    val imageSource: ImageSource = ImageSource.R34 // Backwards compatibility
) {
    val highestQualityFormatUrl = fileUrl.takeIf { it.isNotEmpty() } ?: sampleUrl
    var preferHd by mutableStateOf(false)
    var hdQualityOverride: Boolean? by mutableStateOf(null)

    fun toggleHd(to: Boolean? = null) {
        preferHd = when (to) {
            null -> !preferHd
            else -> to
        }
        hdQualityOverride = preferHd
    }
}