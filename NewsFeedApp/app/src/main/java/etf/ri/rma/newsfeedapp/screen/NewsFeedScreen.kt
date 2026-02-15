package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import etf.ri.rma.newsfeedapp.data.network.AppRepository
import etf.ri.rma.newsfeedapp.model.FilterData
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewsFeedScreen(navController: NavController = rememberNavController()) {
    val kategorije = listOf(
        "general" to "filter_chip_all",
        "politics" to "filter_chip_politics",
        "sports" to "filter_chip_sports",
        "tech" to "filter_chip_tech",
        "science" to "filter_chip_science",
        "business" to "filter_chip_business",
    )

    var aktivnaKategorija by remember { mutableStateOf("general") }
    var unwantedWords by remember { mutableStateOf(listOf<String>()) }
    var selectedDateRange by remember { mutableStateOf<Pair<String?, String?>>(null to null) }
    var vijesti by remember { mutableStateOf<List<NewsItem>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle?.get<FilterData>("selectedFilters")) {
        savedStateHandle?.get<FilterData>("selectedFilters")?.let { filterData ->
            aktivnaKategorija = filterData.kategorija
            unwantedWords = filterData.unwantedWords
            selectedDateRange = filterData.startDate to filterData.endDate
            savedStateHandle.remove<FilterData>("selectedFilters")
        }
    }

    LaunchedEffect(aktivnaKategorija) {
        scope.launch {
            vijesti = if (aktivnaKategorija == "general") {
                AppRepository.newsDAO.getAllStories()
            } else {
                AppRepository.newsDAO.getTopStoriesByCategory(aktivnaKategorija)
            }
        }
    }


    val filtriraneVijesti = vijesti.filter { vijest ->
        val byKategorija = aktivnaKategorija == "general" || vijest.category.equals(aktivnaKategorija, ignoreCase = true)
        val byDatum = if (!selectedDateRange.first.isNullOrBlank() && !selectedDateRange.second.isNullOrBlank()) {
            compareDates(vijest.publishedDate, selectedDateRange.first) >= 0 &&
                    compareDates(vijest.publishedDate, selectedDateRange.second) <= 0
        } else true
        val byNezeljene = !sadrziNepozeljnuRijec(vijest.title, unwantedWords) &&
                !sadrziNepozeljnuRijec(vijest.snippet, unwantedWords)
        byKategorija && byDatum && byNezeljene
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            kategorije.forEach { (kategorija, testTag) ->
                FilterChip(
                    selected = aktivnaKategorija == kategorija,
                    modifier = testTag?.let { Modifier.testTag(it) } ?: Modifier,
                    onClick = {
                        if (aktivnaKategorija != kategorija) {
                            aktivnaKategorija = kategorija
                        }
                    },
                    label = {
                        Text(
                            text = when (kategorija) {
                                "general" -> "Sve"
                                "politics" -> "Politika"
                                "sports" -> "Sport"
                                "science" -> "Nauka"
                                "tech" -> "Tehnologija"
                                "business" -> "Biznis"
                                "tech" -> "Tehnologija"
                                else -> kategorija.replaceFirstChar { it.uppercase() }
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (aktivnaKategorija == kategorija)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        FilterChip(
            selected = false,
            modifier = Modifier.testTag("filter_chip_more"),
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set("kategorija_default", aktivnaKategorija)
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    "filter_data_default",
                    FilterData(
                        kategorija = aktivnaKategorija,
                        startDate = selectedDateRange.first,
                        endDate = selectedDateRange.second,
                        unwantedWords = unwantedWords
                    )
                )
                navController.navigate("/filters")
            },
            label = { Text(text = "Više filtera ...") },
            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            NewsList(
                novosti = filtriraneVijesti,
                filterData = FilterData(
                    kategorija = aktivnaKategorija,
                    startDate = selectedDateRange.first,
                    endDate = selectedDateRange.second,
                    unwantedWords = unwantedWords
                ),
                navController = navController
            )
        }
    }
}

fun sadrziNepozeljnuRijec(tekst: String, nepozeljne: List<String>): Boolean {
    val rijeciUTekstu = tekst.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
    val nepozeljneMale = nepozeljne.map { it.lowercase() }
    return rijeciUTekstu.any { it in nepozeljneMale }
}

fun compareDates(datumVijesti: String?, datumFiltera: String?): Int {
    return try {
        if (datumVijesti == null || datumFiltera == null) return 0
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val d1 = sdf.parse(datumVijesti)
        val d2 = sdf.parse(datumFiltera)
        if (d1 != null && d2 != null) d1.compareTo(d2) else 0
    } catch (e: Exception) {
        0
    }
}
