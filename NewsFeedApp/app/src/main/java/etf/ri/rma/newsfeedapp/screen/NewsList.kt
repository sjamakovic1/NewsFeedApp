package etf.ri.rma.newsfeedapp.screen


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.model.FilterData


@Composable
fun NewsList(novosti: List<NewsItem>,filterData: FilterData,navController: NavController){
    if(novosti.isEmpty()){
        MessageCard(message = "Nema pronađenih vijesti u kategoriji [${filterData.kategorija}].")
    } else {
        LazyColumn (
            modifier = Modifier.fillMaxSize().testTag("news_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) { items(novosti, key = {it.uuid}) {
                item -> if(item.isFeatured){
            FeaturedNewsCard(novost = item, navController,filterData)
        } else {
            StandardNewsCard(novost = item,navController,filterData)
        }
        }
        }
    }
}