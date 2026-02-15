package etf.ri.rma.newsfeedapp.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.navigation.NavController
import ba.etfrma.newsfeedapp.R
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.data.NewsDatabase
import etf.ri.rma.newsfeedapp.data.toNewsItem
import etf.ri.rma.newsfeedapp.data.network.AppRepository
import etf.ri.rma.newsfeedapp.model.FilterData
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NewsDetailsScreen(id: String, navController: NavController) {

    val context = LocalContext.current
    val savedNewsDao = remember { NewsDatabase.getDatabase(context).savedNewsDAO() }


    var vijest by remember { mutableStateOf<NewsItem?>(null) }


    LaunchedEffect(id) {
        val record = savedNewsDao.allNews().find { it.news.uuid == id }
        vijest = record
            ?.toNewsItem()
            ?: AppRepository.newsDAO.getAllStories().find { it.uuid == id }
    }

    val item = vijest ?: return


    val parentEntry = navController.previousBackStackEntry
    val savedStateHandle = parentEntry?.savedStateHandle
    val filterData = savedStateHandle?.get<FilterData>("activeFilter")


    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var related by remember { mutableStateOf<List<NewsItem>>(emptyList()) }


    LaunchedEffect(item.uuid) {
        related = try {
            withContext(Dispatchers.IO) {
                AppRepository.newsDAO.getSimilarStories(item.uuid)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(item.uuid, item.imageUrl) {
        withContext(Dispatchers.IO) {
            // pokušaj iz baze
            var rec = savedNewsDao.allNews().find { it.news.uuid == item.uuid }
            if (rec?.imageTags?.isNotEmpty() == true) {
                tags = rec.imageTags.map { it.value }
                return@withContext
            }

            // fetch tagova (ili prazan list kad si offline)
            val fetched = try {
                item.imageUrl?.let { AppRepository.imagaDAO.getTags(it) } ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
            tags = fetched

            // pohrani vijest ako je još nema, pa tagove
            if (rec == null) {
                savedNewsDao.saveNews(item)
                rec = savedNewsDao.allNews().find { it.news.uuid == item.uuid }
            }
            rec?.news?.id?.let { id ->
                savedNewsDao.addTags(fetched,id)
            }
        }
    }



    BackHandler {
        savedStateHandle?.set("selectedFilters", filterData)
        navController.popBackStack("/home", inclusive = false)
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = item.imageUrl,
                        placeholder = painterResource(R.drawable.placeholder),
                        error = painterResource(R.drawable.placeholder)
                    ),
                    contentDescription = "Slika vijesti",
                    modifier = Modifier
                        .size(350.dp)
                        .padding(top = 4.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        item {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("details_title")
            )
        }

        item {
            Text(
                text = item.snippet,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("details_snippet")
            )
        }

        item {
            Text(
                text = "Kategorija: ${item.category}",
                modifier = Modifier.testTag("details_category")
            )
            Text(
                text = "Izvor: ${item.source}",
                modifier = Modifier.testTag("details_source")
            )
            Text(
                text = "Datum: ${item.publishedDate}",
                modifier = Modifier.testTag("details_date")
            )
        }

        item {
            Text(
                text = "Tagovi slike:",
                modifier = Modifier.testTag("details_tags")
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 56.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("details_tag_item")
                    )
                }
            }
        }

        item {
            Text(
                text = "Povezane vijesti iz iste kategorije:",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            related.getOrNull(0)?.let {
                Text(
                    text = it.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.popBackStack()
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("activeFilter", filterData)
                            navController.navigate("/details/${it.uuid}")
                        }
                        .testTag("related_news_title_1"),
                    color = Color(0xFF64B5F6)
                )
            }
            related.getOrNull(1)?.let {
                Text(
                    text = it.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.popBackStack()
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("activeFilter", filterData)
                            navController.navigate("/details/${it.uuid}")
                        }
                        .testTag("related_news_title_2"),
                    color = Color(0xFF64B5F6)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    savedStateHandle?.set("selectedFilters", filterData)
                    navController.popBackStack("/home", inclusive = false)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("details_close_button")
            ) {
                Text("Zatvori detalje")
            }
        }
    }
}
