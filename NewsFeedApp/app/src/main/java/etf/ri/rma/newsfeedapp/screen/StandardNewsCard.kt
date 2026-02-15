package etf.ri.rma.newsfeedapp.screen


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import etf.ri.rma.newsfeedapp.model.NewsItem
import ba.etfrma.newsfeedapp.R
import coil.compose.rememberAsyncImagePainter
import etf.ri.rma.newsfeedapp.model.FilterData


@Composable
fun StandardNewsCard(novost: NewsItem,navController: NavController=rememberNavController(),filterData: FilterData? = null){
    Card(
        modifier = Modifier.fillMaxWidth().clickable{
            navController.currentBackStackEntry?.savedStateHandle?.set("activeFilter", filterData?: FilterData("Sve", null, null, listOf()))
            navController.navigate("/details/${novost.uuid}")},
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top){
            Image(
                painter = rememberAsyncImagePainter(
                    model = novost.imageUrl,
                    placeholder = painterResource(id = R.drawable.placeholder),
                    error = painterResource(id = R.drawable.placeholder)
                ),
                contentDescription = "Image",
                modifier = Modifier
                    .size(80.dp)
                    .padding(top = 4.dp),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column (modifier = Modifier.fillMaxWidth()){
                Text(
                    text = novost.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = novost.snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${novost.source} • ${novost.publishedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
