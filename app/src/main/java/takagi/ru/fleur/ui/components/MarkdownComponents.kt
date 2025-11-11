package takagi.ru.fleur.ui.components

import android.webkit.WebView
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

/**
 * Minimal, stable Markdown + HTML renderer components used by the app.
 * - Caches rendered Markdown via produceState to avoid repeated main-thread parsing.
 * - Truncates previews to avoid heavy work in compose previews.
 * - Provides a simple Markdown toggle card for compose preview UI.
 */

@Composable
fun MarkdownPreview(
	bodyMarkdown: String,
	modifier: Modifier = Modifier,
	maxPreviewChars: Int = 500,
	enableMarkdown: Boolean = true,
) {
	val truncated = remember(bodyMarkdown, maxPreviewChars) {
		if (maxPreviewChars > 0 && bodyMarkdown.length > maxPreviewChars) {
			bodyMarkdown.substring(0, maxPreviewChars) + "…"
		} else bodyMarkdown
	}

	val context = LocalContext.current
	val textColor = LocalContentColor.current
	
	// Configure Markwon with plugins and proper rendering
	val markwon = remember(context) {
		Markwon.builder(context)
			.usePlugin(StrikethroughPlugin.create())
			.usePlugin(TablePlugin.create(context))
			.usePlugin(TaskListPlugin.create(context))
			.usePlugin(LinkifyPlugin.create())
			.build()
	}

	// Render markdown off the main composable recomposition path to avoid ANR.
	val rendered by produceState<CharSequence>(initialValue = truncated, key1 = truncated, key2 = enableMarkdown) {
		if (!enableMarkdown) {
			value = truncated
		} else {
			// do CPU-bound parsing off the main dispatcher
			value = withContext(Dispatchers.Default) { markwon.toMarkdown(truncated) }
		}
	}

	val colorArgb = textColor.toArgb()

	AndroidView(
		factory = { ctx ->
			TextView(ctx).apply {
				isClickable = false
				isFocusable = false
				setTextIsSelectable(true)
				// Set text appearance to ensure proper styling
				setTextAppearance(android.R.style.TextAppearance_Material_Body1)
			}
		},
		update = { tv: TextView ->
			// Set text color based on theme
			tv.setTextColor(colorArgb)
			tv.text = rendered
		},
		modifier = modifier
	)
}


@Composable
fun MarkdownPreviewCard(
	markdown: String,
	enableMarkdown: Boolean,
	onMarkdownToggle: (Boolean) -> Unit,
	onClick: () -> Unit = {},
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		Row(modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
			Text(text = "Markdown")
			Switch(checked = enableMarkdown, onCheckedChange = onMarkdownToggle)
		}

		Card(modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp)) {
			// preview truncated to avoid heavy parsing in compose sheet
			Box(modifier = Modifier
				.fillMaxWidth()
				.clickable { onClick() }
			) {
				MarkdownPreview(bodyMarkdown = markdown, maxPreviewChars = 500, enableMarkdown = enableMarkdown, modifier = Modifier.padding(12.dp))
			}
		}
	}
}


@Composable
fun HtmlEmailViewer(
	html: String,
	modifier: Modifier = Modifier,
) {
	val sanitized = remember(html) {
		Jsoup.clean(html, Safelist.relaxed())
	}

	AndroidView(factory = { ctx ->
		WebView(ctx).apply {
			settings.javaScriptEnabled = false
			// minimal settings to render sanitized HTML
			settings.loadsImagesAutomatically = true
			settings.defaultTextEncodingName = "utf-8"
		}
	}, update = { web ->
		web.loadDataWithBaseURL(null, sanitized, "text/html", "utf-8", null)
	}, modifier = modifier)
}


@Composable
fun EmailContentRenderer(
	bodyPlain: String? = null,
	bodyText: String? = null, // alias for callers still using bodyText
	bodyMarkdown: String?,
	bodyHtml: String?,
	modifier: Modifier = Modifier,
	enableMarkdownForPreview: Boolean = true,
) {
	val plain = bodyPlain ?: bodyText

	when {
		!bodyHtml.isNullOrBlank() -> {
			HtmlEmailViewer(html = bodyHtml, modifier = modifier)
		}

		!bodyMarkdown.isNullOrBlank() -> {
			// full markdown rendering in detail view (no truncation)
			MarkdownPreview(bodyMarkdown = bodyMarkdown, maxPreviewChars = Int.MAX_VALUE, enableMarkdown = enableMarkdownForPreview, modifier = modifier.padding(8.dp))
		}

		else -> {
			Text(text = plain ?: "", modifier = modifier.padding(8.dp))
		}
	}
}
