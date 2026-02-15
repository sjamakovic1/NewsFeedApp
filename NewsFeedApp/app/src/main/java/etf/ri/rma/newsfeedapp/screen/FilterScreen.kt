package etf.ri.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.network.AppRepository
import etf.ri.rma.newsfeedapp.model.FilterData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun FilterScreen(navController: NavController,defaultKategorija: String="Sve"){
    val parentEntry = navController.previousBackStackEntry
    val savedStateHandle =parentEntry?.savedStateHandle
    val defaultFilterData = savedStateHandle?.get<FilterData>("filter_data_default")

    val scope = rememberCoroutineScope()

    val kategorije = listOf(
        "general" to "filter_chip_all",
        "politics" to "filter_chip_politics",
        "sports" to "filter_chip_sports",
        "tech" to "filter_chip_tech",
        "science" to "filter_chip_science",
        "business" to "filter_chip_business",
    )


    var aktivnaKategorija by remember { mutableStateOf(defaultFilterData?.kategorija ?: "Sve") }
    var unwantedWord by remember { mutableStateOf("") }
    var unwantedWords by remember { mutableStateOf(defaultFilterData?.unwantedWords ?: listOf()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = defaultFilterData?.startDate?.let { parseDateToMillis(it) },
        initialSelectedEndDateMillis = defaultFilterData?.endDate?.let { parseDateToMillis(it) }
    )

    val selectedStart = dateRangeState.selectedStartDateMillis?.let { millisToDate(it) }
    val selectedEnd = dateRangeState.selectedEndDateMillis?.let { millisToDate(it) }
    val displayText = if(selectedStart != null && selectedEnd !=null) "$selectedStart;$selectedEnd" else "Odaberite raspon datuma"



    BackHandler {
        savedStateHandle?.set(
            "selectedFilters", defaultFilterData
        )
        navController.popBackStack()
    }


    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()).padding(WindowInsets.statusBars.asPaddingValues())
    ){
        Row (
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            kategorije.forEach { (kategorija, testTag) ->
                FilterChip(
                    selected = aktivnaKategorija == kategorija,
                    modifier = testTag?.let { Modifier.testTag(it) } ?: Modifier,
                    onClick = {
                        if (aktivnaKategorija != kategorija) {
                            aktivnaKategorija = kategorija
                           scope.launch {
                                AppRepository.newsDAO.getTopStoriesByCategory(aktivnaKategorija)
                            }
                        }
                    },
                    label = { Text(
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
                    ) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if(aktivnaKategorija==kategorija)
                            MaterialTheme.colorScheme.primary.copy(alpha=0.2f)
                        else
                            MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row (horizontalArrangement = Arrangement.spacedBy(12.dp)){
            Text(
                text = displayText,
                modifier = Modifier.weight(1f).testTag("filter_daterange_display").padding(top=10.dp)
            )
            FilterChip(
                selected = false,
                onClick = {showDatePicker = true},
                label = {Text("Odaberi datume")},
                modifier = Modifier.testTag("filter_daterange_button")
            )
        }
        if(showDatePicker){
            Dialog(onDismissRequest =  {showDatePicker = false} ) {
                Surface (shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().heightIn(min=400.dp,max=600.dp)){
                    Column (modifier = Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween){
                        DateRangePicker(
                            state = dateRangeState,
                            title = { Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), contentAlignment = Alignment.Center){
                                Text("Odaberite raspon datuma", style = MaterialTheme.typography.titleMedium)
                            } },
                            headline = {
                                val start = dateRangeState.selectedStartDateMillis?.let{millisToDate(it)} ?: "Početni datum"
                                val end = dateRangeState.selectedEndDateMillis?.let{millisToDate(it)} ?: "Krajnji datum"
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text("$start - $end",style= MaterialTheme.typography.bodyMedium)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
                            TextButton(onClick = {dateRangeState.setSelection(null,null)
                            showDatePicker = false}) { Text("Otkaži")}
                            TextButton(onClick = {showDatePicker=false}) {Text("Ok") }
                         }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)){
            TextField(
                value = unwantedWord,
                onValueChange = {unwantedWord = it},
                label = {Text("Unesi riječ: ")},
                modifier = Modifier.weight(1f).testTag("filter_unwanted_input")
            )
            Button(
                onClick = {
                    val normalized = unwantedWord.trim().lowercase()
                    if(normalized.isNotBlank() && normalized !in unwantedWords.map {it.lowercase()}){
                    unwantedWords = unwantedWords + unwantedWord.trim()
                        unwantedWord=""
                    }
                },
                modifier = Modifier.testTag("filter_unwanted_add_button")
            ) { Text("Dodaj")}}

            Spacer(modifier = Modifier.height(12.dp))
        Text("Nepoželjne riječi:")
      LazyColumn (modifier = Modifier.heightIn(max=500.dp).testTag("filter_unwanted_list"),
           verticalArrangement = Arrangement.spacedBy(4.dp)){
           items(unwantedWords) {word ->
              Text(
                  text = word,
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.clickable{unwantedWords= unwantedWords-word}.padding(vertical=4.dp)
              )
          }
       }

        Spacer(modifier = Modifier.weight(1f))
        Row (modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End) {
            Button(
                modifier = Modifier.fillMaxWidth().testTag("filter_apply_button"),
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedFilters", FilterData(
                        kategorija = aktivnaKategorija,
                        startDate = selectedStart,
                        endDate = selectedEnd,
                        unwantedWords = unwantedWords
                    ))
                    navController.popBackStack()
                }) { Text("Primjeni filtere") }
        }

    }
}

fun parseDateToMillis(datum: String): Long? {
    return try {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.parse(datum)?.time
    } catch (e: Exception) {
        null
    }
}
fun millisToDate(millis: Long): String {
    val sdf= SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}
fun mapDisplayCategoryToApi(display: String): String {
    return when (display) {
        "Politika" -> "politics"
        "Sport" -> "sports"
        "Tehnologija" -> "tech"
        "Nauka" -> "science"
        "Ekonomija" -> "business"
        "Sve" -> "general"
        else -> "general"
    }
}
