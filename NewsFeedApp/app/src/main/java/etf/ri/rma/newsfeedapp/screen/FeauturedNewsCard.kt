package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ba.etfrma.newsfeedapp.R
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.model.FilterData
import etf.ri.rma.newsfeedapp.model.NewsItem



@Composable
fun FeaturedNewsCard(novost: NewsItem,navController: NavController=rememberNavController(),filterData: FilterData? = null){
    Card(
        modifier = Modifier.fillMaxWidth().clickable{
            navController.currentBackStackEntry?.savedStateHandle?.set("activeFilter", filterData?: FilterData("Sve", null, null,listOf()))
            navController.navigate("/details/${novost.uuid}")},
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ){
        Column (modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = novost.imageUrl,
                    placeholder = painterResource(id = R.drawable.placeholder),
                    error = painterResource(id = R.drawable.placeholder)
                ),
                contentDescription = "Image",
                modifier = Modifier
                    .height(180.dp)
                    .align(Alignment.Start),
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = novost.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = novost.snippet,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${novost.source} • ${novost.publishedDate}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

        }
    }
}