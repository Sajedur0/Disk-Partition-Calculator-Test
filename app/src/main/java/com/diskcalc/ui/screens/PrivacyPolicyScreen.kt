package com.diskcalc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.diskcalc.R
import com.diskcalc.core.util.AppActions
import com.diskcalc.ui.components.SectionCard
import com.diskcalc.ui.text.AppStrings
import com.diskcalc.ui.theme.Spacing

private val sectionIcons = listOf(
    R.drawable.ic_info,
    R.drawable.ic_folder_shared,
    R.drawable.ic_settings_suggest,
    R.drawable.ic_security,
    R.drawable.ic_hub,
    R.drawable.ic_verified_user,
    R.drawable.ic_child_care,
    R.drawable.ic_update,
    R.drawable.ic_gavel,
)

/** Full privacy policy, text carried over verbatim from the original app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text(text = AppStrings.Privacy.TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = Spacing.screenTop),
        ) {
            HeaderCard()

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = AppStrings.Privacy.EFFECTIVE_DATE,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppStrings.Privacy.sections.forEachIndexed { index, section ->
                PolicySection(
                    iconRes = sectionIcons.getOrElse(index) { R.drawable.ic_info },
                    title = section.title,
                    content = section.content,
                    bullets = section.bullets,
                    footer = section.footer,
                )
                if (index != AppStrings.Privacy.sections.lastIndex) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.primaryContainer.copy(alpha = 0.4f),
            ) {
                Text(
                    text = AppStrings.Privacy.consent,
                    modifier = Modifier.padding(Spacing.cardPadding),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            ContactCard(
                onAppClick = { AppActions.openLink(context, AppStrings.Store.PLAY_STORE_URL) },
                onDeveloperClick = {
                    AppActions.openLink(context, AppStrings.Store.DEVELOPER_PAGE_URL)
                },
                onEmailClick = { AppActions.openLink(context, AppStrings.Store.MAILTO_URI) },
            )

            Spacer(modifier = Modifier.height(Spacing.screenBottom))
        }
    }
}

@Composable
private fun HeaderCard() {
    val colorScheme = MaterialTheme.colorScheme

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(modifier = Modifier.width(Spacing.cardPadding))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppStrings.Privacy.TITLE,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppStrings.APP_NAME,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PolicySection(
    iconRes: Int,
    title: String,
    content: String,
    bullets: List<String>,
    footer: String?,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(Spacing.itemGap))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.itemGap))

        PolicyBodyText(text = content)

        if (bullets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            bullets.forEach { bullet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_circle_bullet),
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(7.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    PolicyBodyText(text = bullet)
                }
            }
        }

        if (footer != null) {
            Spacer(modifier = Modifier.height(10.dp))
            PolicyBodyText(text = footer)
        }
    }
}

@Composable
private fun PolicyBodyText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ContactCard(
    onAppClick: () -> Unit,
    onDeveloperClick: () -> Unit,
    onEmailClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mail),
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.itemGap))
                Text(
                    text = AppStrings.Privacy.CONTACT_TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.cardPadding))

            ContactRow(
                label = AppStrings.Privacy.CONTACT_APP,
                value = AppStrings.APP_NAME,
                onClick = onAppClick,
            )
            Spacer(modifier = Modifier.height(Spacing.itemGap))
            ContactRow(
                label = AppStrings.Privacy.CONTACT_DEVELOPER,
                value = AppStrings.Privacy.DEVELOPER_NAME,
                onClick = onDeveloperClick,
            )
            Spacer(modifier = Modifier.height(Spacing.itemGap))
            ContactRow(
                label = AppStrings.Privacy.CONTACT_EMAIL,
                value = AppStrings.Privacy.DEVELOPER_EMAIL,
                onClick = onEmailClick,
            )
        }
    }
}

@Composable
private fun ContactRow(label: String, value: String, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(Spacing.tight))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
            ),
            color = colorScheme.primary,
        )
    }
}
