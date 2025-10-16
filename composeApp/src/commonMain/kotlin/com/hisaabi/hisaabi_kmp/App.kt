package com.hisaabi.hisaabi_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hisaabi.hisaabi_kmp.auth.AuthNavigation
import com.hisaabi.hisaabi_kmp.home.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
            var selectedPartySegment by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment?>(null) }
            var addPartyType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType?>(null) }
            var partiesRefreshTrigger by remember { mutableStateOf(0) }
            
            // Category navigation state
            var categoryType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType?>(null) }
            var categoriesRefreshTrigger by remember { mutableStateOf(0) }
            var selectedCategoryForParty by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity?>(null) }
            var selectedAreaForParty by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity?>(null) }
            var returnToAddParty by remember { mutableStateOf(false) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToAuth = { currentScreen = AppScreen.AUTH },
                        onNavigateToParties = { segment ->
                            selectedPartySegment = segment
                            currentScreen = AppScreen.PARTIES
                        }
                    )
                }
                AppScreen.AUTH -> {
                    AuthNavigation(
                        onNavigateToMain = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.PARTIES -> {
                    com.hisaabi.hisaabi_kmp.parties.presentation.ui.PartiesScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onPartyClick = { /* TODO: Navigate to party details */ },
                        onAddPartyClick = {
                            // Determine party type based on current segment
                            addPartyType = when (selectedPartySegment) {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.CUSTOMER
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.VENDOR
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.INVESTOR -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.INVESTOR
                                else -> com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.CUSTOMER
                            }
                            currentScreen = AppScreen.ADD_PARTY
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        initialSegment = selectedPartySegment,
                        refreshTrigger = partiesRefreshTrigger
                    )
                }
                AppScreen.ADD_PARTY -> {
                    addPartyType?.let { type ->
                        com.hisaabi.hisaabi_kmp.parties.presentation.ui.AddPartyScreen(
                            viewModel = org.koin.compose.koinInject(),
                            partyType = type,
                            onNavigateBack = { 
                                if (!returnToAddParty) {
                                    partiesRefreshTrigger++  // Trigger refresh
                                    currentScreen = AppScreen.PARTIES
                                }
                                returnToAddParty = false
                            },
                            onNavigateToCategories = {
                                categoryType = com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.CUSTOMER_CATEGORY
                                returnToAddParty = true
                                currentScreen = AppScreen.CATEGORIES
                            },
                            onNavigateToAreas = {
                                categoryType = com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.AREA
                                returnToAddParty = true
                                currentScreen = AppScreen.CATEGORIES
                            },
                            selectedCategoryFromNav = selectedCategoryForParty,
                            selectedAreaFromNav = selectedAreaForParty
                        )
                    }
                }
                
                AppScreen.CATEGORIES -> {
                    categoryType?.let { type ->
                        com.hisaabi.hisaabi_kmp.categories.presentation.ui.CategoriesScreen(
                            viewModel = org.koin.compose.koinInject(),
                            categoryType = type,
                            onCategorySelected = { category ->
                                // Store selected category based on type
                                if (type == com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.CUSTOMER_CATEGORY) {
                                    category?.let {
                                        selectedCategoryForParty = com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity(
                                            id = it.id,
                                            title = it.title,
                                            description = it.description,
                                            thumbnail = it.thumbnail,
                                            type_id = it.typeId,
                                            slug = it.slug,
                                            business_slug = it.businessSlug,
                                            created_by = it.createdBy,
                                            sync_status = it.syncStatus,
                                            created_at = it.createdAt,
                                            updated_at = it.updatedAt
                                        )
                                    }
                                } else if (type == com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.AREA) {
                                    category?.let {
                                        selectedAreaForParty = com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity(
                                            id = it.id,
                                            title = it.title,
                                            description = it.description,
                                            thumbnail = it.thumbnail,
                                            type_id = it.typeId,
                                            slug = it.slug,
                                            business_slug = it.businessSlug,
                                            created_by = it.createdBy,
                                            sync_status = it.syncStatus,
                                            created_at = it.createdAt,
                                            updated_at = it.updatedAt
                                        )
                                    }
                                }
                                currentScreen = AppScreen.ADD_PARTY
                            },
                            onAddCategoryClick = {
                                currentScreen = AppScreen.ADD_CATEGORY
                            },
                            onNavigateBack = {
                                if (returnToAddParty) {
                                    currentScreen = AppScreen.ADD_PARTY
                                } else {
                                    currentScreen = AppScreen.PARTIES
                                }
                            },
                            refreshTrigger = categoriesRefreshTrigger
                        )
                    }
                }
                
                AppScreen.ADD_CATEGORY -> {
                    categoryType?.let { type ->
                        com.hisaabi.hisaabi_kmp.categories.presentation.ui.AddCategoryScreen(
                            viewModel = org.koin.compose.koinInject(),
                            categoryType = type,
                            onNavigateBack = {
                                categoriesRefreshTrigger++  // Trigger refresh
                                currentScreen = AppScreen.CATEGORIES
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class AppScreen {
    HOME,
    AUTH,
    PARTIES,
    ADD_PARTY,
    CATEGORIES,
    ADD_CATEGORY
}