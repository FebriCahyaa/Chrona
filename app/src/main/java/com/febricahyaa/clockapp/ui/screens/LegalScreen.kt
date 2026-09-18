/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.febricahyaa.clockapp.legal.LegalContent
import com.febricahyaa.clockapp.ui.components.ChronaScaffold

/** Which page of the Legal & Regulatory hub is currently shown. */
private enum class LegalPage { HUB, NOTICES, LICENSE }

/**
 * Universal legal destination used by the main Chrona navigation stack.
 *
 * The document body remains a regular Compose scroll container so the same
 * LargeTopAppBar / nested-scroll contract is used as every other destination.
 */
@Composable
fun LegalScreen(onBack: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(LegalPage.HUB) }

    val title = when (page) {
        LegalPage.HUB -> "Legal & Info"
        LegalPage.NOTICES -> "Legal Notices"
        LegalPage.LICENSE -> "Licenses"
    }
    val subtitle = when (page) {
        LegalPage.HUB -> "Copyright, notices, and third-party licenses"
        LegalPage.NOTICES -> "Chrona legal and regulatory notices"
        LegalPage.LICENSE -> "Open-source and third-party attribution"
    }

    ChronaScaffold(
        title = title,
        subtitle = subtitle,
        onBack = {
            if (page == LegalPage.HUB) onBack() else page = LegalPage.HUB
        },
    ) { paddingValues ->
        when (page) {
            LegalPage.HUB -> LegalHub(
                onOpenNotices = { page = LegalPage.NOTICES },
                onOpenLicense = { page = LegalPage.LICENSE },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            LegalPage.NOTICES -> LegalDocument(
                body = LegalContent.legalNotices,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
            LegalPage.LICENSE -> LegalDocument(
                body = LegalContent.license,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        }
    }
}

/**
 * Full-screen legal document viewer, opened from Settings.
 * Mirrors the platform pattern of Settings > General > Legal & Regulatory:
 * a grouped list of documents, each opening into a plain scrollable text page.
 */
@Composable
fun LegalDialog(onDismiss: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(LegalPage.HUB) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                LegalTopBar(
                    title = when (page) {
                        LegalPage.HUB -> "Legal & Pengatur"
                        LegalPage.NOTICES -> "Pemberitahuan Legal"
                        LegalPage.LICENSE -> "Lisensi"
                    },
                    onBack = { if (page == LegalPage.HUB) onDismiss() else page = LegalPage.HUB },
                )
                when (page) {
                    LegalPage.HUB -> LegalHub(onOpenNotices = { page = LegalPage.NOTICES }, onOpenLicense = { page = LegalPage.LICENSE })
                    LegalPage.NOTICES -> LegalDocument(LegalContent.legalNotices)
                    LegalPage.LICENSE -> LegalDocument(LegalContent.license)
                }
            }
        }
    }
}

@Composable
private fun LegalTopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(onClick = onBack, modifier = Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Kembali", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.size(12.dp))
        Text(title, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegalHub(onOpenNotices: () -> Unit, onOpenLicense: () -> Unit, modifier: Modifier = Modifier) {
    val rows = listOf(
        "Pemberitahuan Legal" to onOpenNotices,
        "Lisensi" to onOpenLicense,
    )
    Column(modifier.padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(10.dp))
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        ) {
            Column {
                rows.forEachIndexed { index, (label, onClick) ->
                    LegalRow(label, onClick)
                    if (index != rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(start = 18.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(6.dp))
            Text(
                "Dokumen legal Chrona, termasuk pemberitahuan hak cipta dan lisensi penggunaan aplikasi.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegalRow(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun LegalDocument(body: String, modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            body,
            fontSize = 13.sp,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
        )
        Spacer(Modifier.height(36.dp))
    }
}
